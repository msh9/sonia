package sonia.parsers;

import java.io.*;
import java.util.*;

import java.awt.Color;
import java.awt.geom.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import sonia.ArcAttribute;
import sonia.DyNetMLXMLWriter;
import sonia.NodeAttribute;
import sonia.PropertyBuilder;
import sonia.ShapeFactory;
import sonia.settings.ApplySettings;
import sonia.settings.BrowsingSettings;
import sonia.settings.GraphicsSettings;
import sonia.settings.LayoutSettings;
import sonia.settings.PropertySettings;

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
 * This version is written to use the SAX framework so that it doesn't run out
 * of memory
 */
public class DyNetMLSAXParser extends DefaultHandler implements Parser {

	private Vector<NodeAttribute> nodeList;

	private Vector<ArcAttribute> arcList;

	private Vector<Double> sliceStarts = null;

	private Vector<Double> sliceEnds = null;

	private Vector<double[]> xCoordArrays = null;

	private Vector<double[]> yCoordArrays = null;

	private ApplySettings appSet = null;

	private LayoutSettings laySet = null;

	private GraphicsSettings graphicSet = null;

	private BrowsingSettings browseSet = null;

	private HashMap<String, Integer> alphaIdMap = null;

	private int maxNodes = 0;

	private int nextNodeIndex = 1;

	private String parseInfo = "";

	private String filename;

	public static final double DEFAULT_START_TIME = 0.0;

	public static final double DEFAULT_END_TIME = 1;

	// crude ways to track context so we know which tags we are parsing

	private boolean isMeasuresContext = false;
	private boolean isNodesContext = false;
	private boolean isNetworksContext = false;
	private boolean isGraphContext = false;
	private boolean isEdgeContext = false;
	private boolean isPropertiesContext = false;

	private HashMap<String, String> nodePropMap = null; // for storying node
														// props while we
														// assemble into node.
	private String dynetMLNodeId = null;
	private HashMap<String, String> graphPropMap = null;
	private String gid = null; // for indentifying active graph element
	HashSet<String> foundEdges = null;

	private HashMap<String, String> edgePropMap = null;
	private String edgeSource = null; // these hold values parsed from xml when
										// not written by sonia.
	private String edgeTarget = null;
	private String edgeWeight = null;

	public DyNetMLSAXParser() {

	}

	public void configureParser(PropertySettings settings) {
		// TODO Auto-generated method stub

	}

	public Vector<ArcAttribute> getArcList() {
		return arcList;
	}

	public int getMaxNumNodes() {
		return maxNodes;
	}

	public String getNetInfo() {
		return parseInfo;
	}

	public Vector<NodeAttribute> getNodeList() {
		return nodeList;
	}

	public int getNumArcEvents() {
		return arcList.size();
	}

	public int getNumNodeEvents() {
		return nodeList.size();
	}

	public String getParserInfo() {
		return "DyNetML XML parser (SAX)";
	}

	public void parseNetwork(String fileAndPath) throws IOException {
		filename = fileAndPath;
		nodeList = new Vector<NodeAttribute>();
		arcList = new Vector<ArcAttribute>();
		sliceStarts = new Vector<Double>();
		sliceEnds = new Vector<Double>();
		xCoordArrays = new Vector<double[]>();
		yCoordArrays = new Vector<double[]>();
		foundEdges = new HashSet<String>();
		// create xml stuff
		SAXParserFactory factory = SAXParserFactory.newInstance();

		// factory.setValidating(true);
		// factory.setNamespaceAware(true);

		// try to read in the xml file
		//each element parsed will trigger the startElement method
		//then we have to respond appropriately
		try {
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new File(fileAndPath), this);
			// TODO: need to validate xml?
			// TODO: check file version?
			// debug
			System.out.println("xml parsing done");
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("Error parsing dynetML xml:" + e.getCause()
					+ " " + e.getMessage());

		}

	}

	/**
	 * check if it is a name indicating what part of the file we are reading. If
	 * it itis, set the appropriate context, other wise parse property
	 */
	public void startElement(String namespaceURI, String sName, // simple name
			String qName, // qualified name
			Attributes attrs) throws SAXException {
		String eName = sName; // element name
		if ("".equals(eName)) eName = qName; // not namespace-aware

		// check if it is context indicating what section we are in
		if (eName.equals(DyNetMLXMLWriter.MEASURES))
			isMeasuresContext = true;

		if (eName.equals(DyNetMLXMLWriter.MEASURE) & isMeasuresContext) {
			// this is likely a sonia application setting, so process
			// to get application settings
			parseMeasureAttrs(attrs);
		}

		if (eName.equals(DyNetMLXMLWriter.NODES))
			isNodesContext = true;
		if (eName.equals(DyNetMLXMLWriter.NODE) & isNodesContext) {
			// assume we are starting a new node so create an object to collect
			// the properties
			nodePropMap = new HashMap<String, String>();
			// get the id, incase we are going to use it as the node id
			dynetMLNodeId = attrs.getValue(DyNetMLXMLWriter.ID);
		}

		if (eName.equals(DyNetMLXMLWriter.PROPS))
			isPropertiesContext = true;
		// TODO: add catch for proprties outside of properties context?

		// if we hit a property and we are in the nodes section, assume it
		// belongs to a node
		if (eName.equals(DyNetMLXMLWriter.PROP) & isNodesContext) {
			// get the property value and copy it into the list of properties
			// for later addition to the node
			String propId = attrs.getValue(DyNetMLXMLWriter.ID);
			String propValue = attrs.getValue(DyNetMLXMLWriter.VAL);
			nodePropMap.put(propId, propValue);
		}

		if (eName.equals(DyNetMLXMLWriter.NETWORKS))
			isNetworksContext = true;
		if (eName.equals(DyNetMLXMLWriter.GRAPH)) {
			gid = attrs.getValue(DyNetMLXMLWriter.ID);
			isGraphContext = true;
			graphPropMap = new HashMap<String, String>();
		}

		// if we hit a property and we are in the graph context, assume we are
		// trying to get slice properties UNLESS WE ARE IN EDGE CONTEXT
		if (eName.equals(DyNetMLXMLWriter.PROP) & (isGraphContext & !isEdgeContext)) {
			String propId = attrs.getValue(DyNetMLXMLWriter.ID);
			String propValue = attrs.getValue(DyNetMLXMLWriter.VAL);
			graphPropMap.put(propId, propValue);
		}
		/*
		 * when reading a standard dynetml file it will respect the source and
		 * target attributes, but when reading one generated by sonia it has to
		 * use the FROM_ID and TO_ID properties so that it can use the sonia
		 * ids. also the same edge may appear in multiple graphs, so we have to
		 * ignore repeats by using the edge id hashcode.
		 */
		if (eName.equals(DyNetMLXMLWriter.EDGE)) {
			isEdgeContext = true;
			edgeSource = attrs.getValue(DyNetMLXMLWriter.SOURCE);
			edgeTarget = attrs.getValue(DyNetMLXMLWriter.TARGET);
			edgeWeight = attrs.getValue(DyNetMLXMLWriter.VAL);
			edgePropMap = new HashMap<String, String>();
		}
		
		if (eName.equals(DyNetMLXMLWriter.PROP) & isEdgeContext) {
			String propId = attrs.getValue(DyNetMLXMLWriter.ID);
			String propValue = attrs.getValue(DyNetMLXMLWriter.VAL);
			edgePropMap.put(propId, propValue);
		}

	}

	/**
	 * make sure we "turn off" the context for whatever file we are reading, and
	 * create the appropriate object if props have been loaded
	 */
	public void endElement(String namespaceURI, String sName, // simple name
			String qName // qualified name
	) throws SAXException {
		String eName = sName; // element name
		if ("".equals(eName))
			eName = qName; // not namespace-aware
		// check if it is context indicating what section we are in
		if (eName.equals(DyNetMLXMLWriter.MEASURES))
			isMeasuresContext = false;
		if (eName.equals(DyNetMLXMLWriter.NODES))
			isNodesContext = false;
		if (eName.equals(DyNetMLXMLWriter.NODE) & isNodesContext) {
			nodeList.add(parseNode(dynetMLNodeId, nodePropMap));
			nodePropMap = null;
			dynetMLNodeId = null;
		}
		if (eName.equals(DyNetMLXMLWriter.PROPS))
			isPropertiesContext = false;
		if (eName.equals(DyNetMLXMLWriter.NETWORKS))
			isNetworksContext = false;
		if (eName.equals(DyNetMLXMLWriter.GRAPH)) {
			parseSlice(graphPropMap, gid);
			graphPropMap = null;
			isGraphContext = false;
		}
		if (eName.equals(DyNetMLXMLWriter.EDGE)) {
			ArcAttribute newArc = parseEdge(edgePropMap, edgeSource, edgeTarget, edgeWeight);
			if (newArc != null){
				arcList.add(newArc);
			}

			edgeSource = null;
			edgeTarget = null;
			edgeWeight = null;
			edgePropMap = null;
			isEdgeContext = false;
		}

	}

	/**
	 * parses a measures element for various configuration settings for sonia if
	 * the file has been saved by sonia
	 * 
	 * @author skyebend
	 * @param doc
	 */
	private void parseMeasureAttrs(Attributes attrs) {

		// look for the id, type and value

		String id = attrs.getValue(DyNetMLXMLWriter.ID);
		String value = attrs.getValue(DyNetMLXMLWriter.VAL);

		// check if it is alayout setting
		if (id.equals(ApplySettings.class.getCanonicalName())) {
			PropertyBuilder builder = new PropertyBuilder(value);
			appSet = builder.getApplySettings();
		}
		// check if it is an apply setting
		else if (id.equals(LayoutSettings.class.getCanonicalName())) {
			PropertyBuilder builder = new PropertyBuilder(value);
			laySet = builder.getLayoutSettings();
		}
		// check if it is a graphics settings
		else if (id.equals(GraphicsSettings.class.getCanonicalName())) {
			PropertyBuilder builder = new PropertyBuilder(value);
			graphicSet = builder.getGraphicsSettings();
		}
		// check if it is a browsing setting
		else if (id.equals(BrowsingSettings.class.getCanonicalName())) {
			PropertyBuilder builder = new PropertyBuilder(value);
			browseSet = builder.getBrowsingSettings();
		} else {
			// it was some other kind of measure that we ignored
			parseInfo += "ignored measure with id" + id;
		}

	}

	/**
	 * parses the slice properties (like node coordinates and time bounds)
	 * 
	 * @param sliceProps
	 * @param gid
	 * @throws SAXException
	 */
	private void parseSlice(HashMap<String, String> sliceProps, String gid)
			throws SAXException {
		double sliceStart = DEFAULT_START_TIME;
		double sliceEnd = DEFAULT_END_TIME;
		double[] xcoords = null;
		double[] ycoords = null;
		// THE DATA COULD GET OUT OF SYNCH IF SOME XML GRAPHS HAVE SOME BUT NOT
		// ALL OF THE PARAMS...
		// slice start
		if (sliceProps.containsKey(DyNetMLXMLWriter.SLICE_S)) {
			try {
				sliceStart = Double.parseDouble((String) sliceProps
						.remove(DyNetMLXMLWriter.SLICE_S));
				sliceStarts.add(sliceStart);
			} catch (NumberFormatException nfe) {
				String msg = "Unable to parse graph slice start time from graph xml id"
						+ gid;
				throw new SAXException(msg);
			}
		}

		// slice end
		if (sliceProps.containsKey(DyNetMLXMLWriter.SLICE_E)) {
			try {
				sliceEnd = Double.parseDouble((String) sliceProps
						.remove(DyNetMLXMLWriter.SLICE_E));
				sliceEnds.add(sliceEnd);
			} catch (NumberFormatException nfe) {
				String msg = "Unable to parse graph slice end time from graph xml id"
						+ gid;
				throw new SAXException(msg);
			}
		}

		// slice x coord
		if (sliceProps.containsKey(DyNetMLXMLWriter.X_COORDS)) {
			xcoords = arrayify((String) sliceProps
					.remove(DyNetMLXMLWriter.X_COORDS));
			xCoordArrays.add(xcoords);
		}

		// slice y coord
		if (sliceProps.containsKey(DyNetMLXMLWriter.Y_COORDS)) {
			ycoords = arrayify((String) sliceProps
					.remove(DyNetMLXMLWriter.Y_COORDS));
			yCoordArrays.add(ycoords);
		}
	}



	/**
	 * Determines if we are creating a "normal" edge or an edge that was created
	 * by sonia. If sonia built it (for now determined by checking if the source
	 * and target are the missing value) it will is the fromID and toID values
	 * as the actual source and target (they should link to SIDs). Will also
	 * check the edges ID number (hash code) to see if it has already been
	 * created so as not to double up the edge if it already appears in the
	 * graph.
	 * 
	 * @author skyebend
	 * @param edgeProps
	 * @param xmlSrc
	 * @param xmlTarget
	 * @throws Exception
	 */
	private ArcAttribute parseEdge(HashMap<String, String> edgeProps,
			String xmlSrc, String xmlTarget, String weightValue)
			throws SAXException {
		// figure out if it is a normal or sonia edge
		ArcAttribute edge = null;
		int from = -1;
		int to = -1;
		double start = DEFAULT_START_TIME;
		double end = DEFAULT_END_TIME;
		double weight = 1;
		double width = 1;

		// check that the weight is something parsable
		try {
			weight = Double.parseDouble(weightValue);
		} catch (NumberFormatException nfe) {
			throw new SAXException("Unable to parse value of edge "
					+ weightValue + " as a double edge weight");
		}

		if (xmlSrc.equals(DyNetMLXMLWriter.UNKOWN_NODE)
				& xmlTarget.equals(DyNetMLXMLWriter.UNKOWN_NODE)) {
			// it should have an edge id so we can identify it
			String eid = "";
			if (edgeProps.containsKey(DyNetMLXMLWriter.EDGE_ID)) {
				eid = (String) edgeProps.remove(DyNetMLXMLWriter.EDGE_ID);
			} else {
				throw new SAXException(
						"Unable to locate sonia edge id in edge xml record");
			}

			// check if this edge already exists
			if (foundEdges.contains(eid)) {
				parseInfo += "skipped duplicate of edge with id " + eid+"\n";
				// stop here and don't create the edge again
				return null;
			}

			// if we got this far, assume it is a sonia edge
			if (edgeProps.containsKey(DotSonColumnMap.FROM_ID)) {
				from = Integer.parseInt((String) edgeProps
						.remove(DotSonColumnMap.FROM_ID));
				// add number format catch
			} else {
				throw new SAXException(
						"Unable to locate edge source ID in edge xml record id "
								+ eid);
			}
			if (edgeProps.containsKey(DotSonColumnMap.TO_ID)) {
				to = Integer.parseInt((String) edgeProps
						.remove(DotSonColumnMap.TO_ID));
				// add number format catch
			} else {
				throw new SAXException(
						"Unable to locate edge target ID in edge xml record id "
								+ eid);
			}

			if (edgeProps.containsKey(DotSonColumnMap.ARC_STARTIME)) {
				try {
					start = Double.parseDouble((String) edgeProps
							.remove(DotSonColumnMap.ARC_STARTIME));
				} catch (NumberFormatException nfe) {
					String msg = "Unable to parse edge start time from xml edge record id "
							+ eid;
					throw new SAXException(msg);
				}
			}

			if (edgeProps.containsKey(DotSonColumnMap.ARC_ENDTIME)) {
				try {
					end = Double.parseDouble((String) edgeProps
							.remove(DotSonColumnMap.ARC_ENDTIME));
				} catch (NumberFormatException nfe) {
					String msg = "Unable to parse edge end time from xml edge record id "
							+ eid;
					throw new SAXException(msg);
				}
			}

			if (edgeProps.containsKey(DotSonColumnMap.ARC_WIDTH)) {
				try {
					width = Double.parseDouble((String) edgeProps
							.remove(DotSonColumnMap.ARC_WIDTH));
				} catch (NumberFormatException nfe) {
					String msg = "Unable to parse edge width from xml edge record id "
							+ eid;
					throw new SAXException(msg);
				}
			}
			// create the edge
			edge = new ArcAttribute(start, end, from, to, weight, width);

			// color
			if (edgeProps.containsKey(DyNetMLXMLWriter.EDGE_RGB_COLOR)) {
				Color c = parseRGB((String) edgeProps
						.remove(DyNetMLXMLWriter.EDGE_RGB_COLOR));
				edge.setArcColor(c);

			}

			// edge label
			if (edgeProps.containsKey(DotSonColumnMap.ARC_LABEL)) {
				edge.setArcLabel((String) edgeProps
						.remove(DotSonColumnMap.ARC_LABEL));
			}

			// TODO: more edge attributes
			foundEdges.add(eid);

		} else { // it must be a normal (non-sonia) edge
			// check that nodes exist, and get the sonia ids corresponding to
			// the source and target ids
			if (alphaIdMap.containsKey(xmlSrc)) {
				from = ((Integer) alphaIdMap.get(xmlSrc)).intValue();
			} else {
				throw new SAXException(
						"Unable to locate node with id matching source id "
								+ xmlSrc);
			}
			if (alphaIdMap.containsKey(xmlTarget)) {
				to = ((Integer) alphaIdMap.get(xmlTarget)).intValue();
			} else {
				throw new SAXException(
						"Unable to locate node with id matching target id "
								+ xmlSrc);
			}
			edge = new ArcAttribute(start, end, from, to, weight, width);
		}
		
		return edge;
	}

	private NodeAttribute parseNode(String dynetmlNodeId, HashMap nodeProps)
			throws SAXException {
		// if it doesn't have a sonia ID, need to assume the ids are alpha
		// store in a hasgmap with int values
		int soniaID = -1;
		if (nodeProps.containsKey(DyNetMLXMLWriter.SID)) {
			soniaID = parseID((String) nodeProps.remove(DyNetMLXMLWriter.SID),
					true, dynetmlNodeId);
		} else {
			soniaID = parseID(dynetmlNodeId, false, dynetmlNodeId);
		}
		String label = soniaID + "";
		double start = DEFAULT_START_TIME;
		double end = DEFAULT_END_TIME;
		double x = 0.0;
		double y = 0.0;
		if (nodeProps.containsKey(DotSonColumnMap.NODE_LABEL)) {
			label = (String) nodeProps.remove(DotSonColumnMap.NODE_LABEL);
		}

		if (nodeProps.containsKey(DotSonColumnMap.NODE_STARTIME)) {
			try {
				start = Double.parseDouble((String) nodeProps
						.get(DotSonColumnMap.NODE_STARTIME));
			} catch (NumberFormatException nfe) {
				String msg = "Unable to parse double from node start time "
						+ nodeProps.remove(DotSonColumnMap.NODE_STARTIME)
						+ " for dynetML node " + dynetmlNodeId;
				throw new SAXException(msg);
			}
		}
		if (nodeProps.containsKey(DotSonColumnMap.NODE_ENDTIME)) {
			try {
				end = Double.parseDouble((String) nodeProps
						.remove(DotSonColumnMap.NODE_ENDTIME));
			} catch (NumberFormatException nfe) {
				String msg = "Unable to parse double from node end time "
						+ nodeProps.remove(DotSonColumnMap.NODE_ENDTIME)
						+ " for dynetML node " + dynetmlNodeId;
				throw new SAXException(msg);
			}
		}
		if (nodeProps.containsKey(DotSonColumnMap.NODE_X_COORD)) {
			try {
				start = Double.parseDouble((String) nodeProps
						.remove(DotSonColumnMap.NODE_X_COORD));
			} catch (NumberFormatException nfe) {
				String msg = "Unable to parse double from node x coordinate "
						+ nodeProps.remove(DotSonColumnMap.NODE_X_COORD)
						+ " for dynetML node " + dynetmlNodeId;
				throw new SAXException(msg);
			}
		}
		if (nodeProps.containsKey(DotSonColumnMap.NODE_Y_COORD)) {
			try {
				start = Double.parseDouble((String) nodeProps
						.remove(DotSonColumnMap.NODE_Y_COORD));
			} catch (NumberFormatException nfe) {
				String msg = "Unable to parse double from node y coordinate "
						+ nodeProps.remove(DotSonColumnMap.NODE_Y_COORD)
						+ " for dynetML node " + dynetmlNodeId;
				throw new SAXException(msg);
			}
		}

		// CREATE THE NODE
		NodeAttribute node = new NodeAttribute(soniaID, label, x, y, start,
				end, filename + ":" + "dynetML nodeID=" + dynetmlNodeId);

		// node color
		if (nodeProps.containsKey(DyNetMLXMLWriter.NODE_RGB_COLOR)) {
			Color c = parseRGB((String) nodeProps
					.remove(DyNetMLXMLWriter.NODE_RGB_COLOR));
			node.setNodeColor(c);
		}

		if (nodeProps.containsKey(DotSonColumnMap.NODE_SHAPE)) {
			RectangularShape shape;
			try {
				shape = ShapeFactory.getShapeFor((String) nodeProps
						.remove(DotSonColumnMap.NODE_SHAPE));
			} catch (Exception e) {
				throw new SAXException(e);
			}
			node.setNodeShape(shape);
		}

		// size
		if (nodeProps.containsKey(DotSonColumnMap.NODE_SIZE)) {
			try {
				double size = Double.parseDouble((String) nodeProps
						.remove(DotSonColumnMap.NODE_SIZE));
				node.setNodeSize(size);
			} catch (NumberFormatException nfe) {
				String msg = "Unable to parse double from node size "
						+ " for dynetML node " + dynetmlNodeId;
				throw new SAXException(msg);
			}
		}

		// border width
		if (nodeProps.containsKey(DotSonColumnMap.NODE_BORDER_WIDTH)) {
			try {
				double bwidth = Double.parseDouble((String) nodeProps
						.remove(DotSonColumnMap.NODE_BORDER_WIDTH));
				node.setBorderWidth(bwidth);
			} catch (NumberFormatException nfe) {
				String msg = "Unable to parse double from node border width "
						+ " for dynetML node " + dynetmlNodeId;
				throw new SAXException(msg);
			}
		}

		// node border color
		if (nodeProps.containsKey(DyNetMLXMLWriter.NODE_BORDER_RGB_COLOR)) {
			Color bc = parseRGB((String) nodeProps
					.remove(DyNetMLXMLWriter.NODE_BORDER_RGB_COLOR));
			node.setBorderColor(bc);
		}

		Iterator propIter = nodeProps.keySet().iterator();
		while (propIter.hasNext()) {
			String key = (String) propIter.next();
			node.setData(key, nodeProps.get(key));
		}

		return node;

	}

	private int parseID(String alphaID, boolean isSonia, String dataLoc)
			throws SAXException {
		int id = -1;
		// see if it
		if (isSonia) {
			try {
				id = Integer.parseInt(alphaID);
			} catch (NumberFormatException nfe) {
				String msg = "Unable to parse sonia id " + alphaID
						+ " as an integer for DyNetML node " + dataLoc;
				throw new SAXException(msg);
			}
			maxNodes = Math.max(id, maxNodes);
		} else {
			if (alphaIdMap == null) {
				alphaIdMap = new HashMap<String, Integer>();
				nextNodeIndex = 1;
			}
			if (!alphaIdMap.containsKey(alphaID)) {
				alphaIdMap.put(alphaID, Integer.valueOf(nextNodeIndex));
				id = nextNodeIndex;
				nextNodeIndex++;
			} else {
				id = ((Integer) alphaIdMap.get(alphaID)).intValue();
			}
			maxNodes = alphaIdMap.size();
		}
		return id;
	}

	/**
	 * parses a comma delimited rgb string (12,12,255) into a java color
	 * 
	 * @author skyebend
	 * @param rgb
	 * @return
	 * @throws Exception
	 */
	private Color parseRGB(String rgbString) throws SAXException {
		Color color = null;
		StringTokenizer split = new StringTokenizer(rgbString, ",");
		try {
			int r = Integer.parseInt(split.nextToken());
			int g = Integer.parseInt(split.nextToken());
			int b = Integer.parseInt(split.nextToken());
			color = new Color(r, g, b);
		} catch (NumberFormatException nfe) {
			String msg = "Unable to parse element of rgb color " + rgbString
					+ " must be in range 0 to 255";
			throw new SAXException(msg);
		}

		return color;
	}

	private double[] arrayify(String arrayText) throws SAXException {
		StringTokenizer split = new StringTokenizer(arrayText, ",");
		double[] array = new double[split.countTokens()];
		int index = 0;
		while (split.hasMoreElements()) {
			String dbl = split.nextToken();
			try {
				array[index] = Double.parseDouble(dbl);
				index++;
			} catch (NumberFormatException nfe) {
				throw new SAXException("Unable to parse element of array "
						+ dbl + "as a double");
			}
		}
		return array;
	}

	public Vector getSliceEnds() {
		return sliceEnds;
	}

	public Vector getSliceStarts() {
		return sliceStarts;
	}

	public Vector getXCoordArrays() {
		return xCoordArrays;
	}

	public Vector getYCoordArrays() {
		return yCoordArrays;
	}

	public ApplySettings getApplySettings() {
		return appSet;
	}

	public GraphicsSettings getGraphicsSetttings() {
		return graphicSet;
	}

	public LayoutSettings getLayoutSettings() {
		return laySet;
	}

	public BrowsingSettings getBrowsingSettings() {
		return browseSet;
	}
}