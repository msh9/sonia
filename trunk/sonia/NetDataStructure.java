package sonia;

import java.util.*;

import sonia.mapper.Colormapper;
import sonia.parsers.DotNetParser;

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

/*
 * Holds the time and space coordinates and attributes for the nodes and edges
 * as they were read from input files.  Provides methods for accessing
 * attributes, generating slices, and retreive nodes and edges by time to
 * generate layout slices.  Probably will eventually become an interface, so
 * that SoNIA can frontend for SQL database.
 */
public class NetDataStructure
{
  private SoniaController control;
  private String netInfo;
  private int maxNetSize;
  private int numNodeEvents;
  private int numArcEvents;
  private double firstTime = Double.POSITIVE_INFINITY;
  private double lastTime = Double.NEGATIVE_INFINITY;
  private DotNetParser parser;  //should be Parser interface

  //aggregation types
  public static int SUM_TIE_VALS = 0;
  public static int AVG_TIE_VALS = 1;
  public static int NUM_TIES = 2;  //should also include max and min?
  
  private HashSet<String> nodeDataKeys; //key strings for user data attached to nodes

  //this array has an entry for every time an observation was made or an
  //attribute changed so it is >> than the number of nodes
  private NodeAttribute[] nodeEventArray;
  //likewise for edges
  private ArcAttribute[] arcEventArray;
  
  private NodeClusterAttribute[] nodeClusterArray = null;


  /**
   * Holds the time and space coordinates and attributes for the nodes and edges
   * as they were read from input files.  Provides methods for accessing
   * attributes, generating slices, and retriving nodes and edges by time to
   * generate layout slices.
   * @param controller the main soniaController
   * @param fileName the name of the original text file
   * @param nodeEvent the number of node events
   * @param arcEvent the number of arc events
   * @param netSize the number of unique nodes
   */
  public NetDataStructure(SoniaController controller, String fileName,
                          int nodeEvent, int arcEvent,int netSize)
  {
    control = controller;
    numNodeEvents = nodeEvent;
    numArcEvents = arcEvent;
    //create arrays of correct size to hold events
    nodeEventArray = new NodeAttribute[numNodeEvents];
    arcEventArray = new ArcAttribute[numArcEvents];
    maxNetSize = netSize;
  }

  /**
  * creates layoutSlices (bins for events) all in one go and sorts events into
  * them.
  * @param startTime the beginning of the first slice
  * @param endTime the end of the last slice
  * @param sliceDuration  how "thick" the slice is
  * @param sliceDelta how far apart the slices are, (slices can overlap)
  */
  public ArrayList<LayoutSlice> getLayoutSlices(double startTime, double endTime,
                                double sliceDuration, double sliceDelta,
    int aggregateType)
  {
    control.showStatus("Getting layout slices...");
    ArrayList<LayoutSlice> layoutSlices = new ArrayList<LayoutSlice>();
    //figure out how many slices there will be
    //equal to the total time divided by slice delta, rounded up
    //(last slice my be funny) ASSUMES startTime < endTime
    int numSlices = (int)Math.ceil((endTime - startTime) / sliceDelta);
    //make slices with correct start and end times
    double sliceStart = startTime;
    double sliceEnd = sliceStart + sliceDuration;
    for (int s = 0; s<numSlices; s++)
    {
      LayoutSlice currentSlice = makeLayoutSlice(sliceStart,sliceEnd, aggregateType);
      layoutSlices.add(currentSlice);
      sliceStart += sliceDelta;
      sliceEnd += sliceDelta;
    }

    //SHOULD OUTPUT TO LOG
    control.log("Divided "+numNodeEvents+" node events and "+numArcEvents+
                " arc events into "+numSlices+" slices\n"+
                "starting at time "+startTime+"\n"+
                "ending at time "+endTime+"\n"+
                "slice duration "+sliceDuration+"\n"+
                "slice delta "+sliceDelta+"\n"+
                "aggregation type "+aggregateType );
    return layoutSlices;
  }

  /**
   * creates a layout slice and loops over all the events, including them if
   * they land within the slice.  Does the aggregation to create the slice matrix.
   * Assumes node will be present, will draw arc
   * even if it isn't.
   * if delta < duration, some objects will be in more than one slice<BR>
   * if delta > duration, some objects will not be in a slice (warn user?)<BR>
   * will exclude objects that started before beginning of layout slices<BR><BR>
   *
   * For Arcs:<BR>
   * include arc in bin if obsTime is within interval (will include relations
   * of lenth shorter than interval) if obsTime before start AND endTime after or equal to
   * end of interval<BR><BR>
   *
   * For Nodes: <BR>
   * include node in bin if obsTime is within interval (inclusive) (will include
   * relations of lenth shorter than interval) if obsTime before start AND
   * endTime after or equal to end of interval
   */
  public LayoutSlice makeLayoutSlice(double sliceStart, double sliceEnd,
                                     int aggregateType)
  {
	  
    LayoutSlice currentSlice = new LayoutSlice(maxNetSize,sliceStart,sliceEnd);
     //loop over all ARCS and sort to correct slice
     //ISOLATES!!
     //assumes node will be present, will draw arc even if it isn't
     //if delta < duration, some objects will be in more than one slice
     //if delta > duration, some objects will not be in a slice (warn user?)
     //will exclude objects that started before beginning of layout slices
     for (int i=0;i<numArcEvents;i++)
     {
       double obsTime = arcEventArray[i].getObsTime();
       double end = arcEventArray[i].getEndTime();

       //include arc in bin if obsTime is within interval
       // (will include relations of lenth shorter than interval)
       //if obsTime before start AND endTime after end of interval
       if(((obsTime >= sliceStart) & (obsTime < sliceEnd))
          | ((obsTime <= sliceStart) & (end >= sliceEnd)))
       {
         int fromId = arcEventArray[i].getFromNodeId();
         int toId = arcEventArray[i].getToNodeId();
         double weight = arcEventArray[i].getArcWeight();
         currentSlice.addArc(fromId,toId,weight);
       }
     }

     //loop over nodes to get coordinates and isolates
     for (int i=0;i<numNodeEvents;i++)
     {
       double obsTime = nodeEventArray[i].getObsTime();
       double end = nodeEventArray[i].getEndTime();
       

       //include node in bin if obsTime is within interval (inclusive)
       // (will include relations of lenth shorter than interval)
       //if obsTime before start AND endTime after end of interval
       if(((obsTime >= sliceStart) & (obsTime < sliceEnd))
          | ((obsTime <= sliceStart) & (end >= sliceEnd)))
       {
         int id = nodeEventArray[i].getNodeId();
         double x = nodeEventArray[i].getObsXCoord();
         double y = nodeEventArray[i].getObsYCoord();
         currentSlice.setCoords(id-1,x,y);
         currentSlice.addPresentNode(id-1);  //these are kind of redundant now...
         currentSlice.addNodeEvent(nodeEventArray[i]);
       }
      }

      //do aggregation, if necessary
      //NEED TO CHANGE HOW THIS IS DONE IF WE ADD MAX OR MIN
      if (aggregateType != SUM_TIE_VALS)
      {
        for (int i = 0; i<maxNetSize; i++)
        {
        	
          for (int j = 0; j<maxNetSize; j++)
          {
            if (aggregateType == AVG_TIE_VALS)
            {
              double count = currentSlice.getArcCount(i,j);
              if (count > 0)
               {
                double value = currentSlice.getArcWeight(i,j)/count;
                currentSlice.setArcWeight(i,j,value);
              }
            }
            else if (aggregateType == NUM_TIES)
            {
              currentSlice.setArcWeight(i,j,currentSlice.getArcCount(i,j));
            }
          }
        }
      }
      return currentSlice;
  }

  public RenderSlice fillRenderSlice(RenderSlice slice)
  {
	  //TODO: build time indexing structure to speed up slice filling
    double sliceStart = slice.getSliceStart();
    double sliceEnd = slice.getSliceEnd();
    //loop over all nodes to see which should be in this slice
    for (int i=0;i<numNodeEvents;i++)
     {
       double obsTime = nodeEventArray[i].getObsTime();
       double end = nodeEventArray[i].getEndTime();
       //include node in bin if obsTime is within interval (inclusive)
       // (will include relations of lenth shorter than interval)
       //if obsTime before start AND endTime after end of interval
       if(((obsTime >= sliceStart) & (obsTime < sliceEnd))
         | ((obsTime <= sliceStart) & (end >= sliceEnd)))
       {
         slice.addNodeEvent(nodeEventArray[i]);
       }
      }

      //loop over the arcs
      for (int i=0;i<numArcEvents;i++)
     {
       double obsTime = arcEventArray[i].getObsTime();
       double end = arcEventArray[i].getEndTime();
       //include arc in bin if obsTime is within interval (inclusive)
       // (will include relations of lenth shorter than interval)
       //if obsTime before start AND endTime after end of interval
       if(((obsTime >= sliceStart) & (obsTime < sliceEnd))
           | ((obsTime <= sliceStart) & (end >= sliceEnd)))
       {
         slice.addArcEvent(arcEventArray[i]);
       }
      }
      //do clusters, if there are any
      if (nodeClusterArray != null){
    	  for (int i = 0; i < nodeClusterArray.length; i++) {
    		  double obsTime = nodeClusterArray[i].getObsTime();
    	       double end = nodeClusterArray[i].getEndTime();
    	       //include arc in bin if obsTime is within interval (inclusive)
    	       // (will include relations of lenth shorter than interval)
    	       //if obsTime before start AND endTime after end of interval
    	       if(((obsTime >= sliceStart) & (obsTime < sliceEnd))
    	           | ((obsTime <= sliceStart) & (end >= sliceEnd)))
    	       {
    	         slice.addClusterEvent(nodeClusterArray[i]);
    	       }
		}
      }
      return slice;
  }

  //moves events from passed vector (from parseing) into array
  public void addNodeEvents(Vector<NodeAttribute> nodeEvents)
  {
    //check that they will fit
    if (nodeEvents.size() == numNodeEvents)
    {
      for (int i = 0; i< numNodeEvents; i++)
      {
        nodeEventArray[i] = (NodeAttribute)nodeEvents.get(i);
        firstTime = Math.min(nodeEventArray[i].getObsTime(),firstTime);
        lastTime = Math.max(nodeEventArray[i].getEndTime(),lastTime);
      }
    }
    else
    {
      //too big,or too small so throw error
    }
  }

  //moves events from passed vector (from parsing) into array
  public void addArcEvents(Vector<ArcAttribute> arcEvents)
  {
    //check that they will fit
    if (arcEvents.size() == numArcEvents)
    {
      for (int i = 0; i< numArcEvents; i++)
      {
    	
        arcEventArray[i] = (ArcAttribute)arcEvents.get(i);
        //check with times
        firstTime = Math.min(arcEventArray[i].getObsTime(),firstTime);
        lastTime = Math.max(arcEventArray[i].getEndTime(),lastTime);
      }
    }
    else
    {
      //too big,or too small so throw error
      //bug
      control.showError("Wrong number of arc events");
    }
  }
  
  public void addClusterEvents(Vector<NodeClusterAttribute> clusterEvents){
	  nodeClusterArray = new NodeClusterAttribute[clusterEvents.size()];
	  clusterEvents.copyInto(nodeClusterArray);
  }

  /**
   * returns a list containing all the nodes and arcs within the passed time bounds
   */
  public ArrayList<NetworkEvent> getEventsFromTo(double sliceStart, double sliceEnd)
  {
    ArrayList<NetworkEvent> returnList = new ArrayList<NetworkEvent>();
    for (int i=0;i<numNodeEvents;i++)
     {
       double obsTime = nodeEventArray[i].getObsTime();
       double end = nodeEventArray[i].getEndTime();
       //include node in bin if obsTime is within interval (inclusive)
       // (will include relations of lenth shorter than interval)
       //if obsTime before start AND endTime after end of interval
       if(((obsTime >= sliceStart) & (obsTime < sliceEnd))
         | ((obsTime <= sliceStart) & (end > sliceEnd)))
       {
         returnList.add(nodeEventArray[i]);
       }
      }

      //loop over the arcs
      for (int i=0;i<numArcEvents;i++)
     {
       double obsTime = arcEventArray[i].getObsTime();
       double end = arcEventArray[i].getEndTime();
       //include arc in bin if obsTime is within interval (inclusive)
       // (will include relations of lenth shorter than interval)
       //if obsTime before start AND endTime after end of interval
       if(((obsTime >= sliceStart) & (obsTime < sliceEnd))
           | ((obsTime <= sliceStart) & (end > sliceEnd)))
       {
         returnList.add(arcEventArray[i]);
       }
      }
    return returnList;

  }

  //for passing in the complete set after it is assembeld in the parser
  /*
  public void setNodeIdLookup(Hashtable table)
  {
    nodeIdLookup = table;
  }
  */

  public void setMaxNetSize(int size)
  {
    maxNetSize = size;
  }
  
  /*
   * largest number of distinct nodes in the network
   */
  public int getMaxNetSize()
  {
     return(maxNetSize);
  }

  //SHOULD CREATE A PARSER INTERFACE
  public DotNetParser getParser()
  {
    return parser;
  }
  public void setParser(DotNetParser p)
  {
    parser = p;
  }

  //returns the smallest time value
  public double getFirstTime()
  {
    return firstTime;
  }
  //returns the largest time value
  public double getLastTime()
  {
    return lastTime;
  }
  //returns comment string from network if there is one
  public String getNetInfo()
  {
    return netInfo;
  }
  //sets the net info value so that comments can be added from file
  public void setNetInfo(String info)
  {
    netInfo = info;
  }
  
  public Set<Object> getUniqueNodeValues(String key){
	  HashSet<Object> unique = new HashSet<Object>();
	  for (int i=0;i<numNodeEvents;i++)
	     {
	       Object value = nodeEventArray[i].getData(key);
	       unique.add(value);
	      }
	  return unique;
  }
  
  public Set<String> getNodeDataKeys(){
	  if (nodeDataKeys == null){
		  nodeDataKeys = new HashSet<String>();
		  nodeDataKeys.add("<nodes have no attached data>");
	  }
	  return nodeDataKeys;
  }
  
  public void setNodeDataKeys(HashSet<String> keys){
	  nodeDataKeys = keys;
  }
  
  /**
   * this is kind of a hack, should be moved elsewhere
   * @param map
   */
  public void setNodeColormap(Colormapper map){
	  for (int i=0;i<numNodeEvents;i++)
	  {
	       nodeEventArray[i].setColormap(map);
	  }
  }



}