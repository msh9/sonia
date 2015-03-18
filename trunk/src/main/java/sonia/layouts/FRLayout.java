package sonia.layouts;

import java.util.*;
import java.lang.Math;

import sonia.CoolingSchedule;
import sonia.LayoutSlice;
import sonia.LayoutUtils;
import sonia.NetUtils;
import sonia.SoniaController;
import sonia.SoniaLayoutEngine;
import sonia.Subnet;
import sonia.settings.ApplySettings;
import sonia.ui.ApplySettingsDialog;
import cern.colt.matrix.DoubleMatrix2D;
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
   * Positions nodes in layout according to a modified implementation
   * of the Fruchterman-Reingold graph layout algorithm. Nodes are
   * positioned according to an iterative algorithm that assumes that
   * nodes repel each other when close, but are attracted to connected
   * nodes. Convergence is obtained by using a "simulated
   * annealing"-style technique with an arbitrary cooling
   * function. Acts on existing node positions, so randomizeLayout()
   * will be called before first update.<p>
   *
   * See, Fruchterman, T.M.J and Reingold, E.M. (1991) <CITE>
   * "Graph Drawing by Force-directed Placement" in Software-Practice and
   * Experience, </CITE>Vol 21(11), 1129-1164
   *
   *
   * <b> is this pseudo code up to date?</b><p>
   *<BR>
   * pseudo code of implementation of Furchterman-Reingold Algorithm<BR>
   * -----------------------------------------
   *<BR>
   * As implemented in Pajek, the algorithm makes initialIter (10) through
   * the algorithm before starting the cooling function.  If this is the
   * first layout, each nodes is given a random initial position.
   *<BR><BR>
   * <CODE>
   * while temp > 0.5 and passes < maxIterations (500)
   * <BR> //calculate repulsive forces between each node
   * <BR> for v = 0 to numberOfNodes
   *  <BR>  for u = v+1 to numberOfNodes
   *   <BR>   calculate the distance vector between the positions of v and u
   *    <BR>  calculate a displacement displaceVec = (distVec/|distVec|)
   *                                               * repulsion(|distVec|)
   *     <BR> add displaceVec vector to v's displacement vector
   *     <BR> subtract displaceVec from u's displacement vector
   *    <BR>end
   *  <BR>end
   *
   * <BR> //calculate attractive forces
   *  <BR>for e = 0 to numberOfEdges
   *   <BR> get the nodes attached to the edge (v and u)
   *   <BR> calculate the distance vector between the positions of v and u
   *    <BR>calculate a displacement displaceVec = (distVec/|distVec|) * attraction(|distVec|)
   *   <BR> subtract displaceVec vector from v's displacement vector
   *   <BR> add displaceVec to u's displacement vector
   *  <BR>end
   *<BR><BR>
   *  <BR>calculate each nodes's displacement, but limit max displacement to temp
   *<BR><BR>
   *  <BR>//decrease temperature parameter acording to cooling schedule
   *<BR>
   *  <BR>if "Show updates" is true and this is an Nth pass, update the layout on screen
   *<BR>
   *  at the end, go over all the nodes to find the max and min of the coords,
   *  rescale all coords so that network will fill the display
   * end while
   *<BR>
   *  <BR>//repulsion function
   * <BR> repulsion(distance) = (optDist^2*arcWeight^2) / layoutdistance
 * <BR>
   * <BR>//attraction function attraction(distance) =
   * <BR> layoutDistance^2 /(optDist*arcWeight))
   *<BR>
   *  <BR>//cooling function
   * <BR> coolTemp(temp) = querys the CoolingSchedule for the the appropriate
   * value for the iteration, as set by the user with the cooling function.
   *<BR>
   * <BR>//optimal distance optimalDistance = layout parameter in apply layout settings
   *</CODE>
   * <p>
   * Additional comments: Because the original algorithm repositions the
   * nodes in a deterministic order, highly structured / regular networks
   * may exhibit rotations drift during the layout.
   *
   */
public class FRLayout implements NetLayout, Runnable
{

  private SoniaController control;
  private SoniaLayoutEngine engine;
  private int pad = 20;
  private int maxPasses = 100;     //maximum number of loops through the Fruch layout procedure
  private int passes;
  private double optDist;    //optimal distance for nodes, gets reset later in code

  private boolean noBreak = true;
  private double width;
  private double height;
  private CoolingSchedule schedule;
  private LayoutSlice slice;
  private ApplySettings settings;
  private String layoutInfo = "";
  
  public static final String OPT_DIST = "optimum distance";
  public static final String MOVE_DIST = "move distance";
  public static final String REPEL_CUTOFF = "repel cutoff";
	/**
	 * property key whose value gives the maximum number of iterations a layout
	 * algorithm should be allowed. Value must be parseable as an int.
	 */
	public static final String MAX_PASS = "max passes";

  /**
   * Configures the layout, displays a cooling schedule with default control points.
   */
  public FRLayout(SoniaController cont, SoniaLayoutEngine eng)
  {
    control = cont;
    engine = eng;
    schedule = new CoolingSchedule(8);
    //schedule.setMaxPasses(maxPasses);
   // maxPasses = Integer.parseInt(settings.getProperty(MAX_PASS,maxPasses+""));
	schedule.setMaxPasses(maxPasses);
    schedule.parseCtlValueString("(0,1.0) (3,0.64) (9,0.33) (17,0.21) (30,0.12) (52,0.08) (74,0.04) (100,0.0)");
    //control.showFrame(schedule);
  }

  /**
   * Adds the FR layout parameters to the applySettingsDialog.  The parameters are:
   * <BR>"optimum distance" - the desired spaceing (in pixels) for nodes connected
   * by arcs of weight 1.
   * <BR>"repel cutoff" - distance in pixels after which to ignore the repulsion effect,
   * to prevent isolates and components from flying away
   * <BR>"move distance" - the distance (in pixels) nodes are permitted to move at
   * eacher iteration if the cooling schedule is set to its maximum value. (the
   * y scale of the cooling schedule)
   */
  public void setupLayoutProperties(ApplySettingsDialog settings)
  {
	settings.addLayoutProperty(MAX_PASS,maxPasses);
    settings.addLayoutProperty(OPT_DIST,20);
    settings.addLayoutProperty(REPEL_CUTOFF,999);
    settings.addLayoutProperty(MOVE_DIST,200);
  }


  public void applyLayoutTo(LayoutSlice s, int w, int h,
                            ApplySettings set)
  {
    slice = s;
    settings = set;
   // maxPasses = schedule.getMaxUsrPasses();
    maxPasses = (int)Math.round(Double.parseDouble(settings.getProperty(MAX_PASS)));
    schedule.setMaxPasses(maxPasses);
    width = w;
    height = h;


    //threading stuff, to make this run indepent of the displays
    //make a new thread of this
    Thread layoutRunner = new Thread(this,"FRLayout loop");
    layoutRunner.setPriority(10);
    layoutRunner.start();
  }

  /**
   * called by the layout thread, should only be called internally by the layout
   */
  public void run()
  {
    //get the network of only the nodes present
    Subnet subnet = new Subnet(NetUtils.getMatrix(slice),
                               slice.getPresentNodes());
    // calc constants
    int nNodes = subnet.getNumNodes();
//    optDist = 0.6*Math.sqrt(((width * height) / (nNodes+1)));
    optDist = Double.parseDouble(settings.getProperty(OPT_DIST));
    double boundRadius = Math.min(width/2,height/2);   //radius of circle used by bounding circvle
    double centerX = width/2;
    double centerY = height/2;
    double startTemp= Double.parseDouble(settings.getProperty(MOVE_DIST));  //check with cooling schedule?
    double temp = startTemp;
    passes = 0;
    double xDelta = 0;
    double yDelta = 0;
    double deltaLength = 0;
    double force = 0;

    //make arrays corresponding to the coords of each node
    double[] xPos = subnet.getSubsetArray(slice.getXCoords());
    double[] yPos = subnet.getSubsetArray(slice.getYCoords());


    //set up arcs matrix, (assumed to be similarities)
    //symetrize it
    DoubleMatrix2D arcsMatrix = NetUtils.getSymMaxMatrix(subnet.getMatrix());
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
        if (false) //TODO: this is not used, should either add the option or remove the code
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
      int repaintN = Integer.parseInt(settings.getProperty(ApplySettings.LAYOUT_REPAINT_N));
      if ((repaintN > 0)&& (passes % repaintN == 0))
      {
        //need to pass coords back to slice via subnet's remaping of indexes
        for (int i=0;i<nNodes;i++ )
        {
          slice.setCoords(subnet.getNetIndex(i),xPos[i],yPos[i]);
        }
        if (settings.getProperty(ApplySettings.RECENTER_TRANSFORM).equals(ApplySettings.RECENTER_DURING))
        {
          //just recenters to avg of coords
          LayoutUtils.barycenterLayout(slice, (int)width, (int)height,
        		  Boolean.getBoolean(settings.getProperty(ApplySettings.TRANSFORM_ISOLATE_EXCLUDE)));
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

    //calc the stress WHAT ABOUT ISOLATES?
    //layoutInfo += "stress: "+NetUtils.getStress(slice)+"\n";

    engine.finishLayout(settings,this,slice,width, height);
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
    if (dist > Double.parseDouble(settings.getProperty(REPEL_CUTOFF)))
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
    return "Fructerman-Reingold layout";
  }
  public String getLayoutInfo()
  {
    return layoutInfo;
  }

  /**
   * Kills of the cooling schedule and nulls itself
   */
  public void disposeLayout()
  {
    //need to get rid of layout settigns dialog?
    schedule.hide();
    schedule = null;
  }
  public CoolingSchedule getSchedule(){
		return schedule;
	}
  
}