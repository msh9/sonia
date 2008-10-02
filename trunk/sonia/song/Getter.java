package sonia.song;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import sonia.parsers.DotSonParser;
import sonia.song.filters.AllProblemFilter;
import sonia.song.filters.CleaningFilter;
import sonia.song.filters.SqlDateToDecimal;

/**
 * Class to manage the process of building .son file from a series of queries to
 * an SQL database default version generates the nodeset based on nodes that
 * appear in the edgest. For mysql, requires the Connector/J libraries from
 * http://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.6.tar.gz/
 * 
 * @author skyebend
 * 
 */

public class Getter {
	/**
	 * string used in node queries that will be replaced by a node id before
	 * excuting
	 */
	public static final String nodeIdTag = "$NODEID";

	/**
	 * string used in node queries that will be replaced by a time before
	 * executing
	 */
	public static final String timeIdTag = "$TIME";

	private String jdbcDriver = "com.mysql.jdbc.Driver";

	private String hostBase = "jdbc:mysql://";

	private String hostSuffix = "?useCompression=true"; // for compression and

	// other options

	private SonGUI ui = null;

	private Connection currentDB = null;

	private Statement query = null;

	private ArrayList<String> nodeHeaders = new ArrayList<String>();

	private ArrayList<String> arcHeaders = new ArrayList<String>();

	private int fromCol = -1;

	private int toCol = -1;

	private String currentNet = "";

	private HashSet<String> nodeIds;

	private ArrayList<String[]> nodeData = new ArrayList<String[]>();;

	private ArrayList<String[]> arcData = new ArrayList<String[]>();

	private ArrayList<DataProblem> problems = new ArrayList<DataProblem>();

	private ArrayList<CleaningFilter> knownFilters = new ArrayList<CleaningFilter>();

	public Getter() {

	}

	public Getter(SonGUI gui) {
		ui = gui;
		// initialize the list of known filters
		knownFilters.add(new AllProblemFilter());
		knownFilters.add(new SqlDateToDecimal(this));
	}

	/**
	 * executes the relationship query, pulls out all the node ids and runs a
	 * nodes attr query once for each, stores results as the current network
	 * 
	 * @return
	 */
	public boolean getNetwork(String arcQuery, boolean makeNodes,
			String nodePropsQuery, boolean makeTimes) {
		// TODO: need to check if the data being processed contains tabs that
		// need to be escaped or quoted..
		problems.clear();
		status("Running queries to build network...");
		// get the arcAttr query
		// try to run it
		ResultSet arcs = runQuery(arcQuery);
		fromCol = -1;
		toCol = -1;
		nodeIds = new HashSet<String>();
		try {
			ResultSetMetaData arcsMeta = arcs.getMetaData();
			int numCols = arcsMeta.getColumnCount();
			status("   arcs query returned " + numCols + " columns.");
			// check that FromId and ToId are defined as column names
			// and also store column names in list of headers
			arcHeaders.clear();
			for (int c = 0; c < numCols; c++) {
				arcHeaders.add(arcsMeta.getColumnLabel(c + 1));
				if (arcsMeta.getColumnLabel(c + 1).equals("FromId")) {
					fromCol = c;
				}
				if (arcsMeta.getColumnLabel(c + 1).equals("ToId")) {
					toCol = c;
				}
			}
			if (fromCol < 0 | toCol < 0) {
				error("Arcs query did not contain columns for both 'FromId' and 'ToId'");
				return (false);
			}
			// store results in arcData (too bad we don't yet know how big it
			// is...
			arcData.clear();
			int arcId = 0;
			// process each row
			while (arcs.next()) {
				String[] row = new String[numCols];
				// loop over cols
				for (int i = 0; i < numCols; i++) {
					row[i] = arcs.getString(i + 1).trim();
				}
				// add any new ids to the list
				if (!nodeIds.contains(row[fromCol])) {
					nodeIds.add(row[fromCol]);
				}
				if (!nodeIds.contains(row[toCol])) {
					nodeIds.add(row[toCol]);
				}
				arcData.add(row);
				arcId++;
			}
			status("   processed " + arcId + " arc rows.");
			// get rid of he resultset
			arcs.close();

		} catch (Exception e) {
			error("processing arcs query:" + e.getMessage());
			return false;
		}
		// HERE WE BEGIN TO PROCESS NODES
		try {
			nodeHeaders.clear();
			// if we are auto generating the nodes section..
			if (makeNodes) {
				status("Running properties queries for " + nodeIds.size()
						+ " nodes...");
				nodeHeaders.add("AlphaId");
				// check if a query was passed in
				// if blank, use a default
				if (nodePropsQuery.equals("")) {
					nodePropsQuery = "select '$NODEID' as Label";
					status("Using default node query: select '$NODEID' as Label");
				}

				if (nodePropsQuery != null && !nodePropsQuery.equals("")) {
					// find where the node id is in the query
					int replaceIndex = nodePropsQuery.indexOf(nodeIdTag);
					if (replaceIndex < 0) {
						warn("node properties query does not contain '"
								+ nodeIdTag
								+ "' to indicate where ids should be inserted");
					}
					// if we are alse replacing times
					HashSet<String> timeset;
					if (makeTimes) {
						int timeIndex = nodePropsQuery.indexOf(timeIdTag);
						if (timeIndex < 0) {
							warn("node properties query does not contain '"
									+ timeIdTag
									+ "' to indicate where times should be inserted");
						}
						// get the list of times that are going to be
						// substituted
						timeset = getTimeset(arcData);
						// TODO: should have seperate timeset for starting and
						// ending times
					} else {
						// put in a dummy value so that the loop will go once
						timeset = new HashSet<String>();
						timeset.add("");
					}
					nodeData.clear();
					// need to figure out how many cols returning and col names
					// so first run a fake query that should return only the
					// columns
					String propQuery = nodePropsQuery.replace(nodeIdTag, "''")
							+ " limit 0";
					if (makeTimes) {
						propQuery = propQuery.replace(timeIdTag, "0");
					}
					ResultSet nodesCols = runQuery(propQuery);
					ResultSetMetaData nodesMeta = nodesCols.getMetaData();
					int numCols = nodesMeta.getColumnCount() + 1; // +1 for
																	// alpha id
																	// added
																	// earlier
					// put the col names in the list of headers
					for (int h = 1; h < numCols; h++) {
						nodeHeaders.add(nodesMeta.getColumnLabel(h));
					}
					nodesCols.close();
					// loop over each id
					int rowCounter = 0;
					Iterator<String> nodeIter = nodeIds.iterator();
					while (nodeIter.hasNext()) {
						Iterator<String> timeIter = timeset.iterator();
						String id = nodeIter.next();
						//loop over each time (default is just once) 
						while (timeIter.hasNext()) {
							String time = timeIter.next();
							// replace the id into the query
							propQuery = nodePropsQuery.replace(nodeIdTag, id);
							if (makeTimes) {
								propQuery = propQuery.replace(timeIdTag, time);
							}
							// and execute the node attr query
							ResultSet nodeProps = runQuery(propQuery);
							if (nodeProps.next()) { // wierd 'cause we check and
													// advance counter at the
													// same time
								String[] row = new String[numCols];
								row[0] = id;
								for (int i = 1; i < numCols; i++) {
									row[i] = nodeProps.getString(i);
								}
								nodeData.add(row);
							} else { // no data for this node, so what do we
										// do?
								// add a blank row
								String[] row = new String[numCols];
								row[0] = id;
								nodeData.add(row);
								DataProblem prob = new DataProblem(
										DataProblem.NODES,
										DataProblem.NO_DATA_FOR_NODE_ID,
										"query returned no property for nodeId '"
												+ id + "'", rowCounter,
										propQuery);
								prob.setRef(row);
								problems.add(prob);
								// warn("query returned no property for nodeId
								// '"+id+"'");
							}
						} //end of time loop
						rowCounter++;
					}

				}
			} else {// end of auto generated node block
			// FOR NON-AUTO GENERATED NODES
				// execute the node query as passed
				nodeData.clear();
				ResultSet nodeProps = runQuery(nodePropsQuery);
				// read the headers
				ResultSetMetaData nodesMeta = nodeProps.getMetaData();
				int numCols = nodesMeta.getColumnCount();
				// put the col names in the list of headers
				for (int h = 1; h <= numCols; h++) {
					nodeHeaders.add(nodesMeta.getColumnLabel(h));
				}
				// now read the data
				while (nodeProps.next()) { // wierd 'cause we check and advance
											// counter at the same time
					String[] row = new String[numCols];
					for (int i = 1; i <= numCols; i++) {
						row[i - 1] = nodeProps.getString(i);
					}
					nodeData.add(row);
				}

			}

		} catch (Exception e) {
			error("running  query for nodes: " + e.getClass() + " "
					+ e.getMessage());
			return false;
		}

		formatSonFile();

		if (ui != null) {
			ui.showSonPreview(currentNet);
		}
		if (ui != null & problems.size() > 0) {
			ui.showProblem(null);
		}
		status("Done. (" + problems.size() + " problems)");
		return true;
	}

	private HashSet<String> getTimeset(ArrayList<String[]> arcData) {
		HashSet<String> times = new HashSet<String>();
		// figure out if there are start and end times
		int startIndex = arcHeaders.indexOf("StartTime");
		int endIndex = arcHeaders.indexOf("EndTime");
		Iterator<String[]> rowIter = arcData.iterator();
		while (rowIter.hasNext()) {
			String[] row = rowIter.next();
			if (startIndex >= 0) {
				times.add(row[startIndex]);
			}
			if (endIndex >= 0) {
				times.add(row[endIndex]);
			}
		}

		return times;
	}

	public void validateData() {
		status("Validating data...");
		// recompute the set of node ids to match what is in the node data
		problems.clear();
		nodeIds.clear();
		int rowCount = 0;
		Iterator<String[]> nodeIter = nodeData.iterator();
		while (nodeIter.hasNext()) {
			String[] row = nodeIter.next();
			nodeIds.add(row[0]);

			// check for blank fields
			for (int i = 0; i < row.length; i++) {
				if (row[i] == null || row[i].equals("")) {
					DataProblem prob = new DataProblem(DataProblem.NODES,
							DataProblem.BLANK_FIELD, "Row " + rowCount
									+ " contains a blank or null field",
							rowCount, "");
					prob.setRef(row);
					problems.add(prob);
					break;
				}
			}
			rowCount++;
		}
		// check for arcs refering to missing nodes
		Iterator<String[]> arcIter = arcData.iterator();
		rowCount = 0;
		while (arcIter.hasNext()) {
			String[] row = arcIter.next();
			if (!nodeIds.contains(row[fromCol])) {
				DataProblem prob = new DataProblem(DataProblem.ARCS,
						DataProblem.NO_NODE_FOR_FROM_ID, "FromId '"
								+ row[fromCol]
								+ "' is not present in the set of nodeids",
						rowCount, "");
				prob.setRef(row);
				problems.add(prob);
			}
			if (!nodeIds.contains(row[toCol])) {
				DataProblem prob = new DataProblem(DataProblem.ARCS,
						DataProblem.NO_NODE_FOR_TO_ID, "ToId '" + row[toCol]
								+ "' is not present in the set of nodeids",
						rowCount, "");
				problems.add(prob);
				prob.setRef(row);
			}
			// check for blank fileds
			for (int i = 0; i < row.length; i++) {
				if (row[i] == null | row[i].equals("")) {
					DataProblem prob = new DataProblem(DataProblem.ARCS,
							DataProblem.BLANK_FIELD, "Row " + rowCount
									+ " contains a blank or null field",
							rowCount, "");
					prob.setRef(row);
					problems.add(prob);
					break;
				}
			}
			rowCount++;
		}
		status("Data validation found " + problems.size() + " problems.");
	}

	public void filterData(Object[] filters) {
		status("Running " + filters.length + " filters...");
		// for each filter on the list
		for (int f = 0; f < filters.length; f++) {
			CleaningFilter filter = (CleaningFilter) filters[f];
			// pass the filter the data and the list of problems
			if (filter.isActive()) {
				status("   " + filter.getName());
				// don't know if it likes nodes or arcs, so do both
				if (filter.likesArcs()) {
					filter.process(arcData, problems, true);
				}
				if (filter.likesNodes()) {
					filter.process(nodeData, problems, false);
				}
			}
		}
		validateData();
		// updatethe preview also
		formatSonFile();
		if (ui != null) {
			ui.showSonPreview(currentNet);
		}
		if (ui != null & problems.size() > 0) {
			ui.showProblem(null);
		}
	}

	/**
	 * processes the stored network attributes into the appropriate .son
	 * formatted text
	 * 
	 */
	public void formatSonFile() {
		StringBuffer buffer = new StringBuffer();
		// comments?
		buffer.append("//.son file generated from database using sonG\n");
		// print node headers
		buffer.append(nodeHeaders.get(0));
		for (int h = 1; h < nodeHeaders.size(); h++) {
			buffer.append("\t" + nodeHeaders.get(h));
		}
		buffer.append("\n");
		// print nodes
		Iterator<String[]> nodeIter = nodeData.iterator();
		while (nodeIter.hasNext()) {
			String[] row = nodeIter.next();
			buffer.append(row[0]);
			for (int j = 1; j < nodeHeaders.size(); j++) {
				buffer.append("\t" + row[j]);
			}
			buffer.append("\n");
		}
		// print arc headers
		buffer.append(arcHeaders.get(0));
		for (int h = 1; h < arcHeaders.size(); h++) {
			buffer.append("\t" + arcHeaders.get(h));
		}
		buffer.append("\n");
		// print arc data
		Iterator<String[]> arcIter = arcData.iterator();
		while (arcIter.hasNext()) {
			String[] row = arcIter.next();
			buffer.append(row[0]);
			for (int j = 1; j < arcHeaders.size(); j++) {
				buffer.append("\t" + row[j]);
			}
			buffer.append("\n");
		}
		// store the resulting text
		currentNet = buffer.toString();
	}

	/**
	 * open a connection to the database and store it, closes connection if
	 * already open
	 * 
	 * @param host
	 * @param usr
	 * @param pw
	 * @return
	 */
	public boolean connectToDB(String host, String db, String usr, String pwd) {

		// if there is already a connection, close it
		if (db != null) {
			closeDB();
		}

		try {
			Class.forName(jdbcDriver).newInstance();
		} catch (Exception e) {
			error("creating driver: " + e.getClass() + ": " + e.getMessage()
					+ " is the appropriate driver library installed?");
			return false;
		}

		String hostString = hostBase + host + "/" + db + hostSuffix; // "jdbc:mysql://localhost:3306/"
		try {
			status("Connecting to database " + hostString + " using "
					+ jdbcDriver + "...");
			currentDB = DriverManager.getConnection(hostString, usr, pwd);
			status("Connected to " + currentDB.getMetaData().getURL());
		} catch (Exception e) {
			error("connecting to database: " + e.getMessage());
		}
		return true;
	}

	public void closeDB() {
		if (currentDB != null) {
			try {
				currentDB.close();
				status("closed connection to DB");
			} catch (SQLException e) {
				error("closing connection: " + e.getMessage());
			}
		}
		currentDB = null;
	}

	/**
	 * execute an sql statement on a database
	 * 
	 * @param query
	 */
	public ResultSet runQuery(String queryStr) {
		if (currentDB == null) {
			error("no current database connection open");
		} else {
			if (query == null) {
				// create a new statement object for executing queries
				try {
					query = currentDB.createStatement();
				} catch (SQLException e) {
					error("creating query statement: " + e.getMessage());
				}
			}
			// execute the query
			try {
				return query.executeQuery(queryStr);
			} catch (SQLException e) {
				error(e.getMessage() + " executing query:" + queryStr);
			}
		}
		return null;
	}

	/**
	 * save out the currently constructed network data as a .son file
	 * 
	 * @param fileName
	 */
	public void saveAsSon(String fileName) {
		File outfile = new File(fileName);
		// make new outputstream
		FileWriter outWriter;
		try {
			outWriter = new FileWriter(outfile);
			// make new printwrinter
			PrintWriter outPrinter = new PrintWriter(new BufferedWriter(
					outWriter), true);
			outPrinter.print(currentNet);
			outPrinter.flush();
			outPrinter.close();
		} catch (IOException e) {
			error("unable to write .son file " + outfile.getAbsolutePath()
					+ ":" + e.getMessage());
		}
		status("Saved file as " + outfile.getAbsolutePath());
	}

	public void status(String status) {
		if (ui != null) {
			ui.showStatus(status);
		} else {
			System.out.println(status);
		}
	}

	public void error(String error) {
		if (ui != null) {
			ui.showStatus("\nERROR: " + error + "\n");
		} else {
			System.out.println("\nERROR:" + error + "\n");
		}
	}

	public void warn(String warning) {
		if (ui != null) {
			ui.showStatus("warning: " + warning);
		} else {
			System.out.println("warning:" + warning + "\n");
		}
	}

	public String resultAsString(ResultSet set) {
		String result = "";
		try {
			ResultSetMetaData meta = set.getMetaData();
			int ncol = meta.getColumnCount();
			// get the col names
			for (int i = 1; i <= ncol; i++) {
				result += meta.getColumnName(i) + "\t";
			}
			result += "\n";
			// loop over the rows
			while (set.next()) {
				// loop over cols
				for (int i = 1; i <= ncol; i++) {
					result += set.getString(i) + "\t";
				}
				result += "\n";
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * runs the .son formated text through the .son parser to see if it will
	 * throw errors
	 * 
	 */
	public void validateSon() {
		status("Validating .son output...");
		// create a file and write out to it
		DotSonParser parser = new DotSonParser();
		try {
			String tempFile = File.createTempFile("temp", ".son")
					.getAbsolutePath();
			saveAsSon(tempFile);
			parser.parseNetwork(tempFile);
			// TODO:delete the temp file
			status("Validation parsed " + parser.getNumNodeEvents()
					+ " node events and " + parser.getNumArcEvents()
					+ " arc events, no errors found");
		} catch (IOException e) {
			status("validation problem: " + e.getMessage());
			int lineNum = parser.getLastLineNum();
			if (ui != null) {
				ui.showSonPreview(currentNet, lineNum);
			}
		}
	}

	/**
	 * returns a list of all the know filters
	 * 
	 * @return
	 */
	public ArrayList<CleaningFilter> getAllFilters() {
		return knownFilters;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Getter song = new Getter();
		song.status("Launched SonG");
		System.exit(1);

	}

	public ArrayList<String[]> getArcData() {
		return arcData;
	}

	public ArrayList<String> getArcHeaders() {
		return arcHeaders;
	}

	public ArrayList<String[]> getNodeData() {
		return nodeData;
	}

	public ArrayList<String> getNodeHeaders() {
		return nodeHeaders;
	}

	public ArrayList<DataProblem> getProblems() {
		return problems;
	}

}
