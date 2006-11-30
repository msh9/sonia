package sonia.parsers;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;

import sonia.ArcAttribute;
import sonia.NodeAttribute;
import sonia.settings.PropertySettings;

/**
 * reads the version of the UCINET DL multiple matrix edgelist format written by
 * the R sna package write.dl command
 * 
 * @author skyebend
 * 
 */
public class DLParser implements Parser {

	private LineNumberReader reader;

	private Vector nodeList;

	private Vector arcList;

	private HashSet idSet;

	private int numNodes = 0;

	private int numArcs = 0;

	private int numMatricies = 0;

	private boolean hasLabels = false;

	private String originalFile; // path and name of the file it was loaded

	// from

	private String infoString = "";

	public void parseNetwork(String fileAndPath) throws IOException {
		originalFile = fileAndPath;

		nodeList = new Vector();
		arcList = new Vector();
		idSet = new HashSet();
		HashMap headerTagMap = new HashMap();
		// open connection to file
		reader = new LineNumberReader(new FileReader(fileAndPath));

		String line = reader.readLine();
		// first line should look like : DL n = 16, nm = 100, format = edgelist1
		// check that it starts with DL
		if (!line.startsWith("DL") & !line.startsWith("dl")) {
			String error = "First line of DL file must begin with 'DL'";
			throw (new IOException(error));
		} else { // take of the DL and break the string into tag pairs
			infoString = line;
			String rawTags = line.substring(2);
			StringTokenizer tagkoniser = new StringTokenizer(rawTags, ",");
			while (tagkoniser.hasMoreTokens()) {
				String tag = tagkoniser.nextToken();
				headerTagMap.put(tag.substring(0, tag.indexOf("=") - 1).trim(),
						tag.substring(tag.indexOf("=") + 1).trim());
			}
			// now loop over the tags to see which ones are present

			// check that format = edgelist1 (not some other dl file format
			String format = (String) headerTagMap.get("format");
			if (format == null) {
				String error = "DL file header is missing 'format' tag in first line of header";
				throw (new IOException(error));
			}
			if (!format.equals("edgelist1") & !format.equals("fullmatrix")) {
				String error = "Sorry, SoNIA does not know how to read the DL format '"
						+ format + "'";
				throw (new IOException(error));
			}
			// read number of nodes
			try {
				numNodes = Integer.parseInt((String) headerTagMap.get("n"));
			} catch (Exception e) {
				String error = "Header of DL file must contain tag 'n = <numNodes>'"
						+ e.getMessage();
				throw (new IOException(error));
			}
			// read number matricies
			String mats = (String) headerTagMap.get("nm");
			if (mats != null) {
				try {
					numMatricies = Integer.parseInt(mats);
				} catch (Exception e) {
					String error = "Header of DL file must contain tag 'm = <numMatricies>'"
							+ e.getMessage();
					throw (new IOException(error));
				}
			} else {// must assume only be one time step
				numMatricies = 1;
			}
			// next line should be: labels:
			line = reader.readLine();
			if (line.equals("labels:")) {
				hasLabels = true;
				// TODO: DL parser should check "labels: embeded" tag.
			}

			if (hasLabels) {
				line = reader.readLine();
				StringTokenizer labelkonizer = new StringTokenizer(line, ",");
				// check that there are the right number of labels
				if (labelkonizer.countTokens() != numNodes) {
					String error = "Number of labels ("
							+ labelkonizer.countTokens()
							+ ") does not equal n tag (" + numNodes + ")";
					throw (new IOException(error));
				}
				int nodeCount = 0;
				while (labelkonizer.hasMoreTokens()) {
					String label = labelkonizer.nextToken();
					nodeCount++;
					// make the nodes
					makeNode(nodeCount, label, 0, numMatricies);
				}
			} else { // no labels, so need to fake them
				for (int nodeCount = 1; nodeCount <= numNodes; nodeCount++) {
					makeNode(nodeCount, nodeCount + "", 0, numMatricies);
				}
			}

			while ((line != null) && (!line.trim().endsWith("data:"))) {
				line = reader.readLine();

			}
			if (line == null) {
				String error = "Reached end of DL file without locating 'data:' tag";
				throw (new IOException(error));
			}
			// now do it differntly if we are reading matricies or edgelists
			if (format.equals("edgelist1")) {
				parseEdgelistBlock(reader);
			} else if (format.equals("fullmatrix")) {
				parseMatrixBlock(reader);
			} else {
				String error = "Sorry, SoNIA does not know how to read the DL format '"
						+ format + "'";
				throw (new IOException(error));
			}

		}

	}

	private void parseMatrixBlock(LineNumberReader reader) throws IOException {
		int startTime = 0;
		String line = reader.readLine();
		while (line != null) { // loop over the edgelist for each time
			// start the edgelist step corresponding to the next time step
			// read each line for the matrix until reaching EOF or the
			// sign for
			// netxt matrix
			int rowNum = 0;
			while ((line != null) && (!line.trim().equals("!"))) {
				// check that there are the correct number of rows
				if (rowNum > numNodes) {
					String error = "Number of matrix rows (" + rowNum
							+ ") is  greater than n tag(" + numNodes + ")";
					throw (new IOException(error));
				}
				readMatrixRow(line, rowNum, startTime, startTime + 1);
				line = reader.readLine();
				rowNum++;
			}
			if (rowNum < numNodes) {
				String error = "Number of matrix rows (" + rowNum
						+ ") is  less than n tag(" + numNodes + ")";
				throw (new IOException(error));
			}
			// increment the time step before starting next matrix
			startTime++;
			line = reader.readLine();
		}
		// cheack that we have read the right number of matricies
		if (startTime != numMatricies) {
			String error = "Number of matricies sets (" + startTime
					+ ") does not equal nm tag (" + numMatricies + ")";
			throw (new IOException(error));
		}
	}

	private void readMatrixRow(String line, int row, int startTime, int endTime)
			throws IOException {
		StringTokenizer rowkonizer = new StringTokenizer(line, " ");
		// should have three entries, int from, int to, weight
		int from = row + 1;
		int to = 1;
		double weight = -1;
		//loop along the row, reading the weights
		while (rowkonizer.hasMoreTokens()) {
			String toParse = (String) rowkonizer.nextToken();
			// check number of entries in row
			if (to > numNodes) {
				String error = "Number of matrix entries on row " + row
						+ " of matrix " + startTime + " has more than "
						+ numNodes + " entries. (line "
						+ reader.getLineNumber() + " of DL file)";
				throw (new IOException(error));
			}
			try {
				weight = Double.parseDouble(toParse);
			} catch (Exception e) {
				String error = "Unable to parse weight " + toParse
						+ " on matrix " + startTime + " (" + from + "," + to
						+ ") , on line" + reader.getLineNumber() + "of file :"
						+ e.getMessage();
				throw (new IOException(error));
			}

			// if the weight is not 0, make the edge
			if (weight > 0.0) {
				ArcAttribute arc = new ArcAttribute(startTime, endTime, from,
						to, weight, weight);
				arcList.add(arc);
			}
			to++;
		}
	}

	private void parseEdgelistBlock(LineNumberReader reader) throws IOException {
		/*
		 * now there should be a part that looks like data: 8 1 1 11 1 1 12 1 1
		 * 13 1 1 5 16 1 9 16 1 !
		 * 
		 * where the first to give from and two ids and the last is the weight
		 * space delimited
		 */
		int startTime = 0;
		String line = reader.readLine();
		while (line != null) { // loop over the edgelist for each time
			// start the edgelist step corresponding to the next time step
			// read each line for the matrix until reaching EOF or the
			// sign for
			// netxt matrix
			while ((line != null) && (!line.trim().equals("!"))) {
				readEdgelistRow(line, startTime, startTime + 1);
				line = reader.readLine();
			}
			// increment the time step before starting next matrix
			startTime++;
			line = reader.readLine();
		}
		// cheack that we have read the right number of matricies
		if (startTime != numMatricies) {
			String error = "Number of edgelist sets (" + startTime
					+ ") does not equal nm tag (" + numMatricies + ")";
			throw (new IOException(error));
		}
	}

	private void makeNode(int id, String label, double timeStart, double timeEnd) {
		// make the node
		NodeAttribute node = new NodeAttribute(id, label, 0.0, 0.0, timeStart,
				timeEnd, originalFile);
		// could set other attributes here
		nodeList.add(node);
	}

	private void readEdgelistRow(String row, double startTime, double endTime)
			throws IOException {
		StringTokenizer rowkonizer = new StringTokenizer(row, " ");
		// should have three entries, int from, int to, weight
		int from = -1;
		int to = -1;
		double weight = -1;
		String toParse = (String) rowkonizer.nextToken();
		try {
			from = Integer.parseInt(toParse);
			toParse = (String) rowkonizer.nextToken();
		} catch (Exception e) {
			String error = "Unable to parse from id " + toParse
					+ " on edgelist line " + reader.getLineNumber() + " :"
					+ e.getMessage();
			throw (new IOException(error));
		}
		// check range of id
		if ((from <= 0) | from > numNodes) {
			String error = "Id of from node (" + toParse
					+ ") on edgelist line " + reader.getLineNumber()
					+ " is out of range";
			throw (new IOException(error));
		}
		try {
			to = Integer.parseInt(toParse);
			toParse = (String) rowkonizer.nextToken();
		} catch (Exception e) {
			String error = "Unable to parse to id " + toParse
					+ " on edgelist line " + reader.getLineNumber() + " :"
					+ e.getMessage();
			throw (new IOException(error));
		}
		// check range of id
		if ((to <= 0) | to > numNodes) {
			String error = "Id of to node (" + toParse + ") on edgelist line "
					+ reader.getLineNumber() + " is out of range";
			throw (new IOException(error));
		}
		try {
			weight = Double.parseDouble(toParse);
		} catch (Exception e) {
			String error = "Unable to parse weight " + toParse
					+ " on edgelist line " + reader.getLineNumber() + " :"
					+ e.getMessage();
			throw (new IOException(error));
		}
		// make the edge
		ArcAttribute arc = new ArcAttribute(startTime, endTime, from, to,
				weight, weight);
		arcList.add(arc);

	}

	public int getMaxNumNodes() {
		// TODO Auto-generated method stub
		return numNodes;
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

	public String getParserInfo() {
		return "DL (edglist1,fullmatrix) parser";
	}

	/**
	 * this parser is not configurable
	 */
	public void configureParser(PropertySettings settings) {

	}

}
