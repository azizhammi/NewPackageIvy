package org.processmining.newpackageivy.plugins.OCELdata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Date;

public class OCELObject {
	
	public OCELEventLog eventLog;
    public String id;
    public OCELObjectType objectType;
    public Set<OCELEvent> relatedEvents;
    public List<OCELEvent> sortedRelatedEvents;
    public Map<String, String> relatedObjectIdentifiers;
    
    
    public Map<String, Object> attributes; //the first value of the object attribute value
    
    public Map<String, Map<Date, Object>> timedAttributes; //the evolution of the object attribute values
    
    
    public OCELObject(OCELEventLog eventLog) {
        this.eventLog = eventLog;
        this.relatedEvents = new HashSet<OCELEvent>(); // avoid to have the same event
        this.attributes = new HashMap<String, Object>();
        this.sortedRelatedEvents = new ArrayList<OCELEvent>();
        this.relatedObjectIdentifiers = new HashMap<String, String>();
        this.timedAttributes = new HashMap<String, Map<Date, Object>>();
    }
    
    //add the object to its objectType
    public void register() {
        this.objectType.objects.add(this);
        this.sortEvents();
    }
    
    //sort the related events
    public void sortEvents() {
        this.sortedRelatedEvents = new ArrayList<OCELEvent>(relatedEvents);
		Collections.sort(this.sortedRelatedEvents, new OCELEventComparator());
    }
    
    
}
