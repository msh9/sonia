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
   * Takes a list, creates a dialog presenting the user with a list view of all the objects on it,
   * and returns the one chosen by the user
   *
   * @deprecated  no longer used, delte
   */
//TODO: no longer used, delete this class
public class ListPicker extends Object
{
  private Dialog dialog;
  private Button OK;
  private List displayList;
  private Object pickedObject = null;
  private ArrayList pickList;

  public ListPicker(Frame owner, ArrayList list, String promptString)
  {

    int numItems = list.size();
    dialog = new Dialog(owner,promptString,true);
    OK = new Button("OK");
    pickList = list;
    displayList = new List(numItems);
    for (int i=0;i<numItems ;i++ )
    {
      //add the string representation of each object in the pickList
      displayList.add(((Object)pickList.get(i)).toString());
    }
    displayList.select(0);
    displayList.setBackground(Color.white);

    OK.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent evt)
      {
        int index = displayList.getSelectedIndex();
        pickedObject = (Object)pickList.get(index);
        dialog.hide();
      }
    });

    dialog.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(0,2,0,2);
    c.gridx=0;c.gridy=0;c.gridwidth=1;c.gridheight=1;c.weightx=1;c.weighty=1;
    dialog.add(displayList,c);
    c.gridx=0;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=1;c.weighty=1;
    dialog.add(OK,c);
    dialog.setBackground(Color.lightGray);
    dialog.setSize(200,300);
    dialog.setLocation(100,100);
    dialog.show();

  }
  public Object getPickedObject()
  {
    return pickedObject;
  }
}