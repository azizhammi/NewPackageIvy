package org.processmining.newpackageivy.plugins;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.*;
import java.util.stream.Collectors;

public class LimitGraph {

    private static final int MaxNodes = 50;

    public static void limitGraph(Graph graph) {
     
    	//Get Total Number Of Nodes
        List<Node> AllNodes = new ArrayList<>();
        graph.nodes().forEach(AllNodes::add);

        if (AllNodes.size() > MaxNodes) {

            Random random = new Random();
            
            //To Have A Fair Choice
            Collections.shuffle(AllNodes, random);  

            Set<Node> NodesSelected = new HashSet<>();
            Queue<Node> Queue = new LinkedList<>();

            
            // Get One Node To Start With
            for (Node node : AllNodes) {
            	
            	//Get A Node With Neighbors
                if (node.getDegree() > 0) { 
                    NodesSelected.add(node);
                    Queue.add(node);
                    break;
                }
            }

            // Continue selecting connected nodes until we reach the limit
            while (!Queue.isEmpty() && NodesSelected.size() < MaxNodes) {
            	
                Node current = Queue.poll();

                List<Edge> edges = current.edges().collect(Collectors.toList());

                for (Edge edge : edges) {
                    Node NeighborNode = edge.getOpposite(current);

                    if (!NodesSelected.contains(NeighborNode)) {
                    	NodesSelected.add(NeighborNode);
                        Queue.add(NeighborNode);
                    }

                    //Respect The Limit Of Number Of Nodes
                    if (NodesSelected.size() >= MaxNodes) {
                        break;
                    }
                }
            }

            //Nodes To Be Removed
            List<Node> nodesToRemove = new ArrayList<>();
            for (Node node : AllNodes) {
                if (!NodesSelected.contains(node)) {
                    nodesToRemove.add(node);
                }
            }

            //Remove Nodes
            for (Node node : nodesToRemove) {
                graph.removeNode(node.getId());
            }

            //Edges To Be Removed
            List<Edge> edgesToRemove = graph.edges()
                    .filter(edge -> !NodesSelected.contains(edge.getSourceNode()) || !NodesSelected.contains(edge.getTargetNode()))
                    .collect(Collectors.toList());

            //Remove Edges
            for (Edge edge : edgesToRemove) {
                graph.removeEdge(edge.getId());
            }

        } 
    }
}
