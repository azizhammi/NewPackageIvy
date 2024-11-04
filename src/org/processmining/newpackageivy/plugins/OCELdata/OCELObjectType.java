package org.processmining.newpackageivy.plugins.OCELdata;

import java.util.Set;
import java.util.HashSet;


public class OCELObjectType {

	
	public OCELEventLog eventLog; // The event log to which this object type belongs.
    public String name; // Name of the object type
    public Set<OCELObject> objects; // the objects of this type in the event log.

    
    public OCELObjectType(OCELEventLog eventLog, String name) {
        this.eventLog = eventLog;
        this.name = name; // the type name.
        this.objects = new HashSet<OCELObject>(); // Initializes the  objects for this type.
    }
}
