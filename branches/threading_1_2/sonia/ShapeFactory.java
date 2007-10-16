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

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;


/**
 * this class returns the appropriate shape when passed a string
 * @author skyebend
 *
 */
public class ShapeFactory {
	

	
	public static RectangularShape getShapeFor(String shapeName) throws Exception {
		//rect
		if (shapeName.equalsIgnoreCase("square")) return new Rectangle2D.Double();
		if (shapeName.equalsIgnoreCase("box")) return new Rectangle2D.Double();
		if (shapeName.equalsIgnoreCase("rectangle")) return new Rectangle2D.Double();
		if (shapeName.equalsIgnoreCase("rect")) return new Rectangle2D.Double();
		if (shapeName.equalsIgnoreCase(Rectangle2D.Double.class.getCanonicalName())) return new Rectangle2D.Double();
		//ellipse
		if (shapeName.equalsIgnoreCase("circle")) return new Ellipse2D.Double();
		if (shapeName.equalsIgnoreCase("ellipse")) return new Ellipse2D.Double();
		if (shapeName.equalsIgnoreCase(Ellipse2D.Double.class.getCanonicalName())) return new Ellipse2D.Double();
		
		throw new Exception("Unable to match shape name "+shapeName+" to a shape");
	}
	
	public static String getStringFor(Shape shape){
		if (shape instanceof Ellipse2D) return "ellipse";
		if (shape instanceof Rectangle2D) return "rect";
		return shape.getClass().getCanonicalName();
	}

}
