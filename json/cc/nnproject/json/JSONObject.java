package cc.nnproject.json;

import java.util.Hashtable;

public class JSONObject extends AbstractJSON {

	private Hashtable table;

	public JSONObject() {
		this.table = new Hashtable();
	}

	public JSONObject(Hashtable table) {
		this.table = table;
	}
	
	public boolean has(String name) {
		return table.containsKey(name);
	}
	
	public Object get(String name) throws JSONException {
		try {
			if (has(name)) {
				if (JSON.parse_members) {
					return table.get(name);
				} else {
					Object o = table.get(name);
					if (o instanceof String)
						table.put(name, o = JSON.parseJSON((String) o));
					return o;
				}
			}
		} catch (JSONException e) {
			throw e;
		} catch (Exception e) {
		}
		throw new JSONException("No value for name: " + name);
	}
	
	public Object get(String name, Object def) {
		if(!has(name)) return def;
		try {
			return get(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public Object getNullable(String name) {
		return get(name, null);
	}
	
	public String getString(String name) throws JSONException {
		return get(name).toString();
	}
	
	public String getString(String name, String def) {
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
	
	public String getNullableString(String name) {
		return getString(name, null);
	}
	
	public JSONObject getObject(String name) throws JSONException {
		try {
			return (JSONObject) get(name);
		} catch (ClassCastException e) {
			throw new JSONException("Not object: " + name);
		}
	}
	
	public JSONObject getNullableObject(String name) {
		if(!has(name)) return null;
		try {
			return getObject(name);
		} catch (Exception e) {
			return null;
		}
	}
	
	public JSONArray getArray(String name) throws JSONException {
		try {
			return (JSONArray) get(name);
		} catch (ClassCastException e) {
			throw new JSONException("Not array: " + name);
		}
	}
	
	public JSONArray getNullableArray(String name) {
		if(!has(name)) return null;
		try {
			return getArray(name);
		} catch (Exception e) {
			return null;
		}
	}
	
	public Double getNumber(String name) throws JSONException {
		return JSON.getDouble(get(name));
	}
	
	public int getInt(String name) throws JSONException {
		return getNumber(name).intValue();
	}
	
	public int getInt(String name, int def) {
		if(!has(name)) return def;
		try {
			return getInt(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public long getLong(String name) throws JSONException {
		return getNumber(name).longValue();
	}

	public long getLong(String name, long def) {
		if(!has(name)) return def;
		try {
			return getLong(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public double getDouble(String name) throws JSONException {
		return getNumber(name).doubleValue();
	}

	public double getDouble(String name, double def) {
		if(!has(name)) return def;
		try {
			return getDouble(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public boolean getBoolean(String name) throws JSONException {
		Object o = get(name);
		if(o instanceof Boolean) return ((Boolean) o).booleanValue();
		if(o instanceof Integer) return ((Integer) o).intValue() > 0;
		if(o instanceof String) {
			String s = (String) o;
			if(s.equals("1") || s.equals("true") || s.equals("TRUE")) return true;
			else if(s.equals("0") || s.equals("false") || s.equals("FALSE") || s.equals("-1")) return false;
		}
		throw new JSONException("Not boolean: " + o);
	}

	public boolean getBoolean(String name, boolean def) {
		if(!has(name)) return def;
		try {
			return getBoolean(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public boolean isNull(String name) throws JSONException {
		return JSON.isNull(get(name));
	}

	public String build() {
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
					v = JSON.parseJSON((String) v);
				}
			} catch (JSONException e) {
			}
			if (v instanceof JSONObject) {
				s += ((JSONObject) v).build();
			} else if (v instanceof JSONArray) {
				s += "[]";
			} else if (v instanceof String) {
				s += "\"" + (String) v + "\"";
			} else s += v.toString();
			if(!elements.hasMoreElements()) {
				return s + "}";
			}
			s += ",";
		}
	}
	
	public void put(String name, Object obj) {
		table.put(name, JSON.getJSON(obj));
	}
	
	public void clear() {
		table.clear();
	}
	
	public int size() {
		return table.size();
	}
	
	public String toString() {
		return table.toString();
	}

}
