package sonia;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import cern.colt.list.IntArrayList;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

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
 * Manages mouseovers that allow inspecting the properties of nodes and the
 * data that may be attached
 */
public class NodeInspector implements MouseListener,MouseMotionListener
{
  private SoniaController control;
  private SoniaLayoutEngine engine;
  private JComponent  frame;
  private LayoutSlice slice;
  private double[] xCoords;
  private double[] yCoords;
  private Vector nodeEvents;
  private RenderSlice nodeView;
  private int selectedIndex;
  private double selectedSize;
  //how for to translate the coords by (because of margins)
  private int left;
  private int top;
  boolean inspecting = false;
  
  private NumberFormat format;
  
  //gui components
  private JPanel inspectPanel = null;
  private JLabel mainLabel;
  private JLabel timeLabel;
  private JLabel colors;
  private JLabel size;
  private JLabel fileXY;
  private JLabel currentXY;
  private JLabel shape;
  private JLabel origFileLoc;
  private JComponent nodeData;
  
  //labels/keys for GUI
  public static final String ID ="ID";
  public static final String LABEL ="Label";
  public static final String START ="Start time";
  public static final String END ="End time";
  public static final String SIZE ="Size";
  public static final String REND_XY ="Render XY";
  public static final String FILE_XY ="File XY";
  public static final String COLOR = "Color";
  public static final String SHAPE ="Shape";
  public static final String BORDER_W ="Border width";
  public static final String BORDER_C ="Border color";
  public static final String LABEL_S ="Label size";
  public static final String LABEL_C ="Label color";
  public static final String EFFECT = "Effect";
  public static final String FILE ="File location";
  public static final String ICON ="Icon";
  
  private String[] propkeys = new String[]{ID,LABEL,START,END,SIZE,REND_XY,FILE_XY
		  ,COLOR,SHAPE,ICON,BORDER_W,BORDER_C,LABEL_S,LABEL_C,EFFECT,FILE};
  
  

  public NodeInspector(SoniaController cont, SoniaLayoutEngine eng, JComponent frm)
  {
    control = cont;
    engine = eng;
    frame = frm;
   
    format = NumberFormat.getInstance(Locale.ENGLISH);
    format.setMaximumFractionDigits(3);
    format.setMinimumFractionDigits(3);;
    
    //how for to translate the coords by (because of margins)
    left = engine.getLeftPad();
    top = engine.getTopPad();
    selectedIndex = -1;
    

  }
  
  public void activate(){
	  slice = engine.getCurrentSlice();
	    //add this as a lister to the frame to
	    frame.addMouseListener(this);
	    frame.addMouseMotionListener(this);
	    xCoords = engine.getCurrentXCoords();
	    yCoords = engine.getCurrentYCoords();
	    nodeView = engine.getRenderSlice(slice.getSliceStart(),slice.getSliceEnd());
	    nodeEvents = nodeView.getNodeEvents();
	    inspecting = true;
  }
  
  public void deactivate(){
	  frame.removeMouseListener(this);
	  frame.removeMouseMotionListener(this);
	  inspecting = false;
  }

  public void mouseMoved(MouseEvent evt){}

  public void mouseDragged(MouseEvent evt)
  {
    
  }

  public void mousePressed(MouseEvent e)
  {
    selectedIndex = getTarget(e.getX(),e.getY());
    //if nothing was selected

    if (selectedIndex < 0 )
    {
    	showNoInfo();
    } else {
    	showNodeInfo((NodeAttribute)nodeEvents.get(selectedIndex));
    }
    engine.updateDisplays();
    hiliteSelected();
  }
  public void mouseReleased(MouseEvent e)
  {
    engine.updateDisplays();
    hiliteSelected();
  }
  public void mouseExited(MouseEvent e)
  {
  }
  public void mouseEntered(MouseEvent e)
  {
  }
  public void mouseClicked(MouseEvent e)
  {
  }

  private int getTarget(double x, double y)
  {

    int targetIndex = -1;
    double nodeSize;
    //if overlapp, will return last (topmost?) index
    for (int i = 0; i<nodeEvents.size();i++)
    {
      NodeAttribute node = (NodeAttribute)nodeEvents.get(i);
      nodeSize = node.getNodeSize()/2.0;

      int nodeIndex = node.getNodeId()-1;
      //check for hit
      if (((x-left < xCoords[nodeIndex]+nodeSize) & (x-left > xCoords[nodeIndex]-nodeSize))
          &((y-top < yCoords[nodeIndex]+nodeSize) & (y-top > yCoords[nodeIndex]-nodeSize)))
      {
        targetIndex = nodeIndex;
        selectedSize = nodeSize;  //for later use by the graphics
        //TODO: need to get the correctly scaled node size
      }

    }
    return targetIndex;
  }

  private void hiliteSelected()
  {
	  //TODO: hiliting is broken for node mover
    Graphics2D graphics = (Graphics2D)frame.getGraphics();
    graphics.setColor(Color.black);
    graphics.setXORMode(Color.white);
    //now draw the hilighting
    int x;
    int y;
    int size = (int)Math.round(selectedSize);
    if (selectedIndex >= 0)
    {
      x = (int)Math.round(xCoords[selectedIndex]);
      y = (int)Math.round(yCoords[selectedIndex]);
      //draw black "handle" box at each corner
      graphics.fillRect(x+left-size-1,y+top-size-1,3,3);
      graphics.fillRect(x+left-size-1,y+top+size-1,3,3);
      graphics.fillRect(x+left+size-1,y+top+size-1,3,3);
      graphics.fillRect(x+left+size-1,y+top-size-1,3,3);
    }
  }

  
  /**
   * return a panel that will will show all the data for the node, and will
   * be updated as things are clicked. 
   * @author skyebend
   * @return
   */
  public JPanel getInspectPanel(){
	  if (inspectPanel == null){
		  //build the panel gui
		  inspectPanel = new JPanel(new GridBagLayout());
		  GridBagConstraints c = new GridBagConstraints();
		  inspectPanel.setBorder(new TitledBorder("Node Attributes"));
		   mainLabel = new JLabel("No node selected");
		   mainLabel.setForeground(NodeAttribute.DEFULAT_LABEL_COLOR);
		   timeLabel = new JLabel("Start:? End:?");
		   currentXY = new JLabel("Render Coordinates:?,?");
		   fileXY = new JLabel("File Coordinates:?,?");
		   colors = new JLabel("Colors:default");
		   colors.setBackground(NodeAttribute.DEFAULT_NODE_COLOR);
		   colors.setBorder(new LineBorder(NodeAttribute.DEFAULT_BORDER_COLOR,1));
		   shape = new JLabel("Shape:?");
		   size = new JLabel("Size: ?");
		   origFileLoc = new JLabel("File: ?");
		   nodeData = getNodeDataComp(null);
		   
		   
		   // first col
		   c.gridx =0;c.gridy=0;c.anchor=GridBagConstraints.WEST;
		  inspectPanel.add(mainLabel,c);
		  c.gridy=1;
		  inspectPanel.add(timeLabel,c);
		  c.gridy=2;
		  inspectPanel.add(currentXY,c);
		  c.gridy=3;
		  inspectPanel.add(fileXY,c);
		  c.gridy=4;
		  inspectPanel.add(colors,c);
		  //second col
		  c.gridx=1;c.gridy=0;
		  inspectPanel.add(shape,c);
		  c.gridx=1;c.gridy=1;
		  inspectPanel.add(size,c);
		 
		  //third col
		  c.gridx=2;c.gridy=0;
		  JButton activate = new JButton("inspect");
		  inspectPanel.add(activate,c);
		  activate.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (inspecting){
					deactivate();
					inspecting = false;
				} else {
					activate();
					inspecting = true;
				}
			}
		  });
		  c.gridx=2;c.gridy=1;c.gridheight=5;
		  inspectPanel.add(nodeData,c);
		  //span col
		  c.gridx=0;c.gridy=5;c.gridwidth=3;c.gridheight=1;
		  inspectPanel.add(origFileLoc,c);
			
	  }
	  return inspectPanel;
  }
  
  /**
   * update the inspector panel to show the data attached to the passed node
   * @author skyebend
   * @param node
   */
  public void showNodeInfo(NodeAttribute node){
	  mainLabel.setText("ID: "+ node.getNodeId()+ " Label: \""+node.getNodeLabel()+"\"");
	   timeLabel.setText("Start: "+node.getObsTime()+ " End: "+node.getEndTime());
	   currentXY.setText("Render Coordinates: ("+
			   format.format(xCoords[node.getNodeId()-1])+","+
			   format.format(yCoords[node.getNodeId()-1])+")");
	   fileXY.setText("File Coordinates: ("+ 
			   format.format(node.getObsXCoord())+","+
			   format.format(node.getObsYCoord())+")");
	   colors.setText("Node & border color ");
	   colors.setBackground(node.getNodeColor());
	   colors.setBorder(new LineBorder(node.getBorderColor(),
			   (int)Math.round(node.getBorderWidth())));
	   shape.setText("Shape: "+node.getNodeShape().getClass().getSimpleName());
	   size.setText(format.format(node.getNodeSize()));
	   origFileLoc.setText("File: "+node.getOrigFileLoc());
	   inspectPanel.remove(nodeData);
	   nodeData = getNodeDataComp(node);
	   GridBagConstraints c = new GridBagConstraints();
	   c.gridx=2;c.gridy=1;c.gridheight=5;
		  inspectPanel.add(nodeData,c);
	   
	  System.out.println("inspecting node "+node.toString());
  }
  
  /**
   * set the inspector panel ui to show that no node is selected
   * @author skyebend
   */
  public void showNoInfo(){
	  mainLabel.setText("No node selected");
	   timeLabel.setText("Start: ? End: ?");
	   currentXY.setText("Render Coordinates: (?,?)");
	   fileXY.setText("File Coordinates: (?,?)");
	   colors.setText("Node & border color");
	   colors.setBackground(NodeAttribute.DEFAULT_NODE_COLOR);
	   colors.setBorder(new LineBorder(NodeAttribute.DEFAULT_BORDER_COLOR,1));
	   shape.setText("Shape: ?");
	   size.setText("Size: ?");
	   origFileLoc.setText("File: ?");
	   inspectPanel.remove(nodeData);
	   nodeData = getNodeDataComp(null);
	   GridBagConstraints c = new GridBagConstraints();
	   c.gridx=2;c.gridy=1;c.gridheight=5;
		  inspectPanel.add(nodeData,c);
  }
  
  private JComponent getNodeDataComp(NodeAttribute node){
	  Object[][] keyAndValue;
	  Object[] header = new Object[]{"DataKey","Value"};
	  if (node != null && node.getDataKeys() != null){
		  Object[] keys = node.getDataKeys().toArray();
		  keyAndValue = new Object[keys.length][2];
		  for (int i = 0; i < keys.length; i++) {
			  keyAndValue[i][0] = keys[i];
			  keyAndValue[i][1] = node.getData((String)keys[i]);
		  }
	  } else {
		  keyAndValue = new Object[1][2];
		  keyAndValue[0][0]="<none>";
		  keyAndValue[0][1]="<none>";
	  }
	
	  JTable dataTable = new JTable(keyAndValue,header);
	  dataTable.setPreferredScrollableViewportSize(new Dimension(120, 50));
	  JScrollPane scroller = new JScrollPane(dataTable);
	  return scroller;
  }

}