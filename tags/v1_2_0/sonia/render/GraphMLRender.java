package sonia.render;

import sonia.ArcAttribute;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.w3c.dom.*;
import sonia.NodeAttribute;

/**
 * <p>Title:SoNIA (Social Network Image Animator) </p>
 * <p>Description:Animates layouts of time-based networks
 * <p>Copyright: CopyLeft  2004: GNU GPL</p>
 * <p>Company: none</p>
 * @author Skye Bender-deMoll unascribed
 * @version 1.1
 */



/**
 * Exports momentary (single slice) view of the network in GraphML XML format.  
 * http://graphml.graphdrawing.org/primer/graphml-primer.html
* 
 * @author skyebend
 *
 */
public class GraphMLRender implements Render {
	
	public static final String MAIN = "graphml";
	public static final String KEY = "key";
	public static final String GRAPH = "graph";
	public static final String NODE = "node";
	public static final String EDGE = "edge";
	public static final String SOR = "source";
	public static final String TAR = "target";
	public static final String LAB = "label";
	public static final String WEIGHT = "weight";
	public static final String COLOR = "rgbcolor";
	public static final String SIZE = "size";
	public static final String LABEL = "label";
	
	
	private Hashtable<NodeAttribute, Point2D>nodes = new Hashtable();
	private HashSet<ArcAttribute>arcs = new HashSet();

	
	

	/**
	 * ignored
	 */
	public void paintArc(ArcAttribute arc, float widthFactor, double fromX,
			double fromY, double toX, double toY, boolean flash,
			boolean arrows, boolean labels) {
		arcs.add(arc);

	}

	public void paintNode(NodeAttribute node, double xCoord, double yCoord,
			double scaleFact) {
		nodes.put(node, new Point2D.Double(xCoord, yCoord));

	}

	/**
	 * ignored
	 */
	public void paintNodeLabels(NodeAttribute node, double xCoord,
			double yCoord, double scaleFact, boolean showLabels, boolean showId) {
	

	}

	public void paintStats(String stats) {
		// TODO Auto-generated method stub

	}

	public void setDrawingTarget(Object drawTarget) {
		// TODO Auto-generated method stub

	}

	/**
	 * ignored
	 */
	public void setTransparency(float trans) {
		

	}
	
	public void createXML(String filename, int width, int height, 
			int mouseRange, String caption) throws ParserConfigurationException, TransformerException{
	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        DOMImplementation di = db.getDOMImplementation();

	        Document doc = di.createDocument(null, "graphml", null);
	        Element root = doc.getDocumentElement();
//	      This element will change in each line
	    	Element graphInfo = null;
	        
	    	graphInfo = doc.createElement(MAIN);
	    	 root.appendChild(graphInfo);
	    	//set up a key for attaching size info
	    	Element sizedfn = doc.createElement(KEY);
	    	graphInfo.appendChild(sizedfn);
	    	sizedfn.setAttribute("id", "s");
	    	sizedfn.setAttribute("attr.name", SIZE);
	    	sizedfn.setAttribute("for", NODE);
	    	sizedfn.setAttribute("attr.type", "double");
	    	Element sizeDefault = doc.createElement("default");
	    	sizedfn.appendChild(sizeDefault);
	    	sizeDefault.setTextContent(NodeAttribute.DEFAULT_SIZE+"");
	    	//set up a key for attaching label
	    	Element labelDefn = doc.createElement(KEY);
	    	graphInfo.appendChild(labelDefn);
	    	labelDefn.setAttribute("id", "l");
	    	labelDefn.setAttribute("attr.name", LABEL);
	    	labelDefn.setAttribute("for", NODE);
	    	labelDefn.setAttribute("attr.type", "string");
	    	Element labelDefault = doc.createElement("default");
	    	labelDefn.appendChild(labelDefault);
	    	labelDefault.setTextContent("");
	    	//set up a key for attaching color
	    	Element colorlDefn = doc.createElement(KEY);
	    	graphInfo.appendChild(colorlDefn);
	    	colorlDefn.setAttribute("id", "c");
	    	colorlDefn.setAttribute("attr.name", COLOR);
	    	colorlDefn.setAttribute("for", NODE);
	    	colorlDefn.setAttribute("attr.type", "string");
	    	Element colorDefault = doc.createElement("default");
	    	colorlDefn.appendChild(colorDefault);
	    	colorDefault.setTextContent(NodeAttribute.DEFAULT_NODE_COLOR.getRed()+","+
	    			NodeAttribute.DEFAULT_NODE_COLOR.getGreen()+","+
	    			NodeAttribute.DEFAULT_NODE_COLOR.getBlue());
	    	//TODO: define other xml  attributes to be used by nodes and edges
	    	//set up key for edge weight
	    	Element edgeWdefn = doc.createElement(KEY);
	    	graphInfo.appendChild(edgeWdefn);
	    	edgeWdefn.setAttribute("id", "w");
	    	edgeWdefn.setAttribute("attr.name", WEIGHT);
	    	edgeWdefn.setAttribute("for", EDGE);
	    	edgeWdefn.setAttribute("attr.type", "double");
	    	Element weightDefault = doc.createElement("default");
	    	edgeWdefn.appendChild(weightDefault);
	    	weightDefault.setTextContent("1.0");
	    	//create the graph 
	    	Element graph = doc.createElement(GRAPH);
	    	root.appendChild(graph);
	    	// we may set the ide of the graph later using the time
	    	//graph.setAttribute("id", "G");
	    	graph.setAttribute("edgedefault", "directed");
	    	
		   
		    //loop to add each of the nodes
		    Iterator<NodeAttribute> nodeIter = nodes.keySet().iterator();
		    while (nodeIter.hasNext()){
		    	NodeAttribute node = nodeIter.next();
		    	Element nodeEl = doc.createElement(NODE);
		    	graph.appendChild(nodeEl);
		    	nodeEl.setAttribute("id", node.getNodeId()+"");
		    	//start
		    	Element nodeStart = doc.createElement("data");
		    	nodeEl.appendChild(nodeStart);
		    	nodeStart.setAttribute(KEY, "o");
		    	nodeStart.setTextContent(node.getObsTime()+"");
//		    	start
		    	Element nodeEnd = doc.createElement("data");
		    	nodeEl.appendChild(nodeEnd);
		    	nodeEnd.setAttribute(KEY, "e");
		    	nodeEnd.setTextContent(node.getEndTime()+"");
		    	//label
		    	Element nodeLabel = doc.createElement("data");
		    	nodeEl.appendChild(nodeLabel);
		    	nodeLabel.setAttribute(KEY, "l");
		    	nodeLabel.setTextContent(node.getNodeLabel());
		    	//size
		    	Element nodeSize = doc.createElement("data");
		    	nodeEl.appendChild(nodeSize);
		    	nodeSize.setAttribute(KEY, "s");
		    	nodeSize.setTextContent(node.getNodeSize()+"");
		    	//color
		    	Element nodeColor = doc.createElement("data");
		    	nodeEl.appendChild(nodeColor);
		    	nodeColor.setAttribute(KEY, "c");
		    	Color col = node.getNodeColor();
		    	nodeColor.setTextContent(col.getRed()+","+col.getGreen()+","+col.getBlue()+",");
//		    	x
		    	Point2D p = nodes.get(node);
		    	Element nodeX = doc.createElement("data");
		    	nodeEl.appendChild(nodeX);
		    	nodeX.setAttribute(KEY, "x");
		    	nodeX.setTextContent(p.getX()+"");
//		    	y
		    	Element nodeY = doc.createElement("data");
		    	nodeEl.appendChild(nodeY);
		    	nodeY.setAttribute(KEY, "y");
		    	nodeY.setTextContent(p.getY()+"");
		    
		    }
		    //loop for edges
		    Iterator arciter = arcs.iterator();
		    while (arciter.hasNext()){
		    	ArcAttribute arc = (ArcAttribute)arciter.next();
		    	Element arcEl = doc.createElement(EDGE);
		    	graph.appendChild(arcEl);
		    	//we don't have a concept of edge ID in sonia
		    	arcEl.setAttribute("id", arc.hashCode()+"");
		    	arcEl.setAttribute("source", arc.getFromNodeId()+"");
		    	arcEl.setAttribute("target", arc.getToNodeId()+"");
		    	//weight
		    	Element weight = doc.createElement("data");
		    	arcEl.appendChild(weight);
		    	weight.setAttribute(KEY, "w");
		    	weight.setTextContent(arc.getArcWeight()+"");
		    	//color
		    	Element arcColor = doc.createElement("data");
		    	arcEl.appendChild(arcColor);
		    	arcColor.setAttribute(KEY, "c");
		    	Color col = arc.getArcColor();
		    	arcColor.setTextContent(col.getRed()+","+col.getGreen()+","+col.getBlue()+",");
		    	//obs time
		    	Element obsTime = doc.createElement("data");
		    	arcEl.appendChild(obsTime);
		    	obsTime.setAttribute(KEY,"o");
		    	obsTime.setTextContent(arc.getObsTime()+"");
		    	//end Time
		    	Element endTime = doc.createElement("data");
		    	arcEl.appendChild(endTime);
		    	endTime.setAttribute(KEY,"e");
		    	endTime.setTextContent(arc.getEndTime()+"");
		    	//label
		    	if (arc.getArcLabel() != ""){
			    	Element arcLabel = doc.createElement("data");
			    	arcEl.appendChild(arcLabel);
			    	arcLabel.setAttribute(KEY, "l");
			    	arcLabel.setTextContent(arc.getArcLabel());
		    	}
		    	
//		   
		    	
		    }
		    
		    DOMSource ds = new DOMSource(doc);
	        StreamResult sr = new StreamResult(new File(filename+".xml"));
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer trans = tf.newTransformer();
	        trans.transform(ds, sr);	
	        
	        //now clear out the lists
	        nodes.clear();
	        System.out.println("finished xml xport");
	    
	}
	
	//for debuging
	public static void main(String[] args){
		System.out.println("starting xml test");
		GraphMLRender xml = new GraphMLRender();
		try {
			xml.createXML("xmltest", 500, 500, 5, "this is only a test");
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("xml test done");
	}

}
