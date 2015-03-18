package sonia.ui;

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

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.io.*;
import java.util.*;
import java.awt.color.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import sonia.SoniaController;

/**
 * Semi-independent window that logs all actions of a session for repeatability
 * includes ability to write contents out to text file independently
 * 
 * @TODO: Decouple logging from window
 */

public class LogWindow extends JPanel implements ActionListener {

	private static final long serialVersionUID = -1218915476685752230L;

	private SoniaController control;

	// instantiate window objects so they can be refered to
	private JTextArea LogText;

	private JButton writeLogFile;

	private JScrollPane scroller;

	public LogWindow(SoniaController theController) {
		control = theController;

		// make new font for the log window
		Font textFont = new Font("Monospaced ", Font.PLAIN, 10);

		// create layout objects
		LogText = new JTextArea(14, 0);
		LogText.setBackground(Color.white);
		LogText.setFont(textFont);
		LogText.setTabSize(1);
		LogText.setLineWrap(true);
		LogText.setWrapStyleWord(true);
		JPanel scrollPanel = new JPanel(new BorderLayout());
		scrollPanel.add(LogText,BorderLayout.CENTER);
		scroller = new JScrollPane(scrollPanel);

		writeLogFile = new JButton("Write Log to File...");

		writeLogFile.setFont(control.getFont());

		// LAYOUT
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);

		// add components to the layout GBlayout using constraints
		// buttons
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		add(scroller, c);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.1;
		c.fill = GridBagConstraints.NONE;
		add(writeLogFile, c);

		// add action listeners for button clicks
		writeLogFile.addActionListener(this);

		// setBackground(Color.lightGray);

		// construct frame
		//this.setSize(350, 250);
		this.setBorder(new TitledBorder("SoNIA Session Log"));
		this.setLocation(0, 290);
		this.setVisible(true);

	}

	// ACTION LISTENER //figures out what user did and calls apropriate method
	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals("Write Log to File...")) {
			writeLogToFile(null);
		}
	}

	/**
	 * Displays a save file dialog, opens an ouput stream, and writes out the
	 * contents of the log window to a text file with the name and location
	 * acquired from the file dialog.
	 */
	public void writeLogToFile(String logFileName) {
		// debug
		// System.out.println("writeLogToFile");
		// if no file name is included, get name and location for log file
		if (logFileName == null) {
			String promptString = "Please Choose location and name for log text file";
			String sugestFile = control.getFileName() + "log.txt";
			logFileName = control.getOutputFile(sugestFile, promptString);
		}
		// WRITE TEXT TO FILE -----------------------------
		// check if user canceled save dialog don't output data to file (but
		// still do to screen)

		if ((logFileName != null) && (control.getCurrentPath() != null)) {
			// create new file
			try {
				// give fileobject name and path from dialog
				File outfile = new File(control.getCurrentPath(), logFileName);
				// make new outputstream
				FileWriter outWriter = new FileWriter(outfile);
				// make new printwrinter
				PrintWriter outPrinter = new PrintWriter(new BufferedWriter(
						outWriter), true);
				outPrinter.print(LogText.getText());
				// close connection to output file
				outPrinter.flush();
				outPrinter.close();
				control.showStatus("Log Saved to " + control.getCurrentPath()
						+ logFileName);
			} catch (IOException error) {
				control.showStatus("ERROR: unable to save output file: "
						+ error.toString());
			}
		}
	}

	/**
	 * Appends the passed string into the log buffer for display or output to a
	 * text file.
	 */
	public void log(String text) {
		LogText.append("<> " + text + "\n\n");
		LogText.setCaretPosition(LogText.getText().length() - 1);
	}

}