package sonia;

import java.util.*;
import java.lang.Math;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.impl.*;


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
 * Positions nodes in layout according to iterations of an
 * implementation of the Fruchmen-Reingold graph layout
 * algorithm, except that nodes also maintain a phatom tie or "rubber band"
 * tugging them close to their location in the previous layout.  Needs more
 * work, has not been mainted up the current implementation of FRLayout <p>
 *
 *
 */

public class RubBandFRLayout implements NetLayout, Runnable
{

  private SoniaController control;
  private SoniaLayoutEngine engine;
  private int pad = 20;
  private int maxPasses = 500;     //maximum number of loops through the Fruch layout procedure
  private int passes;
  private double optDist;    //optimal distance for nodes, gets reset later in code

  private boolean animate = true;       //whether to animate the transitions
  private boolean firstLayout = true;
  private boolean noBreak = true;
  private double width;
  private double height;
  private CoolingSchedule schedule;
  private LayoutSlice slice;
  private ApplySettingsDialog settings;
  private String layoutInfo = "";

  public RubBandFRLayout(SoniaController cont, SoniaLayoutEngine eng)
  {
    control = cont;
    engine = eng;
    schedule = new CoolingSchedule(8);

  }

  public void setupLayoutProperties(ApplySettingsDialog settings)
  {
    settings.addLayoutProperty("optimum dist",20);
    settings.addLayoutProperty("repel cutoff",999);
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
    Thread layoutRunner = new Thread(this,"RubBandFRLayout loop");
    layoutRunner.setPriority(10);
    layoutRunner.start();
  }

  public void run()
  {
    //get the network of only the nodes present
    Subnet subnet = new Subnet(NetUtils.getMatrix(slice),
                               slice.getPresentNodes());
    // calc constants
    int nNodes = subnet.getNumNodes();
//    optDist = 0.6*Math.sqrt(((width * height) / (nNodes+1)));
    optDist = settings.getLayoutProperty("optimum dist");
    double boundRadius = Math.min(width/2,height/2);   //radius of circle used by bounding circvle
    double centerX = width/2;
    double centerY = height/2;
    double startTemp= settings.getLayoutProperty("move dist");  //check with cooling schedule?
    double temp = startTemp;
    passes = 0;
    double xDelta = 0;
    double yDelta = 0;
    double deltaLength = 0;
    double force = 0;

    //make arrays corresponding to the coords of each node
    double[] xPos = subnet.getSubsetArray(slice.getXCoords());
    double[] yPos = subnet.getSubsetArray(slice.getYCoords());
    //arrays which hold the starting coords (could also have loaded them from prev slice)
    double[] xFixed =subnet.getSubsetArray(slice.getXCoords());
    double[] yFixed = subnet.getSubsetArray(slice.getYCoords());


    //set up arcs matrix, (assumed to be similarities)
    //symetrize it
    SparseDoubleMatrix2D arcsMatrix = NetUtils.getSymMaxMatrix(subnet.getMatrix());
    //reverse it so that it will be disimilarities
    //using max and min values of all slices is engine
    arcsMatrix  = NetUtils.getReverse(arcsMatrix,engine.getMaxMatrixVal(),
                                      engine.getMinMatrixValue());

    // check for self loops and remove them
    // if (NetUtilities.hasSelfLoops(nodeList)) edges = removeLoops(edges);

    //make arrays corresponding to the displacement vector for each node
    double[] xDisp = new double[nNodes];
    double[] yDisp = new double[nNodes];

    // keep passing through the layout loop until the temp is low
    // initialIter + time for cooling schedule
    //this should be a thread
    while ((temp > 0.1) && (passes < maxPasses) && noBreak)
    {
      //calculate repulsive forces between each pair of nodes (set both)
      int limit = nNodes - 1;
      for (int v = 0; v < limit; v++)
      {
        xDisp[v] = 0;
        yDisp[v] = 0;
        // can skip many loops by assuming that uv = -vu and looping in factorial
        for (int u = v + 1; u < nNodes; u++)
        {
          //get difference of position vectors
          xDelta = xPos[v] - xPos[u];
          yDelta = yPos[v] - yPos[u];

          //trap condition where nodes have same position
          if((xDelta == 0) && (yDelta == 0))
          {
            //don't do anything in hopes that someone else
            //will kick them apart

            // Do we continue or break here? Waiting for Skye's
            // answer.
            continue;
            //break;
          }
          //set vu disp vector
          deltaLength = Math.sqrt((xDelta*xDelta) + (yDelta*yDelta));
          force = calcRepulsion(deltaLength,arcsMatrix.getQuick(v,u));
          xDisp[v] += (xDelta / deltaLength) * force;
          yDisp[v] += (yDelta / deltaLength) * force;
          //set uv disp vector (-vu)
          xDisp[u] -=  (xDelta / deltaLength) * force;
          yDisp[u] -= (yDelta / deltaLength) * force;
        }
      }

      //calculate attractive forces between nodes connected by an edge

      for (int i=0; i<nNodes; i++)
      {
        for (int j=i+1; j<nNodes; j++)
        {
          if ((arcsMatrix.getQuick(i,j) != 0.0) &
              (arcsMatrix.getQuick(i,j) != Double.POSITIVE_INFINITY))
          {
          int vIndex = i;  //Quick KLUDGE
          int uIndex = j;

          //get difference of position vectors
          xDelta = xPos[vIndex] - xPos[uIndex];
          yDelta = yPos[vIndex] - yPos[uIndex];
          //set vu disp vector
          deltaLength = Math.sqrt((xDelta * xDelta) + (yDelta * yDelta));
          // get div by 0 "errors" if deltaLength is 0.
          // BUT WHAT SHOULD deltaLength BE IN THESE CASES?
          if (deltaLength == 0) deltaLength = 0.001;//adds a HUGE kick
          force = calcAttraction(deltaLength,arcsMatrix.getQuick(vIndex,uIndex));
          xDisp[vIndex] -= (xDelta / deltaLength) * force;
          yDisp[vIndex] -= (yDelta / deltaLength) * force;
          //set uv disp vector to (-vu) because nodes may not be mutually
          //ASSUMES MUTUAL ATTRACTION111
          //also redundant since we are loopoing over all i and j
          xDisp[uIndex] +=  (xDelta / deltaLength) * force;
          yDisp[uIndex] +=  (yDelta / deltaLength) * force;

        }
        }
      }

      //ADD ADITIONAL LOOP ATTRACTING NODES TO THEIR STARTING POSITION
      for (int i=0; i<nNodes; i++)
              {

                //get difference of position vectors
                xDelta = xPos[i] - xFixed[i];
                yDelta = yPos[i] - yFixed[i];
                //set vu disp vector
                deltaLength = Math.sqrt((xDelta * xDelta) + (yDelta * yDelta));
                // get div by 0 "errors" if deltaLength is 0.
                // BUT WHAT SHOULD deltaLength BE IN THESE CASES?
                if (deltaLength == 0) deltaLength = 0.001;//adds a HUGE kick
                force = calcAttraction(deltaLength,1);
                xDisp[i] -= (xDelta / deltaLength) * force;
                yDisp[i] -= (yDelta / deltaLength) * force;
        }
      //caculate displacement, but limit max displacement to temp
      //and optionaly to stay within circle
      for (int v=0; v<nNodes; v++)
      {
        double xDispVal = xDisp[v];
        double yDispVal = yDisp[v];
        deltaLength = Math.sqrt((xDispVal * xDispVal) +
                                (yDispVal * yDispVal));
        if (deltaLength > temp)
        {
          //rescale displacement vectors
          xDisp[v] = xDisp[v] / (deltaLength / temp);
          yDisp[v] = yDisp[v] / (deltaLength / temp);
        }
        //if circle restrict is enabled, make sure it doesn't leave boundding circle
        if (settings.isCircleRestrict())
        {
          //calc what the new distance from center would be
          // sqrt((Xc-Xp)^2 + (Yc-Yp)^2)
          double newX = xPos[v]+xDisp[v];
          double newY = yPos[v]+yDisp[v];
          double centXDiff = centerX-newX;
          double centYDiff = centerY-newY;
          double newDist = Math.sqrt(centXDiff*centXDiff +centYDiff*centYDiff);
          //if the new distance would be too big, rescale so it just hits the circle
          if (newDist > boundRadius)
          {
            //but this is very hard, so just don't move the point if it would leave
            xDisp[v] = 0;
            yDisp[v] = 0;
          }
        }
        //now add the displacement vectors to original coords
        xPos[v] += xDisp[v];
        yPos[v] += yDisp[v];
      }

      //cool temp
      temp = startTemp * schedule.getTempFactor(passes);

      //if set to update display, update on every nth pass
      if (settings.isRepaint() & (settings.getRepaintN() > 0)
          & (passes % settings.getRepaintN() == 0))
      {
        //need to pass coords back to slice via subnet's remaping of indexes
        for (int i=0;i<nNodes;i++ )
        {
          slice.setCoords(subnet.getNetIndex(i),xPos[i],yPos[i]);
        }
        if (settings.isRecenter())
        {
          LayoutUtils.barycenterLayout(slice, (int)width, (int)height,
                                       settings.isIsolateExclude());
        }
        control.updateDisplays();
      }
      passes++;
      //attempt to let redraws of other windows, pause, etc
      Thread.yield();

    }


    //need to pass coords back to slice via subnet's remaping of indexes
    for (int i=0;i<nNodes;i++ )
    {
      slice.setCoords(subnet.getNetIndex(i),xPos[i],yPos[i]);
    }

    for (int i =0; i<nNodes; i++)
    {
      slice.setCoords(i,xPos[i],yPos[i]);
    }
    engine.finishLayout(this,slice,width,height);
  }

//IDEA IS TO MULTIPLAY OPT DISTANCE BY THE DESIRED EDGE LENGTH AND HOPE IT WORKS
  private double calcAttraction(double dist, double arcLength)
  {
    return (dist * dist) /(optDist*arcLength);
  }

  private double calcRepulsion(double dist,double arcLength)
  {
    if (arcLength == Double.POSITIVE_INFINITY)
    {
      arcLength = 1;
    }
    double repelVal = (optDist*arcLength * optDist*arcLength) / dist;
    //we want to limit repulsion at cutoff, so if it is greater, set it to 0
    if (dist > settings.getLayoutProperty("repel cutoff"))
    {
      repelVal = 0;
    }
    return repelVal;
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
    return "FR w/ rubberbands layout";
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