package sonia.ui;

import java.awt.*;
import java.awt.font.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.*;
import java.util.*;
import java.awt.color.*;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;

import sonia.SoniaController;

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
 * Handles gui and UI for SoniaControler and and calls all methods via
 * controller. Message window is used to provide updates on ongoing processes or
 * errors. Buttons are as follows:
 * <P>
 * Load Files.. - Brings up an open file dialog for chosing input file, uses
 * .net or .son parser depending on file extension
 * </P>
 * <P>
 * Create Layout.. Makes a new layout window, and brings up the
 * LayoutSettingsDialog for specifing bining etc.
 * </P>
 * <P>
 * Export Movie.. - Brings up a dialog to choose which layout to export, and
 * then a save dialog, then plays through the layout recording frames into a
 * QuickTime movie file.
 * </P>
 * <P>
 * Save to File.. - At this point, it just brings up a dialog asking which
 * layout to save, a save file dialog, and saves the matricies from each slice
 * into a text file.
 * </P>
 * <P>
 * Pause - attempts to interrupt ongoing processes
 * </P>
 */

public class SoniaInterface extends JFrame implements WindowListener,
		ActionListener
// ItemListener
{
	private SoniaController control;

	// instantiate window objects so they can be refered to
	private JLabel Credits;

	private JButton LoadButton;

	private JButton LayoutButton;

	private JButton PauseButton;

	private JButton MovieButton;

	// private JButton SaveButton;

	private JTextArea StatusText;

	private JDesktopPane workPane;

	private JPanel menuPane;

	private Font msgFont;

	private Font errorFont;

	public SoniaInterface(SoniaController theController, boolean show) {
		control = theController;
		changeAllFonts(new FontUIResource(control.getFont()));
		Image soniaIcon = Toolkit.getDefaultToolkit().getImage(
				control.getClass().getResource("image/soniaLogo16.jpg"));
		super.setIconImage(soniaIcon);

		super.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		// create layout objects
		Credits = new JLabel(
				"SoNIA is free for non-comercial, non-military use");
		LoadButton = new JButton("Load Files...");
		LayoutButton = new JButton("Create Layout...");
		PauseButton = new JButton("Pause");
		MovieButton = new JButton("Export Movie ...");
		// SaveButton = new JButton("Save to File...");
		StatusText = new JTextArea(
				"   Welcome to SoNIA "
						+ SoniaController.VERSION
						+ " (code date "
						+ SoniaController.CODE_DATE
						+ ")\n"
						+ "   For help and information, please visit http://sonia.stanford.edu"
						+ "  or send questions/bugs to sonia-users@lists.sourceforge.net",
				1, 50);
		// StatusText.setBackground(Color.white);
		StatusText.setEditable(false);
		StatusText.setBorder(new CompoundBorder(new BevelBorder(
				BevelBorder.LOWERED), new TitledBorder("Status:")));
		StatusText.setLineWrap(true);
		StatusText.setWrapStyleWord(true);
		// StatusText.setColumns(30);

		workPane = new JDesktopPane();
		workPane.setBorder(new TitledBorder("Layouts:"));
		workPane.setSize(300, 250);
		workPane.setVisible(true);

		// LAYOUT
		GridBagLayout layout = new GridBagLayout();
		menuPane = new JPanel(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.anchor = GridBagConstraints.WEST;

		// add components to the layout GBlayout using constraints
		// buttons
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		menuPane.add(StatusText, c);
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.1;
		menuPane.add(LoadButton, c);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.1;
		menuPane.add(LayoutButton, c);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.1;
		menuPane.add(PauseButton, c);
		// c.gridx = 0;
		// c.gridy = 2;
		// c.gridwidth = 1;
		// c.gridheight = 1;
		// c.weightx = 0.1;
		// c.weighty = 0.1; // movie is now a menu item on the layout window
		// menuPane.add(MovieButton, c);
		// c.gridx = 1;
		// c.gridy = 2;
		// c.gridwidth = 1;
		// c.gridheight = 1;
		// c.weightx = 0.1;
		// c.weighty = 0.1;
		// menuPane.add(SaveButton, c);
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		menuPane.add(control.getLogRef(), c);

		// add action listeners for button clicks
		LoadButton.addActionListener(this);
		LayoutButton.addActionListener(this);
		PauseButton.addActionListener(this);
		// SaveButton.addActionListener(this);
		// MovieButton.addActionListener(this);

		getContentPane().setLayout(new BorderLayout());
		JSplitPane jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, menuPane,
				workPane);
		jsp.setOneTouchExpandable(true);
		jsp.setDividerLocation(250);
		getContentPane().add(jsp, BorderLayout.CENTER);
		// add(workPane, BorderLayout.CENTER);
		// add(menuPane, BorderLayout.WEST);
		getContentPane().add(StatusText, BorderLayout.SOUTH);

		addWindowListener(this);
		// setBackground(Color.lightGray);

		// debug test the jinternal frame
		// JInternalFrame frameTest = new JInternalFrame("dummy network");
		// frameTest.setSize(300, 200);
		// workPane.add(frameTest);
		// frameTest.setVisible(true);

		// construct frame
		this.setSize(850, 650);
		this.setTitle("SoNIA v" + SoniaController.VERSION);
		this.setVisible(show);
		LoadButton.requestFocus();
		this.repaint();

	}

	public void addFrame(JInternalFrame toShow) {
		toShow.setVisible(true);
		workPane.add(toShow);
		workPane.getDesktopManager().activateFrame(toShow);
	}

	// protected JComponent getGraphicContent() {
	// // TODO Auto-generated method stub
	// return super.getGraphicContent();
	// }

	/**
	 * Displays the passed text as a message in the status window
	 */
	public void showStatus(String text) {
		this.setForeground(Color.black);
		StatusText.setText("");
		StatusText.setText("  " + text);
		StatusText.setCaretPosition(0);
	}

	/**
	 * Displays the passed text as an error message in the status window, may
	 * beep and/or turn red/
	 */
	public void showError(String text) {
		(Toolkit.getDefaultToolkit()).beep();
		this.setBackground(Color.red);
		this.setForeground(Color.red);
		StatusText.setText("");
		StatusText.setText("  " + text);
		StatusText.setCaretPosition(0);
		repaint();
		this.setBackground(Color.lightGray);
		this.setForeground(Color.black);
		repaint();
	}

	/**
	 * Throws up a save file dialog and, returns a string with path and filename
	 * for saving files
	 */
	public String getOutputFileName(String fname, String message) {
		FileDialog locateOutput = new FileDialog(this, message, FileDialog.SAVE);
		// throw up open dialog
		locateOutput.setSize(450, 300);
		locateOutput.setFile(fname);
		locateOutput.setDirectory(control.getCurrentPath());
		locateOutput.setVisible(true);
		String outputFileName = locateOutput.getFile();
		String outputPath = locateOutput.getDirectory();
		control.setCurrentPath(outputPath);
		return outputFileName;
	}

	/**
	 * throws up an open file dialog for choosing files to load.
	 */
	public String getInputFileName(String fname, String message) {
		FileDialog locateInput = new FileDialog(this, message, FileDialog.LOAD);
		// throw up open dialog
		locateInput.setSize(450, 300);
		locateInput.setFile(fname);
		locateInput.setDirectory(control.getCurrentPath());
		locateInput.setVisible(true);
		String inputFileName = locateInput.getFile();
		String inputPath = locateInput.getDirectory();
		control.setCurrentPath(inputPath);
		return inputFileName;
	}

	// ACTION LISTENER //figures out what user did and calls apropriate method
	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals("Load Files...")) {
			control.loadNetFromFile();
			LayoutButton.requestFocus();
		} else if (evt.getActionCommand().equals("Pause")) {
			control.setPaused(true);
			PauseButton.setLabel("Resume");
		} else if (evt.getActionCommand().equals("Resume")) {
			control.setPaused(false);
			PauseButton.setLabel("Pause");
		} else if (evt.getActionCommand().equals("Create Layout...")) {
			// should launch this on a new thread?
			control.createNewLayout();
			// } else if (evt.getSource().equals(SaveButton)) {
			// // check that network exisits?
			// if (control.hasNetworks()) {
			// // allow choosing which net to export?
			// control.exportMatricies();
			// } else {
			// showError("No networks have been created");
			// }

			// } else if (evt.getSource().equals(MovieButton)) {
			// // check that network exisits?
			// if (control.hasNetworks()) {
			// // allow choosing which net to export?
			// control.exportMovie();
			// } else {
			// showError("No networks have been created");
			// }
		}
	}

	// WINDOW LISTENERS windowClosing exits system when close box is clicked
	// the rest have to be there to satisfy WindowListener
	/**
	 * Prompts with an "are you sure" dialog before exiting sonia.
	 */
	public void windowClosing(WindowEvent evt) {
		// should be more gentle, ask to save and such

		int result = JOptionPane.showConfirmDialog(this,
				"Exiting SoNIA will discard all unsaved layouts",
				" Are you sure you want to quit SoNIA?",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if (result == 0) {
			System.exit(0);
		}

	}

	private static void changeAllFonts(FontUIResource f) {
		//
		// sets the default font for all Swing components.

		Enumeration keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource)
				UIManager.put(key, f);
		}
	}

	public void windowActivated(WindowEvent evt) {
	}

	public void windowClosed(WindowEvent evt) {
	}

	public void windowDeactivated(WindowEvent evt) {
	}

	public void windowDeiconified(WindowEvent evt) {
	}

	public void windowIconified(WindowEvent evt) {
	}

	public void windowOpened(WindowEvent evt) {
	}
}
