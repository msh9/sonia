package sonia.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RectangularShape;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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

import sonia.ShapeFactory;
import sonia.SoniaCanvas;
import sonia.SoniaController;
import sonia.SoniaLayoutEngine;
import sonia.mapper.Colormapper;
import sonia.mapper.DefaultColors;
import sonia.mapper.DefaultShapes;
import sonia.mapper.GrayscaleColors;
import sonia.mapper.MapperFactory;
import sonia.mapper.RedtoBlueColors;
import sonia.mapper.Shapemapper;
import sonia.settings.ColormapperSettings;

/**
 * Provides a UI for specifying mappings from node attributes to shapes
 * 
 * @author skyebend
 * 
 */
@SuppressWarnings("serial")
public class ShapeMapperPanel extends JPanel {
	protected SoniaController control;
	protected Shapemapper mapper;
	protected SoniaLayoutEngine engine;
	protected SoniaCanvas canvas;
	private Object editingValue = null;
	private Vector<Object> keyData;
	
	private JPanel tablePanel = new JPanel(new BorderLayout());
	private JTable table;
	private JComboBox keySelector;
	private JComboBox mappingSelector;
	private JButton saveMapping;
	private JButton loadMapping;

	// ui components

	public ShapeMapperPanel(SoniaController cont, SoniaCanvas canvas,
			SoniaLayoutEngine engine) {
		super(new BorderLayout());
		this.control = cont;
		this.canvas = canvas;
		this.engine = engine;
		// get the list of mappers to choose from
		mappingSelector = new JComboBox(new String[]{DefaultShapes.MAPPER_NAME});
		// get all the known data types to choose from
		keySelector = new JComboBox(engine.getNetData().getNodeDataKeys()
				.toArray());
		mapper = canvas.getShapemapper();

		// if no mapper is set, use default, otherwise reload the mapper
		if (mapper == null) {
			mapper = new DefaultShapes();
			mapper.createMapping(engine.getNetData().getUniqueNodeValues(
					(String) keySelector.getSelectedItem()));
		} else {
			mappingSelector.setSelectedItem(mapper.getMapperName());
			keySelector.setSelectedItem(mapper.getKey());
		}

		keyData = new Vector<Object>(mapper.getValues());

		table = new JTable(new ShapeTableModel());
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
				.setToolTipText("Select user data type to use for node shape values");
		tablePanel.add(keySelector, BorderLayout.NORTH);
		scrollPane.setBorder(new TitledBorder("Value to shape mapping"));

		JPanel mappingPanel = new JPanel();
		mappingPanel.setBorder(new TitledBorder("Type of shape mapping"));
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

		// set up buttons to save and load mappings
		JPanel buttonWrapper = new JPanel();
		buttonWrapper.setLayout(new BoxLayout(buttonWrapper, BoxLayout.Y_AXIS));
		saveMapping = new JButton("Save mapping...");
		saveMapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			/*	Properties props = MapperFactory.asProperties(mapper);
				String filename = control.getOutputFile(
						"SoniaColormapperSettings.prp",
						"Choose location to save colormapper settings");
				String path = control.getCurrentPath(); // should have been set
														// by file dialog
				if (filename != null & path != null) {
					try {
						FileWriter propsOut = new FileWriter(path + filename);
						propsOut.write(props.toString());
						propsOut.close();
					} catch (FileNotFoundException e1) {
						control
								.showError("Unable to save colormapping to file "
										+ path
										+ filename
										+ " "
										+ e1.getMessage());
					} catch (IOException e1) {
						control
								.showError("Unable to save colormapping to file "
										+ path
										+ filename
										+ " "
										+ e1.getMessage());
					}
					control.showStatus("Saved color mapping settings to "
							+ path + filename);
				}
				*/
			}
		});
		buttonWrapper.add(saveMapping);

		loadMapping = new JButton("Reload mapping...");
		loadMapping.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
		/*		String filename = control.getInputFile(
						"SoniaColormapperSettings.prp",
						"Choose a colormapper settings file to load");
				String path = control.getCurrentPath();
				if (filename != null) {
					try {
						ColormapperSettings settings = new ColormapperSettings();
						FileInputStream inStream = new FileInputStream(
								new File(path + filename));
						settings.load(inStream);
						mapper = MapperFactory.restoreMapperFrom(settings);
						mappingSelector.setSelectedItem(mapper.getMapperName());
						keySelector.setSelectedItem(mapper.getKey());
						// TODO: what if the mapping doesn't match the data?
					} catch (FileNotFoundException e1) {
						control
								.showError("Error reading colormapper properties file: "
										+ e1.getMessage());
					} catch (IOException e1) {
						control
								.showError("Error reading colormapper properties file: "
										+ e1.getMessage());
					}
				}
*/
			}
		});
		buttonWrapper.add(loadMapping);
		mappingWrapper.add(buttonWrapper);

		// Set up renderer and editor for the Favorite Color column.
	//	table.setDefaultRenderer(Color.class, new ColorRenderer(true));
		NodeShapeEditor editor = new NodeShapeEditor();
		table.setDefaultEditor(RectangularShape.class, editor);
		
		add(tablePanel);

		// keyData = new Vector<Object>(mapper.getValues());
		refreshData();
	}

	public void refreshData() {
		// check if its different from the one we have

		
		if (!((String) mappingSelector.getSelectedItem()).equals(mapper
				.getMapperName()) || !keySelector.getSelectedItem().equals(mapper.getKey())) {
			table.setRowSorter(null);
			mapper = new DefaultShapes();
			mapper.createMapping(engine.getNetData().getUniqueNodeValues(
					(String) keySelector.getSelectedItem()));
		}
		
		mapper.setKey((String) keySelector.getSelectedItem());
		keyData.clear();
		keyData.addAll(mapper.getValues());
		RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table
				.getModel());
		table.setRowSorter(sorter);
		canvas.setShapemapper(mapper);
		engine.getNetData().setNodeShapemap(mapper);
		tablePanel.validate();
		table.repaint();
	}

	@SuppressWarnings("serial")
	private class ShapeTableModel extends AbstractTableModel {
		private String[] columnNames = { "Data Value", "Shape" };

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

				return ShapeFactory.getStringFor(mapper.getShapeFor(keyData.get(row)));
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

			if (c ==1){
				return RectangularShape.class;
			}
			return String.class;
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
				try {
					mapper.mapShape(value.toString(),  ShapeFactory.getShapeFor(value.toString()));
					//debug
					System.out.println("set value "+value);
				} catch (Exception e) {
					control.showError("The shape "+value.toString()+" could not be parsed by the shape factory");
				}
				
				fireTableCellUpdated(row, col);
			}
		}

	}

	

	public static JDialog showMapperWindow(JDialog owner, SoniaCanvas canvas,
			SoniaLayoutEngine eng, SoniaController control) {
		final JDialog frame = new JDialog(owner, "Edit Shape Mapping", true);
		// Create and set up the content pane.
		JComponent newContentPane = new ShapeMapperPanel(control, canvas, eng);
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
