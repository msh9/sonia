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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.PathIterator;
import java.awt.geom.RectangularShape;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import sonia.ArcAttribute;
import sonia.NodeAttribute;
import sonia.RenderSlice;

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
import com.anotherbigidea.flash.writers.SWFWriter;
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

	// TODO: need to figure out how to reuse layers so the draw order (arcs,
	// nodes, labels) is mantained...

	private HashMap nodeGraphics;

	private HashMap nodeLabels;

	private HashMap edgeGraphics;

	private HashMap activeNodes;

	private HashMap activeLabels;

	private HashMap activeEdges;

	private int transVal = 255;

	private Frame currentFrame;

	private FontDefinition fontdef;

	private Font font;

	private TagWriter tagwriter;

	private Transform statusPosition;

	private Instance testEdge;

	// private Text statusText;
	// private int statusTextLayer;
	// private int statusTextId;
	private Instance statusText;

	public SWFRender() {
		nodeGraphics = new HashMap();
		nodeLabels = new HashMap();
		edgeGraphics = new HashMap();
		activeNodes = new HashMap();
		activeLabels = new HashMap();
		activeEdges = new HashMap();
		statusPosition = new Transform(5, 10);
		try {
			fontdef = FontLoader.loadFont(this.getClass().getResourceAsStream(
					"VerdanaFont.swf"));
			font = new Font(fontdef);
			// font.loadAllGlyphs();

			// swf.tagDefineFont2(1,0,"font",font.getGlyphList().size(),fontdef.getAscent(),fontdef.getDescent(),fontdef.getLeading(),null,fontdef.);
		} catch (IOException e) {
			System.out.println("Error loading font for swf movie: "
					+ e.toString());
			e.printStackTrace();
		}
	}

	private Instance getSWFNode(NodeAttribute node, double scaleFactor)
			throws IOException {

		// int shapeID = nextShapeID;
		Instance inst;
		// if we've already drawn the shape, just return its reference
		if (nodeGraphics.containsKey(node)) {
			inst = (Instance) nodeGraphics.remove(node);
		} else {
			double nodeDrawSize = node.getNodeSize() * scaleFactor;

			// get the java shape
			RectangularShape s = (RectangularShape) node.getNodeShape();
			// adjust for scaling
			s.setFrame((nodeDrawSize / 2.0), (nodeDrawSize / 2.0),
					nodeDrawSize, nodeDrawSize);
			// draw the shape centered around the origin
			double centerX = (s.getWidth() / 2) + s.getMinX(); // have to
			// subtract out the "original" coords...
			double centerY = (s.getHeight() / 2) + s.getMinY();
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
			inst = currentFrame.placeSymbol(shape, null, null);
		}
		activeNodes.put(node, inst);
		return inst;

	}

	private Instance getSWFLabel(NodeAttribute node, String label,
			double scaleFact) {
		Instance inst;
		if (nodeLabels.containsKey(node)) {
			inst = (Instance) nodeLabels.remove(node);
		} else {
			Text text = new Text(null);
			java.awt.Color c = NodeAttribute.DEFULAT_LABEL_COLOR;
			try {
				text.row(font.chars(label, node.getLabelSize() * 0.75f),
						new Color(c.getRed(), c.getGreen(), c.getBlue()), 0, 0,
						false, false);
			} catch (NoGlyphException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			inst = currentFrame.placeSymbol(text, null, null);

		}
		activeLabels.put(node, inst);
		return inst;

	}

	/**
	 * always have to redraw the edge shapes because it is too hard to calculate
	 * a transform to position the ends correctly without distorting the edge
	 * thickness
	 * 
	 * @author skyebend
	 * @param arc
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 * @return
	 * @throws IOException
	 */
	private Instance getSWFEdge(ArcAttribute arc, double startX, double startY,
			double endX, double endY, double widthFactor, boolean arrows)
			throws IOException {
		Instance inst;
		// if we've already drawn the shape, just return its reference
		Shape shape = new Shape();
		// edge width
		double borderWidth = arc.getArcWidth() * widthFactor;
		java.awt.Color c = arc.getArcColor();
		Color arrowC = new AlphaColor(c.getRed(), c.getGreen(), c.getBlue(),
				transVal);
		shape.defineLineStyle(borderWidth, arrowC);
		shape.setLineStyle(1);
		shape.move(Math.round(startX), Math.round(startY));
		shape.line(Math.round(endX), Math.round(endY));
		if (arrows) {
			shape.defineFillStyle(arrowC);
			shape.setRightFillStyle(1);
			// make no line so that it is just the head shape
			shape.defineLineStyle(0.0, arrowC);
			shape.setLineStyle(2);

			double arrowSize = RenderSlice.arrowLength + borderWidth;
			double xDiff = (startX - endX);
			double yDiff = (startY - endY);
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

			// we should be alreay at the tip of the arrow
			// but there is a bug in javaswf..
			// Also have to deal with some round off error in the double to int
			// conversion
			shape.move(Math.round(endX), Math.round(endY));
			// one wedge
			shape.line(Math.round(endX
					+ (arrowSize * Math.sin(lineAngle - 0.3))), Math.round(endY
					+ (arrowSize * Math.cos(lineAngle - 0.3))));
			// other wedge
			shape.line(Math.round(endX
					+ (arrowSize * Math.sin(lineAngle + 0.3))), Math.round(endY
					+ (arrowSize * Math.cos(lineAngle + 0.3))));
			// back to top
			shape.line(Math.round(endX), Math.round(endY));

		}
		if (edgeGraphics.containsKey(arc)) {
			inst = (Instance) edgeGraphics.remove(arc);
			inst = currentFrame.replaceSymbol(shape, inst.getDepth(), null,
					null, 0, 0);
		} else {

			inst = currentFrame.placeSymbol(shape, null, null);
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
	 * 
	 * @author skyebend
	 */
	public void newFrame() {

		// we are starting an new frame, so remove any objects that didn't
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
		// now start next frame
	}

	/**
	 * if the draw target is a flash frame swf it is stored, otherwise ignored
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
		// TODO: will multiple arcs draw correctly?
		try {
			Instance inst = getSWFEdge(arc, fromX, fromY, toX, toY,
					widthFactor, arrows);
			// Instance inst = getSWFEdge(arc, 0.0, 0.0, 10, 0,widthFactor,
			// arrows);
			// double length =
			// Math.sqrt(Math.pow((fromX-toX),2)+Math.pow((fromY-toY),2))/10;
			// double rotate = Math.atan((toY-fromY)/(toX-fromX));
			// Transform xform = new
			// Transform(rotate,length,length,fromX,fromY);
			// currentFrame.alter(inst,xform,null);
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
			Instance inst = getSWFNode(node, scaleFact);
			currentFrame.alter(inst, (int) Math.round(xCoord), (int) Math
					.round(yCoord));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void paintNodeLabels(NodeAttribute node, double xCoord,
			double yCoord, double scaleFact, boolean showLabels, boolean showId) {
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
		double nodeDrawSize = node.getNodeSize() * scaleFact;
		Instance inst = getSWFLabel(node, printLabel, scaleFact);
		currentFrame.alter(inst, (int) Math.round(xCoord + (nodeDrawSize / 2.0)
				+ 2.0), (int) Math.round(yCoord + node.getLabelSize() / 2.0));

	}

	public void paintStats(String stats) {
		Text text = new Text(null);
		try {
			text.row(font.chars(stats, 8), new Color(50, 50, 50), 0, 0, false,
					false);
		} catch (NoGlyphException e) {
			e.printStackTrace();
		}
		if (statusText == null) {
			statusText = currentFrame.placeSymbol(text, 5, 10);
		} else {

			statusText = currentFrame.replaceSymbol(text,
					statusText.getDepth(), statusPosition, null, 0, 0);
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

	/*
	 * private void writeIncrementalFrame(Movie movie, Frame frame){
	 * frame.write( movie, tagwriter, tagwriter ); }
	 * 
	 * private void startIncrementalWrite(Movie movie,String filename,boolean
	 * compressed) throws IOException{ SWFWriter swfwriter = new SWFWriter(
	 * filename ); tagwriter = new TagWriter( swfwriter );
	 * swfwriter.setCompression( compressed ); Hashtable definedSymbols = new
	 * Hashtable(); int maxId = 1;
	 * 
	 * tagwriter.header( movie.getVersion(), -1, //force length calculation
	 * canvas.getWidth() * SWFConstants.TWIPS, canvas.getHeight() *
	 * SWFConstants.TWIPS,
	 * (int)Math.round(1.0/((double)engine.getFrameDelay()/1000.0)), -1);
	 * //force frame calculation
	 * 
	 * 
	 * 
	 * tagwriter.tagSetBackgroundColor( new Color(255, 255, 255) );
	 * 
	 * 
	 * //--Process Imports
	 * 
	 * if( importLibraries != null && ! importLibraries.isEmpty() ) { for(
	 * Iterator keys = importLibraries.keySet().iterator(); keys.hasNext();) {
	 * String libName = (String)keys.next(); List imports =
	 * (List)importLibraries.get( libName );
	 * 
	 * String[] names = new String[imports.size()]; int[] ids = new
	 * int[imports.size()];
	 * 
	 * int i = 0; for( Iterator it = imports.iterator(); it.hasNext(); ) {
	 * ImportedSymbol imp = (ImportedSymbol)it.next();
	 * 
	 * names[i] = imp.getName(); ids[i] = imp.define( movie, tagwriter,
	 * tagwriter );
	 * 
	 * i++; }
	 * 
	 * tagwriter.tagImport( libName, names, ids ); } }
	 * 
	 * //--Process Exports if( exportedSymbols != null && !
	 * exportedSymbols.isEmpty() ) { String[] names = new
	 * String[exportedSymbols.size()]; int[] ids = new
	 * int[exportedSymbols.size()];
	 * 
	 * int i = 0; for( Iterator it = exportedSymbols.iterator(); it.hasNext(); ) {
	 * ExportedSymbol exp = (ExportedSymbol)it.next();
	 * 
	 * names[i] = exp.getExportName(); ids[i] = exp.getSymbol().define( movie,
	 * tagwriter, tagwriter );
	 * 
	 * i++; }
	 * 
	 * tagwriter.tagExport( names, ids ); }
	 * 
	 *  }
	 */

}
