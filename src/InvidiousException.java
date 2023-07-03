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

public class InvidiousException extends RuntimeException {

	private JSONObject json;
	private String url;

	InvidiousException(JSONObject j) {
		super(j.getNullableString("error"));
		json = j;
	}

	InvidiousException(JSONObject j, String msg, String url) {
		super(msg);
		json = j;
		this.url = url;
	}
	
	JSONObject getJSON() {
		return json;
	}
	
	public String toString() {
		return "API error: " + getMessage();
	}
	
	String toErrMsg() {
		boolean j = json != null;
		boolean bt = j && json.has("backtrace");
		boolean u = url != null;
		return  (!bt && j ? "Raw json: " + json.build() : "") + (u ? " \nURL: " + url : "") + (bt ? " \nBacktrace: " + json.getString("backtrace") : "");
	}
	
	String getUrl() {
		return url;
	}

}
