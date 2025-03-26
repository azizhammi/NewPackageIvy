package org.processmining.newpackageivy.plugins.OCELdata;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import java.util.Date;

public class OCELEvent {
	
	public OCELEventLog eventLog;
	public String id;
	public String activity;
	public Date timestamp;
	public Map<String, String> relatedObjectsIdentifiers; // <ID , role>
	public Map<OCELObject, String> relatedObjects;
	public Map<String, Object> attributes;
	
	public OCELEvent(OCELEventLog eventLog) {
	    this.eventLog = eventLog;
	    this.relatedObjectsIdentifiers = new HashMap<String, String>();
	    this.relatedObjects = new HashMap<OCELObject, String>();
	    this.attributes = new HashMap<String, Object>();
	}
	
	
	
	public void register() {
		
	    for (String relatedObject : relatedObjectsIdentifiers.keySet()) { 
	        OCELObject ob = this.eventLog.objects.get(relatedObject);
	        
	        if (ob != null) {
	            this.relatedObjects.put(ob, relatedObjectsIdentifiers.get(relatedObject));
	            ob.relatedEvents.add(this);
	        }
	    }
	    
	    for (String attribute : attributes.keySet()) {
	        ((Set<String>)this.eventLog.globalLog.get("ocel:attribute-names")).add(attribute);
	    }
	}
	
	public OCELEvent clone() {
	    OCELEvent newEvent = new OCELEvent(this.eventLog);
	    newEvent.id = this.id;
	    newEvent.activity = this.activity;
	    newEvent.timestamp = this.timestamp;
	    newEvent.attributes = new HashMap<String, Object>(this.attributes);
	    return newEvent;
	}
	
	
	// test
	// New constructor with id, activity, and timestamp
	public OCELEvent(OCELEventLog eventLog, String id, String activity, Date timestamp) {
	    this(eventLog);  // Call the existing constructor with eventLog
	    this.id = id;
	    this.activity = activity;
	    this.timestamp = timestamp;
	    this.relatedObjectsIdentifiers = new HashMap<String, String>();
	    this.relatedObjects = new HashMap<OCELObject, String>();
	    this.attributes = new HashMap<String, Object>();
	}

	public void addRelatedObject(OCELObject object, String role) {
	    if (this.relatedObjects == null) {
	        this.relatedObjects = new HashMap<OCELObject, String>();  // Ensure relatedObjects is initialized
	    }
	    this.relatedObjects.put(object, role);
	}

}
