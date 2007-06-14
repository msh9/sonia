package sonia.parsers;

import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.Color;
import java.awt.geom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sonia.ArcAttribute;
import sonia.DyNetMLXMLWriter;
import sonia.NodeAttribute;
import sonia.settings.PropertySettings;
import sonia.ui.AttributeMapperDialog;

/**
 * <p>Title:SoNIA (Social Network Image Animator) </p>
 * <p>Description:Animates layouts of time-based networks
 * <p>Copyright: CopyLeft  2004: GNU GPL</p>
 * <p>Company: none</p>
 * @author Skye Bender-deMoll unascribed
 * @version 1.1
 */

/**
 * Parser for reading XML files in the DyNetML format.
 * http://casos.isri.cmu.edu/dynetml/index.html If the file has been exported by
 * sonia it will contain data to allow recreating a set of layout slices
 * 
 */
public class DyNetMLParser implements Parser {

	private Vector nodeList;

	private Vector arcList;
	
	private HashMap<String,Integer> alphaIdMap = null;

	private int maxNodes = 0;
	
	private int nextNodeIndex = 1;

	private String parseInfo = "";
	
	private String filename;
	
	public static final double DEFAULT_START_TIME = 0.0;
	public static final double DEFAULT_END_TIME = 1;
	

	public DyNetMLParser() {
		nodeList = new Vector();
		arcList = new Vector();
	}

	public void configureParser(PropertySettings settings) {
		// TODO Auto-generated method stub

	}

	public Vector getArcList() {
		return arcList;
	}

	public int getMaxNumNodes() {
		return maxNodes;
	}

	public String getNetInfo() {
		return parseInfo;
	}

	public Vector getNodeList() {
		return nodeList;
	}

	public int getNumArcEvents() {
		return arcList.size();
	}

	public int getNumNodeEvents() {
		return nodeList.size();
	}

	public String getParserInfo() {
		return "DyNetML XML parser";
	}

	public void parseNetwork(String fileAndPath) throws IOException {
		filename = fileAndPath;
		// create xml stuff
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// factory.setValidating(true);
		// factory.setNamespaceAware(true);
		DocumentBuilder builder;
		// try to read in the xml file
		try {
			builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(fileAndPath));
			// read the file info
			// read the network level info (params etc if it is a file written
			// by sonia)
			// read the nodes
			parseNodes(document);
			// read the graph data (coordinates)
			// read the arcs
			parseEdges(document);

		} catch (ParserConfigurationException e) {
			// debug
			e.printStackTrace();
			throw new IOException(e.getMessage());
		} catch (SAXException e) {
			// debug
			e.printStackTrace();
			throw new IOException(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("Error parsing dynetML xml:"+e.getCause()+" "+e.getMessage());
			
		}
	}

	/**
	 * when reading a standard dynetml file it will respect the source and
	 * target attributes, but when reading one generated by sonia it has
	 * to use the FROM_ID and TO_ID properties so that it can use the sonia ids.
	 * also the same edge may appear in multiple graphs, so we have to ignore 
	 * repeats by using the edge id hashcode. 
	 * @author skyebend
	 * @param doc
	 * @throws Exception
	 */
	private void parseEdges(Document doc) throws Exception {
		//TODO: should check if graph is directed
		HashSet foundEdges = new HashSet();
		NodeList edges = doc.getElementsByTagName(DyNetMLXMLWriter.EDGE);
		//this is confusing 'cause we are talking about edges that are xml nodes
		Node xmlEdge = null;
		NamedNodeMap attrs = null;
		for (int e = 0; e< edges.getLength();e++){
			xmlEdge = edges.item(e);
			//get the source and target values associated with the edge
			//IF THIS WAS WRITTEN BY SONIA, SOURCE AND TARGET WILL BE IGNORED
			System.out.println("edge:" +xmlEdge);
			String source = xmlEdge.getAttributes().getNamedItem(DyNetMLXMLWriter.SOURCE).getNodeValue();
			String target =  xmlEdge.getAttributes().getNamedItem(DyNetMLXMLWriter.TARGET).getNodeValue();
			HashMap edgePropMap = new HashMap<String,String>();
			
			NodeList edgeKids = xmlEdge.getChildNodes();
			// look for the properties tag
			for (int k = 0; k < edgeKids.getLength(); k++) {
				Node kid = edgeKids.item(k);
				if (kid.getNodeName().equals(DyNetMLXMLWriter.PROPS)) {
					//if we find the property tag
					NodeList props = kid.getChildNodes();
					//loop over each of the properties
					Node prop = null;
					for (int p = 0; p < props.getLength(); p++) {
						prop = props.item(p);
						attrs = prop.getAttributes();
						String propId=attrs.getNamedItem(DyNetMLXMLWriter.ID).getNodeValue();
						String propValue=attrs.getNamedItem(DyNetMLXMLWriter.VAL).getNodeValue();
						System.out.println("\tnodePropAttrs:" +propId+" "+propValue);
						edgePropMap.put(propId,propValue);

					}
				}
				
			}
			//pass the map of poperties to a method to create an edge
			ArcAttribute edge = parseEdge(edgePropMap,source,target,foundEdges);
			if (edge != null){
				arcList.add(edge);
			}
			
		}// end edges loop
	}
	
	
	/**
	 * Determines if we are creating a "normal" edge or an edge that was created
	 * by sonia.  If sonia built it (for now determined by checking if the source and 
	 * target are the missing value) it will is the fromID and toID values as the
	 * actual source and target (they should link to SIDs).  Will also check the
	 * edges ID number (hash code) to see if it has already been created so as 
	 * not to double up the edge if it already appears in the graph. 
	 * @author skyebend
	 * @param edgeProps
	 * @param xmlSrc
	 * @param xmlTarget
	 * @throws Exception 
	 */
	private ArcAttribute parseEdge(HashMap edgeProps, String xmlSrc, 
			String xmlTarget, HashSet foundEdges) throws Exception{
		//figure out if it is a normal or sonia edge
		ArcAttribute edge = null;
		int from = -1;
		int to = -1;
		double start = DEFAULT_START_TIME;
		double end = DEFAULT_END_TIME;
		double weight = 1; //TODO: need to read weight from the edge's attributes
		double width = 1;
		
		
		if (xmlSrc.equals(DyNetMLXMLWriter.UNKOWN_NODE) &
		xmlSrc.equals(DyNetMLXMLWriter.UNKOWN_NODE)){
			//it should have an edge id so we can identify it
			String eid ="";
			if (edgeProps.containsKey(DyNetMLXMLWriter.EDGE_ID)){
				eid = (String)edgeProps.remove(DyNetMLXMLWriter.EDGE_ID);
			} else {
				throw new Exception("Unable to locate sonia edge id in edge xml record");
			}
			//check if this edge already exists
			if (foundEdges.contains(eid)){
				System.out.println("skipped edge "+eid);
				//stop here and don't create the edge again
				return null;
			}
			
			//if we got this far, assume it is a sonia edge
			if (edgeProps.containsKey(DotSonColumnMap.FROM_ID)){
				from = Integer.parseInt((String)edgeProps.remove(DotSonColumnMap.FROM_ID));
				//add number format catch
			} else {
				throw new Exception("Unable to locate edge source ID in edge xml record id "+eid);
			}
			if (edgeProps.containsKey(DotSonColumnMap.TO_ID)){
				from = Integer.parseInt((String)edgeProps.remove(DotSonColumnMap.TO_ID));
				//add number format catch
			} else {
				throw new Exception("Unable to locate edge target ID in edge xml record id "+eid);
			}
			
			if (edgeProps.containsKey(DotSonColumnMap.ARC_STARTIME)){
				try {
				start = Double.parseDouble((String)edgeProps.remove(DotSonColumnMap.ARC_STARTIME));
				} catch (NumberFormatException nfe){
					String msg = "Unable to parse edge start time from xml edge record id "+eid;
					throw new Exception(msg);
				}
			}
			
			if (edgeProps.containsKey(DotSonColumnMap.ARC_ENDTIME)){
				try {
				end = Double.parseDouble((String)edgeProps.remove(DotSonColumnMap.ARC_ENDTIME));
				} catch (NumberFormatException nfe){
					String msg = "Unable to parse edge end time from xml edge record id "+eid;
					throw new Exception(msg);
				}
			}
			
			if (edgeProps.containsKey(DotSonColumnMap.ARC_WIDTH)){
				try {
				width = Double.parseDouble((String)edgeProps.remove(DotSonColumnMap.ARC_WIDTH));
				} catch (NumberFormatException nfe){
					String msg = "Unable to parse edge width from xml edge record id "+eid;
					throw new Exception(msg);
				}
			}
			
			//TODO: more edge attributes
			
			edge = new ArcAttribute(start,end,from,to,weight,width);
			foundEdges.add(eid);
			
			
		} else { //it must be a normal (non-sonia) edge
			//check that nodes exist, and get the sonia ids corresponding to
			//the source and target ids
			if (alphaIdMap.containsKey(xmlSrc)){
			from = ((Integer)alphaIdMap.get(xmlSrc)).intValue();
			} else {
				throw new Exception("Unable to locate node with id matching source id "+xmlSrc);
			}
			if (alphaIdMap.containsKey(xmlSrc)){
				from = ((Integer)alphaIdMap.get(xmlSrc)).intValue();
				} else {
					throw new Exception("Unable to locate node with id matching source id "+xmlSrc);
				}
		}
		 edge = new ArcAttribute(start,end,from,to,weight,width);
		return edge;
	}
	
	private void parseNodes(Document doc) throws Exception {
		// get all the node attributes defined
		NodeList nodes = doc.getElementsByTagName(DyNetMLXMLWriter.NODE);
		Node xmlNode = null;
		NamedNodeMap attrs = null;
		for (int n = 0; n < nodes.getLength(); n++) {
			xmlNode = nodes.item(n);
			String xmlNodeId = xmlNode.getAttributes().getNamedItem(DyNetMLXMLWriter.ID).getNodeValue();
			HashMap nodePropMap = new HashMap<String,String>();
			// dbug
			System.out.println(xmlNode +" id:"+xmlNodeId);
			NodeList nodeKids = xmlNode.getChildNodes();
			// look for the properties tag
			for (int k = 0; k < nodeKids.getLength(); k++) {
				Node kid = nodeKids.item(k);
				if (kid.getNodeName().equals(DyNetMLXMLWriter.PROPS)) {
					//if we find the property tag
					NodeList props = kid.getChildNodes();
					//loop over each of the properties
					Node prop = null;
					for (int p = 0; p < props.getLength(); p++) {
						prop = props.item(p);
						attrs = prop.getAttributes();
						String propId=attrs.getNamedItem(DyNetMLXMLWriter.ID).getNodeValue();
						String propValue=attrs.getNamedItem(DyNetMLXMLWriter.VAL).getNodeValue();
						System.out.println("\tnodePropAttrs:" +propId+" "+propValue);
						nodePropMap.put(propId,propValue);

					}
				}
				//TODO: add nodeset id as property of node
			}
			//LOAD MEASURES?
			
			//pass the map of node attributes to a method to create a node
			NodeAttribute nodeAttr = parseNode(xmlNodeId,nodePropMap);
			nodeList.add(nodeAttr);
			
		}

	}
	
	private NodeAttribute parseNode(String dynetmlNodeId,HashMap nodeProps) 
	throws Exception{
		//if it doesn't have a sonia ID, need to assume the ids are alpha
		//store in a hasgmap with int values
		int soniaID = -1;
		if (nodeProps.containsKey(DyNetMLXMLWriter.SID)){
			soniaID = parseID((String)nodeProps.get(DyNetMLXMLWriter.SID),true,dynetmlNodeId);
		} else {
			soniaID = parseID(dynetmlNodeId,false,dynetmlNodeId);
		}
		String label = soniaID+"";
		double start = DEFAULT_START_TIME;
		double end = DEFAULT_END_TIME;
		double x = 0.0;
		double y = 0.0;
		if (nodeProps.containsKey(DotSonColumnMap.NODE_LABEL)){
			label = (String)nodeProps.remove(DotSonColumnMap.NODE_LABEL);
		}
	
		if (nodeProps.containsKey(DotSonColumnMap.NODE_STARTIME)){
			try {
			start = Double.parseDouble((String)nodeProps.get(DotSonColumnMap.NODE_STARTIME));
			} catch (NumberFormatException nfe){
				String msg = "Unable to parse double from node start time " +
				nodeProps.remove(DotSonColumnMap.NODE_STARTIME)+" for dynetML node "+dynetmlNodeId;
				throw new Exception(msg);
			}
		}
		if (nodeProps.containsKey(DotSonColumnMap.NODE_ENDTIME)){
			try {
			end = Double.parseDouble((String)nodeProps.get(DotSonColumnMap.NODE_ENDTIME));
			} catch (NumberFormatException nfe){
				String msg = "Unable to parse double from node end time " +
				nodeProps.remove(DotSonColumnMap.NODE_ENDTIME)+" for dynetML node "+dynetmlNodeId;
				throw new Exception(msg);
			}
		}
		if (nodeProps.containsKey(DotSonColumnMap.NODE_X_COORD)){
			try {
			start = Double.parseDouble((String)nodeProps.get(DotSonColumnMap.NODE_X_COORD));
			} catch (NumberFormatException nfe){
				String msg = "Unable to parse double from node x coordinate " +
				nodeProps.remove(DotSonColumnMap.NODE_X_COORD)+" for dynetML node "+dynetmlNodeId;
				throw new Exception(msg);
			}
		}
		if (nodeProps.containsKey(DotSonColumnMap.NODE_Y_COORD)){
			try {
			start = Double.parseDouble((String)nodeProps.get(DotSonColumnMap.NODE_Y_COORD));
			} catch (NumberFormatException nfe){
				String msg = "Unable to parse double from node y coordinate " +
				nodeProps.remove(DotSonColumnMap.NODE_Y_COORD)+" for dynetML node "+dynetmlNodeId;
				throw new Exception(msg);
			}
		}
	
		NodeAttribute node = new NodeAttribute(soniaID,label,x,y,start,end,
				filename+":"+"dynetML nodeID="+dynetmlNodeId);
		
		//TODO:  deal with rest of node elemnts, size etc. 
		Iterator propIter = nodeProps.keySet().iterator();
		{
			String key = (String)propIter.next();
			node.setData(key,nodeProps.get(key));
		}
		
		return node;
		
	}
	
	private int parseID(String alphaID,boolean isSonia, String dataLoc) throws Exception{
		int id = -1;
		//see if it 
		if (isSonia){
			try {
				id = Integer.parseInt(alphaID);
			} catch (NumberFormatException nfe) {
				String msg = "Unable to parse sonia id "+alphaID+
				" as an integer for DyNetML node "+dataLoc;
				throw new Exception(msg);
			}
			maxNodes = Math.max(id,maxNodes);
		} else {
			if (alphaIdMap == null){
				alphaIdMap = new HashMap<String,Integer>();
				nextNodeIndex =1;
			}
			if (!alphaIdMap.containsKey(alphaID)){
				alphaIdMap.put(alphaID,Integer.valueOf(nextNodeIndex));
				id = nextNodeIndex;
				nextNodeIndex++;
			} else {
				id = ((Integer)alphaIdMap.get(alphaID)).intValue();
			}
			maxNodes = alphaIdMap.size();
		}
		return id;
	}

	// main for debugging
	public static void main(String[] args) throws IOException {
		DyNetMLParser parser = new DyNetMLParser();
		parser
				.parseNetwork("C:/Documents and Settings/skyebend/workspace/SoNIA/DyNetMLTest.xml.xml");
		System.out.println("nodes:"+parser.getNodeList());
	}

}