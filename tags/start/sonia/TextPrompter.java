package sonia;

import java.awt.*;
import java.util.ArrayList;
import java.awt.event.*;


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
  * Tages a suggestion string, shows it to the user, and returns input via get method,
  * also has prompt string for window title
   */
public class TextPrompter
{
  private Dialog dialog;
  private Button OK;
  private TextField field;
  /**
   * Tages a suggestion string, shows it to the user, and returns input via get method,
   * also has prompt string for window title
   * @param promptString string to be used as the window title
   * @pram suggestString text to be displayed as a suggestion in the text area
   */
  public TextPrompter(Frame owner, String suggestString, String promptString)
  {

    dialog = new Dialog(owner,promptString,true);
    field = new TextField(suggestString,suggestString.length()+10);
    OK = new Button("OK");


    OK.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent evt)
      {
        dialog.hide();
      }
    });

    dialog.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(0,2,0,2);
    c.gridx=0;c.gridy=0;c.gridwidth=1;c.gridheight=1;c.weightx=1;c.weighty=1;
    dialog.add(field,c);
    c.gridx=0;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=1;c.weighty=1;
    dialog.add(OK,c);
    dialog.setBackground(Color.lightGray);
    dialog.setLocation(400,400);
    dialog.setSize(400,100);
    dialog.show();

  }

  /**
   * Returns the string entered in the text field by the user
   */
  public String getUserString()
  {
    return field.getText();
  }
}