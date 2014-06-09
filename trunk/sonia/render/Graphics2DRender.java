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
package sonia.render;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.IllegalPathStateException;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.HashMap;

import sonia.ArcAttribute;
import sonia.NodeAttribute;
import sonia.NodeClusterAttribute;
import sonia.RenderSlice;

public class Graphics2DRender implements Render {

	private GeneralPath arcLine = new GeneralPath(); // the path to draw
	

	private GeneralPath headPath = new GeneralPath();

	private Graphics2D graphics;
	
	private float transparency = 1.0f; //holds the last transparency used 
	
	private AlphaComposite currentComposite;

	// a flyweight for holding the various strokes
	private static HashMap strokeTable = new HashMap();

	public void setDrawingTarget(Object drawTarget) {
		//if (Graphics.class.isAssignableFrom(drawTarget.getClass())){
			graphics = (Graphics2D) drawTarget;
		//}
	}

	public void paintNode(NodeAttribute node, double xCoord, double yCoord,
			 double scaleFact) {
		Color startColor = graphics.getColor();
		Font startFont = graphics.getFont();
		double nodeDrawSize = 0.0;

		nodeDrawSize = node.getNodeSize() * scaleFact;

		// changes size and shape of node by repositioning and scaling the
		// rectacular
		// frame enclosing it
		RectangularShape nodeShape = (RectangularShape) node.getNodeShape();
		nodeShape.setFrame((xCoord - nodeDrawSize / 2.0),
				(yCoord - nodeDrawSize / 2.0), nodeDrawSize, nodeDrawSize);

		Color nodeColor = node.getNodeColor();
		if (nodeColor != null) {
			graphics.setColor(nodeColor);
			graphics.fill(nodeShape);
		}
		Color borderColor = node.getBorderColor();
		// set border color/width and draw it
		graphics.setColor(borderColor);
		BasicStroke borderStroke = getStrokeForWidth(node.getBorderWidth(),
				false);
		graphics.setStroke(borderStroke);
		
		//debug code to try to figureout why renderer is throwing exceptions
		try {
		graphics.draw(nodeShape);
		} catch (Exception e) {
			System.out.println("RENDER ERROR");
			e.printStackTrace();
			System.out.println(nodeShape.toString());
		}

		// if it has never been drawn, than draww it very large so it will
		// show
		if (node.getEffect().equals(NodeAttribute.FLASH_EFFECT)) {
			nodeShape.setFrame(
					(xCoord - (nodeDrawSize + RenderSlice.flashFactor) / 2.0),
					(yCoord - (nodeDrawSize + RenderSlice.flashFactor) / 2.0),
					(nodeDrawSize + RenderSlice.flashFactor),
					(nodeDrawSize + RenderSlice.flashFactor));
			;
			graphics.setColor(RenderSlice.flashColor);
			graphics.draw(nodeShape);
			node.SetEffect(NodeAttribute.NO_EFFECT);
		}

		// drawing image for Node
		if (node.getIcon() != null) {
			int xc = (int) xCoord - (int) nodeDrawSize / 2;
			int yc = (int) yCoord - (int) nodeDrawSize / 2;
			graphics.drawImage(node.getIcon().getImage(), xc, yc,
					(int) nodeDrawSize, (int) nodeDrawSize, null);
		}
		// end draw image
		graphics.setColor(startColor);
		graphics.setFont(startFont);
	}

	public void paintNodeLabels(NodeAttribute node, double xCoord,
			double yCoord, double scaleFact, boolean showLabels, boolean showId, 
			float bgTrans) {
		//float bgTrans = 0.5f;
		Color startColor = graphics.getColor();
		Font startFont = graphics.getFont();
		// rough label
		String printLabel = "";
		
		if (showId) {
			printLabel = printLabel + node.getNodeId();
			// if both are on, show with a ":" seperator
			if (showLabels) {
				printLabel = printLabel + ":";
			}
		}
		
		
		
		if (showLabels) {
			printLabel = printLabel + node.getNodeLabel();
		}
		
		//if the label is null, don't show it 
		if (printLabel.equals("") ){
			return;
		}
		graphics.setFont(startFont.deriveFont(node.getLabelSize()));
		//figure out exactly where string will be drawn
		double nodeDrawSize = node.getNodeSize() * scaleFact;
		double labX =  (xCoord + (nodeDrawSize / 2.0) + 2.0);
		double labY = (yCoord + node.getLabelSize() / 2.0);
//		if transparency set greater than 0, draw a translucent box behind label
		if (bgTrans >0.0f){
			Rectangle2D bgRect = graphics.getFontMetrics().getStringBounds(printLabel, graphics);
			bgRect.setFrame(labX-1,labY-graphics.getFontMetrics().getAscent(),
					bgRect.getWidth()+2,bgRect.getHeight()+2);
			graphics.setColor(Color.white);
			graphics.setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, bgTrans));
			graphics.fill(bgRect);
			//set the transparency back
			graphics.setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, transparency));
		}
		graphics.setColor(node.getLabelColor());
		graphics.drawString(printLabel,(float)labX,(float)labY);
		graphics.setColor(startColor);
		graphics.setFont(startFont);
	}

	public void paintArc(ArcAttribute arc, float widthFactor, double fromX,
			double fromY, double toX, double toY, boolean flash,
			boolean arrows, boolean labels,boolean curvey) {
		Color startColor = graphics.getColor();
		float drawWidth = (float) arc.getArcWidth() * widthFactor;
		graphics.setStroke(getStrokeForWidth(drawWidth, arc.isNegitive()));
		graphics.setColor(arc.getArcColor());
		
		// should correct for width of node (and length of arrow?)
		// arcLine.setLine(fromX,fromY,toX,toY);
		arcLine.reset();
		if (!curvey){
			//set up a straight line 
	     	arcLine.moveTo( fromX,fromY);
	    	arcLine.lineTo( toX, toY);
		} else {
			//set up a curving line
			double theta = Math.atan((fromY-toY)/(fromX-toX));
//			figure how large the offset from centerline should be
			double offset = ((fromY-toY)/Math.sin(theta))/10; 
			
			arcLine.moveTo(fromX,fromY);
	    	arcLine.quadTo((offset*Math.sin(theta)+fromX+(toX-fromX)/2), 
	    			(offset*Math.cos(theta)+toY+(fromY-toY)/2),
	    			 toX, toY);
	    	//crap, still not quite right on the diagonals
		}
		graphics.draw(arcLine);
		// graphics.fill(arcLine);

		// if it has never been drawn, than draww it very large so it will show
		if (arc.shouldFlash()) {
			graphics.setStroke(getStrokeForWidth(drawWidth
					+ RenderSlice.flashFactor, arc.isNegitive()));
			graphics.setColor(RenderSlice.flashColor);
			graphics.draw(arcLine);

			arc.setFlash(false); // so we only draw once, even if stay on
									// same slice..?
		}

		// should turn off dashing
		// dashSkip = 0.0f;

		// CHECK IF ARROWS ARE TO BE DRAWN
		if (arrows) {

			// reset the arrowhead path and make arrowhead
			headPath.reset();
			double arrowSize = RenderSlice.arrowLength + drawWidth;
			double xDiff = (fromX - toX);
			double yDiff = (fromY - toY);
			double lineAngle = Math.atan((xDiff) / (yDiff));
			// trap cases where xDiff and yDiff are zero to stop strange
			// PRException onPC
			if (Double.isNaN(lineAngle)) {
				lineAngle = 0.0;
			}
			if (yDiff < 0) // rotate by 180
			{
				lineAngle += Math.PI;
			}
			try // for concurrency problems on dual processor machines...
			{
				// tip of arrow
				headPath.moveTo(toX,toY);
				// one wedge
				headPath
						.lineTo( (toX + (arrowSize * Math
								.sin(lineAngle - 0.3))),
								 (toY + (arrowSize * Math
										.cos(lineAngle - 0.3))));
				// other wedge
				headPath
						.lineTo( (toX + (arrowSize * Math
								.sin(lineAngle + 0.3))),
								 (toY + (arrowSize * Math
										.cos(lineAngle + 0.3))));
				// back to top
				headPath.closePath();
				graphics.fill(headPath);
			} catch (IllegalPathStateException e) {
				System.out.println("Arrow Drawing error: x:" + toX + " y:"
						+ toY);
				e.printStackTrace();
			}

		}// end draw arrows

		// CHECK IF LABELS ARE TO BE DRAWN
		if (labels) {
			graphics.setColor(arc.getLabelColor());
			Font originalFont = graphics.getFont();
			graphics.setFont(originalFont.deriveFont(arc.getLabelSize()));
			float labelX = (float) (fromX + (toX - fromX) / 2);
			float labelY = (float) (fromY + (toY - fromY) / 2);
			graphics.drawString(arc.getArcLabel(), labelX, labelY);
			graphics.setFont(originalFont);
		}

		// detecting other arcs to same nodes to curve..
		graphics.setColor(startColor);
	}
	
	public void paintClusters(NodeClusterAttribute cluster){
		Color origColor = graphics.getColor();
		if (cluster.getFillColor() != null){
			graphics.setColor(cluster.getFillColor());
			graphics.fill(cluster.getShape());
		}
		graphics.setColor(cluster.getBorderColor());
		graphics.setStroke(getStrokeForWidth(cluster.getWidth(),
				cluster.isDashed()));
		graphics.draw(cluster.getShape());
		graphics.setColor(origColor);
	}

	public void paintStats(String stats) {
		// round and format the slice times
		Color origColor = graphics.getColor();
		graphics.setColor(Color.gray);
		graphics.drawString(stats, 5, 10);
		graphics.setColor(origColor);

	}

	private BasicStroke getStrokeForWidth(float width, boolean isNegitive) {
		if (width > 0 & isNegitive){
			width *= -1;
		}
		Float key = new Float(width);
	   // TODO: negitive weights not working correctly
		width = Math.abs(width);
		if (strokeTable.containsKey(key)) {
			// return the stroke already made for this width
			return (BasicStroke) strokeTable.get(key);
		} else {
			// do the dashing
			float dashSkip = 0.0f;
			float dashLength = 2.0f;

			if (isNegitive) {
				dashSkip = width;
				dashLength = 2.0f * width;
			}
			float[] dash = { dashLength, dashSkip };
			BasicStroke newStroke = new BasicStroke(width,
					BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, dash,
					0.0f);
			strokeTable.put(key, newStroke);
			return newStroke;
		}
	}

	public void setTransparency(float trans) {
		transparency = trans;
		graphics.setComposite(AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, trans));

	}

}
