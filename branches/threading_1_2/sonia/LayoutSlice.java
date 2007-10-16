package sonia;

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

import java.util.*;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.list.IntArrayList;



public class LayoutSlice
{

  private double sliceStart;
  private double sliceEnd;
  private int maxNumNodes;
  private double[] xCoords; //id same as index (0 -> 1 offset problem)
  private double[] yCoords;
  private SparseDoubleMatrix2D arcsMatrix; //holds links
  private SparseDoubleMatrix2D arcCountMatrix; //holds number of ij ties (would be nice if it was int..)
  private IntArrayList presentNodes; //idexesof all the nodes present
  private ArrayList nodeEvents;  // ref for acessing the orignial events
  private boolean layoutFinished = true; //flag for concurency check
  private boolean hasError;  //flag for saying there are layout problems

  private StabilityEstimate stability;

  //PROBLEM IF COORDS ARE CHANGED BY ATTRIBUTE WITHEN LAYOUT SLICE
  //perhaps allowing attributes to change coords means using a differnt layout
  //(static algorithm)?


  /**
   * each LayoutSlice is a bin holding all of the events that are to be
   * included within the same layout.  Also includes functions that return
   * information about the slice.
   * @param maxNodes the total number of nodes possible (size of matrix)
   * @param startTime the start time for the slice (inclusive)
   * @param endTime the end time for the slice (exclusive)
   */
  public LayoutSlice(int maxNodes, double startTime, double endTime)
  {
    sliceStart = startTime;
    sliceEnd = endTime;
    maxNumNodes=maxNodes;
    arcsMatrix = new SparseDoubleMatrix2D(maxNumNodes,maxNumNodes);
    arcCountMatrix = new SparseDoubleMatrix2D(maxNumNodes,maxNumNodes);
    presentNodes = new IntArrayList();
    nodeEvents = new ArrayList();
    xCoords = new double[maxNumNodes];
    yCoords = new double[maxNumNodes];
    //fill arrays with 0.0s just in case
    for(int n=0; n<maxNumNodes;n++)
    {
      xCoords[n] = 0;
      yCoords[n] = 0;
    }
  }

  //puts appropriate entires in arcsMatrix
  //ID'S ARE OFFEST

  /**
   * Adds an arc to the slice, summing the passed weight with the weight already
   * listed for that relation.  From and to ids are ids, not index (so they are
   * decremented by 1 internaly to give matrix index. Also increments the
   * count matrix which keeps track of how many ties there are per relation.
   * @param fromIds the id (NOT index) of the originating node
   * @param toId the id (NOT index) of the destination node
   * @param weight the wight of the relation
   */
  public void addArc(int fromId, int toId,double weight)
  {
    //ADDS TO VALUE ALREADY IN MATRIX
    weight += arcsMatrix.getQuick(fromId-1,toId-1);
    arcsMatrix.setQuick(fromId-1,toId-1, weight);
    arcCountMatrix.setQuick(fromId-1,toId-1, arcCountMatrix.getQuick(fromId-1,toId-1)+1.0);
  }

  /**
   * Sets the stored coordinates for the node at index to the passed values.
   * NOTE: index is the array index, not the id
   * @param index the index for the node position being set
   * @param x the x coordinate to set
   * @param y the y coordinate to set
   *
   */
  public void setCoords(int index, double x, double y)
  {
    //check the range on the coords to make sure they won't give the windows
    //render engine fits
    if (Double.isNaN(x))
    {
      x = 0.0;
    }
    if (Double.isNaN(y))
    {
      y = 0.0;
    }
    xCoords[index] = x;
    yCoords[index] = y;
  }


  /**
   * Returns a list of indecies cooresponding to the nodes with no in or out
   * connections
   * @returns an IntArrayList, each element of which is the index for an isolate
   */
  public IntArrayList getIsolates()
  {
    //NEED TO CHECK THIS
    //add isolates to isolate list
     IntArrayList isolates = new IntArrayList();
     for (int n=0;n<presentNodes.size();n++)
     {
       //check if node is isolate in this slice, and add to list
       //SELF LOOPS PROBLEM!!
       int index = presentNodes.get(n);
       if ((inDegree(index) == 0)
           & (outDegree(index) == 0))
       {
         isolates.add(index);
       }
      }
    return isolates;
  }

  /**
   * Puts the passed index in the list of nodes which are present in this slice.
   * @param index the index of the node to be added to the present list
   */
  public void addPresentNode(int index)
  {
    presentNodes.add(index);
  }

/**
* returns a list containing the indcies of all the nodes present in the slice
* @returns IntArrayList containing an index for each present node. WARNING: does
* not return a copy, so modifying the list will mare nodes as absent.
*/
  public IntArrayList getPresentNodes()
  {
    return presentNodes;
  }

  /**
   * Adds the passed node event to the collection of node events in this slice.
   * @param node the node to add to the slice
   */
  public void addNodeEvent(NodeAttribute node)
  {
    nodeEvents.add(node);
  }

  /**
   * Returns a node event corresponding to the passed index.  NOTE: depending on
   * slice size, there may be more than one node event with the same index,
   * no guarentees are made as to which event will be returned.  Will return
   * null if index is out of range
   */
  public NodeAttribute getNodeEvent(int index)
  {
    NodeAttribute node = null;
    for (int i =0; i<nodeEvents.size();i++)
    {
      NodeAttribute tryNode = (NodeAttribute)nodeEvents.get(i);
      if (tryNode.getNodeId()-1 == index)
      {
        node = tryNode;
      }
    }
    return node;
  }

  //returns the number of non-zero entries on row
  //NEGITIVE RELATIONS?

  /**
   * returns the outdegree of the node with the corresponding index.  In otherwords,
   * the number of non-zero entries on the arcs matrix for that row.  Ignores
   * multiplse ties.  NEGITIVE RELATIONS? SELF LOOPS?
   * @param the index of the node to get the outdegree for
   * @returns the outdegree of the node
   */
  public int outDegree(int index)
  {
    int outdegree = 0;
    for (int i = 0; i<maxNumNodes;i++)
    {
      if (arcsMatrix.getQuick(index,i) != 0)
      {
        outdegree++;
      }
    }
    return outdegree;
  }

  /**
   * returns the indegree of the node with the corresponding index.  In otherwords,
   * the number of non-zero entries on the arcs matrix for that column.  Ignores
   * multiplse ties.  NEGITIVE RELATIONS? SELF LOOPS?
   * @param the index of the node to get the indegree for
   * @returns the indegree of the node
   */
  public int inDegree(int index)
  {
    int indegree = 0;
    for (int i = 0; i<maxNumNodes;i++)
    {
      if (arcsMatrix.getQuick(i,index) != 0.0)
      {
        indegree++;
      }
    }
    return indegree;
  }

  /**
   *  returns the number of entries that are not null or zero in the arcs matrix
   */
  public int getTotalSliceArcs()
  {
    int arcCount = 0;
    //loop over all the entries in the matrix
    for (int i=0;i<maxNumNodes;i++)
    {
      for (int j=0;j<maxNumNodes;j++)
      {
        if ( arcsMatrix.getQuick(i,j) != 0.0 )
        {
          arcCount++;
        }
      }
    }
    return arcCount;
  }

  /**
   * returns the number of arcsEvents relating i and j.  Not the same as the
   * number of entries in the arcsMatrix!
   */
  public double getArcCount(int i, int j)
  {
    return arcCountMatrix.getQuick(i,j);
  }

  //BY INDEX, NOT BY ID
  /**
   * returns the weight of the relation between i and j (the value of the arcs
   * matrix)
   * @param iIndex the index of the origin node
   * @param jIndex the index of the destination node
   * @returns the weight of the relation
   */
  public double getArcWeight(int iIndex, int jIndex)
  {
    return arcsMatrix.getQuick(iIndex,jIndex);
  }

  /**
   * Sets the i,jth entry in the arc's matrix, replacing its value. Should only
   *  be called by  NetDataStructure During Setup!!  Otherwise use AddArc.
   * @param iIndex the index of the origin node
   * @param jIndex the index of the destination node
   * @param weight the weight to set it to
   */
  public void setArcWeight(int iIndex, int jIndex, double weight)
  {
    arcsMatrix.setQuick(iIndex,jIndex,weight);
  }

  /**
   * return2 the average of the i -> j  and j -> i tie
   */
  public double getSymAvgArcWeight(int iIndex, int jIndex)
  {

    double val = (arcsMatrix.get(iIndex,jIndex)+arcsMatrix.get(jIndex,iIndex))/2;
    return val;
  }

  /**
   * returns the max of the i -> j  and j -> i ties
   */
  public double getSymMaxArcWeight(int iIndex, int jIndex)
  {
    double val = Math.max(arcsMatrix.get(iIndex,jIndex),arcsMatrix.get(jIndex,iIndex));
    return val;
  }

  /**
   * returns the min of the i -> j  and j -> i ties
   */
  public double getSymMinArcWeight(int iIndex, int jIndex)
  {
    double val = Math.min(arcsMatrix.get(iIndex,jIndex),arcsMatrix.get(jIndex,iIndex));
    return val;
  }

  /**
   * returns an array of weights corresponding to the out relations of the node
   * @param rowIndex the index of the node to get row for
   * @returns row of the arcs matrix
   */
  public double[] getRow(int rowIndex)
  {
    return arcsMatrix.viewRow(rowIndex).toArray();
  }

  //accessors----------

  /**
   * checks flag to see if layout is in use by an algorithm
   */
  public boolean isLayoutFinished()
  {
    return layoutFinished;
  }

  /**
   * sets flag so that other layouts won't access this slice at the same time
   */
  public void setLayoutFinished(boolean value)
  {
    layoutFinished = value;
    if ((value == true) & (stability != null))
    {
      stability.layoutComplete(xCoords,yCoords);
    }
  }

  /**
   * set flag to say that there are problems with the layout
   */
  public void setError(boolean value)
  {
    hasError = value;
  }

  /**
   * returns flag indicating if layout has an error
   */
  public boolean isError()
  {
    return hasError;
  }


  public double getSliceStart()
  {
    return sliceStart;
  }
  public void setSliceStart(double startTime)
  {
    sliceStart = startTime;
  }
  public double getSliceEnd()
  {
    return sliceEnd;
  }
  public void setSliceEnd(double endTime)
  {
    sliceEnd = endTime;
  }
  public double[] getXCoords()
  {
    return xCoords;
  }
  public double[] getYCoords()
  {
    return yCoords;
  }
  public int getMaxNumNodes()
  {
    return maxNumNodes;
  }
  public void addStabilityEstimate(StabilityEstimate stable)
  {
    stability = stable;
  }

  public void removeStabilityEstimate()
  {
    if (stability != null)
    {
      stability = null;
    }
  }

}