package sonia;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Frame;
import cern.colt.list.IntArrayList;
import java.util.Vector;

import javax.swing.JComponent;

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
 * Temporarily added to the LayoutWindow to allow the user to reposition nodes
 * by dragging them on the screen. This can be slow due to screen redraws.
 *   Multiple nodes can be selected by shift-clicking
 * and then moved as a group.
 */
public class NodeMover implements MouseListener,MouseMotionListener
{
  private SoniaController control;
  private SoniaLayoutEngine engine;
  private JComponent  frame;
  private LayoutSlice slice;
  private double[] xCoords;
  private double[] yCoords;
  private Vector nodeEvents;
  private RenderSlice nodeView;
  private int selectedIndex;
  private double selectedSize;
  private IntArrayList selected;
  //how for to translate the coords by (because of margins)
  private int left;
  private int top;

  public NodeMover(SoniaController cont, SoniaLayoutEngine eng, JComponent frm)
  {
    control = cont;
    engine = eng;
    frame = frm;
    selected = new IntArrayList();
    //lock things so nothing else will change
    slice = engine.getCurrentSlice();
    slice.setLayoutFinished(false);
    //add this as a lister to the frame to
    frame.addMouseListener(this);
    frame.addMouseMotionListener(this);
    xCoords = engine.getCurrentXCoords();
    yCoords = engine.getCurrentYCoords();
    nodeView = engine.getRenderSlice(slice.getSliceStart(),slice.getSliceEnd());
    nodeEvents = nodeView.getNodeEvents();

    //how for to translate the coords by (because of margins)
    left = engine.getLeftPad();
    top = engine.getTopPad();

  }

  public void mouseMoved(MouseEvent evt){}

  public void mouseDragged(MouseEvent evt)
  {
    //debug
    control.showStatus("ID:"+(selectedIndex+1)+" position:("+(evt.getX()-left)+
                       ","+(evt.getY()-top)+")");
    if ((selected.size() > 0)& selectedIndex>=0)
    {
      int newX = evt.getX()-left;
      int newY = evt.getY()-top;
      double diffX = newX - xCoords[selectedIndex];
      double diffY = newY - yCoords[selectedIndex];

      for (int i=0;i<selected.size() ;i++ )
      {
        int select = selected.get(i);
        xCoords[select] += diffX;
        yCoords[select] += diffY;
      }
      engine.updateDisplays();
    }
  }

  public void mousePressed(MouseEvent e)
  {
    selectedIndex = getTarget(e.getX(),e.getY());
    //if nothing was selected

    if (selectedIndex < 0 )
    {
      selected.removeFromTo(0,selected.size()-1);
    }
    //if the shift key is down, add to selected list
    else if(e.isShiftDown())
    {
      //if it is already selected, deslected
      if (selected.contains(selectedIndex))
      {
        selected.remove(selected.indexOf(selectedIndex));
      }
      else
      {
        //otherwise add that index to the list of selected
        selected.add(selectedIndex);
      }
    }
    else if (!selected.contains(selectedIndex))
    {
      //other wise, remove all the others and add selected
      selected.removeFromTo(0,selected.size()-1);
      selected.add(selectedIndex);
    }
    engine.updateDisplays();
    hiliteSelected();


  }
  public void mouseReleased(MouseEvent e)
  {
    engine.updateDisplays();
    hiliteSelected();
  }
  public void mouseExited(MouseEvent e)
  {
  }
  public void mouseEntered(MouseEvent e)
  {
  }
  public void mouseClicked(MouseEvent e)
  {
  }

  private int getTarget(double x, double y)
  {

    int targetIndex = -1;
    double nodeSize;
    //if overlapp, will return last (topmost?) index
    for (int i = 0; i<nodeEvents.size();i++)
    {
      NodeAttribute node = (NodeAttribute)nodeEvents.get(i);
      nodeSize = node.getNodeSize()/2.0;

      int nodeIndex = node.getNodeId()-1;
      //check for hit
      if (((x-left < xCoords[nodeIndex]+nodeSize) & (x-left > xCoords[nodeIndex]-nodeSize))
          &((y-top < yCoords[nodeIndex]+nodeSize) & (y-top > yCoords[nodeIndex]-nodeSize)))
      {
        targetIndex = nodeIndex;
        selectedSize = nodeSize;  //for later use by the graphics
      }

    }
    return targetIndex;
  }

  private void hiliteSelected()
  {
	  //TODO: hiliting is broken for node mover
    Graphics2D graphics = (Graphics2D)frame.getGraphics();
    graphics.setColor(Color.black);
    graphics.setXORMode(Color.white);
    //now draw the hilighting
    int x;
    int y;
    //shold get size of each node...
    int size = (int)Math.round(selectedSize);
    for (int i=0;i<selected.size() ;i++ )
    {
      int select = selected.get(i);
      x = (int)Math.round(xCoords[select]);
      y = (int)Math.round(yCoords[select]);
      //draw black "handle" box at each corner
      graphics.fillRect(x+left-size-1,y+top-size-1,3,3);
      graphics.fillRect(x+left-size-1,y+top+size-1,3,3);
      graphics.fillRect(x+left+size-1,y+top+size-1,3,3);
      graphics.fillRect(x+left+size-1,y+top-size-1,3,3);
    }
  }

  public void endMoveNodes()
  {

    frame.removeMouseListener(this);
    frame.removeMouseMotionListener(this);
    //make sure the slice's coords get set
    LayoutUtils.applyCoordsToSlice(xCoords,yCoords,slice);
    slice.setLayoutFinished(true);
    control.log("Node positions for slice "+engine.getCurrentSliceNum()+
                " manually adjusted");
  }

}