package sonia.parsers;

import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.Color;
import java.awt.geom.*;

import sonia.ArcAttribute;
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
 * The idea for the .son parser is that it reads an edgelist format, but one
 * that is "column based" rather than "token based" like the .net parser. This
 * means that is is much easier to write scripts or translate spreadsheets of
 * attribute data into the .son format, but it will be more combersome than .net
 * for hand coding. Sonia will eventualy be able to save files in the .son
 * format.<BR>
 * <BR>
 * 
 * The input file works as follows:<BR>
 * 
 * The first several lines of the file can contain comments if they begin with //
 * and these comments will show up in Sonia's log file. Next will be a row of
 * TAB-DELIMITED column headings indicating the node attribute catagories. The
 * first item in the row must be "NodeId", as this is how the parser finds the
 * row. Also, the data must contain a column for NodeID, as this is the nodes'
 * unique identifier and is used to reference "to" and "from" nodes in the arc
 * data. NodeIds must form a complete set of integers. However, it is possible
 * to use "AlphaId" in which case node ids and arc to and from records are
 * strings. If the entries in a node row are cannot be parsed to the type
 * spcified by the column heading, an error will be thrown. The remaining
 * headings can be in any order, and can be omitted, in which case a default
 * value is used. However, IF A COLUMN HEADING FOR AN ATTRIBUTE IS INCLUDED,
 * EVERY RECORD MUST CONTAIN AN ENTRY FOR THAT ATTRIBUTE. Blanks are not
 * allowed, as they will cause the columns to misalign. <BR>
 * <BR>
 * the folowing is a list of valid node column headings and values:
 * <P>
 * <B>NodeId</B> - must be an integer. values can be used more than once (to
 * specify changes in a node's attributes over time) BUT MUST FORM A CONTINOUS
 * SEQUENCE. (if you try to leave out numbers it will throw an error, as this
 * would mess up the matrix refrences.
 * </P>
 * <P>
 * <B>AlphaId</B> - any string. use instead of NodeId for string ids. Strings
 * are mapped to ids in the order they are parsed, one for each unique string.
 * </P>
 * <P>
 * <B>NodeLabel</B> - text to be displayed as nodes's label.
 * </P>
 * <P>
 * <B>X<</B> - realvalued number expressing nodes position in pixels, origin
 * is at the upper left corner of the window.
 * </P>
 * <P>
 * <B>Y</B> - ditto x
 * </P>
 * <P>
 * <B>ColorName</B> - text spcifying a color for the node, one of:<BR>
 * <tt>
 *     <BR>Black
 *     <BR>DarkGray
 *    <BR> LightGray
 *    <BR> White
 *    <BR> Cyan
 *    <BR> Green
 *    <BR> Magenta
 *    <BR> Orange
 *   <BR>  Pink
 *   <BR>  Red
 *   <BR>  Yellow
 *   <BR>  Blue
 *<BR></tt> Alternatively, you can specify the color using a "Red-Green-Blue" color
 * model by using the column headings:
 * </P>
 * <P>
 * <B>RedRGB</B> - real number between 0 and 1 specifying the red component
 * </P>
 * <P>
 * <B>GreenRGB</B> - "
 * </P>
 * <P>
 * <B>BlueRGB</B> - "
 * </P>
 * <P>
 * 
 * <B>NodeShape</B> - text specifying the shape for the node, current can only
 * be "ellipse" or "rect"
 * </P>
 * <P>
 * <B>NodeSize</B> - positive real number specifying the size of the node in
 * pixels
 * </P>
 * <P>
 * <B>LabelColor</B> - the color for the label text, must be a color name
 * </P>
 * <P>
 * <B>BorderColor</B> - the color for the node's border, must be a color name
 * </P>
 * <P>
 * <B>BorderWidth</B> - real number for the width of the node's border
 * </P>
 * <P>
 * <B>StartTime</B> - real value specifying the start time for the node
 * </P>
 * <P>
 * <B>EndTime</B> - real value specifying the end time for the node.
 * </P>
 * <P>
 * After the node records should be a row of column headings for the arc
 * records. This line must begin with "FromId", as this is how the parser knows
 * that the end of the node records has been reached. The rest of the entries
 * for the arc column headings can be in any order.
 * </P>
 * <P>
 * <B>FromId</B> - integer indicating the source node MUST MATCH WITH A NODE ID
 * </P>
 * <P>
 * <B>ToId</B> - integer indicating the destination node
 * </P>
 * <P>
 * <B>Weight</B> - real value indicating the strength of the relation
 * </P>
 * <P>
 * <B>Width</B> - real value indicating how wide to draw the arrow
 * </P>
 * <P>
 * <B>ColorName</B>, RedRGB, GreenRGB, BlueRGB - see node colors
 * </P>
 * <P>
 * <B>StartTime</B> - real value indicating the arc's start
 * </P>
 * <P>
 * <B>EndTime</B> - real value indicating the arc's termination
 * </P>
 * 
 * Example: <tt>
 * //optional comments proceeded by double backslash
 *  <table border=0>
 * 	<tr>
 * 		<td>NodeId</td> <td>Label</td> <td>StartTime</td> <td>EndTime</td> <td>NodeSize</td> <td>NodeShape</td> <td>ColorName</td> <td>BorderWidth</td> <td>BorderColor</td>
 * 	</tr>
 * 	<tr>
 * 		<td>1</td> <td>129473</td> <td>0.0</td> <td>42.0</td> <td>5.0</td> <td>ellipse</td> <td>lightGray</td> <td>1.5</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>2</td> <td>129047</td> <td>0.0</td> <td>42.0</td> <td>5.0</td> <td>rect</td> <td>gray</td> <td>1.5</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>3</td> <td>132996</td> <td>0.0</td> <td>42.0</td> <td>5.0</td> <td>ellipse</td> <td>lightGray</td> <td>1.5</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>4</td> <td>145242</td> <td>0.0</td> <td>42.0</td> <td>5.0</td> <td>ellipse</td> <td>gray</td> <td>1.5</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>5</td> <td>127535</td> <td>0.0</td> <td>42.0</td> <td>5.0</td> <td>ellipse</td> <td>lightGray</td> <td>1.5</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>6</td> <td>127319</td> <td>0.0</td> <td>42.0</td> <td>5.0</td> <td>rect</td> <td>lightGray</td> <td>1.5</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>7</td> <td>129801</td> <td>0.0</td> <td>42.0</td> <td>5.0</td> <td>ellipse</td> <td>darkGray</td> <td>1.5</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>8</td> <td>104456</td> <td>0.0</td> <td>42.0</td> <td>5.0</td> <td>ellipse</td> <td>lightGray</td> <td>1.5</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>FromId</td> <td>ToId</td> <td>StartTime</td> <td>EndTime</td> <td>ArcWeight</td> <td>ArcWidth</td> <td>ColorName</td>
 * 	</tr>
 * 	<tr>
 * 		<td>24</td> <td>1</td> <td>0.135</td> <td>0.135</td> <td>0.2</td> <td>1.6</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>24</td> <td>2</td> <td>0.135</td> <td>0.135</td> <td>0.2</td> <td>1.6</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>24</td> <td>4</td> <td>0.135</td> <td>0.135</td> <td>0.2</td> <td>1.6</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>24</td> <td>3</td> <td>0.135</td> <td>0.135</td> <td>0.2</td> <td>1.6</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>24</td> <td>6</td> <td>0.135</td> <td>0.135</td> <td>0.2</td> <td>1.6</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>24</td> <td>5</td> <td>0.135</td> <td>0.135</td> <td>0.2</td> <td>1.6</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>24</td> <td>7</td> <td>0.135</td> <td>0.135</td> <td>0.2</td> <td>1.6</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>24</td> <td>9</td> <td>0.135</td> <td>0.135</td> <td>0.2</td> <td>1.6</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>24</td> <td>8</td> <td>0.135</td> <td>0.135</td> <td>0.2</td> <td>1.6</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>24</td> <td>11</td> <td>0.135</td> <td>0.135</td> <td>0.2</td> <td>1.6</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>24</td> <td>10</td> <td>0.135</td> <td>0.135</td> <td>0.2</td> <td>1.6</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>24</td> <td>12</td> <td>0.135</td> <td>0.135</td> <td>0.2</td> <td>1.6</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>16</td> <td>25</td> <td>3.514</td> <td>3.514</td> <td>0.2</td> <td>1.6</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>16</td> <td>26</td> <td>3.514</td> <td>3.514</td> <td>0.2</td> <td>1.6</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>16</td> <td>27</td> <td>3.514</td> <td>3.514</td> <td>0.2</td> <td>1.6</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>24</td> <td>1</td> <td>3.649</td> <td>3.649</td> <td>0.2</td> <td>1.6</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>24</td> <td>2</td> <td>3.649</td> <td>3.649</td> <td>0.2</td> <td>1.6</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>24</td> <td>4</td> <td>3.649</td> <td>3.649</td> <td>0.2</td> <td>1.6</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>24</td> <td>3</td> <td>3.649</td> <td>3.649</td> <td>0.2</td> <td>1.6</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>24</td> <td>6</td> <td>3.649</td> <td>3.649</td> <td>0.2</td> <td>1.6</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>24</td> <td>5</td> <td>3.649</td> <td>3.649</td> <td>0.2</td> <td>1.6</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>24</td> <td>7</td> <td>3.649</td> <td>3.649</td> <td>0.2</td> <td>1.6</td> <td>black</td>
 * 	</tr>
 * 	<tr>
 * 		<td>24</td> <td>9</td> <td>3.649</td> <td>3.649</td> <td>0.2</td> <td>1.6</td> <td>black</td>
 * 	</tr>
 * </table>
 *
 * <\tt>
 */
public class DotSonParser implements Parser {

	// vars for returned vals
	private LineNumberReader reader;

	private Vector nodeList;

	private Vector arcList;

	// private boolean combineSameNames = true;
	private boolean parseCoords = true;

	// private boolean integerTime = false;
	private HashSet idSet;

	private HashMap nodeHeaderMap; // holds associateion between column labels
									// and index

	private HashMap arcHeaderMap; // holds associateion between column labels
									// and index

	private HashMap alphaIdMap; // holds association between non-numeric ids and
								// numeric ids

	private ArrayList unknownHeaders; // holds list of unrecognized column
										// headers

	private boolean alphaId = false; // true if alphaIds are being used

	private int currentLineNum = 0;

	private String originalFile; // path and name of the file it was loaded
									// from

	private String infoString = "";

	// defualt colum name mappings
	private DotSonColumnMap colMap = null;

	private boolean wasMappingSpecified = false;

	// control vars
	private boolean startAsEnd = false;

	/**
	 * Parser for .son files, list based network format which allows column
	 * variable assignment and a bit more flexiblity than the pajek .net format
	 */
	public DotSonParser() {
	}

	/**
	 * attempts to open a connection to the passed file and path, read in text
	 * and parse it to create a set of nodes and arcs. Lines beginning with "//"
	 * in the start of the file are read as comments. Then looks for line
	 * beginning with "NodeId" and reads it as a header giving the column names
	 * for the node attributes. Parses remaining nodes using "parseNodeRow"
	 * until reaching a line beginning with "FromID". Checks that node Ids form
	 * a complete sequence. Parses this line as the column headings for the arc
	 * records. Parses remaining lines as arc records until the end of the file.
	 * Closes connection to file.
	 * 
	 * @param fileAndPath
	 *            a string giving the complete path and filename
	 * @throws IOEXception
	 *             if problems locating files or parsing
	 */
	public void parseNetwork(String fileAndPath) throws IOException {

		// currentLineNum = 0;

		originalFile = fileAndPath;
		int numNodes = 0;
		int numArcs = 0;
		nodeList = new Vector();
		arcList = new Vector();
		idSet = new HashSet();
		
		if (colMap == null) {
			colMap = new DotSonColumnMap(); // use default colmn mappingings
		} else {
			wasMappingSpecified = true;
		}
		
		// open connection to file
		reader = new LineNumberReader(new FileReader(fileAndPath));

		String line = reader.readLine();
		// check the start of the file for comments and load them
		// comments start with //
		while (line.startsWith("//")) {
			infoString += line + "\n";
			line = reader.readLine();
		}

		// first uncommented line should be the node column headings starting
		// with "NODEID"
		if (line.startsWith(colMap.getProperty(DotSonColumnMap.NODE_ID))
				| line.startsWith(colMap.getProperty(DotSonColumnMap.ALPHA_ID))) {
			parseNodeColHeader(line);
			// Only show the column mapping dialog if there are unknown headers
			// and nothing was included on the command ine
			//TODO: what if the users wants to edit the colum mappings?
			if ((unknownHeaders.size() > 0) & !wasMappingSpecified) {
				// debug
				System.out.println("unknown column headers:" + unknownHeaders);
				AttributeMapperDialog assignAtter = new AttributeMapperDialog(
						colMap, unknownHeaders);

			}
			line = reader.readLine();
		} else // otherwise, throw error
		{
			String error = "Unable to locate node header on line "
					+ reader.getLineNumber()
					+ "\n"
					+ " Node column headings must begin with \"NodeId\" for numeric ids or \"AlphaId\" for alphanumeric ids: "
					+ line;
			throw (new IOException(error));
		}
		// check for eof?
		// parse nodes until encouter line startng with "FROMID" indicating arc
		// header
		while (line != null) {
			// check if we've gotten to arcs yet
			if (line.startsWith(colMap.getProperty(DotSonColumnMap.FROM_ID))
					| line.startsWith(colMap.getProperty(DotSonColumnMap.TO_ID))) {
				break;
			} else if (line.startsWith("//")) {
				infoString += line + "\n";
			} else {
				parseNodeRow(line);
			}
			line = reader.readLine();
		}
		// NEED TO MAKE SURE ALL NODES ARE PRESENT AND ACCOUNTED FOR
		// (no gaps in ID range which will mess up the matrix)
		checkForMissingNodes();

		// parse arc column headings, if there are any
		// WHAT ABOUT TRALING \n or \r ?
		if (line != null) {
			if (line.startsWith(colMap.getProperty(DotSonColumnMap.FROM_ID))) {
				parseArcColHeader(line);
				line = reader.readLine();
			} else // otherwise, throw error
			{
				String error = "Unable to locate arc header on line "
						+ reader.getLineNumber() + "\n"
						+ " Arc column headings must begin with \"FromId\"";
				throw (new IOException(error));
			}
		}

		// parse arcs until EOF or
		while (line != null) {
			if (line.startsWith("//")) {
				infoString += line + "\n";
				line = reader.readLine();
			} else {
				parseArcRow(line);
				line = reader.readLine();
			}
		}
		// include edges?

		// cleanup
		reader.close();
	}

	/**
	 * parses the tab delimited node column header, stores the tokens in the
	 * node header map so parsing methods know which coumn of data to read from.
	 * 
	 * @param nodeHeader
	 *            the string to parse column names from
	 */
	private void parseNodeColHeader(String header) throws IOException {
		// make a list of all the unrecognized column headings
		unknownHeaders = new ArrayList();
		Collection knownHeadings = colMap.values();
		// tokenize the string
		StringTokenizer headerTokens = new StringTokenizer(header, "\t");
		int numCols = headerTokens.countTokens();
		nodeHeaderMap = new HashMap();
		// copy cols into map
		for (int n = 0; n < numCols; n++) {
			String colName = headerTokens.nextToken();
			// make a list of the unrecognized headings
			if (!knownHeadings.contains(colName)) {
				unknownHeaders.add(colName);
			}
			// check for duplicates
			if (nodeHeaderMap.containsKey(colName)) {
				String error = "Node column header cannot contain multiple entries for "
						+ colName;
				throw (new IOException(error));
			} else {
				nodeHeaderMap.put(colName, new Integer(n));
			}
		}
		// check if numeric or alphanumeric ids are used
		if (nodeHeaderMap.containsKey(colMap.getProperty(DotSonColumnMap.ALPHA_ID))
				& nodeHeaderMap.containsKey(colMap.getProperty(DotSonColumnMap.NODE_ID))) {
			String error = "Node column headings cannot contain both \"AlphaID\" and \"NodeId\".";
			throw (new IOException(error));
		} else if (nodeHeaderMap.containsKey(colMap.getProperty(DotSonColumnMap.ALPHA_ID))) {
			// we are using alpha ids
			alphaId = true;
			alphaIdMap = new HashMap();
		}

	}

	/**
	 * parses the tab delimited arc column header, stores the tokens in the arc
	 * header map so parsing methods know which coumn of data to read from.
	 * 
	 * @param arcHeader
	 *            the string to parse column names from
	 */
	private void parseArcColHeader(String arcHeader) {
		// tokenize the string
		StringTokenizer arcHeaderTokens = new StringTokenizer(arcHeader, "\t");
		int numCols = arcHeaderTokens.countTokens();
		arcHeaderMap = new HashMap();
		// copy cols into map
		for (int n = 0; n < numCols; n++) {
			arcHeaderMap.put(arcHeaderTokens.nextToken(), new Integer(n));
		}
	}

	/**
	 * parses a tab delimited string into a series of node attributes by calling
	 * the parse method for each attribute on the token. creates a new node with
	 * the attributes and adds it to the list of nodes.
	 * 
	 * @param row
	 *            the string to be parsed as a row of node attributes
	 * @throws IOException
	 *             if line has fewer tokens than the header row
	 */
	private void parseNodeRow(String row) throws IOException {
		// tokenize the row
		StringTokenizer rowTokens = new StringTokenizer(row, "\t");
		int nCols = rowTokens.countTokens();
		String[] rowArray = new String[nCols];
		// check if there are enough tokens
		if (nCols >= nodeHeaderMap.size()) {
			// copy from tokenizer to array
			for (int n = 0; n < nCols; n++) {
				rowArray[n] = rowTokens.nextToken();

			}

			NodeAttribute node = new NodeAttribute(parseNodeId(rowArray), // id
																			// of
																			// node
					parseNodeLabel(rowArray), parseXCoord(rowArray),
					parseYCoord(rowArray), parseNodeStart(rowArray),
					parseNodeEnd(rowArray), originalFile + ":line "
							+ reader.getLineNumber()); // location line of
														// orginal file
			node.setNodeColor(parseNodeColor(rowArray));
			node.setNodeSize(parseNodeSize(rowArray));
			node.setNodeShape(parseNodeShape(rowArray));
			node.setBorderColor(parseBorderColor(rowArray));
			node.setLabelColor(parseLabelColor(rowArray));
			node.setBorderWidth(parseBorderWidth(rowArray));
			try // url may be invalid, problem creating icon, or network
				// unreachable
			{
				node.setIconURL(parseIconURL(rowArray));
			} catch (Exception e) {
				String error = "Unable to create icon for node: Line "
						+ reader.getLineNumber() + " Error:" + e.getMessage();
				throw (new IOException(error));
			}
			nodeList.add(node);
		} else // the row is too short
		{
			String error = "Unable to parse node: Line "
					+ reader.getLineNumber() + " doesn't have enough entries";
			throw (new IOException(error));
		}
	}

	/**
	 * parses the start time for a node, reading from the column of row array
	 * indicated by the position of "StartTime" in the nodeHeaderMap. Throws
	 * error if unable to parse as a double. If start time is not defined, it
	 * defaults to 0.0
	 * 
	 * @param rowArray
	 *            string array holding the tokens for the row of the parse file
	 * @return the start time for the node
	 * @throws IOException
	 *             if unable to parse a double from the column
	 */
	private double parseNodeStart(String[] rowArray) throws IOException {
		double startTime = 0.0;
		if (nodeHeaderMap.containsKey(colMap.getProperty(DotSonColumnMap.NODE_STARTIME))) {
			int index = ((Integer) nodeHeaderMap
					.get(colMap.getProperty(DotSonColumnMap.NODE_STARTIME))).intValue();
			try {
				startTime = Double.parseDouble(rowArray[index]);
			} catch (NumberFormatException doubleParseEx) {
				String error = "Line " + reader.getLineNumber()
						+ " Unable to parse node start time from column"
						+ colMap.getProperty(DotSonColumnMap.NODE_STARTIME) + ":" + rowArray[index];
				throw (new IOException(error));
			}
		}
		return startTime;
	}

	/**
	 * parses the end time for a node, reading from the column of row array
	 * indicated by the position of "EndTime" in the nodeHeaderMap. Throws error
	 * if unable to parse as a double. If end time is not defined, it defaults
	 * to double.POSITIVE_INFINITY. HOWEVER if EndTime is not included in the
	 * column headings AND startAsEnd is set to true, it will use the start
	 * time.
	 * 
	 * @param rowArray
	 *            string array holding the tokens for the row of the parse file
	 * @return the end time for the node
	 * @throws IOException
	 *             if unable to parse a double from the column
	 */
	private double parseNodeEnd(String[] rowArray) throws IOException {
		double endTime = Double.POSITIVE_INFINITY;
		if (nodeHeaderMap.containsKey(colMap.getProperty(DotSonColumnMap.NODE_ENDTIME))) {
			int index = ((Integer) nodeHeaderMap
					.get(colMap.getProperty(DotSonColumnMap.NODE_ENDTIME))).intValue();
			try {
				endTime = Double.parseDouble(rowArray[index]);
			} catch (NumberFormatException doubleParseEx) {
				String error = "Line " + reader.getLineNumber()
						+ " Unable to parse node end time from column"
						+ colMap.getProperty(DotSonColumnMap.NODE_ENDTIME) + ":" + rowArray[index];
				throw (new IOException(error));
			}
		} else if (startAsEnd) // if there is no end time, and we arn't using
								// defualt use the start time
		{
			endTime = parseNodeStart(rowArray);
		}
		return endTime;
	}

	/**
	 * parses an x coordinate from the row, according to the position of the X
	 * column header. Defaults to 0.0 if X not included. Throws exception if
	 * unable to parse as double.
	 * 
	 * @param rowArray
	 *            array of string tokens corresponding to the row
	 * @return a double x coordinate
	 * @throws IOException
	 *             if unable to parse as double
	 */
	private double parseXCoord(String[] rowArray) throws IOException {
		double xCoord = 0.0;
		String key = colMap.getProperty(DotSonColumnMap.NODE_X_COORD);
		if (parseCoords) {
			if (nodeHeaderMap.containsKey(key)) {
				int index = ((Integer) nodeHeaderMap
						.get(key)).intValue();
				try {
					xCoord = Double.parseDouble(rowArray[index]);

				} catch (NumberFormatException doubleParseEx) {
					String error = "Line " + reader.getLineNumber()
							+ " Unable to parse X coordinate from column"
							+ key + ":"
							+ rowArray[index];
					throw (new IOException(error));
				}
			}
		}
		return xCoord;
	}

	/**
	 * parses a y coordinate from the row, according to the position of the Y
	 * column header. Defaults to 0.0 if Y not included. Throws exception if
	 * unable to parse as double.
	 * 
	 * @param rowArray
	 *            array of string tokens corresponding to the row
	 * @return a double y coordinate
	 * @throws IOException
	 *             if unable to parse as double
	 */
	private double parseYCoord(String[] rowArray) throws IOException {
		double yCoord = 0.0;
		String key = colMap.getProperty(DotSonColumnMap.NODE_Y_COORD);
		if (parseCoords) {
			if (nodeHeaderMap.containsKey(key)) {
				int index = ((Integer) nodeHeaderMap
						.get(key)).intValue();
				try {
					yCoord = Double.parseDouble(rowArray[index]);

				} catch (NumberFormatException doubleParseEx) {
					String error = "Line" + reader.getLineNumber()
							+ " Unable to parse Y coordinate from column"
							+ key+ ":"
							+ rowArray[index];
					throw (new IOException(error));
				}
			}
		}
		return yCoord;
	}

	/**
	 * parses the node label from the row. If label is surrounded by quotes,
	 * they will be stripped. (SHOULD USE ID IF ALPHA IDS USED?)
	 * 
	 * @param rowArray
	 *            array of strings from the row being parsed
	 * @return string label for a node
	 */
	private String parseNodeLabel(String[] rowArray) {
		// should use id if no label?
		String label = "";
		String key = colMap.getProperty(DotSonColumnMap.NODE_LABEL);
		if (nodeHeaderMap.containsKey(key)) {
			int index = ((Integer) nodeHeaderMap
					.get(key)).intValue();
			label = rowArray[index];
			// strip of quotes if there are any
			if (label.startsWith("\"") | label.startsWith("\'")) {
				label = label.substring(1);
			}
			if (label.endsWith("\"") | label.endsWith("\'")) {
				label = label.substring(0, label.length() - 1);
			}
		}
		return label;
	}

	/**
	 * parses a double node size from the row. Gives the diameter for the
	 * "RectangularShape" in pixels. Defaults to 10.0 Throws errors if unable
	 * parse as double or of size is negitive.
	 * 
	 * @param rowArray
	 *            the array of strings from the row to parse.
	 * @return a double size for the node.
	 * @throws IOException
	 *             if unable to parse as double, or if size is negitive
	 */
	private double parseNodeSize(String[] rowArray) throws IOException {
		double size = 10; // size is in pixels
		String key = colMap.getProperty(DotSonColumnMap.NODE_SIZE);
		if (nodeHeaderMap.containsKey(key)) {
			int index = ((Integer) nodeHeaderMap.get(key))
					.intValue();
			// try to parse node ID from row
			try {
				size = Double.parseDouble(rowArray[index]);
			} catch (NumberFormatException e) {
				String error = "Unable to parse node size from column"
						+ key + " on line "
						+ reader.getLineNumber() + " : " + rowArray[index];
				throw (new IOException(error));
			}
			// make sure it is positive
			if (size < 0.0) {
				String error = "Line " + reader.getLineNumber()
						+ " NodeSize must be >= 0";
				throw (new IOException(error));
			}
		}
		return size;
	}

	/**
	 * parses a shape from the row. Currently parses to one of java's
	 * "RectangularShape" which means either Rectangle2D.double or
	 * Ellipse2D.double. Valid strings are "square" and "circle"(case
	 * insensitive). Defaults to ellipse. (ADD MORE SHAPE CLASSES?)
	 * 
	 * @param rowArray
	 *            the array of strings from the row to be parsed
	 * @return RectangularShape for the node
	 * @throws IOException
	 *             if value is not "square" or "circle"
	 */
	private RectangularShape parseNodeShape(String[] rowArray)
			throws IOException {
		// should be changed to some other shape classes that has more members
		// and many more shapes included
		RectangularShape shape;
		String key = colMap.getProperty(DotSonColumnMap.NODE_SHAPE);
		if (nodeHeaderMap.containsKey(key)) {
			int index = ((Integer) nodeHeaderMap
					.get(key)).intValue();
			// try to parse node ID from row
			if (rowArray[index].equalsIgnoreCase("square")
					| rowArray[index].equalsIgnoreCase("rect")) {
				shape = new Rectangle2D.Double();
			} else if (rowArray[index].equalsIgnoreCase("circle")
					| rowArray[index].equalsIgnoreCase("ellipse")) {
				shape = new Ellipse2D.Double();
			} else {
				String error = "Unable to parse NodeShape \"" + rowArray[index]
						+ "\" from column" + key
						+ " on line " + reader.getLineNumber() + "\n"
						+ "currently, shape must be \"square\" or \"circle\"";
				throw (new IOException(error));
			}
		} else {
			// use ellipse as defualt
			shape = new Ellipse2D.Double();
		}
		return shape;
	}

	/**
	 * parses the node ID either as an integer, or as a string. If the header
	 * "NodeId" is used, the node id is parsed as an integer, and added to the
	 * set of ids if not already present. Integer ids must form a continous set
	 * starting with 1. If id is <= 0, or if unable to parse as int, it will
	 * throw an error. If "AlphaId" is used, the string is used as the
	 * indentifier, but mapped to an integer id for internal use. If the string
	 * id exists, the corresponding int value is used. If does not exist,
	 * creates the next consecutive int id and adds it to the alphaIdMap. So
	 * does not check values.
	 */
	private int parseNodeId(String[] rowArray) throws IOException {
		int nodeID = -1;
		String key = colMap.getProperty(DotSonColumnMap.NODE_ID);
		String alphaKey = colMap.getProperty(DotSonColumnMap.ALPHA_ID);
		if (nodeHeaderMap.containsKey(key)) {
			int index = ((Integer) nodeHeaderMap.get(key))
					.intValue();
			// try to parse node ID from row
			try {
				nodeID = Integer.parseInt(rowArray[index]);
				// check to make sure id not less or equal to zero
				if (nodeID <= 0) {
					String error = "Unable to parse NodeId on line "
							+ reader.getLineNumber()
							+ " column"
							+ key
							+ "\n"
							+ "Numeric node ids must be integers greater than zero";
					throw (new IOException(error));
				}
				Integer ID = new Integer(nodeID);
				// if the id is not in the hashset, add it
				if (!idSet.contains(ID)) {
					idSet.add(ID);
				}

			} catch (NumberFormatException e) {
				String error = "Unable to parse NodeId on line "
						+ reader.getLineNumber()
						+ " column"
						+ key
						+ "\n"
						+ "When using numeric ids, each node line must begin with an integer id, followd by a tab";
				throw (new IOException(error));
			}
		} else if (nodeHeaderMap.containsKey(alphaKey)) {
			// used to parse node ids if non-numeric node and arc ids are being
			// used. If
			// node id exists, uses that value. If does not exist, creates the
			// next id
			// and adds it to the alphaIdMap. So does not check values
			int index = ((Integer) nodeHeaderMap.get(alphaKey))
					.intValue();
			String stringID = (String) rowArray[index];
			// check if id exists for the string id
			if (alphaIdMap.containsKey(stringID)) {
				// so get the corresponding id
				nodeID = ((Integer) alphaIdMap.get(stringID)).intValue();
			}
			// if it doesn't exist, add it
			else {
				nodeID = idSet.size() + 1;
				Integer ID = new Integer(nodeID);
				alphaIdMap.put(stringID, ID);
				idSet.add(ID);
			}
		} else // something is teribly wrong
		{
			String error = "line "
					+ reader.getLineNumber()
					+ "\r"
					+ " Node column headings must begin with either \"NodeId\" or \"AlphaId\".";
			throw (new IOException(error));
		}
		return nodeID;
	}

	/**
	 * parses a an arc record row, calling the appropriate parsing methods for
	 * each attribute of the node. Throws error if row has fewer entries than
	 * there are column headings. Creates a new ArcAttribute and adds it ot the
	 * list of ArcAttributes
	 * 
	 * @param row
	 *            string from input file to be tokenized and parsed as an arc
	 *            record
	 * @throw IOException if row has too few tokens
	 */
	private void parseArcRow(String row) throws IOException {
		// tokenize the row
		StringTokenizer rowTokens = new StringTokenizer(row, "\t");
		int nCols = rowTokens.countTokens();
		String[] rowArray = new String[nCols];
		// check if there are enough tokens
		if (nCols >= arcHeaderMap.size()) {
			// copy from tokenizer to array
			for (int n = 0; n < nCols; n++) {
				rowArray[n] = rowTokens.nextToken();
			}
		} else
		// throw error
		{
			String error = "Unable to parse arc record: row "
					+ reader.getLineNumber() + " is missing entries.";
			throw new IOException(error);
		}
		ArcAttribute arc = new ArcAttribute(parseArcStart(rowArray),
				parseArcEnd(rowArray), parseFromId(rowArray),
				parseToId(rowArray), parseArcWeight(rowArray),
				parseArcWidth(rowArray));
		arc.setArcColor(parseArcColor(rowArray));
		arcList.add(arc);
	}

	/**
	 * parses the id of the originating node for an arc. If alpha ids are used,
	 * parses as a string and looks up the list of alpha ids for a match, throws
	 * error if none found. If numeric ids, parses as integer, checks if in list
	 * of ids, throws error if missing or unable to parse.
	 * 
	 * @param rowArray
	 *            the array of tokens for arc attributes
	 * @returns integer id corresponding to the the arc's originating node
	 * @throw IOException if unable to parse or id is missing
	 */
	private int parseFromId(String[] rowArray) throws IOException {
		int fromId = -1;
		String fromKey = colMap.getProperty(DotSonColumnMap.FROM_ID);
		if (arcHeaderMap.containsKey(fromKey)) {
			int index = ((Integer) arcHeaderMap.get(fromKey))
					.intValue();
			// check if alpha or numeric ids
			if (alphaId) {
				// get the id
				String stringID = rowArray[index];
				// check if id is in list
				if (alphaIdMap.containsKey(stringID)) {
					// retrive the id for that string
					fromId = ((Integer) alphaIdMap.get(stringID)).intValue();
				} else {
					String error = "Unable to link FromId on line "
							+ reader.getLineNumber()
							+ "column "
							+ fromKey
							+ " : "
							+ "There is no AlphaID (node) corresponding to FromID "
							+ stringID;
					throw (new IOException(error));
				}

			}
			// parse id as numeric id
			else {
				// try to parse node ID from row
				try {
					fromId = Integer.parseInt(rowArray[index]);
					Integer ID = new Integer(fromId);
					// make sure there is a corresponding ID
					if (!idSet.contains(ID)) {
						String error = "Unable to link FromId on line "
								+ reader.getLineNumber() + "column "
								+ fromKey + " : "
								+ "There is no NodeId corresponding to FromID "
								+ fromId;
						throw (new IOException(error));
					}

				} catch (NumberFormatException e) {
					String error = "Unable to parse FromId on line "
							+ reader.getLineNumber()
							+ ""
							+ "column "
							+ fromKey
							+ ": "
							+ "When using numeric ids, each arc line must have an integer Id for the orginating node";
					throw (new IOException(error));
				}
			}
		} else // something is teribly wrong
		{
			String error = "line "
					+ reader.getLineNumber()
					+ "\r"
					+ " Arc column headings must contain a column for \"FromId\".";
			throw (new IOException(error));
		}
		return fromId;
	}

	/**
	 * parses the id of the destination node for an arc. If alpha ids are used,
	 * parses as a string and looks up the list of alpha ids for a match, throws
	 * error if none found. If numeric ids, parses as integer, checks if in list
	 * of ids, throws error if missing or unable to parse.
	 * 
	 * @param rowArray
	 *            the array of tokens for arc attributes
	 * @returns integer id corresponding to the the arc's destination node
	 * @throw IOException if unable to parse or id is missing
	 */
	private int parseToId(String[] rowArray) throws IOException {
		int toId = -1;
		String toKey = colMap.getProperty(DotSonColumnMap.TO_ID);
		if (arcHeaderMap.containsKey(toKey)) {
			int index = ((Integer) arcHeaderMap.get(toKey))
					.intValue();
			// check if alpha or numeric ids
			if (alphaId) {
				// get the id
				String stringID = rowArray[index];
				// check if id is in list
				if (alphaIdMap.containsKey(stringID)) {
					// retrive the id for that string
					toId = ((Integer) alphaIdMap.get(stringID)).intValue();
				} else {
					String error = "Unable to link ToId on line "
							+ reader.getLineNumber()
							+ "column "
							+ toKey
							+ " : "
							+ "There is no AlphaID (node) corresponding to ToId "
							+ stringID;
					throw (new IOException(error));
				}

			} else {
				// try to parse node ID from row
				try {
					toId = Integer.parseInt(rowArray[index]);
					Integer ID = new Integer(toId);
					// make sure there is a corresponding ID
					if (!idSet.contains(ID)) {
						String error = "Unable to link ToId on line "
								+ reader.getLineNumber() + "column "
								+ DotSonColumnMap.TO_ID + " : "
								+ "There is no NodeId corresponding to ToId "
								+ toId;
						throw (new IOException(error));
					}

				} catch (NumberFormatException e) {
					String error = "Unable to parse ToId on line "
							+ reader.getLineNumber()
							+ "column "
							+ DotSonColumnMap.TO_ID
							+ " : "
							+ "When using numeric ids, each arc line must have an integer toId for the destination node";
					throw (new IOException(error));
				}
			}
		} else // something is teribly wrong
		{
			String error = "line "
					+ reader.getLineNumber()
					+ "\r"
					+ " Arc column headings must contain a column for \"ToId\".";
			throw (new IOException(error));
		}
		return toId;
	}

	private double parseArcWeight(String[] rowArray) throws IOException {
		double weight = 1;
		String weightKey = colMap.getProperty(DotSonColumnMap.ARC_WEIGHT);
		if (arcHeaderMap.containsKey(weightKey)) {
			int index = ((Integer) arcHeaderMap.get(weightKey))
					.intValue();
			// try to parse node ID from row
			try {
				weight = Double.parseDouble(rowArray[index]);
			} catch (NumberFormatException doubleParseEx) {
				String error = "Line " + reader.getLineNumber() + "column "
						+ weightKey
						+ " Unable to parse ArcWeight:" + rowArray[index];
				throw (new IOException(error));
			}
		}
		return weight;
	}

	private double parseArcWidth(String[] rowArray) throws IOException {
		double width = 1;
		String key = colMap.getProperty(DotSonColumnMap.ARC_WIDTH);
		if (arcHeaderMap.containsKey(key)) {
			int index = ((Integer) arcHeaderMap.get(key))
					.intValue();
			// try to parse node ID from row
			try {
				width = Double.parseDouble(rowArray[index]);
			} catch (NumberFormatException doubleParseEx) {
				String error = "Line " + reader.getLineNumber() + "column "
						+ key
						+ " Unable to parse ArcWidth:" + rowArray[index];
				throw (new IOException(error));
			}
		}
		return width;
	}

	private double parseArcStart(String[] rowArray) throws IOException {
		double startTime = 0.0;
		String key = colMap.getProperty(DotSonColumnMap.ARC_STARTIME);
		if (arcHeaderMap.containsKey(key)) {
			int index = ((Integer) arcHeaderMap
					.get(key)).intValue();
			try {
				startTime = Double.parseDouble(rowArray[index]);
			} catch (NumberFormatException doubleParseEx) {
				String error = "Line " + reader.getLineNumber() + "column "
						+ key
						+ " Unable to parse arc start time:" + rowArray[index];
				throw (new IOException(error));
			}
		}
		return startTime;
	}

	private double parseArcEnd(String[] rowArray) throws IOException {
		double endTime = Double.POSITIVE_INFINITY;
		String key = colMap.getProperty(DotSonColumnMap.ARC_ENDTIME);
		if (arcHeaderMap.containsKey(key)) {
			int index = ((Integer) arcHeaderMap
					.get(key)).intValue();
			try {
				endTime = Double.parseDouble(rowArray[index]);
			} catch (NumberFormatException doubleParseEx) {
				String error = "Line " + reader.getLineNumber() + "column "
						+ key
						+ " Unable to parse arc end time:" + rowArray[index];
				throw (new IOException(error));
			}
		} else if (startAsEnd) // if there is no end time, and we arn't using
								// defualt use the start time
		{
			endTime = parseArcStart(rowArray);
		}
		return endTime;
	}

	// loop over the nodes in idSet and make sure there are no gaps in the
	// sequence
	private void checkForMissingNodes() throws IOException {
		int setSize = idSet.size();
		for (int n = 1; n <= setSize; n++) {
			Integer compareInt = new Integer(n);
			if (!idSet.contains(compareInt)) {
				// there is a missing ID, so throw error
				String error = "There is a gap in the sequence of NodeIds, "
						+ n
						+ " is missing\n"
						+ "File may contain multiple records for each NodeId, but the set of "
						+ "NodeIds must form a complete sequence";
				throw (new IOException(error));
			}
		}

	}

	private Color parseNodeColor(String[] rowArray) throws IOException {
		Color theColor = Color.white;
		String colorKey = colMap.getProperty(DotSonColumnMap.NODE_COLOR_NAME);
		String rKey = colMap.getProperty(DotSonColumnMap.NODE_RED_RGB);
		String gKey = colMap.getProperty(DotSonColumnMap.NODE_GREEN_RGB);
		String bKey = colMap.getProperty(DotSonColumnMap.NODE_BLUE_RGB);
		// figure out if and how color is specified
		int index;
		if (nodeHeaderMap.containsKey(colorKey)) {
			index = ((Integer) nodeHeaderMap
					.get(colorKey)).intValue();
			theColor = parseColorName(rowArray[index]);
		} else {
			// check for each of the RGB colums
			float red = 0.0f;
			float green = 0.0f;
			float blue = 0.0f;
			// check for red
			if (nodeHeaderMap.containsKey(rKey)) {
				int indexR = ((Integer) nodeHeaderMap
						.get(rKey)).intValue();
				try {
					red = Float.parseFloat(rowArray[indexR]);
				} catch (NumberFormatException e) {
					String error = "Line " + reader.getLineNumber() + "column "
							+ rKey
							+ " Unable to parse RedRGB value:"
							+ rowArray[indexR];
					throw (new IOException(error));
				}
			}
			// check for green
			if (nodeHeaderMap.containsKey(gKey)) {
				int indexG = ((Integer) nodeHeaderMap
						.get(gKey)).intValue();
				try {
					green = Float.parseFloat(rowArray[indexG]);
				} catch (NumberFormatException e) {
					String error = "Line " + reader.getLineNumber() + "column "
							+ gKey
							+ " Unable to parse GreenRGB value:"
							+ rowArray[indexG];
					throw (new IOException(error));
				}
			}
			// check for blue
			if (nodeHeaderMap.containsKey(bKey)) {
				int indexB = ((Integer) nodeHeaderMap
						.get(bKey)).intValue();
				try {
					blue = Float.parseFloat(rowArray[indexB]);
				} catch (NumberFormatException e) {
					String error = "Line " + reader.getLineNumber() + "column "
							+ bKey
							+ " Unable to parse BlueRGB value:"
							+ rowArray[indexB];
					throw (new IOException(error));
				}
			}
			// try and make an rgb color with these values
			theColor = parseRGBColor(red, green, blue);
		}

		return theColor;
	}

	private Color parseLabelColor(String[] rowArray) throws IOException {
		Color theColor = Color.blue;
		String key = colMap.getProperty(DotSonColumnMap.NODE_LABEL_COLOR_NAME);
		// figure out if and how color is specified
		int index;
		if (nodeHeaderMap.containsKey(key)) {
			index = ((Integer) nodeHeaderMap
					.get(key)).intValue();
			theColor = parseColorName(rowArray[index]);
		}
		return theColor;
	}

	private Color parseBorderColor(String[] rowArray) throws IOException {
		Color theColor = Color.black;
		String key = colMap.getProperty(DotSonColumnMap.NODE_BORDER_COLOR_NAME);
		// figure out if and how color is specified
		int index;
		if (nodeHeaderMap.containsKey(key)) {
			index = ((Integer) nodeHeaderMap
					.get(key)).intValue();
			theColor = parseColorName(rowArray[index]);
		}
		return theColor;
	}

	private double parseBorderWidth(String[] rowArray) throws IOException {
		double width = 1;
		String key = colMap.getProperty(DotSonColumnMap.NODE_BORDER_WIDTH);
		int index;
		if (nodeHeaderMap.containsKey(key)) {
			index = ((Integer) nodeHeaderMap
					.get(key)).intValue();
			try {
				width = Double.parseDouble(rowArray[index]);
			} catch (NumberFormatException doubleParseEx) {
				String error = "Line " + reader.getLineNumber() + " column "
						+ key
						+ " Unable to parse BorderWidth:" + rowArray[index];
				throw (new IOException(error));
			}
		}
		return width;
	}

	private URL parseIconURL(String[] rowArray) throws IOException {
		URL url = null;
		int index;
		String key = colMap.getProperty(DotSonColumnMap.NODE_ICON_URL);

		if (nodeHeaderMap.containsKey(key)) {
			index = ((Integer) nodeHeaderMap.get(key))
					.intValue();
			// "null" string indicates no image URL provided, otherwise check
			// for a proper URL
			if (!(rowArray[index].equals("null"))) {
				try {
					url = new URL(rowArray[index]);
				} catch (MalformedURLException urlParseEx) {
					String error = "Line " + reader.getLineNumber()
							+ " column " + key
							+ " Unable to parse IconURL: " + rowArray[index];
					throw (new IOException(error));
				}
			}
		}
		return url;
	}

	private Color parseArcColor(String[] rowArray) throws IOException {
		Color theColor = Color.gray;
		// figure out if and how color is specified
		int index;
		if (arcHeaderMap.containsKey(colMap.getProperty(DotSonColumnMap.ARC_COLOR_NAME))) {
			index = ((Integer) arcHeaderMap.get(colMap.getProperty(DotSonColumnMap.ARC_COLOR_NAME)))
					.intValue();
			theColor = parseColorName(rowArray[index]);
		} else {
			// check for each of the RGB colums
			float red = 0.0f;
			float green = 0.0f;
			float blue = 0.0f;
			// check for red
			if (arcHeaderMap.containsKey(colMap.getProperty(DotSonColumnMap.ARC_RED_RGB))) {
				int indexR = ((Integer) arcHeaderMap
						.get(colMap.getProperty(DotSonColumnMap.ARC_RED_RGB))).intValue();
				try {
					red = Float.parseFloat(rowArray[indexR]);
				} catch (NumberFormatException e) {
					String error = "Line " + reader.getLineNumber() + "column "
							+ colMap.getProperty(DotSonColumnMap.ARC_RED_RGB)
							+ " Unable to parse RedRGB value:"
							+ rowArray[indexR];
					throw (new IOException(error));
				}
			}
			// check for green
			if (arcHeaderMap.containsKey(colMap.getProperty(DotSonColumnMap.ARC_GREEN_RGB))) {
				int indexG = ((Integer) arcHeaderMap
						.get(colMap.getProperty(DotSonColumnMap.ARC_GREEN_RGB))).intValue();
				try {
					green = Float.parseFloat(rowArray[indexG]);
				} catch (NumberFormatException e) {
					String error = "Line " + reader.getLineNumber() + "column "
							+ colMap.getProperty(DotSonColumnMap.ARC_GREEN_RGB)
							+ " Unable to parse GreenRGB value:"
							+ rowArray[indexG];
					throw (new IOException(error));
				}
			}
			// check for blue
			if (arcHeaderMap.containsKey(colMap.getProperty(DotSonColumnMap.ARC_BLUE_RGB))) {
				int indexB = ((Integer) arcHeaderMap
						.get(colMap.getProperty(DotSonColumnMap.ARC_BLUE_RGB))).intValue();
				try {
					blue = Float.parseFloat(rowArray[indexB]);
				} catch (NumberFormatException e) {
					String error = "Line " + reader.getLineNumber() + "column "
							+ colMap.getProperty(DotSonColumnMap.ARC_BLUE_RGB)
							+ " Unable to parse BlueRGB value:"
							+ rowArray[indexB];
					throw (new IOException(error));
				}
			}
			// try and make an rgb color with these values
			theColor = parseRGBColor(red, green, blue);
		}

		return theColor;
	}

	private Color parseRGBColor(float R, float G, float B) throws IOException {
		// check value ranges
		if ((R > 1.0f) | (R < 0.0f)) {
			String error = "Line" + reader.getLineNumber()
					+ "Red RGB color value out of range: " + R
					+ "\n RGB values bust be between 0.0 and 1.0";
			throw (new IOException(error));
		}
		if ((G > 1.0f) | (G < 0.0f)) {
			String error = "Line" + reader.getLineNumber()
					+ "Green RGB color value out of range: " + R
					+ "\n RGB values bust be between 0.0 and 1.0";
			throw (new IOException(error));
		}
		if ((B > 1.0f) | (B < 0.0f)) {
			String error = "Line" + reader.getLineNumber()
					+ "Blue RGB color value out of range: " + R
					+ "\n RGB values bust be between 0.0 and 1.0";
			throw (new IOException(error));
		}
		// create color with RGB components
		Color theColor = new Color(R, G, B);
		return theColor;
	}

	private Color parseColorName(String text) throws IOException {
		//TODO: extend color names to full list from pajek?
		Color theColor = Color.white;
		if (text.equalsIgnoreCase("Black")) {
			theColor = Color.black;
		} else if (text.equalsIgnoreCase("Cyan")) {
			theColor = Color.cyan;
		} else if (text.equalsIgnoreCase("DarkGray")) {
			theColor = Color.darkGray;
		} else if (text.equalsIgnoreCase("Gray")) {
			theColor = Color.gray;
		} else if (text.equalsIgnoreCase("green")) {
			theColor = Color.green;
		} else if (text.equalsIgnoreCase("lightGray")) {
			theColor = Color.lightGray;
		} else if (text.equalsIgnoreCase("magenta")) {
			theColor = Color.magenta;
		} else if (text.equalsIgnoreCase("Orange")) {
			theColor = Color.orange;
		} else if (text.equalsIgnoreCase("pink")) {
			theColor = Color.pink;
		} else if (text.equalsIgnoreCase("red")) {
			theColor = Color.red;
		} else if (text.equalsIgnoreCase("white")) {
			theColor = Color.white;
		} else if (text.equalsIgnoreCase("yellow")) {
			theColor = Color.yellow;
		} else if (text.equalsIgnoreCase("blue")) {
			theColor = Color.blue;
		} else // unable to match a color name
		{
			String error = "Line" + reader.getLineNumber()
					+ " Unable to parse color name: " + text;
			throw (new IOException(error));
		}
		return theColor;
	}

	// for remapping columns

	// accessor methods--------------------------
	public int getMaxNumNodes() {
		return idSet.size();
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
		return infoString;
	}

	/**
	 * gives the name of the parser. appends "(with alphanumeric IDs)" if alpha
	 * ids are being used
	 * 
	 * @return the name of the parser
	 */
	public String getParserInfo() {
		String details = "DotSonParser";
		if (alphaId) {
			details = details + " (with alphanumeric IDs)";
		}
		return details;
	}


	/**
	 * casts to DotSonColmnMap and uses to controll column labling
	 */
	public void configureParser(PropertySettings settings) {
		//check type of settings
		if (settings instanceof DotSonColumnMap){
			this.colMap = (DotSonColumnMap)settings;
		} else {
			//TODO: need a way to report errors in configuration
		}
		
	}
}