package org.processmining.newpackageivy.plugins;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;
import org.graphstream.graph.implementations.MultiGraph;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;

public class ResetGraph {

    private Graph graph;
    private Graph LimitedGraph;
    private JPanel VisualizationPanel;

    public ResetGraph(Graph graph, JPanel visualizationPanel) {
        this.graph = graph;
        this.VisualizationPanel = visualizationPanel;
        this.LimitedGraph = CopyGraph(graph); 
    }

    
    //Save Initial Graph
    public void SaveLimitedGraph(Graph limitedGraph) {
        this.LimitedGraph = CopyGraph(limitedGraph);
    }

    
    //Reset To Initial Graph
    public void Reset() {
    	
        //Clear Present Graph
        graph.clear();

        // Restore Initial Graph
        Restore(LimitedGraph);

        
        VisualizationPanel.repaint();
    }

    
    //Copy Initial Graph
    private Graph CopyGraph(Graph graph) {
    	
        Graph Copy = new MultiGraph(graph.getId());

        //Copy Nodes
        List<Node> nodes = graph.nodes().collect(Collectors.toList());
        for (Node node : nodes) {
            Copy.addNode(node.getId());
            node.attributeKeys().forEach(key -> {
                Copy.getNode(node.getId()).setAttribute(key, node.getAttribute(key));
            });
        }

        //Copy Edges
        List<Edge> edges = graph.edges().collect(Collectors.toList());
        for (Edge edge : edges) {
            Copy.addEdge(edge.getId(), edge.getSourceNode().getId(), edge.getTargetNode().getId(), true);
            edge.attributeKeys().forEach(key -> {
                Copy.getEdge(edge.getId()).setAttribute(key, edge.getAttribute(key));
            });
        }

        return Copy;
    }

    // Restores a given graph state
    private void Restore(Graph SavedGraph) {
        List<Node> SavedNodes = SavedGraph.nodes().collect(Collectors.toList());
        List<Edge> SavedEdges = SavedGraph.edges().collect(Collectors.toList());

        //Add Initial Nodes
        for (Node node : SavedNodes) {
            graph.addNode(node.getId());
            node.attributeKeys().forEach(key -> {
                graph.getNode(node.getId()).setAttribute(key, node.getAttribute(key));
            });
        }

        
        //Add Initial Edges
        for (Edge edge : SavedEdges) {
            graph.addEdge(edge.getId(), edge.getSourceNode().getId(), edge.getTargetNode().getId(), true);
            edge.attributeKeys().forEach(key -> {
                graph.getEdge(edge.getId()).setAttribute(key, edge.getAttribute(key));
            });
        }

        //Same Styling As The Beginning
        graph.setAttribute("ui.stylesheet",
        	    "graph { padding: 20px; }" +
        	    
        	    "node.event { shape: rounded-box; size: 100px, 100px; text-size: 12px; text-alignment: center; " +
        	    "text-color: white; text-padding: 8px; fill-color: #007bff; stroke-mode: plain; stroke-color: #0056b3; }" +
        	    
        	    "node.object { shape: circle; size: 100px; text-size: 14px; text-alignment: center; " +
        	    "text-color: white; text-padding: 6px; fill-color: #222; stroke-mode: plain; stroke-color: #444; }" +
        	    
        	    
        	    "edge { text-size: 10px; text-alignment: center; fill-color: #aaa; size: 1px; arrow-size: 5px; }" +
        	    
        	    "edge.O2O { fill-color: #d9534f; size: 2px; arrow-size: 7px; stroke-mode: plain; stroke-color: #b52b27; }"
        	);
    }
}
