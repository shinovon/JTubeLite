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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

class Channel extends Model implements CommandListener, Constants {
	
	private String author;
	private String authorId;
	private String description;
	private int subCount;

	private boolean extended;

	ModelForm form;

	private Object[] mainItems = new Object[30];
	private Object[] searchItems = new Object[30];
	
	private List container;

	public Channel(JSONObject o) {
		parse(o, false);
	}
	
	public Channel(JSONObject o, boolean extended) {
		parse(o, extended);
	}
	
	public Channel(String id, String name) {
		this.author = name;
		this.authorId = id;
	}

	private void parse(JSONObject o, boolean extended) {
		this.extended = extended;
		author = o.getString("author");
		authorId = o.getString("authorId");
		if(extended) {
			description = o.getString("description", "");
			subCount = o.getInt("subCount", 0);
		}
	}
	
	public Channel extend() throws InvidiousException, IOException {
		if(!extended) {
			parse((JSONObject) App.invApi("channels/" + authorId + "?", CHANNEL_EXTENDED_FIELDS), true);
		}
		return this;
	}

	public String makeItemForList() {
		return author;
	}
/*
	public void commandAction(Command c, Item arg1) {
		if(c == searchVideosCmd) {
			if(searchList != null) {
				disposeSearchForm();
			}
			TextBox t = new TextBox("", "", 256, TextField.ANY);
			t.setCommandListener(this);
			t.setTitle(App.s(CMD_Search));
			t.addCommand(App.searchOkCmd);
			t.addCommand(App.cancelCmd);
			App.display(t);
		}
		if(c == lastVideosCmd) {
			latestVideos();
		}
		if(c == cOpenCmd || c == null) {
			App.open(this);
		}
	}
*/

	public ModelForm makeForm() {
		form = new ModelForm(author, this);
		form.setCommandListener(this);
		form.addCommand(App.backCmd);
		return form;
	}
	
	// channel form
	
	private static final Command lastVideosCmd = new Command("Latest videos", Command.ITEM, 2);
	private static final Command searchVideosCmd = new Command("Search videos", Command.ITEM, 3);
	
	private List lastVideosList;
	private List searchList;
	
	private void init() {
		form.append(author + "\n" + App.subscribers(subCount) + "\n" + description);
		form.addCommand(lastVideosCmd);
		form.addCommand(searchVideosCmd);
	}

	public void run() {
		try {
			if(!extended) {
				extend();
				init();
			}
		} catch (Exception e) {
			App.error(this, Errors.ChannelForm_load, e.toString());
		}
	}

	private void latestVideos() {
		lastVideosList = new List(author + " - " + "Latest videos", List.IMPLICIT);
		lastVideosList.setCommandListener(this);
		lastVideosList.addCommand(List.SELECT_COMMAND);
		lastVideosList.addCommand(App.backCmd);
		lastVideosList.addCommand(App.settingsCmd);
		lastVideosList.addCommand(App.searchCmd);
		lastVideosList.append("Loading", null);
		App.display(lastVideosList);
		try {
			JSONArray j = ((JSONObject) App.invApi("channels/" + authorId + "/latest?", "title,videoId,author,videos")).getArray("videos");
			int l = j.size();
			lastVideosList.delete(0);
			for(int i = 0; i < l; i++) {
				if(i >= LATESTVIDEOS_LIMIT) break;
				lastVideosList.append(parseAndMakeItem(j.getObject(i), false, i), null);
			}
		} catch (Exception e) {
			App.error(this, Errors.ChannelForm_latestVideos, e);
		}
	}

	private void search(String q) {
		searchList = new List(NAME + " - " + "Search query", List.IMPLICIT);
		searchList.setCommandListener(this);
		searchList.addCommand(List.SELECT_COMMAND);
		searchList.addCommand(App.backCmd);
		searchList.addCommand(App.settingsCmd);
		searchList.addCommand(App.searchCmd);
		searchList.append("Loading", null);
		App.display(searchList);
		try {
			JSONArray j = (JSONArray) App.invApi("channels/search/" + authorId + "?q=" + App.url(q), VIDEO_FIELDS);
			int l = j.size();
			searchList.delete(0);
			for(int i = 0; i < l; i++) {
				if(i >= SEARCH_LIMIT) break;
				searchList.append(parseAndMakeItem(j.getObject(i), true, i), null);
			}
		} catch (Exception e) {
			App.error(this, Errors.ChannelForm_search, e);
		}
	}

	private String parseAndMakeItem(JSONObject j, boolean search, int i) {
		Video v = new Video(j, search ? searchList : lastVideosList);
		(search ? searchItems : mainItems)[i] = v;
		if(search) v.fromSearch = true;
		return v.makeItemForList();
	}

	public void commandAction(Command c, Displayable d) {
		if(c == List.SELECT_COMMAND) {
			int i = ((List)d).getSelectedIndex();
			if(i == -1) return;
			App.open((Model) (d == searchList ? searchItems : mainItems)[i]);
			return;
		}
		if(c == lastVideosCmd) {
			latestVideos();
			return;
		}
		if(c == App.searchCmd || c == searchVideosCmd) {
			searchList = null;
			TextBox t = new TextBox("", "", 256, TextField.ANY);
			t.setCommandListener(this);
			t.setTitle("Search");
			t.addCommand(App.searchOkCmd);
			t.addCommand(App.cancelCmd);
			App.display(t);
			return;
		}
		if(c == App.searchOkCmd && d instanceof TextBox) {
			search(((TextBox) d).getString());
			return;
		}
		if(d == searchList && c == App.backCmd) {
			App.display(form);
			searchList = null;
			return;
		}
		if(d == lastVideosList && c == App.backCmd) {
			App.display(form);
			lastVideosList = null;
			return;
		}
		if(d == form && c == App.backCmd) {
			if(container != null) {
				App.display(container);
			} else {
				App.back(form);
			}
			dispose();
			App.disposeChannelForm();
			return;
		}
		App.inst.commandAction(c, d);
	}

	public void dispose() {
		lastVideosList = null;
		searchList = null;
		extended = false;
		form = null;
	}

	public void setContainer(List form) {
		container = form;
	}

}
