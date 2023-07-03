import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.List;

class Files {
	static void fillDirList(String f, List dirList) throws IOException {
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
	
	static Vector _getRoots() {
		Vector v = new Vector();
		Enumeration roots = FileSystemRegistry.listRoots();
		while(roots.hasMoreElements()) {
			String s = (String) roots.nextElement();
			if(s.startsWith("file:///")) s = s.substring("file:///".length());
			v.addElement(s);
		}
		return v;
	}

	static StreamConnection createFile(String f) throws IOException {
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
