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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import cern.colt.list.IntArrayList;

/**
 * Class for describing the grouping of a set of nodes at a specific time period
 * 
 * @author skyebend
 * 
 */
public class NodeClusterAttribute implements NetworkEvent {

	private String clusterId;

	private double clusterLevel = 0.0;

	private double startTime;

	private double endTime;

	/**
	 * optionaly holds id of a cluster with a higher cluster level that encloses
	 * this cluster
	 */
	private NodeClusterAttribute parent = null;

	/**
	 * optionaly gives the id of a cluster with a lower cluster level that is
	 * enclosed by this cluster TODO: prolly need to allow more than one child
	 * cluster!
	 */
	private HashSet<NodeClusterAttribute> children = null;

	/**
	 * list of node ids that are grouped by this cluster
	 */
	private IntArrayList clusterdNodes;

	private GeneralPath path;

	// ---default drawing attributes
	public static final Color DEFAULT_BORDER_COLOR = Color.lightGray;

	public static final Color DEFAULT_FILL_COLOR = null;

	// ------graphic attributs for drawing
	private Color borderColor = DEFAULT_BORDER_COLOR;

	private Color fillColor = DEFAULT_FILL_COLOR;

	private boolean dashed = true;

	private float width = 1.0f;

	private float transparent = 0.5f;

	public NodeClusterAttribute(String id, double start, double end) {
		clusterId = id;
		startTime = start;
		endTime = end;
		clusterdNodes = new IntArrayList();
		path = new GeneralPath();
		children = new HashSet<NodeClusterAttribute>();
	}

	public NodeClusterAttribute(String id, double start, double end,
			IntArrayList cluster) {
		clusterId = id;
		startTime = start;
		endTime = end;
		clusterdNodes = cluster;
		path = new GeneralPath();
		children = new HashSet<NodeClusterAttribute>();
	}

	public IntArrayList getNodesOfChildren(IntArrayList found) {
		if (found == null){
		  found = new IntArrayList();
		}
		if (children != null) {
			// iterate over children
			Iterator<NodeClusterAttribute> kidIter = children.iterator();
			while (kidIter.hasNext()) {
				NodeClusterAttribute child = kidIter.next();
				found = child.getNodesOfChildren(found);
			}
		} 
		found.addAllOf(clusterdNodes);
		return found;
	}

	public String getId() {
		return clusterId;
		// TODO: should return a copy so it can't be modified
	}

	public double getObsTime() {
		return startTime;
	}

	public void setObsTime(double time) {
		startTime = time;
	}

	public double getEndTime() {
		return endTime;
	}

	public void setEndTime(double time) {
		endTime = time;
	}

	public int compareTo(Object evt) {
		int returnVal = 0;
		// check if can cast to NetworkEvent
		NetworkEvent event = (NetworkEvent) evt;
		double eventStart = event.getObsTime();
		if (eventStart < startTime) {
			returnVal = -1;
		} else if (eventStart > startTime) {
			returnVal = 1;
		}

		return returnVal;
	}

	/**
	 * compute a curvy spline around the convex hull of the appropriate node
	 * positions in the layout slice. WARNING casts doubles to floats
	 * 
	 * @author skyebend
	 * @param xCoords
	 *            x coordinates of all nodes
	 * @param yCoords
	 *            y coordinates of all nodes
	 */
	public void computeShapeFor(double[] xCoords, double[] yCoords,
			double padding, double leftOffset, double topOffset) {
		// check if they are all zeros
		if (ArrayLib.max(xCoords) == 0 & ArrayLib.max(yCoords) == 0) {
			// debug
			// System.out.println("skipping hull 'cause all zeros");
		} else {
			// create the array of appropriate points in the format for convex
			// hull
			// first get the list of points we are gonna include
			IntArrayList hullnodes = getNodesOfChildren(null);
			// if 2 or less nodes, don't draw
			if (hullnodes != null){
				float[] inputPoints = null;
				if ( hullnodes.size() >= 3) {
				inputPoints = new float[hullnodes.size() * 2];
				for (int i = 0; i < hullnodes.size(); i++) {
					// HACK! cast double to float
					inputPoints[i * 2] = (float) xCoords[hullnodes.get(i)-1];
					inputPoints[(i * 2) + 1] = (float) yCoords[hullnodes.get(i)-1];
				}
			
			} else {
				//if 2 nodes, just fake a 3rd point between them
				if (hullnodes.size() == 2){
					inputPoints = new float[6];
					//x coords
					inputPoints[0]=(float) xCoords[hullnodes.get(0)-1];
					inputPoints[2]=(float) xCoords[hullnodes.get(1)-1];
					inputPoints[4]=(float) inputPoints[2]+(inputPoints[0]-inputPoints[2])/2
					+(float)padding;
					//y coords
					inputPoints[1]=(float) yCoords[hullnodes.get(0)-1];
					inputPoints[3]=(float) yCoords[hullnodes.get(1)-1];
					inputPoints[5]=(float) inputPoints[3]+(inputPoints[1]-inputPoints[3])/2
					+(float)padding;
				}
			}
				float[] hull = GeomUtils.convexHull(inputPoints);
				// enlarge the hull to give extra space around the nodes
				// prolly should be by avg radius of nodes
				GeomUtils.growPolygon(hull, (float) padding);
				path = GeomUtils.cardinalSpline(path, hull, 0.1f, true,
						(float) leftOffset, (float) topOffset);
			}

		}
	}

	/**
	 * returns shape of the cluster for drawing. Shape must be first computed
	 * for the current slice's node coordinates using computeShapeFor
	 * 
	 * @author skyebend
	 * @return
	 */
	public GeneralPath getShape() {
		return path;
	}

	public Color getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	public Color getFillColor() {
		return fillColor;
	}

	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

	public HashSet getChildren() {
		return children;
	}

	public void addChild(NodeClusterAttribute child) {
		children.add(child);
	}

	public NodeClusterAttribute getParent() {
		return parent;
	}

	public void setParent(NodeClusterAttribute parent) {
		this.parent = parent;
	}

	public IntArrayList getClusterdNodes() {
		// TODO: should return a copy so it can't be modified
		return clusterdNodes;
	}

	public boolean isDashed() {
		return dashed;
	}

	public void setDashed(boolean dashed) {
		this.dashed = dashed;
	}

	public float getTransparent() {
		return transparent;
	}

	public void setTransparent(float transparent) {
		this.transparent = transparent;
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public String toString() {
		String kids = "[";
		Iterator kiditer = getChildren().iterator();
		while (kiditer.hasNext()) {
			NodeClusterAttribute kid = (NodeClusterAttribute) kiditer.next();
			kids += kid.getId()+",";
		}
		kids +="]";
		String parentStr ="";
		if (parent != null){
			parentStr = " parent{" + parent.getId()+ "}";
		}
		return getId() + ": kids{" + kids + "}"+parentStr+"  nodeIds{" + clusterdNodes + "}";
	}

}
