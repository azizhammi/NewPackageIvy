package org.processmining.newpackageivy.plugins;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.processmining.newpackageivy.plugins.OCELdata.*;

import java.util.List;

public class CustomLayout {
	
    private final Graph graph;
    private final List<OCELEvent> events;
    private final List<OCELObject> objects;

    public CustomLayout(Graph graph, List<OCELEvent> events, List<OCELObject> objects) {
        this.graph = graph;
        this.events = events;
        this.objects = objects;
    }

    public void applyLayout() {
        
        double topY = 50;     
        double bottomY = -50; 
        double startX = -500; 
        
        //Spacing Between Nodes
        double WidthMax = 2000;
        int NumberOfNodes = Math.max(events.size(), objects.size());
        double Spacing = Math.max(30, WidthMax / Math.max(1, NumberOfNodes));

        
        //Objects On Top Half
        for (int i = 0; i < objects.size(); i++) {
            OCELObject object = objects.get(i);
            Node node = graph.getNode(object.id);
            
            if (node != null) {
            	//Spacing With Logarithm
                double x = startX + Math.log(1 + i) * Spacing;
                node.setAttribute("xyz", x, topY, 0);
            }
        }

        //Sort events Chronologically
        events.sort((e1, e2) -> e1.timestamp.compareTo(e2.timestamp));
        
        
        //Event On Bottom Half
        for (int i = 0; i < events.size(); i++) {
            OCELEvent event = events.get(i);
            Node node = graph.getNode(event.id);
            
            if (node != null) {
            	//Spacing With Logarithm
                double x = startX + Math.log(1 + i) * Spacing; 
                node.setAttribute("xyz", x, bottomY, 0);
            }
        }
    }
}
