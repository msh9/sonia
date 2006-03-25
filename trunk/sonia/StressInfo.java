package sonia;

import java.awt.*;
import java.awt.event.*;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.list.DoubleArrayList;
import java.awt.Graphics2D;
import java.util.*;
import java.text.NumberFormat;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


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
 *   The stress measure in SoNIA essentially an adaptation from Kruskal
 * Multidimensional Scaling stress measure.  [I'm not quite sure of the
 * correct cite, since it is the Sage reprint of his 1978 Bell Labs
 * paper]  The basic idea is to report on the degree of distortion
 * present in the two-dimesional screen representation of the
 * (potentially n-1 dimensional) dissimilarity matrix distances.  The
 * measure was adapted for SoNIA to deal with the fact that the original
 * socio-matrix is in the form of similarities, only contains information
 * on the relations between directly connected nodes, may contain
 * multiple components, and the screen distances are in a differnt set of
 * units/scaling than the matrix.
*</P><P>
 * To calculate the stress, we first break the network into its
 * components and symetrize with Max.  We then convert resulting
 * similarities matricies dissimilarities using Dij =  1/Sij * max_value,
 *   where max value is the largest value in any cell of any slice in the
 * complete time network.  An all-pairs-shortest-path algorithm is used
 * to compute the length of the shortest path between each ij pair,
 * resulting in matrix which contains a defined "distance" value for each
 * n-step pair.  These matrixDistances are compared to the
 * screenDistances (2D euclidian distances of the coordinates of the
 * nodes in the layout) according to Kruskal's "f-stress" eqn:
 * </P><P>
 * stress = sqrt(  Sum[i][j]( (screenDist - matrixDist)^2 ) /
 * Sum[i][j](matrixDist^2) )
* </P><P>
* However, unlike Kruskal's MDS example, the distances we are comparing
 * to not have the same units or scaling.  Even in a "perfect" layout,
 * the screen distances will be real values which correspond closely to
 * pixel distances, and are generally considerably larger than the matrix
 * distances, by some unknown scale factor.  (note: this scale factor
 * should be close to the "optDist" value, but probably won't be exact)
 * We cannot directly compare the screen and matrix values to compute the
 * scale factor, because the values are subject to an unkown degree of
 * distortion (the quantity we are trying to estimate).  So we assume
 * that overall sum of matrix distances and screen distances should be
 * roughly proportional, and multiply each screen distances value by
 * TotalMatrixDistance / TotalSreenDistance.   Finally, we pool all the
 * ij's from each component before calculating the stress statistic.  For
 * a fully connected network, this is irrelevent, for a network with
 * multiple components, it is like computing the stress while ignoring
 * the "infinite distance" connections between components.
* </P><P>
 * To graphically display the information, we plot the matrix distances
 * against the (rescaled) screen distances in a Shepard's plot,
 * indicating the diagonal line of equality with a white line.  The
 * stress value can be interpreted as the degree of scatter from
 * equality.    The distances corresponding to the direct relations are
 * shown in darker blue.  Because some algorithms (FR) are not actually
 * attempting to optimize the all-pairs-shortest-path distances (FR works
 * only with direct connetions) its may not be completely appropriate to
 * judge the performance with a stress statistic which takes the indirect
 * values into account.  (this is espcially true for PI and SVD MDS, as
 * they do not aim for screen distances proportional to edge weights).
 * </P>
 */

public class StressInfo extends JFrame implements WindowListener
{
  private SoniaController control;
  private SoniaLayoutEngine engine;
  //private Canvas plotArea;
  private JLabel plotArea = new StressPlot();
  private JPanel mainPanel = new JPanel(new BorderLayout());
  private JLabel stressVale = new JLabel("Stress:");
  private int pad = 20;

  private NumberFormat  formater;

  private int xPlotRange;
  private int xPlotMax;
  private int xPlotMin;

  private int yPlotRange;
  private int yPlotMax;
  private int yPlotMin;

  private double xDataRange;
  private double xDataTotal;
  private double xDataMax;
  private double xDataMin;
  private double yDataRange;
  private double yDataMax;
  private double yDataMin;
  private double yDataTotal;

  private int nPoints = 0;
  private DoubleArrayList layDist;   //distances from the layotu
  private DoubleArrayList matDist;   //distance from the matrix
  private ArrayList colorList;  //colors for each of the points


  public StressInfo(SoniaController cont, SoniaLayoutEngine eng)
  {
    control = cont;
    engine = eng;
    plotArea.setFont(control.getFont());
   // plotArea = new Canvas();
//    GridBagLayout layout = new GridBagLayout();
//    setLayout(layout);
//    GridBagConstraints c = new GridBagConstraints();
//    c.gridx=0;c.gridy=0;c.gridwidth=5;c.gridheight=5;c.weightx=1;c.weighty=1;
    stressVale.setBackground(Color.darkGray);
    plotArea.setBackground(Color.darkGray);
    mainPanel.add(plotArea,BorderLayout.CENTER);
    mainPanel.add(stressVale,BorderLayout.SOUTH);
     add(mainPanel);

    //number formating
    formater = NumberFormat.getInstance(Locale.ENGLISH);
    formater.setMaximumFractionDigits(3);
    formater.setMinimumFractionDigits(3);;

    addWindowListener(this);
    setTitle("Shepard's Plot "+engine.toString());
    setBackground(Color.lightGray);
    setSize(250,250);
    setLocation(100,250);
  }

  public void shepardPlot(LayoutSlice slice)
  {
    //reset globals
    xDataRange = 0;
    xDataTotal = 0;
    xDataMax = Double.NEGATIVE_INFINITY;
    xDataMin = Double.POSITIVE_INFINITY;
    yDataRange = 0;
    yDataMax = Double.NEGATIVE_INFINITY;
    yDataMin = Double.POSITIVE_INFINITY;
    yDataTotal = 0;
    //getCurrent layout coords from engine NOT THE SAME INDICIES AS SUBNET COORDS
    double[] layX = engine.getCurrentXCoords();
    double[] layY = engine.getCurrentYCoords();
    //lists to hold the data points
    layDist = new DoubleArrayList();
    matDist = new DoubleArrayList();
    colorList = new ArrayList();

    //need to run seperately on each component!
    ArrayList components = NetUtils.getComponents(NetUtils.getSymMaxMatrix(slice),true);
    Iterator compIter = components.iterator();
    while (compIter.hasNext())
    {
      Subnet subnet = (Subnet)compIter.next();
      if (subnet.getNumNodes() > 1)  //ignore isolates
      {
        //the raw similarities matrix
        SparseDoubleMatrix2D sim = subnet.getMatrix();
        //the all-pairs-shortest path distances from disimilaritise
        DenseDoubleMatrix2D pathDist =
            NetUtils.getAllShortPathMatrix(NetUtils.getReverse(subnet.getMatrix(),
            engine.getMaxMatrixVal(),engine.getMinMatrixValue()));
        //debug
        //System.out.println(distances.toString());

        nPoints = pathDist.rows();

        for (int i=0;i<nPoints ;i++ )
        {
          int subIndexI = subnet.getNetIndex(i);
          for (int j=i+1;j<nPoints ;j++ ) //only loops over upper triangel of matrix
          {
            int subIndexJ = subnet.getNetIndex(j);
            //compute the distance on the layout from coords
            double layoutDist = Math.sqrt((layX[subIndexI]-layX[subIndexJ])*(layX[subIndexI]-layX[subIndexJ]) +
                (layY[subIndexI] -layY[subIndexJ])*(layY[subIndexI] -layY[subIndexJ]));
            xDataTotal += layoutDist;

            //get the "desired" distance from the matrix
            double matrixDist = pathDist.getQuick(i,j);
            yDataTotal += matrixDist;

            //color the points depending on if path or direct distance
            if (sim.getQuick(i,j) != 0)
            {
              colorList.add(Color.blue);
            }
            else
            {
              colorList.add(Color.cyan);
            }

            //put the values on the list of points
            layDist.add(layoutDist);
            matDist.add(matrixDist);
          }
        }
      }
    }//end of component loop

    //rescale the distances to make roughly square
   double scaleFactor = yDataTotal/xDataTotal;
    for (int n=0;n<layDist.size() ;n++ )
    {
    layDist.set(n,layDist.get(n)*scaleFactor);

      xDataMax = Math.max(xDataMax,layDist.get(n));
      xDataMin = Math.min(xDataMin, layDist.get(n));
      yDataMax = Math.max(yDataMax, matDist.get(n));
      yDataMin = Math.min(yDataMin,matDist.get(n));
    }

    xDataRange = xDataMax;
    yDataRange  = yDataMax;
    //show the window if hidden
    //this.setVisible(true);
    repaint();
  }

  private double calcStress()
  {
    double stressSum = 0;
    double denomSum = 0;
    //loop over all the direct ties to calc stress
    if (layDist != null)
    {
      for (int n=0;n<layDist.size() ;n++ )
      {
        stressSum+= (layDist.get(n)-matDist.get(n))*(layDist.get(n)-matDist.get(n));
        denomSum += matDist.get(n)*matDist.get(n);
      }
    }
    return Math.sqrt(stressSum/denomSum);

  }

  private double calcOneStepStress()
  {
    double stressSum = 0;
    double denomSum = 0;
    //loop over all the direct ties to calc stress
    if (layDist != null)
    {
      for (int n=0;n<layDist.size() ;n++ )
      {
        //color list indicates if it was one step or not, so only count the blue ones
        if (((Color)colorList.get(n)).equals(Color.blue))
        {
        stressSum+= (layDist.get(n)-matDist.get(n))*(layDist.get(n)-matDist.get(n));
        denomSum += matDist.get(n)*matDist.get(n);
        }
      }
    }
    return Math.sqrt(stressSum/denomSum);
  }

  public double calcAllSliceAvg()
  {
    int numSlices = engine.getNumSlices();
    double avg = 0.0;
    return avg;
  }

class StressPlot extends JLabel
{
  public void paintComponent(Graphics graphics)
  {
    int titleBarPad = (this.getInsets()).top;
    Graphics2D g = (Graphics2D)graphics;
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                       RenderingHints.VALUE_ANTIALIAS_ON);
    //calc the screen dimensions for the plot
    xPlotMax = plotArea.getWidth() - pad;
    xPlotMin = pad;
    xPlotRange = xPlotMax - xPlotMin;
    yPlotMax = plotArea.getHeight()-pad;
    yPlotMin = plotArea.getInsets().top+pad;
    yPlotRange = yPlotMax-yPlotMin;

    double xScale = xPlotRange / xDataRange;
    double yScale = yPlotRange / yDataRange;

    //draw axes
    g.setColor(Color.darkGray);
    g.setStroke(new BasicStroke(1.5f));
    //x axais
    g.drawLine(xPlotMin,yPlotMax,xPlotMax,yPlotMax);
    g.drawString("layout distance (rescaled)",xPlotMax/3,yPlotMax+12);
    g.drawString(""+xDataMax,xPlotMax-20,yPlotMax+12);
    g.drawString(""+0,xPlotMin,yPlotMax+12);
    //y axis
    g.drawLine(xPlotMin,yPlotMin,xPlotMin,yPlotMax);
    g.drawString("dij",1,yPlotMax/2);
    g.drawString(""+yDataMax,1,yPlotMin);
    g.drawString(""+0,1,yPlotMax);

    //line of equality
    g.setColor(Color.white);
    g.drawLine(xPlotMin,yPlotMax,xPlotMin + (int)Math.round(xDataMax * xScale),
               yPlotMax - (int)Math.round(xDataMax * yScale));

    //stressValue
//    g.setColor(Color.blue);
//    g.drawString("Stlice "+engine.getCurrentSliceNum()+" Stress: "
//                 +formater.format(calcStress()),titleBarPad+10,32);
    stressVale.setText("Slice "+engine.getCurrentSliceNum()+" Stress: "
                 +formater.format(calcStress()));

    //plot the points

    if (layDist != null)
    {
      for (int i = 0;i<layDist.size() ;i++ )
      {
        g.setColor((Color)colorList.get(i));
        int x = xPlotMin + (int)Math.round(layDist.get(i) * xScale);
        int y = yPlotMax - (int)Math.round(matDist.get(i) * yScale);
        g.fillRect(x-1,y-1,3,3);
      }
    }

  }
}

  //window listeners-------

  public void windowClosing (WindowEvent evt)
  {
    engine.disposeStressPlot();
    this.dispose();
  }

  public void windowActivated(WindowEvent evt){}
  public void windowClosed(WindowEvent evt){}
  public void windowDeactivated(WindowEvent evt){}
  public void windowDeiconified(WindowEvent evt){}
  public void windowIconified(WindowEvent evt){}
  public void windowOpened(WindowEvent evt){}

}