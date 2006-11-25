package sonia.ui;

import org.freehep.util.export.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.geom.*;

import javax.swing.border.TitledBorder;
import javax.swing.event.*;

import java.awt.BorderLayout;

import org.freehep.util.UserProperties;

import org.freehep.graphicsio.gif.GIFExportFileType;
import org.freehep.graphicsio.jpg.JPGExportFileType;
import org.freehep.graphicsio.png.PNGExportFileType;
import org.freehep.graphicsio.ppm.PPMExportFileType;
import org.freehep.graphicsio.raw.RawExportFileType;
import org.freehep.graphicsio.cgm.CGMExportFileType;
import org.freehep.graphicsio.emf.EMFExportFileType;
import org.freehep.graphicsio.java.JAVAExportFileType;
import org.freehep.graphicsio.pdf.PDFExportFileType;
import org.freehep.graphicsio.ps.EPSExportFileType;
import org.freehep.graphicsio.ps.PSExportFileType;
import org.freehep.graphicsio.svg.SVGExportFileType;
import org.freehep.graphicsio.swf.SWFExportFileType;

/**
 * An "Export" dialog for saving components as graphic files.
 * 
 * @author tonyj, modified by eytan adar for GUESS, modified by skybend for
 *         sonia
 * @version $Id: HEPDialog.java,v 1.1 2006-11-25 01:26:31 skyebend Exp $
 */
public class HEPDialog extends JOptionPane {
	private static final String rootKey = HEPDialog.class.getName();

	private static final String SAVE_AS_TYPE = rootKey + ".SaveAsType";

	private static final String SAVE_AS_FILE = rootKey + ".SaveAsFile";

	private static ExportFileType epsEFT = null;

	private static ExportFileType psEFT = null;

	private static Vector list = new Vector();
	
	private String creator;

	private Rectangle2D originalSize = null;

	// private JButton browse = null;

	private JButton advanced = null;

	// private JTextField file = null;
	private String fileName = "imageExport";

	private JComboBox type = null;

	private Component component = null;

	private Component epsComponent = null;

	// private JComponent scaledComponent = null;

	private static double scaling = 1;

	private boolean trusted = true;

	private Properties props = new Properties();

	private static String baseDir = null;

	private JTextField scale = null;

	private JLabel imageSize = null;

	static {
		addAllExportFileTypes();
	}

	/**
	 * Set the Properties object to be used for storing/restoring user
	 * preferences. If not called user preferences will not be saved.
	 * 
	 * @param props
	 *            The Properties to use for user preferences
	 */
	public void setUserProperties(Properties properties) {
		props = properties;
	}

	/**
	 * Register an export file type.
	 */
	private static void addExportFileType(ExportFileType fileType) {
		list.addElement(fileType);
	}

	private static void addAllExportFileTypes() {
		// List exportFileTypes = ExportFileType.getExportFileTypes();
		// Collections.sort(exportFileTypes);
		// Iterator iterator = exportFileTypes.iterator();
		// while(iterator.hasNext()) {
		// ExportFileType type = (ExportFileType)iterator.next();
		// addExportFileType(type);
		// }
		addExportFileType(new JPGExportFileType());
		addExportFileType(new PNGExportFileType());
		addExportFileType(new PPMExportFileType());
		addExportFileType(new RawExportFileType());
		addExportFileType(new CGMExportFileType());
		addExportFileType(new EMFExportFileType());
		addExportFileType(new PDFExportFileType());
		epsEFT = new EPSExportFileType();
		addExportFileType(epsEFT);
		psEFT = new PSExportFileType();
		addExportFileType(psEFT);
		addExportFileType(new SVGExportFileType());
		addExportFileType(new SWFExportFileType());
		addExportFileType(new GIFExportFileType());
		addExportFileType(new JAVAExportFileType());
		// addExportFileType(new JPGExportFileType());
	}

	/**
	 * Creates a new instance of ExportDialog with all the standard export
	 * filetypes.
	 */
	public HEPDialog() {
		this(null);
	}

	public void updateSize() {
		double rescale = 1.0;
		try {
			rescale = Double.parseDouble(scale.getText());
		} catch (Exception ex) {
		}

		// System.out.println("os: " + originalSize);

		if (originalSize != null) {
			imageSize.setText("Output image size = "
					+ (int) (originalSize.getWidth() * rescale) + " x "
					+ (int) (originalSize.getHeight() * rescale) + " px");
		}
	}

	public JPanel createPanel() {
		JPanel jpanel1 = new JPanel();
		GridBagLayout gbLayout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		jpanel1.setLayout(gbLayout);

		// JLabel jlabel1 = new JLabel();
		// jlabel1.setText();
		// jpanel1.add(jlabel1, c);
		jpanel1.setBorder(new TitledBorder("Image settings:"));

		scale = new JTextField(3);
		scale.setName("scale");
		scale.setBorder(new TitledBorder("Scale"));
		scale.setText("" + scaling);
		jpanel1.add(scale, c);
		scale.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				updateSize();
			}
		});

		imageSize = new JLabel();
		imageSize.setText("Output image size = ");
		c.gridx = 1;
		c.gridy = 0;
		jpanel1.add(imageSize, c);

		type = new JComboBox(list);
		type.setName("type");
		type.setBorder(new TitledBorder("Image export format"));
		c.gridx = 0;
		c.gridy = 1;
		jpanel1.add(type, c);

		advanced = new JButton();
		advanced.setActionCommand("Options...");
		advanced.setName("advanced");
		advanced.setText("Options...");
		c.gridx = 1;
		c.gridy = 1;
		jpanel1.add(advanced, c);

		// file = new JTextField();
		// file.setName("file");
		// file.setBorder(new TitledBorder("File:"));
		// c.gridx = 0;c.gridy = 2;
		// jpanel1.add(file, c);

		// browse = new JButton();
		// browse.setActionCommand("Browse...");
		// browse.setName("browse");
		// browse.setText("Browse...");
		// c.gridx = 1;c.gridy = 2;
		// jpanel1.add(browse, c);

		// addFillComponents(jpanel1, new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 },
		// new int[] { 1, 2, 3, 4, 5, 6, 7, 8 });
		return jpanel1;
	}

	// void addFillComponents(Container panel, int[] cols, int[] rows) {
	// Dimension filler = new Dimension(10, 10);
	//
	// boolean filled_cell_11 = false;
	// GridBagConstraints c = new GridBagConstraints();
	// if (cols.length > 0 && rows.length > 0) {
	// if (cols[0] == 1 && rows[0] == 1) {
	// /** add a rigid area */
	// panel.add(Box.createRigidArea(filler), c);
	// filled_cell_11 = true;
	// }
	// }
	//
	// for (int index = 0; index < cols.length; index++) {
	// if (cols[index] == 1 && filled_cell_11) {
	// continue;
	// }
	// panel.add(Box.createRigidArea(filler), c.gridx = index);
	// }
	//
	// for (int index = 0; index < rows.length; index++) {
	// if (rows[index] == 1 && filled_cell_11) {
	// continue;
	// }
	// panel.add(Box.createRigidArea(filler), c.gridy = index);
	// }
	//
	// }

	/**
	 * Creates a new instance of ExportDialog with all the standard export
	 * filetypes.
	 * 
	 * @param creator
	 *            The "creator" to be written into the header of the file (may
	 *            be null)
	 */
	public HEPDialog(String creator) {

		super(null, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		this.creator = creator;

		try {
			if (baseDir == null)
				baseDir = System.getProperty("user.home");
			fileName = baseDir;
		} catch (SecurityException x) {
			trusted = false;
		}

		ButtonListener bl = new ButtonListener();

		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		panel.add(createPanel(), BorderLayout.CENTER);
		type.setMaximumRowCount(16); // rather than 8

		// browse.addActionListener(bl);
		advanced.addActionListener(bl);
		type.setRenderer(new SaveAsRenderer());
		type.addActionListener(bl);
		setMessage(panel);
		// if (addAllExportFileTypes) addAllExportFileTypes();
	}

	/**
	 * Show the dialog.
	 * 
	 * @param parent
	 *            The parent for the dialog
	 * @param title
	 *            The title for the dialog
	 * @param target
	 *            The component to be saved.
	 * @param defFile
	 *            The default file name to use.
	 */
	public void showHEPDialog(Component parent, String title, Component target,
			String defFile, Component t2) {
		// this. = target;
		this.component = null;
		this.epsComponent = t2;
		originalSize = new Rectangle2D.Double(0, 0, target.getWidth(), target
				.getHeight());
		scale.setEnabled(true);
		showHEPDialog(parent, title, defFile);
	}

	/**
	 * Show the dialog.
	 * 
	 * @param parent
	 *            The parent for the dialog
	 * @param title
	 *            The title for the dialog
	 * @param target
	 *            The component to be saved.
	 * @param defFile
	 *            The default file name to use.
	 */
	public void showHEPDialog(Component parent, String title, Component target,
			String defFile) {
		// this.jframe = null;

		this.component = target;
		scale.setEnabled(false);
		originalSize = new Rectangle2D.Double(0, 0, target.getWidth(), target
				.getHeight());
		scaling = 1.0;
		scale.setText("1.0");
		showHEPDialog(parent, title, defFile);

	}

	// what is this for?
	// class IComp extends Canvas {
	// BufferedImage bi = null;
	//
	// public IComp(BufferedImage bi) {
	// this.bi = bi;
	// setSize(bi.getWidth(), bi.getHeight());
	// JScrollPane sp = new JScrollPane(this);
	// JFrame f = new JFrame();
	// f.getContentPane().add(sp);
	// f.setSize(20, 20);
	// f.setVisible(true);
	// f.setVisible(false);
	// }
	//
	// public void paint(Graphics g) {
	// // ((Graphics2D)g).scale(.5,.5);
	// g.drawImage(bi, 0, 0, this);
	// }
	// }

	/**
	 * Show the dialog.
	 * 
	 * @param parent
	 *            The parent for the dialog
	 * @param title
	 *            The title for the dialog
	 * @param target
	 *            The component to be saved.
	 * @param defFile
	 *            The default file name to use.
	 */
	public void showHEPDialog(Component parent, String title, String defFile) {

		updateSize();

		if (list.size() > 0)
			type.setSelectedIndex(0);
		String dType = props.getProperty(SAVE_AS_TYPE);
		if (dType != null) {
			for (int i = 0; i < list.size(); i++) {
				ExportFileType saveAs = (ExportFileType) list.elementAt(i);
				if (saveAs.getFileFilter().getDescription().equals(dType)) {
					type.setSelectedItem(saveAs);
					break;
				}
			}
		}
		advanced.setEnabled(currentType() != null
				&& currentType().hasOptionPanel());
		if (trusted) {
			String saveFile = props.getProperty(SAVE_AS_FILE);
			if (saveFile != null) {
				baseDir = new File(saveFile).getParent();
				defFile = saveFile;
			} else {
				defFile = baseDir + File.separator + defFile;
			}
			File f = new File(defFile);
			if (currentType() != null)
				f = currentType().adjustFilename(f, props);
			fileName = f.toString();
		} else {
			// file.setEnabled(false);
			// browse.setEnabled(false);
		}

		JDialog dlg = createDialog(parent, title);
		dlg.pack();
		dlg.show();
	}

	private ExportFileType currentType() {
		return (ExportFileType) type.getSelectedItem();
	}

	/**
	 * Called to open a "file browser". Override this method to provide special
	 * handling (e.g. in a WebStart app)
	 * 
	 * @return The full name of the selected file, or null if no file selected
	 */
	protected boolean selectFile() {

		FileDialog locateOutput = new FileDialog(super.getRootFrame(),
				"Choose location to save file", FileDialog.SAVE);
		// throw up open dialog
		locateOutput.setSize(450, 300);
		locateOutput.setFile(fileName);
		locateOutput.setVisible(true);
		// blocks here to wait for users to choose directory and enter filename
		//check for user cancel
		if (locateOutput.getDirectory() == null
				|| locateOutput.getFile() == null) {
			return false;
		}
		fileName = locateOutput.getDirectory() + locateOutput.getFile();

		// JFileChooser dlg = new JFileChooser();
		// String f = file.getText();
		// if (f != null)
		// dlg.setSelectedFile(new File(f));
		// dlg.setFileFilter(currentType().getFileFilter());
		// if (dlg.showDialog(this, "Select") == dlg.APPROVE_OPTION) {

		return true;
		// } else {
		// return null;
		// }
	}

	/**
	 * Called to acually write out the file. Override this method to provide
	 * special handling (e.g. in a WebStart app)
	 * 
	 * @return true if the file was written, or false to cancel operation
	 */
	protected boolean writeFile(Component component, ExportFileType t)
			throws IOException {
		Dimension orig = null;
		// check if we are using the original or scaled version
		// if ((component == null) && (scaledComponent != null)) {
		// try {
		// scaling = Double.parseDouble(scale.getText());
		// } catch (Exception ex) {
		// }
		// if ((currentType() == epsEFT) || (currentType() == psEFT)) {
		// // jframe.center(new Integer(1),0);
		// component = scaledComponent;
		// try {
		// Thread.sleep(1000);
		// } catch (Exception ex) {
		// }
		// } else {
		// component = new IComp(scaledComponent.get(scaling));
		//				
		// }
		// }

		File f = new File(fileName);
//		if (f.exists()) {
//			int ok = JOptionPane.showConfirmDialog(this,
//					"Replace existing file?");
//			if (ok != JOptionPane.OK_OPTION)
//				return false;
//		}
		// debug
		System.out.println("Exporting file:" + f);
		//TODO: add way to comunicate this export message to controller
		t.exportToFile(f, component, this, props, creator);
		props.put(SAVE_AS_FILE, fileName);
		props.put(SAVE_AS_TYPE, currentType().getFileFilter().getDescription());
		baseDir = f.getParent();
		return true;
	}

	public void setValue(Object value) {
		if (value instanceof Integer
				&& ((Integer) value).intValue() == OK_OPTION) {
			// show the save dialog to get file and path and store them
			if (selectFile()) {
				try {
					if (!writeFile(component, currentType()))
						return;
				} catch (IOException x) {
					JOptionPane.showMessageDialog(this, x, "Error...",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
		}
		super.setValue(value);
	}



	private class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			// if (source == browse) {
			// String fileName = selectFile("test");
			// if (fileName != null) {
			// if (currentType() != null) {
			// File f = currentType().adjustFilename(
			// new File(fileName), props);
			// file.setText(f.getPath());
			// } else {
			// file.setText(fileName);
			// }
			// }
			// } else
			if (source == advanced) {
				JPanel panel = currentType().createOptionPanel(props);
				int rc = JOptionPane
						.showConfirmDialog(
								HEPDialog.this,
								panel,
								"Options for " + currentType().getDescription(),
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.PLAIN_MESSAGE);
				// deal with OK from the options dialog
				if (rc == JOptionPane.OK_OPTION) {
					currentType().applyChangedOptions(panel, props);
					if (fileName != null) {
						fileName = currentType().adjustFilename(
								new File(fileName), props).getAbsolutePath();
					}

					// if (!f1.equals(f2) && file.isEnabled())
					// file.setText(f2.toString());
				}
			} else if (source == type) {
				advanced.setEnabled(currentType().hasOptionPanel());
				File f1 = new File(fileName);
				fileName = currentType().adjustFilename(f1, props)
						.getAbsolutePath();

				// if (!f1.equals(f2))
				// file.setText(f2.toString());
			}
		}
	}

	private static class SaveAsRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			if (value instanceof ExportFileType) {
				this.setText(((ExportFileType) value).getFileFilter()
						.getDescription());
			}
			return this;
		}
	}
}
