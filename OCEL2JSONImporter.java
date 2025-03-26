package org.processmining.newpackageivy.plugins;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;

import org.processmining.newpackageivy.plugins.OCELdata.*;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Plugin(name = "Import OCEL 2.0 from JSON", parameterLabels = { "Filename" }, returnLabels = {
"Object-Centric Event Log" }, returnTypes = { OCELEventLog.class })
@UIImportPlugin(description = "Import OCEL 2.0 from JSON", extensions = { "jsonocel", "json" })

public class OCEL2JSONImporter extends AbstractImportPlugin {
	protected OCELEventLog importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		return doImportFromStream(input);
	}
	
    public OCELEventLog doImportFromStream(InputStream is0) {
        OCELEventLog eventLog = new OCELEventLog();
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode root = mapper.readTree(is0);

            // Parse objectTypes
            JsonNode objectTypesNode = root.get("objectTypes");
            if (objectTypesNode != null && objectTypesNode.isArray()) {
                for (JsonNode objectTypeNode : objectTypesNode) {
                    String name = objectTypeNode.get("name").asText();
                    OCELObjectType objectType = new OCELObjectType(eventLog, name);
                    eventLog.objectTypes.put(name, objectType);
                }
            }

            // Parse eventTypes
            JsonNode eventTypesNode = root.get("eventTypes");
            if (eventTypesNode != null && eventTypesNode.isArray()) {
                Set<String> eventTypeNames = new HashSet<>();
                for (JsonNode eventTypeNode : eventTypesNode) {
                    String name = eventTypeNode.get("name").asText();
                    eventTypeNames.add(name);
                    // Optionally process attributes of event types if needed
                }
                eventLog.globalLog.put("ocel:event-types", eventTypeNames);
            }

            // Parse objects
            JsonNode objectsNode = root.get("objects");
            if (objectsNode != null && objectsNode.isArray()) {
                for (JsonNode objectNode : objectsNode) {
                    String id = objectNode.get("id").asText();
                    String typeName = objectNode.get("type").asText();
                    OCELObjectType objectType = eventLog.objectTypes.get(typeName);
                    if (objectType == null) {
                        objectType = new OCELObjectType(eventLog, typeName);
                        eventLog.objectTypes.put(typeName, objectType);
                    }
                    OCELObject obj = new OCELObject(eventLog);
                    obj.id = id;
                    obj.objectType = objectType;
                    eventLog.objects.put(id, obj);

                    // Process attributes
                    JsonNode attributesNode = objectNode.get("attributes");
                    if (attributesNode != null && attributesNode.isArray()) {
                        for (JsonNode attributeNode : attributesNode) {
                            String attrName = attributeNode.get("name").asText();
                            String timeStr = attributeNode.get("time").asText();
                            String value = attributeNode.get("value").asText();
                            Date time = parseTime(timeStr);
                            if (!obj.attributes.containsKey(attrName)) {
                                obj.attributes.put(attrName, value);
                            } else {
                                Map<Date, Object> timeValues = obj.timedAttributes.get(attrName);
                                if (timeValues == null) {
                                    timeValues = new HashMap<>();
                                    obj.timedAttributes.put(attrName, timeValues);
                                }
                                timeValues.put(time, value);
                            }
                        }
                    }

                    // Process relationships
                    JsonNode relationshipsNode = objectNode.get("relationships");
                    if (relationshipsNode != null && relationshipsNode.isArray()) {
                        for (JsonNode relationshipNode : relationshipsNode) {
                            String objectId = relationshipNode.get("objectId").asText();
                            String qualifier = relationshipNode.get("qualifier").asText();
                            obj.relatedObjectIdentifiers.put(objectId, qualifier);
                        }
                    }
                }
            }

            // Parse events
            JsonNode eventsNode = root.get("events");
            if (eventsNode != null && eventsNode.isArray()) {
                for (JsonNode eventNode : eventsNode) {
                    OCELEvent event = new OCELEvent(eventLog);
                    String id = eventNode.get("id").asText();
                    event.id = id;
                    String type = eventNode.get("type").asText();
                    event.activity = type;
                    String timeStr = eventNode.get("time").asText();
                    Date time = parseTime(timeStr);
                    event.timestamp = time;
                    eventLog.events.put(id, event);

                    // Process attributes
                    JsonNode attributesNode = eventNode.get("attributes");
                    if (attributesNode != null && attributesNode.isArray()) {
                        for (JsonNode attributeNode : attributesNode) {
                            String attrName = attributeNode.get("name").asText();
                            String value = attributeNode.get("value").asText();
                            event.attributes.put(attrName, value);
                        }
                    }

                    // Process relationships
                    JsonNode relationshipsNode = eventNode.get("relationships");
                    if (relationshipsNode != null && relationshipsNode.isArray()) {
                        for (JsonNode relationshipNode : relationshipsNode) {
                            String objectId = relationshipNode.get("objectId").asText();
                            String qualifier = relationshipNode.get("qualifier").asText();
                            event.relatedObjectsIdentifiers.put(objectId, qualifier);
                        }
                    }
                }
            }

            // Resolve related objects in events
            for (OCELEvent event : eventLog.events.values()) {
                Map<String, String> objectIds = event.relatedObjectsIdentifiers;
                for (Map.Entry<String, String> entry : objectIds.entrySet()) {
                    String objId = entry.getKey();
                    String qualifier = entry.getValue();
                    OCELObject obj = eventLog.objects.get(objId);
                    if (obj != null) {
                        event.relatedObjects.put(obj, qualifier);
                        obj.relatedEvents.add(event);
                    } else {
                        // Handle missing object if necessary
                    }
                }
            }

            // Register objects (sort events and resolve object relationships)
            for (OCELObject obj : eventLog.objects.values()) {
                obj.register();
            }

            // Register events
            for (OCELEvent event : eventLog.events.values()) {
                event.register();
            }

            // Update 'ocel:attribute-names' in eventLog.globalLog
            Set<String> attributeNames = (Set<String>) eventLog.globalLog.get("ocel:attribute-names");
            for (OCELEvent event : eventLog.events.values()) {
                attributeNames.addAll(event.attributes.keySet());
            }
            for (OCELObject obj : eventLog.objects.values()) {
                attributeNames.addAll(obj.attributes.keySet());
            }

            // Update 'ocel:object-types' in eventLog.globalLog
            Set<String> objectTypeNames = (Set<String>) eventLog.globalLog.get("ocel:object-types");
            objectTypeNames.addAll(eventLog.objectTypes.keySet());

        } catch (Exception e) {
            e.printStackTrace();
            // Handle exception as needed
        }

        return eventLog;
    }

    private Date parseTime(String timeStr) {
        try {
            OffsetDateTime odt = OffsetDateTime.parse(timeStr);
            return Date.from(odt.toInstant());
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}