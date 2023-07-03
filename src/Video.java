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
import javax.microedition.lcdui.StringItem;

class Video extends Model implements CommandListener {

	private String title;
	private String videoId;
	private String author;
	private String authorId;
	private String description;
	private int viewCount;
	//private long published;
	private String publishedText;
	private int lengthSeconds;

	private boolean extended;
	
	private List container;
	
	ModelForm form;

	// create model without parsing
	Video(String id) {
		videoId = id;
	}

	Video(JSONObject j) {
		this(j, false);
	}

	Video(JSONObject j, boolean extended) {
		parse(j, extended);
	}

	Video(JSONObject j, List form) {
		this(j, false);
		this.container = form;
	}

	private void parse(JSONObject j, boolean extended) {
		this.extended = extended;
		videoId = j.getString("videoId");
		title = j.getNullableString("title");
		author = j.getNullableString("author");
		if(extended) {
			authorId = j.getNullableString("authorId");
			viewCount = j.getInt("viewCount", 0);
			lengthSeconds = j.getInt("lengthSeconds", 0);
			description = j.getNullableString("description");
			publishedText = j.getNullableString("publishedText");
		}
		j = null;
	}
	
	Video extend() throws InvidiousException, IOException {
		if(!extended) {
			parse((JSONObject) App.invApi("videos/" + videoId + "?", VIDEO_EXTENDED_FIELDS), true);
		}
		return this;
	}

	String makeItemForList() {
		return title + " - " + author;
	}

	ModelForm makeForm() {
		form = new ModelForm(title, this);
		form.setCommandListener(this);
		form.addCommand(App.backCmd);
		form.addCommand(App.downloadCmd);
		form.addCommand(App.settingsCmd);
		form.addCommand(App.watchCmd);
		form.addCommand(App.viewChannelCmd);
		loadingItem = new StringItem(null, "Loading");
		//addCommand(browserCmd);
		if(extended) {
			init();
		} else {
			form.append(loadingItem);
		}
		return form;
		//return new VideoForm(this);
	}

	void setContainer(List form) {
		this.container = form;
	}
	
	// videoform

	private StringItem loadingItem;
	private void init() {
		try {
			if(form.get(0) == loadingItem) {
				form.delete(0);
			}
		} catch (Exception e) {
		}
		form.append(new StringItem(null, title + "\n"));
		form.append(new StringItem(null, author + "\n"));
		form.append(new StringItem("Duration", App.timeStr(lengthSeconds) + "\n"));
		form.append(new StringItem("Views", App.views(viewCount) + "\n"));
		form.append(new StringItem("Published", publishedText + "\n"));
		form.append(new StringItem("Description", description + "\n"));
	}

	void dispose() {
		extended = false;
		authorId = null;
		description = null;
		publishedText = null;
		form = null;
	}
	
	public void run() {
		try {
			if(!extended) {
				extend();
				init();
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			App.error(this, Errors.VideoForm_load, e.toString());
		}
	}
	
	public void commandAction(Command c, Displayable d) {
		if(c == App.watchCmd) {
			App.watch(videoId);
			return;
		}
		if(c == App.downloadCmd) {
			App.download(videoId);
			return;
		}
		if(c == App.backCmd) {
			if(container != null) {
				App.display(container);
			} else {
				App.back(form);
			}
			dispose();
			App.disposeVideoForm();
			return;
		}
		if(c == App.viewChannelCmd) {
			App.open(new Channel(authorId, author));
			return;
		}
		App.inst.commandAction(c, d);
	}
}
