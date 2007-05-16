package sonia.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import sonia.SoniaLayoutEngine;
import sonia.mapper.Colormapper;
import sonia.mapper.DefaultColors;

public class ColorMapperPanel extends JPanel {
	private Colormapper mapper;
	protected SoniaLayoutEngine engine;
	private Object editingValue = null;
	private Vector<Object> keyData;
	private JColorChooser colorChooser = new JColorChooser();
	private JPanel chooserPanel = new JPanel(new BorderLayout());
	private JPanel tablePanel = new JPanel(new BorderLayout());
	private JButton setButton = new JButton("<<- Assign Color");
	private JTable table;
	private JComboBox keySelector; 
	
	// ui components

	public ColorMapperPanel(Colormapper map,SoniaLayoutEngine engine) {
		super(new GridLayout(0, 1));
		mapper = map;
		this.engine = engine;
		keyData = new Vector<Object>(mapper.getValues());
		
		table = new JTable(new ColorTableModel());
		table.setPreferredScrollableViewportSize(new Dimension(500, 70));
		// table.setFillsViewportHeight(true);

		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);
		tablePanel.add(scrollPane);
		keySelector  = new JComboBox(engine.getNetData().getNodeDataKeys().toArray());
		
		keySelector.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				refreshData();	
			}
		});
		tablePanel.add(keySelector,BorderLayout.NORTH);
		tablePanel.setBorder(new TitledBorder("Data name (key) and color mapping"));

		// Set up renderer and editor for the Favorite Color column.
		table.setDefaultRenderer(Color.class, new ColorRenderer(true));
		ColorEditor editor = new ColorEditor();
		table.setDefaultEditor(Color.class, editor);
		setButton.addActionListener(editor);
		chooserPanel.add(colorChooser);
		chooserPanel.add(setButton,BorderLayout.SOUTH);
		chooserPanel.setBorder(new TitledBorder("Color chooser for selected data value"));
		// Add the scroll pane to this panel.
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,tablePanel,chooserPanel);
		add(split);
		
		keyData = new Vector<Object>(mapper.getValues());
	}
	
	public void refreshData(){
		mapper.createMapping(engine.getNetData().getUniqueNodeValues((String)keySelector.getSelectedItem()));
		keyData.clear();
		keyData.addAll(mapper.getValues());
		table.repaint();
	}

	private class ColorTableModel extends AbstractTableModel {
		private String[] columnNames = { "Data Value", "Color" };

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return mapper.getValues().size();
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			if (col == 0){
				return keyData.get(row);
			} else if (col ==1){
				
				return mapper.getColorFor(keyData.get(row));
			} else {
				return null;
			}
		}

		/*
		 * JTable uses this method to determine the default renderer/ editor for
		 * each cell. If we didn't implement this method, then the last column
		 * would contain text ("true"/"false"), rather than a check box.
		 */
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public boolean isCellEditable(int row, int col) {
			// Note that the data/cell address is constant,
			// no matter where the cell appears onscreen.
			if (col < 1) {
				return false;
			} else {
				return true;
			}
		}

		public void setValueAt(Object value, int row, int col) {
			if (col == 1){
				
				fireTableCellUpdated(row, col);
			}
		}

	}

	private class ColorEditor extends AbstractCellEditor implements
			TableCellEditor, ActionListener {
		Color currentColor;

		JButton button;

		//JColorChooser colorChooser;

		//JDialog dialog;

		protected static final String EDIT = "edit";

		private ColorEditor() {
			// Set up the editor (from the table's point of view),
			// which is a button.
			// This button brings up the color chooser dialog,
			// which is the editor from the user's point of view.
			button = new JButton();
			button.setActionCommand(EDIT);
			button.addActionListener(this);
			button.setBorderPainted(false);

			// Set up the dialog that the button brings up.
			//colorChooser = new JColorChooser();
			
			//dialog = JColorChooser.createDialog(button, "Pick a new Color", true, // modal
			//		colorChooser, this, // OK button handler
			//		null); // no CANCEL button handler
		}

		/**
		 * Handles events from the editor button and from the dialog's OK
		 * button.
		 */
		public void actionPerformed(ActionEvent e) {
			if (EDIT.equals(e.getActionCommand())) {
				// The user has clicked the cell, so
				// bring up the dialog.
				//button.setBackground(currentColor);
				colorChooser.setColor(currentColor);
				colorChooser.setVisible(true);
				//dialog.setVisible(true);

				// Make the renderer reappear.
				fireEditingStopped();

			} else { // User pressed dialog's "OK" button.
				currentColor = colorChooser.getColor();
				mapper.mapColor(editingValue, currentColor);
				table.repaint();
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
			editingValue = keyData.get(row);
			return button;
		}
	}

	private class ColorRenderer extends JLabel implements TableCellRenderer {
		Border unselectedBorder = null;

		Border selectedBorder = null;

		boolean isBordered = false;

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
	
	public static JFrame showMapperWindow(Colormapper mapper, SoniaLayoutEngine eng){
		JFrame frame = new JFrame("Edit Color Mapping");
		 //Create and set up the content pane.
        JComponent newContentPane = new ColorMapperPanel(mapper,eng);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
        return frame;
	}
	
	/**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("TableDialogEditDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  //mapper
         HashSet<Object> testData = new HashSet<Object>();
        testData.add("a");
        testData.add("b");
        testData.add("colorz drive me crazy");
        testData.add("d");
        Colormapper map = new DefaultColors();
        map.createMapping(testData);
        
        //Create and set up the content pane.
        JComponent newContentPane = new ColorMapperPanel(map,null);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }


}
