package sonia;

import javax.swing.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.net.*;
import java.util.HashMap;
import java.util.Set;

import javax.imageio.*;

import sonia.mapper.Colormapper;


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
 * Stores all the data pertaining to a specific time or period of time, with
 * the ability to draw itself.   The NodeAttribute is instantiatedd with defualt
 * values for all the parameters not specified in the constructor, which may be
 * then be set by the parser.  The node contains its own RectangularShape for
 * drawing, which means at the momment it can only be a square or circle.
 */
public class NodeAttribute implements NetworkEvent{

 public static final String FLASH_EFFECT = "FLASH_EFFECT";
 public static final String NO_EFFECT = "NO_EFFECT";
 public static final Color DEFAULT_NODE_COLOR = Color.white;
 public static final Color DEFAULT_BORDER_COLOR = Color.black;
 public static final Color DEFULAT_LABEL_COLOR = Color.red;
 public static final double DEFAULT_SIZE = 10.0;

  // variables instatiated with defaults
  private int nodeId;   //should never change after instantiation, used for coords
                         //smallest ID should be 1.
  private double obsTime = 1;  //when this observation was made
  private double endTime = Double.POSITIVE_INFINITY;  //when node disapears
  private String nodeLabel = "Node "+nodeId;
  private Color labelColor = DEFULAT_LABEL_COLOR;
  private float labelSize = 10f;
  private Color nodeColor = DEFAULT_NODE_COLOR;
  private Color borderColor = DEFAULT_BORDER_COLOR;
  private float borderWidth = 1.0f;
  //private BasicStroke borderStroke = new BasicStroke(borderWidth);
  private double nodeSize = DEFAULT_SIZE;
  private RectangularShape nodeShape = new Ellipse2D.Double();  //not always a rect, but bounds are rect
  private double obsXCoord = 0.0;  //originaly observed cordinates
  private double obsYCoord = 0.0;
  private String origFileLoc = "";  //name and line in file that created it
 // private boolean flashNode = false;  //if it should be flashed when it is drawn
  private String effect = NO_EFFECT;
  private HashMap userData;
  private Colormapper colormap = null;

  //coords not stored here so that they can be accessed more quickly
  //transparency???

  // use image for node shape (can be in addition to square or circle shape defined by nodeShape)
  private URL iconURL;
  private ImageIcon icon = null; 

  
  public NodeAttribute(int id)
  {
    nodeId = id;
  }

  public NodeAttribute(int id, String label, double x, double y, double time,
                       double end, String fileLoc) {
    nodeId = id;
    nodeLabel = label;
    obsXCoord = x;
    obsYCoord = y;
    obsTime = time;
    endTime = end;
    origFileLoc = fileLoc;
  }

  //self drawing methods ------------
  /**
   * Paints the node onto the passed graphics at the passed coords, using the
   * graphics settings of the canvas.  (MORE DETAILS)
   */
//  public void paint(Graphics2D graphics, SoniaCanvas canvas, double xCoord,
//                    double yCoord)
//  {
//	  Color startColor = graphics.getColor();
//	  Font startFont = graphics.getFont();
//    double nodeDrawSize = 0.0;
//    
//    //check if drawing node
//    if (!canvas.isHideNodes())
//    {
//      nodeDrawSize = nodeSize * canvas.getNodeScaleFact();
//
//      //changes size and shape of node by repositioning and scaling the rectacular
//      //frame enclosing it
//      nodeShape.setFrame((xCoord - nodeDrawSize/2.0),
//                         (yCoord - nodeDrawSize/2.0),nodeDrawSize,nodeDrawSize);
//     
//      if (nodeColor != null){
//	      graphics.setColor(nodeColor);
//	      graphics.fill(nodeShape);
//      }
//      //set border color/width and draw it
//      graphics.setColor(borderColor);
//      graphics.setStroke(borderStroke);
//      graphics.draw(nodeShape);
//      
//      //if it has never been drawn, than draww it very large so it will show
//      if (flashNode)
//      {
//         nodeShape.setFrame((xCoord - (nodeDrawSize+flashFactor)/2.0),
//                         (yCoord - (nodeDrawSize+flashFactor)/2.0),(nodeDrawSize+flashFactor),(nodeDrawSize+flashFactor));;
//         graphics.setColor(flashColor);
//         graphics.draw(nodeShape);
//         flashNode = false; //so we only draw once, even if stay on same slice..?
//      }
//      
//      // drawing image for Node
//      if (icon != null)
//      {
//         int xc = (int)xCoord - (int)nodeDrawSize/2;
//         int yc = (int)yCoord - (int)nodeDrawSize/2;
//         graphics.drawImage(icon.getImage(), xc, yc, (int)nodeDrawSize, (int)nodeDrawSize, null);
//      }
//      // end draw image
//            
//    }
//    
//    //rough label
//    if (canvas.isShowLabels() | canvas.isShowId())
//    {
//      String printLabel ="";
//      graphics.setColor(labelColor);
//      Font originalFont = graphics.getFont();
//      graphics.setFont(originalFont.deriveFont(labelSize));
//      if (canvas.isShowId() && (nodeSize >= canvas.getShowLabelsVal()))
//      {
//        printLabel = printLabel+nodeId;
//        //if both are on, show with a ":" seperator
//        if (canvas.isShowLabels() && (nodeSize >= canvas.getShowIdsVal()))
//        {
//          printLabel = printLabel+":";
//        }
//      }
//      if (canvas.isShowLabels() && (nodeSize >= canvas.getShowLabelsVal()))
//      {
//          printLabel = printLabel+nodeLabel;
//      }
//      graphics.drawString(printLabel, (float)(xCoord+(nodeDrawSize/2.0)+2.0),
//                          (float)(yCoord+labelSize/2.0));
//    }
//    graphics.setColor(startColor);
//    graphics.setFont(startFont);
//
//    //how can I do this to just change the attributes of the same shape?
//    //need to create my own nodeShape classes
//
//  }

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

  //accessors------------------------------------------

  public int getNodeId()
  {
    return nodeId;
  }

  public String getNodeLabel()
  {
    return nodeLabel;
  }
  public void setNodeLabel(String label)
  {
   nodeLabel = label;
  }
  /**
   * Returns the nodes color, or, if a colormapper has been set, returns the color assigned
   * to the node by the mapper according to the attribute.
   * TODO: this should be done at a higher level
   * @return
   */
  public Color getNodeColor()
  {
	  if (colormap == null){
		  return nodeColor;
	  } else {
		  //get key we are using from the color map
		  Object key = colormap.getKey();
		  //get the value of the data from the node
		  //get the color it is mapped to
		  return colormap.getColorFor(userData.get(key));
	  }
  }
  public void setNodeColor(Color c)
  {
    nodeColor = c;
  }

  public Color getBorderColor()
  {
   return borderColor;
  }
  public float getBorderWidth()
  {
    return borderWidth;
  }
  public void setBorderColor(Color c)
  {
    borderColor = c;
  }
  public void setBorderWidth(double width)
  {
    borderWidth = (float)width;
    //also make a borderStroke shape with that width
    //borderStroke = new BasicStroke(borderWidth);
  }
  public float getLabelSize()
  {
    return labelSize;
  }
  public double getNodeSize()
  {
    return nodeSize;
  }
  public void setLabelSize(float size)
  {
    labelSize = size;
  }
  public void setLabelColor(Color c)
  {
    labelColor= c;
  }
  public Shape getNodeShape()
  {
    return nodeShape;
  }
  public URL getIconURL()
  {
    return iconURL;
  }
  
  public ImageIcon getIcon(){
	  return icon;
  }
//  public void flash()
//  {
//     // flashNode = true;
//	  effect = FLASH_EFFECT;
//  }
  public void setNodeShape(RectangularShape s)
  {
    nodeShape = s;
  }
  /**
   * thows exception if unable to find or create the icon specified by the url
   * @param url
   * @throws Exception
   */
  public void setIconURL(URL url) throws Exception
  {
    iconURL = url;
    if (iconURL != null)
    {
        icon = new ImageIcon(iconURL);

        //System.out.println("iconURL:"+iconURL.toString()+"\n");
    }
  }
  public void setNodeSize(double size)
  {
    //make sure it is positive
    nodeSize = Math.abs(size);
  }
  public double getObsTime()
  {
    return obsTime;
  }
  public void setObsTime(double time)
  {
    obsTime = time;
  }
  public double getEndTime()
  {
    return endTime;
  }
  public void setEndTime(double time)
  {
    endTime = time;
  }

  /**
   * Returns the x coordinate originaly specified by the parser (or set later),
   * this is NOT the node's layout coordinate which is stored in the slice.
   */
  public double getObsXCoord()
  {
    return obsXCoord;
  }

  /**
   * Sets the stord x coordinate,  this is NOT the node's layout coordinate
   * which is stored in the slice, but is a default coordinate, generally set
   * by the parser to match the orginal observation in the file.
   */
  public void setObsXCoord(double x)
  {
    obsXCoord = x;
  }

  /**
     * Returns the y coordinate originaly specified by the parser (or set later),
     * this is NOT the node's layout coordinate which is stored in the slice.
   */
  public double getObsYCoord()
  {
    return obsYCoord;
  }

  /**
   * Sets the stord y coordinate,  this is NOT the node's layout coordinate
   * which is stored in the slice, but is a default coordinate, generally set
   * by the parser to match the orginal observation in the file.
   */
  public void setObsYCoord(double y)
  {
    obsXCoord = y;
  }
  
  /**
   * Returns keys corresponding to various visual effects
   */
  public String getEffect(){
	  return effect;
  }
  
  /**
   * Returns keys corresponding to various visual effects
   */
  public void SetEffect(String effectKey){
	   effect = effectKey;
  }
  
  /**
   * test method for adding an aribitrary key -> data association for this node
   * @param key
   * @param datum
   */
  public void setData(String key, Object datum){
	  if (userData == null){
		  userData = new HashMap<String, Object>();
	  }
	  userData.put(key, datum);
  }
  
  /**
   * test method for getting the value of a data association for this node attribute
   * @param key
   * @return the object the key is associated with, otherwise null 
   */
  public Object getData(String key){
	  if (userData != null){
		 return userData.get(key);
	  } else {
		  return null;
	  }
  }
  
  /**
   * returns the set of key objects that have data mapped to them for this node,
   * or null if no data has been attached
   * @author skyebend
   * @return
   */
  public Set getDataKeys(){
	  if (userData != null){
		  return userData.keySet();  
	  }
	  return null;
  }
  
  /**
   * if the color map is not null, getNodeColor will return the value from the color map, rather than
   * the nodes color.  ALL NODES SHOULD HAVE THE SAME COLOR MAP, BUT THIS IS NOT ENFORCED
   * @param map
   */
  public void setColormap(Colormapper map){
	  colormap = map;
  }

public String getOrigFileLoc() {
	return origFileLoc;
}

/**
 * returns the color that should be used to draw the node's label
 * @author skyebend
 * @return
 */
public Color getLabelColor() {
	return labelColor;
}



}