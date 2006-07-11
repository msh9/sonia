package sonia.layouts;

import java.util.*;
import java.lang.Math;

import sonia.ApplySettingsDialog;
import sonia.CoolingSchedule;
import sonia.LayoutSlice;
import sonia.LayoutUtils;
import sonia.NetLayout;
import sonia.NetUtils;
import sonia.SoniaController;
import sonia.SoniaLayoutEngine;
import sonia.Subnet;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

/**
 * <p>Title:SoNIA (Social Network Image Animator) </p>
 * <p>Description:Animates layouts of time-based networks
 * <p>Copyright: CopyLeft  2004: GNU GPL</p>
 * <p>Company: none</p>
 * @author Skye Bender-deMoll unascribed
 * @version 1.1
 */

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
/**
 * Implements a modified version of the Kamada-Kawai spring-embedder layout
 * algorithm. (Kamada, K., Kawai, S. (1989) <CITE>An Algorithm for Drawing
 * General Undirected Graphs.</CITE> Information Processing Letters 31, 7-15)
 * this version is modified in several ways from the original. It give the users
 * much more explicit control over stopping times and energy minimization. It is
 * also configured to break networks of multiple components into individiual
 * networks and run the algorithm independently. In additon to stopping when the
 * specified minimum energy is reached, the algorithm also stops when a set
 * number of passes (determined by the schedule) have been completed, or when
 * the layout cesease to converge. (more about kk) <BR>
 * The main loop of the algorithm runs on its own "layout runner" thread so that
 * it can update the display, pause, etc. The two "regular" KK loops are
 * embedded in a third "epsilon minimizing" loop which gradually increases the
 * minimuim energy target the KK loop is trying to achive. The advantage of
 * doing this incrementally is that if the loop stops short, either because the
 * desired optimization is impossible in two dimensions, or by exceeding the
 * maximum number of iterations, at least some optimization will have been
 * performed. <BR>
 * <BR>
 * What makes this slightly confusing is that, unlike the FR layout, the cooling
 * schedule does not directly linked to the number of primary passes through the
 * algorithm. Instead, it controlls the fraction by which the target epsilon is
 * reduced each time the main KK loop reaches its goal. The initial value is
 * that of the worst-positioned node when the layout begins. Obviously, this
 * could use some conceptual improvement. <BR>
 * <BR>
 * LAYOUT PARAMETERS:
 * </P>
 * <P>
 * "optimum dist" - the desired distance (in pixels) for nodes connected by an
 * arc of weight one. (Essentialy scales the layout)
 * </P>
 * <P>
 * "min epsilon" - sets the final target energy value for energy minimization.
 * Smaller values will give better layouts, but you may need to increase the
 * number passes, and it will take longer.
 * </P>
 * <P>
 * "springConst" - not sure what this does. How "springy" the springs are?
 * </P>
 * <P>
 * "cool factor" - how much the target energy will be reduced in each pass
 * through the "epsilon minimizing" loop.
 * </P>
 * 
 * LONGER DISCUSSION OF THE KK ALGORITHM:
 * <P>
 * The Kamada-Kawai algorithm is commonly described as a "spring-embedder,"
 * meaning that it fits with a general class of algorithms that represent a
 * network as a virtual collection of weights (nodes) connected by springs
 * (arcs) with a degree of elasticity and a desired resting length. The problem
 * then is to reposition the nodes until all the springs are as relaxed as
 * possible. Kamada and Kawai's version utilized simple force equations and a
 * Newton-Raphson steepest decent approach to locating the optimal
 * configuration. In addition, they perform an All-Pairs-Shortest-Path distance
 * calculation to determine the desired spring lengths between nodes.
 * Interestingly, because the KK algorithm is working to optimize distances
 * between every pair of nodes, this makes it conceptually similar to a
 * non-metric multidimensional scaling approach. Because we have adapted the
 * algorithm to work with weighted di-graphs and the possibility of multiple
 * components, some transformations (transformation to dissimilarity,
 * symetrization) are usually necessary. When there are disconnected components
 * in the graph, the algorithm is run independently on each.
 * </P>
 * <P>
 * The original KK algorithm works as follows: First, the algorithm calculates
 * the "energy" (sum of spring tension) for each node. It then loops over all
 * the nodes to find the one with the highest energy (worst positioning), and
 * begins iterating the Newton-Raphson stage to compute new positions for the
 * node until its energy is below epsilon (the target parameter). At this point,
 * it again looks for the node with the highest energy and begins moving it.
 * This process continues until there is no node with energy above epsilon, and
 * then the algorithm is done.
 * </P>
 * <P>
 * This procedure needs several extensions to work for our purposes. To get an
 * optimal layout, the epsilon parameter must be set to the minimum (lowest
 * stress) possible for the graph, but there is no simple way of determining
 * this before running the algorithm. If the value is set too low for a
 * particular graph, the algorithm will not stop unless a maximum number of
 * iterations is exceeded. But this gives no guarantee that the user will
 * acquire a good layout. It is possible for the layout to enter a loop, where
 * positioning one node worsens the position of second, which is then
 * repositioned affecting the first, and so on. (if this causes the layout to
 * exceed the "max passes" parameter, the layout will "bonk" and record an
 * error) In addition, the process of deterministically moving the "worst" node
 * to the best position and then re-evaluating it can be a problematic means of
 * exploring the search space. It is generally more effective to gradually
 * optimize the entire graph.
 * </P>
 * <P>
 * SoNIA's version of KK attempts to implement a gradual decent approach to the
 * minimization. The target epsilon is initially set to the energy of the worst
 * node. When the energy of each node in the layout is better than this
 * parameter, the target value is improved (reduced by a fraction, the "cooling
 * factor" parameter) and the algorithm continues until the layout is better
 * than a global "minimum epsilon". This ensures that if the layout exceeds the
 * maximum number of iterations or lands in a loop, at least a basic level of
 * optimization will have occurred. Informal testing seems to show this
 * procedure arriving at better layouts with fewer iterations. In SoNIA,
 * information on algorithm convergence is provided in the "Cooling Schedule"
 * window by plotting the difference between node energies after each pass.
 * </P>
 * <P>
 * A well behaved layout will show a point cloud smoothly converging on the
 * minimum value. This makes it possible to assess whether the parameters should
 * be changed so as to allow additional iterations in difficult layouts. This
 * suggests the possibility of building a "smarter" algorithm which stops when
 * additional iterations no longer give any improvement, but this will require
 * additional testing and parameter tuning.
 * </P>
 * <P>
 * KK was originally intended for undirected, non-weighted, fully connected
 * graphs. We took a simplistic extension to weighted graphs, symmetrizing the
 * input digraph and using an All-Pairs-Shortest-Path algorithm to give a matrix
 * of graph distances which are multiplied by an "optimal distance" parameter to
 * give a desired screen distance. To deal with the possibility of disconnected
 * components, the algorithm is run independently on each (strong) component
 * with more than one node. This means that there is nothing explicitly
 * separating the components, and they sometimes overlap on screen. Several
 * people have reported some success using a "phantom network" to minimally
 * connect the graph in order to avoid the component positioning problem. This
 * approach sounds promising but needs some additional thought and testing.
 * </P>
 * <P>
 * Kamada and Kawai argue that their layouts are not influenced by initial
 * conditions, but this may be because their work focused on fairly simple
 * graphs. We have found that for many graphs the results are highly dependent
 * on initial conditions. This seems to be especially true for graphs which are
 * dominated by a "star" configuration. This means that there are many nodes
 * with similar connections and their placement in relation to each other is
 * mostly the result of path dependence in the layout procedure - there is no
 * information to select between equivalent layouts. To put it in another way,
 * graphs with "truss-like" structures tend to be stable and graphs with lots of
 * points are more flexible, having multiple "isomorphs" with similar stress
 * levels. Graphs with this property can be tested by running the layout several
 * times from random initial positions.
 * 
 * It is in fact the algorithm we have had the most success with, perhaps
 * because it is simply the algorithm we have devoted the most time too, or
 * because it is better suited to the concepts of social space we are using.
 * There are several strong advantages to the KK algorithm. One is that the
 * Newton-Raphson optimization procedure is completely deterministic - given
 * parameters and starting positions, it will arrive at the same solution on the
 * same graph. This is convenient when working with a sequence of time-based
 * networks. If we start the layout from the previous slice's node coordinates
 * and there is no change in the arc weights, then there should be no change in
 * node positions. In addition, if there are small changes in weights between
 * the layouts, or a small number of arcs added or deleted, then KK will likely
 * find new minimum that is close in coordinate space to the previous solution.
 * This minimizes node movement and generates layouts that appear very stable
 * over time. One drawback to KK is that the energy calculations in the
 * Newton-Raphson procedure are computationally expensive.
 * </P>
 * 
 */
public class MultiCompKKLayout implements NetLayout, Runnable {
	private SoniaController control;

	private SoniaLayoutEngine engine;

	private int pad = 20;

	// private int initialIter = 10; //number of loops before cooling starts
	private int maxPasses = 500; // maximum number of loops through the Fruch
									// layout procedure

	private int passes;

	private double optDist; // optimal distance for nodes, gets reset later in
							// code

	private double springConst = 1; // K in KK paper (avg. i,j distance?)

	private double minEpsilon = 1; // target deltaM goal

	private boolean animate = true; // whether to animate the transitions

	private boolean firstLayout = true;

	private boolean noBreak = true;

	private double width;

	private double height;

	private CoolingSchedule schedule;

	private LayoutSlice slice;

	private LayoutUtils utils;

	private ApplySettingsDialog settings;

	private String layoutInfo = "";

	private double[] sliceXCoords;

	private double[] sliceYCoords;

	/**
	 * Instantiates the layout with reference to the main SoniaController and
	 * the the SoniaLayoutEngine connected to this layout
	 * 
	 * @param cont
	 *            the main controller
	 * @param eng
	 *            the layoutEngine
	 */
	public MultiCompKKLayout(SoniaController cont, SoniaLayoutEngine eng) {
		control = cont;
		engine = eng;
		schedule = new CoolingSchedule(2);
		control.showFrame(schedule);
	}

	/**
	 * Attaches algorithm-specific parameter names and default values to the
	 * ApplySettingsDialog box. Parameters added are:<BR>
	 * "optimum distance" - the desired distance in pixels between nodes of
	 * distance 1<BR>
	 * "min epsilon" - the stopping "energy" of the layout, how far it will try
	 * to optimise<BR>
	 * "springConst" - the spring constant from KK, roughly how hard nodes try
	 * to jump to positions
	 * 
	 * @param settings
	 *            the dialog the parameters will be added to
	 */
	public void setupLayoutProperties(ApplySettingsDialog settings) {
		settings.addLayoutProperty("optimum dist", 20);
		settings.addLayoutProperty("min epsilon", 1.0);
		settings.addLayoutProperty("springConst", 1.0);
		settings.addLayoutProperty("cool factor", 0.25);
		settings.addLayoutProperty("comp connect value", 0.0);
		settings.addLayoutProperty("max passes",1000);
		//TODO:  make layout read max passes from settings and show in cooling schedule
	}

	/**
	 * Gets a few settings and starts the layoutRunner thread.
	 * 
	 * @param s
	 *            the slice the layout will be performed on
	 * @param w
	 *            width of the layout area (in pixels)
	 * @param h
	 *            height of the layout are (in pixels)
	 * @param set
	 *            settings dialog containing the values of the settings.
	 */
	public void applyLayoutTo(LayoutSlice s, int w, int h,
			ApplySettingsDialog set) {
		slice = s;
		settings = set;
		maxPasses = schedule.getMaxUsrPasses();
		width = w;
		height = h;
		layoutInfo = "";
		// arrays for quick acess to node coords
		sliceXCoords = slice.getXCoords(); // has the real slice's object
		sliceYCoords = slice.getYCoords();
		// start algorthem on new thread so that it can be paused, etc
		Thread layoutRunner = new Thread(this, "KKLayout loop");
		layoutRunner.setPriority(10);
		control.showStatus("Layout thread running..");
		layoutRunner.start();
	}

	/**
	 * Runs the KK layout algorithm. First it symetrizes the network to max
	 * values and then breaks it into components. Then it converts each of the
	 * networks into Subnets (to perserve the relations between old and new
	 * indexes, ids, etc) and runs KKLoop() on all the subnets with more than 1
	 * node. Asks the engine to finishLayout() when done. Should only be called
	 * internaly when the thread starts.
	 */
	public void run() {
		// make sure layout is restarted if if it was stopped by error
		noBreak = true;
		// test code to connect with phtom links
		double replaceWeight = settings.getLayoutProperty("comp connect value");
		if (replaceWeight > 0) {
			IntArrayList includeAll = new IntArrayList(slice.getMaxNumNodes());
			for (int i = 0; i < slice.getMaxNumNodes(); i++) {
				includeAll.add(i);
			}
			Subnet subnet = new Subnet(NetUtils.getSymMaxMatrix(slice),
					includeAll);
			// get Matrix from subnet and make it into disimliarity
			// (matrix was symetrized when divided into components)
			// using the max and min value of all matricies in engine
			// then sets up the matrix of path distances with Dijkstras APSP
			DenseDoubleMatrix2D distMatrix = NetUtils
					.getAllShortPathMatrix(NetUtils.getReverse(subnet
							.getMatrix(), engine.getMaxMatrixVal(), engine
							.getMinMatrixValue()));
			// repalce the infinities with the replace Weight
			//should be a more efficent way
			//debug
			System.out.println("replaceing infinate distances with "+replaceWeight);
			for (int i = 0; i < distMatrix.rows(); i++) {
				for (int j = 0; j < distMatrix.columns(); j++) {
					if (distMatrix.getQuick(i,j)== Double.POSITIVE_INFINITY){
						distMatrix.setQuick(i,j,replaceWeight);
					}
				}
			}
			KKLoop(subnet, distMatrix);

		} else {
			// set up components to process independently
			ArrayList components = NetUtils.getComponents(NetUtils
					.getSymMaxMatrix(slice), true);
			Iterator compIter = components.iterator();
			while (compIter.hasNext()) {
				Subnet subnet = (Subnet) compIter.next();
				// Run the KK algorithm independently on each of the compondnets
				if (subnet.getNumNodes() > 1) {
					if (!noBreak) {
						break;
					}
					// get Matrix from subnet and make it into disimliarity
					// (matrix was symetrized when divided into components)
					// using the max and min value of all matricies in engine
					// then sets up the matrix of path distances with Dijkstras
					// APSP
					DenseDoubleMatrix2D distMatrix = NetUtils
							.getAllShortPathMatrix(NetUtils.getReverse(subnet
									.getMatrix(), engine.getMaxMatrixVal(),
									engine.getMinMatrixValue()));
					KKLoop(subnet, distMatrix);
				}

			}
		}

		engine.finishLayout(this, slice, width, height);

	}

	/**
	 * Gets the similarity matrix from the subnet, and converts it to
	 * disimilarity using the NetUtils.getReverse() method with the min and max
	 * values from the engine. Converts the result into a distance matrix with
	 * an All Pairs Shortest Path algorithm (Dijkstra's)
	 * NetUtils.getAllShortPathMatrix(). Gets the spring constant and uses
	 * calcKMatrix() to setup a matrix of forces. Gets the "optimum distance"
	 * parameters: the desired pixels distance between nodes. Gets the starting
	 * coordinates form the subnet. Calculates the "initial energy" of the
	 * network (subnet) <BR>
	 * <BR>
	 * The optimization loop proceeds as follows: First, the node with the
	 * highest energy (worst position) is located.
	 */
	private void KKLoop(Subnet subnet, DenseDoubleMatrix2D distMatrix) {
		int nNodes = subnet.getNumNodes();
		schedule.reset();

		// sets up kmatrix of forces (optimal [but not always achieveable]
		// energies?)
		springConst = settings.getLayoutProperty("springConst");
		DenseDoubleMatrix2D kMatrix = calcKMatrix(distMatrix, springConst);
		/*
		 * //calc desired distance between nodes double optDist =
		 * Math.min(width, height) / Math.max(NetUtils.calcDiameter(distMatrix),
		 * 1); //RECALCS ALLSHORTPAHS, BUT USE FOR NOW FOR COMPATIBLITY
		 */
		double optDist = settings.getLayoutProperty("optimum dist");
		minEpsilon = settings.getLayoutProperty("min epsilon");
		double coolFact = settings.getLayoutProperty("cool factor");
		// do these only apply to the last subnet run?
		layoutInfo = layoutInfo + "\noptimum distance: " + optDist;
		layoutInfo = layoutInfo + "\nminimum epsilon: " + minEpsilon;
		layoutInfo = layoutInfo + "\ncool factor: " + coolFact;
		// sets up lMatrix of distance between nodes pairs
		DenseDoubleMatrix2D lMatrix = calcLMatrix(distMatrix, optDist);
		// arrays for quick acess to node coords
		double[] xPos = subnet.getSubsetArray(slice.getXCoords());
		double[] yPos = subnet.getSubsetArray(slice.getYCoords());

		int numEdges = slice.getTotalSliceArcs();

		// calc value to start minimization from (should be based on previous?)
		// FIGURE OUT COOLING SCHEDULE
		// epsilon = (nNodes * numEdges)/2;
		// figure out the initial stat to compare to at the end
		double initialEnergy = getEnergy(lMatrix, kMatrix, xPos, yPos);
		layoutInfo = layoutInfo + "\ninitial KK energy: " + initialEnergy;
		// double epsilon = { initialEnergy / nNodes;

		// figure out which node to start moving first
		double deltaM;
		int maxDeltaMIndex = 0;
		double maxDeltaM = getDeltaM(0, lMatrix, kMatrix, xPos, yPos);
		for (int i = 1; i < nNodes; i++) {
			deltaM = getDeltaM(i, lMatrix, kMatrix, xPos, yPos);
			if (deltaM > maxDeltaM) {
				maxDeltaM = deltaM;
				maxDeltaMIndex = i;
			}
		}
		// try setting epsilon to the value of the worst node
		double epsilon = maxDeltaM;
		// show this as nrg on schedule
		schedule.setMaxYvalue(epsilon);

		int passes = 0;
		int subPasses = 0;
		// epsilon minimizing loop
		while ((epsilon > minEpsilon) & noBreak) {
			// show value on plot
			schedule.showYValue(epsilon);

			double previousMaxDeltaM = maxDeltaM + 1;
			// KAMADA-KAWAI LOOP: while the deltaM of the node with
			// the largest deltaM > epsilon..
			// ALSO BREAKS IF IT STOPS CONVERGING, set as param?
			while ((maxDeltaM > epsilon)
					& ((previousMaxDeltaM - maxDeltaM) > 0.1) & noBreak) {
				double[] deltas;
				double moveNodeDeltaM = maxDeltaM;
				double previousDeltaM = moveNodeDeltaM + 1;

				// KK INNER LOOP while the node with the largest energy >
				// epsilon...
				while ((moveNodeDeltaM > epsilon) && noBreak) {
					// show subpass on schedule
					schedule.showPassValue(subPasses);
					// also show convergance on schedule
					schedule.showConvergance(subPasses, moveNodeDeltaM);

					// get the deltas which will move node towards the local
					// minima
					deltas = getDeltas(maxDeltaMIndex, lMatrix, kMatrix, xPos,
							yPos);
					// set coords of node to old coords + changes
					xPos[maxDeltaMIndex] += deltas[0];
					yPos[maxDeltaMIndex] += deltas[1];
					previousDeltaM = moveNodeDeltaM;
					// recalculate the deltaM of the node w/ new vals
					moveNodeDeltaM = getDeltaM(maxDeltaMIndex, lMatrix,
							kMatrix, xPos, yPos);
					subPasses++;
					if (subPasses > maxPasses) {
						// break the loop, and tell us
						control.showError("KK inner loop exceeded max passes");
						layoutInfo = layoutInfo
								+ "\nKK inner loop exceeded max passes";
						slice.setError(true);
						noBreak = false;
					}
				}// end KK inner loop
				previousDeltaM = maxDeltaM;
				// recalculate deltaMs and find node with max
				maxDeltaMIndex = 0;
				maxDeltaM = getDeltaM(0, lMatrix, kMatrix, xPos, yPos);
				for (int i = 1; i < nNodes; i++) {
					deltaM = getDeltaM(i, lMatrix, kMatrix, xPos, yPos);
					if (deltaM > maxDeltaM) {
						maxDeltaM = deltaM;
						maxDeltaMIndex = i;
					}
				}

				// if set to update display, update on every nth pass
				if (settings.isRepaint() & (settings.getRepaintN() > 0)
						& (passes % settings.getRepaintN() == 0)) {
					// reset the appropriate values of the the slice coords to
					// the new subnet coords
					for (int i = 0; i < nNodes; i++) {
						sliceXCoords[subnet.getNetIndex(i)] = xPos[i];
						sliceYCoords[subnet.getNetIndex(i)] = yPos[i];
					}

					if (settings.isRecenter()) {
						utils.centerLayout(slice, (int) width, (int) height,
								sliceXCoords, sliceYCoords, settings
										.isIsolateExclude());
					}
					engine.updateDisplays();
				}
				passes++;
				// attempt to let redraws of other windows, pause, etc
				Thread.yield();
			}// end main KK loop

			// USE COOLING SCHEDULE HERE!!
			epsilon -= epsilon * coolFact; // default is 1/4
			// epsilon = epsilon * schedule.getTempFactor(passes);

			// reset the appropriate values of the the slice coords to
			// the new subnet coords
			for (int i = 0; i < nNodes; i++) {
				sliceXCoords[subnet.getNetIndex(i)] = xPos[i];
				sliceYCoords[subnet.getNetIndex(i)] = yPos[i];
			}
			// calc the stress WHAT ABOUT ISOLATES?
			// layoutInfo += "stress: "+NetUtils.getStress(slice)+"\n";
		}// end epsilon minimizing loop
	}

	/**
	 * returns a double[] x,y pair giving the position differnce for the node of
	 * index i, given the passed positions, distance, and spring matricies.
	 */
	private double[] getDeltas(int i, DenseDoubleMatrix2D lMatrix,
			DenseDoubleMatrix2D kMatrix, double[] xPos, double[] yPos) {
		// solve deltaM partial eqns to figure out new position for node of
		// index i
		// where deltaM is close to 0 (or less then epsilon)
		int nNodes = lMatrix.rows();
		double[] deltas = new double[2]; // holds x and y coords to return
		double dx, dy, dd;
		double deltaX, deltaY;
		double xPartial = 0;
		double yPartial = 0;
		double xxPartial = 0;
		double xyPartial = 0;
		double yxPartial = 0;
		double yyPartial = 0;
		for (int j = 0; j < nNodes; j++) {
			if (i != j) {
				dx = xPos[i] - xPos[j];
				dy = yPos[i] - yPos[j];
				dd = Math.sqrt(dx * dx + dy * dy);

				double kMatrixVal = kMatrix.getQuick(i, j);
				double lMatrixVal = lMatrix.getQuick(i, j);
				double ddCubed = dd * dd * dd;

				xPartial += kMatrixVal * (dx - lMatrixVal * dx / dd);
				yPartial += kMatrixVal * (dy - lMatrixVal * dy / dd);
				xxPartial += kMatrixVal * (1 - lMatrixVal * dy * dy / ddCubed);
				xyPartial += kMatrixVal * (lMatrixVal * dx * dy / ddCubed);
				yxPartial += kMatrixVal * (lMatrixVal * dy * dx / ddCubed);
				yyPartial += kMatrixVal * (1 - lMatrixVal * dx * dx / ddCubed);
			}
		}

		// calculate x and y position difference using partials
		deltas[0] = ((-xPartial) * yyPartial - xyPartial * (-yPartial))
				/ (xxPartial * yyPartial - xyPartial * yxPartial);
		deltas[1] = (xxPartial * (-yPartial) - (-xPartial) * yxPartial)
				/ (xxPartial * yyPartial - xyPartial * yxPartial);

		return deltas;
	}

	/**
	 * returns the energy of i (looping over all other nodes)
	 */
	private double getDeltaM(int i, DenseDoubleMatrix2D lMatrix,
			DenseDoubleMatrix2D kMatrix, double[] xPos, double[] yPos) {
		int nNodes = lMatrix.rows();
		double deltaM = 0;
		double xPartial = 0;
		double yPartial = 0;
		double dx, dy, dd;
		for (int j = 0; j < nNodes; j++) {
			if (i != j) {
				dx = xPos[i] - xPos[j];
				dy = yPos[i] - yPos[j];
				dd = Math.sqrt(dx * dx + dy * dy);
				double kMatrixVal = kMatrix.getQuick(i, j);
				double lMatrixVal = lMatrix.getQuick(i, j);
				xPartial += kMatrixVal * (dx - lMatrixVal * dx / dd);
				yPartial += kMatrixVal * (dy - lMatrixVal * dy / dd);
			}
		}
		// deltaM = sqrt(xPartial^2+yPartial^2)
		deltaM = Math.sqrt(xPartial * xPartial + yPartial * yPartial);
		return deltaM;
	}

	private double getEnergy(DenseDoubleMatrix2D lMatrix,
			DenseDoubleMatrix2D kMatrix, double[] xPos, double[] yPos) {
		int nNodes = lMatrix.rows();
		double energy = 0;
		double dx, dy, lij;
		int limit = nNodes - 1;
		// for all pairs..
		for (int i = 0; i < limit; i++) {
			for (int j = i + 1; j < nNodes; j++) {
				dx = xPos[i] - xPos[j];
				dy = yPos[i] - yPos[j];
				lij = lMatrix.getQuick(i, j);
				energy += 0.5
						* kMatrix.getQuick(i, j)
						* (dx * dx + dy * dy + lij * lij - 2 * lij
								* Math.sqrt(dx * dx + dy * dy));
			}
		}
		return energy;
	}

	/**
	 * set up matrix of spring forces between pairs using K/(d[i][j]^2)
	 */
	private DenseDoubleMatrix2D calcKMatrix(DenseDoubleMatrix2D distMatrix,
			double spring) {
		int nNodes = distMatrix.rows();
		DenseDoubleMatrix2D kMatrix = new DenseDoubleMatrix2D(nNodes, nNodes);
		for (int i = 0; i < nNodes; i++) {
			for (int j = 0; j < nNodes; j++) {
				double distMVal = distMatrix.getQuick(i, j);
				kMatrix.setQuick(i, j, (spring / (distMVal * distMVal)));
			}
		}
		return kMatrix;
	}

	/**
	 * set up matrix of desired edge lengths using L*d[i][j]
	 */
	private DenseDoubleMatrix2D calcLMatrix(DenseDoubleMatrix2D distMatrix,
			double optDist) {
		int nNodes = distMatrix.rows();
		DenseDoubleMatrix2D lMatrix = new DenseDoubleMatrix2D(nNodes, nNodes);
		for (int i = 0; i < nNodes; i++) {
			for (int j = 0; j < nNodes; j++) {
				lMatrix.setQuick(i, j, (optDist * distMatrix.getQuick(i, j)));
			}
		}
		return lMatrix;
	}

	/**
	 * Returns a String with the name of the kind of the layout
	 */
	public String getLayoutType() {
		return "Multiple component Kamada-Kawai layout";
	}

	/**
	 * Returns the layout info string for the log, which may contain information
	 * about starting and stopping values, etc.
	 */
	public String getLayoutInfo() {
		return layoutInfo;
	}

	/**
	 * Sets a flag to break the layout loop. Probably should be called "stop"
	 * since there is no way to restart the layout.
	 */
	public void pause() {
		noBreak = false;
	}

	/**
	 * un-pauses the layout by setting the break flag to false, but doesn't
	 * actually restart the layout.
	 */
	public void resume() {
		noBreak = true;
	}

	public void disposeLayout() {
		// need to get rid of layout settigns dialog?
		schedule.hide();
		schedule = null;
	}
}
