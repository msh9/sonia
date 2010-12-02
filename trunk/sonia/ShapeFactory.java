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

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;

/**
 * this class returns the appropriate shape when passed a string
 * 
 * @author skyebend
 * 
 */
public class ShapeFactory {

	public static final String[] shapeNames = {"ellipse","rect","diamond","roundrect","triangle","none"};
	
	public static RectangularShape getShapeFor(String shapeName)
			throws Exception {
		// rect
		if (shapeName.equalsIgnoreCase("square"))
			return new Rectangle2D.Double();
		if (shapeName.equalsIgnoreCase("box"))
			return new Rectangle2D.Double();
		if (shapeName.equalsIgnoreCase("rectangle"))
			return new Rectangle2D.Double();
		if (shapeName.equalsIgnoreCase("rect"))
			return new Rectangle2D.Double();
		if (shapeName.equalsIgnoreCase(Rectangle2D.Double.class
				.getCanonicalName()))
			return new Rectangle2D.Double();
		// ellipse
		if (shapeName.equalsIgnoreCase("circle"))
			return new Ellipse2D.Double();
		if (shapeName.equalsIgnoreCase("ellipse"))
			return new Ellipse2D.Double();
		if (shapeName.equalsIgnoreCase(Ellipse2D.Double.class
				.getCanonicalName()))
			return new Ellipse2D.Double();
		// diamond
		if (shapeName.equalsIgnoreCase("diamond"))
			return new Diamond2D();
		if (shapeName.equalsIgnoreCase(Diamond2D.class
				.getCanonicalName()))
			return new Diamond2D();
		// triangle
		if (shapeName.equalsIgnoreCase("triangle"))
			return new Triangle2D();
		if (shapeName.equalsIgnoreCase(Triangle2D.class
				.getCanonicalName()))
			return new Diamond2D();
		// roundrect
		if (shapeName.equalsIgnoreCase("roundrect"))
			return new RoundRectangle2D.Double(0, 0, 0, 0, 10, 10);
		if (shapeName.equalsIgnoreCase("roundedrectangle"))
			return new RoundRectangle2D.Double(0, 0, 0, 0, 10, 10);
		if (shapeName.equalsIgnoreCase(RoundRectangle2D.Double.class
				.getCanonicalName()))
			return new RoundRectangle2D.Double(0, 0, 0, 0, 10, 10);
		// none, noshape
		if (shapeName.equalsIgnoreCase("none"))
			return new NoShape2D();
		if (shapeName.equalsIgnoreCase("noshape"))
			return new NoShape2D();
		if (shapeName.equalsIgnoreCase(NoShape2D.class
				.getCanonicalName()))
			return new NoShape2D();

		throw new Exception("Unable to match shape name " + shapeName
				+ " to a shape");
	}

	public static String getStringFor(Shape shape) {
		if (shape instanceof Ellipse2D)
			return "ellipse";
		if (shape instanceof Rectangle2D)
			return "rect";
		if (shape instanceof Diamond2D)
			return "diamond";
		if (shape instanceof RoundRectangle2D)
			return "roundrect";
		if (shape instanceof Triangle2D)
			return "triangle";
		if (shape instanceof NoShape2D)
			return "none";
		return shape.getClass().getCanonicalName();
	}
	


	/**
	 * trying to implement a diamond shape by overiding a rectangle.
	 * Intersections may not work correctly (use rect bounds)
	 * 
	 * @author skyebend
	 * 
	 */
	public static class Diamond2D extends RectangularShape {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public double x;
		public double y;
		public double height;
		public double width;

		private Polygon diamond;

		/**
		 * create the shape to match the current settings
		 */
		private void updateShape() {

			if (diamond == null) {
				int[] xpoints = new int[4];
				int[] ypoints = new int[4];
				diamond = new Polygon(xpoints,ypoints,4);
				
			} 
			diamond.xpoints[0] = (int)Math.round(x + (width / 2.0));
			diamond.xpoints[1] = (int)Math.round(x + width );
			diamond.xpoints[2] = (int)Math.round(x + (width / 2.0));
			diamond.xpoints[3] = (int)Math.round(x);
			
			diamond.ypoints[0] = (int)Math.round(y);
			diamond.ypoints[1] = (int)Math.round(y + (height / 2.0));
			diamond.ypoints[2] = (int)Math.round(y + height);
			diamond.ypoints[3] = (int)Math.round(y + (height / 2.0));
			
		}

		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj instanceof Diamond2D) {
				Diamond2D r2d = (Diamond2D) obj;
				return ((getX() == r2d.getX()) && (getY() == r2d.getY())
						&& (getWidth() == r2d.getWidth()) && (getHeight() == r2d
						.getHeight()));
			}
			return false;
		}

		@Override
		public PathIterator getPathIterator(AffineTransform at, double flatness) {
			updateShape();
			return diamond.getPathIterator(at, flatness);
		}

		@Override
		public PathIterator getPathIterator(AffineTransform at) {
			updateShape();
			return diamond.getPathIterator(at);
		}


		@Override
		public double getHeight() {
			return height;
		}

		@Override
		public double getWidth() {
			return width;
		}

		@Override
		public double getX() {
			return x;
		}

		@Override
		public double getY() {
			return y;
		}

		@Override
		public boolean isEmpty() {
			return (width <= 0.0) || (height <= 0.0);
		}

		@Override
		public boolean contains(double x, double y, double w, double h) {
			return diamond.contains(x, y, w, h);
		}

		@Override
		public boolean contains(double x, double y) {
			return diamond.contains(x, y);
		}

		@Override
		public boolean contains(Point2D p) {
			return diamond.contains(p);
		}

		@Override
		public boolean contains(Rectangle2D r) {
			// TODO Auto-generated method stub
			return super.contains(r);
		}

		@Override
		public Rectangle getBounds() {
			return diamond.getBounds();
		}

		
		@Override
		public boolean intersects(Rectangle2D r) {
			return diamond.intersects(r);
		}

		@Override
		public void setFrame(double x, double y, double w, double h) {
			this.x = x;
			this.y = y;
			this.width = w;
			this.height = h;
			
		}

		@Override
		public Rectangle2D getBounds2D() {
			return diamond.getBounds2D();
		}

		@Override
		public boolean intersects(double x, double y, double w, double h) {
			return diamond.intersects(x, y, w, h);
		}
		
		

	}
	
	/**
	 * trying to implement a triangle shape by overiding a rectangle.
	 * Intersections may not work correctly (use rect bounds)
	 * 
	 * @author skyebend
	 * 
	 */
	public static class Triangle2D extends RectangularShape {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public double x;
		public double y;
		public double height;
		public double width;

		private Polygon triangle;

		/**
		 * create the shape to match the current settings
		 */
		private void updateShape() {

			if (triangle == null) {
				int[] xpoints = new int[3];
				int[] ypoints = new int[3];
				triangle = new Polygon(xpoints,ypoints,3);
				
			} 
			triangle.xpoints[0] = (int)Math.round(x + (width / 2.0));
			triangle.xpoints[1] = (int)Math.round(x + width );
			triangle.xpoints[2] = (int)Math.round(x);
			
			triangle.ypoints[0] = (int)Math.round(y);
			triangle.ypoints[1] = (int)Math.round(y + height);
			triangle.ypoints[2] = (int)Math.round(y + height);
			
		}

		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj instanceof Triangle2D) {
				Triangle2D r2d = (Triangle2D) obj;
				return ((getX() == r2d.getX()) && (getY() == r2d.getY())
						&& (getWidth() == r2d.getWidth()) && (getHeight() == r2d
						.getHeight()));
			}
			return false;
		}

		@Override
		public PathIterator getPathIterator(AffineTransform at, double flatness) {
			updateShape();
			return triangle.getPathIterator(at, flatness);
		}

		@Override
		public PathIterator getPathIterator(AffineTransform at) {
			updateShape();
			return triangle.getPathIterator(at);
		}


		@Override
		public double getHeight() {
			return height;
		}

		@Override
		public double getWidth() {
			return width;
		}

		@Override
		public double getX() {
			return x;
		}

		@Override
		public double getY() {
			return y;
		}

		@Override
		public boolean isEmpty() {
			return (width <= 0.0) || (height <= 0.0);
		}

		@Override
		public boolean contains(double x, double y, double w, double h) {
			return triangle.contains(x, y, w, h);
		}

		@Override
		public boolean contains(double x, double y) {
			return triangle.contains(x, y);
		}

		@Override
		public boolean contains(Point2D p) {
			return triangle.contains(p);
		}

		@Override
		public boolean contains(Rectangle2D r) {
			// TODO Auto-generated method stub
			return super.contains(r);
		}

		@Override
		public Rectangle getBounds() {
			return triangle.getBounds();
		}

		
		@Override
		public boolean intersects(Rectangle2D r) {
			return triangle.intersects(r);
		}

		@Override
		public void setFrame(double x, double y, double w, double h) {
			this.x = x;
			this.y = y;
			this.width = w;
			this.height = h;
			
		}

		@Override
		public Rectangle2D getBounds2D() {
			return triangle.getBounds2D();
		}

		@Override
		public boolean intersects(double x, double y, double w, double h) {
			return triangle.intersects(x, y, w, h);
		}
		
		

	}
	
	/**
	 * Implement "none" or "No Shape" shape that draws nothing for the node
	 * 
	 * @author skyebend
	 * 
	 */
	public static class NoShape2D extends RectangularShape {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public double x;
		public double y;
		public static final double height = 0.0;
		public static final double width = 0.0;
		private Polygon point;

		/**
		 * create the shape to match the current settings
		 */
		private void updateShape() {

			if (point == null) {
				int[] xpoints = new int[1];
				int[] ypoints = new int[1];
				point = new Polygon(xpoints,ypoints,1);
				
			} 
			point.xpoints[0] = (int)Math.round(x);
			point.ypoints[0] = (int)Math.round(y);;
			
		}

		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj instanceof NoShape2D) {
				Triangle2D r2d = (Triangle2D) obj;
				return ((getX() == r2d.getX()) && (getY() == r2d.getY()));
			}
			return false;
		}

		@Override
		public PathIterator getPathIterator(AffineTransform at, double flatness) {
			updateShape();
			return point.getPathIterator(at, flatness);
		}

		@Override
		public PathIterator getPathIterator(AffineTransform at) {
			updateShape();
			return point.getPathIterator(at);
		}


		@Override
		public double getHeight() {
			return height;
		}

		@Override
		public double getWidth() {
			return width;
		}

		@Override
		public double getX() {
			return x;
		}

		@Override
		public double getY() {
			return y;
		}

		@Override
		public boolean isEmpty() {
			return (width <= 0.0) || (height <= 0.0);
		}

		@Override
		public boolean contains(double x, double y, double w, double h) {
			return point.contains(x, y, w, h);
		}

		@Override
		public boolean contains(double x, double y) {
			return point.contains(x, y);
		}

		@Override
		public boolean contains(Point2D p) {
			return point.contains(p);
		}

		@Override
		public boolean contains(Rectangle2D r) {
			// TODO Auto-generated method stub
			return super.contains(r);
		}

		@Override
		public Rectangle getBounds() {
			return point.getBounds();
		}

		
		@Override
		public boolean intersects(Rectangle2D r) {
			return point.intersects(r);
		}

		@Override
		/**
		 * ignores width and height
		 */
		public void setFrame(double x, double y, double w, double h) {
			this.x = x;
			this.y = y;
			
		}

		@Override
		public Rectangle2D getBounds2D() {
			return point.getBounds2D();
		}

		@Override
		public boolean intersects(double x, double y, double w, double h) {
			return point.intersects(x, y, w, h);
		}
		
		

	}

}
