package sonia.layouts;

import java.util.*;
import java.lang.Math;

import sonia.ApplySettingsDialog;
import sonia.CoolingSchedule;
import sonia.LayoutSlice;
import sonia.LayoutUtils;
import sonia.NetLayout;
import sonia.SoniaController;
import sonia.SoniaLayoutEngine;
import jal.INT.Modification;

//mport javax.swing.SwingUtilities;

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
 * Random FR is based on the same concept as FRLayout, but uses a differnt procedure
 * for getting to its results.  In each pass, regular FR loops over all the nodes
 *  in order, calculating repulsion and attraction, and then updating positions.
 * Random FR
 * loops over all nodes in each pass, but in a random order.  It calculates the
 * repulsion and attraction for one node at a time, then updates its position and
 * moves on to the next node.  Seems to converge more quickly and without the
 * rotation problem, but some wierdness where ties are asymetric.  Also has not
 * been maintained to stay current with current FR implmentation in FRLayout
 */

public class RandomFRLayout implements NetLayout, Runnable
{

  private SoniaController control;
  private SoniaLayoutEngine engine;
  private int pad = 20;
  private int maxPasses = 500;     //maximum number of loops through the Fruch layout procedure
  private int passes;
  private double optDist;    //optimal distance for nodes, gets reset later in code

  private boolean animate = true;       //whether to animate the transitions
  private boolean noBreak = true;
  private double width;
  private double height;
  private CoolingSchedule schedule;
  private LayoutSlice slice;
  private ApplySettingsDialog settings;
  private String layoutInfo = "";

  public RandomFRLayout(SoniaController cont, SoniaLayoutEngine eng)
  {
    control = cont;
    engine = eng;
    schedule = new CoolingSchedule(6);

  }

  public void setupLayoutProperties(ApplySettingsDialog settings)
  {
    settings.addLayoutProperty("optimum dist",20);
    settings.addLayoutProperty("move dist",200);
  }


  public void applyLayoutTo(LayoutSlice s, int w, int h,
                            ApplySettingsDialog set)
  {
    slice = s;
    settings = set;
    maxPasses = schedule.getMaxUsrPasses();
    width = w;
    height = h;

    //threading stuff, to make this run indepent of the displays
    //make a new thread of this
    Thread layoutRunner = new Thread(this,"FRLayout loop");
    layoutRunner.setPriority(10);
    layoutRunner.start();
  }

  public void run()
  {
    // calc constants
    int nNodes = slice.getMaxNumNodes();
    //tDist = 0.6*Math.sqrt(((width * height) / (nNodes+1)));
    optDist = settings.getLayoutProperty("optimum dist");
    double startTemp= settings.getLayoutProperty("move dist"); //width/10;
    double temp = startTemp;
    passes = 0;
    double xDelta = 0;
    double yDelta = 0;
    double deltaLength = 0;
    double force = 0;

    //an array with all the id's in it to shuffle to randomize order
    //SHOULD ONLY INCLUDE PRSENT NODES
    int[] updateOrder = new int[nNodes];
    for (int i=0; i<nNodes; i++)
    {
      updateOrder[i]=i;
    }

    //make arrays corresponding to the coords of each node
    double[] xPos = slice.getXCoords();
    double[] yPos = slice.getYCoords();

    //remove check for self loops and remove them
    // if (NetUtilities.hasSelfLoops(nodeList)) edges = removeLoops(edges);
    //make arrays corresponding to the displacement vector for each node
    double[] xDisp = new double[nNodes];
    double[] yDisp = new double[nNodes];

    // keep passing through the layout loop until the temp is low
    // initialIter + time for cooling schedule
    //this should be a thread
    while ((temp > 0.1) && (passes < maxPasses) && noBreak)
    {
      //shuffle the order of the elements in updateOrder
      Modification.random_shuffle(updateOrder,0,nNodes);
      //update the positions of each node, one at atime, in the random order
      for (int i = 0; i < nNodes; i++)
      {
        int v = updateOrder[i];
        xDisp[v] = 0;
        yDisp[v] = 0;

        //calc repulsion from all nodes execpt v
        for (int u = 0; u < nNodes; u++)
        {
          //for u not equal v
          if (u != v)
          {
            //get difference of position vectors
            xDelta = xPos[v] - xPos[u];
            yDelta = yPos[v] - yPos[u];

            //set vu disp vector
            deltaLength = Math.sqrt((xDelta*xDelta) + (yDelta*yDelta));
            // get div by 0 "errors" if deltaLength is 0.
            // BUT WHAT SHOULD deltaLength BE IN THESE CASES?
            if (deltaLength == 0) deltaLength = 0.001;//adds a HUGE kick

            //calc repulsion
            force = calcRepulsion(deltaLength);
            xDisp[v] += (xDelta / deltaLength) * force;
            yDisp[v] += (yDelta / deltaLength) * force;

            //calculate attractive forces between nodes connected by an edge to v
            if (slice.getSymMaxArcWeight(v,u) != 0)
            {

              force = calcAttraction(deltaLength);
              xDisp[v] -= (xDelta / deltaLength) * force;
              yDisp[v] -= (yDelta / deltaLength) * force;
            }
          }
          //caculate displacement, but limit max displacement to temp

           double xDispVal = xDisp[v];
           double yDispVal = yDisp[v];
           deltaLength = Math.sqrt((xDispVal * xDispVal) +
                                   (yDispVal * yDispVal));
           if (deltaLength > temp)
           {
             xPos[v] += xDisp[v] / (deltaLength / temp);
             yPos[v] += yDisp[v] / (deltaLength / temp);
           }
           else
           {
             xPos[v] += xDisp[v];
             yPos[v] += yDisp[v];
           }
        }

      }

      //cool temp
      temp = startTemp * schedule.getTempFactor(passes);

      //if set to update display, update on every nth pass
      if (settings.isRepaint() & (settings.getRepaintN() > 0)
          & (passes % settings.getRepaintN() == 0))
      {
        if (settings.isRecenter())
        {
          LayoutUtils.centerLayout(slice, (int)width, (int)height, xPos, yPos,
                                   settings.isIsolateExclude());
        }
        engine.updateDisplays();
      }
      passes++;
      //attempt to let redraws of other windows, pause, etc
      Thread.yield();

    }

    for (int i =0; i<nNodes; i++)
    {
      slice.setCoords(i,xPos[i],yPos[i]);
    }
    engine.finishLayout(this,slice,width,height);

  }


  private double calcAttraction(double dist) {
    return dist * dist / optDist;
  }

  private double calcRepulsion(double dist) {
    return optDist * optDist / dist;
  }



  public void pause()
  {
    noBreak = false;
  }
  public void resume()
  {
    noBreak = true;
  }


  //should be get details, and return all the stats on the layout
  public String getLayoutType()
  {
    return "Random FR layout";
  }
  public String getLayoutInfo()
  {
    return layoutInfo;
  }
  public void disposeLayout()
  {
    //need to get rid of layout settigns dialog?
    schedule.hide();
    schedule = null;
  }
}