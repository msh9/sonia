package sonia;

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

/**
 * Semi-independent window that logs all actions of a session for repeatability
 * includes ability to write contents out to text file independently
 */

public class LogWindow extends Frame implements ActionListener, WindowListener
{

private SoniaController control;
 //instantiate window objects so they can be refered to
 private TextArea LogText;
 private Button WriteLogFile;

 public LogWindow(SoniaController theController)
 {
   control = theController;

   //make new font to help keep layouts consitant across platforms
    Font textFont = new Font("Monospaced ",Font.PLAIN,10);
    this.setFont(textFont);

   //create layout objects
   LogText = new TextArea(14,50);
   LogText.setBackground(Color.white);
   WriteLogFile = new Button("Write Log to File...");
   WriteLogFile.setFont(control.getFont());

   //LAYOUT
   GridBagLayout layout = new GridBagLayout();
   setLayout(layout);
   GridBagConstraints c = new GridBagConstraints();
   c.insets = new Insets(2,2,2,2);

   // add components to the layout GBlayout using constraints
   //buttons
   c.gridx=0;c.gridy=0;c.gridwidth=1;c.gridheight=1;c.weightx=0.1;c.weighty=0.1;
   add(LogText,c);
   c.gridx=0;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=0.1;c.weighty=0.1;
   add(WriteLogFile,c);

   //add action listeners for button clicks
   WriteLogFile.addActionListener(this);

   addWindowListener(this);
   setBackground(Color.lightGray);

   //construct frame
   this.setSize (350,250);
   this.setTitle ("SoNIA Session Log");
   this.setLocation(0,290);
   this.setVisible (true);




 }

 //ACTION LISTENER  //figures out what user did and calls apropriate method
 public void actionPerformed(ActionEvent evt)
 {
   if (evt.getActionCommand().equals("Write Log to File..."))
   {
     writeLogToFile();
   }
 }
/**
 * Displays  a save file dialog, opens an ouput stream,  and writes out the
 * contents of the log window to a text file with the name and location acquired
 * from the file dialog.
 */
 public void writeLogToFile()
 {
   //debug
   //System.out.println("writeLogToFile");
   //get name and location for log file
   String promptString = "Please Choose location and name for log text file";
   String sugestFile = control.getFileName()+"log.txt";
   String logFileName = control.getOutputFile(sugestFile,promptString);
   //WRITE TEXT TO FILE -----------------------------
    //check if user canceled save dialog don't output data to file (but still do to screen)

   if ((logFileName != null) && (control.getCurrentPath() != null))
   {
     //create new file
     try
     {
       //give fileobject name and path from dialog
       File outfile = new File(control.getCurrentPath(), logFileName);
       //make new outputstream
       FileWriter outWriter = new FileWriter(outfile);
       //make new printwrinter
       PrintWriter outPrinter = new PrintWriter(new BufferedWriter(outWriter),true);
       outPrinter.print(LogText.getText());
       //close connection to output file
       outPrinter.flush();
       outPrinter.close();
       control.showStatus("Log Saved to " + control.getCurrentPath()
                          + logFileName);
     }
     catch (IOException error)
     {
       control.showStatus("ERROR: unable to save output file: "
                          + error.toString());
     }
   }
 }

 /**
  * Appends the passed string into the log buffer for display or output to
  * a text file.
  */
 public void log(String text)
 {
   LogText.append("<> "+text+"\n\n");
   LogText.setCaretPosition(LogText.getText().length()-1);
 }


 //WINDOW LISTENERS windowClosing closes window when close box is clicked
//the rest have to be there to satisfy WindowListener
public void windowClosing (WindowEvent evt){}
public void windowActivated(WindowEvent evt){}
public void windowClosed(WindowEvent evt){}
public void windowDeactivated(WindowEvent evt){}
public void windowDeiconified(WindowEvent evt){}
public void windowIconified(WindowEvent evt){}
 public void windowOpened(WindowEvent evt){}
}