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
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.List;

public class FileOperations {
	
	public static void fillDirList(String f, List dirList) throws IOException {
		FileConnection fc = (FileConnection) Connector.open("file:///" + f);
		Enumeration list = fc.list();
		while(list.hasMoreElements()) {
			String s = (String) list.nextElement();
			if(s.endsWith("/")) {
				dirList.append(s.substring(0, s.length() - 1), null);
			}
		}
		fc.close();
	}
	
	public static Vector getRoots() {
		Vector v = new Vector();
		Enumeration roots = FileSystemRegistry.listRoots();
		while(roots.hasMoreElements()) {
			String s = (String) roots.nextElement();
			if(s.startsWith("file:///")) s = s.substring("file:///".length());
			v.addElement(s);
		}
		return v;
	}

	public static StreamConnection createFile(String f) throws IOException {
		FileConnection fc = (FileConnection) Connector.open(f, Connector.READ_WRITE);
		
		if (fc.exists()) {
			try {
				fc.delete();
			} catch (IOException e) {
			}
		}
		fc.create();
		return fc;
	}

}
