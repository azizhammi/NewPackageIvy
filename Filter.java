package org.processmining.newpackageivy.plugins;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Filter extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JPanel FilterPanel; 
    private final JButton AddButton; 
    private final JButton ApplyButton; 
    private final JButton CancelButton; 
    private final List<JTextField> AttributeField;
    private final List<JTextField> ValueField; 
    private final List<JComboBox<String>> OperatorField; 
    
    private final Graph graph; 
    private final JPanel visualizationPanel; 

    public Filter(Frame frame, Graph graph, JPanel visualizationPanel) {
    	
        super(frame, "Filter", true);
        
        this.graph = graph;
        this.visualizationPanel = visualizationPanel;
        
        //Filter Panel
        FilterPanel = new JPanel();
        FilterPanel.setLayout(new GridLayout(0, 3, 5, 5)); 
        AttributeField = new ArrayList<>();
        ValueField = new ArrayList<>();
        OperatorField = new ArrayList<>();

        // Add initial attribute-value pair fields
        AddFilter();

        AddButton = new JButton("Add Attribute");
        ApplyButton = new JButton("Apply Filter");
        CancelButton = new JButton("Cancel");

        setLayout(new BorderLayout());
        
        //Scroll Feature For Navigation In Filter Panel
        JScrollPane scrollPane = new JScrollPane(FilterPanel);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(AddButton);
        buttonPanel.add(ApplyButton);
        buttonPanel.add(CancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        AddButton.addActionListener(e -> AddFilter());

        ApplyButton.addActionListener(e -> ApplyFilter());

        CancelButton.addActionListener(e -> dispose());

        setSize(600, 450); 
        
        setLocationRelativeTo(frame); 
    }

    //Add New Filter
    private void AddFilter() {
    	
        JTextField attributeField = new JTextField();
        JTextField valueField = new JTextField();
        JComboBox<String> operatorField = new JComboBox<>(new String[]{"=", "contains"});

        AttributeField.add(attributeField);
        ValueField.add(valueField);
        OperatorField.add(operatorField);

        //Add The Components
        FilterPanel.add(new JLabel("Attribute:"));
        FilterPanel.add(attributeField);
        FilterPanel.add(operatorField);
        FilterPanel.add(new JLabel("Value:"));
        FilterPanel.add(valueField);

        //Go Back In Line
        FilterPanel.add(Box.createVerticalStrut(10));  

        FilterPanel.revalidate(); 
        FilterPanel.repaint();
    }

    
    // Applies the filter based on the user inputs
    private void ApplyFilter() {
    	
        List<String> Attributes = new ArrayList<>();
        List<String> Values = new ArrayList<>();
        List<String> Operators = new ArrayList<>();

        //Check Every Criteria
        for (int i = 0; i < AttributeField.size(); i++) {
        	
            String attribute = AttributeField.get(i).getText().trim();
            String value = ValueField.get(i).getText().trim();
            String operator = (String) OperatorField.get(i).getSelectedItem();


            if (!attribute.isEmpty() && !value.isEmpty() && operator != null) {
                Attributes.add(attribute);
                Values.add(value);
                Operators.add(operator);
            }
        }

        //At least One Criteria
        if (Attributes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter at least one attribute-value pair.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        
        List<String> nodesToRemove = new ArrayList<>();
        Set<String> connectedNodes = new HashSet<>();

        //Filter Edges
        graph.edges().forEach(edge -> {
            boolean match = true;

            for (int i = 0; i < Attributes.size(); i++) {
                String attribute = Attributes.get(i);
                String value = Values.get(i);
                String operator = Operators.get(i);

                String EdgeAttributeValue = getEdgeAttributeValue(edge, attribute);

                if (EdgeAttributeValue == null || !applyOperator(EdgeAttributeValue, value, operator)) {
                    match = false;
                    break;
                }
            }

            //Keep Edge and Its Nodes (When match)
            if (match) {
                Node Node1 = edge.getNode0();
                Node Node2 = edge.getNode1();
                connectedNodes.add(Node1.getId());
                connectedNodes.add(Node2.getId());
            }
        });

        
        //Filter Nodes
        graph.nodes().forEach(node -> {
            boolean match = true;

            for (int i = 0; i < Attributes.size(); i++) {
                String attribute = Attributes.get(i);
                String value = Values.get(i);
                String operator = Operators.get(i);
                
                String NodeAttributeValue = getNodeAttributeValue(node.getId(), attribute);

                if (NodeAttributeValue == null || !applyOperator(NodeAttributeValue, value, operator)) {
                    match = false;
                    break;
                }
            }

            if (match) {
                node.edges().forEach(edge -> {
                    String RelatedNodeID = edge.getOpposite(node).getId();
                    connectedNodes.add(RelatedNodeID);
                });
            } else {
                nodesToRemove.add(node.getId());
            }
        });

        //Keep Connected Nodes 
        nodesToRemove.removeAll(connectedNodes);

        //Remove Unmatched Nodes 
        for (String nodeId : nodesToRemove) {
            graph.removeNode(nodeId);
        }

        visualizationPanel.repaint();
        dispose();
    }


    private String getNodeAttributeValue(String nodeId, String attribute) {
        Object NodeAttributeValue = graph.getNode(nodeId).getAttribute(attribute);
        if (NodeAttributeValue != null) {
            return NodeAttributeValue.toString();
        }
        return null;
    }
    
    
    
    private String getEdgeAttributeValue(Edge edge, String attribute) {
        Object EdgeAttributeValue = edge.getAttribute(attribute);
        if (EdgeAttributeValue != null) {
            return EdgeAttributeValue.toString();
        }
        return null;
    }

    
    private boolean applyOperator(String nodeValue, String value, String operator) {
        switch (operator) {
            case "=":
                return nodeValue.equals(value);
           
            case "contains":
                return nodeValue.contains(value);
            
            default:
                return false;
        }
    }

}