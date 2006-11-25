package sonia.layouts;

import java.util.*;
import java.lang.Math;

import sonia.LayoutSlice;
import sonia.NetUtils;
import sonia.SoniaController;
import sonia.SoniaLayoutEngine;
import sonia.Subnet;
import sonia.settings.ApplySettings;
import sonia.ui.ApplySettingsDialog;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.*;
import cern.colt.matrix.linalg.SingularValueDecomposition;


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
 * Metric Multi-Dimensional Scaling Layout attempts to position the nodes by
 * doing an eigenvalue calculation on the distance matrix to determine the
 * optimal projection.  See Kruskal.  We are not convinced that our
 * implementation is working correctly, because Skye does not completely
 * understand the matrix algebra behind it. Generally, the eigenvectors corresponding
 * to the two largest eigenvalues are chosen to use as the x and y coords, but
 * this can be changed in the layout paramters.
 * <BR><BR>
 * Parameters:
 * <P>
 * "SVD_XcoordCol" - (name is a misnomer) the index of the eigenvector to use
 * for the x coords (defult is 0)
 * </P><P>
 * "SVD_YcoordCol" - (name is a misnomer) the index of the eigenvector to use
 * for the y coords (defult is 1)
 * </P><P>
 * "addNoise"  - adds the specified ammout of random pertubations to the distance
 * matrix to prevent nodes from overlapping.
 */
public class MetricMDSLayout implements NetLayout, Runnable
{

  private SoniaController control;
  private SoniaLayoutEngine engine;
  private boolean noBreak = true;
  private double width;
  private double height;
  private LayoutSlice slice;
  private ApplySettings settings;
  private String layoutInfo = "";
  
  public static final String SVD_X = "SVD_XcoordCol";
  public static final String SVD_Y = "SVD_YcoordCol";
  public static final String ADD_NOISE = "addNoise";

  public MetricMDSLayout(SoniaController cont, SoniaLayoutEngine eng)
  {
    control = cont;
    engine = eng;
  }

  public void setupLayoutProperties(ApplySettingsDialog settings)
  {
    settings.addLayoutProperty(SVD_X,0);
    settings.addLayoutProperty(SVD_Y,1);
    settings.addLayoutProperty(ADD_NOISE,0);
  }


  public void applyLayoutTo(LayoutSlice s, int w, int h,
                            ApplySettings set)
  {
    slice = s;
    settings = set;
    width = w;
    height = h;


    //threading stuff, to make this run indepent of the displays
    //make a new thread of this
    Thread layoutRunner = new Thread(this,"MDSLayout loop");
    layoutRunner.setPriority(10);
    layoutRunner.start();
  }

  public void run()
  {
    //COMPONENTS!!!

    //get the network of only the nodes present
    Subnet subnet = new Subnet(NetUtils.getMatrix(slice),
                               slice.getPresentNodes());
    // calc constants
    int nNodes = subnet.getNumNodes();
//    optDist = 0.6*Math.sqrt(((width * height) / (nNodes+1)));
   // optDist = settings.getLayoutProperty("optimum dist");

    //make arrays corresponding to the coords of each node
    double[] xPos = subnet.getSubsetArray(slice.getXCoords());
    double[] yPos = subnet.getSubsetArray(slice.getYCoords());
    //arrays which hold the starting coords (could also have loaded them from prev slice)

    //set up arcs matrix, (assumed to be similarities)
    //symetrize it
     SparseDoubleMatrix2D sym = NetUtils.getSymMaxMatrix(subnet.getMatrix());
     //reverse it so that it will be disimilarities
    //using max and min values of all slices is engine
     //and do all pairs shortest path
    DenseDoubleMatrix2D distances = NetUtils.getAllShortPathMatrix(NetUtils.getReverse(sym,
        engine.getMaxMatrixVal(),engine.getMinMatrixValue()));

    //if add noise, add small random values to distance matrix so it won't converge to tightly
    double noiseVal = Double.parseDouble(settings.getProperty(ADD_NOISE));
    if (noiseVal > 0)
    {
      double randVal;
      for (int i=0;i<nNodes ;i++ )
      {
        for (int j=0;j<nNodes ;j++ )
        {
          randVal = control.getUniformRand(-noiseVal,noiseVal);
          distances.set(i,j,distances.get(i,j)+randVal);
        }
      }
    }


    nNodes = distances.rows();
    double[] rVect = new double[nNodes];
    double[] cVect = new double[nNodes];
    double m = 0;
    //loop over all i,j, and square
    for (int i=0;i<nNodes ;i++ )
    {
      for (int j=0;j<nNodes ;j++ )
      {
        //square matrix entries
        distances.setQuick(i,j,distances.getQuick(i,j)*distances.getQuick(i,j));
        //compute row sums
        rVect[i] += distances.getQuick(i,j);
        //compute col sums
        cVect[j] += distances.getQuick(i,j);
        m += distances.getQuick(i,j);
      }
    }

//divide row and colums sums by n
    for (int i = 0;i<nNodes ;i++ )
    {
      rVect[i] = rVect[i]/(double)nNodes;
      cVect[i] = cVect[i]/(double)nNodes;
    }

//divide m by the swuare of number of nodes
     m = m/((double)nNodes * (double)nNodes);

     //subtract row and col sums and add m to each entry
    for (int i=0;i<nNodes ;i++ )
    {
      for (int j=0;j<nNodes ;j++ )
      {
        distances.setQuick(i,j,(-0.5*(distances.getQuick(i,j)-rVect[i]-cVect[j]+m)));
       }
    }

//do the svd
    SingularValueDecomposition SVD = new SingularValueDecomposition(distances);
    int rank = SVD.rank();
    double [] eVect = SVD.getSingularValues();
    for (int j=0;j<rank ;j++ )
    {
      for (int i=0;i<nNodes ;i++ )
      {
        distances.setQuick(i,j,(distances.getQuick(i,j)* Math.sqrt(eVect[j])));
       }
    }
//apprently distances now has the coordinates for points  as rows whwere cols are dims,
    //so pick first two?
    int xCol = (int)Math.round(Double.parseDouble(settings.getProperty(SVD_X)));
    int yCol = (int)Math.round(Double.parseDouble(settings.getProperty(SVD_Y)));
    xPos = distances.viewColumn(xCol).toArray();
    yPos = distances.viewColumn(yCol).toArray();

    //need to pass coords back to slice via subnet's remaping of indexes
    for (int i=0;i<nNodes;i++ )
    {
      slice.setCoords(subnet.getNetIndex(i),xPos[i],yPos[i]);
    }



      for (int i =0; i<nNodes; i++)
      {
        slice.setCoords(i,xPos[i],yPos[i]);
      }
      engine.finishLayout(settings,this,slice,width, height);

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
    return "MetricMDS (SVD)";
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