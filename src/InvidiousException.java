import cc.nnproject.json.JSONObject;

public class InvidiousException extends RuntimeException {

	private JSONObject json;
	private String url;

	public InvidiousException(JSONObject j) {
		super(j.getNullableString("error"));
		json = j;
	}

	public InvidiousException(JSONObject j, String msg, String url) {
		super(msg);
		json = j;
		this.url = url;
	}
	
	public JSONObject getJSON() {
		return json;
	}
	
	public String toString() {
		return "API error: " + getMessage();
	}
	
	public String toErrMsg() {
		boolean j = json != null;
		boolean bt = j && json.has("backtrace");
		boolean u = url != null;
		return  (!bt && j ? "Raw json: " + json.build() : "") + (u ? " \nURL: " + url : "") + (bt ? " \nBacktrace: " + json.getString("backtrace") : "");
	}
	
	public String getUrl() {
		return url;
	}

}
