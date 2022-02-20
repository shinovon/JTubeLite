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
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.rms.RecordStore;

import cc.nnproject.json.JSON;
import cc.nnproject.json.JSONObject;

public class Settings extends Form implements Constants, CommandListener, ItemCommandListener {

	private static final Command dirCmd = new Command("...", Command.ITEM, 1);

	private static Vector rootsVector;
	
	private TextField regionText;
	private TextField downloadDirText;
	private TextField httpProxyText;
	private TextField invidiousText;
	private StringItem dirBtn;

	private List dirList;

	private String curDir;

	private final static Command dirOpenCmd = new Command(Locale.s(CMD_Open), Command.ITEM, 1);
	private final static Command dirSelectCmd = new Command(Locale.s(CMD_Apply), Command.OK, 2);

	public Settings() {
		super(Locale.s(TITLE_Settings));
		setCommandListener(this);
		addCommand(applyCmd);
		regionText = new TextField(Locale.s(SET_CountryCode), App.region, 3, TextField.ANY);
		append(regionText);
		downloadDirText = new TextField(Locale.s(SET_DownloadDir), App.downloadDir, 256, TextField.URL);
		append(downloadDirText);
		dirBtn = new StringItem(null, "...", Item.BUTTON);
		dirBtn.setDefaultCommand(dirCmd);
		dirBtn.setItemCommandListener(this);
		append(dirBtn);
		invidiousText = new TextField(Locale.s(SET_InvAPI), App.inv, 256, TextField.URL);
		append(invidiousText);
		httpProxyText = new TextField(Locale.s(SET_StreamProxy), App.serverstream, 256, TextField.URL);
		append(httpProxyText);
	}
	
	public void show() {
	}
	
	private static void getRoots() {
		if(rootsVector != null) return;
		rootsVector = new Vector();
		Enumeration roots = FileSystemRegistry.listRoots();
		while(roots.hasMoreElements()) {
			String s = (String) roots.nextElement();
			if(s.startsWith("file:///")) s = s.substring("file:///".length());
			rootsVector.addElement(s);
		}
	}
	

	public static void loadConfig() {
		RecordStore r = null;
		try {
			r = RecordStore.openRecordStore(CONFIG_RECORD_NAME, false);
		} catch (Exception e) {
		}
		if(r == null) {
			// Defaults
					String downloadDir = System.getProperty("fileconn.dir.videos");
					if(downloadDir == null)
						downloadDir = System.getProperty("fileconn.dir.photos");
					if(downloadDir == null)
						downloadDir = "C:/";
					else if(downloadDir.startsWith("file:///"))
						downloadDir = downloadDir.substring("file:///".length());
					App.downloadDir = downloadDir;
			
		} else {
			try {
				JSONObject j = JSON.getObject(new String(r.getRecord(1)));
				r.closeRecordStore();
				if(j.has("region"))
					App.region = j.getString("region");
				if(j.has("downloadDir"))
					App.downloadDir = j.getString("downloadDir");
				if(j.has("serverstream"))
					App.serverstream = j.getString("serverstream");
				if(j.has("inv"))
					App.inv = j.getString("inv");
				if(j.has("startScreen"))
					App.startScreen = j.getInt("startScreen");
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void applySettings() {
		try {
			App.region = regionText.getString();
			String dir = downloadDirText.getString();
			//dir = Util.replace(dir, "/", dirsep);
			dir = Util.replace(dir, "\\", Path_separator);
			while (dir.endsWith(Path_separator)) {
				dir = dir.substring(0, dir.length() - 1);
			}
			App.downloadDir = dir;
			App.serverstream = httpProxyText.getString();
			App.inv = invidiousText.getString();
			saveConfig();
		} catch (Exception e) {
			e.printStackTrace();
			App.error(this, Errors.Settings_apply, e.toString());
		}
	}
	
	public static void saveConfig() {
		try {
			RecordStore.deleteRecordStore(CONFIG_RECORD_NAME);
		} catch (Throwable e) {
		}
		try {
			RecordStore r = RecordStore.openRecordStore(CONFIG_RECORD_NAME, true);
			JSONObject j = new JSONObject();
			j.put("v", "\"v1\"");
			j.put("region", "\"" + App.region + "\"");
			j.put("downloadDir", "\"" + App.downloadDir + "\"");
			j.put("serverstream", "\"" + App.serverstream + "\"");
			j.put("inv", "\"" + App.inv + "\"");
			j.put("startScreen", new Integer(App.startScreen));
			byte[] b = j.build().getBytes();
			
			r.addRecord(b, 0, b.length);
			r.closeRecordStore();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void dirListOpen(String f, String title) {
		dirList = new List(title, List.IMPLICIT);
		dirList.addCommand(backCmd);
		dirList.setCommandListener(this);
		dirList.addCommand(dirSelectCmd);
		dirList.append("- " + Locale.s(CMD_Select), null);
		try {
			FileConnection fc = (FileConnection) Connector.open("file:///" + f);
			Enumeration list = fc.list();
			while(list.hasMoreElements()) {
				String s = (String) list.nextElement();
				if(s.endsWith("/")) {
					dirList.append(s.substring(0, s.length() - 1), null);
				}
			}
			fc.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		App.display(dirList);
	}
	
	public void commandAction(Command c, Displayable d) {
		if(d == dirList) {
			if(c == backCmd) {
				if(curDir == null) {
					dirList = null;
					App.display(this);
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
				if(is.equals("- " + Locale.s(CMD_Select))) {
					dirList = null;
					downloadDirText.setString(f);
					curDir = null;
					App.display(this);
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
				App.display(this);
			}
			return;
		}
		applySettings();
		App.display(null);
	}

	public void commandAction(Command c, Item item) {
		if(c == dirCmd) {
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
		}
	}

}
