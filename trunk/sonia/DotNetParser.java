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

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;



/**
* Class for constructing networks from text files in Pajek's *.net Arc/Edge
* list file format. Will not read Pajek Arclist/Edgelist format, or Pajek
* matrix format.  Requires that if "*Arcs" and "*Edges" both exist in file,
* "*Arcs" must be before "*Edges" (this is Pajek's default).
*<BR><BR>
* Pajek is Windows-based freeware, written by Vladimir Batagelj and
* Andrej Mrvar,University of Ljubljana,Slovenia downloadable from:
* http://vlado.fmf.uni-lj.si/pub/networks/pajek/
* <BR><BR>
* currently does not read colors, edge strengths, node sizes, etc..
*
* The .net format is meant to follow Pajek's "time-interval format" to make it
*  possible to share files between the two programs.  The .net format does not allow
*  a great deal of flexibility for working with time (node and arc attributes
* cannot change, for example) but it is relatively simple and includes many
* options for controlling the graphic attributes.  Currently, SoNIA only reads
* a minimal subset of these, so for most situations the .son format is recommended.
* <BR><BR>
* A .net file is divided into two or three sections.  The first, beginning with
* "*Vertices n" describes the nodes and the second and third, beginning with
*  "*Edges" for undirected and "*Arcs" for directed define the ties between the
* nodes.  The header for the Vertices section must give the number of nodes in
* the files (n) and each subsequent line must begin with the integer id for the
* node, one line per node.  Elements of the record are space delimited so if
* names include spaces they must be enclosed in quotes.  Following the id is
* the label, optional x-y-z coordinates , various tokens describing the node
* (all of which, with the exception of color,  are currently ignored by SoNIA),
* and finally the time information enclosed in square brackets [].  Within the
* brackets, "-" is used to divide lower and upper limit of interval, "," is used
* to separate intervals, and "*" means infinity.  Pajek only allows integer times,
* SoNIA will read in decimal times (1.52) from .net files, but this will make the
* file incompatible with Pajek. See the note on the .net concept of time below.
*<BR><BR>
* Example:
* <BR><BR><tt>
* *Vertices 3<BR>
* 1 "a" c blue [5-10,12-14]<BR>
* 2 "b" c red [1-3,7]<BR>
* 3 "e" c green [4-*]<BR>
* *Edges<BR>
* 1 2 1 c gray [7]<BR>
* 1 3 1 [6-8]</tt><BR>
<BR><BR>
* Vertex 'a' is active from times 5 to 10, and 12 to 14.  Vertex 'b' is active
* in times 1 to 3 and in time 7.  Vertex 'e' is active from time 4 on.  Lines
* 1 to 2 are active only in time 7, and lines 1 to 3 are active in times 6 to 8.
* For Arc and Edge records, each line denotes one arc or edge.
*  The first entry gives the id of the originating node, the second the
* destination node, and the third gives the weight.  Additional graphic
*  parameters may be included, but will be ignored by SoNIA. The line must
* end with the time information in square brackets.  SoNIA does not have
* undirected edges, so when edge records are encountered, one arc will be
* created in each direction.
* <BR><BR>
* <big>Note on .net's concept of time</big><BR>
* Pajek uses an integer/interval concept of time, SoNIA uses a continous concept
* of time, so there can be some confusion when interperting integer time coordinates
* from .net files.  When numbers are used discretely, they refer to a chunk of time rather than
* to a point in time.  For example, 1 refers to the 1st chunk, 2 to the 2nd, etc.
*  This is very straight forward, but some confusion comes up when we want to
* describe an interval.   Does "1 to 2" mean 1 and 2?  Does "from 1996 to 1997"
* mean "all of 1996 and 1997" (two years), or just "from the beginning of 1996
* to the beginning 1997" (one year).  If we are using discrete intervals, "1 to
* 2" means two units of time, where in the continuous approach, 1 to 2 could
* either mean 1.0 to 2.0 (one unit of time) or 1.0 to 2.99 (very close to two
* units of time).  In SoNIA it is possible (and necessary) to work with both
* kinds of time, but in order to combine them in ways which allow for a
* meaningful visualization, it is crucial to be clear about which mode is used
* by various kinds of data and how they relate to the underlying network.  Which
* is why the .net parser throws up the dialog to ask "parse times as integers."
* If this is set to true, the end times will all have 0.99999 added to them, so
* that the interval 1-2 will become 1.0-2.99999 instead of 1.0 to 2.0.
* @version $Revision: 1.2 $ $Date: 2004-09-20 04:33:56 $
* @author Skye Bender-deMoll e-mail skyebend@santafe.edu
*/
public class DotNetParser extends Object implements Parser, ActionListener
{
  private BufferedReader reader;
  private Vector nodeList;
  private Vector arcList;
  private boolean combineSameNames = true;
  private boolean parseCoords = true;
  private boolean integerTime = false;
  private Hashtable nodeIdLookup;
  private int currentLineNum = 0;
  private Dialog settings;
  private Checkbox combineNames;
  private Checkbox coords;
  private Checkbox intTime;
  private Button OK;
  private String info = "";

  /**
   * Instantiates a parser for importing network data in files folowing the .net
   * format.  Throws up a dialog box to ask some settings question:  Should the
   * parser attempt to read the X and Y coords from the file? .net files are
   * supposed to include the coordinates, but the user may not want them, or
   * may not have coded them, in which case the parser needs to skip those tokens.
   * Should the parser parse times as integers?  This option is due to the possible
   * confusion when changing the conceptual model of time from discrete to continous.
   * FUTURE VERSIONS SHOULD FIGURE WAY TO DO THIS IN THE FILE DIALOG, SO THAT
   * THIS WILL HAVE NO GUI FUNCTIONS.
   */
  public DotNetParser()
  {
    //throw up a dilog to ask about combining and coords
    settings = new Dialog(new Frame(),"DotNet Parser Settings",true);
    settings.setLayout(new GridBagLayout());
    settings.setBackground(Color.lightGray);
    combineNames = new Checkbox("Combine nodes with same names",true);
    coords = new Checkbox("Parse X and Y coords from file",true);
    intTime = new Checkbox("parse times as integers",false);
    OK = new Button("OK");
    OK.addActionListener(this);
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(0,2,0,2);
    c.gridx=0;c.gridy=0;c.gridwidth=1;c.gridheight=1;c.weightx=1;c.weighty=1;
    settings.add(combineNames,c);
    c.gridx=0;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=1;c.weighty=1;
    settings.add(coords,c);
    c.gridx=0;c.gridy=2;c.gridwidth=1;c.gridheight=1;c.weightx=1;c.weighty=1;
    settings.add(intTime,c);
    c.gridx=0;c.gridy=3;c.gridwidth=1;c.gridheight=1;c.weightx=1;c.weighty=1;
    settings.add(OK,c);
    settings.setSize(200,150);
    settings.setLocation(300,50);
    settings.show();
    OK.requestFocus();
  }

  //gets the settings from the dilog
  /**
   * Action listener for getting the OK from the parser options dialog.
   */
  public void actionPerformed(ActionEvent evt)
  {
    combineSameNames = combineNames.getState();
    parseCoords = coords.getState();
    integerTime = intTime.getState();
    settings.hide();
    settings.dispose();
    settings= null;
  }
/**
 * Opens a connection to the .net file and attempts to parse a network from it.
 * Calls individual parsing methods for each kind of data, headers, etc.
 * @param fileAndPath string giving the path (including the file name) of the file
 * to be imported
 * @throws IOException if unable to parse correctly.  Includes very specific error
 * mesages with line numbers
 */
  public void parseNetwork(String fileAndPath)
  throws IOException
  {
    reader = new BufferedReader(new FileReader(fileAndPath));
    currentLineNum = 0;
    int numNodes = 0;
    int numArcs = 0;
    nodeList = new Vector();
    arcList = new Vector();
    nodeIdLookup = new Hashtable();
    //first line may start with *Network, in whichcase make it into info string
    String firstLine = reader.readLine();
    currentLineNum ++;
    if (firstLine.startsWith("*Network"))
    {
      info += firstLine;
      //next line is header
      firstLine = reader.readLine();
      currentLineNum ++;
    }
    //parse header (1st data lineline)
    numNodes = parseHeader(fileAndPath, firstLine);

    //next numNodes lines should be entries for nodes
    for (int n=1; n<=numNodes; n++)
    {
      //parse node and add it to list
      currentLineNum ++;
      parseNode(fileAndPath, reader.readLine().trim(), n);
    }

    //make sure next line is "*ARCS"
    currentLineNum ++;
    String line = reader.readLine().trim();
    if (line.equalsIgnoreCase("*Arcs"))
    {
      //rest of lines should be arcs or edges until arcs or edges or null
      currentLineNum ++;
      //PROBLEM HERE IF NO ARCS!
      line = reader.readLine().trim();
      boolean moreArcs = true;
      while  ((line != null) & moreArcs)
      {
       if (!line.equalsIgnoreCase("*Edges"))
       {
          line = line.trim();
          parseArc(fileAndPath,line,numNodes+2);
          currentLineNum ++;
          line = reader.readLine();
       }
       else
       {
         moreArcs = false;
       }
      }
    }
    if (line != null)
    {
      if (line.equalsIgnoreCase("*Edges"))
      {
        //go to the next line
        line = reader.readLine().trim();
        currentLineNum ++;
        //look for edge entries if there are any
        while (line != null)
        {
          //kludge! just make two arcs, one each way (otherWay has from and to reversed)
          line = line.trim();
          parseEdge(fileAndPath,line,numNodes+3);
          currentLineNum ++;
          line = reader.readLine();
        }
      }
    }
      reader.close();
    }


//need to add aditional code for time network format?
  private int parseHeader(String fileAndPath, String firstLine)
  throws IOException
  {
    int returnInt = 0;
    //first line should be *Vertices N where N is numNodes
    if (firstLine != null)
    {
      StringTokenizer header = new StringTokenizer(firstLine.trim()," ");
      if (header.countTokens() == 2)
      {
        if (header.nextToken().equalsIgnoreCase("*Vertices"))
        {
          try
          {
            returnInt = Integer.parseInt(header.nextToken());
          }
          catch (NumberFormatException intParseEx)
          {
            String error = "Unable to parse number of Vertices:\n" +fileAndPath;
            throw(new IOException(error));
          }
        }
        else
        {
          String error = ".net file must begin with \"*Vertices\":\n"+fileAndPath;
          throw(new IOException(error));
        }
      }
      else
      {
        String error = "Wrong number of entries in first line of file:\n" +fileAndPath;
          throw(new IOException(error));
      }
    }
    return returnInt;
  }

  private void parseNode(String fileAndPath, String line, int lineNumber)
  throws IOException
  {
    double x = 0.0;
    double y = 0.0;
    String label= "";
    String color = "";
    int nodeNumber = 0;  //node number within .net file
    int nodeId = 0; //sonia node id
    double nodeObsTime = 0;
    double nodeEndTime = Double.POSITIVE_INFINITY;
    // line should be: 1 "NodeLabel" xCoord yCoord paramStrings
    //space delimeted
    StringTokenizer nodeString = new StringTokenizer(line," ");
    if ((nodeString.countTokens() < 2) |
        //check if coordinates parsing is on
        (parseCoords & (nodeString.countTokens() < 4)))
    {
      String error = "Error line "+currentLineNum+" Missing entries";
      throw(new IOException(error));
    }
    else
    {
      //make sure line number match correctly
      try
      {
        nodeNumber = Integer.parseInt(nodeString.nextToken());
        if (nodeNumber != lineNumber)
        {

          String error = "Error line "+currentLineNum+
                         " Vertex line numbers must be in sequence";
          throw(new IOException(error));
        }
      }
      catch (NumberFormatException intParseEx)
      {
        String error = "Error line"+currentLineNum+
             "Each vertex must be proceeded by an integer line number";
        throw(new IOException(error));
      }
      //parse label
      label = nodeString.nextToken();
      //WHAT ABOUT SPACES IN LABEL?
      //if the label startes with quote and doesn't end with quote, include
      //tokens until quote located
      if (label.startsWith("\""))
      {
        if ((!label.endsWith("\"")) | label.equals("\"")) //incase " is followed by a space
        {
          try
          {
            String token = nodeString.nextToken();
            while (!token.endsWith("\""))
            {
              label += " " + token;
              token = nodeString.nextToken();
            }
            label += " " + token;
          }
          catch (NoSuchElementException e)
          {
            String error = "Error line"+currentLineNum+
                           "Unclosed quotes?";
            throw(new IOException(error));
          }
        }
        label = label.substring(1,label.lastIndexOf("\""));
      }

      //combine same names if applicable
      if (combineSameNames)
      {
        //check if name already exists
        if (nodeIdLookup.containsKey(label))
        {
          //set the id of this node to the matching name so arcs will connect
          nodeId = ((Integer)nodeIdLookup.get(label)).intValue();
        }
        else
          //it is not on the list, so add it
        {
          nodeId = nodeNumber;
          nodeIdLookup.put(new Integer(nodeId),label);
        }
      }
      else
        //otherwise, add this entry in the name id lookup
        //(in case it is needed for a later step)
      {
        nodeId = nodeNumber;
        nodeIdLookup.put(new Integer(nodeId),label);
      }

      //parse coords
      if (parseCoords)
      {
        try
        {
          x = Double.parseDouble(nodeString.nextToken());
          y = Double.parseDouble(nodeString.nextToken());
        }
        catch (NumberFormatException doubleParseEx)
        {
          String error = "Line"+currentLineNum+" Unable to parse coordinate";
          throw(new IOException(error));
        }
      }
      //remaining tokens
      //NEED TO DISCARD Z, DO COLORS AND ALL SORTS OF HARD STUFF
      //USE Z AS TIME COORD?
      //parse time coordinates
      String time = "";
      while (nodeString.hasMoreTokens())
      {
        time = nodeString.nextToken();
        //kludge colors in here as well
        if (time.startsWith("c"))
        {
         //assume the next token is a string for color
          color = nodeString.nextToken();
        }

        if (time.startsWith("["))
        {
          //strip off the braces
          time = time.substring(time.indexOf("[")+1,time.indexOf("]"));
          //find out if there are commas, meaning we have to add this line
          //several times
          Vector timesToParse = new Vector();
          if (time.indexOf(",") > 0)
          {
            //put each of the time entries into a list to be parserd later
            StringTokenizer comanizer = new StringTokenizer(time,",");
            while (comanizer.hasMoreElements())
            {
              timesToParse.add(comanizer.nextToken());
            }
          }
          else
          {
            timesToParse.add(time);
          }
          //parse and make a entry for each time on list
          for (int t = 0; t<timesToParse.size(); t++)
          {
            nodeObsTime = parseStartTime((String)timesToParse.get(t),currentLineNum);
            nodeEndTime = parseEndTime((String)timesToParse.get(t),currentLineNum);
            NodeAttribute node = new NodeAttribute(nodeId, label, x,y,nodeObsTime,
        nodeEndTime,fileAndPath + ":"+nodeNumber);
            nodeList.add(node);
            node.setNodeColor(parseColor(color));
          }
        }
      }
    }
  }

  //NOTE does not check for duplicate arcs
  //DOES check that nodes exist
  private void parseArc(String fileAndPath, String line, int lineNumber)
      throws IOException
  {
    double arcObsTime = 0;
    double arcEndTime = Double.POSITIVE_INFINITY;
    int fromId;
    int toId;
    double weight;
    double width = 1.0;
    Color color = Color.lightGray;

    StringTokenizer arcString = new StringTokenizer(line," ");
    if (arcString.countTokens() < 3)
    {
      String error = "Line "+currentLineNum+" is missing Entries";
      throw(new IOException(error));
    }
    else
    {
      try
      {
        //parse the from and to nodes to integers
        fromId = Integer.parseInt(arcString.nextToken());
        toId = Integer.parseInt(arcString.nextToken());
      }
      catch (NumberFormatException intParseEx)
      {
       String error = "Line "+currentLineNum+
                      "Each arc must have an integer id for from and to nodes";
       throw(new IOException(error));
      }
      //check to make sure start and end node ids exist
      if ((nodeIdLookup.containsKey(new Integer(fromId))) &
           (nodeIdLookup.containsKey(new Integer(toId))))
      {
        //parse weight
        try
        {
          weight = Double.parseDouble(arcString.nextToken());
        }
        catch (NumberFormatException doubleParseEx)
        {
         String error = "Line "+currentLineNum+" Unable to parse arc weight";
         throw(new IOException(error));
        }
        //NEED TO DEAL WITH LETTER TOKENS AND COMPLICATED STUFF
        //parse width

        //parse all the rest
        //parse time coordinates
        String time = "";
        while (arcString.hasMoreTokens())
        {
          time = arcString.nextToken();
          //KLUDGE FOR COLOR
          if (time.startsWith("c"))
          {
            color = parseColor(arcString.nextToken());
          }

          if (time.startsWith("["))
          {
            //strip off the braces
            time = time.substring(time.indexOf("[")+1,time.indexOf("]"));
            //find out if there are commas, meaning we have to add this line
            //several times
            Vector timesToParse = new Vector();
            if (time.indexOf(",") > 0)
            {
              //put each of the time entries into a list to be parserd later
              StringTokenizer comanizer = new StringTokenizer(time,",");
              while (comanizer.hasMoreElements())
              {
                timesToParse.add(comanizer.nextToken());
              }
            }
            else
            {
              timesToParse.add(time);
            }
            //parse and make a entry for each time on list
            for (int t = 0; t<timesToParse.size(); t++)
            {
              arcObsTime = parseStartTime((String)timesToParse.get(t),currentLineNum);
               arcEndTime = parseEndTime((String)timesToParse.get(t),currentLineNum);
              ArcAttribute arc = new ArcAttribute(arcObsTime, arcEndTime,
                                        fromId,toId,weight,width);
              arc.setArcColor(color);
              arcList.add(arc);
            }
          }
        }
      }
      else
      {
        String error = "Line "+currentLineNum+
                       " No node corresponds to node id in arc def'n";
       throw(new IOException(error));
      }
    }
  }

  //NOTE does not check for duplicate arcs
   //DOES check that nodes exist
   private void parseEdge(String fileAndPath, String line, int lineNumber)
       throws IOException
   {
     double arcObsTime = 0;
     double arcEndTime = Double.POSITIVE_INFINITY;
     int fromId;
     int toId;
     double weight;
     double width = 1.0;
     Color color = Color.lightGray;

     StringTokenizer arcString = new StringTokenizer(line," ");
     if (arcString.countTokens() < 3)
     {
       String error = "Line "+currentLineNum+" is missing Entries";
       throw(new IOException(error));
     }
     else
     {
       try
       {
         //parse the from and to nodes to integers
         fromId = Integer.parseInt(arcString.nextToken());
         toId = Integer.parseInt(arcString.nextToken());
       }
       catch (NumberFormatException intParseEx)
       {
        String error = "Line "+currentLineNum+
                       "Each arc must have an integer id for from and to nodes";
        throw(new IOException(error));
       }
       //check to make sure start and end node ids exist
       if ((nodeIdLookup.containsKey(new Integer(fromId))) &
            (nodeIdLookup.containsKey(new Integer(toId))))
       {
         //parse weight
         try
         {
           weight = Double.parseDouble(arcString.nextToken());
         }
         catch (NumberFormatException doubleParseEx)
         {
          String error = "Line "+currentLineNum+" Unable to parse arc weight";
          throw(new IOException(error));
         }
         //NEED TO DEAL WITH LETTER TOKENS AND COMPLICATED STUFF
         //parse width

         //parse all the rest
         //parse time coordinates
         String time = "";
         while (arcString.hasMoreTokens())
         {
           time = arcString.nextToken();
           //Kludge colors
           if (time.startsWith("c"))
           {
             //assume next token is color
             color = parseColor(arcString.nextToken());
           }
           if (time.startsWith("["))
           {
             //strip off the braces
             time = time.substring(time.indexOf("[")+1,time.indexOf("]"));
             //find out if there are commas, meaning we have to add this line
             //several times
             Vector timesToParse = new Vector();
             if (time.indexOf(",") > 0)
             {
               //put each of the time entries into a list to be parserd later
               StringTokenizer comanizer = new StringTokenizer(time,",");
               while (comanizer.hasMoreElements())
               {
                 timesToParse.add(comanizer.nextToken());
               }
             }
             else
             {
               timesToParse.add(time);
             }
             //parse and make a entry for each time on list
             for (int t = 0; t<timesToParse.size(); t++)
             {
               arcObsTime = parseStartTime((String)timesToParse.get(t),currentLineNum);
               arcEndTime = parseEndTime((String)timesToParse.get(t),currentLineNum);
               //make two arcs, one in each direction
               ArcAttribute oneWay = new ArcAttribute(arcObsTime, arcEndTime,
                                         fromId,toId,weight,width);
               ArcAttribute otherWay = new ArcAttribute(oneWay.getObsTime(),
                   oneWay.getEndTime(),oneWay.getToNodeId(),oneWay.getFromNodeId(),
                   oneWay.getArcWeight(),oneWay.getArcWidth());
               oneWay.setArcColor(color);
               otherWay.setArcColor(color);
               arcList.add(oneWay);
               arcList.add(otherWay);

             }
           }
         }
       }
       else
       {
         String error = "Line "+currentLineNum+
                        " No node corresponds to node id in arc def'n";
        throw(new IOException(error));
       }
     }
  }

  //extracts the start time from pajek style timecode
  //"1-2" = 1 or "1" = 1
  private double parseStartTime(String time, int currentLineNum)
    throws IOException
  {
    double startTime = 0.0;
    int dashIndex = time.indexOf("-");
    try
      {
        //check if there is a dash
       if (dashIndex > 0)
        {
          startTime = Double.parseDouble(time.substring(0,dashIndex));
        }
        else
        {
          //no dash, so just parse the number
          startTime = Double.parseDouble(time);
        }
      }
      catch (NumberFormatException doubleParseEx)
      {
       String error = "Line"+currentLineNum+" Unable to parse start time coordinate";
       throw(new IOException(error));
      }
    return startTime;
  }

  //extracts the end time from pajek style timecode
  //"1-4" = 4 or "0-*" = infinity or "3" = 3
  private double parseEndTime(String time, int currentLineNum)
    throws IOException
  {
    double endTime = Double.POSITIVE_INFINITY;
    //strip of the bracket, parse until dash
    int dashIndex = time.indexOf("-");
    if (time.endsWith("*"))
    {
      //return infinity
    }
    else
    {
      try
      {
        endTime = Double.parseDouble(time.substring(dashIndex+1,time.length()));
        //KLUDGE
        //if "integer time" the end time gives the number of time block, so end should equal a longer time
        if (integerTime)
        {
          endTime = endTime+0.99999999;
        }
      }
      catch (NumberFormatException doubleParseEx)
      {
        String error = "Line"+currentLineNum+" Unable to parse end time coordinate";
        throw(new IOException(error));
      }
    }
    return endTime;
  }

  //takes a string and returns one of java's colors, defaults to blue
  private Color parseColor(String text)
      throws IOException
  {
    Color theColor;
    if (text.indexOf(",") > 0)
    {
      try
      {
      //asume it is an RBG triple and parse as such
      StringTokenizer numTokenizer = new StringTokenizer(text, ",");
      float red = Float.parseFloat(numTokenizer.nextToken());
      float green = Float.parseFloat(numTokenizer.nextToken());
      float blue = Float.parseFloat(numTokenizer.nextToken());
      theColor = new Color(red,green,blue);
      }
      catch (Exception e)
      {
        String error = "Line"+currentLineNum+" Unable to parse colorNumbers";
        throw(new IOException(error));
      }
    }
    else
    {
      if (text.equalsIgnoreCase("Black"))
      {
        theColor = Color.black;
      }
      else if (text.equalsIgnoreCase("Cyan"))
      {
        theColor = Color.cyan;
      }
      else if (text.equalsIgnoreCase("DarkGray"))
      {
        theColor = Color.darkGray;
      }
      else if (text.equalsIgnoreCase("gray"))
      {
        theColor = Color.gray;
      }
      else if (text.equalsIgnoreCase("green"))
      {
        theColor = Color.green;
      }
      else if (text.equalsIgnoreCase("lightGray"))
      {
        theColor = Color.lightGray;
      }
      else if (text.equalsIgnoreCase("magenta"))
      {
        theColor = Color.magenta;
      }
      else if (text.equalsIgnoreCase("Orange"))
      {
        theColor = Color.orange;
      }
      else if (text.equalsIgnoreCase("pink"))
      {
        theColor = Color.pink;
      }
      else if (text.equalsIgnoreCase("red"))
      {
        theColor = Color.red;
      }
      else if (text.equalsIgnoreCase("white"))
      {
        theColor = Color.white;
      }
      else if (text.equalsIgnoreCase("yellow"))
      {
        theColor = Color.yellow;
      }
      else
      {
        theColor = Color.blue;
      }
    }

    return theColor;
  }

  //accessors----------------------------------------
  /**
   * Returns the largest possible number of nodes (NOT node events) which can
   * be in existance at once - the number of unique node ids.
   * @return the number of unique node ids
   */
  public int getMaxNumNodes()
  {
    //should be equal to the number of entries in the nodeID lookup
    return nodeIdLookup.size();
  }

  /**
   * Returns the number of node events parsed.
   */
  public int getNumNodeEvents()
  {
    return nodeList.size();
  }

  /**
   * Returns the number of arc events parsed.
   */
  public int getNumArcEvents()
  {
    return arcList.size();
  }

  /**
   * Returns a Vector containing all the NodeAttributes parsed from the file.
   * (This is the method used to actually get the data from the parser)
   */
  public Vector getNodeList()
  {
    return nodeList;
  }

  /**
   * Returns a Vector containing all the ArcAttributes parsed from the file.
   * (This is the method used to get the arc attributes from the parser)
   */
  public Vector getArcList()
  {
    return arcList;
  }

  /**
   * Returns the info string from the network, this will include anything included
   * in the "* Network" tag at the start of the file.
   */
  public String getNetInfo()
  {
    return info;
  }

  /**
   * Returns hash table for geting the internal id numbers of nodes from their
   * name or pajekID
   */
  public Hashtable getNodeIdLookup()
  {
    return nodeIdLookup;
  }

  /**
   * Returns the name of the parser, in this case "DotNetParser".
   */
  public String getParserInfo()
  {
    return "DotNetParser";
  }

}