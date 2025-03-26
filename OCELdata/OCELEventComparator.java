package org.processmining.newpackageivy.plugins.OCELdata;

import java.util.Comparator; // to compare different events

public class OCELEventComparator implements Comparator<OCELEvent>{
	
	public int compare(OCELEvent o1, OCELEvent o2) {
		// TODO Auto-generated method stub
		if (o1.timestamp.getTime() < o2.timestamp.getTime()) {
			return -1;
		}
		else if (o1.timestamp.getTime() > o2.timestamp.getTime()) {
			return 1;
		}
		return o1.id.compareTo(o2.id);
	}

}
