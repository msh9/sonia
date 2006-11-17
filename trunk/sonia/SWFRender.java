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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.PathIterator;
import java.awt.geom.RectangularShape;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import com.anotherbigidea.flash.SWFConstants;
import com.anotherbigidea.flash.interfaces.SWFShape;
import com.anotherbigidea.flash.interfaces.SWFText;
import com.anotherbigidea.flash.movie.Font;
import com.anotherbigidea.flash.movie.FontDefinition;
import com.anotherbigidea.flash.movie.FontLoader;
import com.anotherbigidea.flash.movie.Frame;
import com.anotherbigidea.flash.movie.Instance;
import com.anotherbigidea.flash.movie.Movie;
import com.anotherbigidea.flash.movie.Shape;
import com.anotherbigidea.flash.movie.Text;
import com.anotherbigidea.flash.movie.Transform;
import com.anotherbigidea.flash.movie.Font.Chars;
import com.anotherbigidea.flash.movie.Font.NoGlyphException;
import com.anotherbigidea.flash.structs.AlphaColor;
import com.anotherbigidea.flash.structs.AlphaTransform;
import com.anotherbigidea.flash.structs.Color;
import com.anotherbigidea.flash.structs.Matrix;
import com.anotherbigidea.flash.structs.Rect;
import com.anotherbigidea.flash.writers.TagWriter;

/**
 * converts calls to draw nodes and edges into SFW graphics calls as part of the
 * process of creating a flash movie. Stores mappings of NodeAttributes and
 * ArcAttributes to SFW objects to allow the SWF objects to be reused
 * 
 * @author skyebend
 * 
 */
public class SWFRender implements Render {

	private HashMap nodeGraphics;

	private HashMap nodeLabels;

	private HashMap edgeGraphics;

	private HashMap activeNodes;

	private HashMap activeLabels;

	private HashMap activeEdges;

	// private HashMap layerLookup;
	private int transVal = 255;

	// private TagWriter swf;
	private Movie movie;

	private Frame currentFrame;

	private FontDefinition fontdef;

	private Font font;


	// private Text statusText;
	// private int statusTextLayer;
	// private int statusTextId;
	private Instance statusText;

	public SWFRender(Movie movie) {
		this.movie = movie;
		nodeGraphics = new HashMap();
		nodeLabels = new HashMap();
		edgeGraphics = new HashMap();
		activeNodes = new HashMap();
		activeLabels = new HashMap();
		activeEdges = new HashMap();
		try {
			fontdef = FontLoader.loadFont(this.getClass().getResourceAsStream(
					"image/VerdanaFont.swf"));
			font = new Font(fontdef);
			font.loadAllGlyphs();

			// swf.tagDefineFont2(1,0,"font",font.getGlyphList().size(),fontdef.getAscent(),fontdef.getDescent(),fontdef.getLeading(),null,fontdef.);
		} catch (IOException e) {
			System.out.println("Error loading font for swf: " + e.toString());
			e.printStackTrace();
		}
	}

	private Instance getSWFNode(NodeAttribute node) throws IOException {

		// int shapeID = nextShapeID;
		Instance inst;
		// if we've already drawn the shape, just return its reference
		if (nodeGraphics.containsKey(node)) {
			// shapeID = ((Integer) nodeGraphics.get(node)).intValue();
			inst = (Instance) nodeGraphics.remove(node);
		} else {
			// get the java shape
			RectangularShape s = (RectangularShape) node.getNodeShape();
			// make the new shape
			// set the min and max for the swf shape
			// dimensions are in twips
			double centerX = (s.getWidth() / 2) + s.getMinX(); // have to
																// subtract out
																// the
																// "original"
																// coords...
			double centerY = (s.getHeight() / 2) + s.getMinY();
			// Rect outline = new Rect((int) Math.round(-1 * (frameWidth / 2)
			// * SWFConstants.TWIPS), (int) Math.round(-1
			// * (frameHeight / 2) * SWFConstants.TWIPS), (int) Math
			// .round((frameWidth / 2) * SWFConstants.TWIPS), (int) Math
			// .round((frameHeight / 2) * SWFConstants.TWIPS));
			// SWFShape shape = swf.tagDefineShape(shapeID, outline);
			Shape shape = new Shape();
			// edge width
			double borderWidth = node.getBorderWidth();
			java.awt.Color c = node.getBorderColor();
			shape.defineLineStyle(borderWidth, new AlphaColor(c.getRed(), c
					.getGreen(), c.getBlue(), transVal));
			shape.setLineStyle(1);
			java.awt.Color nc = node.getNodeColor();
			shape.defineFillStyle(new AlphaColor(nc.getRed(), nc.getGreen(), nc
					.getBlue(), transVal));
			shape.setRightFillStyle(1);
			PathIterator pathIter = s.getPathIterator(null, 0.5);
			double[] current = new double[6];
			int segType = pathIter.currentSegment(current);

			double startX = (current[0] - centerX);
			double startY = (current[1] - centerY);
			// set the start position for the shape
			shape.move(startX, startY);
			pathIter.next();
			while (!pathIter.isDone()) {
				segType = pathIter.currentSegment(current);
				if (segType == PathIterator.SEG_CLOSE) {
					shape.line(startX, startY);
				} else if (segType == PathIterator.SEG_MOVETO) {
					shape.move((current[0] - centerX), (current[1] - centerY));
				} else if (segType == PathIterator.SEG_LINETO) {
					shape.line((current[0] - centerX), (current[1] - centerY));
				} else {
					// TODO: add translation for graphics curveTo?
					System.out
							.println("undrawable segment type in swf graphics translation:"
									+ segType);
				}
				pathIter.next();
			}
			inst = currentFrame.placeSymbol(shape, 0, 0);
		}
		activeNodes.put(node, inst);
		return inst;

		// private Instance getSWFLabel(NodeAttribute){
		// Instance inst;
		//			
		// }
	}

	private Instance getSWFEdge(ArcAttribute arc, double startX, double startY,
			double endX, double endY) throws IOException {
		Instance inst;
		// if we've already drawn the shape, just return its reference

		// set the min and max for the swf shape
		// dimensions are in twips
		// double frameWidth = endX-startX;
		// double frameHeight = endY-startY;
		// Rect outline = new Rect((int) Math.round(-1 * (frameWidth / 2)
		// * SWFConstants.TWIPS), (int) Math.round(-1
		// * (frameHeight / 2) * SWFConstants.TWIPS), (int) Math
		// .round((frameWidth / 2) * SWFConstants.TWIPS), (int) Math
		// .round((frameHeight / 2) * SWFConstants.TWIPS));
		// SWFShape shape = swf.tagDefineShape(shapeID, outline);
		Shape shape = new Shape();
		// edge width
		double borderWidth = arc.getArcWidth();
		java.awt.Color c = arc.getArcColor();
		shape.defineLineStyle(borderWidth, new AlphaColor(c.getRed(), c
				.getGreen(), c.getBlue(), transVal));
		shape.setLineStyle(1);
		shape.move(startX, startY);
		shape.line(endX, endY);

		if (edgeGraphics.containsKey(arc)) {
			inst = (Instance) edgeGraphics.remove(arc);
			inst = currentFrame.replaceSymbol(shape, inst.getDepth(), null,
					null, 0, 0);
		} else {
			inst = currentFrame.placeSymbol(shape, 0, 0);
		}
		activeEdges.put(arc, inst);
		return inst;
	}

	private void removeInactive(HashMap map) {
		Iterator iter = map.keySet().iterator();
		while (iter.hasNext()) {
			Instance inst = (Instance) map.get(iter.next());
			currentFrame.remove(inst);
		}
		map.clear();
	}
	
	/**
	 * Reset various elements (remove undrawn edges) and start next frame
	 * @author skyebend
	 */
	public void newFrame(){
//		 we are starting an new frame, so remove any objects that didn't
		// get drawn last time
		removeInactive(nodeGraphics);
		nodeGraphics.putAll(activeNodes);
		activeNodes.clear();
		removeInactive(edgeGraphics);
		edgeGraphics.putAll(activeEdges);
		activeEdges.clear();
		removeInactive(nodeLabels);
		nodeLabels.putAll(activeLabels);
		activeLabels.clear();
		//now start next frame
		//currentFrame = movie.appendFrame();
	}

	/**
	 * if the draw target is a graphics2D swf it is stored, otherwise ignored
	 */
	public void setDrawingTarget(Object drawTarget) {
		if (drawTarget.getClass().equals(Frame.class)) {
			currentFrame = (Frame) drawTarget;
		} else {
			System.out
					.println("draw target is not a compatible swf Frame object");
			if (Graphics.class.isAssignableFrom(drawTarget.getClass())) {
				((Graphics2D) drawTarget).drawString("Exporting movie..", 50,
						50);
			}
		}

	}

	public void paintArc(ArcAttribute arc, float widthFactor, double fromX,
			double fromY, double toX, double toY, boolean flash,
			boolean arrows, boolean labels) {
		// TODO: width factor not supported for swf arcs
		// TODO: flash not supported for swf arcs
		// TODO: arrows not supported for swf arcs
		// TODO: labels not supported for swf arcs
		try {
			Instance inst = getSWFEdge(arc, fromX, fromY, toX, toY);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void paintNode(NodeAttribute node, double xCoord, double yCoord,
			double scaleFact) {
		// TODO: node scale not supported in swf
		// TODO: node flash not supported in swf
		// TODO: node icon not supported in swf
		// get (or create) graphics for this node attribute so we can just
		// reposition it..
		try {
			Instance inst = getSWFNode(node);
			currentFrame.alter(inst, (int) xCoord, (int) yCoord);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void paintNodeLabels(NodeAttribute node, double xCoord,
			double yCoord, double scaleFact, boolean showLabels, boolean showId) {
		// TODO Auto-generated method stub

	}

	public void paintStats(String stats) {
		Text text = new Text(null);
		try {
			text.row(font.chars(stats, 8), new Color(50, 50, 50), 0, 0, false,
					false);
		} catch (NoGlyphException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (statusText == null) {
			statusText = currentFrame.placeSymbol(text, 5, 10);
		} else {
			statusText = currentFrame.replaceSymbol(text,
					statusText.getDepth(), new Transform(5, 10), null, 0, 0);
		}
	}

	/**
	 * if argument is in range 0.0:1.0 sets transparency (alpha) by converting
	 * to an int from 1 to 255
	 */
	public void setTransparency(float trans) {
		if (0f <= trans & trans <= 1.0f) {
			transVal = (int) Math.round(trans * 255);
		} else {
			trans = 255;
		}

	}

}
