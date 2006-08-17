package sonia.parsers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import sonia.ArcAttribute;
import sonia.NodeAttribute;
import sonia.PropertySettings;

/**
 * Instead of reading a file as input, this class takes sets of arrays passed in
 * via the rJava calling methods from R input should be deparse("myNetwork",
 * control=NULL); [1] "list(list(inl = 1, outl = 4, atl = list(na = FALSE)),
 * list(inl = 1, outl = 5, atl = list(na = FALSE)), list(inl = 2, outl = 3, atl =
 * list(na = FALSE)), list(inl = 2, outl = 4, atl = list(na = FALSE)), list(inl =
 * 3, outl = 1, atl = list(na = FALSE)), list(inl = 3, outl = 4, atl = list(na =
 * FALSE)), list(inl = 4, outl = 1, atl = list(na = FALSE)), list(inl = 4, outl =
 * 2, atl = list(na = FALSE)), list(inl = 4, outl = 3, atl = list(na = FALSE)),
 * list(inl = 4, outl = 5, atl = list(na = FALSE)), list(inl = 5, " [2] "list(n =
 * 5, mnext = 15, directed = TRUE, hyper = FALSE, loops = FALSE, multiple =
 * FALSE, bipartite = FALSE)" [3] "list(list(na = FALSE), list(na = FALSE),
 * list(na = FALSE), list(na = FALSE), list(na = FALSE))" [4] "list(c(2, 1),
 * c(4, 3), c(6, 5), c(7, 8, 10, 9), c(11, 12, 13, 14))" [5] "list(c(11, 7, 5),
 * c(12, 8), c(3, 13, 9), c(1, 4, 14, 6), c(2, 10))"
 * 
 * @author skyebend
 * 
 */
public class RJavaParser implements Parser {

	private Vector nodeList;

	private Vector arcList;

	private boolean isDirected = true;

	private boolean isHyperGraph = false;

	private boolean hasLoops = false;

	private boolean isMultiplex = false;

	// private boolean isBipartite = false;
	private HashMap galTagMap;

	int maxNodes = 0;

	int numEdges = 0;

	private static final String VERTEX_NAME = "vertex.names";

	/**
	 * String may be a list of networks, need to seperate them
	 * String is the output of the paste() command for a network object in R.
	 * Will be a set of lists "mel" : Master Edge List, "gal" : Graph Attribute
	 * List, "val" : Vertex Attribute List, "iel" : In Edge List, "oel", Out
	 * Edge List.
	 */
	public void parseNetwork(String netString) throws IOException {

		nodeList = new Vector();
		arcList = new Vector();

		try {
			Vector mainElements = parseList(netString);
			String mel = (String) mainElements.get(0);
			String gal = (String) mainElements.get(1);
			String val = (String) mainElements.get(2);
			String iel = (String) mainElements.get(3);
			String oel = (String) mainElements.get(4);

			parseGal(gal);
			parseVal(val);
			parseMel(mel);

		} catch (Exception e) {
			// debug
			e.printStackTrace();
			String error = "Unable to parse text representation of R network object: "
					+ e.toString();
			throw (new IOException(error));
		}

	}

	/**
	 * puts the elements of gal into map by variable names gal = list(n = 10,
	 * mnext = 46, directed = TRUE, hyper = FALSE, loops = FALS E, multiple =
	 * FALSE, bipartite = FALSE)
	 * 
	 * @param gal
	 * @throws Exception
	 */
	private void parseGal(String gal) throws Exception {
		galTagMap = new HashMap();
		// should start with "gal = list(..."
		if (!gal.startsWith("gal = ")) {
			throw new Exception(
					"Graph Attribute List does not start with 'gal = '");
		}
		Vector attributeTokens = parseList(gal.substring(6));
		// put all the name = value pairs into a map
		for (int i = 0; i < attributeTokens.size(); i++) {
			String tag = (String) attributeTokens.get(i);
			galTagMap.put(tag.substring(0, tag.indexOf("=") - 1).trim(), tag
					.substring(tag.indexOf("=") + 1).trim());
		}
		maxNodes = Integer.parseInt((String) galTagMap.get("n"));
		numEdges = Integer.parseInt((String) galTagMap.get("mnext")) - 1;
		isDirected = parseRBoolIsTrue((String) galTagMap.get("directed"));
		isHyperGraph = parseRBoolIsTrue((String) galTagMap.get("hyper"));
		hasLoops = parseRBoolIsTrue((String) galTagMap.get("loops"));
		isMultiplex = parseRBoolIsTrue((String) galTagMap.get("multiple"));
		// isBipartite = parseRBoolIsTrue((String)galTagMap.get("bipartite"));
		// bipartite is not a boolean flag, if it is present it is a index
		// number of
		// last node of the class

	}

	/**
	 * creates edges corresponding to elements in mel mel = list(list(inl = 1,
	 * outl = 4, atl = list(na = FALSE)), list(inl = 1, outl = 5, atl = list(na =
	 * FALSE)), list(inl = 2, outl = 3, atl = list(na = FALSE)), list(inl = 2,
	 * outl = 4, atl = list(na = FALSE)), list(inl = 3, outl = 1, atl = list(na =
	 * FALSE)), list(inl = 3, outl = 4, atl = list(na = FALSE)), list(inl = 4,
	 * outl = 1, atl = list(na = FALSE)), list(inl = 4, outl = 2, atl = list(na =
	 * FALSE)), list(inl = 4, outl = 3, atl = list(na = FALSE)), list(inl = 4,
	 * outl = 5, atl = list(na = FALSE)), list(inl = 5, "
	 * 
	 * @param mel
	 * @throws Exception
	 */
	private void parseMel(String mel) throws Exception {

		int edgeCount = 1;

		Iterator edgeIter = parseList(mel.substring(6)).iterator();
		while (edgeIter.hasNext()) {

			String protoEdge = (String) edgeIter.next();
			// need to deal with missing deleted edges when parsing, they will
			// have NULL values
			if (!protoEdge.equals("NULL")) {
				Iterator edgeTokenIter = parseList(protoEdge).iterator();
				int fromId = -1;
				int toId = -1;
				double start = 0.0;
				double end = 1.0;
				double weight = 1.0;
				double width = 1.0;
				while (edgeTokenIter.hasNext()) {
					String edgeToken = ((String) edgeTokenIter.next()).trim();
					// should be list of in node, out nodes, and a list of
					// attributes
					// since at the moment sonia doesn't support multiple head
					// or
					// tail sets, willthrow an error if there are more than one
					if (edgeToken.startsWith("inl")) { // in list
						// parse the id of the starting node
						try {
							fromId = Integer.parseInt(edgeToken.substring(
									edgeToken.indexOf("=") + 1).trim());
						} catch (Exception e) {
							String error = "Unable to parse starting id for edge #"
									+ edgeCount
									+ " : "
									+ edgeToken
									+ " : "
									+ e.getMessage();
							throw new Exception(error);
						}
					} else if (edgeToken.startsWith("outl")) {// out list
					// parse the id of the ending node
						try {
							toId = Integer.parseInt(edgeToken.substring(
									edgeToken.indexOf("=") + 1).trim());
						} catch (Exception e) {
							String error = "Unable to parseending id for edge #"
									+ edgeCount
									+ " : "
									+ edgeToken
									+ " : "
									+ e.getMessage();
							throw new Exception(error);
						}
					} else if (edgeToken.startsWith("atl")) { // attributes
						// TODO: parse attribute of edge from r

					} else {
						String error = "Unrecognized element in master edge list for edge #"
								+ edgeCount + " : " + edgeToken;
						throw new Exception(error);
					}

				}// end parsing of edge

				ArcAttribute arc = new ArcAttribute(start, end, fromId, toId,
						weight, width);
				arcList.add(arc);

				edgeCount++;
			}//end if null loop
		}// end edge loop

	}

	/**
	 * parses the list of vertex attributes and creates nodes with appropriates
	 * values "list(list(na = FALSE), list(na = FALSE), list(na = FALSE),
	 * list(na = FALSE), list(na = FALSE))" *
	 * 
	 * @param val
	 * @throws Exception
	 */
	private void parseVal(String val) throws Exception {

		int nodeId = 1;
		// get the string for each node
		Iterator nodeIter = parseList(val.substring(6)).iterator();
		while (nodeIter.hasNext()) {
			// get the attributes for each node
			String nodeToken = (String) nodeIter.next();
			Iterator nodeAttrIter = parseList(nodeToken).iterator();
			String label = nodeId + "";
			double x = 000;
			double y = 0.0;
			double start = 0.0;
			double end = 1.0;
			String orgiFile = "R export";
			while (nodeAttrIter.hasNext()) {
				// check if is na
				// TODO: what do do if node is missing
				// TODO: figure out how to map rest of attributes...
				String attribute = ((String) nodeAttrIter.next()).trim();
				if (attribute.startsWith(VERTEX_NAME)) {
					label = attribute.substring(attribute.indexOf("=") + 1)
							.trim();
				}
			}

			NodeAttribute node = new NodeAttribute(nodeId, label, x, y, start,
					end, orgiFile);
			nodeList.add(node);
			nodeId++;
		}

	}

	/*
	 * takes an R-style boolean (T TRUE F FALSE) and evaluates it
	 */
	private boolean parseRBoolIsTrue(String bool) {
		return (bool.equals("T") | bool.equals("TRUE"));
	}

	/**
	 * returns a vector with the elments at the first level of the passed list
	 * 
	 * @param list
	 * @return A vector of strings, or null if argument was not a list
	 */
	private Vector parseList(String list) {
		// list(list(na = FALSE), list(na = FALSE), list(na = FALSE), list(na =
		// FALSE))
		Vector listContents = new Vector();
		// check if it really is a list
		if (!list.startsWith("list("))
			return null;
		// strip off the outside () wrappers
		list = list.substring(5, list.length() - 1);
		// items are delimted by ",", but not if inside ()
		// so scan accross the string, breaking at each "," but ignore if open
		int chunkStart = 0;
		int chunkEnd = 0;
		int openParen = 0;

		// find the first (
		// count ( and ) until find matich )
		while (chunkEnd < list.length()) {
			String checkChar = list.substring(chunkEnd, chunkEnd + 1);
			if (checkChar.equals(",")) {
				if (openParen == 0) { // ignore the comma if it is inside
					// parens
					listContents.add(list.substring(chunkStart, chunkEnd)
							.trim());
					chunkStart = chunkEnd + 1; // need to remove the comma?
				}
			} else if (checkChar.equals("(") || checkChar.equals("[")) {
				openParen++;
			} else if (checkChar.equals(")") || checkChar.equals("]")) {
				openParen--;

			}
			chunkEnd++;
		}
		listContents.add(list.substring(chunkStart, chunkEnd).trim());

		return listContents;
	}

	public int getMaxNumNodes() {
		return maxNodes;
	}

	public int getNumNodeEvents() {
		return nodeList.size();
	}

	public int getNumArcEvents() {
		return arcList.size();
	}

	public Vector getNodeList() {
		return nodeList;
	}

	public Vector getArcList() {
		return arcList;
	}

	public String getNetInfo() {
		return "parsed from r";
	}

	public String getParserInfo() {
		return "R network object parser";
	}

	/**
	 * this parser is not configurable
	 */
	public void configureParser(PropertySettings settings) {

	}

}
