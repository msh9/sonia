package sonia.layouts;

import sonia.LayoutSlice;
import sonia.settings.ApplySettings;
import sonia.ui.ApplySettingsDialog;

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
 * Interface for layout algorithms which allows them to be interchangeable.
 */
public interface NetLayout
{

  public void applyLayoutTo(LayoutSlice slice,
                            int width, int height, ApplySettings settings);

  /**
   * Asks the layout to add layout specific propertyName/defaultValue pairs
   * to the dialog to the layout settings dialog.
   * @param settings the dialog to add the settings to
   */
  public void setupLayoutProperties(ApplySettingsDialog settings);

  /**
   * Returns a string giving the name of the kind of layout.
   */
  public String getLayoutType();

  /**
   * Returns a string with several lines of text describing the layout settings.
   */
  public String getLayoutInfo();

  /**
   * Interupts the layout if it is running.
   */
  public void pause();

  /**
   * Un-sets the interput flag, but does not actually restart he layout
   */
  public void resume();


  /**
   * gives the layout a chance to dispose of any associate objects or windows
   * (such as the cooling schedule)
   */
  public void disposeLayout();
  



}