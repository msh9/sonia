package sonia;

import java.util.*;

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
 * CoosineInterpolation calculates a slow-in, slow-out sinusoidal interpolation
 * between the node coordinates of the current slice and the coordinates in the
 * destination slice. This makes
 * it so that the nodes appear to move more slowly at the start and end of the
 * transition, giving them a more natural sense of inertia, and making the overall
 * layout sequence smoother.
 *
 */
public class CosineInterpolation implements CoordInterpolator
{
  //private LayoutSlice[] layoutSlices;
  //private int numSlices;

  /**
   * Instantiates the consine interpolator.  NO VARABLES ARE SET, MAKE THIS INTO
   * A STATIC CLASS?
   */
  public CosineInterpolation()
  {

  }
/*
  public void setLayoutSlices(Vector slices)
  {
    numSlices = slices.size();
    layoutSlices = new LayoutSlice[numSlices];
    for (int i=0;i<numSlices; i++)
    {
      layoutSlices[i]=(LayoutSlice)slices.get(i);
    }
  }
  */


  //SHOULD COMBINE X AND Y INTO ONE CALL
  /**
   * Returns an array of coordinates corresponding to the x coords of each node,
   * interpolated between the coordinates of the startSlice and the nextSlice.
   * The interpolation formula is
   * <BR><BR>
   * fromX[i]+((1-Math.cos(Math.PI*((time-startTime)
   * /interval)))/2)*(toX[i]-fromX[i])
   * <BR><BR>where fromX is the the coordinate in the
   * start slice, toX is the coordinate in the destination slice, startTime is
   * the start of the first slice, interval is the the difference of the slice
   * start times, and time is the point in time for which the interpolation
   * is being calculated.
   * @param startSlice the LayoutSlice to provide the starting coordinates
   * @param nextSlice the LayoutSlice to provide the ending coordinates
   * @param time the time at which the interpolation is evaluated
   * @return double array containing the x coords for each node.
   */
  public double[] interpXCoords(LayoutSlice startSlice, LayoutSlice nextSlice,
                                double time)
  {
    //WHAT HAPPENS WHEN SLICES OVERLAPP? Average?
    int numNodes = startSlice.getMaxNumNodes();
    double startTime = startSlice.getSliceStart();
    double interval = nextSlice.getSliceStart()-startTime;
    double[] returnX = new double[numNodes];
    double[] fromX = startSlice.getXCoords();
    double[] toX = nextSlice.getXCoords();
    //do the interpolation
    for (int i=0;i<numNodes;i++)
    {
      returnX[i]= fromX[i]+((1-Math.cos(Math.PI*((time-startTime)/interval)))/2)
              *(toX[i]-fromX[i]);
    }
    return returnX;
  }

  /**
    * Returns an array of coordinates corresponding to the y coords of each node,
    * interpolated between the coordinates of the startSlice and the nextSlice.
    * The interpolation formula is
    * <BR><BR>
    * fromY[i]+((1-Math.cos(Math.PI*((time-startTime)
    * /interval)))/2)*(toY[i]-fromY[i])
    * <BR><BR>where fromY is the the coordinate in the
    * start slice, toY is the coordinate in the destination slice, startTime is
    * the start of the first slice, interval is the the difference of the slice
    * start times, and time is the point in time for which the interpolation
    * is being calculated.
    * @param startSlice the LayoutSlice to provide the starting coordinates
    * @param nextSlice the LayoutSlice to provide the ending coordinates
    * @param time the time at which the interpolation is evaluated
   * @return double array containing the y coords for each node.
   */
   public double[] interpYCoords(LayoutSlice startSlice, LayoutSlice nextSlice,
                                 double time)
 {
   //WHAT HAPPENS WHEN SLICES OVERLAPP? Average?
     int numNodes = startSlice.getMaxNumNodes();
     double startTime = startSlice.getSliceStart();
     double interval = nextSlice.getSliceStart()-startTime;
     double[] returnY = new double[numNodes];
     double[] fromY = startSlice.getYCoords();
     double[] toY = nextSlice.getYCoords();
     //do the interpolation
     for (int i=0;i<numNodes;i++)
     {
       returnY[i]= fromY[i]+((1-Math.cos(Math.PI*((time-startTime)/interval)))/2)
               *(toY[i]-fromY[i]);
     }
    return returnY;
  }

  /**
   * Returns a string giving the name of the interpolation.  In this case "Cosine
   * Interpolation".
   */
 public String getType()
 {
   return "Cosine Interpolation";
 }
}