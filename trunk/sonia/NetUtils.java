package sonia;

import java.util.*;

import com.sun.corba.se.impl.orbutil.DenseIntMapImpl;

import cern.colt.matrix.impl.*;
import cern.colt.matrix.*;
import cern.colt.bitvector.BitMatrix;
import cern.colt.bitvector.BitVector;
import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;

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
 * Contains a set of static utility methods for performing network calculations
 * and matrix operations used in sonia.
 */
public class NetUtils {

	public NetUtils() {
	}

	/**
	 * creates and returns the adjacency matrix of arc weights in the passed
	 * slice
	 */
	public static SparseDoubleMatrix2D getMatrix(LayoutSlice slice) {
		int nNodes = slice.getMaxNumNodes();
		SparseDoubleMatrix2D sliceMatrix = new SparseDoubleMatrix2D(nNodes,
				nNodes);
		for (int i = 0; i < nNodes; i++) {
			for (int j = 0; j < nNodes; j++) {
				sliceMatrix.setQuick(i, j, slice.getArcWeight(i, j));
			}
		}
		return sliceMatrix;
	}

	/**
	 * returns a matrix which is the symetrized (i->j and j->i =
	 * Max((i->j,j->i))) version of the passed LayoutSlice.
	 */
	public static SparseDoubleMatrix2D getSymMaxMatrix(LayoutSlice slice) {
		int nNodes = slice.getMaxNumNodes();
		SparseDoubleMatrix2D sliceMatrix = new SparseDoubleMatrix2D(nNodes,
				nNodes);
		for (int i = 0; i < nNodes; i++) {
			for (int j = 0; j < nNodes; j++) {
				double value = Math.max(slice.getArcWeight(i, j), slice
						.getArcWeight(j, i));
				sliceMatrix.setQuick(i, j, value);
			}
		}
		return sliceMatrix;
	}

	/**
	 * reterns a new matrix which is the reverse similarity->disimilairty,
	 * disimilarity->similart of the passed matrix. returns a matrix in which
	 * each value is equal to  ( 1 / original ) * max
	 */
	public static DoubleMatrix2D getReverse(
			DoubleMatrix2D similarity, double max, double min) {
		DoubleMatrix2D disSim = new DenseDoubleMatrix2D(similarity
				.rows(), similarity.columns());
		// now loop over each and subtract the similairty value from the max
		for (int i = 0; i < disSim.rows(); i++) {
			for (int j = 0; j < disSim.columns(); j++) {
				double value = (1.0 / similarity.getQuick(i, j)) * max; // (or
																		// should
																		// it be
																		// range?
																		// max-min?
				disSim.setQuick(i, j, value);
			}
		}
		return disSim;
	}
	
	/**
	 * reterns a new matrix which is the reverse similarity->disimilairty,
	 * disimilarity->similart of the passed matrix. returns a matrix in which
	 * each value is equal to  ( 1 / original ) * max.  Replaces any resulting
	 * infininte falues with the infinteReplace param. 
	 */
	public static SparseDoubleMatrix2D getReverse(
			SparseDoubleMatrix2D similarity, double max, double min, double infinteReplace) {
		SparseDoubleMatrix2D disSim = new SparseDoubleMatrix2D(similarity
				.rows(), similarity.columns());
		// now loop over each and subtract the similairty value from the max
		for (int i = 0; i < disSim.rows(); i++) {
			for (int j = 0; j < disSim.columns(); j++) {
				double value = (1.0 / similarity.getQuick(i, j)) * max; // (or
																		// should
																		// it be
																		// range?
																		// max-min?
				if (value == Double.POSITIVE_INFINITY){
					value = infinteReplace;
				}
				disSim.setQuick(i, j, value);
			}
		}
		return disSim;
	}

	/**
	 * returns a matrix which is the symetrized (i->j and j->i =
	 * Max((i->j,j->i))) version of the passed adjacency matrix.
	 */
	public static SparseDoubleMatrix2D getSymMaxMatrix(
			DoubleMatrix2D adjMatrix) {
		int nNodes = adjMatrix.rows();
		// make a new matrix so we don't change the old one
		SparseDoubleMatrix2D symMatrix = new SparseDoubleMatrix2D(nNodes,
				nNodes);
		for (int i = 0; i < nNodes; i++) {
			for (int j = 0; j < nNodes; j++) {
				double value = Math.max(adjMatrix.getQuick(i, j), adjMatrix
						.getQuick(j, i));
				symMatrix.setQuick(i, j, value);
			}
		}
		return symMatrix;
	}

	/**
	 * Returns an array list containing a set of Subnets correspondig to the
	 * strong components of the network.
	 */
	public static ArrayList getComponents(SparseDoubleMatrix2D net,
			boolean isSymetric) {
		// list to hold all the components
		ArrayList subnets = new ArrayList();
		ArrayList components = new ArrayList();
		IntArrayList checkedNodes = new IntArrayList();

		if (isSymetric) {
			// construct lists of which nodes are in components,
			// then use lists to construct subnets

			// for each node
			for (int i = 0; i < net.rows(); i++) {
				// if it has not been checked,
				if (!checkedNodes.contains(i)) {
					// make a new componet
					IntArrayList component = new IntArrayList();
					// add all the connected nodes to the component with
					// recursive depth-first search
					addConnectedNodes(i, checkedNodes, net, component);
					// add it to the list of components
					components.add(component);
				}
			}
			// construct subnets from node lists
			Iterator compIter = components.iterator();
			while (compIter.hasNext()) {
				IntArrayList component = (IntArrayList) compIter.next();
				Subnet compNet = new Subnet(net, component);
				subnets.add(compNet);
			}
		}
		// System.out.println(components.size()+" components");
		return subnets;
	}

	/**
	 * returns the max and min values of a matrix, assumes no negitive values
	 */
	public static double[] getMatrixMaxMin(DoubleMatrix2D matrix) {
		double[] maxMin = { 0.0, Double.POSITIVE_INFINITY };
		for (int i = 0; i < matrix.rows(); i++) {
			for (int j = 0; j < matrix.columns(); j++) {
				maxMin[0] = Math.max(maxMin[0], matrix.getQuick(i, j));
				maxMin[1] = Math.min(maxMin[1], matrix.getQuick(i, j));
			}
		}
		return maxMin;
	}

	/**
	 * loops over all the slices in engine and returns the max and min value
	 */
	public static double[] getAllSliceMaxMin(ArrayList slices) {
		double[] maxMin = { 0.0, Double.POSITIVE_INFINITY };
		int numSlices = slices.size();
		// loop over all the slices in the engine
		for (int s = 0; s < numSlices; s++) {
			// calc the max and min of each slice
			double[] sliceMaxMin = getMatrixMaxMin(getMatrix((LayoutSlice) slices
					.get(s)));
			// compare it to maxMin of previously checked slices
			maxMin[0] = Math.max(maxMin[0], sliceMaxMin[0]);
			maxMin[1] = Math.min(maxMin[1], sliceMaxMin[1]);
		}
		return maxMin;
	}

	private static void addConnectedNodes(int node, IntArrayList checked,
			SparseDoubleMatrix2D network, IntArrayList component) {
		// recursive search for connected nodes
		// ASSUMES SYMETRY!!
		// add node to list of checked nodes
		checked.add(node);
		// add node to current component
		component.add(node);

		// go along the row of the adjacency matrix (out arcs)
		// look for non-zero entries
		for (int j = 0; j < network.columns(); j++) {
			// if the toNode has not been checked
			if (!checked.contains(j)) {
				// if there is a tie
				if (network.getQuick(node, j) != 0.0) {
					// call this method on the toNode
					addConnectedNodes(j, checked, network, component);
				}
			}
		}
	}

	/**
	 * creates a new matrix with dimensions equal to number of nodes in
	 * intArrayList and containing only the relationships between the listed
	 * elements and of the same type (sparse, dense) as the passed matrix.
	 * Matrix is backed by original, so modifications will change original. 
	 */
	public static DoubleMatrix2D getSubnetMatrix(
			DoubleMatrix2D net, IntArrayList nodes) {
		//int nNodes = nodes.size();
		nodes.trimToSize();

		DoubleMatrix2D subnet = net.viewSelection(nodes.elements(),nodes.elements());
//		SparseDoubleMatrix2D subnet = new SparseDoubleMatrix2D(nNodes, nNodes);
//		for (int n = 0; n < nNodes; n++) {
//			int i = nodes.getQuick(n);
//			for (int m = 0; m < nNodes; m++) {
//				int j = nodes.getQuick(m);
//				subnet.setQuick(n, m, net.getQuick(i, j));
//			}
//		}
		return subnet;
	}

	/**
	 * returns an adjacency matrix in which each entry is the "All-Pairs
	 * shortest path: distance between the nodes, calculated with Dijkstra's
	 * algorithm
	 */
	public static DenseDoubleMatrix2D getAllShortPathMatrix(LayoutSlice slice) {
		// get the adj matrix from the slice and call APSP on it
		DenseDoubleMatrix2D pathMatrix = getAllShortPathMatrix(getMatrix(slice));
		return pathMatrix;
	}

	/**
	 * returns an adjacency matrix in which each entry is the "All-Pairs
	 * shortest path: distance between the nodes, calculated with Dijkstra's
	 * algorithm
	 */
	public static DenseDoubleMatrix2D getAllShortPathMatrix(
			DoubleMatrix2D adjMatrix) {
		// CHECK FOR MULTIPLEX!!
		// MATRIX MUST BE SYMMETRIZED!!
		// adjMatrix = getSymMaxMatrix(adjMatrix);
		int nNodes = adjMatrix.rows();
		DenseDoubleMatrix2D distMatrix = new DenseDoubleMatrix2D(nNodes, nNodes);
		distMatrix.assign(Double.POSITIVE_INFINITY);
		DoubleArrayList priorityList = new DoubleArrayList();
		IntArrayList nodeQueue = new IntArrayList();
		IntArrayList checkedNodes = new IntArrayList();

		for (int i = 0; i < nNodes; i++) {

			checkedNodes.clear();
			priorityList.clear();
			nodeQueue.clear();
			// find paths to all nodes connected to i
			// set distance to self as 0, and add to list
			distMatrix.setQuick(i, i, 0.0);
			checkedNodes.add(i);
			priorityList.add(0.0);
			nodeQueue.add(i);
			while (nodeQueue.size() > 0) {
				// find node on fringe with smallest priority value
				double fringeNodePrior = Double.POSITIVE_INFINITY;
				int fringeNodeIndex = -1;// Integer.MAX_VALUE;
				for (int n = 0; n < priorityList.size(); n++) {
					if (priorityList.getQuick(n) < fringeNodePrior) {
						fringeNodeIndex = n;
						fringeNodePrior = priorityList
								.getQuick(fringeNodeIndex);
					}
				}
				int fringeNode = nodeQueue.get(fringeNodeIndex);
				double fringeNodeDist = priorityList.getQuick(fringeNodeIndex);
				// take it of the list
				nodeQueue.remove(fringeNodeIndex);
				priorityList.remove(fringeNodeIndex);
				checkedNodes.add(fringeNode);
				// put its distance in matrix !SYMMETRIC!
				distMatrix.setQuick(i, fringeNode, fringeNodeDist);
				distMatrix.setQuick(fringeNode, i, fringeNodeDist);
				// loop over its edges, adding nodes to queue with their
				// distances
				DoubleMatrix1D row = adjMatrix.viewRow(fringeNode);
				for (int j = 0; j < nNodes; j++) {
					if ((row.getQuick(j) < Double.POSITIVE_INFINITY) && (!checkedNodes.contains(j))) {
						
						// calc workNode's distance from iNode
						// use an max of i -> j, j -> i, to symmetrieze
						double workNodeDist = fringeNodeDist
								+ adjMatrix.getQuick(fringeNode, j);
						int prevDistIndex = nodeQueue.indexOf(j);
						if (prevDistIndex >= 0) {
							// check if it has a lower distance
							if (priorityList.getQuick(prevDistIndex) > workNodeDist) {
								// repace it with new value
								priorityList.set(prevDistIndex, workNodeDist);
							}
						} else {
							// add the worknode to the queue with priority
							priorityList.add(workNodeDist);
							nodeQueue.add(j);
						}
					}
				}
			}
		}
		return distMatrix;
	}
	
	public static DenseDoubleMatrix2D getFastFastAllShortPathMatrix(
			DoubleMatrix2D adjMatrix, double max, double min, boolean isDistance) {
		if (isDistance){
			System.out.println("FastAPSP algorithm not currently set up to accept a distance matrix as input");
			return null;
		}

		// CHECK FOR MULTIPLEX!!
		// MATRIX MUST BE SYMMETRIZED!!
		// adjMatrix = getSymMaxMatrix(adjMatrix);
		int nNodes = adjMatrix.rows();
		DenseDoubleMatrix2D distMatrix = new DenseDoubleMatrix2D(nNodes, nNodes);
		distMatrix.assign(Double.POSITIVE_INFINITY);
		DoubleArrayList priorityList = new DoubleArrayList();
		IntArrayList nodeQueue = new IntArrayList();
		IntArrayList checkedNodes = new IntArrayList();
		for (int i = 0; i < nNodes; i++) {

			checkedNodes.clear();
			priorityList.clear();
			nodeQueue.clear();
			// find paths to all nodes connected to i
			// set distance to self as 0, and add to list
			distMatrix.setQuick(i, i, 0.0);
			checkedNodes.add(i);
			priorityList.add(0.0);
			nodeQueue.add(i);
			while (nodeQueue.size() > 0) {
				// find node on fringe with smallest priority value
				 double fringeNodeDist = Double.POSITIVE_INFINITY;
				 int fringeNodeIndex = -1;// Integer.MAX_VALUE;
				for (int n = 0; n < priorityList.size(); n++) {
					if (priorityList.getQuick(n) < fringeNodeDist) {
						fringeNodeIndex = n;
						fringeNodeDist = priorityList
								.getQuick(fringeNodeIndex);
					}
				}
				int fringeNode = nodeQueue.get(fringeNodeIndex);
			//	double fringeNodeDist = priorityList.getQuick(fringeNodeIndex);
				// take it of the list
				nodeQueue.remove(fringeNodeIndex);
				priorityList.remove(fringeNodeIndex);
				checkedNodes.add(fringeNode);
				// put its distance in matrix !SYMMETRIC!
				distMatrix.setQuick(i, fringeNode, fringeNodeDist);
				distMatrix.setQuick(fringeNode, i, fringeNodeDist);
				// loop over its edges, adding nodes to queue with their
				// distances
			//	 row = reverse.viewRow(fringeNode); //.getNonZeros(outTies,outValues);
				for (int j = 0; j < nNodes; j++) {
					if ( (adjMatrix.getQuick(fringeNode,j) >  0.0) 
							&& (!checkedNodes.contains(j))) {
						
						// calc workNode's distance from iNode
						// use an max of i -> j, j -> i, to symmetrieze
						double workNodeDist = fringeNodeDist
								+ ( 1.0 / adjMatrix.getQuick(fringeNode,j)) * max;
						int prevDistIndex = nodeQueue.indexOf(j);
						if (prevDistIndex >= 0) {
							// check if it has a lower distance
							if (priorityList.getQuick(prevDistIndex) > workNodeDist) {
								// repace it with new value
								priorityList.set(prevDistIndex, workNodeDist);
								
							}
						} else {
							// add the worknode to the queue with priority
							priorityList.add(workNodeDist);
							nodeQueue.add(j);
						}
					}
				}
			}
		}

		return distMatrix;
	}


	
	public static DenseDoubleMatrix2D getFastAllShortPathMatrix(
			DoubleMatrix2D adjMatrix, double max, double min, boolean isDistance) {
		DoubleMatrix2D reverse;
		if (!isDistance){
			//need to reverse the matrix
			 reverse = NetUtils.getReverse(adjMatrix, max,min);
		}else {
			reverse = adjMatrix;
		}
		// MATRIX MUST BE SYMMETRIZED!!
		int nNodes = adjMatrix.rows();
		DenseDoubleMatrix2D distMatrix = new DenseDoubleMatrix2D(nNodes, nNodes);
		distMatrix.assign(Double.POSITIVE_INFINITY);
		// Make a list to hold the priority-index pairs so we can find them
		Vector buckets = new Vector(nNodes);
		BitVector checked = new BitVector(nNodes);
		BitVector onQueue = new BitVector(nNodes);
		for (int i = 0; i < nNodes; i++) {
			buckets.add(new PriorityIntTuple(Double.POSITIVE_INFINITY, i));
		}
		PriorityQueue priorityQ = new PriorityQueue(nNodes);
		IntArrayList outTies = new IntArrayList();
		DoubleArrayList outValues = new DoubleArrayList();
		for (int i = 0; i < nNodes; i++) {
			checked.clear();
			priorityQ.clear();
			//reset distances
			for (int j = 0; j < nNodes; j++) {
				((PriorityIntTuple)buckets.get(j)).priority = Double.POSITIVE_INFINITY;
			}
			// nodeQueue.clear();
			// find paths to all nodes connected to i
			// set distance to self as 0, and add to list
			distMatrix.setQuick(i, i, 0.0);
			((PriorityIntTuple)buckets.get(i)).priority = 0.0;
			priorityQ.add(buckets.get(i));
			onQueue.put(i,true);
		//	System.out.println("i node:"+i);
			while (priorityQ.size() > 0) {
				//System.out.println("queue:"+priorityQ);
				// find node on fringe with smallest priority value
//				 take it of the list
				PriorityIntTuple bucket = (PriorityIntTuple) priorityQ.poll();
				int fringeNode = bucket.index;
				onQueue.put(fringeNode,false);
				double fringeNodeDist = bucket.priority;
				// put its distance in matrix !SYMMETRIC!
				distMatrix.setQuick(i, fringeNode, fringeNodeDist);
				distMatrix.setQuick(fringeNode, i, fringeNodeDist);
//				 put the distance in the list of checked distances
			//	System.out.print("\nfringeNode:\t"+fringeNode+"\tdist:\t"+fringeNodeDist+"\t"+checked.get(fringeNode));
				checked.put(fringeNode,true);
				// loop over its edges, adding nodes to queue with their
				// distances
				outTies.clear();
				outValues.clear();
				//get only the non-zero edges to save time
				adjMatrix.viewRow(fringeNode).getNonZeros(outTies,outValues);
				for (int out = 0; out < outTies.size(); out++) {
					int j = outTies.get(out);
					// first, check if we've already calced the ij distance in
					// an earlier pass
				//	if (distMatrix.getQuick(i,j) == Double.POSITIVE_INFINITY) {
				//(row.getQuick(j) < Double.POSITIVE_INFINITY) &
				//	System.out.print(" "+j);
						if ( !checked.get(j)) {
							// calc workNode's distance from iNode
							double workNodeDist = fringeNodeDist
									+ reverse.getQuick(fringeNode, j);
							//TODO: don't use reverse matrix, just grap the value here
							// get the old temporary distance for the node
							PriorityIntTuple jBucket = ((PriorityIntTuple) buckets
									.get(j));
							double previous = jBucket.priority;
							// check if we have already estimated the distance
							//if (previous < Double.POSITIVE_INFINITY){
							if (onQueue.get(j)){ //TODO: is .contains fast enough
								//if the new distance is shorter, replace it on the q
								if (workNodeDist < previous) {
									priorityQ.remove(jBucket);
									jBucket.priority = workNodeDist;
									priorityQ.add(jBucket);
								//	System.out.println("\t\tupdated:"+j+" from:"+previous+" to:"+workNodeDist);
								}
							//	System.out.println("\t\tskipped j:"+j);
								// otherwise leave it alone
							} else { //we have not put it in the Q yet in this pass
									jBucket.priority = workNodeDist;
									priorityQ.add(jBucket);
									onQueue.put(j,true);	
							}
							
						}
						
					//}  //we've already calced this distance going the other way
					
						
				}
			}
		}
		return distMatrix;
	}
	

	/**
	 * Returns a double equal to the graph-theoretic diameter of the passed
	 * network (the length of the longest shortest path). Requires the
	 * calculation of all shortest paths using "Dijkstra's" algorithm, and
	 * timing will be approx. O(N E log N).Distances are obtained from the
	 * outedge's getStrength() method, and are assumed to be symmetric and
	 * non-negitive.
	 * 
	 * @param nodes
	 *            the network for which the diameter will be calculated
	 */
	public static double calcDiameter(LayoutSlice slice) {
		double graphDiam = 0.0;
		int nNodes = slice.getMaxNumNodes();
		DenseDoubleMatrix2D distMatrix = getAllShortPathMatrix(slice);
		for (int i = 0; i < nNodes; i++) {
			for (int j = 0; j < nNodes; j++) {
				graphDiam = Math.max(graphDiam, distMatrix.getQuick(i, j));
			}
		}
		return graphDiam;
	}

	public static double calcDiameter(DenseDoubleMatrix2D distMatrix) {
		double graphDiam = 0.0;
		int nNodes = distMatrix.rows(); // assumes it is square
		for (int i = 0; i < nNodes; i++) {
			for (int j = 0; j < nNodes; j++) {
				graphDiam = Math.max(graphDiam, distMatrix.getQuick(i, j));
			}
		}
		return graphDiam;
	}

	public static double getPearsons(SparseDoubleMatrix2D network) {
		double value = 0.0;
		return value;
	}

	// returns a version of kruskal's stress
	// SUMMATION ROUND-OFF PROBLEM!!
	// should just use corelation between the two matricies?
	public static double getStress(LayoutSlice slice) {
		int nNodes = slice.getMaxNumNodes();
		double[] xCoords = slice.getXCoords();
		double[] yCoords = slice.getYCoords();
		// assume original was similarity, so reverse it to get dissim
		double[] maxMin = getMatrixMaxMin(NetUtils.getMatrix(slice));
		DoubleMatrix2D reverseMatrix = getReverse(NetUtils
				.getMatrix(slice), maxMin[0], maxMin[1]);
		// symetrize before all pairs shortest path.. (should allow for floyed
		// instead of dijkstra)
		reverseMatrix = NetUtils.getSymMaxMatrix(reverseMatrix);
		DenseDoubleMatrix2D distMatrix = getAllShortPathMatrix(reverseMatrix);
		DenseDoubleMatrix2D obsMatrix = new DenseDoubleMatrix2D(distMatrix
				.rows(), distMatrix.rows());

		double stress = 0.0;
		double xDiff;
		double yDiff;
		double obs;
		double dij;
		double scaleFact = 0.0; // for rescaling from coordinate space ~ optDist
		double stressScale = 0.0; // stress denominator
		double dijTotal = 0.0; // to adjust for coordinate transform
		double obsTotal = 0.0;

		// loop over links to get values for a scale factor
		for (int i = 0; i < nNodes; i++) {
			for (int j = 0; j < nNodes; j++) {
				xDiff = xCoords[i] - xCoords[j];
				yDiff = yCoords[i] - yCoords[j];
				// obs distance = sqrt of xDiff squared + yDiff squared
				obs = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
				// put the value in the obs matrix so we won't have to calc
				// again
				obsMatrix.setQuick(i, j, obs);
				// desired distance is the i,j th entry in the matrix
				dij = distMatrix.getQuick(i, j);
				dijTotal += dij;
				obsTotal += obs;
			}
		}
		// compute the scale factor to correct for coordinate spaces
		scaleFact = obsTotal / dijTotal;
		// sum over all i and j
		for (int i = 0; i < nNodes; i++) {
			for (int j = 0; j < nNodes; j++) {
				obs = obsMatrix.getQuick(i, j) / scaleFact;
				// desired distance is the i,j th entry in the matrix
				dij = distMatrix.getQuick(i, j);
				// the square of the differnces
				stress += (obs - dij) * (obs - dij);
				stressScale += dij * dij;
			}

		}
		stress = Math.sqrt(stress / scaleFact); // *(dijTotal/obsTotal);
		// debug
		System.out.println(distMatrix.toString());
		System.out.println(obsMatrix.toString());
		System.out.println("scaleFact:" + scaleFact);
		System.out.println("stress:" + stress);
		return stress;
	}
}
