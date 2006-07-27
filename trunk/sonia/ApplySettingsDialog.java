package sonia;

import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

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
 * Provides the controls for specifing layout parameters and how the layots will
 * be applied. Includes starting coords, hondeling of isolates, animating
 * redraws, post-processing, and options to apply to all the remaining slices.
 */
public class ApplySettingsDialog implements ActionListener {
	
	private JDialog settingsDialog;
	private SoniaLayoutEngine engine;
	private SoniaController control;
	private NetLayout layoutAlgorithm;
	private ApplySettings applySettings;
	private HashMap layoutProperties = new HashMap(); // holds items added by
														// specific layouts
    private JPanel mainPanel;
    private JPanel algProps;
    private JPanel iterationProps;
    private JPanel transformProps;
	// container dynamically added layout elements
	private ArrayList propComponents = new ArrayList();

	// layout elements
//	private JLabel SliceInfoLabel; // shows the laytout info, type of layout

	private JComboBox startChoice;
	private JComboBox isolateChoice;
	private JComboBox recenterChoice;
	private JComboBox rescaleChoice;
	private JTextField RepaintN;
	private JLabel repaintLabel;
	private JCheckBox IsolateExclude;
	private JButton Apply;
	private JButton Cancel;
	private JButton MultiApply;
	private JCheckBox ErrorStop;
	private JButton saveSettings;

	/**
	 * Creates a dilog box with settings to control the various stages of the
	 * layout process, including some algorithm specific parameters, which it
	 * adds to the window by communicating with the layout algorithm.
	 * 
	 * @param cont
	 *            the main soniaController
	 * @param eng
	 *            the layout engine which the layout belongs to
	 * @param owner
	 *            the parent frame (the layout window)
	 * @param layoutAlg
	 *            the layout algorithm
	 */
	public ApplySettingsDialog( ApplySettings settings, SoniaController cont,
			SoniaLayoutEngine eng, Frame owner, NetLayout layoutAlg) {
		control = cont;
		engine = eng;
		applySettings = settings;
		layoutAlgorithm = layoutAlg;
		settingsDialog = new JDialog(owner, layoutAlgorithm.getLayoutType()
				+ " Settings", true);
		String sliceInfoLabel = "Apply " + layoutAlgorithm.getLayoutType()
				+ " to slice# " + engine.getCurrentSliceNum() + " ["
				+ engine.getCurrentSlice().getSliceStart() + "-"
				+ engine.getCurrentSlice().getSliceEnd() + "]";
		startChoice = new JComboBox(new Object[] {
				ApplySettings.COORDS_RANDOM, ApplySettings.COORDS_FROM_PREV,
				ApplySettings.COORDS_FROM_FILE, ApplySettings.COORDS_CURRENT,
				ApplySettings.COORDS_CIRCLE });
		startChoice.setBorder(new TitledBorder("Starting Coordinates"));
		
		isolateChoice = new JComboBox(new Object[] {
				ApplySettings.ISLOLATE_IGNORE, ApplySettings.ISLOLATE_CIRCLE,
				ApplySettings.ISLOLATE_EDGE, ApplySettings.ISLOLATE_FILE,
				ApplySettings.ISLOLATE_PREVIOUS });
		isolateChoice.setBorder(new TitledBorder("Isolate Positioning"));

		RepaintN = new JTextField("0", 2);
		repaintLabel=new JLabel("Repaint on Nth layout pass");

		
		recenterChoice = new JComboBox(new Object[] {ApplySettings.RECENTER_AFTER,ApplySettings.NONE,
				ApplySettings.BARYCENTER,ApplySettings.RECENTER_DURING});
		recenterChoice.setBorder(new TitledBorder("Recentering"));
		rescaleChoice = new JComboBox(new Object[] {ApplySettings.NONE,ApplySettings.RESCALE_TO_FIT,});
		rescaleChoice.setBorder(new TitledBorder("Rescaling"));
		IsolateExclude = new JCheckBox("Exclude Isolates in transform", false);
		
		Apply = new JButton("Apply");
		Cancel = new JButton("Cancel");
		MultiApply = new JButton("Apply to Remaining");
		saveSettings = new JButton("Save Settings");
		ErrorStop = new JCheckBox("Stop on Layout Errors");

		// get any addl components layouts might want to add...
		layoutAlgorithm.setupLayoutProperties(this);

		GridBagLayout layout = new GridBagLayout();
		mainPanel = new JPanel(layout);
		mainPanel.setBorder(new TitledBorder(sliceInfoLabel));
		//settingsDialog.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 5, 2, 5);
		// add components to the layout GBlayout using constraints

		// starting coordinates
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.VERTICAL;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 0;
		mainPanel.add(startChoice,c);
		
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 0;
		mainPanel.add(isolateChoice,c);

		iterationProps = new JPanel(new GridBagLayout());
		iterationProps.setBorder(new TitledBorder("Iteration Options"));
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 1;
		iterationProps.add(repaintLabel,c);
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 1;
		iterationProps.add(RepaintN, c);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 0;
		iterationProps.add(ErrorStop, c);
		
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 1;
		mainPanel.add(iterationProps,c);

		transformProps = new JPanel(new GridBagLayout());
		transformProps.setBorder(new TitledBorder("Transformation Options"));
		c.fill = GridBagConstraints.VERTICAL;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		transformProps.add(recenterChoice,c);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		transformProps.add(rescaleChoice,c);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		transformProps.add(IsolateExclude, c);
		
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 4;
		c.weightx = 1;
		c.weighty = 0;
		mainPanel.add(transformProps,c);


		// buttons
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		mainPanel.add(MultiApply, c);
		c.gridx = 2;
		c.gridy = 2;
		mainPanel.add(Apply, c);
		c.gridx = 2;
		c.gridy = 3;
		mainPanel.add(saveSettings, c);
		c.gridx = 2;
		c.gridy = 4;
		mainPanel.add(Cancel, c);
		

		// add any addl compondents from the layout
		
		algProps = new JPanel(new GridBagLayout());
		algProps.setBorder(new TitledBorder("Algorithm-specific properties"));
		// first colum (alterneat entries in propcomponents)
		int compIndex = 0;
		for (int i = 0; i < (propComponents.size() / 2); i++) {
			c.anchor = GridBagConstraints.EAST;
			c.gridx = 0;
			c.gridy = i;
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0;
			c.weighty = 0;
			algProps.add((Component) propComponents.get(compIndex), c);
			compIndex += 2;
		}
		// 2nd colum (alternet entries in propcomponents)
		compIndex = 1;
		for (int i = 0; i < (propComponents.size() / 2); i++) {
			c.anchor = GridBagConstraints.WEST;
			c.gridx = 1;
			c.gridy = i;
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0;
			c.weighty = 0;
			algProps.add((Component) propComponents.get(compIndex), c);
			compIndex += 2;
		}
		c.anchor = GridBagConstraints.CENTER;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 4;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.VERTICAL;
		mainPanel.add(algProps, c);

		Cancel.addActionListener(this);
		Apply.addActionListener(this);
		MultiApply.addActionListener(this);
		saveSettings.addActionListener(this);

		//settingsDialog.setBackground(Color.lightGray);
		settingsDialog.getContentPane().add(mainPanel);
		settingsDialog.setSize(600, 300);
	}

	/**
	 * displays the dialog on screen
	 */
	public void showDialog() {
		settingsDialog.setVisible(true);
		Apply.requestFocus();
		//TODO: read the values from the passed settings
	}

	/**
	 * called by the layout algorithm during setup so that it can add parameters
	 * to the dilog box.
	 * 
	 * @param name
	 *            the string name of the prameter to display and to use as the
	 *            key
	 * @param value
	 *            the defualt double value of the parameter
	 */
	public void addLayoutProperty(String name, double value) {
		// make new GUI objects
		JLabel PropLabel = new JLabel(name);
		JTextField PropField = new JTextField(value + "", 3);
		PropField.setName(name);
		// add the pair to the Hashmap
		layoutProperties.put(name, PropField);
		propComponents.add(PropLabel);
		propComponents.add(PropField);
	}

	/**
	 * sets the algorithm specific layout pramter to the passed value, after
	 * checking that there is a parameter corresponding to name. Will spit out
	 * an error (TO CONSOLE, SHOULD FIX THIS?) if there is no param.
	 * 
	 * @param name
	 *            the string name of the paramter to set
	 * @param value
	 *            the double value to set it to
	 */
	public void setLayoutProperty(String name, double value) {
		// make sure it exists
		if (layoutProperties.keySet().contains(name)) {
			// get the corresponding text field
			JTextField propField = (JTextField) layoutProperties.get(name);
			// set its text to the value
			propField.setText(value + "");
		} else {
			System.out.println("unable to match property to name " + name);
		}
	}

	/**
	 * gets the user entered value of the algorithm specific layout property, if
	 * name matches one of the previously setup parameters. Otherwise spits an
	 * error to console.
	 * 
	 * @param name
	 *            a string corresponding to the name of the property
	 */
	/*
	public double getLayoutProperty(String name) {
		double value = 0.0;
		// make sure it exists
		if (layoutProperties.keySet().contains(name)) {
			// find the component with matching name
			JTextField propField = (JTextField) layoutProperties.get(name);
			value = Double.parseDouble(propField.getText());
		} else {
			System.out.println("unable to match property to name " + name);
		}
		return value;
	}
	*/

	
//
//	/**
//	 * gets all the parameters from the settings dialog and sets them in the
//	 * LayoutEngine
//	 * 
//	 */
//	//TODO: need to read this from params
//	public void setValuesFromDialog() {
//		// repaint
//		if (isRepaint()) {
//			engine.setUpdatesN(Integer.parseInt(RepaintN.getText()));
//		} else {
//			engine.setShowUpdates(false);
//		}
//	}
//	
//
//	/**
//	 * returns the int corresponding to the setting for the starting coordinates
//	 * (where nodes are positioned before algorithm runs). one of the static
//	 * ints PREV_SLICE - use the coordinates from the previous slice, CIRCLE -
//	 * postion the nodes in a circle to start, ORIG_FILE - use the coordinates
//	 * from the node record, CURRENT - use the current screen coordinates,
//	 * RANDOMIZE - randomly position the nodes.
//	 */
//	public int getStartCoordCode() {
//		int returnInt;
//		if (FromPrevSlice.isSelected()) {
//			returnInt = PREV_SLICE;
//		} else if (Circle.isSelected()) {
//			returnInt = CIRCLE;
//		} else if (FromOrigFile.isSelected()) {
//			returnInt = ORIG_FILE;
//		} else if (Current.isSelected()) {
//			returnInt = CURRENT;
//		} else {
//			returnInt = RANDOMIZE;
//		}
//		return returnInt;
//	}
//
//	/**
//	 * returns an int indicating the setting for how to handle the position of
//	 * isolated nodes. IGNORE - leave them where they are. PIN_CIRCLE - position
//	 * them in a ring on the layout. PIN_EDGE - position them in a line on the
//	 * bottom of the layout. PIN_ORIG - use the original coordinates stored in
//	 * the nodes. PIN_PREVIOUS - use the position from the previous slice.
//	 */
//	public int getIsolatesCode() {
//		int returnInt = IGNORE;
//		if (PinToCircle.isSelected()) {
//			returnInt = PIN_CIRCLE;
//		} else if (PinToEdge.isSelected()) {
//			returnInt = PIN_EDGE;
//		} else if (UseOrigCoords.isSelected()) {
//			returnInt = PIN_ORIG;
//		} else if (PinPrevious.isSelected()) {
//			returnInt = PIN_PREVIOUS;
//		}
//		return returnInt;
//	}
//
//	/**
//	 * should the layout be rescaled when finsihed?
//	 */
//	public boolean isRescale() {
//		return Rescale.isSelected();
//	}
//
//	/**
//	 * should isolates be excluded when rescaling? (often used if the isolates
//	 * are being positioned seperately)
//	 */
//	public boolean isIsolateExclude() {
//		return IsolateExclude.isSelected();
//	}
//
//	/**
//	 * restrict nodes to a bounding circle? (only works with FR, and not very
//	 * well)
//	 * 
//	 */
//	public boolean isCircleRestrict() {
//		return CircleRestrict.isSelected();
//	}
//
//	/**
//	 * recenter the layout after each pass of the algorithm (so that it won't
//	 * drift off screen if you are watching the redraws)
//	 */
//	public boolean isRecenter() {
//		return Recenter.isSelected();
//	}
//
//	/**
//	 * recenter the layout (max, min) when the algorithm is finished?
//	 */
//	public boolean isEndRecenter() {
//		return EndRecenter.isSelected();
//	}
//
//	/**
//	 * recenter the layout the average of the nodes' coordinates when finished?
//	 */
//
//	public boolean isEndBarycenter() {
//		return EndBarycenter.isSelected();
//	}
//
//	/**
//	 * true if the layout is going to be redrawn while algorithm is running
//	 */
//	public boolean isRepaint() {
//		return Repaint.isSelected();
//	}
//
//	/**
//	 * if true, the "apply to remaining" thread will stop if a layout has an
//	 * erorr, other wise it will just go on to the next layout
//	 */
//	public boolean isStopOnError() {
//		return ErrorStop.isSelected();
//	}
//
//	/**
//	 * returns an integer which controls how frequently the network will be
//	 * redrawn while the layout is in process (redraw every Nth pass)
//	 */
//	public int getRepaintN() {
//		return Integer.parseInt(RepaintN.getText());
//	}

	/**
	 * returns a string summarizing the layout settings to record in the log
	 */
	public String getSummaryString() {
//		String layoutParams = layoutAlgorithm.getLayoutInfo();
//		String startingPos;
//		String isolates;
//		String modifications = "";
//		// get a string for layout params
//
//		// get a string for starting
//		int startPos = getStartCoordCode();
//		startingPos = "from ORIGINAL FILE coordinates";
//		if (startPos == RANDOMIZE)
//			startingPos = "from RANDOM coordinates";
//		if (startPos == CIRCLE)
//			startingPos = "from CIRCULAR coordinates";
//		if (startPos == PREV_SLICE)
//			startingPos = "from PREVIOUS SLICE coordinates";
//		if (startPos == CURRENT)
//			startingPos = "from CURRENT display coordinates";
//
//		// get a string for isolates
//		int isoType = getIsolatesCode();
//		isolates = "not adjusted";
//		if (isoType == PIN_CIRCLE)
//			isolates = "pinned to CIRCLE";
//		if (isoType == PIN_EDGE)
//			isolates = "pinned to EDGE";
//		if (isoType == PIN_ORIG)
//			isolates = "pinned to ORIGINAL FILE coords";
//		if (isoType == PIN_PREVIOUS)
//			isolates = "pinned to PREVIOUS SLICE";
//
//		// get a string for afterlayout modifications
//		if (isCircleRestrict()) {
//			modifications += "restricted to circle,";
//		}
//		if (isEndRecenter()) {
//			modifications += "recenterd when finished,";
//		}
//		if (isRecenter()) {
//			modifications += "adjusted to barycenter during layout,";
//		}
//		if (isRescale()) {
//			modifications += "rescaled to fit,";
//			if (isIsolateExclude()) {
//				modifications += "isolates scaled independently,";
//			}
//		}
//		String summary = "Layout Type:" + layoutAlgorithm.getLayoutType()
//				+ "\n" + "Layout Parameters:" + layoutParams + "\n"
//				+ "Starting Positions:" + startingPos + "\n"
//				+ "Isolate Treatment:" + isolates + "\n" + "Modifications:"
//				+ modifications;
//
//		return summary;
		return applySettings.toString();
	}
	
	public ApplySettings getSettings(){
		//TODO: need to load the settings from the gui
		
		if (applySettings == null){
			applySettings = new ApplySettings();
		}
//		starting positions
		applySettings.put(ApplySettings.STARTING_COORDS,startChoice.getSelectedItem());
//		isolate positions
		applySettings.put(ApplySettings.ISOLATE_POSITION,isolateChoice.getSelectedItem());
		
		// iteration control
		applySettings.put(ApplySettings.STOP_ON_ERROR,ErrorStop.isSelected()+"");
		//TODO: should check that this is a valid int..
		applySettings.put(ApplySettings.LAYOUT_REPAINT_N,RepaintN.getText());
		applySettings.put(ApplySettings.APPLY_REMAINING,MultiApply.isSelected()+"");
		
		//transformations
		applySettings.put(ApplySettings.RECENTER_TRANSFORM,ErrorStop.isSelected()+"");
		applySettings.put(ApplySettings.RESCALE_LAYOUT,rescaleChoice.getSelectedItem());
		applySettings.put(ApplySettings.TRANSFORM_ISOLATE_EXCLUDE,IsolateExclude.isSelected()+"");
		
		//layout specific params
		applySettings.put(ApplySettings.ALG_NAME,layoutAlgorithm.getLayoutType());
		Iterator keyIter = layoutProperties.keySet().iterator();
		while (keyIter.hasNext()){
			String key = (String)keyIter.next();
			applySettings.setLayoutSpecificProperty(key,
					((JTextField)layoutProperties.get(key)).getText());
		}
		//debug
		System.out.println("apply settings: "+applySettings);
		
		return applySettings;
	}

	// ACTION LISTENER
	/**
	 * figures out what user did and calls apropriate method
	 * 
	 * @param evt
	 *            the ActionEvent to determine the source of the event
	 */
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource().equals(Cancel)) {
			settingsDialog.setVisible(false);
		} else if (evt.getSource().equals(saveSettings)) {
			//TODO:  create better option for choosing output dir for slice settings
			String fileAndPath = control.getCurrentPath()+"SoniaLayoutSettings.prp";
			ApplySettings settings = getSettings();
			try {
				
				FileOutputStream propsOut = new FileOutputStream(fileAndPath);
				settings.store(propsOut,settingsDialog.getTitle());
				control.showStatus("Saved layout settings to "+fileAndPath);
			} catch (FileNotFoundException e) {
				control.showError("Unable to create or locate layout settings file: "+fileAndPath
						+" "+e.getMessage());
			} catch (IOException e) {
				control.showError("IO error writing settings file: "+fileAndPath
						+" "+e.getMessage());
			}
		} else {
			// read the values from the dialog and set them in engine
			//setValuesFromDialog();

			// now figure out if is apply or multi
			if (evt.getSource().equals(Apply)) {
				engine.applyLayoutTo(getSettings(),engine.getCurrentSlice());
				settingsDialog.setVisible(false);
			} else if (evt.getSource().equals(MultiApply)) {
				//this will ask ask the dialog for the settings
				engine.applyLayoutToRemaining();
				settingsDialog.setVisible(false);
			}
		}
	}
}
