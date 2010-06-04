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

//import java.awt.Graphics2D;
//import java.awt.geom.*;
import java.awt.*;

/**
 * Holds the attributes which describe an arc event, starting, ending, colors,
 * original coords, labels, etc.  Contains paint methods for drawing itself to
 * the graphics of a renderslice, when givin the proper node coordinates.  It
 * has the ids of the starting and ending nodes, but no references to the objects.
 * If an arc is set to a negitive value, it will be set to positive, but will be
 * drawn as a dashed line.
 */
public class ArcAttribute implements NetworkEvent
{
	
  // variables instatiated with defaults
 private double obsTime = 3.2;  //when this observation was made
 private double endTime = Double.POSITIVE_INFINITY;  //when arc disapears
 private String arcLabel = "";
 private Color labelColor = Color.darkGray;
 private float labelSize = 10f;
 private Color arcColor = Color.darkGray;
 //private Line2D arc = new Line2D.Double();   //the path to draw
// private GeneralPath headPath = new GeneralPath();
 //private Color arrowColor = Color.lightGray;
 private double arcWidth = 1.0;
 private double arcWeight = 1.0;
 private boolean isNegitive = false;
 private int fromNodeId;
 private int toNodeId;
 private boolean flashArc = false;  //if it should be flashed when it is drawn

 //coords not stored here so that they can be accessed more quickly
  //transparency???
// private float transparencey = 0.7f;


 /**
 * instantiates an arc attribute with defualt values and no from or to
 */
 public ArcAttribute()
 {
 }

 /**
  * instantiate an arc attribute with some of the basic parameters
  * @param time the starting time for the arc
  * @param end the ending time for the arc
  * @param from  the int index for the starting node
  * @param to the int index for the ending node
  * @param weight the weighting of the arc in the network
  * @param width how wide to draw the arc on screen
  */
 public ArcAttribute(double time, double end, int from, int to, double weight,
                     double width)
 {
   setObsTime(time);
   setEndTime(end);
   setFromNodeId(from);
   setToNodeId(to);
   setArcWeight(weight);
   arcWidth = width;
   //setArcWidth(arcWeight);  // temporary kludge
 }

  //NEED TO PROVIDE METHODS FOR ASKING NODES FOR COORDS FOR DRAWING
  /**
   * draws the arc to the passed graphics, using the passed coordinates (from
   * the current coords in the layout engine)  the arcs internal values can be
   * modified by settings at the canvas level.   Arc width, for example, can
   * be scaled up by setting the ArcWidthFactor in the graphic settings dialog.
   * other settings include hiding the arrows, labels, or not drawing arcs at all.
   * If the arc's isNegitive is set true, it will be drawn dashed.
   * @param graphics the 2D graphics to draw on
   * @param canvas the canvas where drawing occurs (to ask settings)
   * @param fromX the X of the start node
   * @param fromY the Y of the start node
   * @param toX the X of the end node
   * @param toY the Y of the end node
   */
 //public void paint(Graphics2D graphics, SoniaCanvas canvas, double fromX, double fromY,
//                   double toX, double toY)
// {
    
//   //check if drawing arc
//   if (!canvas.isHideArcs())
//   {
//     //do the dashing
//     float dashSkip = 0.0f;
//     float dashLength = 2.0f;
//     float drawWidth = (float)arcWidth*canvas.getArcWidthFact();
//  
//     
//     if (isNegitive)
//     {
//       dashSkip = drawWidth;
//       dashLength = 2.0f * drawWidth;
//     }
//     float[] dash = {dashLength,dashSkip};
//     BasicStroke linewidth = new BasicStroke(drawWidth,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,
//                                        1.0f,dash,0.0f);
//     graphics.setStroke(linewidth);
//     graphics.setColor(arcColor);
//     //should correct for width of node (and length of arrow?)
//     arc.setLine(fromX,fromY,toX,toY);
//     graphics.draw(arc);
//     
//       //if it has never been drawn, than draww it very large so it will show
//     if (flashArc)
//     {
//         graphics.setStroke(new BasicStroke(drawWidth+flashFactor,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,
//                                        1.0f,dash,0.0f));
//         graphics.setColor(flashColor);
//         graphics.draw(arc);
//         flashArc = false; //so we only draw once, even if stay on same slice..?
//         graphics.setStroke(linewidth);
//     }
//     
//     //should turn off dashing
//     dashSkip = 0.0f;
//
//     // CHECK IF ARROWS ARE TO BE DRAWN
//     if (canvas.isShowArrows())
//     {
//
//       //reset the arrowhead path and make arrowhead
//       headPath.reset();
//       double arrowSize = arrowLength+drawWidth;
//       double xDiff = (fromX - toX);
//       double yDiff = (fromY - toY);
//       double lineAngle = Math.atan((xDiff) / (yDiff));
//       //trap cases where xDiff and yDiff are zero to stop strange PRException onPC
//       if (Double.isNaN(lineAngle))
//       {
//         lineAngle = 0.0;
//       }
//       if (yDiff < 0)  //rotate by 180
//       {
//         lineAngle += Math.PI;
//       }
//       try  //for concurrency problems on dual processor machines...
//       {
//       //tip of arrow
//       headPath.moveTo((float)toX, (float)toY);
//       //one wedge
//       headPath.lineTo((float)(toX + (arrowSize * Math.sin(lineAngle-0.3))),
//                       (float)(toY + (arrowSize * Math.cos(lineAngle-0.3))));
//       //other wedge
//       headPath.lineTo((float)(toX + (arrowSize * Math.sin(lineAngle+0.3))),
//                       (float)(toY + (arrowSize * Math.cos(lineAngle+0.3))));
//       //back to top
//       headPath.closePath();
//       graphics.fill(headPath);
//       }
//       catch (IllegalPathStateException e)
//       {
//           System.out.println("Arrow Drawing error: x:"+toX+" y:"+toY);
//           e.printStackTrace();
//       }
//
//     }//end draw arcs
//   }
//   //CHECK IF LABELS ARE TO BE DRAWN
//   if (canvas.isShowArcLabels())
//   {
//     graphics.setColor(labelColor);
//     Font originalFont = graphics.getFont();
//     graphics.setFont(originalFont.deriveFont(labelSize));
//     float labelX = (float)(fromX+ (toX-fromX)/2);
//     float labelY = (float)(fromY + (toY-fromY)/2);
//     graphics.drawString(arcLabel,labelX,labelY);
//   }
//
//   //detecting other arcs to same nodes to curve..
   
 //}

 /**
  * compares start times for list sorting. returns -1 if start timve of evt
  * is less than start time of self, 0 if equal, 1 if greater.
  * See java.lang.Compareable
  * @param evt NetworkEvent to be compared
  * @throws ClassCastException if unable to cast object to NetworkEvent
   */
  public int compareTo(Object evt)
  {
    int returnVal = 0;
    //check if can cast to NetworkEvent
    NetworkEvent event = (NetworkEvent)evt;
    double eventStart = event.getObsTime();
    if (eventStart < obsTime)
    {
      returnVal = -1;
    }
    else if (eventStart > obsTime)
    {
      returnVal = 1;
    }

    return returnVal;
  }

  //accessors-------------------------------
  /**
   * returns the color of the arc
   */
  public Color getArcColor()
  {
    return arcColor;
  }

  /**
   * sets the color of the arc to the passed Color
   * @param c the color to draw the arc
   */
  public void setArcColor(Color c)
  {
	  //this to stop onself being shot in the foot with invisible edges
	  if (c != null){
		  arcColor = c;
	  }

  }

  /**
   * returns the string of the arc label
   */
  public String getArcLabel()
  {
    return arcLabel;
  }

  /**
   * sets the label of the arc to the string
   * @param the new label for the arc
   */
  public void setArcLabel(String label)
  {
    arcLabel = label;
  }

  /**
   * returns the double weight of the arc
   */
  public double getArcWeight()
  {
    return arcWeight;
  }

  /**
   * sets weight of the arc to the passed double.  If weight is < 0,
   *  sets isNegitive to true (so it will be drawn dashed) and multiplies by -1
   * NOT A GOOD WAY TO DEAL WITH NEGITIVE ARCS
   * @param weight the weight of the arc
   */
  public void setArcWeight(double weight)
  {
    if (weight < 0)
    {
      isNegitive = true;
      weight= weight*-1;
    }
      arcWeight = weight;
  }

  /**
   * the double value giving the number of pixels wide to draw the arc
   */
  public double getArcWidth()
  {
    return arcWidth;
  }

  /**
   * sets the width of the arc to the absolute value of the passed double
   * (prevents negitive widths)
   * @param width the number of pixels wide to draw the arc
   */
  public void setArcWidth(double width)
  {
    arcWidth = Math.abs(width);
  }

  /**
   * returns the int corresponding to the starting node
   */
  public int getFromNodeId()
  {
    return fromNodeId;
  }

  /**
   * sets the int corresponding to the starting node
   * @param id the int id of the from node
   */
  public void setFromNodeId(int id)
  {
    fromNodeId = id;
  }

  /**
   * returns the int corresponding to the ending node
   */
  public int getToNodeId()
  {
    return toNodeId;
  }

  /**
   * sets the int corresponding to the ending node
   * @param id the ide of the to node
   */
  public void setToNodeId(int id)
  {
    toNodeId = id;
  }

  /**
   * returns the color of the arc's label
   */
  public Color getLabelColor()
  {
    return labelColor;
  }

  /**
   * sets the color of the arc's label
   * @param c the Color to draw the label
   */
  public void setLabelColor(Color c)
  {
    labelColor = c;
  }

  /**
   * returns the size (font size?) to draw the label
   */
  public float getLabelSize()
  {
    return labelSize;
  }

  /**
   * sets the size (font size?) to draw the arc label
   * @param size  the font size for the label
   */
  public void setLabelSize(float size)
  {
    labelSize = size;
  }

  /**
   * returns the double starting time for the arc, controls which slice it will
   * appear in
   */
  public double getObsTime()
  {
    return obsTime;
  }

  /**
   * sets the starting time for the arc to the passed double
   * @param time start time for the arc
   */
  public void setObsTime(double time)
  {
    obsTime = time;
  }

  /**
   * returns the double end time for the arc.  controls when the arc will disappear
   */
  public double getEndTime()
  {
    return endTime;
  }

  /**
   * sets the ending time for the arc to the passed double
   * @param time the end time for the arc
   */
  public void setEndTime(double time)
  {
    endTime = time;
  }
  
  /**
   * sets flashArc to true, so the next time the arc is drawn it will be flashed.
   * after drawning, sets back to false so it only flashes once
   */
  public void setFlash(boolean flash)
  {
      flashArc = flash;
  }
  
  /**
   * indicates if this arc should be hilited/flashed in the display when it is drawn.  
   * @return
   */
  public boolean shouldFlash()
  {
	  return flashArc;
  }

public boolean isNegitive() {
	return isNegitive;
}
}