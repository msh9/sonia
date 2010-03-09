package sonia;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.Component;
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
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

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
 * Manages mouseovers that allow inspecting the properties of nodes and the data
 * that may be attached
 */
public class NodeInspector implements MouseListener, ChangeListener, MouseMotionListener {
	private SoniaController control;

	private SoniaLayoutEngine engine;

	//private JComponent frame;
	private SoniaCanvas canvas;

	private LayoutSlice slice;

	private double[] xCoords;

	private double[] yCoords;

	private Vector nodeEvents;

	private RenderSlice nodeView;

	private int selectedIndex;
	
	private NodeAttribute selectedNode;

	private double selectedSize;

	// how for to translate the coords by (because of margins)
	private int left;

	private int top;

	boolean inspecting = false;

	private NumberFormat format;

	// gui components
	private JPanel inspectPanel = null;

	private JComponent nodeData;

	private JComponent nodeProps;

	private TableCellRenderer basic = new DefaultTableCellRenderer();

	private TableCellRenderer color = new ColorRender();

	// labels/keys for GUI
	public static final String ID = "ID";

	public static final String LABEL = "Label";

	public static final String START = "Start time";

	public static final String END = "End time";

	public static final String SIZE = "Size";

	public static final String REND_X = "Render X";

	public static final String REND_Y = "Render Y";

	public static final String FILE_X = "File X";

	public static final String FILE_Y = "File Y";

	public static final String COLOR = "Color";

	public static final String SHAPE = "Shape";

	public static final String BORDER_W = "Border width";

	public static final String BORDER_C = "Border color";

	public static final String LABEL_S = "Label size";

	public static final String LABEL_C = "Label color";

	public static final String EFFECT = "Effect";

	public static final String FILE = "File location";

	public static final String ICON = "Icon";

	private String[] propkeys = new String[] { ID, LABEL, START, END, SIZE,
			REND_X, REND_Y, FILE_X, FILE_Y, COLOR, SHAPE, ICON, BORDER_W,
			BORDER_C, LABEL_S, LABEL_C, EFFECT, FILE };

	public NodeInspector(SoniaController cont, SoniaLayoutEngine eng,
			JComponent frm) {
		control = cont;
		engine = eng;
		//frame = frm;

		format = NumberFormat.getInstance(Locale.ENGLISH);
		format.setMaximumFractionDigits(3);
		format.setMinimumFractionDigits(3);
		;

		// how for to translate the coords by (because of margins)
		left = engine.getLeftPad();
		top = engine.getTopPad();
		selectedIndex = -1;
		selectedNode = null;
	}

	public void activate() {
		slice = engine.getCurrentSlice();
		canvas = engine.getLayoutWindow().getDisplay();
		// add this as a lister to the frame to
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		xCoords = engine.getCurrentXCoords();
		yCoords = engine.getCurrentYCoords();
		nodeView = engine.getRenderSlice(slice.getSliceStart(), slice
				.getSliceEnd());
		nodeEvents = nodeView.getNodeEvents();
		inspecting = true;

	}

	public void deactivate() {
		if (canvas != null){
		canvas.removeMouseListener(this);
		canvas.removeMouseMotionListener(this);
		}
		inspecting = false;
		selectedIndex = -1;
		selectedNode = null;
	}

	public void mousePressed(MouseEvent e) {
		selectedIndex = getTarget(e.getX(), e.getY());
		// if nothing was selected

		if (selectedIndex < 0) {
			showNoInfo();
		} else {
			showNodeInfo(selectedNode);
		}
		engine.updateDisplays();
		hiliteSelected();
		inspectPanel.repaint();

	}

	public void mouseReleased(MouseEvent e) {
		// engine.updateDisplays();
		// hiliteSelected();
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	private int getTarget(double x, double y) {

		int targetIndex = -1;
		double nodeSize;
		// if overlapp, will return last (topmost?) index
		for (int i = 0; i < nodeEvents.size(); i++) {
			NodeAttribute node = (NodeAttribute) nodeEvents.get(i);
			nodeSize = Math.max(1.0,node.getNodeSize() / 2.0); //always leave at least 1 pixel for clicking

			int nodeIndex = node.getNodeId() - 1;
			// check for hit
			if (((x - left < xCoords[nodeIndex] + nodeSize) & (x - left > xCoords[nodeIndex]
					- nodeSize))
					& ((y - top < yCoords[nodeIndex] + nodeSize) & (y - top > yCoords[nodeIndex]
							- nodeSize))) {
				targetIndex = nodeIndex;
				selectedSize = nodeSize; // for later use by the graphics
				selectedNode = node;
				// TODO: need to get the correctly scaled node size
			}

		}
		return targetIndex;
	}

	private void hiliteSelected() {
		// TODO: hiliting is broken, should move into render slice
		Graphics2D graphics = (Graphics2D) canvas.getGraphics();
		graphics.setColor(Color.black);
		graphics.setXORMode(Color.white);
		// now draw the hilighting
		int x;
		int y;
		int size = (int) Math.round(selectedSize);
		if (selectedIndex >= 0) {
			x = (int) Math.round(xCoords[selectedIndex]);
			y = (int) Math.round(yCoords[selectedIndex]);
			// draw black "handle" box at each corner
			graphics.fillRect(x + left - size - 1, y + top - size - 1, 3, 3);
			graphics.fillRect(x + left - size - 1, y + top + size - 1, 3, 3);
			graphics.fillRect(x + left + size - 1, y + top + size - 1, 3, 3);
			graphics.fillRect(x + left + size - 1, y + top - size - 1, 3, 3);
		}
	}

	/**
	 * return a panel that will will show all the data for the node, and will be
	 * updated as things are clicked.
	 * 
	 * @author skyebend
	 * @return
	 */
	public JPanel getInspectPanel() {
		if (inspectPanel == null) {
			// build the panel gui
			inspectPanel = new JPanel(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			inspectPanel.setBorder(new TitledBorder("Node Attributes"));
			nodeData = getNodeDataComp(null);
			JTable nodeTable = new JTable(new AttributeTableModel()){
				public TableCellRenderer getCellRenderer(int row, int column) {
					if (getValueAt(row, column) instanceof Color) {
						return color;
					}
					// else...
					return basic;

				}
			};
			nodeTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
			nodeTable
					.setPreferredScrollableViewportSize(new Dimension(250, 70));
			nodeProps = new JScrollPane(nodeTable);
			// nodeProps.setPreferredSize();
			// nodeProps.setBorder(new TitledBorder("Properties"));
			// first col
			c.fill = GridBagConstraints.BOTH;
			c.insets.right = 5;
			c.gridx = 0;
			c.gridy = 0;
			c.anchor = GridBagConstraints.WEST;
			c.gridheight = 1;
			inspectPanel.add(nodeProps, c);
			c.gridx = 1;
			c.gridy = 0;
			c.gridheight = 1;
			inspectPanel.add(nodeData, c);

		}
		return inspectPanel;
	}

	/**
	 * used to check if this is the active window and set the mouse performance
	 * accordingly
	 */
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof JTabbedPane) {
			if (((JTabbedPane) e.getSource()).getSelectedComponent().equals(
					inspectPanel)) {
				activate();
			} else {
				deactivate();
			}
		}
	}

	/**
	 * update the inspector panel to show the data attached to the passed node
	 * 
	 * @author skyebend
	 * @param node
	 */
	public void showNodeInfo(NodeAttribute node) {
		inspectPanel.remove(nodeData);
		nodeData = getNodeDataComp(node);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets.right = 5;
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 1;
		inspectPanel.add(nodeData, c);
		inspectPanel.doLayout();
		inspectPanel.repaint();
	}

	/**
	 * set the inspector panel ui to show that no node is selected
	 * 
	 * @author skyebend
	 */
	public void showNoInfo() {
		inspectPanel.remove(nodeData);
		// nodeData = getNodeDataComp(null);
		// GridBagConstraints c = new GridBagConstraints();
		// c.gridx=1;c.gridy=0;c.gridheight=1;
		// inspectPanel.add(nodeData,c);
		inspectPanel.repaint();
	}

	private JComponent getNodeDataComp(NodeAttribute node) {
		Object[][] keyAndValue;
		Object[] header = new Object[] { "DataKey", "Value" };
		if (node != null && node.getDataKeys() != null) {
			Object[] keys = node.getDataKeys().toArray();
			keyAndValue = new Object[keys.length][2];
			for (int i = 0; i < keys.length; i++) {
				keyAndValue[i][0] = keys[i];
				keyAndValue[i][1] = node.getData((String) keys[i]);
			}
		} else {
			keyAndValue = new Object[1][2];
			keyAndValue[0][0] = "<none>";
			keyAndValue[0][1] = "<none>";
		}

		JTable dataTable = new JTable(keyAndValue, header);
		
		dataTable.setPreferredScrollableViewportSize(new Dimension(120, 70));
		JScrollPane scroller = new JScrollPane(dataTable);
		// scroller.setBorder(new TitledBorder("Attached data"));
		return scroller;
	}

	private class AttributeTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		private String[] columnNames = { "Node property", "Value" };

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return propkeys.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			if (col == 0) {
				return propkeys[row];
			} else if (col == 1) {
				if (selectedIndex < 0) {
					return "No node selected";
				} else {
					// check which attribute it is
					String attr = propkeys[row];
					if (attr.equals(ID)) {
						return selectedNode.getNodeId();
					} else if (attr.equals(LABEL)) {
						return selectedNode.getNodeLabel();
					} else if (attr.equals(START)) {
						return selectedNode.getObsTime();
					} else if (attr.equals(END)) {
						return selectedNode.getEndTime();
					} else if (attr.equals(SIZE)) {
						return selectedNode.getNodeSize();
					} else if (attr.equals(REND_X)) {
						return xCoords[selectedIndex];
					} else if (attr.equals(REND_Y)) {
						return yCoords[selectedIndex];
					} else if (attr.equals(FILE_X)) {
						return selectedNode.getObsXCoord();
					} else if (attr.equals(FILE_Y)) {
						return selectedNode.getObsYCoord();
					} else if (attr.equals(COLOR)) {
						return selectedNode.getNodeColor();
					} else if (attr.equals(SHAPE)) {
						return selectedNode.getNodeShape();
					} else if (attr.equals(BORDER_W)) {
						return selectedNode.getBorderWidth();
					} else if (attr.equals(BORDER_C)) {
						return selectedNode.getBorderColor();
					} else if (attr.equals(LABEL_S)) {
						return selectedNode.getLabelSize();
					} else if (attr.equals(LABEL_C)) {
						return selectedNode.getLabelColor();
					} else if (attr.equals(EFFECT)) {
						return selectedNode.getEffect();
					} else if (attr.equals(FILE)) {
						return selectedNode.getOrigFileLoc();
					} else if (attr.equals(ICON)) {
						return selectedNode.getIconURL();
					} else {
						return null;
					}
				}

			} else {
				return null;
			}
		}

		/*
		 * JTable uses this method to determine the default renderer/ editor for
		 * each cell. If we didn't implement this method, then the last column
		 * would contain text ("true"/"false"), rather than a check box.
		 */
		// public Class getColumnClass(int c) {
		//			
		// if (getValueAt(0, c) != null){
		// return getValueAt(0, c).getClass();
		// }
		// return Object.class;
		// }
		public boolean isCellEditable(int row, int col) {
			// Note that the data/cell address is constant,
			// no matter where the cell appears onscreen.
			if (col < 1) {
				return false;
			} else {
				String attr = propkeys[row];
				if (attr.equals(LABEL)) {
					return true;
				} else if (attr.equals(SIZE)) {
					return true;
				} else if (attr.equals(REND_X)) {
					return true;
				} else if (attr.equals(REND_Y)) {
					return true;
				} else if (attr.equals(BORDER_W)) {
					return true;
				} else if (attr.equals(LABEL_S)) {
					return true;
				} else {
					return false;
				}
			}
		}

		public void setValueAt(Object value, int row, int col) {
			if (col == 1) {
				String attr = propkeys[row];
				if (attr.equals(LABEL)) {
					selectedNode.setNodeLabel(value.toString());
				} else if (attr.equals(SIZE)) {
					selectedNode.setNodeSize(Double.parseDouble(value.toString()));
				} else if (attr.equals(REND_X)) {
					xCoords[selectedIndex] = Double.parseDouble(value
							.toString());
				} else if (attr.equals(REND_Y)) {
					yCoords[selectedIndex] = Double.parseDouble(value
							.toString());
				} else if (attr.equals(BORDER_W)) {
					selectedNode.setBorderWidth(Float.parseFloat(value.toString()));
				} else if (attr.equals(LABEL_S)) {
					selectedNode.setLabelSize(Float.parseFloat(value.toString()));
				}
				control.log("modified attribute of node " + selectedNode.getNodeId()
						+ ", " + attr + "=" + value);
				fireTableCellUpdated(row, col);
				engine.updateDisplays();
			}
		}
	}
	
	/**
	 * class used to color in the color values in the editor
	 * 
	 * @author skyebend
	 * 
	 */
	private class ColorRender extends DefaultTableCellRenderer {

		public Component getTableCellRendererComponent(JTable table,
				Object color, boolean isSelected, boolean hasFocus,
				int row, int column) {
			setOpaque(true);
			Color newColor = (Color) color;
			setBackground(newColor);
			setText("r="+newColor.getRed()+",g="+newColor.getGreen()+",b="+newColor.getBlue());
			return this;
		}
	}

	public void mouseDragged(MouseEvent e) {
		
	}

	public void mouseMoved(MouseEvent e) {
		int found = getTarget(e.getX(), e.getY());
		if (found >= 0){
			//set the tool tip with the label of that node
			NodeAttribute node = (NodeAttribute)nodeEvents.get(found);
			canvas.setToolTipText(node.getNodeLabel());
			
		} else {
			canvas.setToolTipText(null);
		}
		
	}

}