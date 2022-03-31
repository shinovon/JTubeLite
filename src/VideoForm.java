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
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;

public class VideoForm extends ModelForm implements CommandListener, ItemCommandListener, Constants {

	private static App app = App.inst;
	
	private VideoModel video;

	private StringItem loadingItem;

	private Form formContainer;

	public VideoForm(VideoModel v) {
		super(v.getTitle());
		this.video = v;
		setCommandListener(this);
		addCommand(backCmd);
		addCommand(downloadCmd);
		addCommand(settingsCmd);
		addCommand(watchCmd);
		loadingItem = new StringItem(null, Locale.s(TITLE_Loading));
		//addCommand(browserCmd);
		if(v.isExtended()) {
			init();
		} else {
			append(loadingItem);
		}
	}

	private void init() {
		try {
			if(get(0) == loadingItem) {
				delete(0);
			}
		} catch (Exception e) {
		}
		append(new StringItem(null, video.getTitle()));
		append(new StringItem(null, video.getAuthor()));
		append(new StringItem(Locale.s(TXT_VideoDuration), Util.timeStr(video.getLengthSeconds())));
		append(new StringItem(Locale.s(TXT_Views), Locale.views(video.getViewCount())));
		append(new StringItem(Locale.s(TXT_Published), video.getPublishedText()));
		append(new StringItem(Locale.s(TXT_Description), video.getDescription()));
	}

	public void run() {
		try {
			if(!video.isExtended()) {
				video.extend();
				init();
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			App.error(this, Errors.VideoForm_load, e.toString());
		}
	}

	public void commandAction(Command c, Displayable d) {
		if(c == watchCmd) {
			App.watch(video.getVideoId());
			return;
		}
		if(c == downloadCmd) {
			App.download(video.getVideoId());
			return;
		}
		if(c == backCmd) {
			if(formContainer != null) {
				App.display(formContainer);
			} else {
				App.back(this);
			}
			app.disposeVideoForm();
			return;
		}
		App.inst.commandAction(c, d);
	}

	public void commandAction(Command c, Item i) {
		if(c == watchCmd) {
			App.watch(video.getVideoId());
		}
	}

	public void dispose() {
		video.disposeExtendedVars();
		video = null;
	}

	public VideoModel getVideo() {
		return video;
	}

	public AbstractModel getModel() {
		return getVideo();
	}

	public void setFormContainer(Form form) {
		formContainer = form;
	}

}
