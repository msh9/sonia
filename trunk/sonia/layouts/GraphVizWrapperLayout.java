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

	private String gvPath = "/usr/local/bin/dot";

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
	 * From GV docs: An aspect ratio, double, followed optionally by a ',' and a
	 * maximum pass count. If the aspect ratio is given, but no maximum pass
	 * count, the latter defaults to 5. Target aspect ratio (width of the layout
	 * divided by the height) of the graph drawing. If unset, dot minimizes the
	 * total edge length. For certain graphs, like those with large fan-in or
	 * fan-out, this can lead to very wide layouts. Setting aspect will cause
	 * dot to try to adjust the layout to get an aspect ratio close to that
	 * specified by aspect.
	 * 
	 * By default, dot will do 5 passes attempting to achieve the desired aspect
	 * ratio. For certain graphs, more passes will be needed to get close
	 * enough. The aspect attribute can also be used to specify the maximum
	 * number of passes to try.
	 * 
	 * At present, there is no mechanism for widening a very tall layout. Also,
	 * the algorithm doesn't handle clusters, nor disconnected graphs. For the
	 * latter case, one can split the pipeline ccomps -x | dot | gvpack | neato
	 * -n2 to get a similar effect.
	 */
	private String aspect;

	/**
	 * Factor by which to scale up the layout results
	 */
	public static final String SCALE_FACTOR = "scale factor";

	public static final String RANK_DIR = "rankdir";

	public static final String RANK_SEP = "ranksep";

	public static final String RANK = "rank";

	public static final String NODE_SEP = "nodesep";

	private Process gvPrc = null;

	public GraphVizWrapperLayout(SoniaController cont, SoniaLayoutEngine eng) {
		control = cont;
		engine = eng;
		// check if we can access graphViz by printing version or fail fast
		String[] cmd = { gvPath, "-V" };
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
				control
						.showError("Error with GraphViz external process at "
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
		runExternalGV(graphString, sliceXCoords, sliceYCoords);

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

	private String getGVFileString(DoubleMatrix2D matrix, IntArrayList present) {
		String gv = "digraph G {\n";
		// write in the graph params
		String gParams = "graph [" + RANK_SEP + "=\"" + rankSep + "\","
				+ RANK_DIR + "=\"" + rankDir + "\", " + NODE_SEP + "=\""
				+ nodeSep + "\"];\n";
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
			String[] cmd = { gvPath, "-Tplain", "-y", "-s" };
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
			System.out.println(graphText);

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

	}

}
