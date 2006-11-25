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
import java.awt.geom.RectangularShape;
import java.util.HashMap;

import sonia.ArcAttribute;
import sonia.NodeAttribute;
import sonia.RenderSlice;

public class Graphics2DRender implements Render {

	private GeneralPath arcLine = new GeneralPath(); // the path to draw

	private GeneralPath headPath = new GeneralPath();

	private Graphics2D graphics;

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
		graphics.draw(nodeShape);

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
			double yCoord, double scaleFact, boolean showLabels, boolean showId) {
		Color startColor = graphics.getColor();
		Font startFont = graphics.getFont();
		// rough label
		String printLabel = "";
		graphics.setColor(NodeAttribute.DEFULAT_LABEL_COLOR);
		graphics.setFont(startFont.deriveFont(node.getLabelSize()));
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
		double nodeDrawSize = node.getNodeSize() * scaleFact;
		graphics.drawString(printLabel,
				(float) (xCoord + (nodeDrawSize / 2.0) + 2.0),
				(float) (yCoord + node.getLabelSize() / 2.0));
		graphics.setColor(startColor);
		graphics.setFont(startFont);
	}

	public void paintArc(ArcAttribute arc, float widthFactor, double fromX,
			double fromY, double toX, double toY, boolean flash,
			boolean arrows, boolean labels) {
		Color startColor = graphics.getColor();
		float drawWidth = (float) arc.getArcWidth() * widthFactor;
		graphics.setStroke(getStrokeForWidth(drawWidth, arc.isNegitive()));
		graphics.setColor(arc.getArcColor());
		// should correct for width of node (and length of arrow?)
		// arcLine.setLine(fromX,fromY,toX,toY);
		arcLine.reset();
		arcLine.moveTo((float) fromX, (float) fromY);
		arcLine.lineTo((float) toX, (float) toY);
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
				headPath.moveTo((float) toX, (float) toY);
				// one wedge
				headPath
						.lineTo((float) (toX + (arrowSize * Math
								.sin(lineAngle - 0.3))),
								(float) (toY + (arrowSize * Math
										.cos(lineAngle - 0.3))));
				// other wedge
				headPath
						.lineTo((float) (toX + (arrowSize * Math
								.sin(lineAngle + 0.3))),
								(float) (toY + (arrowSize * Math
										.cos(lineAngle + 0.3))));
				// back to top
				headPath.closePath();
				graphics.fill(headPath);
			} catch (IllegalPathStateException e) {
				System.out.println("Arrow Drawing error: x:" + toX + " y:"
						+ toY);
				e.printStackTrace();
			}

		}// end draw arcs

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

	public void paintStats(String stats) {
		// round and format the slice times
		Color origColor = graphics.getColor();
		graphics.setColor(Color.gray);
		graphics.drawString(stats, 5, 10);
		graphics.setColor(origColor);

	}

	private BasicStroke getStrokeForWidth(float width, boolean isNegitive) {
		if (isNegitive) {
			width = width * -1;
			// TODO: negitive weights not working correctly
		}
		width = Math.abs(width);
		Float key = new Float(width);
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
		graphics.setComposite(AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, trans));

	}

}
