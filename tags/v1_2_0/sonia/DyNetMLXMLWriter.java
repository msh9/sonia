/* This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package sonia;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sonia.parsers.DotSonColumnMap;
import sonia.parsers.DotSonParser;
import sonia.settings.ApplySettings;
import sonia.settings.BrowsingSettings;
import sonia.settings.GraphicsSettings;
import sonia.settings.LayoutSettings;

/**
 * Class for writing entire network as a DyNetML xml file.
 * http://casos.isri.cmu.edu/dynetml/index.html DTD is copyright from casos and
 * should be downloaded seperately
 * 
 * example: <?xml version = "1.0" encoding = "UTF -8"?> <DynamicNetwork xmlns :
 * x s i="http://www.w3.org/2001/XMLSchema -instance" xsi
 * :noNamespaceSchemaLocation = "DyNetML.xsd"> ><br>
 * <!-- Define the metamatrix... watch comments in the XML for explanation of
 * particular features of the format --><br>
 * 
 * <MetaMatrix timePeriod="1"><br>
 * <!-- A global measure on the entire metamatrix --><br>
 * <measures><br>
 * <measure name="global" type="double" value="3.14"><br>
 * <input id="knowledgeNetwork"/><br>
 * <input id="friendship"/><br>
 * </measure><br>
 * </measures><br>
 * <!-- First, we specify the nodes --><br>
 * <nodes><br>
 * <!-- Nodes are broken up into nodesets by type (e.g. agent, knowledge,
 * resource, task, etc) --><br>
 * <nodeset id="foo" type="agent"><br>
 * <!-- This is the simple node with no extended data --><br>
 * <node id="b"/><br>
 * <!--This is a more complex node with properties and attached measures --><br>
 * <node id="a"><br>
 * <!-- This is how to specify internal node properties (see
 * %admissibleProperties entity in DTD) --><br>
 * <properties><br>
 * <property name="foo" type="double" value="3.14"/><br>
 * <property name="bar" type="double" value="3.14"/><br>
 * </properties><br>
 * <!-- This is how to specify node-level measures --><br>
 * <measures><br>
 * <!-- Each measure is named and accompanied by type (double|string|binary) --><br>
 * <measure name="centrality" type="double" value="3.14"/><br>
 * <measure name="betweenness" type="double" value="3.14"/><br>
 * </measures><br>
 * </node><br>
 * </nodeset><br>
 * <!-- Another nodeset --><br>
 * <nodeset id="bar" type="knowledge"><br>
 * <node id="a1"/><br>
 * <node id="a2" title="boss"/><br>
 * </nodeset><br>
 * </nodes><br>
 * <!-- Now we specify the graphs that comprise the metamatrix --><br>
 * <networks><br>
 * <!-- NOTE: source and target of each edge should be a valid node; however
 * it's up to the software developer to ensure that - or to check consistency in
 * any code that imports this data --><br>
 * <!-- A very simple graph --><br>
 * <graph id="friendship" sourceType="agent" targetType="agent"><br>
 * <edge source="a" target="b" type="binary"/><br>
 * <edge source="b" target="a" type="binary"/><br>
 * </graph><br>
 * <!-- A graph with some graph-level measures --><br>
 * <graph id="advice" sourceType="agent" targetType="agent"><br>
 * <measures><br>
 * <!-- Just like node-level measures; nothing new here --><br>
 * <measure name="centralization" type="double" value="3.14159"/><br>
 * <measure name="foo" type="double" value="3.14159"/><br>
 * <measure name="bar" type="double" value="3.14159"/><br>
 * </measures><br>
 * <edge source="a" target="b" type="binary"/><br>
 * </graph><br>
 * <graph id="knowledgeNetwork" isDirected="true" sourceType="agent"
 * targetType="knowledge"><br>
 * <edge source="a" target="1" type="string" value="foobar"/><br>
 * <edge source="b" target="2" type="double" value="3.14159"/><br>
 * </graph><br>
 * </networks><br>
 * </MetaMatrix><br>
 * </DynamicNetwork><br>
 * 
 * @author skyebend
 * 
 */
public class DyNetMLXMLWriter {

	// constants for tag names in dynetML dtd
	public static final String NAMESPACE = "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
			+ "xsi:noNamespaceSchemaLocation=\"DyNetML.xsd\"";

	public static final String MAIN = "DynamicNetwork";

	public static final String METAMAT = "MetaMatrix";

	/**
	 * collection of nodes and edges
	 */
	public static final String GRAPH = "graph";

	/**
	 * graphs within meta matrix
	 */
	public static final String NETWORKS = "networks";

	/**
	 * A global measure on the entire metamatrix
	 */
	public static final String MEASURES = "measures";

	/**
	 * measures (computed variables) attached to nodes or edges
	 */
	public static final String MEASURE = "measure";

	public static final String NODES = "nodes";

	/**
	 * Nodes are broken up into nodesets by type (e.g. agent, knowledge,
	 * resource, task, etc)
	 */
	public static final String NODESET = "nodeset";

	public static final String NODE = "node";

	public static final String ID = "id";

	/**
	 * sonia id is the numeric id (starting from 1) used by sonia to refer
	 * to nodes, think of it as the matrix index
	 */
	public static final String SID = "soniaId";

	/**
	 * properties attached to nodes or edges or graphs
	 */
	public static final String PROPS = "properties";

	public static final String PROP = "property";

	/**
	 * name of measure or property
	 */
	public static final String NM = "name";

	/**
	 * data type for measure or property (double, string)
	 */
	public static final String TP = "type";

	/**
	 * value of string or property
	 */
	public static final String VAL = "value";

	public static final String EDGE = "edge";

	public static final String SOURCE = "source";

	public static final String TARGET = "target";

	public static final String STR = "string";

	public static final String DBL = "double";

	// -------- constants for sonia tag names

	/**
	 * tag used to store the starting timepoint for the slice in the graph
	 */
	public static final String SLICE_S = "startTime";

	/**
	 * tag used to store the ending timepoint for the slice in the graph
	 */
	public static final String SLICE_E = "endTime";
	
	/**
	 * color specified as a comma seperated rgb triple
	 */
	public static final String NODE_RGB_COLOR = "rgbColorNode";
	
	public static final String EDGE_RGB_COLOR = "rgbColorEdge";
	
	public static final String NODE_BORDER_RGB_COLOR = "rgbColorNodeBorder";
	
	public static final String X_COORDS = "xCoords";
	
	public static final String Y_COORDS = "yCoords";
	
	public static final String UNKOWN_NODE = "?";
	
	/**
	 * hash code that makes it possible to know if the edges
	 * in two graphs are really the same ArcAttribute
	 */
	public static final String EDGE_ID = "edgeID";

	// --------- parser varrs

	private SoniaController control;

	public DyNetMLXMLWriter(SoniaController control) {
		this.control = control;

	}

	public void saveXML(SoniaLayoutEngine engine, String filename)
			throws ParserConfigurationException, TransformerException {
		NetDataStructure data = engine.getNetData();
		// need to figure out the actual start and end time used
		double start = engine.getSlice(0).getSliceStart();
		double end = engine.getSlice(engine.getNumSlices() - 1).getSliceEnd();
		int eventID = 0;
		
		//TODO:store log file within xml?
		// get the set of nodes
		ArrayList events = data.getEventsFromTo(start, end);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		DOMImplementation di = db.getDOMImplementation();

		Document doc = di.createDocument(NAMESPACE, MAIN, null);
		Element root = doc.getDocumentElement();

		// build the xml file
		Element metamat = doc.createElement(METAMAT);
		root.appendChild(metamat);
		// should add meta info here
		//add properties that specify sonia configuration as measures
		Element metaMeasures = doc.createElement(MEASURES);
		metamat.appendChild(metaMeasures);
		
		//store the apply settings as if it was a measrure
		ApplySettings apSet = engine.getCurrentApplySettings();
		Element applySettings = doc.createElement(MEASURE);
		applySettings.setAttribute(ID,apSet.getClass().getCanonicalName());
		applySettings.setAttribute(TP,STR);
		applySettings.setAttribute(VAL,apSet.toString());
		metaMeasures.appendChild(applySettings);
		
		//store the layout settings as if it was a measrure
		LayoutSettings laySet = engine.getLayoutSettings();
		Element layoutSettings = doc.createElement(MEASURE);
		layoutSettings.setAttribute(ID,laySet.getClass().getCanonicalName());
		layoutSettings.setAttribute(TP,STR);
		layoutSettings.setAttribute(VAL,laySet.toString());
		metaMeasures.appendChild(layoutSettings);
		
		//store the graphics settings as if it was a measure
		GraphicsSettings graphicsSet = control.getGraphicsSettings();
		Element graphSettings = doc.createElement(MEASURE);
		graphSettings.setAttribute(ID,graphicsSet.getClass().getCanonicalName());
		graphSettings.setAttribute(TP,STR);
		graphSettings.setAttribute(VAL,graphicsSet.toString());
		metaMeasures.appendChild(graphSettings);
		
		//store the browsing settings as if it was a measure
		BrowsingSettings browseSet = control.getBrowsingSettings();
		Element browSettings = doc.createElement(MEASURE);
		browSettings.setAttribute(ID,browseSet.getClass().getCanonicalName());
		browSettings.setAttribute(TP,STR);
		browSettings.setAttribute(VAL,browseSet.toString());
		metaMeasures.appendChild(browSettings);
		
		// define nodes
		Element nodes = doc.createElement(NODES);
		metamat.appendChild(nodes);
		Element nodeset = doc.createElement(NODESET);
		nodes.appendChild(nodeset);
		nodeset.setAttribute(ID, "sonianodes"); // could use a better name here
		// loop over set of nodes and create them
		Iterator eventiter = events.iterator();
		// TODO: give warning that original alpha ids and some other data from
		// the input file will not be preserved
		//TODO:  create a set of dummy nodes for edges to attach to?
		while (eventiter.hasNext()) {
			NetworkEvent event = (NetworkEvent) eventiter.next();
			if (event instanceof NodeAttribute) {
				NodeAttribute nodeAttr = (NodeAttribute) event;

				Element node = doc.createElement(NODE);
				node.setAttribute(ID, eventID + "");
				Element props = doc.createElement(PROPS);
				node.appendChild(props);
				Element prop = doc.createElement(PROP);
				// attributes
				// label
				
				prop.setAttribute(ID, DotSonColumnMap.NODE_LABEL);
				prop.setAttribute(TP, STR);
				prop.setAttribute(VAL, nodeAttr.getNodeLabel());
				props.appendChild(prop);

				// sonia id
				prop = doc.createElement(PROP);
				prop.setAttribute(ID, SID);
				prop.setAttribute(TP, STR);
				prop.setAttribute(VAL, nodeAttr.getNodeId() + "");
				props.appendChild(prop);
				// start time
				prop = doc.createElement(PROP);
				prop.setAttribute(ID, DotSonColumnMap.NODE_STARTIME);
				prop.setAttribute(TP, DBL);
				prop.setAttribute(VAL, nodeAttr.getObsTime() + "");
				props.appendChild(prop);
				// end time
				prop = doc.createElement(PROP);
				prop.setAttribute(ID, DotSonColumnMap.NODE_ENDTIME);
				prop.setAttribute(TP, DBL);
				prop.setAttribute(VAL, nodeAttr.getEndTime() + "");
				props.appendChild(prop);
//				 file x
				prop = doc.createElement(PROP);
				prop.setAttribute(ID, DotSonColumnMap.NODE_X_COORD);
				prop.setAttribute(TP, DBL);
				prop.setAttribute(VAL, nodeAttr.getObsXCoord() + "");
				props.appendChild(prop);
//				 file y
				prop = doc.createElement(PROP);
				prop.setAttribute(ID, DotSonColumnMap.NODE_Y_COORD);
				prop.setAttribute(TP, DBL);
				prop.setAttribute(VAL, nodeAttr.getObsYCoord() + "");
				props.appendChild(prop);
				// size
				prop = doc.createElement(PROP);
				prop.setAttribute(ID, DotSonColumnMap.NODE_SIZE);
				prop.setAttribute(TP, DBL);
				prop.setAttribute(VAL, nodeAttr.getNodeSize() + "");
				props.appendChild(prop);
			
				//node color
				prop = doc.createElement(PROP);
				Color c = nodeAttr.getNodeColor();
				prop.setAttribute(ID, NODE_RGB_COLOR);
				prop.setAttribute(TP, STR);
				prop.setAttribute(VAL, c.getRed()+","+c.getGreen()+","+c.getBlue());
				props.appendChild(prop);
				
				//node shape
				prop = doc.createElement(PROP);
				prop.setAttribute(ID, DotSonColumnMap.NODE_SHAPE);
				prop.setAttribute(TP, STR);
				prop.setAttribute(VAL, ShapeFactory.getStringFor(nodeAttr.getNodeShape()));
				props.appendChild(prop);
				
				//border width
				prop = doc.createElement(PROP);
				prop.setAttribute(ID, DotSonColumnMap.NODE_BORDER_WIDTH);
				prop.setAttribute(TP, DBL);
				prop.setAttribute(VAL, nodeAttr.getBorderWidth()+"");
				props.appendChild(prop);
				
				//node border color
				Color bc =nodeAttr.getBorderColor();
				prop = doc.createElement(PROP);
				prop.setAttribute(ID, NODE_BORDER_RGB_COLOR);
				prop.setAttribute(TP, DBL);
				prop.setAttribute(VAL, bc.getRed()+","+bc.getGreen()+","+bc.getBlue());
				props.appendChild(prop);
				
				// add rest of properties
				
				//add user data elements
				if (nodeAttr.getDataKeys() != null){
					Iterator keyiter = nodeAttr.getDataKeys().iterator();
					while (keyiter.hasNext()){
						String key = (String)keyiter.next();
						prop = doc.createElement(PROP);
						prop.setAttribute(ID, key);
						prop.setAttribute(TP, STR);
						prop.setAttribute(VAL, nodeAttr.getData(key).toString());
						props.appendChild(prop);
					}
				}

				nodeset.appendChild(node);
				eventID++;
			}  
		}

		// create the graph to hold the slice info
		Element networks = doc.createElement(NETWORKS);
		metamat.appendChild(networks);
		Element graph = null;
		// loop over slices, and get all the edges
		for (int g = 0; g < engine.getNumSlices(); g++) {
			LayoutSlice slice = engine.getSlice(g);
			graph = doc.createElement(GRAPH);
			networks.appendChild(graph);
			graph.setAttribute(ID, g + "");
			graph.setAttribute("isDirected", "true");
			
			// set properties of graph
			Element props = doc.createElement(PROPS);
			graph.appendChild(props);
			
			// start time of slice
			Element prop = doc.createElement(PROP);
			graph.appendChild(prop);
			prop.setAttribute(ID, SLICE_S);
			prop.setAttribute(TP, DBL);
			prop.setAttribute(VAL, slice.getSliceStart() + "");
			props.appendChild(prop);
			
			// end time of slice
			prop = doc.createElement(PROP);
			prop.setAttribute(ID, SLICE_E);
			prop.setAttribute(TP, DBL);
			prop.setAttribute(VAL, slice.getSliceEnd() + "");
			props.appendChild(prop);
			
			// need to store xy coords
			prop = doc.createElement(PROP);
			prop.setAttribute(ID, X_COORDS);
			prop.setAttribute(TP, STR);
			prop.setAttribute(VAL, arrayToString(slice.getXCoords()));
			props.appendChild(prop);
			prop = doc.createElement(PROP);
			prop.setAttribute(ID, Y_COORDS);
			prop.setAttribute(TP, STR);
			prop.setAttribute(VAL, arrayToString(slice.getYCoords()));
			props.appendChild(prop);

			// STORE THE APPROPRIATE EDGES IN THE GRAPH
			// THIS IS NOT VERY EFFICIENT SINCE THE SAME
			// AC MAY APPEAR MULTIPLE TIMES
			// get a render slice so we can get the eges
			RenderSlice render = engine.getRenderSlice(slice.getSliceStart(),slice.getSliceEnd());
			Iterator sliceArcs = render.getArcEvents().iterator();
			
			while (sliceArcs.hasNext()){
				ArcAttribute arcattr = (ArcAttribute)sliceArcs.next();
		
				//BIG PROBLEM HERE, CANT USE THE SOURCE TARGET BECAUSE
				//AN ARC ATTRIBUTE COULD LINK TO MULTIPLE NODES
				//TODO: add check to see if node attributes do not change?
				Element edge = doc.createElement(EDGE);
				edge.setAttribute(SOURCE,UNKOWN_NODE);
				edge.setAttribute(TARGET,UNKOWN_NODE);
				edge.setAttribute(TP,DBL);
				edge.setAttribute(VAL,arcattr.getArcWeight()+"");
				graph.appendChild(edge);
				//edge properties
				props = doc.createElement(PROPS);
				
				//from id
				prop = doc.createElement(PROP);
				prop.setAttribute(ID,DotSonColumnMap.FROM_ID);
				prop.setAttribute(TP,STR);
				prop.setAttribute(VAL,arcattr.getFromNodeId()+"");
				props.appendChild(prop);
				
				//to id
				prop = doc.createElement(PROP);
				prop.setAttribute(ID,DotSonColumnMap.TO_ID);
				prop.setAttribute(TP,STR);
				prop.setAttribute(VAL,arcattr.getToNodeId()+"");
				props.appendChild(prop);
				
//				start time
				prop = doc.createElement(PROP);
				prop.setAttribute(ID,DotSonColumnMap.ARC_STARTIME);
				prop.setAttribute(TP,DBL);
				prop.setAttribute(VAL,arcattr.getObsTime()+"");
				props.appendChild(prop);
				
//				end time
				prop = doc.createElement(PROP);
				prop.setAttribute(ID,DotSonColumnMap.ARC_ENDTIME);
				prop.setAttribute(TP,DBL);
				prop.setAttribute(VAL,arcattr.getEndTime()+"");
				props.appendChild(prop);
				
				//edge hash
				prop = doc.createElement(PROP);
				prop.setAttribute(ID,EDGE_ID);
				prop.setAttribute(TP,STR);
				prop.setAttribute(VAL,arcattr.hashCode()+"");
				props.appendChild(prop);
				
//				 width
				prop = doc.createElement(PROP);
				prop.setAttribute(ID,DotSonColumnMap.ARC_WIDTH);
				prop.setAttribute(TP,STR);
				prop.setAttribute(VAL,arcattr.getArcWidth()+"");
				props.appendChild(prop);
				
//				color 
				Color c = arcattr.getArcColor();
				prop = doc.createElement(PROP);
				prop.setAttribute(ID,EDGE_RGB_COLOR);
				prop.setAttribute(TP,DBL);
				prop.setAttribute(VAL,c.getRed()+","+c.getGreen()+","+c.getBlue());
				props.appendChild(prop);
				
				//label optional
				if (arcattr.getArcLabel() != ""){
					prop = doc.createElement(PROP);
					prop.setAttribute(ID,DotSonColumnMap.ARC_LABEL);
					prop.setAttribute(TP,DBL);
					prop.setAttribute(VAL,arcattr.getArcLabel());
					props.appendChild(prop);
				}
				
				
				
				edge.appendChild(props);
				
			} //end slice edges loop
			

		}// end graph loop

		// transform and write out the xml file
		DOMSource ds = new DOMSource(doc);
		StreamResult sr = new StreamResult(new File(filename));
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer trans = tf.newTransformer();
		trans.transform(ds, sr);

		control.showStatus("saved network as DyNetML file: " + filename);
		control.log("saved network as DyNetML file: " + filename);
		// TODO: list attribute not preserved by dynetml export
	}// end save
	
	private String arrayToString(double[] coords){
		String array = "";
		for (int i = 0; i < coords.length; i++) {
			array = array+coords[i]+",";
		}
		return array.substring(0,array.length()-1)+"";
	}

}
