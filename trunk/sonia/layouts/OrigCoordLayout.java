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
 * OrigCoordLayout does nothing, since the idea is to use the original
 * coordinates from the parsed file, but the rescale and isolate options do work
 */
public class OrigCoordLayout implements NetLayout
{
  private SoniaController control;
  private SoniaLayoutEngine engine;

  public OrigCoordLayout(SoniaController cont,SoniaLayoutEngine eng)
  {
    control = cont;
    engine = eng;
  }

  public void setupLayoutProperties(ApplySettingsDialog settings){}

  public void applyLayoutTo( LayoutSlice slice,
                            int width, int height,
                            ApplySettingsDialog settings)
  {
    engine.finishLayout(this,slice,width,height);
  }
  public void pause()
  {
  }
  public void resume()
  {
  }
  public String getLayoutInfo()
  {
   return "should return info about original file";
  }

  public String getLayoutType()
  {
    return "Original Coordinate Layout";
  }
  public void disposeLayout()
  {
    //need to get rid of layout settigns dialog?
  }
}