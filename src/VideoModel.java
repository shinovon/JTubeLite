

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;

import cc.nnproject.json.JSONObject;

public class VideoModel extends AbstractModel implements ItemCommandListener {

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
	private boolean fromSearch;
	
	private Form formContainer;

	// create model without parsing
	public VideoModel(String id) {
		videoId = id;
	}

	public VideoModel(JSONObject j) {
		this(j, false);
	}

	public VideoModel(JSONObject j, boolean extended) {
		parse(j, extended);
	}

	public VideoModel(JSONObject j, Form form) {
		this(j, false);
		this.formContainer = form;
	}

	private void parse(JSONObject j, boolean extended) {
		this.extended = extended;
		videoId = j.getString("videoId");
		title = j.getNullableString("title");
		author = j.getNullableString("author");
		authorId = j.getNullableString("authorId");
		if(extended) {
			viewCount = j.getInt("viewCount", 0);
			description = j.getNullableString("description");
			publishedText = j.getNullableString("publishedText");
		}
		j = null;
	}
	
	public VideoModel extend() throws InvidiousException, IOException {
		if(!extended) {
			parse((JSONObject) App.invApi("v1/videos/" + videoId + "?", VIDEO_EXTENDED_FIELDS), true);
		}
		return this;
	}
	private Item makeItem() {
		return new StringItem(author, title);
	}

	public Item makeItemForList() {
		Item i = makeItem();
		i.addCommand(vOpenCmd);
		i.setDefaultCommand(vOpenCmd);
		i.setItemCommandListener(this);
		return i;
	}

	public Item makeAuthorItem() {
		Item i = new StringItem(null, getAuthor());
		i.addCommand(vOpenChannelCmd);
		i.setItemCommandListener(this);
		return i;
	}

	public void commandAction(Command c, Item item) {
		if(c == vOpenCmd || c == null) {
			App.open(this, formContainer);
		}
		if(c == vOpenChannelCmd) {
			if(formContainer != null) {
				App.display(formContainer);
				return;
			}
			App.open(new ChannelModel(authorId, author));
		}
	}

	public String getTitle() {
		return title;
	}

	public String getVideoId() {
		return videoId;
	}

	public String getAuthor() {
		return author;
	}

	public String getAuthorId() {
		return authorId;
	}

	public String getDescription() {
		return description;
	}

	public int getViewCount() {
		return viewCount;
	}

	//public long getPublished() {
	//	return published;
	//}

	public String getPublishedText() {
		return publishedText;
	}

	public int getLengthSeconds() {
		return lengthSeconds;
	}

	public void load() {
	}

	public void setFromSearch() {
		fromSearch = true;
	}
	
	public boolean isFromSearch() {
		return fromSearch;
	}

	public void disposeExtendedVars() {
		extended = false;
		authorId = null;
		description = null;
		publishedText = null;
	}

	public boolean isExtended() {
		return extended;
	}

	public ModelForm makeForm() {
		return new VideoForm(this);
	}

	public void setFormContainer(Form form) {
		this.formContainer = form;
	}

	public void dispose() {
		
	}

}
