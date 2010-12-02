package sonia.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import sonia.SoniaCanvas;
import sonia.SoniaLayoutEngine;
import sonia.mapper.Colormapper;
import sonia.mapper.DefaultColors;
import sonia.mapper.GrayscaleColors;
import sonia.mapper.RedtoBlueColors;

/**
 * Provides a UI for specifying mappings from node attributes to colors
 * 
 * @author skyebend
 * 
 */
@SuppressWarnings("serial")
public class ColorMapperPanel extends JPanel {
	protected Colormapper mapper;
	protected SoniaLayoutEngine engine;
	protected SoniaCanvas canvas;
	private Object editingValue = null;
	private Vector<Object> keyData;
	private JColorChooser colorChooser = new JColorChooser();
	private JPanel chooserPanel = new JPanel(new BorderLayout());
	private JPanel tablePanel = new JPanel(new BorderLayout());
	private JButton setButton = new JButton("<<- Assign Color");
	private JButton setAllButton = new JButton("Assign Color to All");
	private JTable table;
	private JComboBox keySelector;
	private JComboBox mappingSelector;
	/**
	 * All color mapper classes need to be listed here to show up in the UI
	 */
	private String[] knownMappers = new String[] { DefaultColors.MAPPER_NAME,
			GrayscaleColors.MAPPER_NAME,RedtoBlueColors.MAPPER_NAME };

	// ui components

	public ColorMapperPanel(SoniaCanvas canvas, SoniaLayoutEngine engine) {
		super(new BorderLayout());
		this.canvas = canvas;
		//get the list of mappers to choose from
		mappingSelector = new JComboBox(knownMappers);
		//get all the known data types to choose from
		keySelector = new JComboBox(engine.getNetData().getNodeDataKeys()
				.toArray());
		mapper = canvas.getColormapper();
		
		//if no mapper is set, use default, otherwise reload the mapper
		if (mapper == null) {
			setMapperTo(DefaultColors.MAPPER_NAME);
			mapper.createMapping(engine.getNetData().getUniqueNodeValues(
					(String) keySelector.getSelectedItem()));
		} else {
			mappingSelector.setSelectedItem(mapper.getMapperName());
			keySelector.setSelectedItem(mapper.getKey());
		}

		this.engine = engine;
		keyData = new Vector<Object>(mapper.getValues());

		table = new JTable(new ColorTableModel());
		table.setPreferredScrollableViewportSize(new Dimension(250, 50));
		table.setFillsViewportHeight(true);
		

		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);
		tablePanel.add(scrollPane);
		

		keySelector.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				refreshData();
			}
		});
		keySelector
				.setToolTipText("Select user data type to use for node color values");
		tablePanel.add(keySelector, BorderLayout.NORTH);
		scrollPane.setBorder(new TitledBorder("Value to color mapping"));

		JPanel mappingPanel = new JPanel();
		mappingPanel.setBorder(new TitledBorder("Type of color mapping"));
		mappingSelector.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				refreshData();
			}
		});
		mappingPanel.add(mappingSelector);
		JPanel mappingWrapper = new JPanel(new GridLayout(5, 1));
		mappingWrapper.add(mappingPanel);
		JPanel keyPanel = new JPanel();
		keyPanel.setBorder(new TitledBorder("Mapping data key"));
		keyPanel.add(keySelector);
		mappingWrapper.add(keyPanel);
		tablePanel.add(mappingWrapper, BorderLayout.WEST);

		// Set up renderer and editor for the Favorite Color column.
		table.setDefaultRenderer(Color.class, new ColorRenderer(true));
		ColorEditor editor = new ColorEditor();
		table.setDefaultEditor(Color.class, editor);
		setButton.addActionListener(editor);
		setAllButton.addActionListener(new ActionListener() {
			// loop over data elements and set them all to the current color
			@Override
			public void actionPerformed(ActionEvent e) {
				Color currentColor = colorChooser.getColor();
				Iterator valIter = mapper.getValues().iterator();
				while (valIter.hasNext()) {
					Object value = valIter.next();
					mapper.mapColor(value, currentColor);
				}
				table.repaint();

			}
		});
		setAllButton
				.setToolTipText("Set all data elemetns to the selected color");
		chooserPanel.add(colorChooser);
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(setButton);
		buttonPanel.add(setAllButton);
		chooserPanel.add(buttonPanel, BorderLayout.SOUTH);
		chooserPanel.setBorder(new TitledBorder(
				"Color chooser for selected data value"));
		// Add the scroll pane to this panel.
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				tablePanel, chooserPanel);
		add(split);

		//keyData = new Vector<Object>(mapper.getValues());
		refreshData();
	}

	public void refreshData() {
		// check if its different from the one we have
		if (!((String) mappingSelector.getSelectedItem()).equals(mapper
				.getMapperName())) {
			table.setRowSorter(null);
			setMapperTo((String) mappingSelector.getSelectedItem());
			mapper.createMapping(engine.getNetData().getUniqueNodeValues(
					(String) keySelector.getSelectedItem()));
		}
		mapper.setKey((String) keySelector.getSelectedItem());
		keyData.clear();
		keyData.addAll(mapper.getValues());
		RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table
				.getModel());
		table.setRowSorter(sorter);
		canvas.setColormapper(mapper);
		engine.getNetData().setNodeColormap(mapper);
		tablePanel.validate();
		table.repaint();
	}

	/**
	 * Factory method to create and store the mapper appropriate for the string
	 */
	private void setMapperTo(String mapperName) {
		if (mapperName.equals(DefaultColors.MAPPER_NAME)) {
			mapper = new DefaultColors();
		} else if (mapperName.equals(GrayscaleColors.MAPPER_NAME)) {
			mapper = new GrayscaleColors();
		} else if (mapperName.equals(RedtoBlueColors.MAPPER_NAME)) {
			mapper = new RedtoBlueColors();
		}
		
	}

	@SuppressWarnings("serial")
	private class ColorTableModel extends AbstractTableModel {
		private String[] columnNames = { "Data Value", "Color" };

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return keyData.size();
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			if (col == 0) {
				return keyData.get(row);
			} else if (col == 1) {

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

			if (getValueAt(0, c) != null) {
				return getValueAt(0, c).getClass();
			}
			return Object.class;
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
			if (col == 1) {

				fireTableCellUpdated(row, col);
			}
		}

	}

	/* Adapted from the java exmaple */
	private class ColorEditor extends AbstractCellEditor implements
			TableCellEditor, ActionListener {
		Color currentColor;
		JButton button;
		protected static final String EDIT = "edit";

		private ColorEditor() {
			button = new JButton();
			button.setActionCommand(EDIT);
			button.addActionListener(this);
			button.setBorderPainted(false);

		}

		/**
		 * Handles events from the editor button and from the dialog's OK
		 * button.
		 */
		public void actionPerformed(ActionEvent e) {
			if (EDIT.equals(e.getActionCommand())) {
				// The user has clicked the cell, so
				// button.setBackground(currentColor);
				colorChooser.setColor(currentColor);
				colorChooser.setVisible(true);

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

	public static JDialog showMapperWindow(JDialog owner, SoniaCanvas canvas,
			SoniaLayoutEngine eng) {
		final JDialog frame = new JDialog(owner, "Edit Color Mapping", true);
		// Create and set up the content pane.
		JComponent newContentPane = new ColorMapperPanel(canvas, eng);
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);
		JButton okbutton = new JButton("OK");
		okbutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);

			}
		});
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(okbutton);
		newContentPane.add(buttonPanel, BorderLayout.SOUTH);
		// Display the window.
		frame.pack();
		frame.setVisible(true);
		return frame;
	}

}
