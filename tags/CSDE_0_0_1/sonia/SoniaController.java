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
import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

import sonia.parsers.DLParser;
import sonia.parsers.DotNetParser;
import sonia.parsers.DotSonParser;
import sonia.parsers.Parser;
import sonia.parsers.RJavaParser;

import cern.jet.random.Uniform;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;

/**
 * Main controlling class for SoNIA. Handles most method calls for coordinating
 * loading files, creating layouts, random numbers, logging, error mesages,
 * status messags. some display updating, saving files, and saving movies. Data
 * structures for holding loaded data, and Engines for layouts. Seed for random
 * number generator can be passed in as an argument when sonia is launched from
 * the command line, otherwise the last 7 digits of milisecond time are used.
 */

public class SoniaController {
	public static final String CODE_DATE = "2006-09-15";

	public static final String VERSION = "1.1.3_unstable";

	private SoniaInterface ui;

	private LogWindow log;

	private ArrayList engines; // holds engines for each layout

	private SoniaLayoutEngine engine;

	private ArrayList networks; // holds NetDataStructures for eachLayout

	private NetDataStructure networkData;

	private Font textFont; // the font for the GUIs

	private boolean showGUI = true;

	private String fileName = "network001";

	private String currentPath = "";

	private boolean combineSameNames = true;

	private boolean fileLoaded = false;

	private boolean paused = false;

	private LayoutSettings sliceSettings = null;

	private ApplySettings applySettings = null;

	private GraphicsSettings graphicSettings = null;

	private BrowsingSettings browseSettings = null;

	private PropertySettings parserSettings = null;

	private Uniform randomUni; // colt package mersense twister random numbers

	// NEED TO INCLUDE COLT LISCENCE AGREEMENT
	private Date date;

	/**
	 * Instantiates the controller class, sets up data structures, fonts, starts
	 * log.
	 * 
	 * @param rngSeed
	 *            an integer seed for the random number generator
	 */
	public SoniaController(int rngSeed) {
		// make new font to help keep layouts consistant across platforms
		textFont = new Font("SanSerif", Font.PLAIN, 10);
		// make a new log window
		log = new LogWindow(this);
		// construct new UI and pass a ref
		ui = new SoniaInterface(this);
		networks = new ArrayList();// to hold nets
		engines = new ArrayList();
		log("Log of SoNIA session beginning "
				+ DateFormat.getDateTimeInstance().format(new Date()));
		// setup random numbers
		try {
			randomUni = new Uniform(0.0, 1.0, rngSeed);
			log("Random number generator seeded with " + rngSeed);
		} catch (NoClassDefFoundError e) {
			// this probably means the colt library is not installed
			System.out.println("ERROR: unable to locate: " + e.getMessage());
			// if (e.getMessage().matches("cern"))
			// {
			ui
					.showError("Error launching SoNIA:\n"
							+ "It seems that SoNIA is unable to locate the external library"
							+ e.getMessage());
			// }
			log("ERROR: unable to initialize random number generator: "
					+ e.getMessage());
		}
	}

	/**
	 * Instantiates the main controller class, with optional arguments. Some of
	 * the routines in SoNIA require the use of random numbers. Because it is
	 * impossible to have random numbers on contemporary computers, SoNIA makes
	 * uses of the "pseudo-random" number generation provided in the Colt
	 * package. The Colt package generally uses the very long period Marsene
	 * Twister (sp?) algorithm, which is reportedly one of the best available.
	 * Normally SoNIA seeds the generator with the clock time, and reports the
	 * seed value to the log. When launching SoNIA from the command line it is
	 * possible to include specify the random number seed so that, if the same
	 * sequence of commands is followed, the same layout will result. The first
	 * entry in the log file is the seed for the random number generator. It is
	 * also possible to pass a file name to load as an argument, to facilitate
	 * launching sonia from another program. <BE>The possible arguments are:<BR>
	 * seed:<some integer> example: "seed:32342347892"<BR>
	 * file:<fileNameAndPath> example: "file:/home/sonia/input.son"
	 * 
	 * @param args
	 *            the arguments to parse when started from the command line
	 */
	public static void main(String[] args) {
		// set the look and feel so it will appear the same on all platforms
		try {
			// UIManager.setLookAndFeel(
			// UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println(e);
		}

		Date seedDate = new Date();
		// kludge here 'cause millisecond value of date is too large for int
		int rngSeed = (int) Math
				.round((double) seedDate.getTime() - 1050960000000.0);
		String inFile = "";
		String networkData = "";
		String settingsFile = "";
		String batchSettings = "";
		boolean runBatch = false;
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			// look at the arguments passed on the command line
			// if there is "seed:9809283434" use it as the random seed
			if (arg.startsWith("seed:")) {
				rngSeed = Integer.parseInt(arg.substring(5));
			}
			// if there is "file:<filenameand path>"try to load the file
			if (arg.startsWith("file:")) {
				inFile = arg.substring(5);
			}
			if (arg.startsWith("network:")) {
				networkData = arg.substring(8);
			}
			if (arg.startsWith("settings:")) {
				settingsFile = arg.substring(9);
			}
			if (arg.startsWith("batchsettings:")) {
				batchSettings = arg.substring(6);
			}
			if (arg.startsWith("runbatch:")) {
				runBatch = true;
			}

		}
		SoniaController sonia = new SoniaController(rngSeed);

		// batch overides settings
		if (!batchSettings.equals("")) {
			// check if it is a file or a string and try to load it
			sonia.loadBatchSettings(batchSettings);
			//this should be an option
		} else if (!settingsFile.equals("")) {
			sonia.loadSettings(settingsFile);
		}
		// if a file has been passed on the command line, load it
		if (!inFile.equals("")) {
			sonia.loadFile(inFile);
		} else if (!networkData.equals("")) {
			sonia.loadData(networkData);
		}

		// if we are going to run a batch, run it here ( later move this code)
		if (runBatch & !batchSettings.equals("")) {
			sonia.runBatch();
		}
		else {
			
		}
	}

	/**
	 * Removes the passed Network from the list of stored networks
	 * 
	 * @param network
	 *            the NetDataStructure to remove.
	 */
	public void disposeNetwork(NetDataStructure network) {
		int netIndex = networks.indexOf(network);
		// check if it was on list
		if (netIndex >= 0) {
			networks.remove(netIndex);
			if (networkData == network) {
				networkData = null; // should set it to differn net?
			}
			if (networks.size() < 1) {
				fileLoaded = false;
			}
		}
	}

	/**
	 * Removes the passed layout engine from the list of layouts
	 * 
	 * @param eng
	 *            the layout engine to remove
	 */
	public void disposeEngine(SoniaLayoutEngine eng) {
		int engIndex = engines.indexOf(eng);
		eng.disposeEngine();
		if (engIndex >= 0) {
			engines.remove(eng);
			if (engine == eng) {
				engine = null;
			}
		}
	}

	/**
	 * Brings up a file dialog, if a file is chosen it looks at the extension
	 * (.net or .son) to determine which parser to use, passes the text to the
	 * parser, and gets and stores the resulting network.
	 */
	public void loadNetFromFile() {
		// check that processes isn't running?
		// check before discarding existing netDataStructure
		String inFile = ui.getInputFileName(fileName,
				"Choose file to import from");
		// need to CHECK FOR CANCEL BETTER
		loadFile(inFile);

	}

	public void loadFile(String inFile) {
		if (inFile != null) {
			fileName = inFile;
			// eventually need to figure out or ask for filetype for corret
			// parser
			// if it ends in .son use the DotSonParser
			Parser parser;
			if (fileName.endsWith(".son")) {
				parser = new DotSonParser();
				parser.configureParser(parserSettings);
			} else if (fileName.endsWith(".dl")) {
				parser = new DLParser();
			}
			// otherwise, try the DotNetParser
			else {
				parser = new DotNetParser();
			}
			try {
				parser.parseNetwork(currentPath + inFile);
				fileLoaded = true;
				showStatus("Parsed file " + currentPath + inFile);
			} catch (IOException error) {
				showError("Unable to load file: " + error.getMessage());
				fileLoaded = false;

			}

			// CHECK IF PARSING WAS SUCCESFULL
			if (fileLoaded) {
				setupData(inFile, parser);
			}
		}

	}

	/**
	 * command for passing raw string data to parser without reading from a file
	 * 
	 * @param data
	 *            String representation of the network (for now in R format)
	 */
	public void loadData(String data) {
		Parser parser = new RJavaParser();
		try {
			//try to configure the parser
			if (parserSettings != null){
				parser.configureParser(parserSettings);
			}
			parser.parseNetwork(data);
			//put in a fake file name
			fileName = "R_import";
			fileLoaded = true;
			showStatus("Parsed network data from command line");
		} catch (IOException error) {
			showError("Unable to parse network data from command line: "
					+ error.getMessage());
			fileLoaded = false;
		}

		// CHECK IF PARSING WAS SUCCESFULL
		if (fileLoaded) {
			setupData("data from R", parser);
		}
	}

	/**
	 * desides if argument is a file or a string of compound settings, and tries
	 * to create the appropriate settings arguments.
	 * 
	 * @param settingsOrFile
	 */
	public void loadBatchSettings(String settingsOrFile) {
		// default is to assuming is a string containing lots of settings
		String compoundSettings = settingsOrFile;
		// but we also try to see if it is a string pointing to a file of
		// settings
		try {
			LineNumberReader reader = new LineNumberReader(new FileReader(
					settingsOrFile));
			compoundSettings = "";
			String line;
			try {
				line = reader.readLine(); // this is really a pretty silly way
											// to do this
				while (line != null) {
					compoundSettings += line + "\n";
					line = reader.readLine();
				}
				showStatus("Read batch settings file: " + settingsOrFile);
				log("Read batch settings file" + settingsOrFile);
			} catch (IOException e) {
				showError("Error reading batch settings from file: "
						+ settingsOrFile + " :" + e.getMessage());
			}
		} catch (FileNotFoundException e) {
		}
		// now try to get the settings objects
		PropertyBuilder proper = new PropertyBuilder(compoundSettings);
		parserSettings = proper.getParserSettings();
		if (parserSettings != null) {
			showStatus("Read parser settings from batch instructions");
			log("Read parser settings from batch instructions:\n "
					+ parserSettings);
		}
		sliceSettings = proper.getLayoutSettings();
		if (sliceSettings != null) {
			showStatus("Read layout slice settings from batch instructions");
			log("Read layout slice settings from batch instructions:\n "
					+ sliceSettings);
		}
		applySettings = proper.getApplySettings();
		if (applySettings != null) {
			showStatus("Read layout apply settings from batch instructions");
			log("Read layout apply settings from batch instructions:\n "
					+ applySettings);
		}
		graphicSettings = proper.getGraphicsSettings();
		if (sliceSettings != null) {
			showStatus("Read graphics settings from batch instructions");
			log("Read graphics settings from batch instructions:\n "
					+ graphicSettings);
		}
		browseSettings = proper.getBrowsingSettings();
		if (sliceSettings != null) {
			showStatus("Read layout browsing settings from batch instructions");
			log("Read layout browsing settings from batch instructions:\n "
					+ browseSettings);
		}

	}

	/**
	 * if batch settings have been loaded, runs sonia as specified without user
	 * interaction.
	 * 
	 * @author skyebend
	 */
	public void runBatch() {
		try {
			showGUI = false;
			ui.setVisible(false);
			// try to do the slicing
			if (sliceSettings != null) {
				showStatus("Read layout settings from batch instructions");
				log("Read layout settings from batch instructions");
				createLayout();
			}

			if ((engine != null) && (applySettings != null)) {
				engine.setApplySettings(applySettings);
				engine.applyLayoutToCurrent();
				engine.changeToSliceNum(1);
				applySettings.setProperty(ApplySettings.STARTING_COORDS,
						ApplySettings.COORDS_FROM_PREV);
				applySettings.setProperty(ApplySettings.APPLY_REMAINING,
						true + "");
				engine.setApplySettings(applySettings);
				//call the non-threaded version so we can catch the exceptions...
				engine.startApplyLayoutToRemaining();
				engine.changeToSliceNum(0);
				engine.getLayoutWindow().showCurrentSlice();
				String movFileName = currentPath+getFileName()+".mov";
				 exportMovie(engine,movFileName);
				 log.writeLogToFile(currentPath+getFileName()+"_log.txt");
				 //if there is no ui, then we should quite when done
				 System.exit(1);
				
			}
		} catch (Exception e) {
			System.out.println("ERROR in batch execution: "); 
			e.printStackTrace();
			System.exit(-1);
		}

	}

	/**
	 * try to read in a previously saved settings file
	 * 
	 * @param settingsFile
	 */
	public void loadSettings(String settingsFile) {
		try {
			FileInputStream settIn = new FileInputStream(settingsFile);
			sliceSettings = new LayoutSettings();
			sliceSettings.load(settIn);
			showStatus("Read layout settings from " + settingsFile);
			log("Read layout settings from " + settingsFile);
			createLayout();
		} catch (FileNotFoundException e) {
			showError("Unable to locate specified settings file: "
					+ settingsFile + " " + e.getMessage());
		} catch (IOException e) {
			showError("Unable to read specified settings file: " + settingsFile
					+ " " + e.getMessage());
		}
	}

	private void setupData(String inFile, Parser parser) {
		networkData = new NetDataStructure(this, inFile, parser
				.getNumNodeEvents(), parser.getNumArcEvents(), parser
				.getMaxNumNodes());
		// load data from parser into net data
		networkData.addNodeEvents(parser.getNodeList());
		networkData.addArcEvents(parser.getArcList());
		networkData.setNetInfo(parser.getNetInfo());
		// print details out to log
		log("loaded network from " + currentPath + inFile + "\nparser used:"
				+ parser.getParserInfo() + "\n" + parser.getNumNodeEvents()
				+ " node events, " + parser.getNumArcEvents() + " arc events"
				+ "\n" + "smallest time value:" + networkData.getFirstTime()
				+ "\n" + "largest time value:" + networkData.getLastTime()
				+ "\n" + "number of unique nodes:" + parser.getMaxNumNodes()
				+ "\n" + "comments from file:" + networkData.getNetInfo());
		// put it in list of loaded networks
		networks.add(networkData);
	}

	/**
	 * Checks that a file has been loaded, then creates a layout engien for the
	 * most recently loaded file.
	 */
	public void createLayout() {
		// should make sure there is some network data
		if (fileLoaded == true) {

			String engName = getFileName() + " #" + (engines.size() + 1);
			// probably should ask for kind of layout here
			if (isShowGUI() & (sliceSettings == null)) {
				LayoutSettingsDialog windowSettings = new LayoutSettingsDialog(
						sliceSettings, this, engName, ui);
				if (sliceSettings == null) {
					// tell the settings dialog what the start and end times for
					// the
					// data
					// are
					windowSettings.setDataStartDefault(networkData
							.getFirstTime());
					windowSettings.setDataEndDefault(networkData.getLastTime());
				}
				// show the dialog
				sliceSettings = windowSettings.askUserSettings();
			}
			engine = new SoniaLayoutEngine(sliceSettings, this, networkData,
					engName);
			showStatus("layout " + engName + " created.");
			engines.add(engine);
			// check if we have graphic settings
			if (graphicSettings == null) {
				// kludge to get settings with defaults
				graphicSettings = (new GraphicsSettingsDialog(
						new GraphicsSettings(), this, engine, null, null))
						.storeSettings();
			}
			// debug
			// System.out.println("graphic settings: "+graphicSettings);
			LayoutWindow display = new LayoutWindow(graphicSettings,
					browseSettings, this, engine, 490, 420);
			engine.setDisplay(display);
			ui.addFrame(display);
			engine.setDisplayWidth(Integer.parseInt(graphicSettings
					.getProperty(GraphicsSettings.LAYOUT_WIDTH)));
			engine.setDisplayHeight(Integer.parseInt(graphicSettings
					.getProperty(GraphicsSettings.LAYOUT_HEIGHT)));
			// try {
			// display.setMaximum(true);
			// } catch (PropertyVetoException e) {
			// System.out.println("somebody stoped the window from expanding "
			// + e);
			// }

		} else {
			showError("Load file before creating layout");
		}
	}

	/**
	 */
	public void pippo() {

	}

	/**
	 * Not sure if we should have this method as it is for gui..
	 * 
	 * @param frame
	 */
	public void showFrame(JInternalFrame frame) {
		ui.addFrame(frame);
	}

	/**
	 * Brings up a dialog to pick which of the layouts to export as a movie,
	 * then makes a movie exporter and passes it to the layout engine.
	 */
	public void exportMovie(SoniaLayoutEngine engToFilm, String fileName) {
		// NEED TOP PICK WHICH layout to export!!
		// ListPicker engPicker = new ListPicker(ui,engines,"Choose Layout to
		// film");
		// SoniaLayoutEngine engToFilm =
		// (SoniaLayoutEngine)engPicker.getPickedObject();
		SoniaMovieMaker exporter = new SoniaMovieMaker(this, engToFilm,
				fileName);
		// for now, tell the engine to tell the layout...
		try {
			engToFilm.makeMovie(exporter);
		} catch (Exception e) {
			//TODO: need to deal more elegantly when the movie file is busy or locked...
			showError("ERROR saving movie: "+e.getMessage());
			e.printStackTrace();
			exporter.setVisible(false);
			exporter = null;
		}

	}

	/**
	 * Brings up a dialog to chose which layout to export, then a save dialog,
	 * and then saves out a text file containing a set of matricies
	 * corresponding to each slice in the layout.
	 */
	public void exportMatricies(SoniaLayoutEngine engToExport) {
		// NEED TO PICK WHICH LAYOUTEnginge TO EXPORT
		// first make sure there is at leat one
		if (engines.size() < 1) {
			showError("At least one layout must be created to export");
		} else {
			// ListPicker layoutPicker = new ListPicker(ui, engines,
			// "Choose Layout to Export");
			// SoniaLayoutEngine engToExport = (SoniaLayoutEngine) layoutPicker
			// .getPickedObject();
			String promptString = "Please Choose location and name for the matrix text file";
			String sugestFile = getFileName() + ".mat";
			String logFileName = getOutputFile(sugestFile, promptString);
			// WRITE TEXT TO FILE -----------------------------
			// check if user canceled save dialog don't output data to file (but
			// still do to screen)

			if ((logFileName != null) && (getCurrentPath() != null)) {
				// create new file
				try {
					// give fileobject name and path from dialog
					File outfile = new File(getCurrentPath(), logFileName);
					// make new outputstream
					FileWriter outWriter = new FileWriter(outfile);
					// make new printwrinter
					PrintWriter outPrinter = new PrintWriter(
							new BufferedWriter(outWriter), true);

					// get matrices from current layout slices
					Vector matricies = engToExport.getSliceMatricies();
					int numMats = matricies.size();
					// include the file info
					outPrinter.println(numMats + " Matricies from layout "
							+ engToExport.toString());
					for (int n = 0; n < numMats; n++) {
						outPrinter.print(((SparseDoubleMatrix2D) matricies
								.get(n)).toString());
						outPrinter.print("\n\n");
					}
					// close connection to output file
					outPrinter.flush();
					outPrinter.close();
					showStatus("Matricies Saved to " + getCurrentPath()
							+ logFileName);
				} catch (IOException error) {
					showStatus("ERROR: unable to save matrix file: "
							+ error.toString());
				}
			}
		}
	}

	/**
	 * Asks the UI to display text in the status window of the controller
	 * interface
	 * 
	 * @param text
	 *            the text to display in the text area
	 */
	public void showStatus(String text) {
		ui.showStatus(text);
	}

	/**
	 * Asks the UI to display text in the status window of the controller
	 * interface as an error (red text, will beep)
	 * 
	 * @param text
	 *            the text of the error message
	 */
	public void showError(String text) {
		ui.showError(text);
	}

	/**
	 * Asks the log to append the passed text.
	 * 
	 * @param text
	 *            the text to append to the log
	 */
	public void log(String text) {
		log.log(text);
	}

	/**
	 * returns a reference to the current log object (for now is a panel, but
	 * shouldb be abstracted from UI
	 * 
	 * @return
	 */
	public LogWindow getLogRef() {
		return log;
	}

	/**
	 * Asks the interface to throw up an open file dialog, gets file name and
	 * returns it.
	 * 
	 * @param suggestName
	 *            the default file name to display in the dialog
	 * @param msg
	 *            the explanatory text message to include in the file dialog.
	 */

	public String getInputFile(String suggestName, String msg) {
		return ui.getInputFileName(suggestName, msg);
	}

	/**
	 * Asks the interface to throw up a save file dialog, gets file name and
	 * returns it.
	 * 
	 * @param suggestName
	 *            the default file name to display in the dialog
	 * @param msg
	 *            the explanatory text message to include in the file dialog.
	 */
	public String getOutputFile(String suggestName, String msg) {
		return ui.getOutputFileName(suggestName, msg);
	}

	/**
	 * If not paused, iterates over all the layout engines and asks them to
	 * update their displays.
	 */
	public void updateDisplays() {
		if (!paused) {
			// iterate over all engines and ask them to update their displays
			Iterator engIter = engines.iterator();
			while (engIter.hasNext()) {
				((SoniaLayoutEngine) engIter.next()).updateDisplays();
			}

		}
	}

	// accessors------------------
	/**
	 * Returns a random integer drawn with a uniform probability from the
	 * spcified range. Backed by the Colt Marsense twister implementation,
	 * seeded at startup with the clock time (defualt) or from the command line.
	 * 
	 * @param from
	 *            the lower bound on the range
	 * @param to
	 *            the upper bound on the range
	 */
	public int getUniformRand(int from, int to) {

		int returnInt = randomUni.nextIntFromTo(from, to);
		return returnInt;
	}

	/**
	 * Returns a random double drawn with a uniform probability from the
	 * spcified range. Backed by the Colt Marsense twister implementation,
	 * seeded at startup with the clock time (defualt) or from the command line.
	 * 
	 * @param from
	 *            the lower bound on the range
	 * @param to
	 *            the upper bound on the range
	 */
	public double getUniformRand(double from, double to) {
		double returnDouble = randomUni.nextDoubleFromTo(from, to);
		return returnDouble;
	}

	/**
	 * Checks is the controler is in paused mode.
	 */
	public boolean isPaused() {
		return paused;
	}

	/**
	 * Sets paused mode the passed state, and asks the current engine to pause
	 * or resume.
	 * 
	 * @param state
	 *            the desired pause state
	 */
	public void setPaused(boolean state) {
		paused = state;
		if (paused) {
			if (engine != null)
				engine.pause();
			showStatus("Paused. Press Resume to contine");
		} else {
			if (engine != null)
				engine.resume();
			showStatus("");
		}
	}

	/**
	 * Returns the path to the last used file.
	 */
	public String getCurrentPath() {
		return currentPath;
	}

	public void setCurrentPath(String path) {
		currentPath = path;
	}

	/**
	 * Returns fileName, but with any extension after "." removed
	 */

	public String getFileName() {
		String shortName = fileName;
		int dotLoc = shortName.lastIndexOf(".");
		if (dotLoc > 0) {
			shortName = shortName.substring(0, dotLoc);
		}
		return shortName;
	}

	/**
	 * Sets the file name to the passed string
	 * 
	 * @param name
	 *            the name for the file
	 */
	public void setFileName(String name) {
		fileName = name;
	}

	/**
	 * Returns the UI font for consistancy across layouts. (Is this necessary in
	 * the peering structure?)
	 */

	public Font getFont() {
		return textFont;
	}

	/**
	 * Checks if there are any NetworkDataStructures store in an array
	 * 
	 * @return true if there is at least one layout created
	 */
	public boolean hasNetworks() {
		if (networks.size() > 0)
			return true;
		return false;
	}

	public boolean isShowGUI() {
		return showGUI;
	}

}