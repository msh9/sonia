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
 * NoInterpolation just returns the coords of the layout which the time lies
 * within.  PROBLEM IS WHEN SLICES OVERLAP
 */
public class NoInterpolation implements CoordInterpolator
{
  private LayoutSlice[] layoutSlices;
  private int numSlices;

  public NoInterpolation()
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
  public double[] interpXCoords(LayoutSlice startSlice, LayoutSlice nextSlice,
                                double time)
  {
    //WHAT HAPPENS WHEN SLICES OVERLAPP? Average?
    return startSlice.getXCoords();
  }

 public double[] interpYCoords(LayoutSlice startSlice, LayoutSlice nextSlice,
                               double time)
 {
  //WHAT HAPPENS WHEN SLICES OVERLAPP? Average?

  return startSlice.getYCoords();
  }


 public String getType()
 {
   return "No Interpolation";
 }
}