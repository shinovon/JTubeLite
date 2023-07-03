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
import java.util.Vector;

class JSONArray {

	private Vector vector;

	JSONArray(Vector vector) {
		this.vector = vector;
	}
	
	Object get(int index) throws RuntimeException {
		try {
			if (App.parse_members)
				return vector.elementAt(index);
			else {
				Object o = vector.elementAt(index);
				if (o instanceof String)
					vector.setElementAt(o = App.parseJSON((String) o), index);
				return o;
			}
		} catch (Exception e) {
		}
		throw new RuntimeException("No value at " + index);
	}
	
	Object get(int index, Object def) {
		try {
			return get(index);
		} catch (Exception e) {
			return def;
		}
	}
	
	String getString(int index) throws RuntimeException {
		return get(index).toString();
	}
	
	String getString(int index, String def) {
		try {
			return get(index).toString();
		} catch (Exception e) {
			return def;
		}
	}
	
	JSONObject getObject(int index) throws RuntimeException {
		try {
			return (JSONObject) get(index);
		} catch (ClassCastException e) {
			throw new RuntimeException("Not object at " + index);
		}
	}
	
	JSONObject getNullableObject(int index) {
		try {
			return getObject(index);
		} catch (Exception e) {
			return null;
		}
	}
	
	JSONArray getArray(int index) throws RuntimeException {
		try {
			return (JSONArray) get(index);
		} catch (ClassCastException e) {
			throw new RuntimeException("Not array at " + index);
		}
	}
	
	JSONArray getNullableArray(int index) {
		try {
			return getArray(index);
		} catch (Exception e) {
			return null;
		}
	}
	
	int getInt(int index) throws RuntimeException {
		return (int) App.getLong(get(index)).longValue();
	}
	
	int getInt(int index, int def) {
		try {
			return getInt(index);
		} catch (Exception e) {
			return def;
		}
	}
	
	long getLong(int index) throws RuntimeException {
		return App.getLong(get(index)).longValue();
	}

	long getLong(int index, long def) {
		try {
			return getLong(index);
		} catch (Exception e) {
			return def;
		}
	}
	
	boolean getBoolean(int index) throws RuntimeException {
		Object o = get(index);
		if(o == App.TRUE) return true;
		if(o == App.FALSE) return false;
		if(o instanceof Boolean) return ((Boolean) o).booleanValue();
		if(o instanceof String) {
			String s = (String) o;
			s = s.toLowerCase();
			if(s.equals("true")) return true;
			if(s.equals("false")) return false;
		}
		throw new RuntimeException("Not boolean: " + o + " (" + index + ")");
	}

	boolean getBoolean(int index, boolean def) {
		try {
			return getBoolean(index);
		} catch (Exception e) {
			return def;
		}
	}
	
	int size() {
		return vector.size();
	}
	
	public String toString() {
		return vector.toString();
	}
}
