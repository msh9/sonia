package sonia;

import java.util.*;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntIntHashMap;

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
 * Subnet contains a matrix describing a subset of the relationships of the
 * overall network, and remaps its indicies to those for the original net
 */
public class Subnet
{
  private SparseDoubleMatrix2D subnet;
  private OpenIntIntHashMap remap;
  private int nNodes;

  //takes the original net and a list of the nodes to include in the subnet
  public Subnet(SparseDoubleMatrix2D net, IntArrayList includeNodes)
  {
    nNodes = includeNodes.size();
    //construct the maping of subnet-to-net indcies
    subnet = NetUtils.getSubnetMatrix(net,includeNodes);
    remap = new OpenIntIntHashMap(nNodes);
    for( int i=0;i<nNodes;i++)
    {
      remap.put(i,includeNodes.get(i));
    }
  }

  //returns the index of the node in the original (big) network
  public int getNetIndex(int subIndex)
  {
    return remap.get(subIndex);
  }

  //returns the list of all the origina (big net) indcies present
  public IntArrayList getNetIndexList()
  {
    return remap.values();
  }

  //returns a shorter array in which contains the appropriate values of the big array
  public double[] getSubsetArray(double[] sliceArray)
  {
    double[] subnetArray = new double[nNodes];
    for (int i = 0; i <nNodes; i++)
    {
      //coppy the correct values from the slice-sized array
      subnetArray[i] = sliceArray[remap.get(i)];
    }
    return subnetArray;
  }

  public int getNumNodes()
  {
    return subnet.rows();
  }

  //returns the internal matrix
  public SparseDoubleMatrix2D getMatrix()
  {
    //is this dangerous, will it get modified later?
    return subnet;
  }
  //returns the value stored at given subnet indicies
  public double get(int subIndexI, int subIndexJ)
  {
    //does not check ranges or preconditions
    return subnet.getQuick(subIndexI,subIndexJ);
  }
  public String toString()
  {
    String str = remap.values().toString()+"\n";
    str += subnet.toString();
    return str;
  }
}