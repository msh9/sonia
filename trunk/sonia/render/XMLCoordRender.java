package sonia.render;

import sonia.ArcAttribute;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;

import java.awt.geom.Point2D;
import java.io.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.w3c.dom.*;
import sonia.NodeAttribute;





/**
 * stores nodes coords labels, and optionaly urls, to an xml file to be loaded into a web browser
 * should save jpeg image?
 * 
 * <networkimage id="1" width="800" height="500" mouserange="10" caption="this is my image" >
 *  <nodeitem  id="a1" xpos="324" ypos="123" label="hi there" url="http//go.here.com" />
  * <nodeitem   id="b2" xpos="700" ypos="323" label="hi there2" url="http//go.here.com" />
  * <nodeitem   id="c3" xpos="600" ypos="423" label="hi there3" url="http//go.here.com" />
  * <nodeitem   id="d4" xpos="300" ypos="213" label="hi there4" url="http//go.here.com" />
  * <nodeitem   id="e5" xpos="378" ypos="453" label="hi there5" url="http//go.here.com" />
* </networkimage>
 * @author skyebend
 *
 */
public class XMLCoordRender implements Render {
	
	public static final String MAIN = "networkimage";
	public static final String XTAG = "xpos";
	public static final String YTAG = "ypos";
	public static final String NODE = "nodeitem";
	public static final String URL = "url";
	public static final String LABEL = "label";
	public static final String RANGE = "mouserange";
	public static final String CAP = "caption";
	
	private Hashtable<NodeAttribute, Point2D>nodes = new Hashtable();

	
	

	/**
	 * ignored
	 */
	public void paintArc(ArcAttribute arc, float widthFactor, double fromX,
			double fromY, double toX, double toY, boolean flash,
			boolean arrows, boolean labels) {
		// TODO Auto-generated method stub

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

	        Document doc = di.createDocument(null, "soniaimageinfo", null);
	        Element root = doc.getDocumentElement();
//	      This element will change in each line
	    	Element coordBlock = null;
	        
	    	coordBlock = doc.createElement(MAIN);
	    	coordBlock.setAttribute("width",width+"");
	    	coordBlock.setAttribute("height",height+"");
	    	coordBlock.setAttribute(RANGE,mouseRange+"");
	    	coordBlock.setAttribute(CAP,caption);
		    root.appendChild(coordBlock);
		    //loop to add each of the nodes
		    Iterator<NodeAttribute> nodeIter = nodes.keySet().iterator();
		    while (nodeIter.hasNext()){
		    	NodeAttribute node = nodeIter.next();
		    	Element coordEl = doc.createElement(NODE);
		    	coordEl.setAttribute(XTAG, nodes.get(node).getX()+"");
		    	coordEl.setAttribute(YTAG, nodes.get(node).getY()+"");
		    	coordEl.setAttribute("id", node.getNodeId()+"");
		    	coordEl.setAttribute(LABEL, node.getNodeLabel());
		    	coordBlock.appendChild(coordEl);
		    }
		    
		    DOMSource ds = new DOMSource(doc);
	        StreamResult sr = new StreamResult(new File(filename+".xml"));
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer trans = tf.newTransformer();
	        trans.transform(ds, sr);	
	        
	        //now clear out the lists
	        nodes.clear();
	    
	}
	
	//for debuging
	public static void main(String[] args){
		System.out.println("starting xml test");
		XMLCoordRender xml = new XMLCoordRender();
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
