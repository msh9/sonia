package sonia;

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

import java.util.*;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.text.*;

/**
 * A bin or container for the node and arc events meeting its criteria, it draws
 * them to the screen when asked.  See Layout window for an explanation of the
 * rendering sequence.
 */
public class RenderSlice
{

  private SoniaLayoutEngine layoutEngine;
  private double sliceStart;
  private double sliceEnd;
  private Vector nodeEvents;
  private Vector arcEvents;
  private NumberFormat formater;
  private double flashWindow = 0.1;


  /**
   * Render slice is a bin holding all the objects to be renderd at a given
   * time.  ATTRIBUTES MAY NOT CHANGE WITHIN SLICE (use first or last?)
   * computes nodes coordinates using the interpolation formula, and the
   * coordinates specified by the appropriate layoutSlices
   * (usually the layout slice it falls within, and the next one)
   * Responsible for doing paint calls on objects
   */
  public RenderSlice(SoniaLayoutEngine engine, double startTime, double endTime, Vector nodes,
                     Vector arcs)
  {
    layoutEngine = engine;
    sliceStart = startTime;
    sliceEnd = endTime;
    nodeEvents = nodes;
    arcEvents = arcs;
  }

  public RenderSlice(SoniaLayoutEngine engine,double startTime, double endTime)
  {
    layoutEngine = engine;
    sliceStart = startTime;
    sliceEnd = endTime;
    nodeEvents = new Vector();
    arcEvents = new Vector();
  }

  public void addArcEvent(ArcAttribute arc)
  {
    arcEvents.add(arc);
  }

  public void addNodeEvent(NodeAttribute node)
  {
    nodeEvents.add(node);
  }

  //loops over all objects and asks them to paint themselves
  public void paint(Graphics2D graphics,SoniaCanvas canvas)
  {

    //need to calc new coords for nodes from layout slices
    //STORE COORDS SO THEY DON'T HAVE TO BE RECALC'D EACH TIME?
    //SHOULD IT BE START OR END OF SLICE?
    //ASSUMES ALL LISTS ARE THE SAME SIZE
    double[] xCoords = layoutEngine.getCurrentXCoords();
    double[] yCoords = layoutEngine.getCurrentYCoords();
    //how for to translate the coords by
    int left = layoutEngine.getLeftPad();
    int top = layoutEngine.getTopPad();

    //sets the number of fractional digits to display
    formater = NumberFormat.getInstance(Locale.ENGLISH);
      formater.setMaximumFractionDigits(3);
      formater.setMinimumFractionDigits(3);

    //first do arcs
    //KLUDG check to not draw arcs for speed
    if (!canvas.isHideArcs())
    {

    // check settings before transparency
    if (canvas.isArcTrans())
    {
      graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
          canvas.getArcTransVal()));
    }


    ArcAttribute arc;
    int fromId;
    int toId;
    Font originalFont = graphics.getFont();
    graphics.setFont(originalFont.deriveFont(10)); //this should be adjustable

    //LOOP OVER ARC EVENTS
    for (int i=0;i<arcEvents.size();i++)
    {
      arc = (ArcAttribute)arcEvents.get(i);
      //check if we should flash it
      if (canvas.isFlashNew())
      {
          //
          double flashStart = arc.getObsTime();
          double flashEnd = flashStart+canvas.getFlashDuration();
          //flash if it is within the interval
          if(((flashStart >= sliceStart) & (flashStart < sliceEnd))
         | ((flashStart <= sliceStart) & (flashEnd > sliceEnd)))
          {
              arc.flash();
          }       
      }
      //correct for id ofset 0 -> 1
      fromId = arc.getFromNodeId()-1;
      toId = arc.getToNodeId()-1;
      //check if doing arcs at all
      if (!canvas.isHideArcs())
      {
        //translate coords to allow for visual insets
        arc.paint(graphics,canvas,xCoords[fromId]+left,yCoords[fromId]+top,
        xCoords[toId]+left,yCoords[toId]+top);
      }
    //CHECK IF WEIGHT LABELS ARE TO BE DRAWN !Not the same as arc labels!
    if (canvas.isShowArcWeights())
    {
      double sliceArcWeight = layoutEngine.getCurrentSlice().getSymMaxArcWeight(fromId,toId);
      graphics.setColor(Color.darkGray);
      float labelX = (float)(xCoords[fromId]+left + (xCoords[toId]-xCoords[fromId])/2);
      float labelY = (float)(yCoords[fromId]+top + (yCoords[toId]-yCoords[fromId])/2);
      //round the arc weight

      graphics.drawString(""+sliceArcWeight,labelX,labelY);
    }

  } //end arc event loop

    }

  //set transparency back to solid
  graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

  //NODE EVENT LOOP
  //then do nodes (so nodes are on top)
  for (int i=0;i<nodeEvents.size();i++)
    {
      NodeAttribute node = (NodeAttribute)nodeEvents.get(i);
      if (canvas.isFlashNew())
      {
          //
          double flashStart = node.getObsTime();
          double flashEnd = flashStart+canvas.getFlashDuration();
          //flash if it is within the interval
          if(((flashStart >= sliceStart) & (flashStart < sliceEnd))
         | ((flashStart <= sliceStart) & (flashEnd > sliceEnd)))
          {
              node.flash();
          }         
      }
      int index = node.getNodeId()-1;
      node.paint(graphics,canvas,xCoords[index]+left,yCoords[index]+top);
    }

    //debug show slice stats
    if (canvas.isShowStats())
    {
      //round and format the slice times

      graphics.setColor(Color.gray);

      graphics.drawString("slice:"+layoutEngine.getCurrentSliceNum()+
                          " time:"+formater.format(sliceStart)+
                          "-"+formater.format(sliceEnd),5,10);
      graphics.drawString(" layout:"+layoutEngine.getLayoutInfo(),5,20);
    }
  }

  //accessors (not allowed to set in case objects would be at wrong time)
  public double getSliceStart()
  {
    return sliceStart;
  }
  public double getSliceEnd()
  {
    return sliceEnd;
  }
  public Vector getArcEvents()
  {
    return arcEvents;
  }

  public Vector getNodeEvents()
  {
    return nodeEvents;
  }

  public String toString()
  {
    return "start:"+sliceStart+" end:"+sliceEnd+" nodes:"+nodeEvents.toString()+
        " arcs:"+arcEvents.toString();
  }
}