package sonia;
import java.awt.*;
import java.awt.event.*;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import java.awt.Graphics;
import java.awt.geom.*;
import cern.colt.list.IntArrayList;
import java.util.ArrayList;

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
 * This class is not yet functioning.  The idea is to have it re-run the layout
 * multiple times from random starting coords, recording the results, and testing
 * to see how often it comes up with the same layouts.
 */
public class StabilityEstimate extends Frame implements WindowListener
{
  private SoniaController control;
  private SoniaLayoutEngine engine;
  private ArrayList xCoords;
  private ArrayList yCoords;
  private LayoutSlice currentSlice;
  private  int numObs;
  private int nNodes;
  private boolean[] useNodes;
  //TEMPORARY
  private Point2D.Double[] firstLayout;
  private Point2D.Double[] secondLayout;


  private Canvas plotArea;

  public StabilityEstimate(SoniaController cont, SoniaLayoutEngine eng)
  {
    control = cont;
    engine = eng;
    currentSlice = eng.getCurrentSlice();
    nNodes = currentSlice.getMaxNumNodes();
    xCoords = new ArrayList();
    yCoords = new ArrayList();

    //make list of which nodes to include/exclue
    useNodes = new boolean[nNodes];
    //set all elemnts to false to start
    for (int i = 0; i < useNodes.length; i++)
    {
      useNodes[i] = false;
    }
    //set indecies to true for nodes  present in this slice
    IntArrayList presentIndex = currentSlice.getPresentNodes();
    for (int i = 0; i < presentIndex.size(); i++)
    {
      useNodes[presentIndex.getQuick(i)] = true;
      //debug
      System.out.println("present:"+presentIndex.getQuick(i));
    }

    //now set elemnts to false on indecies corresponding to isolates
    IntArrayList isolateIndex = currentSlice.getIsolates();
    for (int i = 0; i < isolateIndex.size(); i++)
    {
      useNodes[isolateIndex.getQuick(i)] = false;
      //debug
      System.out.println("isolate:"+isolateIndex.getQuick(i));
    }


//do ui
    this.setFont(control.getFont());
    plotArea = new Canvas();
    GridBagLayout layout = new GridBagLayout();
    setLayout(layout);
    GridBagConstraints c = new GridBagConstraints();
    c.gridx=0;c.gridy=0;c.gridwidth=5;c.gridheight=5;c.weightx=1;c.weighty=1;
    add(plotArea,c);

    addWindowListener(this);
    setTitle("Stability Estimate");
    setBackground(Color.white);
    setSize(400,400);
    setVisible(true);
  }

  public void calcStability()
  {
    //repeat the layout numObs times, from random initial conditions?
    //store all the resulting coords
    //calc confindence intervals for each point
    //or overall correlation?
    //aggregate stat of all confidance intervals?



    //CHECK THAT LAYOUT FINISHED
    if (currentSlice.isLayoutFinished())
    {
      //get the current coords (assumes a layout has been done)
      double[] x1 = currentSlice.getXCoords();
      double[] y1 = currentSlice.getYCoords();

      //store them
      xCoords.add(x1);
      yCoords.add(y1);

      //add this to the slice so it can tell when it is finished
      currentSlice.addStabilityEstimate(this);

      //now redo the layout (assumes allready set to random, need to change this
      //would be nice too surpress logging
      engine.applyLayoutToCurrent();

    }
  }

  //tell stability that the current layout has finished
  public void layoutComplete(double[] newX, double[] newY)
  {
    double[] x1 = (double[])xCoords.get(0);
    double[] y1 = (double[])yCoords.get(0);

    xCoords.add(newX);
    yCoords.add(newY);

     //for a test try matching
      matchLayouts( x1,y1,newX,newY);
  }

  /**
   *
   */
  public void matchLayouts( double[] x1, double[] y1,
                            double[] x2, double[] y2)
  {
    firstLayout = new Point2D.Double[useNodes.length];
    secondLayout = new Point2D.Double[useNodes.length];

    //debug
    System.out.println("UseNodes:");
    System.out.print("useNodes: ");
   for (int i = 0; i < useNodes.length; i++)
   {
     System.out.print(useNodes[i]+" ");
    }
    System.out.println("");
    System.out.println("Coordinates:");
    System.out.print("x1 ");
    for (int i = 0; i < x1.length; i++)
    {
      System.out.print(x1[i]+" ");
    }
    System.out.println("");
    System.out.print("x2 ");
    for (int i = 0; i < x2.length; i++)
    {
      System.out.print(x2[i]+" ");
    }
    System.out.println("");
    System.out.print("y1 ");
    for (int i = 0; i < y1.length; i++)
    {
      System.out.print(y1[i]+" ");
    }
    System.out.println("");
    System.out.print("y2 ");
    for (int i = 0; i < y2.length; i++)
    {
      System.out.print(y2[i]+" ");
    }
    System.out.println("");
    /*

    exclude isolates?

    finds the best candidate matching for the two alternate layouts for the
    network.  checks only "ridgid" affine transforms, translation, rotation,
    reflection.
    */
   // recenter both layouts to avg of coords
   // or
   // pick a node at random, and center both layouts on that node
    int pickIndex = control.getUniformRand(0,useNodes.length-1);
    double pick1x = x1[pickIndex];
    double pick1y = y1[pickIndex];
    double pick2x = x2[pickIndex];
    double pick2y = y2[pickIndex];
    //calc the offset
    double diffX = pick1x - pick2x;
    double diffY = pick1y - pick2y;
    //adjust the 2nd layout
    for (int i = 0; i < useNodes.length; i++)
    {
      x2[i] = x2[i] - diffX;
      y2[i] = y2[i] - diffY;
    }

    //make arrays into points
    for (int i = 0; i < useNodes.length; i++)
    {
      firstLayout[i] = new Point2D.Double(x1[i],y1[i]);
      secondLayout[i] = new Point2D.Double(x2[i],y2[i]);
    }

    //draw to screen
    plotTwoLayouts(plotArea.getGraphics(), firstLayout, secondLayout);


   // calculate a match score or correlation and store it

   // rotate around in 15 degree increments,
   // record orientation and match score
   // reflect and repeat
   // pick the best, and explor + - 15 degrees.

   // what if we picked a very bad node?

    //try again with another node



  }

  private void calcMatch (boolean[] useNodes, Point2D.Double[] firstLayout,
                              Point2D.Double[] secondLayout)
  {
    //should we use absolute differnence or correlation?

  }


  /**
   * Draws points for two layouts on screen to debug matching
   */
  private void plotTwoLayouts(Graphics graphics, Point2D.Double[] firstLayout,
                              Point2D.Double[] secondLayout)
  {
    //debug
    System.out.println("plotTwoLayouts");
    //temp debug graphics:
    Graphics2D g = (Graphics2D)graphics;
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                       RenderingHints.VALUE_ANTIALIAS_ON);
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
        0.5f));
    RectangularShape nodeShape = new Ellipse2D.Double();
    Dimension nodeSize = new Dimension(5,5);
    g.setColor(Color.blue);
    //plot first layout
    for (int i = 0; i < useNodes.length; i++)
    {
      if (useNodes[i] == true)
      {
      nodeShape.setFrame(firstLayout[i],nodeSize);
      g.draw(nodeShape);
    }
    }
    //plot 2nd layout
    g.setColor(Color.red);
    for (int i = 0; i < useNodes.length; i++)
    {
      if (useNodes[i] == true)
      {
      nodeShape.setFrame(secondLayout[i],nodeSize);
      g.draw(nodeShape);
    }
    }

  }

  public void paint(Graphics g)
  {
    //debug
    System.out.println("paintCalled");
    //paint all of the points on the window,
    //differnt colors for each layout

    g.clearRect(0,0,this.getWidth(),this.getHeight());
    g.setColor(Color.red);
    g.drawString("notWorking", 50,50);
    if ((firstLayout != null) & (secondLayout != null))
    {
      plotTwoLayouts(g, firstLayout, secondLayout);
    }


  }

  //window listeners-------

  public void windowClosing (WindowEvent evt)
  {
    this.setVisible(false);
    currentSlice.removeStabilityEstimate();
    this.dispose();
  }

  public void windowActivated(WindowEvent evt){}
  public void windowClosed(WindowEvent evt){}
  public void windowDeactivated(WindowEvent evt){}
  public void windowDeiconified(WindowEvent evt){}
  public void windowIconified(WindowEvent evt){}
  public void windowOpened(WindowEvent evt){}
}