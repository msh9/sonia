package sonia;

import java.awt.*;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;

import sonia.mapper.Colormapper;
import sonia.mapper.DefaultColors;
import sonia.render.Graphics2DRender;
import sonia.settings.GraphicsSettings;


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

public class SoniaCanvas extends JPanel
{
  private SoniaLayoutEngine engine;
 // private LayoutWindow display;
  private RenderSlice lastRender = null;
  private Graphics2DRender g2dRender;
  private GraphicsSettings settings = null;
private Colormapper colormapper = null;
  //private Image offScreen;
  private Image ghostImage = null;
  private int drawWidth;
  private int drawHeight;

  private boolean antiAlias = true;
//  private boolean showStats = true;
//  private boolean showLabels = true;
//  private float showLabelsVal = 10.0f;
//  private boolean showID = false;
//  private float showIdsVal = 10.0f;
//  private boolean showArrows = true;
//  private boolean arcTrans = true;
//  private boolean nodeTrans = true;
//  private boolean showArcWeights = false;
//  private boolean showArcLabels = false;
  private boolean ghostSlice = false;
 // private boolean flashNewEvents = false;
  private float arcWidthFact = 1.0f;
  private float arcTransVal = 0.5f;
  private float nodeTransVal = 0.5f;
  private float nodeLabelBgTransVal = 0.5f;
  private float clusterTransVal = 0.5f;
  private float nodeScaleFact = 1.0f;
 // private boolean hideNodes = false;
 // private boolean hideArcs = false;
  //private double flashDuration = 1.0;

  /**
   * Does the actual job of rendering the network, and maintains the graphics
   * settings.  See LayoutWindow for an overview of the rendering process.
   */
  public SoniaCanvas(GraphicsSettings settings, SoniaLayoutEngine eng)
  {
    engine = eng;
    this.settings = settings;
    //display = window;
    //this.setBorder(new EtchedBorder());
    drawWidth = engine.getDisplayWidth();
    drawHeight = engine.getDisplayHeight();
  //  this.setBackground(Color.white);
  //  offScreen = createImage(drawWidth, drawHeight);
    g2dRender = new Graphics2DRender();
  }
  
  


public  void setRenderSlice(RenderSlice slice)
  {
    lastRender=slice;
  }
  
  public RenderSlice getRenderSlice(){
	  return lastRender;
  }

  /**
   * Redraws the previously drawn off-screen image (buffer) to the passed Graphics from the
   * window.  Creates a new offscreen buffer is one is missing.  Does NOT
   * actually redraw the network.  Hopefully this will save time on renders..
   * @param g  - Graphics object to draw the screen buffer on
   */
  public void paintComponent(Graphics g)
  {
	  super.paintComponent(g);
//    if(offScreen == null)
//    {
//      drawWidth = engine.getDisplayWidth();
//      drawHeight = engine.getDisplayHeight();
//      offScreen = createImage(drawWidth, drawHeight);
//    }
//    g.drawImage(offScreen, 0, 0, null);

	  updateDisplay(g,ghostSlice);
  }

//need a much faster way of doing this, using the BufferdImage class,
  //check to see if any thing has changed state, and if not just resuing the old image?

  /**
   * Redraws the entire network on the buffer image, and then draws this image
   * to the passed graphics object.  Note that this actually redraw the network,
   * instead of just passing up the last image like as in paint(). Internally,
   * it casts the passed Graphics to Graphics2D, turns anti-alias on or off, etc.
   * @param g Graphics object from the SoniaCanvas which the network will be
   * drawn on.
   * @param includeGhost if true, includes a ghosted (50% transparent) image,
   * usually of a previous slice, under the network as it is drawn. makes things slow
   */
  public void updateDisplay(Graphics g,boolean includeGhost)
  {
	 
   // leftInset = getInsets();
   // topInset = display.getInsets().top;
  //  pad = engine.getPad();
	 // super.paintComponent(g);
    //check if the window has changed size
    if ((drawWidth != engine.getDisplayWidth()) |
    (drawHeight != engine.getDisplayHeight()))
    {
      drawWidth = engine.getDisplayWidth();
      drawHeight = engine.getDisplayHeight();
   //   offScreen = createImage(drawWidth, drawHeight);
    }
    //if image is missing or window has changed size,
    //make image for double buffering
//    if(offScreen == null)
//    {
//         offScreen = createImage(drawWidth, drawHeight);
//    }
    //get offscreen graphics andcast to Graphics2D
  //  Graphics2D graphics = (Graphics2D)offScreen.getGraphics();
    Graphics2D graphics = (Graphics2D)g;
    // check settings, and then set aliasing
    if (antiAlias)
    {
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                              RenderingHints.VALUE_ANTIALIAS_ON);
    }
    else
    {
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                              RenderingHints.VALUE_ANTIALIAS_OFF);
    }
    //take care of menubar or other items which might cover layout
   //graphics.translate(leftInset+pad,topInset+pad);
    //draw the network to the offscreen image
    //draw a blank background with the specified background color
    graphics.setColor(getBackground());
    graphics.fillRect(0,0,drawWidth,drawHeight);
    //if ghosting is on, draw the ghost slice
    if (ghostSlice & includeGhost)
    {
      if(ghostImage != null)
      {
          graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
          0.5f));
        graphics.drawImage(ghostImage, 0, 0, null);
        //set transparency back to solid
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
      }
    }
    //now draw the currently selected render slice
    //TODO:  should render something else to the UI when the render is being diverted?
    if (lastRender != null)
    {
      lastRender.render(graphics,this,g2dRender);
    }
   // g.drawImage(offScreen, 0, 0, null);

  }

  /**
   * Takes the current off-screen image (of the soon-to-be-previous frame)
   * and stores it to be drawn underneath the picture of the curret network.
   * If ghosting is off, does nothing.
   *
   */
  public void saveImageForGhost()
  {
    if (ghostSlice)
    {
      if (ghostImage == null)
      {
        ghostImage = createImage(drawWidth,drawHeight);
      }
      ghostImage.flush();
      updateDisplay(ghostImage.getGraphics(),false);
    }
  }

/*
  public boolean isDoubleBufferd()
  {
    System.out.println("double buffer quesntion");
    return true;
  }
*/
  public boolean isAntiAlias()
  {
    //return antiAlias;
	  return Boolean.parseBoolean(settings.getProperty(GraphicsSettings.ANTI_ALIAS));
  }
//  public void setAntiAlias(boolean onOff)
//  {
//    antiAlias = onOff;
//  }
  public boolean isShowArrows()
  {
    //return showArrows;
	  return settings.getProperty(GraphicsSettings.ARROW_STYLE).equals(GraphicsSettings.ARROW_END);
  }
//  public void setShowArrows(boolean onOff)
//  {
//    showArrows = onOff;
//  }
  public boolean isShowArcWeights()
  {
    //return showArcWeights;
    return settings.getProperty(GraphicsSettings.ARC_LABELS).equals(GraphicsSettings.LAYOUT_WEIGHTS);
  }
//  public void setShowArcWeights(boolean onOff)
//  {
//    showArcWeights = onOff;
//  }
  public boolean isShowArcLabels()
  {
    //return showArcLabels;
	  return settings.getProperty(GraphicsSettings.ARC_LABELS).equals(GraphicsSettings.LABELS);
  }
//  public void setShowArcLabels(boolean onOff)
//  {
//    showArcLabels = onOff;
//  }
  
  public boolean drawCurvyArcs(){
	  return Boolean.parseBoolean(settings.getProperty(GraphicsSettings.ARCS_CURVY,"false"));
  }
  public boolean isGhostSlice()
  {
     ghostSlice = Boolean.parseBoolean(settings.getProperty(GraphicsSettings.GHOST_SLICE));
     if (!ghostSlice)
     {
       ghostImage = null;
     }
	  return ghostSlice;
  }
//  public void setGhostSlice(boolean onOff)
//  {
//    ghostSlice = onOff;
//    if (!ghostSlice)
//    {
//      ghostImage = null;
//    }
//  }
//  public void setFlashNew(boolean value) 
//  {
//	 
//	  
//          flashNewEvents = value;
//  }
  /**
   * indicates if "new" events should be "flashed" (graphically hilited) in the 
   * display.  new events are defined by their start time and flash duration
   */
  public boolean isFlashNew()
  {
      return getFlashDuration() != 0.0;
  }
  /**
   * how long (in data time) flashed events should be drawn as flashed events
   */
//  public void setFlashDuration(double duration)
//  {
//      flashDuration = duration;
//  }
  public double getFlashDuration()
  {
      //return flashDuration;
	  return Double.parseDouble(settings.getProperty(GraphicsSettings.FLASH_EVENTS));
  }
  
  public boolean isShowLabels()
  {
    //return showLabels;
	  return settings.getProperty(GraphicsSettings.NODE_LABELS).equals(GraphicsSettings.LABELS);
  }
//  public void setShowLabels(boolean onOff)
//  {
//    showLabels = onOff;
//  }
  public float getShowLabelsVal()
  {
    //return showLabelsVal;
    return Float.parseFloat(settings.getProperty(GraphicsSettings.NODE_LABEL_CUTOFF));
  }
//  public void setShowLabelsVal(float thresh)
//  {
//    showLabelsVal = thresh;
//  }
  public boolean isShowId()
  {
    //return showID;
	  return settings.getProperty(GraphicsSettings.NODE_LABELS).equals(GraphicsSettings.IDS);
  }
//  public void setShowId(boolean onOff)
//  {
//    showID = onOff;
//  }
  public float getShowIdsVal()
  {
    return getShowLabelsVal();
  }
//  public void setShowIdsVal(float thresh)
//  {
//    showIdsVal = thresh;
//  }
  public boolean isShowStats()
  {
    //return showStats;
	 return Boolean.parseBoolean(settings.getProperty(GraphicsSettings.SHOW_STATS));
  }
//  public void setShowStats(boolean onOff)
//  {
//    showStats = onOff;
//  }
  public boolean isArcTrans()
  {
	 
    return getArcTransVal() != 1.0;
  }
  public boolean isNodeTrans()
  {
    return getNodeTransVal() != 1.0;
  }
  public boolean isClusterTrans(){
	  return getClusterTransVal() != 1.0;
  }
  
//  public void setArcTrans(boolean onOff)
//  {
//    arcTrans = onOff;
//  }
//  public void setNodeTrans(boolean onOff)
//  {
//    nodeTrans = onOff;
//  }
  public float getArcTransVal()
  {
    return Float.parseFloat(settings.getProperty(GraphicsSettings.ARC_TRANSPARENCY,arcTransVal+""));
  }
  public float getNodeTransVal()
  {
    return Float.parseFloat(settings.getProperty(GraphicsSettings.NODE_TRANSPARENCY,nodeTransVal+""));
  }
  public float getLabelBgTransVal()
  {
    return Float.parseFloat(settings.getProperty(GraphicsSettings.NODE_LABEL_BG_TRANS,nodeLabelBgTransVal+""));
  }
  public float getClusterTransVal()
  {
    return Float.parseFloat(settings.getProperty(GraphicsSettings.CLUSTER_TRANSPARENCY,clusterTransVal+""));
  }
//  public void setArcTransVal(float trans)
//  {
//    arcTransVal = trans;
//  }
//  public void setNodeTransVal(float trans)
//  {
//    nodeTransVal = trans;
//  }
  public float getArcWidthFact()
  {
    return Float.parseFloat(settings.getProperty(GraphicsSettings.ARCS_WIDTH_FACTOR,arcWidthFact+""));
  }
//  public void setArcWidthFact(float fact)
//  {
//    arcWidthFact = fact;
//  }
//  public void setNodeScaleFact(float fact)
//  {
//    nodeScaleFact = fact;
//  }
  public float getNodeScaleFact()
  {
    return Float.parseFloat(settings.getProperty(GraphicsSettings.NODE_SCALE_FACTOR,nodeScaleFact+""));
  }

  public boolean isHideNodes()
  {
    return settings.getProperty(GraphicsSettings.HIDE_NODES).equals(GraphicsSettings.ALL);
  }
//  public void setHideNodes(boolean state)
//  {
//    hideNodes = state;
//  }
  public boolean isHideArcs()
  {
     return settings.getProperty(GraphicsSettings.HIDE_ARCS).equals(GraphicsSettings.ALL);
  }
//  public void setHideArcs(boolean state)
//  {
//    hideArcs = state;
//  }



  /**
   * null out the offscreen buffer as part of invalidation
   */
  /*
  public void invalidate() {
    super.invalidate();
   // offScreen = null;
    //debug
    System.out.println("invalidate called on canvas");
  }
*/

  /**
   * override update to *not* erase the background before painting
   */
  public void update(Graphics g) {

  }
public GraphicsSettings getSettings() {
	return settings;
}
public void setSettings(GraphicsSettings settings) {
	this.settings = settings;
	repaint();
}
public Colormapper getColormapper() {
	if (this.colormapper == null){
		this.colormapper = new DefaultColors();
	}
	return colormapper;
}
public void setColormapper(Colormapper colormapper) {
	this.colormapper = colormapper;
}



}