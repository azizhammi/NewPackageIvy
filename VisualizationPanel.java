package org.processmining.newpackageivy.plugins;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.camera.Camera;
import org.processmining.newpackageivy.plugins.OCELdata.*;


import javax.swing.*;
import java.awt.*;
import java.io.File;


public class VisualizationPanel extends JPanel {
	
    private static final long serialVersionUID = 1L;

    private final Graph graph;
    private final OCELEventLog eventLog;
    private final Camera camera;
    
    private JLabel Zoom;
    private TimeSlider timeSlider; 
    
    
    private ResetGraph graphResetted;


    public VisualizationPanel(Graph graph, OCELEventLog eventLog) {
    	
        this.graph = graph;
        this.eventLog = eventLog;

        // Interface Layout
        this.setLayout(new BorderLayout());
        
        this.graphResetted = new ResetGraph(graph, this);


        // Limit Graph
        LimitGraph.limitGraph(graph);
        
        //Save Limited Graph
        this.graphResetted.SaveLimitedGraph(graph);

        // Display Graph
        SwingViewer Viewer = new SwingViewer(graph, SwingViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        
        // Allow Customized Layout
        View View = Viewer.addDefaultView(false);

        //Create Wrapper
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setFocusable(false); 
        wrapper.setRequestFocusEnabled(false);
        wrapper.add((Component) View, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);
        

        this.camera = View.getCamera();

        Zoom();
        UIControls();
        
        timeSlider = new TimeSlider(graph, eventLog.getEvents(), eventLog.getObjects());
        this.add(timeSlider, BorderLayout.NORTH);
        
    }

    //Zoom Feature
    private void Zoom() {
    	
        addMouseWheelListener(e -> {
            double ZoomPower = 1.1;
            double NewZoom = e.getWheelRotation() < 0 ? camera.getViewPercent() / ZoomPower : camera.getViewPercent() * ZoomPower;
            camera.setViewPercent(Math.max(0.01, Math.min(10, NewZoom)));
            
            UpdateZoom();
            refreshGraphView();
        });
    }

    private void UpdateZoom() {
        Zoom.setText("Zoom: " + Math.round(camera.getViewPercent() * 100) + "%");
    }
    
    private void refreshGraphView() {
        camera.setViewCenter(camera.getViewCenter().x, camera.getViewCenter().y, 0);
    }
    
    
    //UI Components
    private void UIControls() {
    	
    	JPanel ControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        Zoom = new JLabel("Zoom: 100%");
        ControlPanel.add(Zoom, BorderLayout.NORTH);

        //Bottom Panel For all Buttons
        JPanel BottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 
        ControlPanel.add(BottomPanel, BorderLayout.SOUTH);
       
        this.add(ControlPanel, BorderLayout.SOUTH);
        
        //Reset Button
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> graphResetted.Reset());  // Use the resetGraph method from GraphResetter
        ControlPanel.add(resetButton, BorderLayout.WEST);

        //Enter Node ID Button
        JButton NodeInfoButton = new JButton("Enter Node ID");
        NodeInfoButton.addActionListener(e -> NodeInfomation());
        ControlPanel.add(NodeInfoButton);

        //Filter Button
        JButton FilterButton = new JButton("Filter");
        FilterButton.addActionListener(e -> Filter());
        ControlPanel.add(FilterButton);

        //Export Button
        JButton exportButton = new JButton("Export");
        exportButton.addActionListener(e -> Export());
        ControlPanel.add(exportButton);
 
    }

    //Node Information
    private void NodeInfomation() {
    	
        String nodeID = JOptionPane.showInputDialog(this, "Enter Node ID:", "Node Information", JOptionPane.PLAIN_MESSAGE);

        
        if (nodeID != null && graph.getNode(nodeID) != null) {
        	
            Node node = graph.getNode(nodeID);
            StringBuilder attributes = new StringBuilder();

            attributes.append("Node ID: ").append(node.getId()).append("\n");

            // Show Node Attributes
            node.attributeKeys().forEach(key -> {
            	//Remove useless Attributes Used For Implementation
                if (!key.equals("ui.label") && !key.equals("xyz") && !key.equals("ui.class")) { 
                    attributes.append(key).append(": ").append(node.getAttribute(key)).append("\n");
                }
            });

            // Show Activity And Timestamp
            if (eventLog != null) {
                eventLog.getEvents().forEach((key, event) -> {

                    if (event.id.equals(nodeID)) { 
                        // Show Activity And Timestamp of the event
                        event.attributes.forEach((Key, Value) -> {
                        	if (Key.equals("Activity") || Key.equals("Timestamp"))
                            attributes.append(Key).append(": ").append(Value).append("\n");
                        });
                    }
                });
            }

            // Pop-up With Result
            JOptionPane.showMessageDialog(this, attributes.toString(), "Node Details", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // Pop-up With Error 
            JOptionPane.showMessageDialog(this, "Node ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private void Filter() {
        Filter Filter = new Filter((Frame) SwingUtilities.getWindowAncestor(this), graph, this);
        Filter.setVisible(true);
    } 

    
    private void Export() {
    	 JFileChooser FileChooser = new JFileChooser();
    	    FileChooser.setDialogTitle("Save Graph File");

    	    // Set default file name and ensure it's in JSON format
    	    FileChooser.setSelectedFile(new File("ResultingGraph.json"));

    	    int userSelection = FileChooser.showSaveDialog(this);
    	    if (userSelection == JFileChooser.APPROVE_OPTION) {
    	        String filePath = FileChooser.getSelectedFile().getAbsolutePath();

    	        //File Extension
    	        if (!filePath.endsWith(".json")) {
    	            filePath = filePath + ".json";
    	        }

    	        try {
    	        	//Converting Graph To JSON
    	            Export exporter = new Export(graph);
    	            exporter.JSONExport(filePath);
    	            
    	            JOptionPane.showMessageDialog(this, "Export successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
    	        } catch (Exception ex) {
    	            JOptionPane.showMessageDialog(this, "Error exporting graph: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    	        }
    	    }
    	    
    }

}
