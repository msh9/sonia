package sonia;

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

import java.util.*;
import java.awt.Color;
import java.text.*;

import sonia.render.Graphics2DRender;
import sonia.render.Render;

/**
 * A bin or container for the node and arc events meeting its criteria, it draws
 * them to the screen when asked. See Layout window for an explanation of the
 * rendering sequence.
 */
public class RenderSlice {
	public static final Color flashColor = Color.yellow;

	public static final float flashFactor = 4.0f; // how large to expand new

	// events when they are
	// flashed

	public static final double arrowLength = 10;

	// sets the number of fractional digits to display
	public static final NumberFormat formater = NumberFormat
			.getInstance(Locale.ENGLISH);

	private SoniaLayoutEngine layoutEngine;

	// private Render render;

	private double sliceStart;

	private double sliceEnd;

	private Vector<NodeAttribute> nodeEvents;

	private Vector<ArcAttribute> arcEvents;
	
	private Vector<NodeClusterAttribute> clusterEvents = null;
	
	private double clusterPadding = 10;
	
	private double fps= 0;

	// private double flashWindow = 0.1;
	// private Line2D arcLine = new Line2D.Double(); //the path to draw

	/**
	 * Render slice is a bin holding all the objects to be renderd at a given
	 * time. ATTRIBUTES MAY NOT CHANGE WITHIN SLICE (use first or last?)
	 * computes nodes coordinates using the interpolation formula, and the
	 * coordinates specified by the appropriate layoutSlices (usually the layout
	 * slice it falls within, and the next one) Responsible for doing paint
	 * calls on objects
	 */
	public RenderSlice(SoniaLayoutEngine engine, double startTime,
			double endTime, Vector<NodeAttribute> nodes, Vector<ArcAttribute> arcs) {
		layoutEngine = engine;
		sliceStart = startTime;
		sliceEnd = endTime;
		nodeEvents = nodes;
		arcEvents = arcs;
		formater.setMaximumFractionDigits(3);
		formater.setMinimumFractionDigits(3);
	}

	public RenderSlice(SoniaLayoutEngine engine, double startTime,
			double endTime) {
		layoutEngine = engine;
		sliceStart = startTime;
		sliceEnd = endTime;
		nodeEvents = new Vector<NodeAttribute>();
		arcEvents = new Vector<ArcAttribute>();
		clusterEvents = new Vector<NodeClusterAttribute>();
		formater.setMaximumFractionDigits(3);
		formater.setMinimumFractionDigits(3);
	}

	public void addArcEvent(ArcAttribute arc) {
		arcEvents.add(arc);
	}

	public void addNodeEvent(NodeAttribute node) {
		nodeEvents.add(node);
	}
	
	public void addClusterEvent(NodeClusterAttribute cluster){
		clusterEvents.add(cluster);
	}
	
	/**
	 * gets the number of seconds it took to render the last frame
	 * @author skyebend
	 * @return
	 */
	public double getFps(){
		return fps;
	}

	// loops over all objects and asks them to paint themselves
	public void render(Object drawTarget, SoniaCanvas canvas, Render render)
	{
		//get time to calc render duration
		long rendStart = System.currentTimeMillis();
		// need to calc new coords for nodes from layout slices
		// STORE COORDS SO THEY DON'T HAVE TO BE RECALC'D EACH TIME?
		// SHOULD IT BE START OR END OF SLICE?
		// ASSUMES ALL LISTS ARE THE SAME SIZE
		double[] xCoords = layoutEngine.getCurrentXCoords();
		double[] yCoords = layoutEngine.getCurrentYCoords();
		// how for to translate the coords by
		double left = (double)layoutEngine.getLeftPad();
		double top = (double)layoutEngine.getTopPad();

		// Color origColor = graphics.getColor();
		// Font originalFont = graphics.getFont();

		render.setDrawingTarget(drawTarget);
		
		//CLUSTER EVENT LOOP
		//for now, just do this if it is graphics 2d render
		if (render instanceof Graphics2DRender){
//			 check settings before transparency
			if (canvas.isClusterTrans()) {
				render.setTransparency(canvas.getClusterTransVal());
			}
			//TODO: sort clusters to order by size before drawing. 
			List<NodeClusterAttribute> list = clusterEvents.subList(0,clusterEvents.size());
			Collections.sort(list, NodeClusterAttribute.sizeComparer);
			Iterator<NodeClusterAttribute> clustiter = clusterEvents.iterator();
			while (clustiter.hasNext()){
				NodeClusterAttribute cluster = clustiter.next();
				cluster.computeShapeFor(xCoords,yCoords,clusterPadding,left,top);
				((Graphics2DRender)render).paintClusters(cluster);
			}
		
			
		}

		// first do arcs
		// KLUDG check to not draw arcs for speed
		if (!canvas.isHideArcs()) {

			

			ArcAttribute arc;
			int fromId;
			int toId;

			// LOOP OVER ARC EVENTS
			for (int i = 0; i < arcEvents.size(); i++) {
				arc = (ArcAttribute) arcEvents.get(i);
				// check if we should flash it
				if (canvas.isFlashNew()) {
					//
					double flashStart = arc.getObsTime();
					double flashEnd = flashStart + canvas.getFlashDuration();
					// flash if it is within the interval
					if (((flashStart >= sliceStart) & (flashStart < sliceEnd))
							| ((flashStart <= sliceStart) & (flashEnd > sliceEnd))) {
						arc.setFlash(true);
					}
				}
				//figure out transparency
				float trans = 1.0f;
				if (canvas.isArcTrans()) {
					trans = canvas.getArcTransVal();
				}
				//if arc should be faded out, make it more transparent
				if (canvas.getFadeDuration() != 0.0){
					if ((arc.getObsTime()-canvas.getFadeDuration() < sliceStart) 
							& (arc.getEndTime() < sliceEnd ))
					{
						trans = trans * (float)Math.max(0,(arc.getObsTime()-sliceStart)/canvas.getFadeDuration());
					} else if ((arc.getEndTime()-canvas.getFadeDuration() < sliceEnd) &
							(sliceStart > arc.getObsTime())){
						trans = trans * (float)Math.max(0,(arc.getEndTime()-sliceEnd)/canvas.getFadeDuration());
					}
				}
				
				render.setTransparency(trans);
				
				// correct for id ofset 0 -> 1
				fromId = arc.getFromNodeId() - 1;
				toId = arc.getToNodeId() - 1;

				// CHECK IF WEIGHT LABELS ARE TO BE DRAWN !Not the same as arc
				// labels!
				// TODO: check if this kludge to paint arc weights will mess up
				// the arc labels?
				String kludgeLabel = arc.getArcLabel();
				if (canvas.isShowArcWeights()) {
					double sliceArcWeight = layoutEngine.getCurrentSlice()
							.getSymMaxArcWeight(fromId, toId);
					arc.setArcLabel(formater.format(sliceArcWeight) + "");
				}

				// translate coords to allow for visual insets
				render.paintArc(arc, canvas.getArcWidthFact(), xCoords[fromId]
						+ left, yCoords[fromId] + top, xCoords[toId] + left,
						yCoords[toId] + top, arc.shouldFlash(), canvas
								.isShowArrows(), canvas.isShowArcLabels() | canvas.isShowArcWeights(),
								canvas.drawCurvyArcs());
				arc.setArcLabel(kludgeLabel);

			} // end arc event loop
			// graphics.setFont(originalFont);
			// graphics.setColor(origColor);
		}

		float trans = 1.0f; //default to no transparency
	

		// NODE EVENT LOOP
		// then do nodes (so nodes are on top)
		for (int i = 0; i < nodeEvents.size(); i++) {
			NodeAttribute node = (NodeAttribute) nodeEvents.get(i);
			
			if (canvas.isFlashNew()) {
				//
				double flashStart = node.getObsTime();
				double flashEnd = flashStart + canvas.getFlashDuration();
				// flash if it is within the interval
				if (((flashStart >= sliceStart) & (flashStart < sliceEnd))
						| ((flashStart <= sliceStart) & (flashEnd > sliceEnd))) {
					node.SetEffect(NodeAttribute.FLASH_EFFECT);
				}
			}
			// check settings before node transparency
			if (canvas.isNodeTrans()) {
				trans = canvas.getNodeTransVal();
			} 
			
			//if node should be faded out, make it more transparent
			if (canvas.getFadeDuration() != 0.0){
				if ((node.getObsTime()-canvas.getFadeDuration() < sliceStart) 
						& (node.getEndTime() < sliceEnd ))
				{
					trans = trans * (float)Math.max(0,(node.getObsTime()-sliceStart)/canvas.getFadeDuration());
				} else if ((node.getEndTime()-canvas.getFadeDuration() < sliceEnd) &
						(sliceStart > node.getObsTime())){
					trans = trans * (float)Math.max(0,(node.getEndTime()-sliceEnd)/canvas.getFadeDuration());
				}
			}
			
			render.setTransparency(trans);
			int index = node.getNodeId() - 1;
			if (!canvas.isHideNodes()){
				render.paintNode(node, xCoords[index] + left, yCoords[index] + top,
					canvas.getNodeScaleFact());
			}
			// vertex size based label cutoffs
			boolean showId = canvas.isShowId();
			boolean showLabels = canvas.isShowLabels();
			if (showId && (node.getNodeSize() < canvas.getShowLabelsVal())) {
				showId = false;
			}
			if (showLabels && (node.getNodeSize() < canvas.getShowLabelsVal())) {
				showLabels = false;
			}
			if (showLabels | showId) {
				render.paintNodeLabels(node, xCoords[index] + left,
						yCoords[index] + top, canvas.getNodeScaleFact(),
						showLabels, showId, canvas.getLabelBgTransVal());
			}
		}
		
	

		// debug show slice stats
		if (canvas.isShowStats()) {
			// round and format the slice times
			render.paintStats("        slice:"
					+ layoutEngine.getCurrentSliceNum() + "  time:"
					+ formater.format(sliceStart) + "-"
					+ formater.format(sliceEnd));
			// graphics.drawString("
			// layout:"+layoutEngine.getLayoutInfo(),5,20);
		}
		//record how long it took
		fps = 1/((System.currentTimeMillis() - rendStart)/1000+0.00000001);
	}

	

	// accessors (not allowed to set in case objects would be at wrong time)
	public double getSliceStart() {
		return sliceStart;
	}

	public double getSliceEnd() {
		return sliceEnd;
	}

	public Vector<ArcAttribute> getArcEvents() {
		return arcEvents;
	}

	public Vector<NodeAttribute> getNodeEvents() {
		return nodeEvents;
	}

	public String toString() {
		return "start:" + sliceStart + " end:" + sliceEnd + " nodes:"
				+ nodeEvents.toString() + " arcs:" + arcEvents.toString();
	}
}