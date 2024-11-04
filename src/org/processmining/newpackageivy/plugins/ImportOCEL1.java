package org.processmining.newpackageivy.plugins;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.json.JSONObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.newpackageivy.plugins.OCELdata.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

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

@Plugin(name = "Import OCEL from JSON", parameterLabels = { "Filename" }, returnLabels = {
"Object-Centric Event Log" }, returnTypes = { OCELEventLog.class })
@UIImportPlugin(description = "Import OCEL from JSON", extensions = { "jsonocel", "gz" })
public class ImportOCEL1 extends AbstractImportPlugin {
	String logPath;
	
	public ImportOCEL1() {
		
	}
	
	public ImportOCEL1(String logPath) {
		this.logPath = logPath;
	}
	
	protected OCELEventLog importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		this.logPath = filename;
		return doImportFromStream(input);
	}
	
	public OCELEventLog doImport() throws JSONException {
		File file = new File(this.logPath);
		InputStream is0 = null;
		try {
			is0 = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return doImportFromStream(is0);
	}
	
	public OCELEventLog doImportFromStream(InputStream is0) throws JSONException {
		InputStream is = null;
		if (this.logPath.endsWith(".gz")) {
			try {
				is = new GZIPInputStream(is0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			is = is0;
		}
		OCELEventLog eventLog = new OCELEventLog();
		JSONTokener tokener = new JSONTokener(is);
		JSONObject object = new JSONObject(tokener);
		importGlobalEvent(eventLog, object.getJSONObject("ocel:global-event"));
		importGlobalObject(eventLog, object.getJSONObject("ocel:global-object"));
		importGlobalLog(eventLog, object.getJSONObject("ocel:global-log"));
		importEvents(eventLog, object.getJSONObject("ocel:events"));
		importObjects(eventLog, object.getJSONObject("ocel:objects"));
		eventLog.register();
		return eventLog;
	}
	
	public void importGlobalEvent(OCELEventLog eventLog, JSONObject globalEvent) {
		for (String key : globalEvent.keySet()) {
			eventLog.globalEvent.put(key, (Object)globalEvent.getString(key));
		}
	}
	
	public void importGlobalObject(OCELEventLog eventLog, JSONObject globalObject) {
		for (String key : globalObject.keySet()) {
			eventLog.globalObject.put(key, (Object)globalObject.getString(key));
		}
	}
	
	public void importGlobalLog(OCELEventLog eventLog, JSONObject globalLog) throws JSONException {
		eventLog.globalLog.put("ocel:version", globalLog.get("ocel:version"));
		eventLog.globalLog.put("ocel:ordering", globalLog.get("ocel:ordering"));
		JSONArray attributeNames = (JSONArray)globalLog.get("ocel:attribute-names");
		Integer i = 0;
		while (i < attributeNames.length()) {
			((Set<String>)eventLog.globalLog.get("ocel:attribute-names")).add((String)attributeNames.get(i));
			i = i + 1;
		}
		JSONArray objectTypes = (JSONArray)globalLog.get("ocel:object-types");
		i = 0;
		while (i < objectTypes.length()) {
			((Set<String>)eventLog.globalLog.get("ocel:object-types")).add((String)objectTypes.get(i));
			i = i + 1;
		}
	}
	
	public void importEvents(OCELEventLog eventLog, JSONObject events) {
		for (String eventId : events.keySet()) {
			JSONObject jsonEvent = events.getJSONObject(eventId);
			OCELEvent event = new OCELEvent(eventLog);
			event.id = eventId;
			event.activity = jsonEvent.getString("ocel:activity");
			String timestampStr = jsonEvent.getString("ocel:timestamp");
			timestampStr = timestampStr.replaceAll(" ", "T");

			DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

			TemporalAccessor temporalAccessor = formatter.parseBest(
			    timestampStr, 
			    OffsetDateTime::from, 
			    LocalDateTime::from
			);

			Instant instant;
			if (temporalAccessor instanceof OffsetDateTime) {
			    instant = ((OffsetDateTime) temporalAccessor).toInstant();
			} else {
			    instant = ((LocalDateTime) temporalAccessor).atZone(ZoneOffset.UTC).toInstant();
			}

			event.timestamp = Date.from(instant);
			
			JSONArray jsonRelatedObjects = jsonEvent.getJSONArray("ocel:omap");
			int i = 0;
			while (i < jsonRelatedObjects.length()) {
				event.relatedObjectsIdentifiers.put(jsonRelatedObjects.getString(i), "");
				i++;
			}
			JSONObject jsonVmap = jsonEvent.getJSONObject("ocel:vmap");
			for (String att_key : jsonVmap.keySet()) {
				event.attributes.put(att_key, jsonVmap.get(att_key));
			}
			eventLog.events.put(eventId, event);
		}
	}
	
	public void importObjects(OCELEventLog eventLog, JSONObject objects) {
		for (String objectId : objects.keySet()) {
			JSONObject jsonObject = objects.getJSONObject(objectId);
			OCELObject object = new OCELObject(eventLog);
			object.id = objectId;
			String objectTypeName = jsonObject.getString("ocel:type");
			if (!(eventLog.objectTypes.containsKey(objectTypeName))) {
				OCELObjectType objectType = new OCELObjectType(eventLog, objectTypeName);
				eventLog.objectTypes.put(objectTypeName, objectType);
			}
			OCELObjectType objectType = eventLog.objectTypes.get(objectTypeName);
			object.objectType = objectType;
			JSONObject jsonVmap = jsonObject.getJSONObject("ocel:ovmap");
			for (String att_key : jsonVmap.keySet()) {
				object.attributes.put(att_key, jsonVmap.get(att_key));
			}
			eventLog.objects.put(objectId, object);
		}
	}
}