package org.processmining.newpackageivy.plugins;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.processmining.newpackageivy.plugins.OCELdata.*;

public class TimeSlider extends JPanel {
	
    private static final long serialVersionUID = 1L;

    private final JSlider StartSlider;
    private final JSlider EndSlider;
    private final JLabel TimeRange;
    private final TreeMap<Long, Date> SortedTimestamps;
    private final Graph graph;
    private JPanel visualizationPanel;

    public TimeSlider(Graph graph, Map<String, OCELEvent> events, Map<String, OCELObject> objects) {
    	
        this.graph = graph;
        
        this.setLayout(new BorderLayout());

        // Extract Timestamps Of All Events
        SortedTimestamps = ExtractSortedTimestamps(events);

        // Set Times in Timeslider 
        int min = 0;
        int max = SortedTimestamps.size() - 1;

        StartSlider = new JSlider(min, max, min);
        EndSlider = new JSlider(min, max, max);

        //Use Of TimeSlider
        StartSlider.setMajorTickSpacing(1);
        EndSlider.setMajorTickSpacing(1);
        StartSlider.setPaintTicks(true);
        EndSlider.setPaintTicks(true);
        StartSlider.setSnapToTicks(true);
        EndSlider.setSnapToTicks(true);

        //Size Of TimeSlider
        StartSlider.setPreferredSize(new Dimension(1000, 40));  
        EndSlider.setPreferredSize(new Dimension(1000, 40));
        
        TimeRange = new JLabel();
        UpdateRange(StartSlider.getValue(), EndSlider.getValue());

        ChangeListener sliderListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (StartSlider.getValue() > EndSlider.getValue()) {
                    if (e.getSource() == StartSlider) {
                        EndSlider.setValue(StartSlider.getValue());
                    } else {
                        StartSlider.setValue(EndSlider.getValue());
                    }
                }
                UpdateRange(StartSlider.getValue(), EndSlider.getValue());
            }
        };

        StartSlider.addChangeListener(sliderListener);
        EndSlider.addChangeListener(sliderListener);

        JButton ConfirmButton = new JButton("Confirm");
        ConfirmButton.addActionListener(e -> filterGraphByTimeframe(StartSlider.getValue(), EndSlider.getValue()));

   
        JPanel ControlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        //Style "StartSlider"
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        ControlPanel.add(new JLabel("Start Time"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        ControlPanel.add(StartSlider, gbc);

        //Style "EndSlider"
        gbc.gridx = 0;
        gbc.gridy = 1; 
        gbc.anchor = GridBagConstraints.WEST; 
        ControlPanel.add(new JLabel("End Time"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1; 
        gbc.gridwidth = 2; 
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        ControlPanel.add(EndSlider, gbc);

        //Style "Confirm" Button
        gbc.gridx = 0; 
        gbc.gridy = 2; 
        gbc.gridwidth = 3; 
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        ControlPanel.add(ConfirmButton, gbc);
        
        this.add(TimeRange, BorderLayout.NORTH);
        this.add(ControlPanel, BorderLayout.CENTER);
    }

    //Extract TimeStamps Of Events
    private TreeMap<Long, Date> ExtractSortedTimestamps(Map<String, OCELEvent> events) {
    	
        TreeMap<Long, Date> Timestamps = new TreeMap<>();

        events.forEach((eventId, event) -> {
            if (event.timestamp != null) {
                Timestamps.put(event.timestamp.getTime(), event.timestamp);
            }
        });

        return Timestamps;
    }

    private void UpdateRange(int startValue, int endValue) {
        Date startTime = (Date) SortedTimestamps.values().toArray()[startValue];
        Date endTime = (Date) SortedTimestamps.values().toArray()[endValue];
        TimeRange.setText("Selected Timeframe: " + startTime + " - " + endTime);
    }

    private void filterGraphByTimeframe(int startValue, int endValue) {
        Date StartTime = (Date) SortedTimestamps.values().toArray()[startValue];
        Date EndTime = (Date) SortedTimestamps.values().toArray()[endValue];
        
        SimpleDateFormat DateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        
        Set<String> nodesToRemove = new HashSet<>();
        Set<String> nodesToKeep = new HashSet<>();

        graph.nodes().forEach(node -> {
            boolean isValidNode = false;

            // Check for Timestamp Of Event Node
            if (node.hasAttribute("Timestamp")) {
                Object EventTimestamp = node.getAttribute("Timestamp");
                
                if (EventTimestamp instanceof String) {
                    try {
                        Date NodeTimestamp = DateFormat.parse((String) EventTimestamp);
                        
                        //Check If Timestamp in Selected Timeframe
                        if (!NodeTimestamp.before(StartTime) && !NodeTimestamp.after(EndTime)) {
                            isValidNode = true;
                            nodesToKeep.add(node.getId());
                            
                            //Keep connected nodes
                            node.edges().forEach(edge -> {
                                Node connectedNode = edge.getOpposite(node);
                                nodesToKeep.add(connectedNode.getId());
                            });
                        }
                   
                    } catch (ParseException e) {
                        System.out.println("Skipping invalid timestamp: " + EventTimestamp);
                    }
                }
            }

            if (isValidNode) {
                nodesToKeep.add(node.getId());
            }
        });

        //Get Nodes To Remove
        graph.nodes().forEach(node -> {
            if (!nodesToKeep.contains(node.getId())) {
                nodesToRemove.add(node.getId());
            }
        });

        //Remove Them
        nodesToRemove.forEach(graph::removeNode);

        if (visualizationPanel != null) {
            visualizationPanel.repaint();
        }

    }


    public void setVisualizationPanel(JPanel panel) {
        this.visualizationPanel = panel;
    }
}
