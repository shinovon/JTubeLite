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
import java.util.Hashtable;

class JSONObject {

	private Hashtable table;

	JSONObject() {
		this.table = new Hashtable();
	}

	JSONObject(Hashtable table) {
		this.table = table;
	}
	
	boolean has(String name) {
		return table.containsKey(name);
	}
	
	Object get(String name) throws RuntimeException {
		try {
			if (has(name)) {
				if (App.parse_members) {
					return table.get(name);
				} else {
					Object o = table.get(name);
					if (o instanceof String)
						table.put(name, o = App.parseJSON((String) o));
					return o;
				}
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
		}
		throw new RuntimeException("No value for name: " + name);
	}
	
	Object get(String name, Object def) {
		if(!has(name)) return def;
		try {
			return get(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	Object getNullable(String name) {
		return get(name, null);
	}
	
	String getString(String name) throws RuntimeException {
		return get(name).toString();
	}
	
	String getString(String name, String def) {
		try {
			Object o = get(name, def);
			if(o == null || o instanceof String) {
				return (String) o;
			}
			return o.toString();
		} catch (Exception e) {
			return def;
		}
	}
	
	String getNullableString(String name) {
		return getString(name, null);
	}
	
	JSONObject getObject(String name) throws RuntimeException {
		try {
			return (JSONObject) get(name);
		} catch (ClassCastException e) {
			throw new RuntimeException("Not object: " + name);
		}
	}
	
	JSONObject getNullableObject(String name) {
		if(!has(name)) return null;
		try {
			return getObject(name);
		} catch (Exception e) {
			return null;
		}
	}
	
	JSONArray getArray(String name) throws RuntimeException {
		try {
			return (JSONArray) get(name);
		} catch (ClassCastException e) {
			throw new RuntimeException("Not array: " + name);
		}
	}
	
	JSONArray getNullableArray(String name) {
		if(!has(name)) return null;
		try {
			return getArray(name);
		} catch (Exception e) {
			return null;
		}
	}
	
	int getInt(String name) throws RuntimeException {
		return (int) App.getLong(get(name)).longValue();
	}
	
	int getInt(String name, int def) {
		if(!has(name)) return def;
		try {
			return getInt(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	long getLong(String name) throws RuntimeException {
		return App.getLong(get(name)).longValue();
	}

	long getLong(String name, long def) {
		if(!has(name)) return def;
		try {
			return getLong(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	boolean getBoolean(String name) throws RuntimeException {
		Object o = get(name);
		if(o == App.TRUE) return true;
		if(o == App.FALSE) return false;
		if(o instanceof Boolean) return ((Boolean) o).booleanValue();
		if(o instanceof String) {
			String s = (String) o;
			s = s.toLowerCase();
			if(s.equals("true")) return true;
			if(s.equals("false")) return false;
		}
		throw new RuntimeException("Not boolean: " + o);
	}

	boolean getBoolean(String name, boolean def) {
		if(!has(name)) return def;
		try {
			return getBoolean(name);
		} catch (Exception e) {
			return def;
		}
	}

	void put(String name, String s) {
		table.put(name, "\"".concat(s).concat("\""));
	}
	
	void put(String name, Object obj) {
		table.put(name, App.getJSON(obj));
	}
	
	void clear() {
		table.clear();
	}
	
	int size() {
		return table.size();
	}
	
	public String toString() {
		return table.toString();
	}

	String build() {
		int l = size();
		if (l == 0)
			return "{}";
		String s = "{";
		java.util.Enumeration elements = table.keys();
		while (true) {
			String k = elements.nextElement().toString();
			s += "\"" + k + "\":";
			Object v = null;
			try {
				v = table.get(k);
				if(v instanceof String) {
					v = App.parseJSON((String) v);
				}
			} catch (RuntimeException e) {
			}
			if (v instanceof String) {
				s += "\"" + (String) v + "\"";
			} else s += v.toString();
			if(!elements.hasMoreElements()) {
				return s + "}";
			}
			s += ",";
		}
	}
}
