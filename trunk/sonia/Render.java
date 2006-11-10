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

import java.awt.Graphics2D;

/**
 * abstracts the painting calls need to draw the network to make it possible to
 * divert them to differnt kinds of drawing calls such as Graphics2D or SWF
 * 
 * @author skyebend
 * 
 */
public interface Render {

	/**
	 * the target is where the painting will be done usually a graphics context
	 * like Graphics2D. THIS METHOD MUST BE CALLED BEFORE ANY OF THE PAINTING
	 * 
	 * @author skyebend
	 * @param drawTarget
	 */
	public void setDrawingTarget(Object drawTarget);

	public void paintArc(ArcAttribute arc, float widthFactor, double fromX,
			double fromY, double toX, double toY, boolean flash,
			boolean arrows, boolean labels);

	public void paintNode(NodeAttribute node, double xCoord, double yCoord,
			 double scaleFact);

	public void paintNodeLabels(NodeAttribute node, double xCoord,
			double yCoord, double scaleFact, boolean showLabels, boolean showId);

	public void paintStats(String stats);

	public void setTransparency(float trans);

}
