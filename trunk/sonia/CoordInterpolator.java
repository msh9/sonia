package sonia;
import java.util.Vector;

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

/**
 * Interface for interpolation schemes to be used in generating coordinates for
 * nodes when rendering slices between slice layouts.   SoNIA uses a
 * coordinate-interpolation approach when deciding where to place the
 * nodes on the tweening render slices.  Although other possibilities are allowed,
 * the default is to use the CosineInterpolation class to create slow-in, slow-out
 * sinusoidal interpolation.  This makes
 * it so that the nodes appear to move more slowly at the start and end of the
 * transition, giving them a more natural sense of inertia, and making the overall
 * layout sequence smoother.   AT SOME POINT THIS SHOULD BE REVISITED TO DEAL MORE
 * ELEGANTLY WITH OVERLAPPING SLICES...
 */
public interface CoordInterpolator
{
  //public void setLayoutSlices(Vector slices);
  /**
   * Returns an array of values for the x coordinates, interpolating between the
   * coordinates of currentSlice and nextSlices based on the value of time.
   * @param currentSlice the layoutSlice with the starting coordinates
   * @param nextSlice the layoutSlice with the ending coordinates
   * @param time the time for which to compute the interpolation
   * @return double array containing the x coordinate for each node
   */
  public double[] interpXCoords(LayoutSlice currentSlice ,LayoutSlice nextSlice,
                                                      double time);

  /**
   * Returns an array of values for the x coordinates, interpolating between the
   * coordinates of currentSlice and nextSlices based on the value of time.
   * @param currentSlice the layoutSlice with the starting coordinates
   * @param nextSlice the layoutSlice with the ending coordinates
   * @param time the time for which to compute the interpolation
   * @return double array containing the x coordinate for each node
   */
  public double[] interpYCoords(LayoutSlice currentSlice ,LayoutSlice nextSlice,
                                double time);

  /**
   * Returns a String with the name of the layout
   * @return string with the name of the layout
   */
  public String getType();

}