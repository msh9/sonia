package sonia.layouts;
import cern.colt.matrix.*;
import cern.colt.matrix.impl.*;
import cern.colt.matrix.linalg.*;
import java.util.*;

import sonia.LayoutSlice;
import sonia.LayoutUtils;
import sonia.NetUtils;
import sonia.SoniaController;
import sonia.SoniaLayoutEngine;
import sonia.settings.ApplySettings;
import sonia.ui.ApplySettingsDialog;


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
 * Moody's Peer Influence<BR>
 * <P>
 * A distinct kind of algorithm, demonstrating a completely different model of
 * social space,  can be found in Moody's Peer Influence algorithm
 * (see Moody, James, Daniel A. McFarland, and Skye Bender-deMoll. In press.
 * <CITE>Visualizing Network Dynamic</CITE>. American Journal of Sociology,
 * forthcoming (January 2000). Moody, James. 2001. <CITE>Peer Influence Groups:
 * Identifying Dense Clusters in Large Networks</CITE>: Social Networks 23: 261-283).
 * It does not optimize arc lengths at all, and works instead to position nodes
 * so they directly interpolate the coordinates of the nominations which are most
 * influential.  This creates a layout which conveys relations more through motion
 * than distance.
 * </P><P>
 * In the PI algorithm, a node's position is a function of (a) their prior position
 * - a self-weight and (b) the average position of those they nominate. This process
 * is iterative, adjusting each person's position to that of others multiple times.
 * The algorithm can be elegantly operationalized as a set of matrix operations on
 *  the vectors of node coordinates. (Moody, 2001) First, the raw similarity
 * adjacency matrix of arc weights is modified by setting the diagonal value to
 * the "self-weight" parameter.
*  The matrix is then row-normalized (each row is divided by its sum) so that
* it won't blow up the network with each iteration.  The x and y coordinate
* vectors are then repeatedly multiplied by the matrix.  In practice, the
* number of needed iterations is quite small, as the procedure tends toward
* high "consensus" overlapping positions.  Using a fairly large value for the
* self-weight parameter is also important in preventing nodes from stacking.
* Krempel (1999) discusses a technique for a similar "Barycentric" placement
* algorithm which prevents overlap by pinning one set of nodes into position
* around the perimiter of the layout, forcing the remaining nodes to arrange
* themselves in the intermediate space.
* </P><P>
 * The result of PI is often similar in flavor to a Metric MDS plot, which sh
 * ould not be surprising given their common roots (under a particular set of
 * assumptions) to a general class of eigenvalue models (see Friedkin 1998).
 * However, unlike metric MDS, the PI algorithm is very strongly influenced by
 * its starting coordinates.   It also presents a challenge because the
 * "chaining" technique (starting each layout from the previous layout’s
 * coordinates to increase animation stability) can function much like several
 * iterations of the algorithm, resulting in too much clumping.  An more
 *  effective procedure is to start each layout from a fixed set of node
 *  positions, such as a circle, so that the nodes will move into the center
 * in a consistent fashion as they move towards eachother.

 */
public class PILayout implements NetLayout, Runnable
{
  private SoniaController control;
  private SoniaLayoutEngine engine;
  private ApplySettings settings;
  private int width;
  private int height;
  private LayoutSlice slice;
  private boolean noBreak = true;
  private String layoutInfo;
  private  DenseDoubleMatrix1D xCoords;
  private DenseDoubleMatrix1D yCoords;
  private SparseDoubleMatrix2D adjMatrix;
  private Algebra matMath = new Algebra();
  private double selfWeight = 1;
  private int iterations = 6;
  private int passes = 0;
  
  public static final String ITERS = "iterations";
  public static final String SELF_WEIG = "selfWeight";
  public static final String MIN_RAD = "minRadius";

  public PILayout(SoniaController cont, SoniaLayoutEngine eng)
  {
    control = cont;
    engine = eng;
  }

  //called by settings dialog to ask if there are any properties that need fields
  public void setupLayoutProperties(ApplySettingsDialog dialog)
  {
    //add layout specific vars to layot apply settings dialog
     dialog.addLayoutProperty(ITERS,6);
     dialog.addLayoutProperty(SELF_WEIG,1);
     dialog.addLayoutProperty(MIN_RAD,0.0);
  }

  public void applyLayoutTo(LayoutSlice sl,int w, int h, ApplySettings set)
  {
    slice = sl;
    settings = set;
    width = w;
    height = h;
    iterations = (int)Math.round(Double.parseDouble(settings.getProperty(ITERS)));
    selfWeight = Double.parseDouble(settings.getProperty(SELF_WEIG));
    passes = 0;

    //get x and y coords and into 1D matricies (vectors)
    xCoords = new DenseDoubleMatrix1D(slice.getXCoords());
    yCoords = new DenseDoubleMatrix1D(slice.getYCoords());
    //get the an adjacency matrix for the slice
    adjMatrix = NetUtils.getMatrix(slice);
    //change diagonals to selfWeight
    for (int i=0; i<adjMatrix.rows(); i++)
    {
      //assume matrix is square
      adjMatrix.setQuick(i,i,selfWeight);
    }

    //renorm rows so they add up to 1 and coords won't blow up
    //ASSUMEING SIMILARITY!!
    //for each row
    for (int i=0; i<adjMatrix.rows(); i++)
    {
      //find the sum of each row
      double rowSum = 0;
      for (int j=0; j<adjMatrix.columns(); j++)
      {
        rowSum += adjMatrix.getQuick(i,j);
      }
      //renorm row so it sums to 1
      for (int j=0; j<adjMatrix.columns(); j++)
      {
        adjMatrix.setQuick(i,j,(adjMatrix.getQuick(i,j)/rowSum));
      }
    }
    //call the thread to go into the loop
    //threading stuff, to make this run indepent of the displays
   //make a new thread of this
   Thread layoutRunner = new Thread(this,"PILayout loop");
   layoutRunner.setPriority(10);
    layoutRunner.start();
  }

  public void run()
  {

    while ((passes < iterations) & noBreak)
    {
      double minRad = Double.parseDouble(settings.getProperty(MIN_RAD));
      if (minRad > 0.0)
      {
        //do PI with a constraint of a minimum radius between nodes
        //do the pi calc
        double[] newX = ((DenseDoubleMatrix1D)matMath.mult(adjMatrix,xCoords)).toArray();
        double[] newY = ((DenseDoubleMatrix1D)matMath.mult(adjMatrix,yCoords)).toArray();
        checkMinRadius(minRad,newX,newY);
      }
      else
      {
    //multiply the coord vectors by the matrix iterations  times
      xCoords = (DenseDoubleMatrix1D)matMath.mult(adjMatrix,xCoords);
      yCoords = (DenseDoubleMatrix1D)matMath.mult(adjMatrix,yCoords);
      }

      //if set to update display, update on every nth pass
      int repaintN = Integer.parseInt(settings.getProperty(ApplySettings.LAYOUT_REPAINT_N));
      if ((repaintN > 0)&& (passes % repaintN == 0))
	
      {
    	  if (settings.getProperty(ApplySettings.RECENTER_TRANSFORM).equals(ApplySettings.RECENTER_DURING))
          {
            LayoutUtils.centerLayout(slice, (int)width, (int)height, xCoords.toArray(), yCoords.toArray(),
          		  Boolean.getBoolean(settings.getProperty(ApplySettings.TRANSFORM_ISOLATE_EXCLUDE)));
          }
        //show the value one the schedule
        engine.updateDisplays();

      }
      passes++;
      //attempt to let redraws of other windows, pause, etc
      Thread.yield();
    }
   engine.finishLayout(settings,this,slice,width, height);
  }

  private void checkMinRadius(double minRad, double[] newX, double[] newY)
  {
    double[] oldX = xCoords.toArray();
    double[] oldY = yCoords.toArray();
    int maxLoops = 0;  //to make sure we never get stuck
    ArrayList closeNodes = new ArrayList();
    while (maxLoops < 10)
    {
      //make a list of all the node pairs that are too close
      double distance;
      for (int i=0;i<newX.length ; i++)
      {
        for (int j=i+1;j<newX.length ;j++ )
        {
          //calc all the pairwise distances
          distance = Math.sqrt((newX[i]-newX[j])*(newX[i]-newX[j]) +
                            (newY[i]-newY[j])*(newY[i]-newY[j]));
          //debug
          System.out.println("distance "+distance);
          if (distance < minRad)
          {
            int[] pair = {i,j};
            closeNodes.add(pair);
          }
        }
      }
      //debg
      System.out.println("num close nodes" +closeNodes.size());
      if(closeNodes.size() < 1)
      {
        maxLoops = 10;
      }
      else
      {
        //try to adjust the positions by shortening both displacement vectors
        //since we are looping in order, need to double buffer coords
          double[] bufferX = (double[])newX.clone();
          double[] bufferY = (double[])newY.clone();

        for (int n=0;n<closeNodes.size() ;n++ )
        {
          int[] pair = (int[])closeNodes.get(n);
          int i = pair[0];
          int j = pair[1];


          //components of displacement vector
          double iDeltaX = newX[i] - oldX[j];
          double iDeltaY = newY[i] - oldY[j];
          double jDeltaX = newX[i] - oldX[j];
          double jDeltaY = newY[i] - oldY[j];
          //length of displacement vector
          double iDisp = Math.sqrt(iDeltaX*iDeltaX + iDeltaY*iDeltaY);
          double jDisp = Math.sqrt(jDeltaX*jDeltaX + jDeltaY*iDeltaY);

          //distance if moved to new position
          double newPosDist = Math.sqrt((newX[i]-newX[j])*(newX[i]-newX[j]) +
                            (newY[i]-newY[j])*(newY[i]-newY[j]));
          //total distance to be "bactracked"
          double totalAdjust = minRad - newPosDist;
          //adjustment should be pe proportional to distance I and j move
          double iAdjFact = (iDisp+jDisp) / iDisp;
          double jAdjFact = (iDisp+jDisp) / jDisp;
          //calc desierd length of displacements
          double newIDisp = iDisp - totalAdjust*iAdjFact;
          double newJDisp = jDisp - totalAdjust*iAdjFact;
          //rescale x y and components
          iDeltaX = iDeltaX * (newIDisp / iDisp);
          iDeltaY = iDeltaY * (newIDisp / iDisp);
          jDeltaX = jDeltaX * (newJDisp / jDisp);
          jDeltaY = jDeltaY * (newJDisp / jDisp);
          //update buffer coords
          bufferX[i] = oldX[i]+iDeltaX;
          bufferY[i] = oldY[i]+iDeltaY;
          bufferX[j] = oldX[j]+jDeltaX;
          bufferY[j] = oldY[j]+jDeltaY;

        }
        //set new coords to buffer
        newX = bufferX;
       newY = bufferY;
      }
       //clear the list
       closeNodes.clear();
      maxLoops++;
    }
    //update the actual vectors of coords
    xCoords.assign(newX);
    xCoords.assign(newY);
    if (maxLoops >=10)
    {
      control.showError("unable to adjust all node distances to min radius in 10 passes");
    }

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
    return "Moody's Peer Influence layout";
  }
  public String getLayoutInfo()
  {
    return layoutInfo;
  }
  public void disposeLayout()
  {
    //need to get rid of layout settigns dialog?
  }

}