/*
Copyright (c) 2022 Arman Jussupgaliyev

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.StreamConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.rms.RecordStore;

public class App implements CommandListener, Constants, Runnable {
	
	static final String ver = "1.2.2";
	
	// Settings
	static String region;
	static String downloadDir;
	static int startScreen; // 0 - Trends 1 - Popular
	static String inv = iteroni;
	static String invProxy = invproxy;
	static int downloadBuffer = 1024;
	
	static final String quality = "144p";
	
	static App inst;
	static AppLite midlet;
	
	static List mainList;
	private static List searchList;
	static Form settingsForm;
	private static Video videoForm;
	private static Channel channelForm;
	
	private static Vector queuedTasks = new Vector();
	private static Object tasksLock = new Object();
	private static Thread tasksThread;

	private Object[] mainItems = new Object[30];
	private Object[] searchItems = new Object[30];

	App() {
		type = 0;
	}

	public void run() {
		switch(type) {
		case 0: // tasks thread
			while(midlet.running) {
				try {
					synchronized (tasksLock) {
						tasksLock.wait();
					}
					while(queuedTasks.size() > 0) {
						try {
							((Runnable)queuedTasks.elementAt(0)).run();
						} catch (Exception e) {
						}
						queuedTasks.removeElementAt(0);
						Thread.yield();
					}
				} catch (InterruptedException e) {
					return;
				}
			}
			break;
		case 1: // downloader
			if(cancel) return;
			StreamConnection fc = null;
			OutputStream out = null;
			HttpConnection hc = null;
			InputStream in = null;
			try {
				String f = id + ".3gp";
				file = file + f;
				label.setText(f);

				JSONObject o = App.getVideoInfo(id);
				String url = o.getString("url");
				//int idx = url.indexOf("/videoplayback");
				//url = App.inv + url.substring(idx+1);

				url = "http://nnp.nnchan.ru/glype/browse.php?u=" + url(url);
				int contentLength = o.getInt("clen", 0);
				// подождать
				Thread.sleep(500);
				fc = Files.createFile(file);
				label.setText("Connecting");
				hc = (HttpConnection) Connector.open(url);
				int r;
				try {
					r = hc.getResponseCode();
				} catch (IOException e) {
					label.setText("Error! Waiting for retry...");
					try {
						hc.close();
					} catch (Exception e2) {
					}
					Thread.sleep(2000);
					label.setText("Connection retry");
					hc = (HttpConnection) Connector.open(url);
					hc.setRequestMethod("GET");
					r = hc.getResponseCode();
				}
				if(cancel) return;
				int redirectCount = 0;
				while (r == 301 || r == 302) {
					label.setText("Reditected ("
							.concat(Integer.toString(redirectCount++))
							.concat(")"));
					String redir = hc.getHeaderField("Location");
					if (redir.startsWith("/")) {
						String tmp = url.substring(url.indexOf("//") + 2);
						String host = url.substring(0, url.indexOf("//"))
								.concat("//")
								.concat(tmp.substring(0, tmp.indexOf("/")));
						redir = host + redir;
					}
					hc.close();
					hc = (HttpConnection) Connector.open(redir);
					hc.setRequestMethod("GET");
					r = hc.getResponseCode();
				}
				if(cancel) return;
				out = fc.openOutputStream();
				in = hc.openInputStream();
				label.setText("Connceted");
				int bufSize = App.downloadBuffer;
				byte[] buf = new byte[bufSize];
				int read = 0;
				int d = 0;
				int l = contentLength;
				if(l <= 0) {
					try {
						l = (int) hc.getLength();
					} catch (Exception e) {
				
					}
				}
				boolean ind = true;
				if(l <= 0) {
					// indicator unavailable
					ind = false;
				} else {
				}
				if(cancel) return;
				String sizeStr = ind ? Integer.toString(l) : null;
				int i = 0;
				while((read = in.read(buf)) != -1) {
					if(cancel) {
						fail("Cancel", "");
						return;
					}
					out.write(buf, 0, read);
					d += read;
					if(i++ % 100 == 0) {
						if(cancel) return;
						if(ind) {
							label.setText("Downloading \n"
									.concat(Integer.toString(d))
									.concat(" / ")
									.concat(sizeStr)
									.concat(" MB\n"));
						} else {
							label.setText("Downloaded: ".concat(Integer.toString(d / 1024)).concat(" Kb"));
						}
					}
				}
				done();
			} catch (InterruptedException e) {
				fail("Cancel", "");
			} catch (Exception e) {
				fail(e.toString(), "Error");
			} catch (Throwable e) {
				fail(e.toString(), "Error");
			} finally {
				try {
					if(out != null) out.close();
				} catch (Exception e) {
				} 
				try {
					if(fc != null) fc.close();
				} catch (Exception e) {
				} 
				try {
					if(in != null) in.close();
				} catch (Exception e) {
				} 
				try {
					if(hc != null) hc.close();
				} catch (Exception e) {
				} 
			}
			break;
		}
	}

	void scheduleRunnable(Runnable r) {
		if(queuedTasks.contains(r)) return;
		queuedTasks.addElement(r);
		synchronized(tasksLock) {
			tasksLock.notify();
		}
	}
	
	static int width;
	static int height;

	void startApp() {
		System.out.println(System.getProperty("microedition.platform"));
		region = "US";
		initForm();
		tasksThread = new Thread(this);
		tasksThread.setPriority(4);
		tasksThread.start();
		loadConfig();
		loadForm();
	}
	
	private void loadForm() {
		try {
			mainList.append("Loading", null);
			if(startScreen == 0) {
				loadTrends();
			} else {
				loadPopular();
			}
		} catch (OutOfMemoryError e) {
			error(this, Errors.App_loadForm, "Out of memory!");
		} catch (Throwable e) {
			error(this, Errors.App_loadForm, e);
		}
	}

	private void initForm() {
		mainItems = new Object[30];
		mainList = new List(NAME, List.IMPLICIT);
		/*
		searchText = new TextField("", "", 256, TextField.ANY);
		searchText.setLayout(Item.LAYOUT_LEFT | Item.LAYOUT_2);
		mainForm.append(searchText);
		searchBtn = new StringItem(null, "Поиск", StringItem.BUTTON);
		searchBtn.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_RIGHT | Item.LAYOUT_2);
		searchBtn.addCommand(searchCmd);
		searchBtn.setDefaultCommand(searchCmd);
		mainForm.append(searchBtn);
		*/
		mainList.setCommandListener(this);
		mainList.addCommand(List.SELECT_COMMAND);
		mainList.addCommand(aboutCmd);
		mainList.addCommand(searchCmd);
		mainList.addCommand(idCmd);
		mainList.addCommand(settingsCmd);
		mainList.addCommand(exitCmd);
		display(mainList);
	}
	
	static Object invApi(String s) throws InvidiousException, IOException {
		return invApi(s, null);
	}

	static Object invApi(String s, String fields) throws InvidiousException, IOException {
		if(!s.endsWith("?")) s = s.concat("&");
		s = s.concat("region=" + region);
		if(fields != null) {
			s = s.concat("&fields=" + fields + ",error,errorBacktrace,code");
		}
		String url = s = inv + "api/v1/" + s;
		if(invProxy != null && invProxy.length() > 0) {
			s = invProxy.concat("?u=").concat(url(s));
		}
		try {
			s = getUtf(s);
		} catch (IOException e) {
			throw new IOException(e.toString() + ";\nURL: " + url);
		}
		Object res;
		if(s.charAt(0) == '{') {
			res = getObject(s);
			if(((JSONObject) res).has("code")) {
				throw new InvidiousException((JSONObject) res, ((JSONObject) res).getString("code") + ": " + ((JSONObject) res).getNullableString("message"), url);
			}
			if(((JSONObject) res).has("error")) {
				throw new InvidiousException((JSONObject) res, ((JSONObject) res).getString("error"), url);
			}
		} else {
			res = getArray(s);
		}
		return res;
	}

	private void loadTrends() {
		mainList.addCommand(switchToPopularCmd);
		try {
			mainList.setTitle(NAME + " - Trending");
			Object r = invApi("trending?", VIDEO_FIELDS);
			if(r instanceof JSONObject) {
				error(this, Errors.App_loadTrends, "Wrong response", r.toString());
				return;
			}
			JSONArray j = (JSONArray) r;
			try {
				mainList.delete(0);
			} catch (Exception e) {
			}
			int l = j.size();
			for(int i = 0; i < l; i++) {
				if(i >= TRENDS_LIMIT) break;
				mainList.append(parseAndMakeItem(j.getObject(i), false, i), null);
			}
			j = null;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			error(this, Errors.App_loadTrends, e);
		}
	}

	private void loadPopular() {
		mainList.addCommand(switchToTrendsCmd);
		try {
			mainList.setTitle(NAME + " - Popular");
			JSONArray j = (JSONArray) invApi("popular?", VIDEO_FIELDS);
			try {
				mainList.delete(0);
			} catch (Exception e) {
			}
			int l = j.size();
			for(int i = 0; i < l; i++) {
				if(i >= TRENDS_LIMIT) break;
				mainList.append(parseAndMakeItem(j.getObject(i), false, i), null);
			}
			j = null;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			error(this, Errors.App_loadPopular, e);
		}
	}

	private void search(String q) {
		searchItems = new Object[30];
		searchList = new List(NAME + " - Search query", List.IMPLICIT);
		searchList.setCommandListener(this);
		searchList.addCommand(List.SELECT_COMMAND);
		searchList.addCommand(settingsCmd);
		searchList.addCommand(searchCmd);
		searchList.addCommand(backCmd);
		searchList.append("Loading", null);
		display(searchList);
		//disposeMainForm();
		try {
			JSONArray j = (JSONArray) invApi("search?q=" + url(q), SEARCH_FIELDS + ",type,videoCount&type=all");
			int l = j.size();
			searchList.delete(0);
			for(int i = 0; i < l; i++) {
				if(i >= SEARCH_LIMIT) break;
				searchList.append(parseAndMakeItem(j.getObject(i), true, i), null);
			}
			j = null;
		} catch (Exception e) {
			error(this, Errors.App_search, e);
		}
	}
	
	private String parseAndMakeItem(JSONObject j, boolean search, int i) {
		String type = j.getNullableString("type");
		if(type == null) {
			// video
			Video v = new Video(j);
			(search ? searchItems : mainItems)[i] = v;
			if(search) v.fromSearch = true;
			return v.makeItemForList();
		}
		if(type.equals("video")) {
			Video v = new Video(j);
			(search ? searchItems : mainItems)[i] = v;
			if(search) v.fromSearch = true;
			return v.makeItemForList();
		}
		if(type.equals("channel")) {
			Channel c = new Channel(j);
			(search ? searchItems : mainItems)[i] = c;
			if(search) c.fromSearch = true;
			return c.makeItemForList();
		}
		return "Unknown object";
	}

	private void openVideo(String id) {
		final String https = "https://";
		final String ytshort = "youtu.be/";
		final String www = "www.";
		final String watch = "youtube.com/watch?v=";
		if(id.startsWith(https)) id = id.substring(https.length());
		if(id.startsWith(ytshort)) id = id.substring(ytshort.length());
		if(id.startsWith(www)) id = id.substring(www.length());
		if(id.startsWith(watch)) id = id.substring(watch.length());
		try {
			open(new Video(id).extend());
			//disposeMainForm();
		} catch (Exception e) {
			error(this, Errors.App_openVideo, e);
		}
	}

	static JSONObject getVideoInfo(String id) throws RuntimeException, IOException {
		JSONObject j = (JSONObject) invApi("videos/" + id + "?", "formatStreams");
		JSONArray arr = j.getArray("formatStreams");
		if(j.size() == 0) {
			throw new RuntimeException("failed to get link for video: " + id);
		}
		int l = arr.size();
		for(int i = 0; i < l; i++) {
			JSONObject o = arr.getObject(i);
			String q = o.getString("qualityLabel");
			if(quality.equals(q)) {
				return o;
			}
		}
		return null;
	}

	static String getVideoLink(String id) throws RuntimeException, IOException {
		String s = getVideoInfo(id).getString("url");
		//int i = s.indexOf("/videoplayback");
		//s = inv + s.substring(i+1);
		s = "http://nnp.nnchan.ru/glype/browse.php?u=" + url(s);
		return s;
	}

	static void open(Model model) {
		open(model, null);
	}

	static void open(Model model, List formContainer) {
		if(model == null) return;
		// check if already loading
		if(formContainer == null && videoForm != null && model instanceof Video) {
			return;
		}
		if(model.fromSearch) {
			disposeSearchForm();
		}
		ModelForm form = model.makeForm();
		display(form);
		if(model instanceof Video) {
			videoForm = (Video) model;
		} else if(model instanceof Channel) {
			videoForm = null;
			channelForm = (Channel) model;
		}
		if(formContainer != null) {
			form.setContainer(formContainer);
		}
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
		}
		inst.scheduleRunnable(model);
	}
	
	static void download(final String id) {
		new App(id, videoForm.form, downloadDir).start();
	}
	
	static void watch(final String id) {
		String url;
		try {
			url = getVideoLink(id);
			platReq(url);
		} catch (Throwable e) {
			error(null, Errors.App_watch, e);
		}
	}

	static void back(Form f) {
		if(f instanceof ModelForm && ((ModelForm)f).getModel().fromSearch && searchList != null) {
			App.display(searchList);
		} else if(mainList != null) {
			App.display(mainList);
		} else {
			inst.initForm();
			App.display(mainList);
			inst.loadForm();
		}
	}

	public void commandAction(Command c, Displayable d) {

		//downloader
		if(c == dlOkCmd) {
			App.display(dd);
			dd = null;
			return;
		}
		if(c == dlCancelCmd) {
			cancel = true;
			try {
				t.interrupt();
			} catch (Throwable e) {
			}
			App.display(dd);
			dd = null;
			return;
		}
		
		if(d instanceof Alert) {
			display(mainList);
			return;
		}
		if(d == dirList) {
			if(c == backCmd) {
				if(curDir == null) {
					dirList = null;
					display(settingsForm);
				} else {
					if(curDir.indexOf("/") == -1) {
						dirList = new List("", List.IMPLICIT);
						dirList.addCommand(backCmd);
						dirList.setCommandListener(this);
						for(int i = 0; i < rootsVector.size(); i++) {
							String s = (String) rootsVector.elementAt(i);
							if(s.startsWith("file:///")) s = s.substring("file:///".length());
							if(s.endsWith("/")) s = s.substring(0, s.length() - 1);
							dirList.append(s, null);
						}
						curDir = null;
						App.display(dirList);
						return;
					}
					String sub = curDir.substring(0, curDir.lastIndexOf('/'));
					String fn = "";
					if(sub.indexOf('/') != -1) {
						fn = sub.substring(sub.lastIndexOf('/'));
					}
					curDir = sub;
					dirListOpen(sub, fn);
				}
			}
			if(c == dirOpenCmd || c == List.SELECT_COMMAND) {
				String fs = curDir;
				String f = "";
				if(fs != null) f += curDir + "/";
				String is = dirList.getString(dirList.getSelectedIndex());
				if(is.equals("- Select")) {
					dirList = null;
					downloadDirText.setString(f);
					curDir = null;
					display(settingsForm);
					return;
				}
				f += is;
				curDir = f;
				dirListOpen(f, is);
				return;
			}
			if(c == dirSelectCmd) {
				dirList = null;
				downloadDirText.setString(curDir + "/");
				curDir = null;
				display(settingsForm);
			}
			return;
		}
		if(c == dirCmd) {
			if(System.getProperty("microedition.io.file.FileConnection.version") == null) {
				msg("JSR-75 is not supported");
				return;
			}
			dirList = new List("", List.IMPLICIT);
			getRoots();
			for(int i = 0; i < rootsVector.size(); i++) {
				String s = (String) rootsVector.elementAt(i);
				if(s.startsWith("file:///")) s = s.substring("file:///".length());
				if(s.endsWith("/")) s = s.substring(0, s.length() - 1);
				dirList.append(s, null);
			}
			dirList.addCommand(backCmd);
			dirList.setCommandListener(this);
			App.display(dirList);
			return;
		}
		if(c == List.SELECT_COMMAND) {
			int i = ((List)d).getSelectedIndex();
			if(i == -1) return;
			App.open((Model) (d == searchList ? searchItems : mainItems)[i]);
			return;
		}
		if(d == settingsForm) {
			applySettings();
			display(null);
			return;
		}
		if(c == exitCmd) {
			try {
				String[] a = RecordStore.listRecordStores();
				for(int i = 0; i < a.length; i++) {
					if(a[i].equals(CONFIG_RECORD_NAME)) continue;
					RecordStore.deleteRecordStore(a[i]);
				}
			} catch (Exception e) {
			}
			midlet.notifyDestroyed();
			return;
		}
		if(c == aboutCmd) {
			Alert a = new Alert("", "", null, null);
			a.setTimeout(-2);
			a.setString("JTube Lite " + ver + "\n"
					+ "By Shinovon (nnp.nnchan.ru) \n"
					+ "t.me/nnmidletschat");
			//a.setCommandListener(this);
			//a.addCommand(new Command("OK", Command.OK, 1));
			display(a);
			return;
		}

		if(c == goCmd && d instanceof TextBox) {
			openVideo(((TextBox) d).getString());
			return;
		}
		if(c == searchOkCmd && d instanceof TextBox) {
			search(((TextBox) d).getString());
			return;
		}
		if(c == settingsCmd) {
			if(settingsForm == null) {
				settingsForm = new Form("Settings");
				settingsForm.setCommandListener(this);
				settingsForm.addCommand(applyCmd);
				regionText = new TextField("Country code (ISO 3166)", App.region, 3, TextField.ANY);
				settingsForm.append(regionText);
				downloadDirText = new TextField("Download directory", App.downloadDir, 256, TextField.URL);
				settingsForm.append(downloadDirText);
				//dirBtn = new StringItem(null, "...", Item.BUTTON);
				settingsForm.addCommand(dirCmd);
				//settingsForm.append(dirBtn);
				invidiousText = new TextField("Invidious API instance", App.inv, 256, TextField.URL);
				settingsForm.append(invidiousText);
				proxyText = new TextField("API Proxy", App.invProxy, 256, TextField.URL);
				settingsForm.append(proxyText);
				downloadBufferText = new TextField("Download buffer size", Integer.toString(App.downloadBuffer), 5, TextField.NUMERIC);
				settingsForm.append(downloadBufferText);
			}
			display(settingsForm);
		}
		if(c == searchCmd) {
			if(searchList != null) {
				disposeSearchForm();
			}
			TextBox t = new TextBox("", "", 256, TextField.ANY);
			t.setCommandListener(this);
			t.setTitle("Search");
			t.addCommand(searchOkCmd);
			t.addCommand(cancelCmd);
			display(t);
		}
		if(c == idCmd) {
			TextBox t = new TextBox("", "", 256, TextField.ANY);
			t.setCommandListener(this);
			t.setTitle("Video URL or ID");
			t.addCommand(goCmd);
			t.addCommand(cancelCmd);
			display(t);
		}
		/*if(c == browserCmd) {
			try {
				platReq(getVideoInfo(video.getVideoId(), videoRes).getString("url"));
			} catch (Exception e) {
				e.printStackTrace();
				msg(e.toString());
			}
		}*/
		if(c == cancelCmd && d instanceof TextBox) {
			display(mainList);
		}
		if(c == backCmd && d == searchList) {
			disposeSearchForm();
			if(mainList == null) {
				display(mainList);
			} else {
				initForm();
				loadForm();
			}
		}
		if(c == switchToPopularCmd) {
			startScreen = 1;
			initForm();
			if(searchList != null) {
				disposeSearchForm();
			}
			loadForm();
			saveConfig();
		}
		if(c == switchToTrendsCmd) {
			startScreen = 0;
			initForm();
			if(searchList != null) {
				disposeSearchForm();
			}
			loadForm();
			saveConfig();
		}
	}

	static void msg(String s) {
		Alert a = new Alert("", s, null, null);
		a.setTimeout(-2);
		display(a);
	}
	
	static void display(Displayable d) {
		boolean b = false;
		if(d == null) {
			if(videoForm != null) {
				d = videoForm.form;
			} else if(channelForm != null) {
				d = channelForm.form;
			} else if(searchList != null) {
				d = searchList;
			} else if(mainList != null) {
				d = mainList;
			} else {
				inst.initForm();
				d = mainList;
				b = true;
			}
		}
		Display.getDisplay(midlet).setCurrent(d);
		if(b) inst.loadForm();
	}

	void disposeMainForm() {
		mainList = null;
	}

	static void disposeVideoForm() {
		videoForm = null;
	}

	static void disposeChannelForm() {
		channelForm = null;
	}

	static void disposeSearchForm() {
		searchList = null;
	}
	
	static void platReq(String s) throws Exception {
		if(midlet.platformRequest(s)) {
			midlet.notifyDestroyed();
		}
	}

	static void warn(Object o, String str) {
		String cls = "";
		if(o != null) cls = "at " + o.getClass().getName();
		String s = str + " \n\n" + cls + " \nt:" + Thread.currentThread().toString();
		Alert a = new Alert("", s, null, AlertType.WARNING);
		a.setTimeout(-2);
		display(a);
	}

	static void error(Object o, int i, Throwable e) {
		if(e instanceof InvidiousException) {
			error(o, i, e.toString(), ((InvidiousException)e).toErrMsg());
			return;
		}
		String s1 = e.toString();
		int f = s1.indexOf(";\n");
		String s2 = null;
		if(f != -1) {
			s2 = s1.substring(f+2);
			s1 = s1.substring(0, f);
		}
		error(o, i, s1, s2);
	}

	static void error(Object o, int i, String str) {
		error(o, i, str, null);
	}

	static void error(Object o, int i, String str, String str2) {
		String cls = "null";
		if(o != null) cls = o.getClass().getName();
		String s = str + " \n\ne: " + i + " \nat " + cls + " \nt: " + Thread.currentThread().toString() + (str2 != null ? " \n" + str2 : "");
		Alert a = new Alert("", s, null, AlertType.ERROR);
		a.setTimeout(-2);
		display(a);
	}
	
	// downloader
	
	private String id;
	
	private Form form;
	private StringItem label;
	private Displayable dd;
	
	private String file;
	private Thread t;
	private boolean cancel;

	private int type;
	
	App(String vid, Displayable d, String downloadDir) {
		type = 1;
		this.id = vid;
		this.dd = d;
		this.file = "file:///" + downloadDir;
		if(!(file.endsWith("/") || file.endsWith("\\"))) {
			file += Path_separator;
		}
	}
	
	void start() {
		if(System.getProperty("microedition.io.file.FileConnection.version") == null) {
			msg("JSR-75 is not supported");
			return;
		}
		form = new Form("");
		form.addCommand(dlCancelCmd);
		display(form);
		form.setCommandListener(this);
		label = new StringItem("", "Initializing");
		label.setLayout(Item.LAYOUT_NEWLINE_AFTER);
		form.append(label);
		t = new Thread(this);
		t.start();
	}

	private void done() {
		form.addCommand(dlOkCmd);
		form.removeCommand(dlCancelCmd);
		label.setText("Done\n".concat(file));
	}

	private void fail(String s, String title) {
		form.addCommand(dlOkCmd);
		form.removeCommand(dlCancelCmd);
		form.setTitle(title);
		label.setText(s);
	}
	
	// json
	final static boolean parse_members = false;

	//public static final String FORMAT_TAB = "  ";
	
	static final Boolean TRUE = new Boolean(true);
	static final Boolean FALSE = new Boolean(false);

	static JSONObject getObject(String string) throws RuntimeException {
		if (string == null || string.length() <= 1)
			throw new RuntimeException("Empty string");
		if (string.charAt(0) != '{')
			throw new RuntimeException("Not JSON object");
		return (JSONObject) parseJSON(string);
	}

	static JSONArray getArray(String string) throws RuntimeException {
		if (string == null || string.length() <= 1)
			throw new RuntimeException("Empty string");
		if (string.charAt(0) != '[')
			throw new RuntimeException("Not JSON array");
		return (JSONArray) parseJSON(string);
	}

	static Object getJSON(Object obj) throws RuntimeException {
		if (obj instanceof Hashtable) {
			return new JSONObject((Hashtable) obj);
		} else if (obj instanceof Vector) {
			return new JSONArray((Vector) obj);
		} else {
			return obj;
		}
	}

	static Object parseJSON(String str) throws RuntimeException {
		if (str == null || str.equals(""))
			throw new RuntimeException("Empty string");
		if (str.length() < 2) {
			return str;
		} else {
			str = str.trim();
			char first = str.charAt(0);
			int length = str.length() - 1;
			char last = str.charAt(length);
			if (first == '{' && last != '}' || first == '[' && last != ']' || first == '"' && last != '"') {
				throw new RuntimeException("Unexpected end of text");
			} else if (first == '"') {
				// String
				str = str.substring(1, str.length() - 1);
				char[] chars = str.toCharArray();
				str = null;
				try {
					int l = chars.length;
					StringBuffer sb = new StringBuffer();
					int i = 0;
					// Parse string escape chars
					loop: {
						while (i < l) {
							char c = chars[i];
							switch (c) {
							case '\\': {
								next: {
									replaced: {
										if(l < i + 1) {
											sb.append(c);
											break loop;
										}
										char c1 = chars[i + 1];
										switch (c1) {
										case 'n':
											sb.append('\n');
											i+=2;
											break replaced;
										case 'r':
											sb.append('\r');
											i+=2;
											break replaced;
										case 't':
											sb.append('\t');
											i+=2;
											break replaced;
										case 'f':
											sb.append('\f');
											i+=2;
											break replaced;
										case 'b':
											sb.append('\b');
											i+=2;
											break replaced;
										case '\"':
										case '\'':
										case '\\':
										case '/':
											i+=2;
											sb.append((char) c1);
											break replaced;
										default:
											break next;
										}
									}
									break;
								}
								sb.append(c);
								i++;
								break;
							}
							default:
								sb.append(c);
								i++;
							}
						}
					}
					
					str = sb.toString();
				} catch (Exception e) {
				}
				return str;
			} else if (first != '{' && first != '[') {
				if (str.equals("true"))
					return TRUE;
				if (str.equals("false"))
					return FALSE;
				return str;
			} else {
				// Parse json object or array
				int unclosed = 0;
				boolean object = first == '{';
				int i = 1;
				char nextDelimiter = object ? ':' : ',';
				boolean escape = false;
				String key = null;
				Object res = null;
				if (object) res = new Hashtable();
				else res = new Vector();
				
				for (int splIndex; i < length; i = splIndex + 1) {
					// skip all spaces
					for (; i < length - 1 && str.charAt(i) <= ' '; i++);

					splIndex = i;
					boolean quotes = false;
					for (; splIndex < length && (quotes || unclosed > 0 || str.charAt(splIndex) != nextDelimiter); splIndex++) {
						char c = str.charAt(splIndex);
						if (!escape) {
							if (c == '\\') {
								escape = true;
							} else if (c == '"') {
								quotes = !quotes;
							}
						} else escape = false;
		
						if (!quotes) {
							if (c == '{' || c == '[') {
								unclosed++;
							} else if (c == '}' || c == ']') {
								unclosed--;
							}
						}
					}

					if (quotes || unclosed > 0) {
						throw new RuntimeException("Corrupted JSON");
					}

					if (object && key == null) {
						key = str.substring(i, splIndex);
						key = key.substring(1, key.length() - 1);
						nextDelimiter = ',';
					} else {
						String s = str.substring(i, splIndex);
						while (s.endsWith("\r") || s.endsWith("\n")) {
							s = s.substring(0, s.length() - 1);
						}
						Object value = s.trim();
						if (parse_members) value = parseJSON(value.toString());
						if (object) {
							((Hashtable) res).put(key, value);
							key = null;
							nextDelimiter = ':';
						} else if (splIndex > i) ((Vector) res).addElement(value);
					}
				}
				return getJSON(res);
			}
		}
	}

	static Long getLong(Object o) throws RuntimeException {
		try {
			if (o instanceof Integer)
				return new Long(((Integer)o).longValue());
			else if (o instanceof Long)
				return (Long) o;
			else if (o instanceof String)
				return new Long(Long.parseLong((String) o));
		} catch (Throwable e) {
		}
		throw new RuntimeException("Value cast failed: " + o);
	}
	
	// settings
	
	private static final Command dirCmd = new Command("Select download dir", Command.ITEM, 1);

	private static Vector rootsVector;
	
	private TextField regionText;
	private TextField downloadDirText;
	private TextField invidiousText;
	private TextField downloadBufferText;
	private TextField proxyText;
	//private StringItem dirBtn;

	private List dirList;

	private String curDir;


	private final static Command dirOpenCmd = new Command("Open", Command.ITEM, 1);
	private final static Command dirSelectCmd = new Command("Apply", Command.OK, 2);
	
	private static void getRoots() {
		if(rootsVector != null) return;
		try {
			rootsVector = Files._getRoots();
		} catch (Throwable e) {
		}
	}
	

	static void loadConfig() {
		RecordStore r = null;
		try {
			r = RecordStore.openRecordStore(CONFIG_RECORD_NAME, false);
		} catch (Exception e) {
		}
		if(r == null) {
			if(System.getProperty("microedition.io.file.FileConnection.version") == null) {
				return;
			}
			String downloadDir = System.getProperty("fileconn.dir.videos");
			if(downloadDir == null) {
				try {
					getRoots();
					String root = "";
					for(int i = 0; i < rootsVector.size(); i++) {
						String s = (String) rootsVector.elementAt(i);
						if(s.startsWith("file:///")) s = s.substring("file:///".length());
						if(s.startsWith("Video")) {
							root = s;
							break;
						}
						if(s.startsWith("SDCard")) {
							root = s;
							break;
						}
						if(s.startsWith("F:")) {
							root = s;
							break;
						}
						if(s.startsWith("E:")) {
							root = s;
							break;
						}
						if(s.startsWith("/Storage")) {
							root = s;
							break;
						}
						if(s.startsWith("/MyDocs")) {
							root = s;
						}
						if(s.startsWith("C:")) {
							root = s;
						}
					}
					if(!root.endsWith("/")) root += "/";
					downloadDir = root;
				} catch (Throwable e) {
				}
			}
			if(downloadDir == null)
				downloadDir = System.getProperty("fileconn.dir.photos");
			if(downloadDir == null)
				downloadDir = "C:/";
			else if(downloadDir.startsWith("file:///"))
				downloadDir = downloadDir.substring("file:///".length());
			App.downloadDir = downloadDir;
			saveConfig();
		} else {
			try {
				JSONObject j = getObject(new String(r.getRecord(1)));
				r.closeRecordStore();
				if(j.has("region"))
					App.region = j.getString("region");
				if(j.has("downloadDir"))
					App.downloadDir = j.getString("downloadDir");
				if(j.has("inv"))
					App.inv = j.getString("inv");
				if(j.has("startScreen"))
					App.startScreen = j.getInt("startScreen");
				if(j.has("downloadBuffer"))
					App.downloadBuffer = j.getInt("downloadBuffer");
				if(j.has("invProxy"))
					App.invProxy = j.getString("invProxy");
				return;
			} catch (Exception e) {
			}
		}
	}
	
	private void applySettings() {
		try {
			App.region = regionText.getString();
			String dir = downloadDirText.getString();
			//dir = Util.replace(dir, "/", dirsep);
			dir = replace(dir, "\\", Path_separator);
			while (dir.endsWith(Path_separator)) {
				dir = dir.substring(0, dir.length() - 1);
			}
			App.downloadDir = dir;
			App.inv = invidiousText.getString();
			App.invProxy = proxyText.getString();
			App.downloadBuffer = Integer.parseInt(downloadBufferText.getString());
			saveConfig();
		} catch (Exception e) {
			App.error(this, Errors.Settings_apply, e.toString());
		}
	}
	
	static void saveConfig() {
		try {
			RecordStore.deleteRecordStore(CONFIG_RECORD_NAME);
		} catch (Throwable e) {
		}
		try {
			RecordStore r = RecordStore.openRecordStore(CONFIG_RECORD_NAME, true);
			JSONObject j = new JSONObject();
			j.put("v", "v1");
			j.put("region", App.region);
			j.put("downloadDir", App.downloadDir);
			j.put("inv", App.inv);
			j.put("startScreen", new Integer(App.startScreen));
			j.put("downloadBuffer", new Integer(App.downloadBuffer));
			j.put("invProxy", App.invProxy);
			byte[] b = j.build().getBytes();
			
			r.addRecord(b, 0, b.length);
			r.closeRecordStore();
		} catch (Exception e) {
		}
	}
	
	private void dirListOpen(String f, String title) {
		dirList = new List(title, List.IMPLICIT);
		dirList.addCommand(backCmd);
		dirList.setCommandListener(this);
		dirList.addCommand(dirSelectCmd);
		dirList.append("- Select", null);
		try {
			Files.fillDirList(f, dirList);
		} catch (Throwable e) {
		}
		App.display(dirList);
	}
	
	
	// platform utils
	
	static final String platform = System.getProperty("microedition.platform");
	// Symbian 9.x or higher check
	static boolean isSymbian9() {
		return platform.indexOf("platform=S60") != -1 ||
				System.getProperty("com.symbian.midp.serversocket.support") != null ||
				System.getProperty("com.symbian.default.to.suite.icon") != null;
	}

	static boolean isSamsung() {
		return platform != null && platform.toLowerCase().startsWith("samsung");
	}

	static boolean isSonyEricsson() {
		return System.getProperty("com.sonyericsson.java.platform") != null || (platform != null && platform.toLowerCase().startsWith("sonyericsson"));
	}

	static boolean isWTK() {
		return platform != null && (platform.startsWith("wtk") || platform.endsWith("wtk"));
	}

	static boolean isJ2ME() {
		return "j2me".equals(platform);
	}
	
	// util
	
	private static String charset = "UTF-8";
	private static String alt_charset = "ISO-8859-1";

	static {
		// Test UTF-8 support
		String testWord = "выф";
		if((isSonyEricsson() || isSamsung() || isJ2ME() || isWTK()) && !isSymbian9()) {
			try {
				String tmp = new String(testWord.getBytes("UTF8"), "UTF8");
				if(tmp.charAt(0) == 'в') {
					charset = "UTF8";
				}
			} catch (Throwable e) {
			}
		}
		boolean test = false;
		try {
			String tmp = new String(testWord.getBytes(charset), charset);
			if(tmp.charAt(0) == 'в') test = true;
		} catch (Throwable e) {
		}
		if(!test) {
			charset = alt_charset;
		}
	} 

	static byte[] get(String url) throws IOException {
		ByteArrayOutputStream o = null;
		HttpConnection hc = null;
		InputStream in = null;
		try {
			hc = (HttpConnection) Connector.open(url);
			hc.setRequestMethod("GET");
			hc.setRequestProperty("User-Agent", userAgent);
			int r = hc.getResponseCode();
			if(r >= 300 && r != 500) throw new IOException(r + " " + hc.getResponseMessage());
			in = hc.openDataInputStream();
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				throw new IOException();
			}
			int read;
			o = new ByteArrayOutputStream();
			byte[] b = new byte[512];
			while((read = in.read(b)) != -1) {
				o.write(b, 0, read);
			}
			return o.toByteArray();
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException e) {
			}
			try {
				if (hc != null) hc.close();
			} catch (IOException e) {
			}
			try {
				if (o != null) o.close();
			} catch (IOException e) {
			}
		}
	}
	
	static String getUtf(String url) throws IOException {
		byte[] b = get(url);
		try {
			return new String(b, charset);
		} catch (Throwable e) {
			return new String(b, charset = alt_charset);
		}
	}
	
	static String url(String url) {
		StringBuffer sb = new StringBuffer();
		char[] chars = url.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			int c = chars[i];
			if (65 <= c && c <= 90) {
				sb.append((char) c);
			} else if (97 <= c && c <= 122) {
				sb.append((char) c);
			} else if (48 <= c && c <= 57) {
				sb.append((char) c);
			} else if (c == 32) {
				sb.append("%20");
			} else if (c == 45 || c == 95 || c == 46 || c == 33 || c == 126 || c == 42 || c == 39 || c == 40
					|| c == 41) {
				sb.append((char) c);
			} else if (c <= 127) {
				sb.append(hex(c));
			} else if (c <= 2047) {
				sb.append(hex(0xC0 | c >> 6));
				sb.append(hex(0x80 | c & 0x3F));
			} else {
				sb.append(hex(0xE0 | c >> 12));
				sb.append(hex(0x80 | c >> 6 & 0x3F));
				sb.append(hex(0x80 | c & 0x3F));
			}
		}
		return sb.toString();
	}

	private static String hex(int i) {
		String s = Integer.toHexString(i);
		return "%" + (s.length() < 2 ? "0" : "") + s;
	}

	static String replace(String str, String from, String to) {
		int j = str.indexOf(from);
		if (j == -1)
			return str;
		final StringBuffer sb = new StringBuffer();
		int k = 0;
		for (int i = from.length(); j != -1; j = str.indexOf(from, k)) {
			sb.append(str.substring(k, j)).append(to);
			k = j + i;
		}
		sb.append(str.substring(k, str.length()));
		return sb.toString();
	}

	static String timeStr(int i) {
		if(i <= 0) return null;
		String s = "" + i % 60;
		if(s.length() < 2) s = "0".concat(s);
		String m = "" + (i % 3600) / 60;
		int h = i / 3600;
		if(h > 0) {
			if(m.length() < 2) m = "0".concat(m);
			return Integer.toString(i).concat(":").concat(m).concat(":").concat(s);
		} else {
			return m.concat(":").concat(s);
		}
	}
	
	// commands
	
	// Main form commands
	static final Command settingsCmd = new Command("Settings", Command.SCREEN, 4);
	static final Command idCmd = new Command("Open by ID", Command.SCREEN, 8);
	static final Command searchCmd = new Command("Search", Command.SCREEN, 2);
	static final Command aboutCmd = new Command("About", Command.SCREEN, 10);
	static final Command switchToPopularCmd = new Command("Switch to popular", Command.SCREEN, 3);
	static final Command switchToTrendsCmd = new Command("Switch to trends", Command.SCREEN, 3);
	
	static final Command searchOkCmd = new Command("Search", Command.OK, 1);
	static final Command exitCmd = new Command("Exit", Command.EXIT, 2);
	static final Command goCmd = new Command("Go", Command.OK, 1);
	static final Command cancelCmd = new Command("Cancel", Command.CANCEL, 2);
	static final Command backCmd = new Command("Back", Command.BACK, 1);
	
	static final Command applyCmd = new Command("Apply", Command.BACK, 1);
	
	// Video page commands
	static final Command watchCmd = new Command("Watch", Command.OK, 10);
	static final Command downloadCmd = new Command("Download", Command.SCREEN, 9);
	static final Command viewChannelCmd = new Command("View channel", Command.SCREEN, 8);
	//static final Command browserCmd = new Command("Open with browser", Command.SCREEN, 3);
	
	// Downloader alert commands
	static final Command dlOkCmd = new Command("Ok", Command.CANCEL, 1);
	//static final Command dlWatchCmd = new Command("Watch", Command.SCREEN, 2);
	static final Command dlCancelCmd = new Command("Cancel", Command.CANCEL, 1);
	
	static String subscribers(int i) {
		if(i <= 0) return "";
		if(i == 1) return i + " subscriber";
		return i + " subscribers";
	}
	
	static String views(int i) {
		/*if(i >= 1000000) {
			return ((int) ((i / 1000000D) * 100) / 100) + "M";
		}*/
		return "" + i;
	}
	
	static String videos(int i) {
		if(i <= 0) return null;
		if(i == 1) return i + " video";
		return i + " videos";
	}
	
	// file operations
	
	
}
