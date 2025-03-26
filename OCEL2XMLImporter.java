package org.processmining.newpackageivy.plugins;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import org.processmining.newpackageivy.plugins.OCELdata.*;


@Plugin(name = "Import OCEL 2.0 from XML", parameterLabels = { "Filename" }, returnLabels = {
"Object-Centric Event Log" }, returnTypes = { OCELEventLog.class })
@UIImportPlugin(description = "Import OCEL 2.0 from XML", extensions = { "xmlocel", "xml", "xml2" })

public class OCEL2XMLImporter extends AbstractImportPlugin {
	
	protected OCELEventLog importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		return doImportFromStream(input);
	}
	
    public OCELEventLog doImportFromStream(InputStream is0) {
        OCELEventLog eventLog = new OCELEventLog();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is0);

            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement(); 

            // Parse object-types
            NodeList objectTypeNodes = root.getElementsByTagName("object-types");
            if (objectTypeNodes.getLength() > 0) {
                Element objectTypesElement = (Element) objectTypeNodes.item(0);
                NodeList objectTypeList = objectTypesElement.getElementsByTagName("object-type");
                for (int i = 0; i < objectTypeList.getLength(); i++) {
                    Element objectTypeElement = (Element) objectTypeList.item(i);
                    String objectTypeName = objectTypeElement.getAttribute("name");
                    OCELObjectType objectType = new OCELObjectType(eventLog, objectTypeName);
                    eventLog.objectTypes.put(objectTypeName, objectType);
                    eventLog.getObjectTypes().add(objectTypeName);
                    // You can extend OcelObjectType to store attribute definitions if needed
                }
            }

            // Parse event-types if needed (similar to object-types)

            // Parse objects
            NodeList objectsNodes = root.getElementsByTagName("objects");
            for (int i = 0; i < objectsNodes.getLength(); i++) {
                Element objectsElement = (Element) objectsNodes.item(i);
                NodeList objectList = objectsElement.getElementsByTagName("object");
                for (int j = 0; j < objectList.getLength(); j++) {
                    Element objectElement = (Element) objectList.item(j);
                    String objectId = objectElement.getAttribute("id");
                    String objectTypeName = objectElement.getAttribute("type");

                    OCELObject ocelObject = new OCELObject(eventLog);
                    ocelObject.id = objectId;

                    // Get or create OcelObjectType
                    OCELObjectType objectType = eventLog.objectTypes.get(objectTypeName);
                    if (objectType == null) {
                        objectType = new OCELObjectType(eventLog, objectTypeName);
                        eventLog.objectTypes.put(objectTypeName, objectType);
                        eventLog.getObjectTypes().add(objectTypeName);
                    }
                    ocelObject.objectType = objectType;
                    eventLog.objects.put(objectId, ocelObject);

                    // Parse attributes
                    NodeList attributesList = objectElement.getElementsByTagName("attributes");
                    if (attributesList.getLength() > 0) {
                        Element attributesElement = (Element) attributesList.item(0);
                        NodeList attributeList = attributesElement.getElementsByTagName("attribute");
                        for (int k = 0; k < attributeList.getLength(); k++) {
                            Element attributeElement = (Element) attributeList.item(k);
                            String attributeName = attributeElement.getAttribute("name");
                            String timeStr = attributeElement.getAttribute("time");
                            String valueStr = attributeElement.getTextContent().trim();

                            // Parse the time
                            Date time = parseISODate(timeStr);

                            // Store attribute value and time
                            Map<Date, Object> attrValues = ocelObject.timedAttributes.get(attributeName);
                            if (attrValues == null) {
                                attrValues = new HashMap<Date, Object>();
                                ocelObject.timedAttributes.put(attributeName, attrValues);
                            }
                            attrValues.put(time, valueStr);

                            // Update attributes map with the earliest value
                            if (!ocelObject.attributes.containsKey(attributeName)) {
                                ocelObject.attributes.put(attributeName, valueStr);
                            } else {
                                Date existingTime = getEarliestTime(ocelObject.timedAttributes.get(attributeName));
                                if (time.before(existingTime)) {
                                    ocelObject.attributes.put(attributeName, valueStr);
                                }
                            }
                        }
                    }

                    // Parse related objects
                    NodeList objectsList = objectElement.getElementsByTagName("objects");
                    if (objectsList.getLength() > 0) {
                        for (int idx = 0; idx < objectsList.getLength(); idx++) {
                            Element objectsElement2 = (Element) objectsList.item(idx);
                            NodeList relationshipList = objectsElement2.getElementsByTagName("relationship");
                            for (int k = 0; k < relationshipList.getLength(); k++) {
                                Element relationshipElement = (Element) relationshipList.item(k);
                                String relatedObjectId = relationshipElement.getAttribute("object-id");
                                String qualifier = relationshipElement.getAttribute("qualifier");
                                ocelObject.relatedObjectIdentifiers.put(relatedObjectId, qualifier);
                            }
                        }
                    }
                }
            }

            // Parse events
            NodeList eventsNodes = root.getElementsByTagName("events");
            for (int i = 0; i < eventsNodes.getLength(); i++) {
                Element eventsElement = (Element) eventsNodes.item(i);
                NodeList eventList = eventsElement.getElementsByTagName("event");
                for (int j = 0; j < eventList.getLength(); j++) {
                    Element eventElement = (Element) eventList.item(j);
                    String eventId = eventElement.getAttribute("id");
                    String eventType = eventElement.getAttribute("type");
                    String timeStr = eventElement.getAttribute("time");

                    OCELEvent ocelEvent = new OCELEvent(eventLog);
                    ocelEvent.id = eventId;
                    ocelEvent.activity = eventType;
                    ocelEvent.timestamp = parseISODate(timeStr);
                    eventLog.events.put(eventId, ocelEvent);

                    // Parse attributes
                    NodeList attributesList = eventElement.getElementsByTagName("attributes");
                    if (attributesList.getLength() > 0) {
                        Element attributesElement = (Element) attributesList.item(0);
                        NodeList attributeList = attributesElement.getElementsByTagName("attribute");
                        for (int k = 0; k < attributeList.getLength(); k++) {
                            Element attributeElement = (Element) attributeList.item(k);
                            String attributeName = attributeElement.getAttribute("name");
                            String valueStr = attributeElement.getTextContent().trim();
                            ocelEvent.attributes.put(attributeName, valueStr);
                            eventLog.getAttributeNames().add(attributeName);
                        }
                    }

                    // Parse related objects
                    NodeList objectsList = eventElement.getElementsByTagName("objects");
                    if (objectsList.getLength() > 0) {
                        for (int idx = 0; idx < objectsList.getLength(); idx++) {
                            Element objectsElement2 = (Element) objectsList.item(idx);
                            NodeList relationshipList = objectsElement2.getElementsByTagName("relationship");
                            for (int k = 0; k < relationshipList.getLength(); k++) {
                                Element relationshipElement = (Element) relationshipList.item(k);
                                String relatedObjectId = relationshipElement.getAttribute("object-id");
                                String qualifier = relationshipElement.getAttribute("qualifier");
                                ocelEvent.relatedObjectsIdentifiers.put(relatedObjectId, qualifier);
                            }
                        }
                    }
                }
            }

            // After parsing, we need to register the events and objects
            eventLog.register();

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        return eventLog;
    }

    private Date parseISODate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                return sdf.parse(dateStr);
            } catch (ParseException e2) {
                try {
                    // Remove parentheses and content within, if any
                    int idx = dateStr.indexOf(" (");
                    if (idx != -1) {
                        dateStr = dateStr.substring(0, idx);
                    }
                    // Replace 'GMT' with an empty string to correctly parse the time zone offset
                    if (dateStr.contains("GMT")) {
                        dateStr = dateStr.replace("GMT", "").trim();
                    }
                    // Use the appropriate date pattern
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss Z", Locale.ENGLISH);
                    return sdf.parse(dateStr);
                } catch (ParseException e3) {
                    try {
                        // Additional attempt without modifying the date string
                        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss z", Locale.ENGLISH);
                        return sdf.parse(dateStr);
                    } catch (ParseException e4) {
                        e4.printStackTrace();
                        return null;
                    }
                }
            }
        }
    }

    private Date getEarliestTime(Map<Date, Object> timedValues) {
        Date earliest = null;
        for (Date time : timedValues.keySet()) {
            if (earliest == null || time.before(earliest)) {
                earliest = time;
            }
        }
        return earliest;
    }
}
