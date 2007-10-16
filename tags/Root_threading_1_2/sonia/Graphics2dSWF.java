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
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import cern.colt.function.IntObjectProcedure;

import com.anotherbigidea.flash.movie.Frame;
import com.anotherbigidea.flash.movie.Instance;
import com.anotherbigidea.flash.movie.Movie;
import com.anotherbigidea.flash.movie.MovieClip;
import com.anotherbigidea.flash.movie.Text;
import com.anotherbigidea.flash.movie.Transform;


/**
 * Basic idea is to map java2d graphics calls into flash calls and be smart enough to recognize
 * when the same object has moved...
 * @author skyebend
 *
 */
public class Graphics2dSWF extends Graphics2D {
	//private Movie movie;
	
	private Color javaColor = Color.BLACK;
	private com.anotherbigidea.flash.structs.Color swfColor = null;
	private Color bgColor = Color.WHITE;
	private Stroke javaStroke = new BasicStroke();
	private double strokeWidth = 1.0;
	//private HashMap lineStyleLookup;
	private Frame currentFrame;
	private Graphics2D origGraphics;
	//private HashMap clut;
	private Vector instances;
 
	
	public Graphics2dSWF(Graphics2D g,Frame frame){
		super();
		origGraphics = g;
		instances = new Vector();
		//lineStyleLookup = new HashMap();
		//lineStyleLookup.put(new PseudoStrokeStyle(javaColor,strokeWidth),Integer.valueOf(1));
	//	clut = new HashMap();
		//clut.put(Color.WHITE,Integer.valueOf(1));
		//start the first frame
		currentFrame = frame;
		//double width = origGraphics.getDeviceConfiguration().getBounds().getWidth();
		//double height = origGraphics.getDeviceConfiguration().getBounds().getHeight();
		

	}
	

	public Vector getInstances(){
		return instances;
	}
	
	
	
//	/**
//	 * holds color / width association for tracking line styles used
//	 * @author skyebend
//	 *
//	 */
//	private class PseudoStrokeStyle{
//		public Color color;
//		public double width;
//		public PseudoStrokeStyle(Color color, double width){
//			this.color = color;
//			this.width = width;
//		}
//		public boolean equals(Object obj) {
//			PseudoStrokeStyle other = (PseudoStrokeStyle)obj;
//			if (color.equals(other.color) & width == other.width){
//				return true;
//			}
//			return false;
//		}
//		public int hashCode() {
//			return color.hashCode();
//		}
//		
//		
//		
		
		
		//TODO: will to psedudoStrokes with the same values hash the same?
		
	//}
	
	/**
	 * sets the fill for the shape by mapping the current setting in the graphics 
	 * context to a flash fill style, or creating a new style for the color if none exists
	 * @author skyebend
	 * @param s
	 */
//	protected void setFillStyle(com.anotherbigidea.flash.movie.Shape s){
//		Object value = clut.get(javaColor);
//		int styleNum = -1;
//		if (value == null){
//			styleNum = clut.size()+1;
//			clut.put(javaColor,Integer.valueOf(styleNum));
//		} else {
//			styleNum = ((Integer)value).intValue();
//		}
//		s.setRightFillStyle(styleNum);
//		s.setLeftFillStyle(styleNum);
//	}
	
	/**
	 * set the line with and color of the shape to the current values of the graphics, 
	 * using a line style, creating a new line style if needed
	 * @author skyebend
	 * @param s
	 */
//	protected void setLineStyle(com.anotherbigidea.flash.movie.Shape s){
//		//check if this style allready exists
//		PseudoStrokeStyle style = new PseudoStrokeStyle(javaColor, strokeWidth);
//		Object value = lineStyleLookup.get(style);
//		int styleNum = -1;
//		if (value == null){ //not in the list, so add it
//			styleNum = lineStyleLookup.size()+1;
//			s.defineLineStyle(strokeWidth,swfColor); //style should have same number as on list
//			lineStyleLookup.put(style,Integer.valueOf(styleNum));
//		}
//		else {
//			styleNum = ((Integer)value).intValue();
//		}
//		//debug
//		System.out.println("line style:"+styleNum);
//		s.setLineStyle(styleNum);
//	}

	public void draw(Shape s) {

		com.anotherbigidea.flash.movie.Shape swfShape = new com.anotherbigidea.flash.movie.Shape();
		//setLineStyle(swfShape);
		//setFillStyle(swfShape);
		swfShape.defineLineStyle(strokeWidth,swfColor);
		swfShape.setLineStyle(1);
		//swfShape.defineFillStyle(swfColor);
		//swfShape.setRightFillStyle(1);
		//swfShape.setLeftFillStyle(1);
		double startX;
		double startY;
		PathIterator pathIter = s.getPathIterator(null,0.5);
		double[] current = new double[6];
		int segType =  pathIter.currentSegment(current);
		startX = current[0];
		startY = current[1];
		swfShape.move(startX,startY);
		pathIter.next();
		while (!pathIter.isDone()){
			segType =  pathIter.currentSegment(current);
			if (segType == PathIterator.SEG_CLOSE){
				swfShape.line(startX,startY);
			} else if (segType == PathIterator.SEG_MOVETO){
				swfShape.move(current[0],current[1]);
			} else if (segType == PathIterator.SEG_LINETO){  
				swfShape.line(current[0],current[1]);
			} else {
				System.out.println("undrawable segment type:"+segType);
			}
			pathIter.next();
		}
		//TODO:  modify shape coords to smallest bounding box, use place symbol for position?
		Instance inst  = currentFrame.placeSymbol(swfShape,0,0);
		instances.add(inst); //so we can delete it later

	}

	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
		// TODO Auto-generated method stub
		return false;
	}

	public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
		// TODO Auto-generated method stub
		
	}

	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		// TODO Auto-generated method stub
		
	}

	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		// TODO Auto-generated method stub
		
	}

	public void drawString(String str, int x, int y) {
		drawString(str,(float)x,(float)y);
		
	}

	public void drawString(String s, float x, float y) {
//		com.anotherbigidea.flash.movie.Font f = movie.
//		Text text = new Text(null);
//		text.row(font.c,swfColor,x,y,true,true);
		
	}

	public void drawString(AttributedCharacterIterator iterator, int x, int y) {
		// TODO Auto-generated method stub
		
	}

	public void drawString(AttributedCharacterIterator iterator, float x, float y) {
		// TODO Auto-generated method stub
		
	}

	public void drawGlyphVector(GlyphVector g, float x, float y) {
		// TODO Auto-generated method stub
		
	}

	public void fill(Shape s) {
		com.anotherbigidea.flash.movie.Shape swfShape = new com.anotherbigidea.flash.movie.Shape();
		//setLineStyle(swfShape);
		//setFillStyle(swfShape);
		swfShape.defineFillStyle(swfColor);
		swfShape.setRightFillStyle(1);
		//swfShape.setLeftFillStyle(1);
		double startX;
		double startY;
		PathIterator pathIter = s.getPathIterator(null,0.5);
		double[] current = new double[6];
		int segType =  pathIter.currentSegment(current);
		startX = current[0];
		startY = current[1];
		swfShape.move(startX,startY);
		pathIter.next();
		while (!pathIter.isDone()){
			segType =  pathIter.currentSegment(current);
			if (segType == PathIterator.SEG_CLOSE){
				swfShape.line(startX,startY);
			} else if (segType == PathIterator.SEG_MOVETO){
				swfShape.move(current[0],current[1]);
			} else if (segType == PathIterator.SEG_LINETO){  
				swfShape.line(current[0],current[1]);
			} else {
				System.out.println("undrawable segmant type:"+segType);
			}
			pathIter.next();
			
		}
		swfShape.line(startX,startY);
		//TODO:  modify shape coords to smallest bounding box, use place symbol for position?
		Instance inst  = currentFrame.placeSymbol(swfShape,0,0);
		instances.add(inst); //so we can delete it later		
	}

	public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
		return origGraphics.hit(rect,s,onStroke);
	}

	public GraphicsConfiguration getDeviceConfiguration() {
		return origGraphics.getDeviceConfiguration();
	}

	public void setComposite(Composite comp) {
		origGraphics.setComposite(comp);
		
	}

	public void setPaint(Paint paint) {
		origGraphics.setPaint(paint);
		
	}

	public void setStroke(Stroke s) {
		javaStroke = s;
		strokeWidth = (double)((BasicStroke)s).getLineWidth();
	}

	public void setRenderingHint(Key hintKey, Object hintValue) {
		origGraphics.setRenderingHint(hintKey,hintValue);
		
	}

	public Object getRenderingHint(Key hintKey) {
		return origGraphics.getRenderingHint(hintKey);
	}

	public void setRenderingHints(Map arg0) {
		origGraphics.setRenderingHints( arg0);
		
	}

	public void addRenderingHints(Map arg0) {
		origGraphics.addRenderingHints(arg0);
		
	}

	public RenderingHints getRenderingHints() {
		return origGraphics.getRenderingHints();
	}

	public void translate(int x, int y) {
		// TODO Auto-generated method stub
		
	}

	public void translate(double tx, double ty) {
		// TODO Auto-generated method stub
		
	}

	public void rotate(double theta) {
		// TODO Auto-generated method stub
		
	}

	public void rotate(double theta, double x, double y) {
		// TODO Auto-generated method stub
		
	}

	public void scale(double sx, double sy) {
		// TODO Auto-generated method stub
		
	}

	public void shear(double shx, double shy) {
		// TODO Auto-generated method stub
		
	}

	public void transform(AffineTransform Tx) {
		// TODO Auto-generated method stub
		
	}

	public void setTransform(AffineTransform Tx) {
		// TODO Auto-generated method stub
		
	}

	public AffineTransform getTransform() {
		return origGraphics.getTransform();
	}

	public Paint getPaint() {
		return origGraphics.getPaint();
	}

	public Composite getComposite() {
		return origGraphics.getComposite();
	}

	public void setBackground(Color color) {
		bgColor = color;
		
	}

	public Color getBackground() {
		return bgColor;
	}

	public Stroke getStroke() {
		return javaStroke;
	}

	public void clip(Shape s) {
		origGraphics.clip(s);
		
	}

	public FontRenderContext getFontRenderContext() {
		return origGraphics.getFontRenderContext();
	}

	public Graphics create() {
		return origGraphics.create();
	}

	public Color getColor() {
		return javaColor;
	}

	public void setColor(Color c) {
		javaColor = c;
		swfColor = new com.anotherbigidea.flash.structs.AlphaColor(c.getRed(),c.getGreen(),c.getBlue(),100);
	}

	public void setPaintMode() {
		origGraphics.setPaintMode();
		
	}

	public void setXORMode(Color c1) {
		origGraphics.setXORMode(c1);
		
	}

	public Font getFont() {
		return origGraphics.getFont();
	}

	public void setFont(Font font) {
		origGraphics.setFont(font);
		
	}

	public FontMetrics getFontMetrics(Font f) {
		return origGraphics.getFontMetrics(f);
	}

	public Rectangle getClipBounds() {
		return origGraphics.getClipBounds();
	}

	public void clipRect(int x, int y, int width, int height) {
		origGraphics.clipRect(x,y,width,height);
		
	}

	public void setClip(int x, int y, int width, int height) {
		origGraphics.setClip(x,y,width,height);
		
	}

	public Shape getClip() {
		return origGraphics.getClip();
	}

	public void setClip(Shape clip) {
		origGraphics.setClip(clip);
		
	}

	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		// TODO Auto-generated method stub
		
	}

	public void drawLine(int x1, int y1, int x2, int y2) {

		
		
	}

	public void fillRect(int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	public void clearRect(int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
		// TODO Auto-generated method stub
		
	}

	public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
		// TODO Auto-generated method stub
		
	}

	public void drawOval(int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	public void fillOval(int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
		// TODO Auto-generated method stub
		
	}

	public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
		// TODO Auto-generated method stub
		
	}

	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
		// TODO Auto-generated method stub
		
	}

	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		// TODO Auto-generated method stub
		
	}

	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		// TODO Auto-generated method stub
		
	}

	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	


	/**
	 * An example of using the Movie package to create a simple Flash movie
	 * consisting of 4 instances of a Movie Clip containing a rotating red
	 * square.
	 */
	
	    /**
	     * First arg is output filename
	     */
	    public static void main( String[] args ) throws IOException
	    {        
	        Movie movie = new Movie();
	     
	        //--Create a red square with a 2 pixel black outline
	        // centered on (0,0)
	        com.anotherbigidea.flash.movie.Shape shape = new com.anotherbigidea.flash.movie.Shape();
	        shape.defineFillStyle( new com.anotherbigidea.flash.structs.Color(255,0,0) );
	        shape.defineLineStyle( 2.0, new com.anotherbigidea.flash.structs.Color(0,0,0) );
	        shape.setRightFillStyle( 1 );
	        shape.setLineStyle( 1 );    
	        shape.move( -50, -50 );
	        shape.line(  50, -50 );
	        shape.line(  50,  50 );
	        shape.line( -50,  50 );
	        shape.line( -50, -50 );
	        
	        //--Create a Movie Clip (Sprite)
	        MovieClip clip = new MovieClip();
	        Frame f1 = clip.appendFrame();
	        
	        //--Place the red square in the center of the movie clip
	        Instance inst = f1.placeSymbol( shape, 0, 0 );
	        
	        //--Rotate the square (using degrees for clarity)
	        for( int angle = 10; angle < 90; angle += 10 )
	        {
	            //--Convert degrees to radians
	            double radians = angle * Math.PI / 180.0;
	            
	            Frame f = clip.appendFrame();
	            
	            //--Create a rotation matrix
	            Transform matrix = new Transform( radians, 0.0, 0.0 );
	            
	            //--Alter the square using the transformation matrix
	            f.alter( inst, matrix, null );
	        }
	        
	        //--Add a single frame to the movie and give it the stop action to
	        //  prevent it from looping (the Movie Clip loops independently)
	        Frame frame = movie.appendFrame();
	        frame.stop();  
	        
	        //--Place 4 instances of the Movie Clip
	        frame.placeSymbol( clip, 100, 100 );
	        frame.placeSymbol( clip, 300, 100 );
	        frame.placeSymbol( clip, 100, 300 );
	        frame.placeSymbol( clip, 300, 300 );
	        
	        //--Save the movie to the output file
	        movie.write( "flashSquaresTest.swf" );
	    }
	

}
