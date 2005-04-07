package sonia;

import java.awt.*;


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

public class SoniaCanvas extends Canvas
{
  private SoniaLayoutEngine engine;
  private LayoutWindow display;
  private RenderSlice lastRender = null;
  private Image offScreen;
  private Image ghostImage = null;
  private int drawWidth;
  private int drawHeight;
  private int topInset;
  private int leftInset;
  private int pad;
  private boolean antiAlias = true;
  private boolean showStats = true;
  private boolean showLabels = true;
  private boolean showID = false;
  private boolean showArrows = true;
  private boolean arcTrans = true;
  private boolean showArcWeights = false;
  private boolean showArcLabels = false;
  private boolean ghostSlice = false;
  private boolean flashNewEvents = false;
  private float arcWidthFact = 1.0f;
  private float arcTransVal = 0.5f;
  private float nodeScaleFact = 1.0f;
  private boolean hideNodes = false;
  private boolean hideArcs = false;
  private double flashDuration = 1.0;

  /**
   * Does the actual job of rendering the network, and maintains the graphics
   * settings.  See LayoutWindow for an overview of the rendering process.
   */
  public SoniaCanvas(SoniaLayoutEngine eng, LayoutWindow window)
  {
    engine = eng;
    display = window;

    drawWidth = engine.getDisplayWidth();
    drawHeight = engine.getDisplayHeight();
    this.setBackground(Color.white);
    offScreen = createImage(drawWidth, drawHeight);
  }
  public void setRenderSlice(RenderSlice slice)
  {
    lastRender=slice;
  }

  /**
   * Redraws the previously drawn off-screen image (buffer) to the passed Graphics from the
   * window.  Creates a new offscreen buffer is one is missing.  Does NOT
   * actually redraw the network.  Hopefully this will save time on renders..
   * @param g  - Graphics object to draw the screen buffer on
   */
  public void paint(Graphics g)
  {

    if(offScreen == null)
    {
      drawWidth = engine.getDisplayWidth();
      drawHeight = engine.getDisplayHeight();
      offScreen = createImage(drawWidth, drawHeight);
    }
    g.drawImage(offScreen, 0, 0, null);
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
    leftInset = display.getInsets().left;
    topInset = display.getInsets().top;
    pad = engine.getPad();
    this.setBackground(Color.white);
    //check if the window has changed size
    if ((drawWidth != engine.getDisplayWidth()) |
    (drawHeight != engine.getDisplayHeight()))
    {
      drawWidth = engine.getDisplayWidth();
      drawHeight = engine.getDisplayHeight();
      offScreen = createImage(drawWidth, drawHeight);
    }
    //if image is missing or window has changed size,
    //make image for double buffering
    if(offScreen == null)
    {
         offScreen = createImage(drawWidth, drawHeight);
    }
    //get offscreen graphics andcast to Graphics2D
    Graphics2D graphics = (Graphics2D)offScreen.getGraphics();
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
    //draw a blank background
    graphics.clearRect(0,0,drawWidth,drawHeight);
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
    if (lastRender != null)
    {
      lastRender.paint(graphics,this);
    }
    g.drawImage(offScreen, 0, 0, null);

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
    return antiAlias;
  }
  public void setAntiAlias(boolean onOff)
  {
    antiAlias = onOff;
  }
  public boolean isShowArrows()
  {
    return showArrows;
  }
  public void setShowArrows(boolean onOff)
  {
    showArrows = onOff;
  }
  public boolean isShowArcWeights()
  {
    return showArcWeights;
  }
  public void setShowArcWeights(boolean onOff)
  {
    showArcWeights = onOff;
  }
  public boolean isShowArcLabels()
  {
    return showArcLabels;
  }
  public void setShowArcLabels(boolean onOff)
  {
    showArcLabels = onOff;
  }
  public boolean isGhostSlice()
  {
    return ghostSlice;
  }
  public void setGhostSlice(boolean onOff)
  {
    ghostSlice = onOff;
    if (!ghostSlice)
    {
      ghostImage = null;
    }
  }
  public void setFlashNew(boolean value) 
  {
          flashNewEvents = value;
  }
  /**
   * indicates if "new" events should be "flashed" (graphically hilited) in the 
   * display.  new events are defined by their start time and flash duration
   */
  public boolean isFlashNew()
  {
      return flashNewEvents;
  }
  /**
   * how long (in data time) flashed events should be drawn as flashed events
   */
  public void setFlashDuration(double duration)
  {
      flashDuration = duration;
  }
  public double getFlashDuration()
  {
      return flashDuration;
  }
  
  public boolean isShowLabels()
  {
    return showLabels;
  }
  public void setShowLabels(boolean onOff)
  {
    showLabels = onOff;
  }
  public boolean isShowId()
  {
    return showID;
  }
  public void setShowId(boolean onOff)
  {
    showID = onOff;
  }
  public boolean isShowStats()
  {
    return showStats;
  }
  public void setShowStats(boolean onOff)
  {
    showStats = onOff;
  }
  public boolean isArcTrans()
  {
    return arcTrans;
  }
  public void setArcTrans(boolean onOff)
  {
    arcTrans = onOff;
  }
  public float getArcTransVal()
  {
    return arcTransVal;
  }
  public void setArcTransVal(float trans)
  {
    arcTransVal = trans;
  }
  public float getArcWidthFact()
  {
    return arcWidthFact;
  }
  public void setArcWidthFact(float fact)
  {
    arcWidthFact = fact;
  }
  public void setNodeScaleFact(float fact)
  {
    nodeScaleFact = fact;
  }
  public float getNodeScaleFact()
  {
    return nodeScaleFact;
  }

  public boolean isHideNodes()
  {
    return hideNodes;
  }
  public void setHideNodes(boolean state)
  {
    hideNodes = state;
  }
  public boolean isHideArcs()
  {
    return hideArcs;
  }
  public void setHideArcs(boolean state)
  {
    hideArcs = state;
  }



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



}