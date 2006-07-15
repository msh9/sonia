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

import java.util.*;
import java.awt.Dialog;
import java.awt.Frame;

import javax.swing.JFrame;

import sonia.layouts.CircleLayout;
import sonia.layouts.FRLayout;
import sonia.layouts.MetricMDSLayout;
import sonia.layouts.MultiCompKKLayout;
import sonia.layouts.OrigCoordLayout;
import sonia.layouts.PILayout;
import sonia.layouts.RandomFRLayout;
import sonia.layouts.RubBandFRLayout;

import cern.colt.matrix.impl.SparseDoubleMatrix2D;

/**
 * The main controlling class for a layout. Coordinates with the the
 * NetDataStructure to get the network data, stores it internally as a vector of
 * layoutSlices, positions the nodes using a NetLayout algorithm, does the
 * animation interpolation using the CoordInterpolator stratagey. Also returns
 * various information about the layout and network, and performs operations.
 * Provides renderslices to the layoutWindow to draw the network. Tracks various
 * settings. Ideally, the engine should be stand alone enough that it could be
 * called by a script, with no gui.
 */
public class SoniaLayoutEngine {
	private SoniaController control;

	private NetDataStructure netData;

	private LayoutWindow display; // eventually need to allow for multiples of

	// these

	private ArrayList layoutSlices; // slices, bins of all events to be in same

	private LayoutSettings layoutSettings;

	private ApplySettings applySettings;

	// private Vector renderSlices;
	private NetLayout currentLayout;

	private CoordInterpolator interpolator;

	private ApplySettingsDialog settings;

	private LayoutSettingsDialog windowSettings;

	private StressInfo shepPlot;

	private StabilityEstimate stability;

	private PhasePlot timePlot;

	private String engineName; // the name of this engine to display

	private boolean recenter = true; // settings dialog

	private boolean rescale = true;

	private boolean showUpdates = false;

	private int updatesN = 0;

	private int interpFrames = 0;

	private int frameDelay = 30; // how long to wait after each frame, in

	// milliseconds

	private float renderOffset = 0; // position of start of render relitive to

	// slice

	private int currentSlice;

	private double[] currentXcoords;

	private double[] currentYcoords;

	private int bottomPad = 95;

	private int pad = 10;

	private int dispInitWidth = 480;

	private int dispInitHeight = 440;

	private boolean[] errorSlices;

	private double maxMatrixValue;

	private double minMatrixValue;

	/**
	 * Class responsible for maintaining data structures for layouts,
	 * coordinating layout algorithms, suppling graphics to consumers, etc
	 * 
	 * @param controller
	 *            the main sonia controller
	 * @param data
	 *            the network data to divide into slices
	 * @param name
	 *            the name of the engine (to show in the window)
	 */
	public SoniaLayoutEngine(LayoutSettings settings,
			SoniaController controller, NetDataStructure data, String name) {
		control = controller;
		netData = data;
		engineName = name;
		layoutSettings = settings;

		// probably should ask for kind of layout here
		if (control.isShowGUI() & (settings == null)) {
			windowSettings = new LayoutSettingsDialog(layoutSettings, control,
					this, null);
			if (settings == null) {
				// tell the settings dialog what the start and end times for the
				// data
				// are
				windowSettings.setDataStartDefault(netData.getFirstTime());
				windowSettings.setDataEndDefault(netData.getLastTime());
			}
			// show the dialog
			settings = windowSettings.askUserSettings();
		}
		// debug
		System.out.println("settings " + settings);

		// take care of all the settings
		// make the right kinds of layout
		String layoutType = (String) settings.get(LayoutSettings.LAYOUT_TYPE);
		String animateType = (String) settings.get(LayoutSettings.ANIMATE_TYPE);
		String sliceAggregation = (String) settings
				.get(LayoutSettings.SLICE_AGGREGATION);
		NetLayout theLayout;
		if (layoutType.equals("circular layout")) {
			theLayout = new CircleLayout(control, this);
		} else if (layoutType.equals("FR layout")) {
			theLayout = new FRLayout(control, this);
		} else if (layoutType.equals("random FR layout")) {
			theLayout = new RandomFRLayout(control, this);
		} else if (layoutType.equals("Rubber-Band FR Layout")) {
			theLayout = new RubBandFRLayout(control, this);
		} else if (layoutType.equals("MultiComp KK Layout")) {
			theLayout = new MultiCompKKLayout(control, this);
		} else if (layoutType.equals("Moody PI layout")) {
			theLayout = new PILayout(control, this);
		} else if (layoutType.equals("MetricMDS (SVD)?")) {
			theLayout = new MetricMDSLayout(control, this);
		} else {
			theLayout = new OrigCoordLayout(control, this);
		}
		// make the right kind of coordinate interpolator
		CoordInterpolator interpolator;
		if (animateType.equals("cosine animation")) {
			interpolator = new CosineInterpolation();
		} else {
			interpolator = new NoInterpolation();
			this.setInterpFrames(0);
		}

		// figure out what kind of aggreation will be used
		int aggregateType = 0; // sum ties
		if (sliceAggregation.equals("Avg of i->j ties")) {
			aggregateType = 1;
		} else if (sliceAggregation.equals("Number  of i->j ties")) {
			aggregateType = 2;
		}

		// try {
		double sliceStart = Double.parseDouble((String) settings
				.get(LayoutSettings.SLICE_START));
		double sliceEnd = Double.parseDouble((String) settings
				.get(LayoutSettings.SLICE_END));
		double sliceDuration = Double.parseDouble((String) settings
				.get(LayoutSettings.SLICE_DURATION));
		double sliceDelta = Double.parseDouble((String) settings
				.get(LayoutSettings.SLICE_DELTA));
		// tell the engine to setup (get slices from netData, etc
		this.setupLayout(sliceStart, sliceEnd, sliceDuration, sliceDelta,
				aggregateType, theLayout, interpolator);
		// } catch (Exception e) {
		// // TODO: handle exception when bad slice settings are passed in
		// System.out.println("error parsing slice settings properties "
		// + e.toString());
		// }

	}

	public void setDisplay(LayoutWindow display) {
		this.display = display;
		// tell layout to DRAW the first layout without applying layout
		display.showCurrentSlice();
		display.setTitle(display.getTitle() + currentLayout.getLayoutType());
		// make sure the network gets drawn
		display.updateDisplay();
		// try to make the layout window active
		display.show();
	}

	/**
	 * Throws up the ApplySettingsDialog for the user to specify the details of
	 * how the chosen layout algorithm will run.
	 */
	public void showApplyLayoutSettings() {

		if (applySettings == null) {
			applySettings = new ApplySettings();
		}
		
		if (control.isShowGUI()) {
			if (settings == null) {
				settings = new ApplySettingsDialog(applySettings, control, this,
						null, currentLayout);
			}
			settings.showDialog();
		}

	}

	/**
	 * Creates and displays a PhasePlot showing the location of each arc and
	 * node event in time, and how the slices land.
	 */
	public void showPhasePlot() {
		// check if it exists
		if (timePlot == null) {
			timePlot = new PhasePlot(this, netData, layoutSettings);
		}
		control.showFrame(timePlot);
		// timePlot.show();
	}

	/**
	 * if there is already a phase plot for this engine, it will be returned,
	 * Otherwise null will be returned.
	 */
	public PhasePlot getPhasePlot() {

		return timePlot;
	}

	/**
	 * Sets up the layout and data sctructures, asking netData to generate a
	 * vector of slices from the given range, finding max values (to be used in
	 * converting similarity to disimilarity matrix), shows slice, etc.
	 * 
	 * @param sliceStart
	 *            start time for the range of slices
	 * @param sliceEnd
	 *            end time for the range of slices
	 * @param sliceDuration
	 *            how long ("thick") the slices are
	 * @param sliceDelta
	 *            the offset between the start of succesive slices
	 * @param aggregateType
	 *            the method used to aggregate ties within a slice
	 * @param layout
	 *            the layout algorithm to position the nodes
	 * @param interp
	 *            the coordinate interpolator to be used for animating
	 *            transitions
	 */
	public void setupLayout(double sliceStart, double sliceEnd,
			double sliceDuration, double sliceDelta, int aggregateType,
			NetLayout layout, CoordInterpolator interp) {
		// asks user for params
		// gets layoutslices from netData
		layoutSlices = netData.getLayoutSlices(sliceStart, sliceEnd,
				sliceDuration, sliceDelta, aggregateType);
		// set up array to hold list of slices with errors
		errorSlices = new boolean[layoutSlices.size()];
		// figure out the max and min values for later use
		double[] maxMin = NetUtils.getAllSliceMaxMin(layoutSlices);
		maxMatrixValue = maxMin[0];
		minMatrixValue = maxMin[1];
		// construt appropriate layout
		currentLayout = layout;
		interpolator = interp;

	}

	/**
	 * Checks if the passed slice number exists, then changes the current slice
	 * number.
	 * 
	 * @param num
	 *            the slice number to change to
	 */
	public void changeToSliceNum(int num) {
		if (num > layoutSlices.size() - 1) {
			control.showError("There is no slice number " + num);
			// or gray out button
		} else if (num < 0) {
			control.showError("There is no slice number " + num);
			// should beep or something
			// or gray out button
		} else {
			currentSlice = num;
			repaintDisplays();
		}
	}

	/**
	 * Applies the layout to the current slice. Called by the "Re-apply" button.
	 * Uses the current layout settings, or calles the ApplyLayoutSettings
	 * dialog if they have not been set.
	 */
	public void applyLayoutToCurrent(ApplySettings settings) {
		if (settings == null) {
			showApplyLayoutSettings();
		} else {
			applyLayoutTo(settings, (LayoutSlice) layoutSlices.get(currentSlice));
		}
	}

	/**
	 * Starts an independent thread to run the layout algorithms, this allows
	 * screen redraws and events (like pause) to take place. Thread calls
	 * startApplyLayoutToRemaining()
	 */
	public void applyLayoutToRemaining() {
		Thread layoutRunner = new Thread() {
			public void run() {
				startApplyLayoutToRemaining();
			}
		};
		layoutRunner.setName("apply layout to remaining");
		layoutRunner.setPriority(5);
		layoutRunner.start();
	}

	/**
	 * Calls applyLayoutTo(slice) on the current slice and all remaining slices,
	 * chaning the engine to each slice before continuing. Called by the layout
	 * thread generated with applyLayoutToRemaining. Should be private?
	 */
	private void startApplyLayoutToRemaining() {
		// will throw up a settings window, but for now, apply the current
		// layout to all subsequent layouts
		// makesure there is a layout chosen
		int startSlice = currentSlice;
		int slicesLeft = layoutSlices.size() - currentSlice;
		//get the settings
		if (applySettings == null){
			applySettings = settings.getSettings();
		}
		control.showStatus("Applying layouts to slices " + startSlice + " to "
				+ startSlice + slicesLeft);
		// PROBLEMS WITH THREADING, NEED TO MAKE SURE THAT IF THEY ARE DEPENDENT
		// ONE LAYOUT FINISHES BEFORE NEXT STARTS
		// SHOULD CHECK THAT THE prev slice is finished
		LayoutSlice slice = getCurrentSlice();
		applyLayoutTo(applySettings,slice);
		startSlice++;
		slicesLeft--;
		while ((slicesLeft > 0) & (!control.isPaused())) {
			// check that layout is finished bfore startign next
			if (slice.isLayoutFinished()) {
				// check if we should break for errors
				if (applySettings.get(ApplySettings.STOP_ON_ERROR).equals(Boolean.toString(true))
					& slice.isError()) {
					slicesLeft = 0;
					break;
				} else // go ahead with the next layout
				{
					changeToSliceNum(startSlice);
					slice = getCurrentSlice();
					applyLayoutTo(applySettings, slice);
					//TODO: decide if there should be a repaint call here, check the repaintN?
					display.updateDisplay();
					startSlice++;
					slicesLeft--;
				}
			}
		}

	}

	/**
	 * Applies a layout algorithm to the passed slice. First checks that no
	 * other layout is working on the slice, then locks it, gets the current
	 * screen dimensions of the layout, sets the starting coordinates of the
	 * slice to the appotion specified in the settings dialog, asks the layout
	 * algorithm to start, and records the starting info in the log.
	 * 
	 * @param slice
	 *            the slice which the layout will be applied to
	 */
	public void applyLayoutTo(ApplySettings settings, LayoutSlice slice) {
		// check if slice is in use
		if (!slice.isLayoutFinished()) {
			control.showError("Slice layout is not finished");
		} else if (control.isPaused()) {
			control.showError("Paused. click || or resume to continue");
		} else {

			// lock slice so other layouts will not be applied
			// layout must set finished back to true when done.
			slice.setLayoutFinished(false);

			// reset error if there was one
			slice.setError(false);

			// figure out what the actuall displayable area is
			// PROBABLY LAYOUT SHOULD WORK ON AN COORD SYSTEM INDEPENT OF
			// DISPLAY
			int width = getLayoutWidth();
			int height = getLayoutHeight();
			int thisIndex = layoutSlices.indexOf(slice);
			// apply any start coordinte options
			if (settings.get(ApplySettings.STARTING_COORDS).equals(ApplySettings.COORDS_RANDOM)) {
				LayoutUtils.randomizeLayout(control, slice, width, height);
			} else if (settings.get(ApplySettings.STARTING_COORDS).equals(ApplySettings.COORDS_CIRCLE)) {
				LayoutUtils.circleLayout(slice, width, height);
			} else if (settings.get(ApplySettings.STARTING_COORDS).equals(ApplySettings.COORDS_FROM_PREV)) {
				// make sure there is a previous slice
				if (thisIndex > 0) {
					LayoutSlice prevSlice = (LayoutSlice) layoutSlices
							.get(thisIndex - 1);
					// ASSUMES SLICES ARE THE SAME SIZE AND IN SAME ORDER!!
					// ALSO ASSUMES PREVIOUS SLICE IS FINISHED
					LayoutUtils.copyLayout(prevSlice, slice);
				}

			} else if (settings.get(ApplySettings.STARTING_COORDS).equals(ApplySettings.COORDS_FROM_FILE)) {
				// since slices is initialized with coords from orig file,
				// get a new slice with the same times, and copy the coords
				LayoutSlice copySlice = netData.makeLayoutSlice(slice
						.getSliceStart(), slice.getSliceEnd(), 0);
				// aggregate type doesn't matter since we only want coords
				LayoutUtils.copyLayout(copySlice, slice);
			}

			// incase there are redraws in the processes...
			// make sure we are looking at the right slice
			// setCoordsToSlice(thisIndex);
			// makesure the display is current THIS WILL SLOW THINGS DOWN
			// display.updateDisplay();
			// ask the layout to calculate the cordinates
			currentLayout.applyLayoutTo(slice, width, height, settings);

			// print out layout info to log
			control.showStatus("Starting layout on slice "
					+ layoutSlices.indexOf(slice) + "\nstart time:"
					+ slice.getSliceStart() + "\nend time:"
					+ slice.getSliceEnd());

		}
	}

	/**
	 * Does any final repositioning (rescaling, recentering, Isolate
	 * repositioning, etc) of the nodes, logs layout settings and results, and
	 * sets the slice to "finished" status, updates the display.
	 * @param settings TODO
	 * @param layout
	 *            the layout to finish
	 * @param slice
	 *            the slice being worked on
	 * @param width
	 *            the width (in pixels) of the layout area
	 * @param height
	 *            the height (in pixels) of the layout area
	 */
	public void finishLayout( ApplySettings settings, NetLayout layout, LayoutSlice slice,
			double width, double height) {
		// check for rescale
		if (settings.getProperty(ApplySettings.RESCALE_LAYOUT).equals(ApplySettings.RESCALE_TO_FIT)) {
			LayoutUtils.rescalePositions(slice, (int) width, (int) height,
					slice.getXCoords(), slice.getYCoords(), 
					Boolean.parseBoolean(settings.getProperty(ApplySettings.TRANSFORM_ISOLATE_EXCLUDE)));
		}
		// check for recenter
		if (settings.getProperty(ApplySettings.RECENTER_TRANSFORM).equals(ApplySettings.RECENTER_AFTER)) {
			LayoutUtils.centerLayout(slice, (int) width, (int) height, 
					Boolean.parseBoolean(settings.getProperty(ApplySettings.TRANSFORM_ISOLATE_EXCLUDE)));
		} else if (settings.getProperty(ApplySettings.RECENTER_TRANSFORM).equals(ApplySettings.BARYCENTER)) {
			LayoutUtils.barycenterLayout(slice, (int) width, (int) height,
					Boolean.parseBoolean(settings.getProperty(ApplySettings.TRANSFORM_ISOLATE_EXCLUDE)));
		}

		// take care of isolates if necessary, otherwise ignore
		if (settings.getProperty(ApplySettings.ISOLATE_POSITION).equals(ApplySettings.ISLOLATE_CIRCLE)) {
			LayoutUtils.pinIsolatesCircle(slice, (int) width, (int) height);
		} else if (settings.getProperty(ApplySettings.ISOLATE_POSITION).equals(ApplySettings.ISLOLATE_EDGE)) {
			LayoutUtils.pinIsolatesBottom(slice, (int) width, (int) height);
		} else if (settings.getProperty(ApplySettings.ISOLATE_POSITION).equals(ApplySettings.ISLOLATE_FILE)) {
			LayoutUtils.pinIsolatesOrig(slice);
		}
		// check if there were errors
		errorSlices[currentSlice] = slice.isError();

		setCoordsToSlice(layoutSlices.indexOf(slice));
		control.updateDisplays();
		slice.setLayoutFinished(true);

		control.showStatus("Slice " + layoutSlices.indexOf(slice)
				+ " Layout finished");
		control.log("Applied layout to slice " + layoutSlices.indexOf(slice)
				+ "\n start time:" + slice.getSliceStart() + "\n end time:"
				+ slice.getSliceEnd() + "\n " + settings.toString());
	}

	/**
	 * Does stability and stress (modified version of Kruskal's stress, for
	 * comparing screen distances to desired matrix distances) calculations for
	 * current slice, displays them as a shepard's plot in the stress window. If
	 * there is no stress window, it makes a new StressInfo object/window.
	 */
	public void calcStress() {
		// double optDist = settings.get; //temporary test!!
		// double stress =
		// NetUtils.getStress((LayoutSlice)layoutSlices.get(currentSlice));
		// control.showStatus("Kruskal's Stress:"+stress);
		if (shepPlot == null) {
			shepPlot = new StressInfo(control, this);
		}
		shepPlot.shepardPlot(getCurrentSlice());
		control.showFrame(shepPlot);
	}

	public void calcStability() {
		// debug
		System.out.println("calcStability called");
		if (stability == null) {
			stability = new StabilityEstimate(control, this);
		}
		stability.calcStability();
	}

	/**
	 * Creates a new RenderSlice, passes it to the NetDataStructure to get
	 * filled with the node and arc attributes that land in its range.
	 * 
	 * @param start
	 *            the start of the RenderSlice's range
	 * @param end
	 *            the end of the range
	 */
	public RenderSlice getRenderSlice(double start, double end) {
		RenderSlice render = new RenderSlice(this, start, end);
		netData.fillRenderSlice(render);

		// KLUDGE
		// draw the render slice on the phase plot, if it exits
		if (timePlot != null) {
			timePlot.showRenderSliceTimes(start, end);
		}

		return render;
	}

	/**
	 * Returns a double array of the current x coords for the current slice. if
	 * they have not been set yet, it returns the xcoords stored in the slice.
	 */
	public double[] getCurrentXCoords() {
		// if null just get coords from first layout slice
		if (currentXcoords == null) {
			currentXcoords = ((LayoutSlice) layoutSlices.get(currentSlice))
					.getXCoords();
		}
		return currentXcoords;
	}

	/**
	 * Returns a double array of the current y coords for the current slice. if
	 * they have not been set yet, it returns the ycoords stored in the slice.
	 */
	public double[] getCurrentYCoords() {
		// if null just get coords from first layout slice
		if (currentYcoords == null) {
			currentYcoords = ((LayoutSlice) layoutSlices.get(currentSlice))
					.getYCoords();
		}
		return currentYcoords;
	}
	
	public ApplySettings getCurrentApplySettings() {
		return applySettings;
	}

	/**
	 * Givin a start and end slice (with their layout coordinates) and a time
	 * (within the range of those slices) calculates new intermediate
	 * coordinates according to the current interpolation scheme, and stores
	 * them as the current x and y coords to be used when drawing the network to
	 * the screen.
	 * 
	 * @param startSlice
	 *            the slice interpolation will start from
	 * @param endSlice
	 *            the slice interpolation will end at
	 * @param time
	 *            the time for which to calculate the interpolation
	 */
	public void interpCoords(LayoutSlice startSlice, LayoutSlice endSlice,
			double time) {
		// WHAT HAPPENS WHEN SLICES OVERLAPP? Should we Average all the slices
		// within
		// that time?
		currentXcoords = interpolator.interpXCoords(startSlice, endSlice, time);
		currentYcoords = interpolator.interpYCoords(startSlice, endSlice, time);
	}

	/**
	 * Sets the current x and y screen coordinates for the nodes to the
	 * coordinates stored in the slice corresponding to the passed slice number.
	 * 
	 * @param sliceNum
	 *            the number of the slice to get coords from
	 */
	public void setCoordsToSlice(int sliceNum) {
		// check that sliceNumis within range
		if ((sliceNum >= 0) & (sliceNum < layoutSlices.size())) {
			currentXcoords = ((LayoutSlice) layoutSlices.get(sliceNum))
					.getXCoords();
			currentYcoords = ((LayoutSlice) layoutSlices.get(sliceNum))
					.getYCoords();
		}
	}

	/**
	 * Returns a Vector of matricies corresponding to each of the slices stored
	 * in the engine. Calls NetUtilities.getMatrix() on each slice. Called when
	 * exporting slices matricies to a text file for later analysis.
	 */
	public Vector getSliceMatricies() {
		Vector matrixVect = new Vector();
		for (int n = 0; n < layoutSlices.size(); n++) {
			LayoutSlice slice = (LayoutSlice) layoutSlices.get(n);
			matrixVect.add(NetUtils.getMatrix(slice));
		}
		return matrixVect;
	}

	/**
	 * Asks the LayoutWindow to update its display by calling updateDisplay() on
	 * it. Shuld ask other windows?
	 */
	public void updateDisplays() {
		display.updateDisplay();
		// should update the cooling scheduele as well? stress plot

	}

	/**
	 * Asks the additional windows to repaint
	 */
	private void repaintDisplays() {

		if (timePlot != null) {
			timePlot.repaint();
		}
		if (shepPlot != null) {
			shepPlot.shepardPlot(getCurrentSlice());
		}
		// display.repaint();
	}

	/**
	 * Asks the display to start making a movie with the passed SoniaMovieMaker
	 * by callinging makeMovie(exporter) on the LayoutWindow
	 * 
	 * @param exporter
	 *            the SoniaMovieMaker to save the movie frames to
	 */
	public void makeMovie(SoniaMovieMaker exporter) {
		display.makeMovie(exporter);
	}

	/**
	 * Asks the current layout to pause
	 */
	public void pause() {
		currentLayout.pause();
	}

	/**
	 * Ask the current layout to un-pause (set pause flag to false)
	 */
	public void resume() {
		currentLayout.resume();
	}

	// accessors-------------

	/**
	 * Asks the display for the width of the frame using getWidth(). if display
	 * doesn't exist yet, it returns the default initial values. result is in
	 * pixels
	 */
	public int getDisplayWidth() {
		int width = dispInitWidth;
		if (display != null) {
			width = display.getDisplayWidth();
		}
		return width;
	}

	/**
	 * Checks if the passed value is > 0, sets the with of the display
	 * (layoutWindow) to the value, sets the height to its current value. Asks
	 * the window to redo layout to position components.
	 * 
	 * @param width
	 *            the number of pixels wide to set the layout area
	 */
	public void setDisplayWidth(int width) {
		// check for non negitivge
		if (width > 0) {
			display.setDisplaySize(width, display.getDisplayHeight());
		}
	}

	/**
	 * Asks the display for the height of the frame using getHeight(). if
	 * display doesn't exist yet, it returns the default initial values. result
	 * is in pixels
	 */
	public int getDisplayHeight() {
		int height = dispInitHeight;
		if (display != null) {
			height = display.getDisplayHeight();
		}
		return height;
	}

	/**
	 * Checks if the passed value is > 0, sets the width of the display
	 * (layoutWindow) to the value (adding the bottomPad to make room for the
	 * buttons), sets the height to its current value. Asks the window to redo
	 * layout to position components.
	 * 
	 * @param height
	 *            the number of pixels high to set the layout area
	 */
	public void setDisplayHeight(int height) {
		// check for non negitivge
		if (height > 0) {
			display.setDisplaySize(display.getDisplayWidth(), height);
		}
	}

	/**
	 * Returns the pixel width of the area availible for laying out the network.
	 * This is smaller than the width of the layout window by 2*pad, to make it
	 * so that the nodes won't go quite to the edge of the screen.
	 */
	public int getLayoutWidth() {
		int width = dispInitWidth;
		if (display != null) {
			width = display.getDisplayWidth() - (2 * pad);
		}
		return width;
	}

	/**
	 * Returns the pixel hieght of the area availible for laying out the
	 * network. The height is smaller than the window height by 2*pad (to make
	 * sure that the nodes don't quite touch the edge of the screen) and also
	 * has the height of the bottom pad subtracted (to leave room for the
	 * buttons).
	 */
	public int getLayoutHeight() {
		int height = dispInitHeight;
		if (display != null) {
			height = display.getDisplayHeight() - (2 * pad);
		}
		return height;
	}

	public LayoutWindow getLayoutWindow() {
		// if we have this method, maybe we don't need to have the engine track
		// all its own display constants?
		return display;
	}

	/**
	 * returns the pixel height of the bottom pad (the area at the bottom of the
	 * layout window where the buttons and text fields are)
	 */
	public int getBottomPad() {
		return bottomPad;
	}

	/**
	 * Returns the number of pixels from the top to start the coordinate origin.
	 * Leaves room for the menubar, and for the pad with to make sure the nodes
	 * don't get drawn right at the edge of the screen.
	 */
	public int getTopPad() {
		return pad;
	}

	/**
	 * how far from the left edge of the window to start the layout coordinate
	 * origin.
	 */
	public int getLeftPad() {
		return pad;
	}

	/**
	 * the desired margin between the nodes and the edge of the window
	 */
	public int getPad() {
		return pad;
	}

	/**
	 * how much of the top is coverd by title bar
	 */
	public int getTopInset() {
		return display.getInsets().top;
	}

	/**
	 * Gives a string describing the layout. Calles getLayoutType() and
	 * getLayoutInfo() on the current layout.
	 */
	public String getLayoutInfo() {
		return currentLayout.getLayoutType() + " "
				+ currentLayout.getLayoutInfo();
		// need to also include the paramter setings, start, end delta, etc
	}

	/**
	 * returns the name of this layout engine and some info;
	 */
	public String toString() {
		return engineName;
	}

	/**
	 * Sets wether or not individual updates from the layout algorithms will be
	 * drawn to the screen during the layout process. False will be faster, but
	 * sometimes it is usefull to see the layouts. Default is False
	 */
	public void setShowUpdates(boolean state) {
		showUpdates = state;
	}

	public boolean isShowUpdates() {
		return showUpdates;
	}

	/**
	 * Sets how often the layouts will be drawn to the screen while the
	 * algorithm is optimizing. If n > 0, showUpdates will be set to true.
	 * Layouts will be drawn on every "n"th optimization loop, where n is the
	 * past int. Default is 0.
	 * 
	 * @param n
	 *            draw layout to screen every n loops
	 */
	public void setUpdatesN(int n) {
		updatesN = n;
		if (n > 0)
			showUpdates = true;
	}

	public int getUpdatesN() {
		return updatesN;
	}

	/**
	 * returns the index number of the currently selected slice
	 */
	public int getCurrentSliceNum() {
		return currentSlice;
	}

	/**
	 * returns an array of booleans corresponding to each slice, true if there
	 * are errors. WARNING, elements may be null if layout has not been applied!
	 */
	public boolean[] getErrorSlices() {
		return errorSlices;
	}

	/**
	 * returns the number of slices in the slice vector
	 */
	public int getNumSlices() {
		return layoutSlices.size();
	}

	/**
	 * returns the stored maximum value of a cell in the network matricies,
	 * calc'd by setupLayout(). Used for similarity/disimilarty conversions
	 */
	public double getMaxMatrixVal() {
		return maxMatrixValue;
	}

	/**
	 * returns the stored minmum value of a cell in the network matricies,
	 * calc'd by setupLayout(). Used for similarity/disimilarty conversions
	 */
	public double getMinMatrixValue() {
		return minMatrixValue;
	}

	/**
	 * returns the current LayoutSlice
	 */
	public LayoutSlice getCurrentSlice() {
		return (LayoutSlice) layoutSlices.get(currentSlice);
	}

	/**
	 * returns the LayoutSlice with the corresponding index. Does NOT check
	 * range
	 * 
	 * @param sliceNum
	 *            index of slice to be returned
	 */
	public LayoutSlice getSlice(int sliceNum) {
		// does NOT check range
		return (LayoutSlice) layoutSlices.get(sliceNum);
	}

	/**
	 * Sets the number intermediary interpolation ("tweening") frames to be
	 * drawn while animating transitions between slices.
	 * 
	 * @param frames
	 *            how many frames to draw during transition
	 */
	public void setInterpFrames(int frames) {
		interpFrames = frames;
	}

	/**
	 * Returns the current number of intermediary interpolation ("tweening")
	 * frames to be drawn while animating transitions between slices.
	 */
	public int getInterpFrames() {
		return interpFrames;
	}

	public void setFrameDelay(int miliseconds) {
		frameDelay = miliseconds;
	}

	public int getFrameDelay() {
		return frameDelay;
	}

	/**
	 * sets to range 0 - 1
	 * 
	 * @param offset
	 */
	public void setRenderOffset(float offset) {
		renderOffset = Math.min(Math.max(offset, 0), 1);
	}

	public float getRenderOffset() {
		return renderOffset;
	}

	/**
	 * Gives the engine a chance to do any clean up (like asking layouts to
	 * dispose of schedules..
	 */
	public void disposeEngine() {
		currentLayout.disposeLayout();
		disposePhasePlot();
		disposeStressPlot();

	}

	/**
	 * Hides and then nullifies the PhasePlot (if it exists)so that it will not
	 * take proccessing time.
	 */
	public void disposePhasePlot() {
		if (timePlot != null) {
			timePlot.hide();
			timePlot = null;
		}
	}

	public void disposeStressPlot() {
		if (shepPlot != null) {
			shepPlot.hide();
			shepPlot = null;
		}
	}

}