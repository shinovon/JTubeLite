

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
		Item t = new StringItem(null, video.getTitle());
		append(t);
		Item author = new StringItem(null, video.getAuthor());
		append(author);
		Item vi = new StringItem(Locale.s(TXT_Views), Locale.views(video.getViewCount()));
		append(vi);
		Item date = new StringItem(Locale.s(TXT_Published), video.getPublishedText());
		append(date);
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
