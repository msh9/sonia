package sonia.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MenuBar;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;


import sonia.LayoutSlice;
import sonia.LayoutUtils;
import sonia.NodeInspector;
import sonia.NodeMover;
import sonia.PlayAnimationTask;
import sonia.SoniaCanvas;
import sonia.SoniaController;
import sonia.SoniaLayoutEngine;
import sonia.layouts.FRLayout;
import sonia.layouts.GraphVizWrapperLayout;
import sonia.layouts.MultiCompKKLayout;
import sonia.layouts.NetLayout;
import sonia.mapper.Colormapper;
import sonia.movie.MovieMaker;
import sonia.settings.ApplySettings;
import sonia.settings.BrowsingSettings;
import sonia.settings.GraphicsSettings;

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

	/**
	 * 
	 */
	private static final long serialVersionUID = -4946402657442083297L;

	private SoniaController Control;

	private SoniaLayoutEngine engine;


	private GraphicsSettingsDialog graphicsSettings;

	private GraphicsSettings drawSettings;

	private BrowsingSettings browseSettings;

	private SoniaCanvas LayoutArea;

	private JPanel controlPanel;

	private JTabbedPane controlePane;

	private JButton ApplyLayout;

	private JButton ReApply;

	private JButton Stress;

	private JButton Stability;

	private JButton PhasePlot;

	private JButton NextSlice;

	private JButton PrevSlice;

	private JButton PlayAll;

	//private JButton Pause;

	private JButton ViewOptions;
	
	//private JButton makeClusters;
	
	//private JButton layoutOnce;
	
	private JButton zoom;
	private JTextField zoomFactor;
	
	private JButton rotate;
	private JTextField degrees;
	private JButton pan;
	private MouseAdapter panner = null;

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
	
	private JPanel stressPanel;
	private JPanel inspectPanel;
	private JPanel layoutPanel;
	private JPanel timelinePane ;
	
	
	private StressTimeline stressline;
	

	

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
	@SuppressWarnings("serial")
	public LayoutWindow(GraphicsSettings settings, BrowsingSettings browseSet,
			SoniaController controller, SoniaLayoutEngine layoutEng,
			int initWidth, int initHeight) {
		super.setDefaultCloseOperation(ExportableFrame.DO_NOTHING_ON_CLOSE);

		Control = controller;
		engine = layoutEng;
		drawSettings = settings;
		browseSettings = browseSet;
		exportMenu.setName("Export Slice");
		
		exportMenu.add(new AbstractAction("Export XML coords") {
			public void actionPerformed(ActionEvent arg0) {
				Control.exportXML(engine);
			}
		});
		exportMenu.add(new AbstractAction("Export GraphML") {
			public void actionPerformed(ActionEvent arg0) {
				Control.exportGraphML(engine);
			}
		});
		JMenu multipleExport = new JMenu("Export Network");
		menuBar.add(multipleExport);
		multipleExport.add(new AbstractAction("Export QuickTime Movie...") {
			public void actionPerformed(ActionEvent arg0) {
				// Control.exportMovie(engine, null);

				Control.exportQTMovie(engine, LayoutArea, null);
			}
		});

		multipleExport.add(new AbstractAction("Export Matricies...") {
			public void actionPerformed(ActionEvent arg0) {
				Control.exportMatricies(engine);
			}
		});

		multipleExport.add(new AbstractAction("Export Flash Movie...") {
			public void actionPerformed(ActionEvent arg0) {
				Control.exportFlashMovie(engine, LayoutArea, null);
			}
		});
		
		multipleExport.add(new AbstractAction("Export Image Sequence...") {
			public void actionPerformed(ActionEvent arg0) {
				Control.exportImageSequence(engine, LayoutArea, null);
			}
		});
		
		multipleExport.add(new AbstractAction("Export JPEG Animation (Uncompressed)...") {
			public void actionPerformed(ActionEvent arg0) {
				Control.exportJPEGMovie(engine, LayoutArea, null);
			}
		});
		
		multipleExport.add(new AbstractAction("Save As DyNetML xml ..") {
			public void actionPerformed(ActionEvent arg0) {
				recordBrowseSettings();
				Control.saveDyNetML(engine);
			}
		});
		
		//menu for various experimental options that don't fit elsewhere
		JMenu experimental = new JMenu("Experimental");
		menuBar.add(experimental);
		experimental.add(new AbstractAction("Apply Graphviz Layout Once...") {
			public void actionPerformed(ActionEvent arg0) {
				//TODO:show a list of layouts to choose from
				NetLayout layout = new GraphVizWrapperLayout(Control, engine);
				//show a settings dialog for that layout
				ApplySettings settings = new ApplySettings();
				ApplySettingsDialog dialog = new ApplySettingsDialog(settings, Control, engine, null, layout);
				dialog.showDialog();
				//tell the engine to do one layout of that option
				engine.applyLayoutOnce(layout, settings);
				updateDisplay();
			}
		});
		experimental.add(new AbstractAction("Create Modularity clusters for slice"){
			public void actionPerformed(ActionEvent e) {
				engine.createModularityClustersForSlice();
				updateDisplay();
			}
		});
		
	
		// this.setFont(controller.getFont());
		// create layout objects
		controlPanel = new JPanel();
		controlPanel.setBorder(new EtchedBorder());
		controlPanel.setName("view");
		ApplyLayout = new JButton("Apply Layout..");
		ReApply = new JButton("Re-Apply");
		Stress = new JButton("Slice Stress...");
		PhasePlot = new JButton("PhasePlot");
		Stability = new JButton("Stability");
		NextSlice = new JButton(">|");
		NextSlice.setToolTipText("Advance to next slice");
		PrevSlice = new JButton("|<");
		PrevSlice.setToolTipText("Reverse to previous slice");
		PlayAll = new JButton("> / ||");
		PlayAll.setToolTipText("Play or pause animation");
		//Pause = new JButton("||");
		ViewOptions = new JButton("View Options..");
	//	makeClusters = new JButton("Make Clusters");
	//	layoutOnce = new JButton("Other Layout...");
		MoveNodes = new JButton("Move Nodes");
		zoom = new JButton("Scale layout");
		zoomFactor = new JTextField("1.5",3);
		zoomFactor.setBorder(new TitledBorder("zoom factor"));
		zoomFactor.setToolTipText("factor by which the current slice's layout should be enlarge or reduced");
		rotate = new JButton("Rotate layout");
		degrees = new JTextField("45",3);
		degrees.setBorder(new TitledBorder("rotate degrees"));
		degrees.setToolTipText("degrees that the current slice's layout should be rotated");
		pan = new JButton("Pan layout");
		
		RenderTime = new JTextField("0", 4);
		RenderTime.setBorder(new TitledBorder("render time"));
		RenderTime.setToolTipText("Start of current render time bin");
		RenderDuration = new JTextField("0.1", 3);
		RenderDuration.setBorder(new TitledBorder("duration"));
		RenderDuration.setToolTipText("Size of time bin used for viewing");
		renderOffset = new JTextField("0", 3);
		renderOffset.setBorder(new TitledBorder("offset"));
		renderOffset.setToolTipText("position of render start within slice");
		LayoutNum = new JTextField("0", 3);
		LayoutNum.setBorder(new TitledBorder("slice"));
		LayoutNum.setToolTipText("Index of slices used for coordinates");
		// LayoutLabel = new JLabel("Layout (slice) #:");
		LayoutArea = new SoniaCanvas(drawSettings, engine);
		LayoutArea.setBackground(Color.WHITE);

		graphicsSettings = new GraphicsSettingsDialog(LayoutArea.getSettings(),
				Control, engine, null, LayoutArea);

		// NumInterpLabel = new JLabel("num. interp frames");
		NumInterps = new JTextField("10", 3);
		NumInterps.setBorder(new TitledBorder("interp frames"));
		NumInterps
				.setToolTipText("How many in-between frames to use in transition");
		frameDelay = new JTextField("30", 3);
		frameDelay.setBorder(new TitledBorder("delay"));
		frameDelay.setToolTipText("How long to wait between frames");

		// LAYOUT
		this.getContentPane().setLayout(new BorderLayout());

		// add components to the layout GBlayout using constraints
		//will reuse this object for many layouts
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 2, 0, 2);
		// set up top level components
		// c.gridx=0;c.gridy=0;c.gridwidth=7;c.gridheight=1;c.weightx=1;c.weighty=1;
		// c.fill=c.BOTH;
		//LayoutArea.setSize(initWidth,initHeight);
		//LayoutArea.setMinimumSize(new Dimension(initWidth,initHeight));
		getContentPane().add(LayoutArea, BorderLayout.CENTER);
	
		controlePane = new JTabbedPane(JTabbedPane.BOTTOM);
		controlePane.add(controlPanel,0);
		
		NodeInspector inspector = new NodeInspector(Control,engine, LayoutArea);
		inspectPanel = inspector.getInspectPanel();
		inspectPanel.setName("inspect");
		controlePane.add(inspectPanel,1);
		controlePane.addChangeListener(inspector);
		
		layoutPanel = new JPanel(new GridBagLayout());
		layoutPanel.setName("layout");
		//UGLY HACK, GET RID OF THIS
		c.gridx=0;c.gridy=0;c.gridwidth=2;c.fill=GridBagConstraints.BOTH;c.weightx=1;c.weighty=1;
		if (engine.getLayout().getClass().equals(MultiCompKKLayout.class)){
			layoutPanel.add(((MultiCompKKLayout)engine.getLayout()).getSchedule().getContentPane(),c);
		} else if (engine.getLayout().getClass().equals(FRLayout.class)){
			layoutPanel.add(((FRLayout)engine.getLayout()).getSchedule().getContentPane(),c);
		}
		
		c.gridx=0;c.gridy=1;c.gridwidth=1;c.weightx=.1;c.weighty=.1;c.fill=GridBagConstraints.NONE;
		layoutPanel.add(ApplyLayout,c);
		c.gridx=1;c.gridy=1;c.gridwidth=1;
		layoutPanel.add(ReApply,c);
		controlePane.add(layoutPanel,2);
		
		//JPanel timelinePanel = new JPanel(new GridLayout());
		//timelinePanel.add(engine.getPhasePlot().getContentPane());
		//timelinePanel.setName("timeline");
		timelinePane = new JPanel(new GridBagLayout());
		c.gridx=0;c.gridy=0;c.gridwidth=10;c.fill=GridBagConstraints.BOTH;c.weightx=1;c.weighty=1;
		timelinePane.add(engine.getPhasePlot().getContentPane(),c);
		timelinePane.setName("timeline");
		c.gridx=0;c.gridy=1;c.gridwidth=1;c.weightx=0;c.weighty=0;c.fill=GridBagConstraints.NONE;
		timelinePane.add(PrevSlice,c);
		c.gridx=1;c.gridy=1;
		timelinePane.add(NextSlice,c);
		c.gridx=2;c.gridy=1;
		timelinePane.add(PlayAll,c);
		//c.gridx=4;c.gridy=1;
		//timelinePane.add(Pause,c);
		controlePane.add(timelinePane,3);
		c.gridx = 3;c.weightx=1;c.fill=GridBagConstraints.BOTH;
		timelinePane.add(LayoutNum,c);
		c.gridx=4;
		timelinePane.add(RenderTime, c);
		c.gridx = 5;
		timelinePane.add(RenderDuration, c);
		c.gridx = 6;
		timelinePane.add(renderOffset, c);
		c.gridx = 7;
		timelinePane.add(NumInterps, c);
		c.gridx = 8;
		timelinePane.add(frameDelay, c);
		
		//stress panel
		stressPanel = new JPanel(new GridBagLayout());
		stressPanel.setName("stress");
		c.gridx=0;c.gridy=0;c.gridwidth=1;c.weightx=.1;c.weighty=0;c.fill=GridBagConstraints.NONE;
		stressPanel.add(Stress,c);
		stressline = new StressTimeline(engine);
		c.gridx=0;c.gridy=1;c.gridwidth=1;c.weightx=1;c.weighty=1;c.fill=GridBagConstraints.BOTH;
		stressPanel.add(stressline,c);

		controlePane.add(stressPanel,4);
		
		// try to make it so we can tear off tabs
		controlePane.addMouseMotionListener(new MouseMotionListener(){
			Point dragStart = null;
			public void mouseDragged(MouseEvent e) {
			  	if (dragStart==null){
				   dragStart = e.getPoint();
				   return; // don't do anythin
				}
			  	// Get the current position
			  	Point mousePosition = e.getPoint();
// If the drag distance is big enough, then pass on a tear
				if (Math.hypot(dragStart.x-mousePosition.x,dragStart.y-mousePosition.y)>50){
					dragStart=null;
				JInternalFrame frame = new JInternalFrame(
						controlePane.getSelectedComponent().getName(),true,true,true,true);
				frame.getContentPane().add(controlePane.getSelectedComponent());
				frame.setBounds(e.getComponent().getBounds());
				frame.setLocation(controlePane.getLocationOnScreen());
				//add a listener (within the listener) that puts the frame back in the tabb if closed
				frame.addInternalFrameListener(new InternalFrameAdapter(){
					@Override
					public void internalFrameClosed(InternalFrameEvent e) {
						Component comp =e.getInternalFrame().getContentPane();
						comp.setName(e.getInternalFrame().getTitle());
						controlePane.add(comp);
					}
				});
				Control.showFrame(frame);
				//System.out.println("frame removed");
				// e.getComponent().removeMouseMotionListener(this);
				
			}
			}

			public void mouseMoved(MouseEvent e) {
				dragStart=null;
			}
		});
		
	
		getContentPane().add(controlePane, BorderLayout.SOUTH);
		controlePane.setPreferredSize(new Dimension(250, 150));

		GridBagLayout layout = new GridBagLayout();
		controlPanel.setLayout(layout);
		c.insets = new Insets(0, 2, 0, 2);
		c.fill=GridBagConstraints.NONE;
		c.gridx = 0;c.gridy = 0;c.gridwidth = 1;c.gridheight = 1;c.weightx = 0.5;c.weighty = 0.0;
		controlPanel.add(ViewOptions, c);
		c.gridx = 0;c.gridy = 1;c.gridwidth = 1;c.gridheight = 1;c.weightx = 0.5;c.weighty = 0.0;
		//controlPanel.add(makeClusters, c);  //hide this for now so don't have to support or explain...
	//	controlPanel.add(layoutOnce, c);
		
		// buttons
		c.gridx = 1;c.gridy = 0;c.gridwidth = 1;c.gridheight = 1;c.weightx = 0.5;	c.weighty = 0.0;
		controlPanel.add(zoom, c);
		
		c.gridx = 2;c.gridy = 0;c.gridwidth = 1;c.gridheight = 1;c.weightx = 0.75;c.weighty = 0.0;c.fill=GridBagConstraints.BOTH;
		controlPanel.add(zoomFactor, c);
		
		c.gridx = 2;c.gridy = 1;c.gridwidth = 1;c.gridheight = 1;c.weightx = 0.75;c.weighty = 0.0;c.fill=GridBagConstraints.BOTH;
		controlPanel.add(degrees,c);
		
		c.gridx = 1;c.gridy = 1;c.gridwidth = 1;c.gridheight = 1;c.weightx = 0.5;c.weighty = 0.0;c.fill=GridBagConstraints.NONE;
		controlPanel.add(rotate, c);
	
		
		c.gridx = 1;c.gridy = 2;c.gridwidth = 1;c.gridheight = 1;c.weightx = 0.5;c.weighty = 0.0;
		controlPanel.add(pan, c);
	
		c.gridx = 2;c.gridy = 2;c.gridwidth = 1;c.gridheight = 1;c.weightx = 0.5;c.weighty = 0.0;
		controlPanel.add(MoveNodes, c);
		// buttons and controls
		// c.gridx=1;c.gridy=2;c.gridwidth=1;c.gridheight=1;c.weightx=0.1;c.weighty=0.0;
		// add(LayoutLabel,c);
		c.gridx = 3;c.gridy = 2;c.gridwidth = 1;c.gridheight = 1;c.weightx = 0.1;c.weighty = 0.0;


		// add action listeners for button clicks
		ApplyLayout.addActionListener(this);
		ReApply.addActionListener(this);
		Stress.addActionListener(this);
		Stability.addActionListener(this);
		PhasePlot.addActionListener(this);
		ViewOptions.addActionListener(this);
	//	makeClusters.addActionListener(this);
	//	layoutOnce.addActionListener(this);
		MoveNodes.addActionListener(this);
		NextSlice.addActionListener(this);
		PrevSlice.addActionListener(this);
	//	Pause.addActionListener(this);
		PlayAll.addActionListener(this);

		RenderTime.addActionListener(this);
		RenderDuration.addActionListener(this);
		LayoutNum.addActionListener(this);
		NumInterps.addActionListener(this);
		zoom.addActionListener(this);
		rotate.addActionListener(this);
		pan.addActionListener(this);
		// NEED A LISTENER FOR THE TXT FIELD

		addInternalFrameListener(this);
		//add key listener to map arrow keys to next and prev
		controlePane.addKeyListener( new KeyAdapter(){
			public void keyPressed(KeyEvent k){
				Control.setPaused(false);
				if (k.getKeyCode() == KeyEvent.VK_RIGHT){
					startFwdTransThread();
				}
				if (k.getKeyCode()== KeyEvent.VK_LEFT){
					startRevTransThread();
				}
				if (k.getKeyCode()== KeyEvent.VK_DOWN){
					transitionToSlice(0,null);
				}
				if (k.getKeyCode()== KeyEvent.VK_UP){
					transitionToSlice(engine.getNumSlices()-1,null);
				}
				if (k.getKeyCode()== KeyEvent.VK_ENTER){
					if (k.isShiftDown()){
						applyLayout();
					} else {
					 reApplyLayout();
					}
				}
				if (k.getKeyCode() ==KeyEvent.VK_G){
					if (k.isControlDown()){
						graphicsSettings.showDialog();
					}
				}
				
				if (k.getKeyCode()== KeyEvent.VK_SPACE){
				if(engine.isTransitionActive()){
					Control.setPaused(true);
				} else if (Control.isPaused()){
					Control.setPaused(false);
				} else {
				   // startPlayThread();
					playAll();
				}
					
				}
				k.consume();
			}
		});
		
	
		
		// read settings from the browsng properties
		fetchBrowseSettings();
		//and then make sure they've all been stored
		recordBrowseSettings();

		// this.setBackground(Color.lightGray);
		this.setSize(initWidth, initHeight);
		///this.pack();
		this.setTitle(engine.toString());
		this.setLocation(10, 10);
		controlePane.setSelectedComponent(layoutPanel);
		controlePane.requestFocusInWindow();
		//this.setVisible(true);
		
	

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
			engine.getPhasePlot();
		} else if (evt.getSource().equals(NextSlice)) {
			if (!engine.isTransitionActive()) {
				// transitionToSlice(engine.getCurrentSliceNum()+1);
				startFwdTransThread();
			}
		} else if (evt.getSource().equals(PlayAll)) {
			if (Control.isPaused()) {
				Control.setPaused(false);
			}
			if (!engine.isTransitionActive()) {
				
				//startPlayThread();
				playAll();
			} else {
					Control.setPaused(true);
				}
		} else if (evt.getSource().equals(PrevSlice)) {
			if (!engine.isTransitionActive()) {
				startRevTransThread();
			} 
			
		} else if (evt.getSource().equals(ViewOptions)) {
			//in case the window size has changed, make sure the engine knows about it
			engine.setDisplayHeight(LayoutArea.getHeight());
			engine.setDisplayWidth(LayoutArea.getWidth());
			graphicsSettings.showDialog();
			updateDisplay();
		} else if (evt.getSource().equals(LayoutNum)) {
			if (!engine.isTransitionActive()) {
				goToSlice(Integer.parseInt(LayoutNum.getText()));
			}
		} else if (evt.getSource().equals(RenderTime)) {
			if (!engine.isTransitionActive()) {
				renderCurrentSettings();
			}
		} else if (evt.getSource().equals(RenderDuration)) {
			if (!engine.isTransitionActive()) {
				renderCurrentSettings();
			}
		} else if (evt.getSource().equals(NumInterps)) {
			if (!engine.isTransitionActive()) {
				engine.setInterpFrames(Integer.parseInt(NumInterps.getText()));
				// allso reset the render duration
				LayoutSlice slice = engine.getCurrentSlice();
				double duration = (slice.getSliceEnd() - slice.getSliceStart())
						/ (double) engine.getInterpFrames();
				RenderDuration.setText(duration + "");
				renderCurrentSettings();
			}
		} else if (evt.getSource().equals(MoveNodes)) {
			if (!engine.isTransitionActive()) {
				moveNodes();
			}
		} else if (evt.getSource().equals(zoom)){
			double factor = Double.parseDouble(zoomFactor.getText());
			LayoutUtils.scaleLayout(engine.getCurrentSlice(),factor,
					engine.getDisplayWidth(),engine.getDisplayHeight());
			Control.log("scaled layout for slice "+engine.getCurrentSliceNum()
					+" by a factor of "+factor);
			engine.setCoordsToSlice(engine.getCurrentSliceNum());
			updateDisplay();
		} else if (evt.getSource().equals(rotate)){
			double deg = Double.parseDouble(degrees.getText());
			LayoutUtils.rotateLayout(engine.getCurrentSlice(),deg,
					engine.getDisplayWidth(),engine.getDisplayHeight());
			Control.log("rotated layout for slice "+engine.getCurrentSliceNum()
					+" by "+deg+" degrees.");
			engine.setCoordsToSlice(engine.getCurrentSliceNum());
			updateDisplay();
		} else if (evt.getSource().equals(pan)){
			if (panner == null){
				pan.setText("Stop panning");
				engine.setTransitionActive(true);
				panner = new MouseAdapter(){
					int deltax=0;
					int deltay=0;
					Point press = null;
					@Override
					public void mousePressed(MouseEvent e) {
						press = e.getPoint();
					}
					@Override
					public void mouseReleased(MouseEvent e) {
						Point release = e.getPoint();
						deltax = release.x - press.x;
						deltay = release.y - press.y;
						LayoutUtils.panLayout(engine.getCurrentSlice(),deltax,deltay);
						engine.setCoordsToSlice(engine.getCurrentSliceNum());
						updateDisplay();
					}
				};
				LayoutArea.addMouseListener(panner);
				LayoutArea.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			} else {
				pan.setText("Pan Layout");
				LayoutArea.removeMouseListener(panner);
				panner = null;
				LayoutArea.setCursor(Cursor.getDefaultCursor());
				engine.setTransitionActive(false);
			}
		}
		recordBrowseSettings();
		controlePane.requestFocusInWindow();
	}





	/**
	 * Transitions through the slices, starting at the current slice. Called by
	 * the play thread. Uses transitionToSlice.
	 */
	public void playAll() {
		selectPane(timelinePane);
		PlayAnimationTask playTask = new PlayAnimationTask(engine);
		Control.runTask(playTask);
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
				engine.setTransitionActive(true);
				transitionToSlice(destSlice,null);
				engine.setTransitionActive(false);
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
				engine.setTransitionActive(true);
				transitionToSlice(destSlice,null);
				engine.setTransitionActive(false);
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
			controlePane.requestFocusInWindow();
		}
	}
	
	
	/**
	 * flips to the specified component in the jtabbed pane, if it exists
	 * @author skyebend
	 * @param pane
	 */
	private boolean selectPane(JComponent pane){
		try{
			controlePane.setSelectedComponent(pane);
		} catch (IllegalArgumentException e){
			return false;
		}
		return true;
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
//		show  the layout
		selectPane(layoutPanel);
		// apply layout
		engine.showApplyLayoutSettings();
		// Render the entire slice as one block, from start to finish
		RenderTime.setText("" + currentSlice.getSliceStart());
		RenderDuration.setText(""
				+ (currentSlice.getSliceEnd() - currentSlice.getSliceStart()));
		LayoutArea.setRenderSlice(engine.getRenderSlice(currentSlice
				.getSliceStart(), currentSlice.getSliceEnd()));
		
		updateDisplay();
		controlePane.setSelectedComponent(layoutPanel);
		controlePane.requestFocusInWindow();
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
//		show  the layout
		selectPane(layoutPanel);
		// apply layout
		engine.applyLayoutToCurrent();
	
		
		// Render the entire slice as one block, from start to finish
		RenderTime.setText("" + currentSlice.getSliceStart());
		RenderDuration.setText(""
				+ (currentSlice.getSliceEnd() - currentSlice.getSliceStart()));
		LayoutArea.setRenderSlice(engine.getRenderSlice(currentSlice
				.getSliceStart(), currentSlice.getSliceEnd()));
		updateDisplay();
		controlePane.setSelectedComponent(layoutPanel);
		controlePane.requestFocusInWindow();
	}
	
	public void showRender(double renderStart, double renderEnd){
		LayoutArea.setRenderSlice(engine.getRenderSlice(renderStart,renderEnd));
		// update the text fields
		RenderTime.setText("" + renderStart);
		RenderDuration.setText(""
				+ (renderEnd - renderStart));
		
		// now ask the network to redraw isteslf
		updateDisplay();
		controlePane.requestFocusInWindow();
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
		showRender(currentSlice
				.getSliceStart(), currentSlice.getSliceEnd());
		LayoutNum.setText("" + engine.getCurrentSliceNum());
		
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
	public void transitionToSlice(int number, MovieMaker movie) {
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
						// now the rendering is going to fast, so we have to
						// slow it
						// down

						try {
							Thread.sleep(engine.getFrameDelay());
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
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
		LayoutArea.repaint();
		timelinePane.repaint();
	}

	/**
	 * returns a reference to the SoniaCanvas used by this layout window
	 * 
	 * @author skyebend
	 * @return the layout area for network rendering in use by this window
	 */
	public SoniaCanvas getDisplay() {
		return LayoutArea;
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
		Dimension dim = new Dimension(width, height);
		LayoutArea.setSize(dim);
		LayoutArea.setMaximumSize(dim);
		LayoutArea.setPreferredSize(dim);
		LayoutArea.setMinimumSize(dim);
		//this.setSize(width + widthDif, height + heightDif);
		this.pack();
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
			Control.updateDisplays();
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