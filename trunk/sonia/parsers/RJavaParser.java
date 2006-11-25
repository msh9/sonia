package sonia.parsers;

import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import sonia.ArcAttribute;
import sonia.Interval;
import sonia.NodeAttribute;
import sonia.TimedTagBin;
import sonia.settings.PropertySettings;
import sonia.settings.RParserSettings;

/**
 * This class takes sets of lists exported from R using deparse(). Lists may
 * either be as one long string, or can be passed as a file name ending in
 * ".rdump" containing the strings. The R command to generate the appropriate
 * string is: paste(deparse(network,control=NULL),sep=","); which should yield
 * something like: <BR>
 * <BR>
 * [1] "list(list(inl = 1, outl = 4, atl = list(na = FALSE)), list(inl = 1, outl =
 * 5, atl = list(na = FALSE)), list(inl = 2, outl = 3, atl = list(na = FALSE)),
 * list(inl = 2, outl = 4, atl = list(na = FALSE)), list(inl = 3, outl = 1, atl =
 * list(na = FALSE)), list(inl = 3, outl = 4, atl = list(na = FALSE)), list(inl =
 * 4, outl = 1, atl = list(na = FALSE)), list(inl = 4, outl = 2, atl = list(na =
 * FALSE)), list(inl = 4, outl = 3, atl = list(na = FALSE)), list(inl = 4, outl =
 * 5, atl = list(na = FALSE)), list(inl = 5, " [2] "list(n = 5, mnext = 15,
 * directed = TRUE, hyper = FALSE, loops = FALSE, multiple = FALSE, bipartite =
 * FALSE)" [3] "list(list(na = FALSE), list(na = FALSE), list(na = FALSE),
 * list(na = FALSE), list(na = FALSE))" [4] "list(c(2, 1), c(4, 3), c(6, 5),
 * c(7, 8, 10, 9), c(11, 12, 13, 14))" [5] "list(c(11, 7, 5), c(12, 8), c(3, 13,
 * 9), c(1, 4, 14, 6), c(2, 10))"
 * 
 * @author skyebend
 * 
 */
public class RJavaParser implements Parser {

	private static final String rSuffix = ".rdump";

	private Vector nodeList;

	private Vector arcList;

	private boolean isDirected = true;

	private boolean isHyperGraph = false;

	private boolean hasLoops = false;

	private boolean isMultiplex = false;

	private boolean isRenewal = false;

	// private boolean isBipartite = false;
	private HashMap galTagMap;

	private HashSet dynamicVertAttrMap;

	private int maxNodes = 0;

	private int numEdges = 0;

	private double maxTime = 1.0;

	private double minTime = 0.0;

	private RParserSettings settings;

	/**
	 * 
	 * String may be a list of networks, need to seperate them String is the
	 * output of the paste() command for a network object in R. Will be a set of
	 * lists "mel" : Master Edge List, "gal" : Graph Attribute List, "val" :
	 * Vertex Attribute List, "iel" : In Edge List, "oel", Out Edge List.
	 * 
	 * If netString is a file name ending in ".rdump" it will try to read the
	 * network from the file.
	 */
	public void parseNetwork(String netString) throws IOException {
		// configure defaults
		if (settings == null) {
			settings = new RParserSettings();
			settings.setProperty(RParserSettings.NODE_LABEL, "vertex.names");
			settings.setProperty(RParserSettings.NODE_COLOR, "red");
			settings.setProperty(RParserSettings.NODE_SIZE, "5");
			settings.setProperty(RParserSettings.NODE_SHAPE, "circle");
			settings.setProperty(RParserSettings.NODE_X, "0.0");
			settings.setProperty(RParserSettings.NODE_Y, "0.0");

		}

		nodeList = new Vector();
		arcList = new Vector();

		try {
			// figure out if it is a string or a file
			if (netString.endsWith(rSuffix)) {
				// try to open connection to file
				String fileName = netString;
				try {
					BufferedReader reader = new BufferedReader(new FileReader(
							netString));
					netString = "";
					String line = reader.readLine();
					// check the start of the file for comments and load them
					// comments start with //
					while (line != null) {
						netString += line + " ";
						line = reader.readLine();
					}
					reader.close();
				} catch (IOException e) {
					throw (new Exception(
							"Error reading R dump network from file:"
									+ e.getMessage()));
				}
			}

			Vector mainElements = parseList(netString);
			// check if it is null
			if (mainElements.size() <= 1) {
				throw (new Exception("Unexpected network value:" + netString));
			}

			String mel = (String) mainElements.get(0);
			String gal = (String) mainElements.get(1);
			String val = (String) mainElements.get(2);
			String iel = (String) mainElements.get(3);
			String oel = (String) mainElements.get(4);
			String etl = null; // will be missing if it is not a dynamic
			// network
			if (mainElements.size() > 4) {
				etl = (String) mainElements.get(5);
			}
			parseEdges(mel, etl);
			parseGal(gal);
			parseVal(val);

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
		dynamicVertAttrMap = new HashSet();
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
		isRenewal = parseRBoolIsTrue((String) galTagMap.get("is.renewal"));
		// check if there are dynamic attributes
		if (galTagMap.containsKey("dynam.attr.names")) {
			Iterator keyIter = parseVector(
					(String) galTagMap.get("dynam.attr.names")).iterator();
			while (keyIter.hasNext()) {
				dynamicVertAttrMap.add((stripQuotes((String) keyIter.next()))
						.intern());
			}
		}

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
	private void parseEdges(String mel, String etl) throws Exception {

		int edgeCount = 1;

		Iterator edgeIter = parseList(mel.substring(6)).iterator();
		// etl may be null if it is not a dynamic network
		Iterator edgeTimeIter = parseList(etl.substring(6)).iterator();
		while (edgeIter.hasNext()) {

			String protoEdge = (String) edgeIter.next();
			String protoTime = (String) edgeTimeIter.next();
			// need to deal with missing deleted edges when parsing, they will
			// have NULL values
			if (!protoEdge.equals("NULL") & !protoEdge.equals("")) {
				Iterator edgeTokenIter = parseList(protoEdge).iterator();
				Vector edgeTimes = parseVector(protoTime);
				int fromId = -1;
				int toId = -1;
				double start = 0.0;
				double end = 1.0;
				try {
					start = Double.parseDouble((String) edgeTimes.get(0));
					end = Double.parseDouble((String) edgeTimes.get(1));
					maxTime = Math.max(maxTime, end);
					minTime = Math.min(minTime, start);

				} catch (NumberFormatException nfe) {

					String error = "Unable to parse edge time for edge#"
							+ edgeCount + " :" + nfe.toString();
					// debug
					System.out.println("error");
					throw new Exception(error);
				}
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
							String error = "Unable to parse ending id for edge #"
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
			}// end if null loop
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
		// get the string for each node with all its attributes, times etc
		Iterator nodeIter = parseList(val.substring(6)).iterator();
		while (nodeIter.hasNext()) {
			NodeAttribute node = null;
			// get the attributes for each node
			String nodeToken = (String) nodeIter.next();
			Vector nodeAttrs = parseList(nodeToken);
			Iterator nodeAttrIter = nodeAttrs.iterator();
			// set some default values
			String label = nodeId + "";
			double x = 0.0;
			double y = 0.0;
			// if there are not time values included in the data, use the max an
			// min of edge times
			double start = minTime;
			double end = maxTime;
			String orgiFile = "R export";
			Color nc = parseRColor(settings
					.getProperty(RParserSettings.NODE_COLOR));
			double size = Double.parseDouble(settings
					.getProperty(RParserSettings.NODE_SIZE));
			RectangularShape shape = parseShape(settings
					.getProperty(RParserSettings.NODE_SHAPE));
			// if it is a single element, assume
			while (nodeAttrIter.hasNext()) {
				// check if is na
				// TODO: what do do if node is missing
				String attribute = ((String) nodeAttrIter.next()).trim();
				String attrName = attribute.substring(0,
						attribute.indexOf("=") - 1).trim();
				String attrValue = attribute.substring(
						attribute.indexOf("=") + 1).trim();
				// only do non-dynamic attributes here
				if (!dynamicVertAttrMap.contains(attrName.intern())) {
					if (attrName.startsWith(settings
							.getProperty(RParserSettings.NODE_LABEL))) {
						label = attrValue;
					}
					if (attrName.startsWith(settings
							.getProperty(RParserSettings.NODE_X))) {
						x = Double.parseDouble(attrValue);
					}
					if (attrName.startsWith(settings
							.getProperty(RParserSettings.NODE_Y))) {
						y = Double.parseDouble(attrValue);
						;
					}
					if (attribute.startsWith(settings
							.getProperty(RParserSettings.NODE_COLOR))) {
						nc = parseRColor(attrValue);
						if (nc == null) {
							String error = "Unable to parse attribute as RGB value or  R color name:"
									+ attrValue;
							throw new Exception(error);
						}
					}
				}
			}// end non-dynamic attributes
			//if a node has only dynamic attribute, no vertex is created
			// until first specified attribute

			// now that those are set, now loop again for dynamic attributes
			nodeAttrIter = nodeAttrs.iterator();
			// construct a table of times by attribute values
			TimedTagBin timeMapper = new TimedTagBin();
			while (nodeAttrIter.hasNext()) {
				// check if is na
				// TODO: what do do if node is missing na
				String attribute = ((String) nodeAttrIter.next()).trim();
				String attrName = attribute.substring(0,
						attribute.indexOf("=") - 1).trim();
				String attrValue = attribute.substring(
						attribute.indexOf("=") + 1).trim();
				// if attribute is dynamic, need to create a new object with the
				// apropriate values
				if (dynamicVertAttrMap.contains(attrName.intern())) {
					// c(blue, green, 1, 2)
					// hmm, worrysome, should return somethink like "structure(
					// c(...."
					Vector timeData = parseVector(attrValue);
					int nsteps = timeData.size() / 2;
					// make node for first range..
					start = minTime;
					end = Double.parseDouble(stripQuotes((String) timeData
							.get(nsteps)));
					// add an extra time at the end to fudge..
					timeData.add("" + maxTime);
					// loop over times and values
					for (int t = 0; t < nsteps; t++) {
						attrValue = stripQuotes((String) timeData.get(t));
						start = end;
						end = Double.parseDouble(stripQuotes((String) timeData
								.get(t + nsteps + 1)));
						timeMapper.addAssociation(start, end, attrName,
								attrValue);
					}
				}
			}// end dynamic attributes mapping
			// NOW LOOP to actually CREATE NODE Attributes in time
			Iterator timeIter = timeMapper.getBinTimeIter();
			while (timeIter.hasNext()) {
				Interval interval = (Interval) timeIter.next();
				start = interval.start;
				end = interval.end;
				Iterator keyvalItr = timeMapper.getBin(interval).iterator();
				while (keyvalItr.hasNext()) {
					String[] keyval = (String[]) keyvalItr.next();

					if (keyval[0].startsWith(settings
							.getProperty(RParserSettings.NODE_LABEL))) {
						label = keyval[1];
					}
					if (keyval[0].startsWith(settings
							.getProperty(RParserSettings.NODE_X))) {
						x = Double.parseDouble(keyval[1]);
					}
					if (keyval[0].startsWith(settings
							.getProperty(RParserSettings.NODE_Y))) {
						y = Double.parseDouble(keyval[1]);
						;
					}
					if (keyval[0].startsWith(settings
							.getProperty(RParserSettings.NODE_COLOR))) {
						nc = parseRColor(keyval[1]);
						if (nc == null) {
							String error = "Unable to parse attribute as RGB value R color name:"
									+ keyval[1];
							throw new Exception(error);
						}
					}
					node = new NodeAttribute(nodeId, label, x, y, start, end,
							orgiFile);
					node.setNodeColor(nc);
					node.setNodeShape(shape);
					node.setNodeSize(size);
					nodeList.add(node);
				}
			}// end dynamic attribute creation
			// TODO:generates the last value twice?
			if (node == null) {
				node = new NodeAttribute(nodeId, label, x, y, start, end,
						orgiFile);
				node.setNodeColor(nc);
				node.setNodeShape(shape);
				node.setNodeSize(size);
				nodeList.add(node);
			}
			nodeId++;
		}// end node loop

	}

	private Color parseRColor(String c) throws Exception {
		Color col = null;
		Vector comps = parseVector(c);
		// if it is a three element vector, assume it is an rgb triple
		if (comps.size() == 3) {
			// should make this lightweight....
			col = new Color(Integer.parseInt((String) comps.get(0)), Integer
					.parseInt((String) comps.get(1)), Integer
					.parseInt((String) comps.get(2)));

		}
		// if it is a single elemnt, assume it is an R color name

		if (comps.size() == 1) {
			int index = Arrays.asList(R_COLOR_NAMES).indexOf(comps.get(0));
			if (index >= 0) {
				comps = parseVector(R_COLOR_VALUES[index]);
				col = new Color(Integer.parseInt((String) comps.get(0)),
						Integer.parseInt((String) comps.get(1)), Integer
								.parseInt((String) comps.get(2)));
			}
		}
		return col;

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
		list = list.substring(5, list.lastIndexOf(")"));
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

	private String stripQuotes(String text) {
		return text.replaceAll("\"", "");

	}

	/**
	 * returns a vector with the elments at the first level of the passed string
	 * representation of an R vector
	 * 
	 * @param string
	 *            of an R vector: "c(...)"
	 * @return A vector of strings, or null if argument was not a list
	 */
	private Vector parseVector(String vector) {
		// list(list(na = FALSE), list(na = FALSE), list(na = FALSE), list(na =
		// FALSE))
		Vector vectorContents = new Vector();
		// check if it really is a list
		if (vector.startsWith("c(")) {
			// strip off the outside () wrappers
			vector = vector.substring(2, vector.lastIndexOf(")"));
		}

		// items are delimted by ",", but not if inside ()
		// so scan accross the string, breaking at each "," but ignore if open
		int chunkStart = 0;
		int chunkEnd = 0;
		int openParen = 0;

		// find the first (
		// count ( and ) until find matich )
		while (chunkEnd < vector.length()) {
			String checkChar = vector.substring(chunkEnd, chunkEnd + 1);
			if (checkChar.equals(",")) {
				if (openParen == 0) { // ignore the comma if it is inside
					// parens
					vectorContents.add(vector.substring(chunkStart, chunkEnd)
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
		vectorContents.add(vector.substring(chunkStart, chunkEnd).trim());

		return vectorContents;
	}

	private RectangularShape parseShape(String shapeString) throws IOException {
		RectangularShape shape = null;
		if (shapeString.equalsIgnoreCase("square")
				| shapeString.equalsIgnoreCase("rect")) {
			shape = new Rectangle2D.Double();
		} else if (shapeString.equalsIgnoreCase("circle")
				| shapeString.equalsIgnoreCase("ellipse")) {
			shape = new Ellipse2D.Double();
		} else {
			String error = "Unable to parse shape \"" + shapeString
					+ "currently, shape must be \"square\" or \"circle\"";
			throw (new IOException(error));
		}
		return shape;
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
		this.settings = (RParserSettings) settings;
	}

	private static final String[] R_COLOR_NAMES = new String[] { "white",
			"aliceblue", "antiquewhite", "antiquewhite1", "antiquewhite2",
			"antiquewhite3", "antiquewhite4", "aquamarine", "aquamarine1",
			"aquamarine2", "aquamarine3", "aquamarine4", "azure", "azure1",
			"azure2", "azure3", "azure4", "beige", "bisque", "bisque1",
			"bisque2", "bisque3", "bisque4", "black", "blanchedalmond", "blue",
			"blue1", "blue2", "blue3", "blue4", "blueviolet", "brown",
			"brown1", "brown2", "brown3", "brown4", "burlywood", "burlywood1",
			"burlywood2", "burlywood3", "burlywood4", "cadetblue",
			"cadetblue1", "cadetblue2", "cadetblue3", "cadetblue4",
			"chartreuse", "chartreuse1", "chartreuse2", "chartreuse3",
			"chartreuse4", "chocolate", "chocolate1", "chocolate2",
			"chocolate3", "chocolate4", "coral", "coral1", "coral2", "coral3",
			"coral4", "cornflowerblue", "cornsilk", "cornsilk1", "cornsilk2",
			"cornsilk3", "cornsilk4", "cyan", "cyan1", "cyan2", "cyan3",
			"cyan4", "darkblue", "darkcyan", "darkgoldenrod", "darkgoldenrod1",
			"darkgoldenrod2", "darkgoldenrod3", "darkgoldenrod4", "darkgray",
			"darkgreen", "darkgrey", "darkkhaki", "darkmagenta",
			"darkolivegreen", "darkolivegreen1", "darkolivegreen2",
			"darkolivegreen3", "darkolivegreen4", "darkorange", "darkorange1",
			"darkorange2", "darkorange3", "darkorange4", "darkorchid",
			"darkorchid1", "darkorchid2", "darkorchid3", "darkorchid4",
			"darkred", "darksalmon", "darkseagreen", "darkseagreen1",
			"darkseagreen2", "darkseagreen3", "darkseagreen4", "darkslateblue",
			"darkslategray", "darkslategray1", "darkslategray2",
			"darkslategray3", "darkslategray4", "darkslategrey",
			"darkturquoise", "darkviolet", "deeppink", "deeppink1",
			"deeppink2", "deeppink3", "deeppink4", "deepskyblue",
			"deepskyblue1", "deepskyblue2", "deepskyblue3", "deepskyblue4",
			"dimgray", "dimgrey", "dodgerblue", "dodgerblue1", "dodgerblue2",
			"dodgerblue3", "dodgerblue4", "firebrick", "firebrick1",
			"firebrick2", "firebrick3", "firebrick4", "floralwhite",
			"forestgreen", "gainsboro", "ghostwhite", "gold", "gold1", "gold2",
			"gold3", "gold4", "goldenrod", "goldenrod1", "goldenrod2",
			"goldenrod3", "goldenrod4", "gray", "gray0", "gray1", "gray2",
			"gray3", "gray4", "gray5", "gray6", "gray7", "gray8", "gray9",
			"gray10", "gray11", "gray12", "gray13", "gray14", "gray15",
			"gray16", "gray17", "gray18", "gray19", "gray20", "gray21",
			"gray22", "gray23", "gray24", "gray25", "gray26", "gray27",
			"gray28", "gray29", "gray30", "gray31", "gray32", "gray33",
			"gray34", "gray35", "gray36", "gray37", "gray38", "gray39",
			"gray40", "gray41", "gray42", "gray43", "gray44", "gray45",
			"gray46", "gray47", "gray48", "gray49", "gray50", "gray51",
			"gray52", "gray53", "gray54", "gray55", "gray56", "gray57",
			"gray58", "gray59", "gray60", "gray61", "gray62", "gray63",
			"gray64", "gray65", "gray66", "gray67", "gray68", "gray69",
			"gray70", "gray71", "gray72", "gray73", "gray74", "gray75",
			"gray76", "gray77", "gray78", "gray79", "gray80", "gray81",
			"gray82", "gray83", "gray84", "gray85", "gray86", "gray87",
			"gray88", "gray89", "gray90", "gray91", "gray92", "gray93",
			"gray94", "gray95", "gray96", "gray97", "gray98", "gray99",
			"gray100", "green", "green1", "green2", "green3", "green4",
			"greenyellow", "grey", "grey0", "grey1", "grey2", "grey3", "grey4",
			"grey5", "grey6", "grey7", "grey8", "grey9", "grey10", "grey11",
			"grey12", "grey13", "grey14", "grey15", "grey16", "grey17",
			"grey18", "grey19", "grey20", "grey21", "grey22", "grey23",
			"grey24", "grey25", "grey26", "grey27", "grey28", "grey29",
			"grey30", "grey31", "grey32", "grey33", "grey34", "grey35",
			"grey36", "grey37", "grey38", "grey39", "grey40", "grey41",
			"grey42", "grey43", "grey44", "grey45", "grey46", "grey47",
			"grey48", "grey49", "grey50", "grey51", "grey52", "grey53",
			"grey54", "grey55", "grey56", "grey57", "grey58", "grey59",
			"grey60", "grey61", "grey62", "grey63", "grey64", "grey65",
			"grey66", "grey67", "grey68", "grey69", "grey70", "grey71",
			"grey72", "grey73", "grey74", "grey75", "grey76", "grey77",
			"grey78", "grey79", "grey80", "grey81", "grey82", "grey83",
			"grey84", "grey85", "grey86", "grey87", "grey88", "grey89",
			"grey90", "grey91", "grey92", "grey93", "grey94", "grey95",
			"grey96", "grey97", "grey98", "grey99", "grey100", "honeydew",
			"honeydew1", "honeydew2", "honeydew3", "honeydew4", "hotpink",
			"hotpink1", "hotpink2", "hotpink3", "hotpink4", "indianred",
			"indianred1", "indianred2", "indianred3", "indianred4", "ivory",
			"ivory1", "ivory2", "ivory3", "ivory4", "khaki", "khaki1",
			"khaki2", "khaki3", "khaki4", "lavender", "lavenderblush",
			"lavenderblush1", "lavenderblush2", "lavenderblush3",
			"lavenderblush4", "lawngreen", "lemonchiffon", "lemonchiffon1",
			"lemonchiffon2", "lemonchiffon3", "lemonchiffon4", "lightblue",
			"lightblue1", "lightblue2", "lightblue3", "lightblue4",
			"lightcoral", "lightcyan", "lightcyan1", "lightcyan2",
			"lightcyan3", "lightcyan4", "lightgoldenrod", "lightgoldenrod1",
			"lightgoldenrod2", "lightgoldenrod3", "lightgoldenrod4",
			"lightgoldenrodyellow", "lightgray", "lightgreen", "lightgrey",
			"lightpink", "lightpink1", "lightpink2", "lightpink3",
			"lightpink4", "lightsalmon", "lightsalmon1", "lightsalmon2",
			"lightsalmon3", "lightsalmon4", "lightseagreen", "lightskyblue",
			"lightskyblue1", "lightskyblue2", "lightskyblue3", "lightskyblue4",
			"lightslateblue", "lightslategray", "lightslategrey",
			"lightsteelblue", "lightsteelblue1", "lightsteelblue2",
			"lightsteelblue3", "lightsteelblue4", "lightyellow",
			"lightyellow1", "lightyellow2", "lightyellow3", "lightyellow4",
			"limegreen", "linen", "magenta", "magenta1", "magenta2",
			"magenta3", "magenta4", "maroon", "maroon1", "maroon2", "maroon3",
			"maroon4", "mediumaquamarine", "mediumblue", "mediumorchid",
			"mediumorchid1", "mediumorchid2", "mediumorchid3", "mediumorchid4",
			"mediumpurple", "mediumpurple1", "mediumpurple2", "mediumpurple3",
			"mediumpurple4", "mediumseagreen", "mediumslateblue",
			"mediumspringgreen", "mediumturquoise", "mediumvioletred",
			"midnightblue", "mintcream", "mistyrose", "mistyrose1",
			"mistyrose2", "mistyrose3", "mistyrose4", "moccasin",
			"navajowhite", "navajowhite1", "navajowhite2", "navajowhite3",
			"navajowhite4", "navy", "navyblue", "oldlace", "olivedrab",
			"olivedrab1", "olivedrab2", "olivedrab3", "olivedrab4", "orange",
			"orange1", "orange2", "orange3", "orange4", "orangered",
			"orangered1", "orangered2", "orangered3", "orangered4", "orchid",
			"orchid1", "orchid2", "orchid3", "orchid4", "palegoldenrod",
			"palegreen", "palegreen1", "palegreen2", "palegreen3",
			"palegreen4", "paleturquoise", "paleturquoise1", "paleturquoise2",
			"paleturquoise3", "paleturquoise4", "palevioletred",
			"palevioletred1", "palevioletred2", "palevioletred3",
			"palevioletred4", "papayawhip", "peachpuff", "peachpuff1",
			"peachpuff2", "peachpuff3", "peachpuff4", "peru", "pink", "pink1",
			"pink2", "pink3", "pink4", "plum", "plum1", "plum2", "plum3",
			"plum4", "powderblue", "purple", "purple1", "purple2", "purple3",
			"purple4", "red", "red1", "red2", "red3", "red4", "rosybrown",
			"rosybrown1", "rosybrown2", "rosybrown3", "rosybrown4",
			"royalblue", "royalblue1", "royalblue2", "royalblue3",
			"royalblue4", "saddlebrown", "salmon", "salmon1", "salmon2",
			"salmon3", "salmon4", "sandybrown", "seagreen", "seagreen1",
			"seagreen2", "seagreen3", "seagreen4", "seashell", "seashell1",
			"seashell2", "seashell3", "seashell4", "sienna", "sienna1",
			"sienna2", "sienna3", "sienna4", "skyblue", "skyblue1", "skyblue2",
			"skyblue3", "skyblue4", "slateblue", "slateblue1", "slateblue2",
			"slateblue3", "slateblue4", "slategray", "slategray1",
			"slategray2", "slategray3", "slategray4", "slategrey", "snow",
			"snow1", "snow2", "snow3", "snow4", "springgreen", "springgreen1",
			"springgreen2", "springgreen3", "springgreen4", "steelblue",
			"steelblue1", "steelblue2", "steelblue3", "steelblue4", "tan",
			"tan1", "tan2", "tan3", "tan4", "thistle", "thistle1", "thistle2",
			"thistle3", "thistle4", "tomato", "tomato1", "tomato2", "tomato3",
			"tomato4", "turquoise", "turquoise1", "turquoise2", "turquoise3",
			"turquoise4", "violet", "violetred", "violetred1", "violetred2",
			"violetred3", "violetred4", "wheat", "wheat1", "wheat2", "wheat3",
			"wheat4", "whitesmoke", "yellow", "yellow1", "yellow2", "yellow3",
			"yellow4", "yellowgreen" };

	private static final String[] R_COLOR_VALUES = new String[] {
			"255,255,255", "240,248,255", "250,235,215", "255,239,219",
			"238,223,204", "205,192,176", "139,131,120", "127,255,212",
			"127,255,212", "118,238,198", "102,205,170", "69,139,116",
			"240,255,255", "240,255,255", "224,238,238", "193,205,205",
			"131,139,139", "245,245,220", "255,228,196", "255,228,196",
			"238,213,183", "205,183,158", "139,125,107", "0,0,0",
			"255,235,205", "0,0,255", "0,0,255", "0,0,238", "0,0,205",
			"0,0,139", "138,43,226", "165,42,42", "255,64,64", "238,59,59",
			"205,51,51", "139,35,35", "222,184,135", "255,211,155",
			"238,197,145", "205,170,125", "139,115,85", "95,158,160",
			"152,245,255", "142,229,238", "122,197,205", "83,134,139",
			"127,255,0", "127,255,0", "118,238,0", "102,205,0", "69,139,0",
			"210,105,30", "255,127,36", "238,118,33", "205,102,29",
			"139,69,19", "255,127,80", "255,114,86", "238,106,80", "205,91,69",
			"139,62,47", "100,149,237", "255,248,220", "255,248,220",
			"238,232,205", "205,200,177", "139,136,120", "0,255,255",
			"0,255,255", "0,238,238", "0,205,205", "0,139,139", "0,0,139",
			"0,139,139", "184,134,11", "255,185,15", "238,173,14",
			"205,149,12", "139,101,8", "169,169,169", "0,100,0", "169,169,169",
			"189,183,107", "139,0,139", "85,107,47", "202,255,112",
			"188,238,104", "162,205,90", "110,139,61", "255,140,0",
			"255,127,0", "238,118,0", "205,102,0", "139,69,0", "153,50,204",
			"191,62,255", "178,58,238", "154,50,205", "104,34,139", "139,0,0",
			"233,150,122", "143,188,143", "193,255,193", "180,238,180",
			"155,205,155", "105,139,105", "72,61,139", "47,79,79",
			"151,255,255", "141,238,238", "121,205,205", "82,139,139",
			"47,79,79", "0,206,209", "148,0,211", "255,20,147", "255,20,147",
			"238,18,137", "205,16,118", "139,10,80", "0,191,255", "0,191,255",
			"0,178,238", "0,154,205", "0,104,139", "105,105,105",
			"105,105,105", "30,144,255", "30,144,255", "28,134,238",
			"24,116,205", "16,78,139", "178,34,34", "255,48,48", "238,44,44",
			"205,38,38", "139,26,26", "255,250,240", "34,139,34",
			"220,220,220", "248,248,255", "255,215,0", "255,215,0",
			"238,201,0", "205,173,0", "139,117,0", "218,165,32", "255,193,37",
			"238,180,34", "205,155,29", "139,105,20", "190,190,190", "0,0,0",
			"3,3,3", "5,5,5", "8,8,8", "10,10,10", "13,13,13", "15,15,15",
			"18,18,18", "20,20,20", "23,23,23", "26,26,26", "28,28,28",
			"31,31,31", "33,33,33", "36,36,36", "38,38,38", "41,41,41",
			"43,43,43", "46,46,46", "48,48,48", "51,51,51", "54,54,54",
			"56,56,56", "59,59,59", "61,61,61", "64,64,64", "66,66,66",
			"69,69,69", "71,71,71", "74,74,74", "77,77,77", "79,79,79",
			"82,82,82", "84,84,84", "87,87,87", "89,89,89", "92,92,92",
			"94,94,94", "97,97,97", "99,99,99", "102,102,102", "105,105,105",
			"107,107,107", "110,110,110", "112,112,112", "115,115,115",
			"117,117,117", "120,120,120", "122,122,122", "125,125,125",
			"127,127,127", "130,130,130", "133,133,133", "135,135,135",
			"138,138,138", "140,140,140", "143,143,143", "145,145,145",
			"148,148,148", "150,150,150", "153,153,153", "156,156,156",
			"158,158,158", "161,161,161", "163,163,163", "166,166,166",
			"168,168,168", "171,171,171", "173,173,173", "176,176,176",
			"179,179,179", "181,181,181", "184,184,184", "186,186,186",
			"189,189,189", "191,191,191", "194,194,194", "196,196,196",
			"199,199,199", "201,201,201", "204,204,204", "207,207,207",
			"209,209,209", "212,212,212", "214,214,214", "217,217,217",
			"219,219,219", "222,222,222", "224,224,224", "227,227,227",
			"229,229,229", "232,232,232", "235,235,235", "237,237,237",
			"240,240,240", "242,242,242", "245,245,245", "247,247,247",
			"250,250,250", "252,252,252", "255,255,255", "0,255,0", "0,255,0",
			"0,238,0", "0,205,0", "0,139,0", "173,255,47", "190,190,190",
			"0,0,0", "3,3,3", "5,5,5", "8,8,8", "10,10,10", "13,13,13",
			"15,15,15", "18,18,18", "20,20,20", "23,23,23", "26,26,26",
			"28,28,28", "31,31,31", "33,33,33", "36,36,36", "38,38,38",
			"41,41,41", "43,43,43", "46,46,46", "48,48,48", "51,51,51",
			"54,54,54", "56,56,56", "59,59,59", "61,61,61", "64,64,64",
			"66,66,66", "69,69,69", "71,71,71", "74,74,74", "77,77,77",
			"79,79,79", "82,82,82", "84,84,84", "87,87,87", "89,89,89",
			"92,92,92", "94,94,94", "97,97,97", "99,99,99", "102,102,102",
			"105,105,105", "107,107,107", "110,110,110", "112,112,112",
			"115,115,115", "117,117,117", "120,120,120", "122,122,122",
			"125,125,125", "127,127,127", "130,130,130", "133,133,133",
			"135,135,135", "138,138,138", "140,140,140", "143,143,143",
			"145,145,145", "148,148,148", "150,150,150", "153,153,153",
			"156,156,156", "158,158,158", "161,161,161", "163,163,163",
			"166,166,166", "168,168,168", "171,171,171", "173,173,173",
			"176,176,176", "179,179,179", "181,181,181", "184,184,184",
			"186,186,186", "189,189,189", "191,191,191", "194,194,194",
			"196,196,196", "199,199,199", "201,201,201", "204,204,204",
			"207,207,207", "209,209,209", "212,212,212", "214,214,214",
			"217,217,217", "219,219,219", "222,222,222", "224,224,224",
			"227,227,227", "229,229,229", "232,232,232", "235,235,235",
			"237,237,237", "240,240,240", "242,242,242", "245,245,245",
			"247,247,247", "250,250,250", "252,252,252", "255,255,255",
			"240,255,240", "240,255,240", "224,238,224", "193,205,193",
			"131,139,131", "255,105,180", "255,110,180", "238,106,167",
			"205,96,144", "139,58,98", "205,92,92", "255,106,106", "238,99,99",
			"205,85,85", "139,58,58", "255,255,240", "255,255,240",
			"238,238,224", "205,205,193", "139,139,131", "240,230,140",
			"255,246,143", "238,230,133", "205,198,115", "139,134,78",
			"230,230,250", "255,240,245", "255,240,245", "238,224,229",
			"205,193,197", "139,131,134", "124,252,0", "255,250,205",
			"255,250,205", "238,233,191", "205,201,165", "139,137,112",
			"173,216,230", "191,239,255", "178,223,238", "154,192,205",
			"104,131,139", "240,128,128", "224,255,255", "224,255,255",
			"209,238,238", "180,205,205", "122,139,139", "238,221,130",
			"255,236,139", "238,220,130", "205,190,112", "139,129,76",
			"250,250,210", "211,211,211", "144,238,144", "211,211,211",
			"255,182,193", "255,174,185", "238,162,173", "205,140,149",
			"139,95,101", "255,160,122", "255,160,122", "238,149,114",
			"205,129,98", "139,87,66", "32,178,170", "135,206,250",
			"176,226,255", "164,211,238", "141,182,205", "96,123,139",
			"132,112,255", "119,136,153", "119,136,153", "176,196,222",
			"202,225,255", "188,210,238", "162,181,205", "110,123,139",
			"255,255,224", "255,255,224", "238,238,209", "205,205,180",
			"139,139,122", "50,205,50", "250,240,230", "255,0,255",
			"255,0,255", "238,0,238", "205,0,205", "139,0,139", "176,48,96",
			"255,52,179", "238,48,167", "205,41,144", "139,28,98",
			"102,205,170", "0,0,205", "186,85,211", "224,102,255",
			"209,95,238", "180,82,205", "122,55,139", "147,112,219",
			"171,130,255", "159,121,238", "137,104,205", "93,71,139",
			"60,179,113", "123,104,238", "0,250,154", "72,209,204",
			"199,21,133", "25,25,112", "245,255,250", "255,228,225",
			"255,228,225", "238,213,210", "205,183,181", "139,125,123",
			"255,228,181", "255,222,173", "255,222,173", "238,207,161",
			"205,179,139", "139,121,94", "0,0,128", "0,0,128", "253,245,230",
			"107,142,35", "192,255,62", "179,238,58", "154,205,50",
			"105,139,34", "255,165,0", "255,165,0", "238,154,0", "205,133,0",
			"139,90,0", "255,69,0", "255,69,0", "238,64,0", "205,55,0",
			"139,37,0", "218,112,214", "255,131,250", "238,122,233",
			"205,105,201", "139,71,137", "238,232,170", "152,251,152",
			"154,255,154", "144,238,144", "124,205,124", "84,139,84",
			"175,238,238", "187,255,255", "174,238,238", "150,205,205",
			"102,139,139", "219,112,147", "255,130,171", "238,121,159",
			"205,104,137", "139,71,93", "255,239,213", "255,218,185",
			"255,218,185", "238,203,173", "205,175,149", "139,119,101",
			"205,133,63", "255,192,203", "255,181,197", "238,169,184",
			"205,145,158", "139,99,108", "221,160,221", "255,187,255",
			"238,174,238", "205,150,205", "139,102,139", "176,224,230",
			"160,32,240", "155,48,255", "145,44,238", "125,38,205",
			"85,26,139", "255,0,0", "255,0,0", "238,0,0", "205,0,0", "139,0,0",
			"188,143,143", "255,193,193", "238,180,180", "205,155,155",
			"139,105,105", "65,105,225", "72,118,255", "67,110,238",
			"58,95,205", "39,64,139", "139,69,19", "250,128,114",
			"255,140,105", "238,130,98", "205,112,84", "139,76,57",
			"244,164,96", "46,139,87", "84,255,159", "78,238,148",
			"67,205,128", "46,139,87", "255,245,238", "255,245,238",
			"238,229,222", "205,197,191", "139,134,130", "160,82,45",
			"255,130,71", "238,121,66", "205,104,57", "139,71,38",
			"135,206,235", "135,206,255", "126,192,238", "108,166,205",
			"74,112,139", "106,90,205", "131,111,255", "122,103,238",
			"105,89,205", "71,60,139", "112,128,144", "198,226,255",
			"185,211,238", "159,182,205", "108,123,139", "112,128,144",
			"255,250,250", "255,250,250", "238,233,233", "205,201,201",
			"139,137,137", "0,255,127", "0,255,127", "0,238,118", "0,205,102",
			"0,139,69", "70,130,180", "99,184,255", "92,172,238", "79,148,205",
			"54,100,139", "210,180,140", "255,165,79", "238,154,73",
			"205,133,63", "139,90,43", "216,191,216", "255,225,255",
			"238,210,238", "205,181,205", "139,123,139", "255,99,71",
			"255,99,71", "238,92,66", "205,79,57", "139,54,38", "64,224,208",
			"0,245,255", "0,229,238", "0,197,205", "0,134,139", "238,130,238",
			"208,32,144", "255,62,150", "238,58,140", "205,50,120",
			"139,34,82", "245,222,179", "255,231,186", "238,216,174",
			"205,186,150", "139,126,102", "245,245,245", "255,255,0",
			"255,255,0", "238,238,0", "205,205,0", "139,139,0", "154,205,50" };

}
