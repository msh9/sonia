package sonia.layouts;

import sonia.ApplySettingsDialog;
import sonia.LayoutSlice;
import sonia.NetLayout;
import sonia.SoniaController;
import sonia.SoniaLayoutEngine;

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
  * CircleLayout is one of the most boring network layouts, it just puts all
  * nodes in a circle in the middle of the screen.  Order is fairly arbirary,
  * roughly the same as they were in file. (Should be able to set for a
   * alphabetic order or id order).  The only parameter is the radius of the
   * circle.
  */
public class CircleLayout implements NetLayout
{
  private double pageWidth;
  private double pageHeight;
  private SoniaController control;
  private SoniaLayoutEngine engine;
  private String layoutInfo = "";

  /**
   * CircleLayout is one of the most boring network layouts, it just puts all
   * nodes in a circle in the middle of the screen.  Order is fairly arbirary,
   * roughly the same as they were in file. Should be able to set for a
   * alphabetic order or id order. Includes a paramter for setting the radius
   * of the circle.
   * @param cont the main SoniaController
   * @pram eng the engine for the layout it will work on
   */
  public CircleLayout(SoniaController cont,SoniaLayoutEngine eng)
  {
    control = cont;
    engine = eng;
  }

  /**
   * adds the layout specific "radius" paramter to the passed settings dialog. For
   * circle layout this adds the "radius" parameter which controls the radius
   * of the circle in pixels.
   * @param settings the settings dialog which the prameter will be added to
   */
  public void setupLayoutProperties(ApplySettingsDialog settings)
  {
    settings.addLayoutProperty("radius",110);
  }

  /**
   * should pause the layout, but for circle layout it does nothing, since there
   * is only one very fast pass anyway
   */
  public void pause()
  {
  }

  /**
  * should resume the layout, but for circle layout it does nothing, since there
  * is only one very fast pass anyway
   */
  public void resume()
  {
  }

  /**
   * applies the circular layout to the passed slice, using the passed with and
   * height, and the settings from the passed settings dialog.  Circle will be
   * centered on the layout window with padding for window ornaments. Radius is
   * controlled by "radius" parameter in apply settings dialog.
   * @param slice the slice to run on
   * @param width the width of the layout area
   * @param height the height of the layout area
   * @param settings the settings to use
   */
  public void applyLayoutTo(LayoutSlice slice, int width, int height,
                            ApplySettingsDialog settings)
      //if not use defaults, should ask for params (alpha ordering, id ordering..)
  //radius
  {
    pageWidth = (double)width;
    pageHeight = (double)height - 75; //to allow room for buttons on the bottom
    //figure out which dimesion is smaller and radius of circle to fit
    double radius = settings.getLayoutProperty("radius");
    //put the radius as one of the params
    layoutInfo = "radius:"+radius;
    double originX = (pageWidth / 2.0);
    double originY = (pageHeight / 2.0)+10;//to allow a little more space on top
    //should figure out how many nodes are actually in slice
    int n = slice.getMaxNumNodes();
    //loop over all the nodes and set their coords on the circle
    //NOTE: ONLY CHANGES SLICE COORDS, DOES NOT EFFECT NODEATTRIBUTE OBJECTS
    for (int i = 0; i < n; i++)
    {
      double x = (radius * Math.cos(2 * Math.PI * i / n));
      double y = (radius * Math.sin(2 * Math.PI * i / n));
      slice.setCoords(i,originX+x,originY+y);
    }
    engine.finishLayout(this,slice,width,height);

  }

  /**
   * returns information about the layout, in this case the radius
   */
  public String getLayoutInfo()
  {
    return layoutInfo;
  }

  /**
   * returns the name of the layout, in this case "Circle Layout"
   */
  public String getLayoutType()
  {
    return "Circle Layout";
  }
  public void disposeLayout()
  {
    //need to get rid of layout settigns dialog?
  }
}