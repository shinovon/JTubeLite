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
import java.io.IOException;
import java.util.Vector;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.rms.RecordStore;

import cc.nnproject.json.AbstractJSON;
import cc.nnproject.json.JSON;
import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONException;
import cc.nnproject.json.JSONObject;
import nnjtubelite.AppLite;

public class App implements CommandListener, Constants {
	
	public static final String ver = "1.1.1";
	
	// Settings
	public static String region;
	public static String downloadDir;
	public static String serverstream = streamphp;
	public static int startScreen; // 0 - Trends 1 - Popular
	public static String inv = iteroni;
	public static int downloadBuffer = 1024;
	
	public static final String quality = "144p";
	
	public static App inst;
	public static AppLite midlet;
	
	private Form mainForm;
	private Form searchForm;
	public Settings settingsForm;
	private VideoForm videoForm;
	private ChannelForm channelForm;
	private Item loadingItem;
	
	private Vector queuedTasks = new Vector();
	private Object tasksLock = new Object();
	private Thread tasksThread = new Thread() {
		public void run() {
			while(midlet.running) {
				try {
					synchronized (tasksLock) {
						tasksLock.wait();
					}
					while(queuedTasks.size() > 0) {
						try {
							((Runnable)queuedTasks.elementAt(0)).run();
						} catch (Exception e) {
							e.printStackTrace();
						}
						queuedTasks.removeElementAt(0);
						Thread.yield();
					}
				} catch (InterruptedException e) {
					return;
				}
			}
		}
	};

	public void scheduleRunnable(Runnable r) {
		if(queuedTasks.contains(r)) return;
		queuedTasks.addElement(r);
		synchronized(tasksLock) {
			tasksLock.notify();
		}
	}
	
	public static int width;
	public static int height;

	public void startApp() {
		region = "US";
		initForm();
		tasksThread.setPriority(4);
		tasksThread.start();
		Settings.loadConfig();
		loadForm();
	}
	
	private void loadForm() {
		try {
			loadingItem = new StringItem(null, Locale.s(TITLE_Loading));
			mainForm.append(loadingItem);
			if(startScreen == 0) {
				loadTrends();
			} else {
				loadPopular();
			}
			gc();
		} catch (OutOfMemoryError e) {
			gc();
			error(this, Errors.App_loadForm, "Out of memory!");
		} catch (Throwable e) {
			e.printStackTrace();
			error(this, Errors.App_loadForm, e);
		}
	}

	private void initForm() {
		mainForm = new Form(NAME);
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
		mainForm.setCommandListener(this);
		mainForm.addCommand(aboutCmd);
		mainForm.addCommand(searchCmd);
		mainForm.addCommand(idCmd);
		mainForm.addCommand(settingsCmd);
		mainForm.addCommand(exitCmd);
		display(mainForm);
	}
	
	public static AbstractJSON invApi(String s) throws InvidiousException, IOException {
		return invApi(s, null);
	}

	public static AbstractJSON invApi(String s, String fields) throws InvidiousException, IOException {
		if(!s.endsWith("?")) s = s.concat("&");
		s = s.concat("region=" + region);
		if(fields != null) {
			s = s.concat("&fields=" + fields + ",error,errorBacktrace,code");
		}
		String url = s;
		try {
			s = Util.getUtf(inv + "api/" + s);
		} catch (IOException e) {
			throw new NetRequestException(e, s);
		}
		AbstractJSON res;
		if(s.charAt(0) == '{') {
			res = JSON.getObject(s);
			if(((JSONObject) res).has("code")) {
				System.out.println(res.toString());
				throw new InvidiousException((JSONObject) res, ((JSONObject) res).getString("code") + ": " + ((JSONObject) res).getNullableString("message"), url);
			}
			if(((JSONObject) res).has("error")) {
				System.out.println(res.toString());
				throw new InvidiousException((JSONObject) res, null, url);
			}
		} else {
			res = JSON.getArray(s);
		}
		return res;
	}

	private void loadTrends() {
		mainForm.addCommand(switchToPopularCmd);
		try {
			mainForm.setTitle(NAME + " - " + Locale.s(TITLE_Trends));
			AbstractJSON r = invApi("v1/trending?", VIDEO_FIELDS);
			if(r instanceof JSONObject) {
				error(this, Errors.App_loadTrends, "Wrong response", r.toString());
				return;
			}
			JSONArray j = (JSONArray) r;
			try {
				if(mainForm.size() > 0 && mainForm.get(0) == loadingItem) {
					mainForm.delete(0);
				}
			} catch (Exception e) {
			}
			int l = j.size();
			for(int i = 0; i < l; i++) {
				Item item = parseAndMakeItem(j.getObject(i), false, i);
				if(item == null) continue;
				mainForm.append(item);
				if(i >= TRENDS_LIMIT) break;
				System.gc();
			}
			j = null;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			error(this, Errors.App_loadTrends, e);
		}
		gc();
	}

	private void loadPopular() {
		mainForm.addCommand(switchToTrendsCmd);
		try {
			mainForm.setTitle(NAME + " - " + Locale.s(TITLE_Popular));
			JSONArray j = (JSONArray) invApi("v1/popular?", VIDEO_FIELDS);
			try {
				if(mainForm.size() > 0 && mainForm.get(0) == loadingItem) {
					mainForm.delete(0);
				}
			} catch (Exception e) {
			}
			int l = j.size();
			for(int i = 0; i < l; i++) {
				Item item = parseAndMakeItem(j.getObject(i), false, i);
				if(item == null) continue;
				mainForm.append(item);
				if(i >= TRENDS_LIMIT) break;
				System.gc();
			}
			j = null;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			error(this, Errors.App_loadPopular, e);
		}
		gc();
	}

	private void search(String q) {
		searchForm = new Form(NAME + " - " + Locale.s(TITLE_SearchQuery));
		searchForm.setCommandListener(this);
		searchForm.addCommand(settingsCmd);
		searchForm.addCommand(searchCmd);
		display(searchForm);
		disposeMainForm();
		if(mainForm != null) {
			searchForm.addCommand(backCmd);
		} else {
			searchForm.addCommand(switchToTrendsCmd);
			searchForm.addCommand(switchToPopularCmd);
		}
		try {
			JSONArray j = (JSONArray) invApi("v1/search?q=" + Util.url(q), SEARCH_FIELDS + ",type,videoCount&type=all");
			int l = j.size();
			for(int i = 0; i < l; i++) {
				Item item = parseAndMakeItem(j.getObject(i), true, i);
				if(item == null) continue;
				
				searchForm.append(item);
				if(i >= SEARCH_LIMIT) break;
				System.gc();
			}
			j = null;
		} catch (Exception e) {
			e.printStackTrace();
			error(this, Errors.App_search, e);
		}
		gc();
	}
	
	private Item parseAndMakeItem(JSONObject j, boolean search, int i) {
		String type = j.getNullableString("type");
		if(type == null) {
			// video
			VideoModel v = new VideoModel(j);
			if(search) v.setFromSearch();
			return v.makeItemForList();
		}
		if(type.equals("video")) {
			VideoModel v = new VideoModel(j);
			if(search) v.setFromSearch();
			return v.makeItemForList();
		}
		if(type.equals("channel")) {
			ChannelModel c = new ChannelModel(j);
			if(search) c.setFromSearch();
			return c.makeItemForList();
		}
		return null;
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
			open(new VideoModel(id).extend());
			disposeMainForm();
		} catch (Exception e) {
			error(this, Errors.App_openVideo, e);
		}
	}

	static JSONObject getVideoInfo(String id) throws JSONException, IOException {
		JSONObject j = (JSONObject) invApi("v1/videos/"  + id + "?", "formatStreams");
		JSONArray arr = j.getArray("formatStreams");
		if(j.size() == 0) {
			throw new RuntimeException("failed to get link for video: " + id);
		}
		int l = arr.size();
		JSONObject r = null;
		for(int i = 0; i < l; i++) {
			JSONObject o = arr.getObject(i);
			String q = o.getString("qualityLabel");
			if(q.startsWith(quality)) {
				r = o;
				break;
			}
		}
			return r;
	}

	public static String getVideoLink(String id) throws JSONException, IOException {
		return serverstream + Util.url(getVideoInfo(id).getString("url"));
	}

	public static void open(AbstractModel model) {
		open(model, null);
	}

	public static void open(AbstractModel model, Form formContainer) {
		App app = inst;
		// check if already loading
		if(formContainer == null && app.videoForm != null && model instanceof VideoModel) {
			return;
		}
		if(model.isFromSearch()) {
			app.disposeSearchForm();
		}
		ModelForm form = model.makeForm();
		display(form);
		if(form instanceof VideoForm) {
			app.videoForm = (VideoForm) form;
		} else if(form instanceof ChannelForm) {
			app.videoForm = null;
			app.channelForm = (ChannelForm) form;
		}
		if(formContainer != null) {
			form.setFormContainer(formContainer);
		}
		gc();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
		}
		app.scheduleRunnable(form);
	}
	
	public static void download(final String id) {
		Downloader d = new Downloader(id, inst.videoForm, downloadDir);
		d.start();
	}
	
	public static void watch(final String id) {
		String url;
		try {
			url = getVideoLink(id);
			App.gc();
			platReq(url);
		} catch (Throwable e) {
			e.printStackTrace();
			error(null, Errors.App_watch, e);
		}
	}

	public static void back(Form f) {
		if(f instanceof ModelForm && ((ModelForm)f).getModel().isFromSearch() && inst.searchForm != null) {
			App.display(inst.searchForm);
		} else if(inst.mainForm != null) {
			App.display(inst.mainForm);
		} else {
			inst.initForm();
			App.display(inst.mainForm);
			inst.loadForm();
		}
	}

	public void commandAction(Command c, Displayable d) {
		if(d instanceof Alert) {
			display(mainForm);
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
					+ "By Shinovon (nnproject.cc) \n"
					+ "t.me/nnmidletschat");
			a.setCommandListener(this);
			a.addCommand(new Command("OK", Command.OK, 1));
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
				settingsForm = new Settings();
			}
			display(settingsForm);
			settingsForm.show();
		}
		if(c == searchCmd && d instanceof Form) {
			if(searchForm != null) {
				disposeSearchForm();
			}
			TextBox t = new TextBox("", "", 256, TextField.ANY);
			t.setCommandListener(this);
			t.setTitle(Locale.s(CMD_Search));
			t.addCommand(searchOkCmd);
			t.addCommand(cancelCmd);
			display(t);
		}
		if(c == idCmd && d instanceof Form) {
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
			display(mainForm);
		}
		if(c == backCmd && d == searchForm) {
			if(mainForm == null) return;
			display(mainForm);
			disposeSearchForm();
		}
		if(c == switchToPopularCmd) {
			startScreen = 1;
			if(mainForm != null) {
				mainForm.deleteAll();
			} else {
				initForm();
			}
			if(searchForm != null) {
				disposeSearchForm();
			} else {
				d.removeCommand(c);
			}
			loadPopular();
			Settings.saveConfig();
		}
		if(c == switchToTrendsCmd) {
			startScreen = 0;
			if(mainForm != null) {
				mainForm.deleteAll();
			} else {
				initForm();
			}
			if(searchForm != null) {
				disposeSearchForm();
			} else {
				d.removeCommand(c);
			}
			loadTrends();
			Settings.saveConfig();
		}
	}

	public static void msg(String s) {
		Alert a = new Alert("", s, null, null);
		a.setTimeout(-2);
		display(a);
	}
	
	public static void display(Displayable d) {
		boolean b = false;
		if(d == null) {
			if(inst.videoForm != null) {
				d = inst.videoForm;
			} else if(inst.channelForm != null) {
				d = inst.channelForm;
			} else if(inst.searchForm != null) {
				d = inst.searchForm;
			} else if(inst.mainForm != null) {
				d = inst.mainForm;
			} else {
				inst.initForm();
				d = inst.mainForm;
				b = true;
			}
		}
		Display.getDisplay(midlet).setCurrent(d);
		if(b) inst.loadForm();
	}

	void disposeMainForm() {
		if(mainForm != null) return;
		mainForm.deleteAll();
		mainForm = null;
		gc();
	}

	public void disposeVideoForm() {
		videoForm.dispose();
		videoForm = null;
		gc();
	}

	public void disposeChannelForm() {
		channelForm.dispose();
		channelForm = null;
		gc();
	}
	
	public static void gc() {
		System.gc();
	}

	private void disposeSearchForm() {
		searchForm.deleteAll();
		searchForm = null;
		gc();
	}
	
	public static void platReq(String s) throws ConnectionNotFoundException {
		if(midlet.platformRequest(s)) {
			midlet.notifyDestroyed();
		}
	}

	public static void warn(Object o, String str) {
		String cls = "";
		if(o != null) cls = "at " + o.getClass().getName();
		String s = str + " \n\n" + cls + " \nt:" + Thread.currentThread().getName();
		Alert a = new Alert("", s, null, AlertType.WARNING);
		a.setTimeout(-2);
		display(a);
	}

	public static void error(Object o, int i, Throwable e) {
		if(e instanceof InvidiousException) {
			error(o, i, e.toString(), ((InvidiousException)e).toErrMsg());
			return;
		}
		if(e instanceof NetRequestException) {
			NetRequestException e2 = (NetRequestException) e;
			error(o, i, e2.getTheCause().toString(), "URL: " + e2.getUrl());
			return;
		}
		error(o, i, e.toString(), null);
	}

	public static void error(Object o, int i, String str) {
		error(o, i, str, null);
	}

	public static void error(Object o, int i, String str, String str2) {
		String cls = "null";
		if(o != null) cls = o.getClass().getName();
		String s = str + " \n\ne: " + i + " \nat " + cls + " \nt: " + Thread.currentThread().getName() + (str2 != null ? " \n" + str2 : "");
		Alert a = new Alert("", s, null, AlertType.ERROR);
		a.setTimeout(-2);
		display(a);
	}

}
