

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;

import cc.nnproject.json.JSONObject;

public class ChannelModel extends AbstractModel implements ItemCommandListener {
	
	private static final Command cOpenCmd = new Command(Locale.s(CMD_Open), Command.ITEM, 3);
	
	private String author;
	private String authorId;
	private String description;

	private boolean extended;
	private boolean fromSearch;

	public ChannelModel(JSONObject o) {
		parse(o, false);
	}
	
	public ChannelModel(JSONObject o, boolean extended) {
		parse(o, extended);
	}
	
	public ChannelModel(String id, String name) {
		this.author = name;
		this.authorId = id;
	}

	private void parse(JSONObject o, boolean extended) {
		this.extended = extended;
		author = o.getString("author");
		authorId = o.getString("authorId");
		if(extended) {
			description = o.getNullableString("description");
		}
	}
	
	public ChannelModel extend() throws InvidiousException, IOException {
		if(!extended) {
			parse((JSONObject) App.invApi("v1/channels/" + authorId + "?", CHANNEL_EXTENDED_FIELDS), true);
		}
		return this;
	}

	private Item makeItem() {
		return new StringItem(null, author);
	}

	public Item makeItemForList() {
		Item i = makeItem();
		i.addCommand(cOpenCmd);
		i.setDefaultCommand(cOpenCmd);
		i.setItemCommandListener(this);
		return i;
	}

	public Item makeItemForPage() {
		return makeItem();
	}

	public void load() {
	}

	public void commandAction(Command c, Item arg1) {
		if(c == cOpenCmd || c == null) {
			App.open(this);
		}
	}

	public void dispose() {
	}
	
	public void disposeExtendedVars() {
		extended = false;
	}

	public String getAuthor() {
		return author;
	}

	public String getAuthorId() {
		return authorId;
	}

	public void setFromSearch() {
		fromSearch = true;
	}
	
	public boolean isFromSearch() {
		return fromSearch;
	}

	public boolean isExtended() {
		return extended;
	}
	
	public String getDescription() {
		return description;
	}

	public ModelForm makeForm() {
		return new ChannelForm(this);
	}

}
