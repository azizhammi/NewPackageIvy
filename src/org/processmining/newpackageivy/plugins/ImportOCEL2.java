package org.processmining.newpackageivy.plugins;


import java.io.FileFilter;
import java.io.InputStream;
import java.lang.Object;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.Collection;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.newpackageivy.algorithms.YourAlgorithm;
import org.processmining.newpackageivy.connections.YourConnection;
import org.processmining.newpackageivy.dialogs.YourDialog;
import org.processmining.newpackageivy.help.YourHelp;
import org.processmining.newpackageivy.models.YourFirstInput;
import org.processmining.newpackageivy.models.YourOutput;
import org.processmining.newpackageivy.models.YourSecondInput;
import org.processmining.newpackageivy.parameters.YourParameters;

import org.processmining.newpackageivy.plugins.OCELdata.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

import java.util.List;
import java.util.Set;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import java.util.Date;

import java.io.InputStream;
import java.io.IOException;
import java.util.Date;

import java.lang.Object;
import java.io.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;


@Plugin(name = "OCEL 2.0 JSON", parameterLabels = { "Filename" }, returnLabels = {
"Object-Centric Event Log" }, returnTypes = { OCELEventLog.class })
@UIImportPlugin(description = "OCEL 2.0 JSON", extensions = { "jsonocel", "json" })

public class ImportOCEL2 extends AbstractImportPlugin{

	
	protected FileNameExtensionFilter getFileFilter() {
        return (FileNameExtensionFilter) new FileNameExtensionFilter("OCEL 2.0 files", "json");
    }
	
	protected OCELEventLog importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes) throws Exception {
	    // Initialize an empty event log
	    OCELEventLog ocelLog = new OCELEventLog();
	    
	    // Parse the input stream (JSON) and populate ocelLog
	    parseOcelJson(input, ocelLog);
	    
	    // Register events and objects in ocelLog
	    ocelLog.register();
	    
	    return ocelLog;
	}
	
	public void parseOcelJson(InputStream input, OCELEventLog ocelLog) throws IOException {
	    // Initialize JSON parser
	    ObjectMapper mapper = new ObjectMapper();
	    JsonNode rootNode = mapper.readTree(input);

	    // Parse global attributes
	    JsonNode globalLog = rootNode.path("ocel:global-log");
	    parseGlobalLog(globalLog, ocelLog);

	    // Parse objects
	    JsonNode objectsNode = rootNode.path("ocel:objects");
	    parseObjects(objectsNode, ocelLog);

	    // Parse events
	    JsonNode eventsNode = rootNode.path("ocel:events");
	    parseEvents(eventsNode, ocelLog);
	}
	
	private void parseGlobalLog(JsonNode globalLogNode, OCELEventLog ocelLog) {
	    // Set version and ordering metadata in the OCEL log
	    ocelLog.globalLog.put("ocel:version", globalLogNode.path("ocel:version").asText());
	    ocelLog.globalLog.put("ocel:ordering", globalLogNode.path("ocel:ordering").asText());
	}
	
	private void parseObjects(JsonNode rootNode, OCELEventLog eventLog) {
	    JsonNode objectsNode = rootNode.path("ocel:objects");

	    // Iterate through each object in the JSON array
	    if (objectsNode.isObject()) {  // Check if the node is an object
	    	
	        for (Iterator<Map.Entry<String, JsonNode>> it = objectsNode.fields(); it.hasNext();) {
	            Map.Entry<String, JsonNode> objectEntry = it.next();
	            String objectId = objectEntry.getKey(); // Get the object ID
	            JsonNode objectDetails = objectEntry.getValue(); // Get the object details

	            OCELObject ocelObject = new OCELObject(eventLog);
	            ocelObject.id = objectId; // Set the ID

	            // Parse object type
	            String objectTypeName = objectDetails.path("ocel:type").asText(); // Get the object type
	            OCELObjectType objectType = new OCELObjectType(eventLog, objectTypeName); // Create or get the object type
	            ocelObject.objectType = objectType; // Set the object type
	            
	            // Parse object attributes
	            if (objectDetails.has("ocel:attributes") && objectDetails.path("ocel:attributes").isObject()) {
	            	
	                for (Iterator<Map.Entry<String, JsonNode>> itAtt = objectDetails.path("ocel:attributes").fields(); itAtt.hasNext();) {
	                    Map.Entry<String, JsonNode> attributeEntry = itAtt.next();
	                    String attributeName = attributeEntry.getKey(); // Get the attribute name
	                    JsonNode attributeValue = attributeEntry.getValue(); // Get the attribute value

	                    // Store the attribute in the object
	                    ocelObject.attributes.put(attributeName, attributeValue.asText());
	                }
	                
	            }

	            // Register the object in the event log
	            eventLog.objects.put(ocelObject.id, ocelObject);
	        }
	    } else {
	        System.err.println("Expected ocel:objects to be an object.");
	    }
	}

	
	private void parseEvents(JsonNode eventsNode, OCELEventLog ocelLog) {
	    for (JsonNode eventNode : eventsNode) {
	        

	        String eventId = eventNode.path("ocel:id").asText();
	        String activity = eventNode.path("ocel:activity").asText();
	        
	        
	        Date timestamp = new Date(eventNode.path("ocel:timestamp").asLong());

	        // Create and configure OcelEvent
	        OCELEvent event = new OCELEvent(ocelLog);
	        event.id = eventId;
	        event.activity = activity;
	        event.timestamp = timestamp;

	        // Parse related objects
	        for (JsonNode objRefNode : eventNode.path("ocel:related_objects")) {
	            String objectId = objRefNode.asText();
	            event.relatedObjectsIdentifiers.put(objectId, objectId);
	        }

	        // Parse event attributes
	        JsonNode attributesNode = eventNode.path("ocel:attributes");
	        if (attributesNode.isObject()) { // Check if it's an object
	            for (Iterator<Map.Entry<String, JsonNode>> it = attributesNode.fields(); it.hasNext(); ) {
	                Map.Entry<String, JsonNode> attribute = it.next();
	                String attributeName = attribute.getKey();
	                event.attributes.put(attributeName, attribute.getValue().asText());
	            }
	        } else {
	            // Handle unexpected types or log information
	            System.out.println("The 'ocel:attributes' field is of unexpected type. Attributes Node");
	        }

	        // Add event to event log
	        ocelLog.events.put(eventId, event);
	    }
	}


}



