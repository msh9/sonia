package sonia.layouts;

import java.util.*;
import java.lang.Math;

import sonia.CoolingSchedule;
import sonia.LayoutSlice;
import sonia.LayoutUtils;
import sonia.NetUtils;
import sonia.SoniaController;
import sonia.SoniaLayoutEngine;
import sonia.settings.ApplySettings;
import sonia.ui.ApplySettingsDialog;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

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
 * An early implementation of the Kamada-Kawai "spring-embedder" layout algrorithm.
 * See, Kamada, K., Kawai, S. (1989) <CITE>An Algorithm for Drawing General Undirected Graphs.</CITE>
 * Information Processing Letters 31, 7-15.  This version follows the original
 * paper more closely, but is not as well developed or as robust as the
 * Multi-ComponentKKLayout, and it doesn't support layouts with multiple components.
 * This layout algorithm has also not been tested recently.
 */
public class KKLayout implements NetLayout, Runnable
{
  private SoniaController control;
  private SoniaLayoutEngine engine;
  private int pad = 20;
  //private int initialIter = 10;  //number of loops before cooling starts
  private int maxPasses = 500;     //maximum number of loops through the Fruch layout procedure
  private int passes;
  private double optDist;    //optimal distance for nodes, gets reset later in code
  private double springConst = 1;       //K in KK paper (avg. i,j distance?)
  private double minEpsilon = 1;  //target deltaM goal

  private boolean animate = true;       //whether to animate the transitions
  private boolean firstLayout = true;
  private boolean noBreak = true;
  private double width;
  private double height;
  private CoolingSchedule schedule;
  private LayoutSlice slice;
  private LayoutUtils utils;
  private ApplySettings settings;
  private String layoutInfo = "";

  public KKLayout(SoniaController cont)
  {
    control = cont;
    schedule = new CoolingSchedule(5);
    control.showFrame(schedule);
  }
  public void setupLayoutProperties(ApplySettingsDialog settings){}

  public void applyLayoutTo(LayoutSlice s, int w, int h,
                            ApplySettings set)
  {
    slice = s;
    settings = set;
    maxPasses = schedule.getMaxUsrPasses();
    width = w;
    height = h;
    //start algorthem on new thread so that it can be paused, etc
    Thread layoutRunner = new Thread(this,"KKLayout loop");
    layoutRunner.setPriority(10);
    layoutRunner.start();
  }

  public void run()
  {
    //so other processes will know it is being worked on
    slice.setLayoutFinished(false);

    //set up components
    ArrayList components = NetUtils.getComponents(NetUtils.getSymMaxMatrix(slice),true);
    if (components.size() > 1)
    {
      control.showError("this KK layout canont run on nets multiple components");
    }


    int nNodes = slice.getMaxNumNodes();
    //sets up the matrix of path distances
    DenseDoubleMatrix2D distMatrix =
      NetUtils.getAllShortPathMatrix(slice);

    //sets up kmatrix of forces
    DenseDoubleMatrix2D kMatrix = calcKMatrix(distMatrix, springConst);
    //calc desired distance between nodes
    double optDist = Math.min(width, height) /
      Math.max(NetUtils.calcDiameter(distMatrix), 1);  //RECALCS ALLSHORTPAHS, BUT USE FOR NOW FOR COMPATIBLITY
    //sets up lMatrix of distance between nodes pairs
    DenseDoubleMatrix2D lMatrix = calcLMatrix(distMatrix, optDist);
    //arrays for quick acess to node coords
    double[] xPos = slice.getXCoords();
    double[] yPos = slice.getYCoords();

    int numEdges = slice.getTotalSliceArcs();

    //calc value to start minimization from (should be based on previous?)
    //FIGURE OUT COOLING SCHEDULE
    //epsilon = (nNodes * numEdges)/2;
    //figure out the initial stat to compare to at the end
    double initialEnergy = getEnergy(lMatrix, kMatrix, xPos, yPos);
    //debug
   // System.out.println("initial energy "+initialEnergy);
    double epsilon = initialEnergy / nNodes;
    //figure out which node to start moving first
    double deltaM;
    int maxDeltaMIndex = 0;
    double maxDeltaM = getDeltaM(0, lMatrix, kMatrix, xPos, yPos);
    for (int i = 1; i < nNodes; i++) {
      deltaM = getDeltaM(i, lMatrix, kMatrix, xPos, yPos);
      if (deltaM > maxDeltaM) {
        maxDeltaM = deltaM;
        maxDeltaMIndex = i;
      }
    }

    int passes = 0;
    int subPasses = 0;
    //epsilon minimizing loop
    while ((epsilon > minEpsilon) && noBreak) {
      double previousMaxDeltaM = maxDeltaM + 1;
      // KAMADA-KAWAI LOOP: while the deltaM of the node with
      // the largest deltaM  > epsilon..
      while ((maxDeltaM > epsilon) && ((previousMaxDeltaM - maxDeltaM) > 0.1)
          && noBreak)
        {

          double[] deltas;
          double moveNodeDeltaM = maxDeltaM;
          double previousDeltaM = moveNodeDeltaM +1;

          //KK INNER LOOP while the node with the largest energy > epsilon...
          while ((moveNodeDeltaM > epsilon) && noBreak) {

            //get the deltas which will move node towards the local minima
            deltas = getDeltas(maxDeltaMIndex, lMatrix, kMatrix,
                               xPos, yPos);
            //set coords of node to old coords + changes
            xPos[maxDeltaMIndex] += deltas[0];
            yPos[maxDeltaMIndex] += deltas[1];
            previousDeltaM = moveNodeDeltaM;
            //recalculate the deltaM of the node w/ new vals
            moveNodeDeltaM = getDeltaM(maxDeltaMIndex, lMatrix, kMatrix,
                                       xPos, yPos);
            subPasses++;
            if (subPasses > maxPasses) noBreak = false;
          }
          previousDeltaM = maxDeltaM;
          //recalculate deltaMs and find node with max
          maxDeltaMIndex = 0;
          maxDeltaM = getDeltaM(0, lMatrix, kMatrix, xPos, yPos);
          for (int i = 1; i < nNodes; i++) {
            deltaM = getDeltaM(i, lMatrix, kMatrix, xPos, yPos);
            if (deltaM > maxDeltaM) {
              maxDeltaM = deltaM;
              maxDeltaMIndex = i;
            }
          }

          //if set to update display, update on every nth pass
          int repaintN = Integer.parseInt(settings.getProperty(ApplySettings.LAYOUT_REPAINT_N));
          if ((repaintN > 0)&& (passes % repaintN == 0))
          {
            if (settings.getProperty(ApplySettings.RECENTER_TRANSFORM).equals(ApplySettings.RECENTER_DURING))
            {
              LayoutUtils.centerLayout(slice, (int)width, (int)height, xPos, yPos,
            		  Boolean.getBoolean(settings.getProperty(ApplySettings.TRANSFORM_ISOLATE_EXCLUDE)));
            }
            control.updateDisplays();
          }

          passes ++;
          //attempt to let redraws of other windows, pause, etc
          Thread.yield();
        }
        //USE COOLING SCHEDULE HERE!!
      epsilon -= epsilon / 4;
    }

    for (int i =0; i<nNodes; i++)
    {
      slice.setCoords(i,xPos[i],yPos[i]);
    }
    engine.finishLayout(settings,this,slice,width, height);

  }

  private double[] getDeltas(int i,DenseDoubleMatrix2D lMatrix,
                             DenseDoubleMatrix2D kMatrix, double[] xPos,
                             double[] yPos)
  {
    //solve deltaM partial eqns to figure out new position for node of index i
    // where deltaM is close to 0 (or less then epsilon)
    int nNodes = lMatrix.rows();
    double[] deltas = new double[2];  //holds x and y coords to return
    double dx, dy, dd;
    double deltaX, deltaY;
    double xPartial = 0;
    double yPartial = 0;
    double xxPartial = 0;
    double xyPartial = 0;
    double yxPartial = 0;
    double yyPartial = 0;
    for (int j = 0; j < nNodes; j++) {
      if (i != j) {
        dx = xPos[i] - xPos[j];
        dy = yPos[i] - yPos[j];
        dd = Math.sqrt(dx*dx+dy*dy);

        double kMatrixVal = kMatrix.getQuick(i, j);
        double lMatrixVal = lMatrix.getQuick(i, j);
        double ddCubed = dd * dd * dd;


        xPartial += kMatrixVal * (dx - lMatrixVal * dx / dd);
        yPartial += kMatrixVal * (dy - lMatrixVal * dy / dd);
        xxPartial += kMatrixVal * (1 - lMatrixVal * dy * dy / ddCubed);
        xyPartial += kMatrixVal * (lMatrixVal * dx * dy/ ddCubed);
        yxPartial += kMatrixVal * (lMatrixVal * dy * dx/ ddCubed);
        yyPartial += kMatrixVal * (1 - lMatrixVal *dx * dx / ddCubed);
      }
    }

    //calculate x and y position difference using partials
    deltas[0] = ((-xPartial) * yyPartial - xyPartial * (-yPartial)) /
      (xxPartial * yyPartial - xyPartial * yxPartial);
    deltas[1] = (xxPartial * (-yPartial) - (-xPartial) * yxPartial) /
      (xxPartial * yyPartial - xyPartial * yxPartial);

    return deltas;
  }

  //returns the energy of i (looping over all other nodes)
  private double getDeltaM(int i, DenseDoubleMatrix2D lMatrix,
                           DenseDoubleMatrix2D kMatrix, double[] xPos,
                           double[] yPos)
  {
    int nNodes = lMatrix.rows();
    double deltaM = 0;
    double xPartial = 0;
    double yPartial = 0;
    double dx, dy, dd;
    for (int j = 0; j < nNodes; j++) {
      if (i != j) {
        dx = xPos[i] - xPos[j];
        dy = yPos[i] - yPos[j];
        dd = Math.sqrt(dx*dx+dy*dy);
        double kMatrixVal = kMatrix.getQuick(i, j);
        double lMatrixVal = lMatrix.getQuick(i, j);
        xPartial += kMatrixVal * (dx - lMatrixVal * dx / dd);
        yPartial += kMatrixVal * (dy - lMatrixVal * dy / dd);
      }
    }
    //deltaM = sqrt(xPartial^2+yPartial^2)
    deltaM = Math.sqrt(xPartial * xPartial + yPartial * yPartial);
    return deltaM;
  }

  private double getEnergy(DenseDoubleMatrix2D lMatrix,
                         DenseDoubleMatrix2D kMatrix, double[] xPos,
                         double[] yPos)
 {
   int nNodes = lMatrix.rows();
   double energy = 0;
   double dx, dy,lij;
   int limit =  nNodes - 1;
   //for all pairs..
   for(int i = 0; i < limit; i++) {
     for(int j = i+1; j<nNodes; j++) {
       dx = xPos[i] - xPos[j];
       dy = yPos[i] - yPos[j];
       lij = lMatrix.getQuick(i,j);
       energy += 0.5 * kMatrix.getQuick(i,j) * (dx * dx + dy * dy +
                                                lij* lij - 2 * lij*
                                                Math.sqrt(dx*dx+dy*dy));
     }
   }
   //debug
   //System.out.println("kk nrg:"+energy);
   return energy;
 }


  //set up matrix of spring forces between pairs using K/(d[i][j]^2)
 private DenseDoubleMatrix2D calcKMatrix(DenseDoubleMatrix2D distMatrix,
                                         double spring)
 {
   int nNodes = distMatrix.rows();
   DenseDoubleMatrix2D kMatrix = new DenseDoubleMatrix2D(nNodes,nNodes);
   for (int i = 0; i < nNodes; i++) {
     for (int j = 0; j < nNodes; j++) {
       double distMVal = distMatrix.getQuick(i, j);
       kMatrix.setQuick(i, j, (spring/(distMVal * distMVal)));
     }
   }
   return kMatrix;
 }

 //set up matrix of desired edge lengths using L*d[i][j]
 private DenseDoubleMatrix2D calcLMatrix(DenseDoubleMatrix2D distMatrix,
                                         double optDist)
 {
   int nNodes = distMatrix.rows();
   DenseDoubleMatrix2D lMatrix = new DenseDoubleMatrix2D(nNodes,nNodes);
   for (int i = 0; i < nNodes; i++) {
     for (int j = 0; j < nNodes; j++) {
       lMatrix.setQuick(i, j,(optDist * distMatrix.getQuick(i,j)));
     }
   }
   return lMatrix;
 }


  public String getLayoutType()
  {
    return "Kamada-Kawai layout";
  }
  public String getLayoutInfo()
  {
    return layoutInfo;
  }
  public void pause()
  {
    noBreak = false;
  }
  public void resume()
  {
    noBreak = true;
  }
  public void disposeLayout()
  {
    //need to get rid of layout settigns dialog?
    schedule.hide();
    schedule = null;
  }
}
