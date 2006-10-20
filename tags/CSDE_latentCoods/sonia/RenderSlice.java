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
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.GeneralPath;
import java.awt.geom.IllegalPathStateException;
import java.awt.geom.Line2D;
import java.text.*;

import cern.colt.function.IntObjectProcedure;

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
//sets the number of fractional digits to display
  private static NumberFormat formater = NumberFormat.getInstance(Locale.ENGLISH);

	    
  //private double flashWindow = 0.1;
  private Line2D arcLine = new Line2D.Double();   //the path to draw
  private GeneralPath headPath = new GeneralPath();
  private double arrowLength = 10;
  private static Color flashColor = Color.yellow;
  private static float flashFactor = 4.0f; //how large to expand new events when they are flashed
  //a flyweight for holding the various strokes
  private static HashMap strokeTable = new HashMap();

  

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
    formater.setMaximumFractionDigits(3);
    formater.setMinimumFractionDigits(3);
  }

  public RenderSlice(SoniaLayoutEngine engine,double startTime, double endTime)
  {
    layoutEngine = engine;
    sliceStart = startTime;
    sliceEnd = endTime;
    nodeEvents = new Vector();
    arcEvents = new Vector();
    formater.setMaximumFractionDigits(3);
    formater.setMinimumFractionDigits(3);
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

    Color origColor = graphics.getColor();
    Font originalFont = graphics.getFont();

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
              arc.setFlash(true);
          }       
      }
      //correct for id ofset 0 -> 1
      fromId = arc.getFromNodeId()-1;
      toId = arc.getToNodeId()-1;
      //check if doing arcs at all
      if (!canvas.isHideArcs())
      {
        //translate coords to allow for visual insets
        paintArc(arc,graphics,canvas,xCoords[fromId]+left,yCoords[fromId]+top,
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
    graphics.setFont(originalFont);
    graphics.setColor(origColor);
    }

  // check settings before node transparency
  if (canvas.isNodeTrans())
  {
      graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
          canvas.getNodeTransVal()));
  }
  else
  {
      graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
  }
      
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

   graphics.setFont(originalFont);
    //debug show slice stats
    if (canvas.isShowStats())
    {
      //round and format the slice times

      graphics.setColor(Color.gray);

      graphics.drawString("        slice:"+layoutEngine.getCurrentSliceNum()+
                          "  time:"+formater.format(sliceStart)+
                          "-"+formater.format(sliceEnd),5,10);
    //  graphics.drawString(" layout:"+layoutEngine.getLayoutInfo(),5,20);
    }
    graphics.setColor(origColor);
  }
  
  private BasicStroke getStrokeForWidth(float width, boolean isNegitive){
	  if (isNegitive){
		  width = width*-1;
		  //TODO: negitive weights not working correctly
	  }
	  width = Math.abs(width);
	  Float key = new Float(width);
	  if (strokeTable.containsKey(key)){
		  //return the stroke already made for this width
		  return (BasicStroke)strokeTable.get(key);
	  }
	  else {
	//do the dashing
		 float dashSkip = 0.0f;
		 float dashLength = 2.0f;
		 
		 if (isNegitive){
		    dashSkip = width;
		    dashLength = 2.0f * width;
		 }
		 float[] dash = {dashLength,dashSkip};
		  BasicStroke newStroke = new BasicStroke(width,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,
                  1.0f,dash,0.0f);
		  strokeTable.put(key,newStroke);
		  return newStroke;
	  }
  }
  
  private void paintArc(ArcAttribute arc, Graphics2D graphics, SoniaCanvas canvas, 
		  double fromX, double fromY,double toX, double toY)
{
  //check if drawing arc
  if (!canvas.isHideArcs())
  {
   
    float drawWidth = (float)arc.getArcWidth()*canvas.getArcWidthFact();
 
    

    graphics.setStroke(getStrokeForWidth(drawWidth,arc.isNegitive()));
    graphics.setColor(arc.getArcColor());
    //should correct for width of node (and length of arrow?)
    arcLine.setLine(fromX,fromY,toX,toY);
    graphics.draw(arcLine);
    
      //if it has never been drawn, than draww it very large so it will show
    if (arc.shouldFlash())
    {
        graphics.setStroke(getStrokeForWidth(drawWidth+flashFactor,arc.isNegitive()));
        graphics.setColor(flashColor);
        graphics.draw(arcLine);
        arc.setFlash(false); //so we only draw once, even if stay on same slice..?
    }
    
    //should turn off dashing
   // dashSkip = 0.0f;

    // CHECK IF ARROWS ARE TO BE DRAWN
    if (canvas.isShowArrows())
    {

      //reset the arrowhead path and make arrowhead
      headPath.reset();
      double arrowSize = arrowLength+drawWidth;
      double xDiff = (fromX - toX);
      double yDiff = (fromY - toY);
      double lineAngle = Math.atan((xDiff) / (yDiff));
      //trap cases where xDiff and yDiff are zero to stop strange PRException onPC
      if (Double.isNaN(lineAngle))
      {
        lineAngle = 0.0;
      }
      if (yDiff < 0)  //rotate by 180
      {
        lineAngle += Math.PI;
      }
      try  //for concurrency problems on dual processor machines...
      {
      //tip of arrow
      headPath.moveTo((float)toX, (float)toY);
      //one wedge
      headPath.lineTo((float)(toX + (arrowSize * Math.sin(lineAngle-0.3))),
                      (float)(toY + (arrowSize * Math.cos(lineAngle-0.3))));
      //other wedge
      headPath.lineTo((float)(toX + (arrowSize * Math.sin(lineAngle+0.3))),
                      (float)(toY + (arrowSize * Math.cos(lineAngle+0.3))));
      //back to top
      headPath.closePath();
      graphics.fill(headPath);
      }
      catch (IllegalPathStateException e)
      {
          System.out.println("Arrow Drawing error: x:"+toX+" y:"+toY);
          e.printStackTrace();
      }

    }//end draw arcs
  }
  //CHECK IF LABELS ARE TO BE DRAWN
  if (canvas.isShowArcLabels())
  {
    graphics.setColor(arc.getLabelColor());
    Font originalFont = graphics.getFont();
    graphics.setFont(originalFont.deriveFont(arc.getLabelSize()));
    float labelX = (float)(fromX+ (toX-fromX)/2);
    float labelY = (float)(fromY + (toY-fromY)/2);
    graphics.drawString(arc.getArcLabel(),labelX,labelY);
  }

  //detecting other arcs to same nodes to curve..
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