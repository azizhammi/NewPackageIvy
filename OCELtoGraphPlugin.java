package org.processmining.newpackageivy.plugins;


import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.processmining.newpackageivy.plugins.OCELdata.*;


@Plugin(
        name = "OCEL 2.0 Graph Visualization And Analysis",
        parameterLabels = { "OCELEventLog" },
        returnLabels = { "Graph Visualization Panel" },
        returnTypes = { JPanel.class },
        userAccessible = true
)
public class OCELtoGraphPlugin {
	

    @UITopiaVariant(affiliation = "RWTH Aachen University", author = "Mohamed Aziz El Hammi", email = "aziz.hammi@rwth-aachen.de")
    @PluginVariant(variantLabel = "Convert OCEL 2.0 Event Logs Into Graph", requiredParameterLabels = {0})
    
    public JPanel convertToGraph(PluginContext context, OCELEventLog eventLog) {


    	Graph ResultingGraph = new SingleGraph("OCELGraph");

    	
        // Converting Event Log To A Graph
    	
    	
        for (OCELEvent event : eventLog.getEvents().values()) {
            String eventID = event.id;
            String eventActivity = event.activity != null ? event.activity : "Unknown Activity";
            String eventTimestamp = event.timestamp != null ? event.timestamp.toString() : "No Timestamp";

            // Add event node to Graph
            
            ResultingGraph.addNode(eventID);
            ResultingGraph.getNode(eventID).setAttribute("ui.label", String.format(eventID));
            
            // Class Nodes As Event Nodes
            ResultingGraph.getNode(eventID).setAttribute("ui.class", "event");

            
            // Add Event Activity and Timestamp as node attributes
            ResultingGraph.getNode(eventID).setAttribute("Activity", eventActivity);
            ResultingGraph.getNode(eventID).setAttribute("Timestamp", eventTimestamp);

            // Add Other Event Attributes
            if (event.attributes != null && !event.attributes.isEmpty()) {
                for (Map.Entry<String, Object> EventAttribute : event.attributes.entrySet()) {
                	ResultingGraph.getNode(eventID).setAttribute(EventAttribute.getKey(), EventAttribute.getValue().toString());
                }
            }

            // Add Object Nodes To Graph
            for (Map.Entry<OCELObject, String> RelatedObject : event.relatedObjects.entrySet()) {
                OCELObject object = RelatedObject.getKey();
                String objectID = object.id;
                String Type = object.objectType != null ? object.objectType.name : "Unknown Type";

                if (ResultingGraph.getNode(objectID) == null) {
                	ResultingGraph.addNode(objectID);
                	ResultingGraph.getNode(objectID).setAttribute("ui.label", String.format(objectID));
                	ResultingGraph.getNode(objectID).setAttribute("Type", Type);
                	
                    // Class Nodes As Object Nodes
                	ResultingGraph.getNode(objectID).setAttribute("ui.class", "object");

                    // Add Object Static Attributes
                     Map<String, Object> StaticAttributes = object.attributes;
                	 for (Map.Entry<String, Object> StaticAttribute : StaticAttributes.entrySet()) {
                         ResultingGraph.getNode(object.id).setAttribute(StaticAttribute.getKey(), StaticAttribute.getValue().toString());
                     }
                    
                    // Add Object Dynamic Attributes
                    Map<String, Map<Date, Object>> DynamicAttributes = object.timedAttributes;
                    if (DynamicAttributes != null) {
                        for (Map.Entry<String, Map<Date, Object>> DynamicAttribute : DynamicAttributes.entrySet()) {
                            StringBuilder DynamicAttributeValue = new StringBuilder();
                            
                            //Value And Respective Time Of Dynamic Attribute
                            for (Map.Entry<Date, Object> timeAndValue : DynamicAttribute.getValue().entrySet()) {
                            	DynamicAttributeValue.append(timeAndValue.getKey().toString())
                                              .append(": ")
                                              .append(timeAndValue.getValue().toString())
                                              .append("; ");
                            }
                            ResultingGraph.getNode(objectID).setAttribute(DynamicAttribute.getKey(), DynamicAttributeValue.toString());
                        }
                    }
                }

                // Add E2O Relationships As Edge
                String edgeID = eventID + "-" + objectID;
                // Allowing Directed Edges By Adding "true"
                ResultingGraph.addEdge(edgeID, eventID, objectID, true);

                ResultingGraph.getEdge(edgeID).setAttribute("ui.label", RelatedObject.getValue());
            }
        
        }
        
        //Add O2O Relationships
        for (OCELObject object : eventLog.getObjects().values()) {
        	
            // 
            for (Map.Entry<String, String> RelatedObject : object.relatedObjectIdentifiers.entrySet()) {
                String relatedObjectID = RelatedObject.getKey();
                String qualifier = RelatedObject.getValue();  
                
                if (ResultingGraph.getNode(object.id) != null && ResultingGraph.getNode(relatedObjectID) != null) {
                	
                    // Add O2O Relationships As Edge
                    String o2oEdgeId = object.id + "-" + relatedObjectID;

                    if (ResultingGraph.getEdge(o2oEdgeId) == null) {
                        ResultingGraph.addEdge(o2oEdgeId, object.id, relatedObjectID, true);
                        ResultingGraph.getEdge(o2oEdgeId).setAttribute("ui.label", qualifier);
                        ResultingGraph.getEdge(o2oEdgeId).setAttribute("ui.class", "O2O");
                    }
                }
            }
        }

        //Use Customized Layout
        CustomLayout Layout = new CustomLayout(
                ResultingGraph,
                new ArrayList<>(eventLog.getEvents().values()),
                new ArrayList<>(eventLog.getObjects().values())
        );
        Layout.applyLayout(); 
        
   
        //Style Graph 
        ResultingGraph.setAttribute("ui.stylesheet",
        	    "graph { padding: 20px; }" +
        	    
        	    "node.event { shape: rounded-box; size: 100px, 100px; text-size: 12px; text-alignment: center; " +
        	    "text-color: white; text-padding: 8px; fill-color: #007bff; stroke-mode: plain; stroke-color: #0056b3; }" +
        	    
        	    "node.object { shape: circle; size: 100px; text-size: 14px; text-alignment: center; " +
        	    "text-color: white; text-padding: 6px; fill-color: #222; stroke-mode: plain; stroke-color: #444; }" +
        	    
        	    "edge { text-size: 10px; text-alignment: center; fill-color: #aaa; size: 1px; arrow-size: 5px; }" +
        	    
        	    "edge.O2O { fill-color: #d9534f; size: 2px; arrow-size: 7px; stroke-mode: plain; stroke-color: #b52b27; }"
        	);

        // Visualization Panel
        VisualizationPanel Visualizationpanel = new VisualizationPanel(ResultingGraph, eventLog);
        
        //Pop-up Frame
        JFrame frame = new JFrame("OCEL 2.0 Graph Visualization And Analysis");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1000, 800));

        // Add the panel to the frame
        frame.add(Visualizationpanel);

        //Show Frame
        frame.pack();
        frame.setVisible(true);

        return Visualizationpanel;
    }
}
