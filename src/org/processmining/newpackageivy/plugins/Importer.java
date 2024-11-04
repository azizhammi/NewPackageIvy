package org.processmining.newpackageivy.plugins;

import org.processmining.newpackageivy.plugins.ImportOCEL1;
import org.processmining.newpackageivy.plugins.OCELdata.*;


public class Importer {
	public static OCELEventLog importFromFile(String logPath) {
		if (logPath.contains("jsonocel")) {
			ImportOCEL1 importer = new ImportOCEL1(logPath);
			OCELEventLog log = importer.doImport();
			return log;
		}
	}
}
