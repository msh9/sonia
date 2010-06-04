package sonia.layouts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;

import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import mdsj.MDSJ;
import mdsj.StressMinimization;
import sonia.CoolingSchedule;
import sonia.LayoutSlice;
import sonia.LayoutUtils;
import sonia.NetUtils;
import sonia.SoniaController;
import sonia.SoniaLayoutEngine;
import sonia.settings.ApplySettings;
import sonia.ui.ApplySettingsDialog;

/**
 * This layout wraps an external call to the GraphViz dot or neato layout
 * program http://www.graphviz.org/. GraphViz must be installed and accessible
 */
public class GraphVizWrapperLayout implements NetLayout {

	private SoniaController control;

	private SoniaLayoutEngine engine;

	private String layoutInfo;

	private ApplySettings settings;

	private String gvParams;

	private double xOffset;
	private double yOffset;
	private double scaleFactor = 20;

	private String gvPath = ""; // "/usr/local/bin/dot";

	/**
	 * From GV docs: In dot, this gives the desired rank separation, in inches.
	 * This is the minimum vertical distance between the bottom of the nodes in
	 * one rank and the tops of nodes in the next. If the value contains
	 * "equally", the centers of all ranks are spaced equally apart. Note that
	 * both settings are possible, e.g., ranksep = "1.2 equally" Can also be a
	 * list of values: A colon-separated list of doubles: d(:d)* where each d is
	 * a double. .
	 */
	private String rankSep = "1";

	/**
	 * Minimum space between two adjacent nodes in the same rank, in inches.
	 */
	private String nodeSep = "0.25";

	/**
	 * From GV docs: "TB", "LR", "BT", "RL", corresponding to directed graphs
	 * drawn from top to bottom, from left to right, from bottom to top, and
	 * from right to left, respectively. Sets direction of graph layout. For
	 * example, if rankdir="LR", and barring cycles, an edge T -> H; will go
	 * from left to right. By default, graphs are laid out from top to bottom.
	 */
	private String rankDir = "TB";

	/**
	 * subgraph { rank = same; A; B; C; }
	 * 
	 * This (anonymous) subgraph specifies that the nodes A, B and C should all
	 * be placed on the same rank if drawn using dot. Rank constraints on the
	 * nodes in a subgraph. If rank="same", all nodes are placed on the same
	 * rank. If rank="min", all nodes are placed on the minimum rank. If
	 * rank="source", all nodes are placed on the minimum rank, and the only
	 * nodes on the minimum rank belong to some subgraph whose rank attribute is
	 * "source" or "min". Analogous criteria hold for rank="max" and
	 * rank="sink". (Note: the minimum rank is topmost or leftmost, and the
	 * maximum rank is bottommost or rightmost.)
	 */
	private String rank = "";

	/**
	 * Factor by which to scale up the layout results
	 */
	public static final String SCALE_FACTOR = "scale factor";

	public static final String RANK_DIR = "rankdir";

	public static final String RANK_SEP = "ranksep";

	public static final String RANK = "rank";

	public static final String NODE_SEP = "nodesep";

	/**
	 * Path to where the operating system has located the GraphViz executeables
	 */
	public static final String GV_PATH = "GraphViz path";

	private Process gvPrc = null;

	public GraphVizWrapperLayout(SoniaController cont, SoniaLayoutEngine eng) {
		control = cont;
		engine = eng;

		gvPath = "/usr/local/bin/";
		// check if we can access graphViz by printing version or fail fast
		String[] cmd = { gvPath + "dot", "-V" };
		try {
			gvPrc = Runtime.getRuntime().exec(cmd);

			BufferedReader stdOut = new BufferedReader(new InputStreamReader(
					gvPrc.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(
					gvPrc.getErrorStream()));

			// read the output from the command
			String gvOutput = "";
			String line;

			line = stdOut.readLine();

			while (line != null) {
				gvOutput += line + "\n";
				line = stdOut.readLine();
			}
			control.showStatus("GraphViz:" + gvOutput);
			control.log("GraphViz:" + gvOutput);

			// read any errors from the attempted command
			String errors = null;
			String err = stdError.readLine();
			while (err != null) {
				errors += err;
				err = stdError.readLine();
			}
			if (errors != null) {
				control.showError("Error with GraphViz external process at "
						+ gvPath + " :" + errors);
			}
			stdOut.close();
			stdError.close();

		} catch (IOException e) {
			control.log(e.getMessage());
			control.showError(e.getMessage());
		}

	}

	public void applyLayoutTo(LayoutSlice slice, int width, int height,
			ApplySettings settings) {
		this.settings = settings;
		// read out the settings and validate

		if (settings.getProperty(SCALE_FACTOR) == null) {
			control.showStatus("Layout setting for " + SCALE_FACTOR
					+ " not specified, using default of " + scaleFactor);
		} else {
			try {
				scaleFactor = Double.parseDouble(settings.getProperty(
						SCALE_FACTOR, scaleFactor + ""));
			} catch (Exception e) {
				control.showError("Unable to parse layout setting value of "
						+ SCALE_FACTOR + " : " + e);
			}
		}
		rankSep = settings.getProperty(RANK_SEP);
		rankDir = settings.getProperty(RANK_DIR);
		nodeSep = settings.getProperty(NODE_SEP);
		rank = settings.getProperty(RANK);
		gvPath = settings.getProperty(GV_PATH);

		xOffset = engine.getLayoutWidth() / 2;
		yOffset = engine.getLayoutWidth() / 2;

		slice.setLayoutFinished(false);
		double[] sliceXCoords = slice.getXCoords(); // slice's object
		double[] sliceYCoords = slice.getYCoords(); // modifications will move
		// nodes

		// run the mds calculation
		layoutInfo = "Starting GraphViz layout external system call";
		String graphString = getGVFileString(NetUtils.getMatrix(slice), slice
				.getPresentNodes());
		runExternalGV(getGVComponents(graphString),sliceXCoords,sliceYCoords);
		//runExternalGV(graphString, sliceXCoords, sliceYCoords);

		// normalize, recenter, dialate coordinates to fit display area
		// update the slice coords
		for (int i = 0; i < sliceXCoords.length; i++) {
			sliceXCoords[i] = (sliceXCoords[i] * scaleFactor) + xOffset;
			sliceYCoords[i] = (sliceYCoords[i] * scaleFactor) + yOffset;
			// debug
			// System.out.println(i+":"+sliceXCoords[i]+","+sliceYCoords[i] );
		}
		// LayoutUtils.rescalePositions(slice, engine.getLayoutWidth(),
		// engine.getLayoutHeight(), sliceXCoords, sliceYCoords, false);

		engine.finishLayout(settings, this, slice, engine.getLayoutWidth(),
				engine.getLayoutHeight());
		slice.setLayoutFinished(true);
		layoutInfo = "Finished call to extral GraphViz layout.";

	}
/**
 * send the graph to get components, reformat them into subgraph
 * @param graphText
 * @return
 */
	private String getGVComponents(String graphText) {
		String gvOutput = "";
		String graphHeader = "";
		try {
			String[] cmd = { gvPath+"ccomps", "-x" };

			gvPrc = Runtime.getRuntime().exec(cmd);

			BufferedReader stdOut = new BufferedReader(new InputStreamReader(
					gvPrc.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(
					gvPrc.getErrorStream()));

			// write the graph via std in
			BufferedWriter stdIn = new BufferedWriter(new OutputStreamWriter(
					gvPrc.getOutputStream()));
			stdIn.write(graphText);
			stdIn.flush();
			stdIn.close();

			// read the output from the command
			
			String line = stdOut.readLine();
			boolean skip = false;
			
			boolean graphHeaderDone = false;
			while (line != null) {
				if (line.startsWith("digraph")) {
					//start skipping the graph part
					gvOutput += "subgraph cluster_"+line.substring(10,line.length()-2)+"{\n";
					skip = true;
				}
				if (!skip){
					gvOutput += line + "\n";
				} else if (line.endsWith("];")){
					//reached the end of the graph block, so stop skipping
					skip=false;
					if (!graphHeaderDone){
						graphHeader+="];\n";
						graphHeaderDone=true;
					}
				} else if (!graphHeaderDone){
					//we must be dealing with the graph header
					graphHeader += line;
				}
				
				line = stdOut.readLine();
			}


			// read any errors from the attempted command
			String errors = null;
			String err = stdError.readLine();
			while (err != null) {
				errors += err;
				err = stdError.readLine();
			}
			if (errors != null) {
				control.showError("Errors with GraphViz external process:"
						+ errors);
			}
			stdOut.close();
			stdError.close();
			control.showStatus("GraphViz process finished with status: "
					+ gvPrc.waitFor());

		} catch (IOException e) {
			control.showError("Errors with GraphViz external process:"
					+ e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			control.showError("Errors with GraphViz external process:"
					+ e.getMessage());
			e.printStackTrace();
		}
		
		//assemble the graph headers with subgraphs
		gvOutput = graphHeader+gvOutput+"};";
		return gvOutput;

	}

	private String getGVFileString(DoubleMatrix2D matrix, IntArrayList present) {
		String gv = "digraph G {\n";
		// write in the graph params
		String gParams = "graph [" + RANK_SEP + "=\"" + rankSep + "\","
				+ RANK_DIR + "=\"" + rankDir + "\", " + NODE_SEP + "=\""
				+ nodeSep + "\"];\n";
		// check if we using a rank to specify position of subgraph
		if (!rank.equals("")) {
			// subgraph { rank = same; A; B; C; }
			gParams += "subgraph { " + RANK + " =" + rank + "}\n";
		}
		layoutInfo += gParams;
		gv += gParams;
		// write in node records to give the existing positions and include
		// isolates
		for (int i = 0; i < present.size(); i++) {
			gv += "\t" + present.getQuick(i) + ";\n";
		}

		// create arc records
		for (int i = 0; i < matrix.rows(); i++) {
			for (int j = 0; j < matrix.columns(); j++) {
				if (matrix.getQuick(i, j) != 0) {
					gv += "\t" + i + " -> " + j + ";\n";
				}
			}
		}
		gv += "};\n";
		return gv;
	}

	private void runExternalGV(String graphText, double[] sliceXCoords,
			double[] sliceYCoords) {
		try {

			// -y flips the coords to match the computer convention
			// -s should scale like 72 dpi
			String[] cmd = { gvPath+"dot", "-Tplain", "-y", "-s" };

			gvPrc = Runtime.getRuntime().exec(cmd);

			BufferedReader stdOut = new BufferedReader(new InputStreamReader(
					gvPrc.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(
					gvPrc.getErrorStream()));

			// write the graph via std in
			BufferedWriter stdIn = new BufferedWriter(new OutputStreamWriter(
					gvPrc.getOutputStream()));
			stdIn.write(graphText);
			stdIn.flush();
			stdIn.close();

			// read the output from the command
			String gvOutput = "";
			String line = stdOut.readLine();
			while (line != null) {
				gvOutput += line + "\n";
				// if line startes with "node", read out the coords
				if (line.startsWith("node")) {
					String[] tokens = line.split(" ");
					int id = Integer.parseInt(tokens[1]);
					double x = Double.parseDouble(tokens[2]);
					double y = Double.parseDouble(tokens[3]);
					sliceXCoords[id] = x;
					sliceYCoords[id] = y;
				}

				line = stdOut.readLine();
			}


			// read any errors from the attempted command
			String errors = null;
			String err = stdError.readLine();
			while (err != null) {
				errors += err;
				err = stdError.readLine();
			}
			if (errors != null) {
				control.showError("Errors with GraphViz external process:"
						+ errors);
			}
			stdOut.close();
			stdError.close();
			control.showStatus("GraphViz process finished with status: "
					+ gvPrc.waitFor());

		} catch (IOException e) {
			control.showError("Errors with GraphViz external process:"
					+ e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			control.showError("Errors with GraphViz external process:"
					+ e.getMessage());
			e.printStackTrace();
		}

	}

	public void disposeLayout() {
		if (gvPrc != null) {
			gvPrc.destroy();
		}
	}

	public String getLayoutInfo() {
		return layoutInfo;
	}

	public String getLayoutType() {
		return "GraphViz Tree (dot) Layout";
	}

	public void pause() {
		if (gvPrc != null) {
			gvPrc.destroy();
		}

	}

	public void resume() {
		// TODO Auto-generated method stub

	}

	public void setupLayoutProperties(ApplySettingsDialog settings) {
		settings.addLayoutProperty(SCALE_FACTOR, scaleFactor);
		settings.addLayoutProperty(RANK_DIR, rankDir);
		settings.addLayoutProperty(RANK_SEP, rankSep);
		settings.addLayoutProperty(NODE_SEP, nodeSep);
		settings.addLayoutProperty(RANK, rank);
		settings.addLayoutProperty(GV_PATH, gvPath);

	}

}
