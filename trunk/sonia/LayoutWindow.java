package sonia;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.awt.geom.*;
import java.beans.PropertyVetoException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.plaf.basic.BasicBorders;

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
 * LayoutWindow does the drawing of the networks to screen, and provides
 * controls for moving through time slice bins. This is the main view into the
 * network data. It also (for now) generates the images whcih are exported in
 * the movie. Node positioning, and the actual storing of the layout slices is
 * handled by the LayoutEngine. Most of the actions take place on their own
 * threads so that it is possible to pause them. The model for drawing the
 * network to the screen works as follows: When asked to update the display, the
 * LayoutWindow checks the render settings and asks the SoniaCanvas in the
 * layout area to draw the network. The canvas asks layout engine to give it a
 * RenderSlice filled with the Node- and ArcAttributes which fit the binning
 * criteria (see NetDataStructure). The RenderSlice asks the engine for the
 * appropriate coordinates for the nodes, as specified by the current
 * LayoutSlice (with interpolation if the render is part of an animated
 * transition) and asks each of the attributes to draw themselvs in the
 * appropriate location, modified by the settings in the graphicSettingsDialog.
 * <BR>
 * <BR>
 * The commands and settings in the layout window are:
 * <P>
 * Apply Layout.. - brings up the ApplySettings dialog for setting the layout
 * algorithm parameters and running it on the layout.
 * <P>
 * </P>
 * Re-Apply - If algorithm's parameters are already set, it re-runs the layout
 * algorithm with the same settings
 * <P>
 * </P>
 * Move Nodes - Toggles the ability to drag nodes around on the layout, click
 * again to turn off (so other layouts can proceed)
 * <P>
 * </P>
 * Stress - brings up a Shepard's Stress Plot to provide information on the
 * degree of layout distortion.
 * <P>
 * </P>
 * Phase Plot - brings up a "phase-space plot" of the positions of the node and
 * arc events in time, where the slices land, and the active slice and render
 * windows. (you can also navigate by clicking on the slices)
 * <P>
 * </P>
 * View Options - brings up the GraphicSettingsDialog to specify how the network
 * will be renderd.
 * <P>
 * </P>
 * Layout (slice) - the index number of the current slice. (typing a number and
 * hitting enter will change to that slice)
 * <P>
 * </P>
 * display time - the start of the currently displayed render slice. Defaults to
 * match the LayoutSlice, but can be modified to show events at other points in
 * time.
 * <P>
 * </P>
 * duration - the duration of the render slice. Defaults to match the
 * LayoutSlice, but can be changed to show a shorter or longer interval.
 * <P>
 * </P>
 * num. interp frames - the number of interpolated frames to be generated when
 * transitioning between slices. Larger valuees will give smoother slower
 * animations.
 * <P>
 * </P> |< - asks the engine to change to the previous slice with an animated
 * transiton
 * <P>
 * </P> >| - asks the engine to change to the next slice with an animated
 * transition. || - Pause (actually, stop) the transition or most other active
 * processes. (will need to be un-paused before continuing) > - Play through all
 * the slices with transitions until the end.
 * </P>
 * 
 * <BR>
 * <BR>
 */

// TODO: need to fix the window size scaling issues for layouts and for movie
// export
public class LayoutWindow extends ExportableFrame implements ActionListener,
		InternalFrameListener

{

	private SoniaController Control;

	private SoniaLayoutEngine engine;

	private MovieMaker movie;

	private GraphicsSettingsDialog graphicsSettings;

	private GraphicsSettings drawSettings;

	private BrowsingSettings browseSettings;

	private SoniaCanvas LayoutArea;

	private JPanel controlPanel;

	private JButton ApplyLayout;

	private JButton ReApply;

	private JButton Stress;

	private JButton Stability;

	private JButton PhasePlot;

	private JButton NextSlice;

	private JButton PrevSlice;

	private JButton PlayAll;

	private JButton Pause;

	private JButton ViewOptions;

	private JButton MoveNodes;

	// private JLabel NumInterpLabel;
	private JTextField NumInterps;

	private JTextField frameDelay;

	// private Button ViewNodes;

	private JTextField RenderTime;

	private JTextField RenderDuration;

	private JTextField renderOffset;

	// private JLabel RenderLabel;
	// private JLabel DurationLabel;
	private JTextField LayoutNum;

	// private JLabel LayoutLabel;

	private boolean movingNodes = false;

	private boolean isTransitionActive = false; // indicates if a thread is

	// animating

	private NodeMover mover;

	/**
	 * Creates the layout window which functions as a view into the network data
	 * with controls for steping through the data slice by slice, adjusting
	 * graphics parameters, applying the layout algorithms, etc. This method
	 * creates and positions gui objects, and adds itself as the action
	 * listener, and then shows the window.
	 * 
	 * @param controller
	 *            the main soniaController
	 * @param layoutEng
	 *            the LayoutEngine which will provide the data and positioning
	 * @param initWidth
	 *            the initial width of the window in pixels
	 * @param initHeight
	 *            the initial height of the window in pixels
	 */
	public LayoutWindow(GraphicsSettings settings, BrowsingSettings browseSet,
			SoniaController controller, SoniaLayoutEngine layoutEng,
			int initWidth, int initHeight) {
		super.setDefaultCloseOperation(ExportableFrame.DO_NOTHING_ON_CLOSE);

		Control = controller;
		engine = layoutEng;
		drawSettings = settings;
		browseSettings = browseSet;

		exportMenu.add(new AbstractAction("Export Movie...") {
			public void actionPerformed(ActionEvent arg0) {
				Control.exportMovie(engine, null);
			}
		});

		exportMenu.add(new AbstractAction("Export Matricies...") {
			public void actionPerformed(ActionEvent arg0) {
				Control.exportMatricies(engine);
			}
		});
		
		exportMenu.add(new AbstractAction("Export Test Flash Movie...") {
			public void actionPerformed(ActionEvent arg0) {
				Control.exportFlashMovie(engine,LayoutArea,"flashExportTest.swf");
			}
		});

		// this.setFont(controller.getFont());
		// create layout objects
		controlPanel = new JPanel();
		controlPanel.setBorder(new EtchedBorder());
		ApplyLayout = new JButton("Apply Layout..");
		ReApply = new JButton("Re-Apply");
		Stress = new JButton("Stress");
		PhasePlot = new JButton("PhasePlot");
		Stability = new JButton("Stability");
		NextSlice = new JButton(">|");
		PrevSlice = new JButton("|<");
		PlayAll = new JButton(">");
		Pause = new JButton("||");
		ViewOptions = new JButton("View Options..");
		MoveNodes = new JButton("Move Nodes");

		RenderTime = new JTextField("0", 8);
		RenderTime.setBorder(new TitledBorder("render time"));
		RenderTime.setToolTipText("Start of current render time bin");
		RenderDuration = new JTextField("0.1", 5);
		RenderDuration.setBorder(new TitledBorder("duration:"));
		RenderDuration.setToolTipText("Size of time bin used for viewing");
		renderOffset = new JTextField("0", 5);
		renderOffset.setBorder(new TitledBorder("offset"));
		renderOffset.setToolTipText("position of render start within slice");
		LayoutNum = new JTextField("0", 8);
		LayoutNum.setBorder(new TitledBorder("Layout (slice) #:"));
		LayoutNum.setToolTipText("Index of slices used for coordinates");
		// LayoutLabel = new JLabel("Layout (slice) #:");
		LayoutArea = new SoniaCanvas(drawSettings, engine);
		LayoutArea.setBackground(Color.WHITE);

		graphicsSettings = new GraphicsSettingsDialog(LayoutArea.getSettings(),
				Control, engine, null, LayoutArea);

		// NumInterpLabel = new JLabel("num. interp frames");
		NumInterps = new JTextField("10", 5);
		NumInterps.setBorder(new TitledBorder("num. interp frames"));
		NumInterps
				.setToolTipText("How many in-between frames to use in transition");
		frameDelay = new JTextField("30", 5);
		frameDelay.setBorder(new TitledBorder("frame delay"));
		frameDelay.setToolTipText("How long to wait between frames");

		// LAYOUT
		this.getContentPane().setLayout(new BorderLayout());

		// add components to the layout GBlayout using constraints
		// set up top level components
		// c.gridx=0;c.gridy=0;c.gridwidth=7;c.gridheight=1;c.weightx=1;c.weighty=1;
		// c.fill=c.BOTH;
		getContentPane().add(LayoutArea,BorderLayout.CENTER);
		getContentPane().add(controlPanel,BorderLayout.SOUTH);

		GridBagLayout layout = new GridBagLayout();
		controlPanel.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 2, 0, 2);
		// c.fill=c.NONE;

		// buttons
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.5;
		c.weighty = 0.0;
		controlPanel.add(ApplyLayout, c);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.5;
		c.weighty = 0.0;
		controlPanel.add(ReApply, c);
		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.5;
		c.weighty = 0.0;
		controlPanel.add(Stress, c);
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.5;
		c.weighty = 0.0;
		// add(Stability,c);
		c.gridx = 3;
		c.gridy = 1;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 0.5;
		c.weighty = 0.0;
		controlPanel.add(PhasePlot, c);
		c.gridx = 5;
		c.gridy = 1;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 0.5;
		c.weighty = 0.0;
		controlPanel.add(ViewOptions, c);
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.5;
		c.weighty = 0.0;
		controlPanel.add(MoveNodes, c);
		// buttons and controls
		// c.gridx=1;c.gridy=2;c.gridwidth=1;c.gridheight=1;c.weightx=0.1;c.weighty=0.0;
		// add(LayoutLabel,c);
		c.gridx = 2;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.0;
		controlPanel.add(LayoutNum, c);
		c.gridx = 3;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.0;
		controlPanel.add(PrevSlice, c);
		c.gridx = 4;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.0;
		controlPanel.add(NextSlice, c);
		c.gridx = 5;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.0;
		controlPanel.add(Pause, c);
		c.gridx = 6;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.0;
		controlPanel.add(PlayAll, c);

		// c.gridx=1;c.gridy=3;c.gridwidth=1;c.gridheight=1;c.weightx=0.1;c.weighty=0.0;
		// add(RenderLabel,c);
		c.gridx = 2;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.0;
		controlPanel.add(RenderTime, c);
		// c.gridx=3;c.gridy=3;c.gridwidth=1;c.gridheight=1;c.weightx=0.1;c.weighty=0.0;
		// add(DurationLabel,c);
		c.gridx = 3;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.0;
		controlPanel.add(RenderDuration, c);
		c.gridx = 4;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.0;
		controlPanel.add(renderOffset, c);
		c.gridx = 5;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.0;
		controlPanel.add(NumInterps, c);
		c.gridx = 6;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.0;
		controlPanel.add(frameDelay, c);

		// add action listeners for button clicks
		ApplyLayout.addActionListener(this);
		ReApply.addActionListener(this);
		Stress.addActionListener(this);
		Stability.addActionListener(this);
		PhasePlot.addActionListener(this);
		ViewOptions.addActionListener(this);
		MoveNodes.addActionListener(this);
		NextSlice.addActionListener(this);
		PrevSlice.addActionListener(this);
		Pause.addActionListener(this);
		PlayAll.addActionListener(this);

		RenderTime.addActionListener(this);
		RenderDuration.addActionListener(this);
		LayoutNum.addActionListener(this);
		NumInterps.addActionListener(this);
		// NEED A LISTENER FOR THE TXT FIELD

		addInternalFrameListener(this);

		// read settings from the browsng properties
		fetchBrowseSettings();

		// this.setBackground(Color.lightGray);
		this.setSize(initWidth, initHeight);
		this.setTitle(engine.toString());
		this.setLocation(10, 10);
		this.setVisible(true);
		


	}

	/**
	 * returns just the layout area with the network
	 * 
	 * @return
	 */
	protected JComponent getGraphicContent() {
		return LayoutArea;
	}

	// ACTION LISTENER //figures out what user did and calls apropriate method
	/**
	 * Queries the event to determine which button was clicked, and calls the
	 * corresponding method. Some methods check to make sure there is no active
	 * transition.
	 * 
	 * @param evt
	 *            the event indicating the source of the action
	 */
	public void actionPerformed(ActionEvent evt) {

		if (evt.getSource().equals(ApplyLayout)) {
			applyLayout();
		} else if (evt.getSource().equals(ReApply)) {
			reApplyLayout();
		} else if (evt.getSource().equals(Stress)) {
			engine.calcStress();
		} else if (evt.getSource().equals(Stability)) {
			engine.calcStability();
		} else if (evt.getSource().equals(PhasePlot)) {
			engine.showPhasePlot();
		} else if (evt.getSource().equals(NextSlice)) {
			if (!isTransitionActive) {
				// transitionToSlice(engine.getCurrentSliceNum()+1);
				startFwdTransThread();
			}
		} else if (evt.getSource().equals(PlayAll)) {
			if (!isTransitionActive) {
				startPlayThread();
			}
		} else if (evt.getSource().equals(Pause)) {
			if (Control.isPaused()) {
				Control.setPaused(false);
				Pause.setText("||");
			} else {
				Control.setPaused(true);
				Pause.setText("[||]");
			}
		} else if (evt.getSource().equals(PrevSlice)) {
			if (!isTransitionActive) {
				startRevTransThread();
			}
		} else if (evt.getSource().equals(ViewOptions)) {
			graphicsSettings.showDialog();
			updateDisplay();
		} else if (evt.getSource().equals(LayoutNum)) {
			if (!isTransitionActive) {
				goToSlice(Integer.parseInt(LayoutNum.getText()));
			}
		} else if (evt.getSource().equals(RenderTime)) {
			if (!isTransitionActive) {
				renderCurrentSettings();
			}
		} else if (evt.getSource().equals(RenderDuration)) {
			if (!isTransitionActive) {
				renderCurrentSettings();
			}
		} else if (evt.getSource().equals(NumInterps)) {
			if (!isTransitionActive) {
				engine.setInterpFrames(Integer.parseInt(NumInterps.getText()));
				// allso reset the render duration
				LayoutSlice slice = engine.getCurrentSlice();
				double duration = (slice.getSliceEnd() - slice.getSliceStart())
						/ (double) engine.getInterpFrames();
				RenderDuration.setText(duration + "");
				renderCurrentSettings();
			}
		} else if (evt.getSource().equals(MoveNodes)) {
			if (!isTransitionActive) {
				moveNodes();
			}
		}
		recordBrowseSettings();
	}

	/**
	 * Attaches the passed MovieMaker to the layout, and then launches the movie
	 * recording thread, which will call the record movie method to step through
	 * the layouts transitioning and recording each frame.
	 * 
	 * @param exporter
	 *            the SoniaMovieMaker which will record the images
	 */
	public void makeMovie(MovieMaker exporter) throws Exception {
		movie = exporter;
		int endIndex = engine.getNumSlices();
		int numFrames = endIndex * Integer.parseInt(NumInterps.getText());
		
			movie.setupMovie(LayoutArea, numFrames);
			// make sure we are on the first slice
			engine.changeToSliceNum(0);
			// THIS SHOULD BE ON A SEPERATE THREAD SO WE CAN PAUSE
			// should also record movie layout stats to first frame
			startMovieRecordThread();
	
	}

	/**
	 * Creates a new thread to run the movie recording, calls the recordMovie
	 * method, called by makeMove.
	 */
	private void startMovieRecordThread() {
		Thread movieThread = new Thread() {
			public void run() {
				isTransitionActive = true;
				recordMovie();
				movie.finishMovie();
				movie = null;
				isTransitionActive = false;
			}
		};
		movieThread.setName("movie thread");
		movieThread.setPriority(10);
		movieThread.start();
	}

	/**
	 * Transitions through the layouts, recording the image to the movie file.
	 * Called by the movie thread.
	 */
	private void recordMovie() {
		// double check that a movie exists to record on
		if (movie != null) {
			int startIndex = 1;
			int endIndex = engine.getNumSlices();
			for (int s = startIndex; s < endIndex; s++) {
				// awkward transition checks if movie is being recorded and
				// saves theframe
				transitionToSlice(s);
				// make it so that movie recording can be stopped if something
				// goes wrong
				if (Control.isPaused()) {
					break;
				}
			}
		}

	}

	/**
	 * Creates a thread to animate playback in the layout window, which calls
	 * playAll.
	 */
	private void startPlayThread() {
		// should have a method to make sure nothing else tries to change the
		// display at the same time
		Thread playThread = new Thread() {
			public void run() {
				isTransitionActive = true;
				playAll();
				isTransitionActive = false;
			}
		};
		playThread.setName("play thread");
		playThread.setPriority(10);
		playThread.start();
	}

	/**
	 * Transitions through the slices, starting at the current slice. Called by
	 * the play thread. Uses transitionToSlice.
	 */
	private void playAll() {
		int startIndex = engine.getCurrentSliceNum();
		int endIndex = engine.getNumSlices();
		for (int s = startIndex + 1; s < endIndex; s++) {
			// check for pause
			if (!Control.isPaused()) {
				transitionToSlice(s);
			}
		}
	}

	/**
	 * Starts a thread to control the transition to the next slice. Calls
	 * transition to Slice.
	 */
	private void startFwdTransThread() {
		Thread transThread = new Thread() {
			int destSlice = engine.getCurrentSliceNum() + 1;

			public void run() {
				// set flag so no other transitions will start
				isTransitionActive = true;
				transitionToSlice(destSlice);
				isTransitionActive = false;
			}
		};
		transThread.setName("transition thread");
		transThread.setPriority(10);
		transThread.start();
	}

	/**
	 * Starts a thread to control the transition to the previous slice. Calls
	 * transition to Slice.
	 */
	private void startRevTransThread() {
		Thread transThread = new Thread() {
			int destSlice = engine.getCurrentSliceNum() - 1;

			public void run() {
				// set flag so no other transitions will start
				isTransitionActive = true;
				transitionToSlice(destSlice);
				isTransitionActive = false;
			}
		};
		transThread.setName("transition thread");
		transThread.setPriority(10);
		transThread.start();
	}

	/**
	 * Creates a new NodeMover for manualy positioning nodes.The node mover will
	 * add itself as a mouse motion listener, and remove itself when node moving
	 * is disabled.
	 */
	private void moveNodes() {
		if (!movingNodes) {
			movingNodes = true;
			// mover will add itself to layout as a mouse motion listener
			mover = new NodeMover(Control, engine, LayoutArea);
			MoveNodes.setText("Stop Moving");
		} else {
			movingNodes = false;
			mover.endMoveNodes();
			mover = null;
			MoveNodes.setText("Move Nodes");
		}
	}

	// this should be moved to the controller
	/**
	 * Checks the current slice settings, and brings up the Apply Layout dialog.
	 */
	private void applyLayout() {
		// makesure there is a layout chosen
		engine.changeToSliceNum(Integer.parseInt(LayoutNum.getText()));
		// get that slice
		LayoutSlice currentSlice = engine.getCurrentSlice();
		// apply layout
		engine.showApplyLayoutSettings();
		// Render the entire slice as one block, from start to finish
		RenderTime.setText("" + currentSlice.getSliceStart());
		RenderDuration.setText(""
				+ (currentSlice.getSliceEnd() - currentSlice.getSliceStart()));
		LayoutArea.setRenderSlice(engine.getRenderSlice(currentSlice
				.getSliceStart(), currentSlice.getSliceEnd()));
		updateDisplay();
	}

	/**
	 * Checks the current slice settings and, if the LayoutSettings already
	 * exist, applies them to the layout. Otherwise brings up the layout
	 * settings dialog.
	 */
	private void reApplyLayout() {
		// makesure there is a layout chosen
		engine.changeToSliceNum(Integer.parseInt(LayoutNum.getText()));
		// get that slice
		LayoutSlice currentSlice = engine.getCurrentSlice();
		// apply layout
		engine.applyLayoutToCurrent();
		// Render the entire slice as one block, from start to finish
		RenderTime.setText("" + currentSlice.getSliceStart());
		RenderDuration.setText(""
				+ (currentSlice.getSliceEnd() - currentSlice.getSliceStart()));
		LayoutArea.setRenderSlice(engine.getRenderSlice(currentSlice
				.getSliceStart(), currentSlice.getSliceEnd()));
		updateDisplay();
	}

	/**
	 * Changes the layout to the specified slice (both in the layout engine and
	 * graphically) directly without an animated transition.
	 * 
	 * @param the
	 *            number of the slice to go to.
	 */
	public void goToSlice(int number) {
		// ask the canvas to save image for ghost (nothing will hapen if
		// ghosting is off)
		LayoutArea.saveImageForGhost();
		// change the state of the engine to reflect what is showing
		// makesure there is a layout chosen
		engine.changeToSliceNum(number);
		// get that slice
		LayoutSlice currentSlice = engine.getCurrentSlice();
		// make sure number is correct (in case we didn't change 'cause at end
		// or start)
		number = engine.getCurrentSliceNum();
		// make sure the coords are correct
		engine.setCoordsToSlice(number);
		// Render the entire slice as one block, from start to finish
		LayoutArea.setRenderSlice(engine.getRenderSlice(currentSlice
				.getSliceStart(), currentSlice.getSliceEnd()));
		// update the text fields
		RenderTime.setText("" + currentSlice.getSliceStart());
		RenderDuration.setText(""
				+ (currentSlice.getSliceEnd() - currentSlice.getSliceStart()));
		LayoutNum.setText("" + engine.getCurrentSliceNum());
		// now ask the network to redraw isteslf
		updateDisplay();
	}

	/**
	 * Creates a smoothly animated transition from the current slice to the
	 * specified slice number. Gets the ammount of time between the start of the
	 * current and the start of the destination slice, divides this by the
	 * desired number of interpolation frames, and creates a series of render
	 * slices with the desired timings and interpolated node cooordinates.
	 * Called by the play or transition threads.
	 * 
	 * @param number
	 *            the slice number to transition to.
	 */
	public void transitionToSlice(int number) {
		LayoutArea.saveImageForGhost();
		// check if should do interpolation
		engine.setInterpFrames(Integer.parseInt(NumInterps.getText()));
		// check the frame delay
		engine.setFrameDelay(Integer.parseInt(frameDelay.getText()));
		// check render offset
		engine.setRenderOffset(Float.parseFloat(renderOffset.getText()));
		if (engine.getInterpFrames() > 0) {
			// do the render slices for animation
			// get the slice we are at now
			LayoutSlice nowSlice = engine.getCurrentSlice();

			int nowNum = engine.getCurrentSliceNum();
			// try changeing to the requested one (there might not be one)
			engine.changeToSliceNum(number);
			int newNum = engine.getCurrentSliceNum(); // incase we were at the
			// last slice
			if (newNum != nowNum) {
				// get the next (now the current) slice
				LayoutSlice newSlice = engine.getCurrentSlice();
				// figure out what time we start at
				double time = nowSlice.getSliceStart();
				// figure out how much time each step will be
				double delta = (newSlice.getSliceStart() - time)
						/ engine.getInterpFrames();
				double offset = (newSlice.getSliceEnd() - newSlice
						.getSliceStart())
						* engine.getRenderOffset();
				// figure out the "width" of the render window
				// double duration = nowSlice.getSliceEnd()-time;
				// RenderDuration.setText(""+duration);
				double duration = Double.parseDouble(RenderDuration.getText());
				// do each of the displays

				for (int i = 1; i <= engine.getInterpFrames(); i++) {
					// make it so that transition can be stopped by pause
					if (Control.isPaused()) {
						break;
					}

					time = nowSlice.getSliceStart() + offset + i * delta;
					// render is a moving window the same width as the slice,
					// we see edges as they are added to the front
					LayoutArea.setRenderSlice(engine.getRenderSlice(time,
							(time + duration)));
					RenderTime.setText("" + time);
					// make the engine figure out the coord
					engine.interpCoords(nowSlice, newSlice, time - offset);

					// check if we are recording a movie
					if (movie != null) {
						movie.captureImage();
					} else {
						// update the display
						updateDisplay();
					}
					// now the rendering is going to fast, so we have to slow it
					// down

					try {
						Thread.sleep(engine.getFrameDelay());
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				LayoutNum.setText("" + engine.getCurrentSliceNum());
				RenderTime.setText("" + newSlice.getSliceStart());
				// RenderDuration.setText(""+(newSlice.getSliceEnd()-newSlice.getSliceStart()));
			}
		} else {

			// go to slice directly, without interpolation
			goToSlice(number);
		}
	}

	/**
	 * Updates the display to show the slice currently selected in the layout
	 * engine
	 */
	public void showCurrentSlice() {
		engine.setCoordsToSlice(engine.getCurrentSliceNum());
		renderCurrentSettings();
		updateDisplay();
	}

	/**
	 * Creates a render slice network with the current settings for render
	 * parameters displayed on the controlls. Does not actually draw the network
	 * to the window, that is done with updateDisplay()
	 */
	public void renderCurrentSettings() {
		LayoutArea.setRenderSlice(engine.getRenderSlice(Double
				.parseDouble(RenderTime.getText()), Double
				.parseDouble(RenderTime.getText())
				+ Double.parseDouble(RenderDuration.getText())));
		updateDisplay();
	}

	/**
	 * update display actually redraws the network to an Image, stores the image
	 * for later repaints. paint() just redraws the same image
	 */
	public void updateDisplay() {
		// asks the canvas to actually redraw the network, instead of just
		// refreshing
		// the old image
		// Graphics graph = LayoutArea.getGraphics();
		// LayoutArea.updateDisplay(graph,true);
		// LayoutArea.paintComponent(graph);
		LayoutArea.repaint();
		// try to make it updage the controls on mac
	}

	/**
	 * gets the width of the display used to draw the network
	 * 
	 * @return width of display component in pixels
	 */
	public int getDisplayWidth() {
		return LayoutArea.getWidth();
	}

	/**
	 * gets the height of the display used to draw the network
	 * 
	 * @return height of display component in pixels
	 */
	public int getDisplayHeight() {
		return LayoutArea.getHeight();
	}

	/**
	 * sets the pixel dimensions of the component used to plot the network,
	 * tries to adjust the frame size to match
	 * 
	 * @param width
	 * @param height
	 */
	public void setDisplaySize(int width, int height) {
		// get the difference between existing layout size and frame size
		int widthDif = this.getWidth() - LayoutArea.getWidth();
		int heightDif = this.getHeight() - LayoutArea.getHeight();
		LayoutArea.setSize(new Dimension(width, height));
		this.setSize(width + widthDif, height + heightDif);
		this.validate();
	}

	/**
	 * repaints the cached image of the last time the network was drawn to the
	 * screen and hopefully the rest of the gui components
	 */
	// public void paint(Graphics g)
	// {
	// //this will just redraw the old image of the network to same time
	// LayoutArea.paint(g);
	// }
	/*
	 * public void invalidate() { super.invalidate(); LayoutArea.invalidate();
	 * //debug System.out.println("layout window invalidated"); }
	 */

	public void internalFrameClosing(InternalFrameEvent e) {
		// ask if this is a good idea
		int result = JOptionPane
				.showConfirmDialog(
						this,
						"Closing this window will discard the layouts for all of its slices",
						"Close layout without saving?",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE);
		if (result == 0) {
			this.setVisible(false);
			// should make sure to kill off all compondents
			Control.disposeEngine(engine);
			graphicsSettings = null;
			this.dispose();
		}
	}
	
	

	
	/**
	 * reads the browse settings from the gui and stores them in the properties
	 * object
	 * 
	 * @author skyebend
	 */
	private void recordBrowseSettings() {
		if (browseSettings == null) {
			browseSettings = new BrowsingSettings();
		}
		browseSettings.setDoubleProperty(BrowsingSettings.RENDER_DURATION,
				RenderDuration.getText().trim());
		browseSettings.setDoubleProperty(BrowsingSettings.RENDER_OFFSET,
				renderOffset.getText().trim());
		browseSettings.setDoubleProperty(BrowsingSettings.INTERP_FRAMES,
				NumInterps.getText().trim());
		browseSettings.setDoubleProperty(BrowsingSettings.FRAME_DELAY,
				frameDelay.getText().trim());
		
	}

	/**
	 * reads browse settings from the properties object and set them in the gui,
	 * using the current gui values as defaults
	 * 
	 * @author skyebend
	 */
	private void fetchBrowseSettings() {
		if (browseSettings == null) {
			browseSettings = new BrowsingSettings();
		}
		RenderDuration.setText(browseSettings.getProperty(
				BrowsingSettings.RENDER_DURATION, RenderDuration.getText()
						.trim()));
		renderOffset.setText(browseSettings.getProperty(
				BrowsingSettings.RENDER_OFFSET, renderOffset.getText().trim()));
		NumInterps.setText(browseSettings.getProperty(
				BrowsingSettings.INTERP_FRAMES, NumInterps.getText().trim()));
		frameDelay.setText(browseSettings.getProperty(
				BrowsingSettings.FRAME_DELAY, frameDelay.getText().trim()));
	}

}