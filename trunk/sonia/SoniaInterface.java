package sonia;

import java.awt.*;
import java.awt.font.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.awt.color.*;

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

public class SoniaInterface extends Frame implements WindowListener,
		ActionListener
// ItemListener
{
	private SoniaController control;

	// instantiate window objects so they can be refered to
	private Label Credits;

	private Button LoadButton;

	private Button LayoutButton;

	private Button PauseButton;

	private Button MovieButton;

	private Button SaveButton;

	private TextArea StatusText;

	private Font msgFont;

	private Font errorFont;

	private String codeDate = "09/15/04";

	private String version = "1.1";

	public SoniaInterface(SoniaController theController) {
		control = theController;

		this.setFont(control.getFont());
		// create layout objects
		Credits = new Label("SoNIA is free for non-comercial, non-military use");
		LoadButton = new Button("Load Files...");
		LayoutButton = new Button("Create Layout...");
		PauseButton = new Button("Pause");
		MovieButton = new Button("Export Movie ...");
		SaveButton = new Button("Save to File...");
		StatusText = new TextArea(
				"   Welcome to SoNIA "
						+ version
						+ " (code date "
						+ codeDate
						+ ")\n"
						+ "   Please view the README and SoNIAWriteup for instructions\n"
						+ "   Questions/bugs to skyebend@stanford.edu", 5, 50,
				TextArea.SCROLLBARS_NONE);
		StatusText.setBackground(Color.white);
		StatusText.setEditable(false);

		// LAYOUT
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);

		// add components to the layout GBlayout using constraints
		// buttons
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 4;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.1;
		add(StatusText, c);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.1;
		add(LoadButton, c);
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.1;
		add(LayoutButton, c);
		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.1;
		add(PauseButton, c);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.1;
		add(MovieButton, c);
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.1;
		add(SaveButton, c);
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 4;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.1;
		add(Credits, c);

		// add action listeners for button clicks
		LoadButton.addActionListener(this);
		LayoutButton.addActionListener(this);
		PauseButton.addActionListener(this);
		SaveButton.addActionListener(this);
		MovieButton.addActionListener(this);

		addWindowListener(this);
		setBackground(Color.lightGray);

		// construct frame
		this.setSize(350, 250);
		this.setTitle("SoNIA (Social Network Image Animator) v1.0");
		this.setVisible(true);
		LoadButton.requestFocus();
		this.repaint();
	}

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
			control.createLayout();
		} else if (evt.getSource().equals(SaveButton)) {
			// check that network exisits?
			if (control.hasNetworks()) {
//				 allow choosing which net to export?
				control.exportMatricies();
			}
			else
			{
				showError("No networks have been created");
			}
			
		} else if (evt.getSource().equals(MovieButton)) {
			// check that network exisits?
			if (control.hasNetworks()) {
				// allow choosing which net to export?
				control.exportMovie();
			} else {
				showError("No networks have been created");
			}
		}
	}

	// WINDOW LISTENERS windowClosing exits system when close box is clicked
	// the rest have to be there to satisfy WindowListener
	/**
	 * Prompts with an "are you sure" dialog before exiting sonia.
	 */
	public void windowClosing(WindowEvent evt) {
		// should be more gentle, ask to save and such
		OptionPrompter saveDialog = new OptionPrompter(this,
				"Exiting SoNIA will discard all unsaved layouts",
				"Are you sure you want to quit SoNIA?");
		boolean result = saveDialog.getResult();
		if (result == true) {
			System.exit(0);
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
