package sonia;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.RectangularShape;
import java.awt.BorderLayout;
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

import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.EventObject;
import java.util.Locale;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import sonia.ui.NodeShapeEditor;

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
public class NodeInspector implements MouseListener, ChangeListener,
		MouseMotionListener {
	private SoniaController control;

	private SoniaLayoutEngine engine;

	// private JComponent frame;
	private SoniaCanvas canvas;

	private LayoutSlice slice;

	private double[] xCoords;

	private double[] yCoords;

	private Vector<NodeAttribute> nodeEvents;

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

	private TableCellRenderer basic;

	private TableCellRenderer color;

	private ColorEditor colorEdit;

	private JButton inspect;
	
	private NodeShapeEditor shapeEdit;

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

	public static final String FILE = "Orig. file path";

	public static final String ICON = "Icon URL";

	private String[] propkeys = new String[] { ID, LABEL, START, END, SIZE,
			REND_X, REND_Y, FILE_X, FILE_Y, COLOR, SHAPE, ICON, BORDER_W,
			BORDER_C, LABEL_S, LABEL_C, EFFECT, FILE };

	public NodeInspector(SoniaController cont, SoniaLayoutEngine eng,
			JComponent frm) {
		control = cont;
		engine = eng;
		// frame = frm;

		format = NumberFormat.getInstance(Locale.ENGLISH);
		format.setMaximumFractionDigits(3);
		format.setMinimumFractionDigits(3);
		

		// how for to translate the coords by (because of margins)
		left = engine.getLeftPad();
		top = engine.getTopPad();
		selectedIndex = -1;
		selectedNode = null;

		basic = new DefaultTableCellRenderer();
		color = new ColorRenderer(false);
		colorEdit = new ColorEditor();
		shapeEdit = new NodeShapeEditor();
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
		inspect.setText("Stop inspect");
		inspecting = true;

	}

	public void deactivate() {
		if (canvas != null) {
			canvas.removeMouseListener(this);
			canvas.removeMouseMotionListener(this);
		}
		if (selectedNode != null) {
			selectedNode.SetEffect(NodeAttribute.NO_EFFECT);
		}
		inspecting = false;
		inspect.setText("Inspect nodes");
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
		if (selectedNode != null) {
			selectedNode.SetEffect(NodeAttribute.NO_EFFECT);
		}
		// if overlap, will return last (topmost?) index
		for (int i = 0; i < nodeEvents.size(); i++) {
			NodeAttribute node = (NodeAttribute) nodeEvents.get(i);
			nodeSize = Math.max(3.0, node.getNodeSize() / 2.0); // always leave
			// at least 3 pixel for clicking

			int nodeIndex = node.getNodeId() - 1;
			// check for hit
			if (((x - left < xCoords[nodeIndex] + nodeSize) & (x - left > xCoords[nodeIndex]
					- nodeSize))
					& ((y - top < yCoords[nodeIndex] + nodeSize) & (y - top > yCoords[nodeIndex]
							- nodeSize))) {
				targetIndex = nodeIndex;
				selectedSize = nodeSize; // for later use by the graphics
				selectedNode = node;
				selectedNode.SetEffect(NodeAttribute.FLASH_EFFECT);
				// TODO: need to get the correctly scaled node size
			}

		}
		return targetIndex;
	}


	/**
	 * return a panel that will will show all the data for the node, and will be
	 * updated as things are clicked.
	 * 
	 * @author skyebend
	 * @return
	 */
	@SuppressWarnings("serial")
	public JPanel getInspectPanel() {
		if (inspectPanel == null) {
			// build the panel gui
			inspectPanel = new JPanel(new BorderLayout(5, 5));
			inspectPanel.setBorder(new TitledBorder("Node Attributes"));
			nodeData = getNodeDataComp(null);
			JTable nodeTable = new JTable(new AttributeTableModel()) {
				public TableCellRenderer getCellRenderer(int row, int column) {
					if (getValueAt(row, column) instanceof Color) {
						return color;

					} 
					// else...
					return basic;

				}

				public TableCellEditor getCellEditor(int row, int column) {
					if (getValueAt(row, column) instanceof Color) {
						return colorEdit;
					} else if (propkeys[row].equals(SHAPE)) {
						return shapeEdit;

					}
					return getDefaultEditor(getValueAt(row, column).getClass());
				}

			};

			nodeTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
			// nodeTable
			// .setPreferredScrollableViewportSize(new Dimension(250, 70));
			nodeTable.setFillsViewportHeight(true);

			nodeProps = new JScrollPane(nodeTable);

			inspectPanel.add(nodeProps, BorderLayout.CENTER);
			inspectPanel.add(nodeData, BorderLayout.EAST);
			inspect = new JButton("Inspect Nodes");
			inspect.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (inspecting) {
						deactivate();
					} else {
						activate();
					}
				}
			});
			inspect.setToolTipText("Inspect data of nodes on layout");
			JPanel buttonHolder = new JPanel();
			buttonHolder.add(inspect);
			inspectPanel.add(buttonHolder, BorderLayout.WEST);

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
		inspectPanel.add(nodeData, BorderLayout.EAST);
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
		

		//JTable dataTable = new JTable(keyAndValue, header);
		JTable dataTable = new JTable(new NodeDataTableModel(node));
		

		dataTable.setPreferredScrollableViewportSize(new Dimension(120, 70));
		JScrollPane scroller = new JScrollPane(dataTable);
		// scroller.setBorder(new TitledBorder("Attached data"));
		return scroller;
	}
	
	/**
	 * provides a table model for editing a node's user data. 
	 * @author skyebend
	 *
	 */
	@SuppressWarnings("serial")
	private class NodeDataTableModel extends AbstractTableModel {
		
		private NodeAttribute node;
		private Object[] keys;
		public NodeDataTableModel(NodeAttribute node){
			this.node = node;
			if (node != null && node.getDataKeys() != null) {
				keys = node.getDataKeys().toArray();

			} else {
				keys = new String[]{"<none>"};
			}
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0){
				return keys[rowIndex];
			} else if (columnIndex == 1 && node != null){
				return node.getData(keys[rowIndex]);
			}
			return "<none>"; //otherwise something is really wrong
		}
		
		@Override
		public int getRowCount() {
			return keys.length;
		}
		
		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex ==1){
				return true;
			}
			return false;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex ==1){
				node.setData((String)keys[rowIndex], aValue);
			}
		}

		@Override
		public String getColumnName(int column) {
			if (column == 0){
				return "Data Key";
			} else if (column ==1){
				return "Value";
			}
			return null;
		}
		
		
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
						return ShapeFactory.getStringFor(selectedNode.getNodeShape());
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
						URL url = selectedNode.getIconURL();
						if (url != null){
							return url.getFile();
						}
						return "";
					} else {
						return null;
					}
				}

			} else {
				return null;
			}
		}

	

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
				} else if (attr.equals(COLOR)) {
					return true;
				} else if (attr.equals(BORDER_C)) {
					return true;
				} else if (attr.equals(LABEL_C)) {
					return true;
				} else if (attr.equals(ICON)) {
					return true;
				} else if (attr.equals(SHAPE)){
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
					selectedNode.setNodeSize(Double.parseDouble(value
							.toString()));
				} else if (attr.equals(REND_X)) {
					xCoords[selectedIndex] = Double.parseDouble(value
							.toString());
				} else if (attr.equals(REND_Y)) {
					yCoords[selectedIndex] = Double.parseDouble(value
							.toString());
				} else if (attr.equals(BORDER_W)) {
					selectedNode.setBorderWidth(Float.parseFloat(value
							.toString()));
				} else if (attr.equals(LABEL_S)) {
					selectedNode.setLabelSize(Float
							.parseFloat(value.toString()));
				} else if (attr.equals(SHAPE))	{
					try {
						selectedNode.setNodeShape(ShapeFactory.getShapeFor(value.toString()));
					} catch (Exception e) {
						control.showError("The shape "+value.toString()+" could not be parsed by the shape factory");
					}
				} else if (attr.equals(ICON)) {
					try {
						URL url = null;
						if (!value.toString().equals("")){
							url = new URL(value.toString());
						}
						selectedNode.setIconURL(url);
					} catch (MalformedURLException e) {
						control
								.showError("The URL " + value.toString()
										+ " appears to be malformed: "
										+ e.getMessage());
					} catch (Exception e) {
						control
								.showError("There was a problem parsing the URL '"
										+ value.toString()
										+ "': "
										+ e.getMessage());
					}
				} else if (attr.equals(COLOR)) {
					selectedNode.setNodeColor(((Color) value));
				} else if (attr.equals(LABEL_C)) {
					selectedNode.setLabelColor(((Color) value));
				} else if (attr.equals(BORDER_C)) {
					selectedNode.setBorderColor(((Color) value));
				}
				control.log("modified attribute of node id "
						+ selectedNode.getNodeId() + ", " + attr + "=" + value);
				fireTableCellUpdated(row, col);
				engine.updateDisplays();
			}
		}
	}



	/*
	 * The ColorRenderer inner class uses code from http://www.java2s.com/Code/Java/Swing-JFC/Tablewithacustomcellrendererandeditorforthecolordata.htm Originally from
	 * http://java.sun.com/docs/books/tutorial/index.html 
	 * Copyright (c) 2006
	 * Sun Microsystems, Inc. All Rights Reserved.
	 * 
	 * Redistribution and use in source and binary forms, with or without
	 * modification, are permitted provided that the following conditions are
	 * met:
	 * 
	 * -Redistribution of source code must retain the above copyright notice,
	 * this list of conditions and the following disclaimer.
	 * 
	 * -Redistribution in binary form must reproduce the above copyright notice,
	 * this list of conditions and the following disclaimer in the documentation
	 * and/or other materials provided with the distribution.
	 * 
	 * Neither the name of Sun Microsystems, Inc. or the names of contributors
	 * may be used to endorse or promote products derived from this software
	 * without specific prior written permission.
	 * 
	 * This software is provided "AS IS," without a warranty of any kind. ALL
	 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
	 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
	 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
	 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
	 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR
	 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY
	 * LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
	 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
	 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
	 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
	 * POSSIBILITY OF SUCH DAMAGES.
	 * 
	 * You acknowledge that this software is not designed, licensed or intended
	 * for use in the de ColorRenderer.java (compiles with releases 1.2, 1.3,
	 * and 1.4) is used by TableDialogEditDemo.java.
	 */

	class ColorRenderer extends JLabel implements TableCellRenderer {
		Border unselectedBorder = null;
		Border selectedBorder = null;
		boolean isBordered = true;

		public ColorRenderer(boolean isBordered) {
			this.isBordered = isBordered;
			setOpaque(true); // MUST do this for background to show up.
		}

		public Component getTableCellRendererComponent(JTable table,
				Object color, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Color newColor = (Color) color;
			setBackground(newColor);
			if (isBordered) {
				if (isSelected) {
					if (selectedBorder == null) {
						selectedBorder = BorderFactory.createMatteBorder(2, 5,
								2, 5, table.getSelectionBackground());
					}
					setBorder(selectedBorder);
				} else {
					if (unselectedBorder == null) {
						unselectedBorder = BorderFactory.createMatteBorder(2,
								5, 2, 5, table.getBackground());
					}
					setBorder(unselectedBorder);
				}
			}

			setToolTipText("RGB value: " + newColor.getRed() + ", "
					+ newColor.getGreen() + ", " + newColor.getBlue());
			return this;
		}
	}
	
	/*
	 * The ColorEditor inner class uses code from http://www.java2s.com/Code/Java/Swing-JFC/Tablewithacustomcellrendererandeditorforthecolordata.htm Originally from
	 * http://java.sun.com/docs/books/tutorial/index.html 
	 * Copyright (c) 2006
	 * Sun Microsystems, Inc. All Rights Reserved.
	 * 
	 * Redistribution and use in source and binary forms, with or without
	 * modification, are permitted provided that the following conditions are
	 * met:
	 * 
	 * -Redistribution of source code must retain the above copyright notice,
	 * this list of conditions and the following disclaimer.
	 * 
	 * -Redistribution in binary form must reproduce the above copyright notice,
	 * this list of conditions and the following disclaimer in the documentation
	 * and/or other materials provided with the distribution.
	 * 
	 * Neither the name of Sun Microsystems, Inc. or the names of contributors
	 * may be used to endorse or promote products derived from this software
	 * without specific prior written permission.
	 * 
	 * This software is provided "AS IS," without a warranty of any kind. ALL
	 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
	 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
	 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
	 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
	 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR
	 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY
	 * LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
	 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
	 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
	 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
	 * POSSIBILITY OF SUCH DAMAGES.
	 * 
	 * You acknowledge that this software is not designed, licensed or intended
	 * for use in the de ColorRenderer.java (compiles with releases 1.2, 1.3,
	 * and 1.4) is used by TableDialogEditDemo.java.
	 */
	class ColorEditor extends AbstractCellEditor implements TableCellEditor,
			ActionListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		Color currentColor;
		JButton button;
		JColorChooser colorChooser;
		JDialog dialog;
		protected static final String EDIT = "edit";

		public ColorEditor() {
			// Set up the editor (from the table's point of view),
			// which is a button.
			// This button brings up the color chooser dialog,
			// which is the editor from the user's point of view.
			button = new JButton();
			button.setActionCommand(EDIT);
			button.addActionListener(this);
			button.setBorderPainted(false);

			// Set up the dialog that the button brings up.
			colorChooser = new JColorChooser();
			dialog = JColorChooser.createDialog(button, "Pick a Color", true, // modal
					colorChooser, this, // OK button handler
					null); // no CANCEL button handler
		}

		/**
		 * Handles events from the editor button and from the dialog's OK
		 * button.
		 */
		public void actionPerformed(ActionEvent e) {
			if (EDIT.equals(e.getActionCommand())) {
				// The user has clicked the cell, so
				// bring up the dialog.
				button.setBackground(currentColor);
				colorChooser.setColor(currentColor);
				dialog.setVisible(true);

				// Make the renderer reappear.
				fireEditingStopped();

			} else { // User pressed dialog's "OK" button.
				currentColor = colorChooser.getColor();
			}
		}

		// Implement the one CellEditor method that AbstractCellEditor doesn't.
		public Object getCellEditorValue() {
			return currentColor;
		}

		// Implement the one method defined by TableCellEditor.
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			currentColor = (Color) value;
			return button;
		}
	}

	public void mouseDragged(MouseEvent e) {

	}

	public void mouseMoved(MouseEvent e) {
		int found = getTarget(e.getX(), e.getY());
		if (found >= 0) {
			// set the tool tip with the label of that node
			NodeAttribute node = (NodeAttribute) nodeEvents.get(found);
			canvas.setToolTipText(node.getNodeLabel());

		} else {
			canvas.setToolTipText(null);
		}

	}

}