package sonia.ui;

import sonia.NetDataStructure;
import sonia.SoniaController;
import sonia.settings.LayoutSettings;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
 * Displays the controls and parameters for specifying the range of the network
 * data, how to bin it into slices, the aggregation and interpolation
 * techniques, and choosing which layout algorithm will be used. Also provides
 * the ability to preview the network data and slice parameters in a
 * "phase-space" plot.
 */
public class LayoutSettingsDialog extends JDialog implements ActionListener , FocusListener{
	// private Dialog settingsDialog;
	//private SoniaLayoutEngine engine;

	private SoniaController control;

	private PhasePlot timePlot;
	
	private NetDataStructure data;

	// layout elements
	//private JOptionPane dialoger;
	
	private JPanel mainPanel;
	
	private JPanel controlPanel;

	private JComboBox LayoutType; // for choosing which kind of layout

	private JTextField SliceStart;

	private JTextField SliceEnd;

	private JTextField SliceDuration;

	private JTextField SliceDelta;

	private JComboBox AnimateType;

	private JComboBox SliceAggregation;

	private JPanel sliceSettings;

	private JButton OK;
	
	private JButton saveSettings;

	private JButton Cancel;

	//private JButton Plot;

	// holds the defualt values for start and endtimes
	private double layoutStartTime = 0.0;

	private double layoutEndTime = 10.0;

	private String[] layoutNames = { LayoutSettings.COORD_ORIG,
			LayoutSettings.CIRCULAR, LayoutSettings.FR, LayoutSettings.RAND_FR,
			LayoutSettings.RUB_FR, LayoutSettings.MULTI_KK, LayoutSettings.PI,
			LayoutSettings.METRIC_MDS , LayoutSettings.MDSJ_CMDS,LayoutSettings.GRAPHVIZ};

	private String[] interpNames = { LayoutSettings.NO_ANIMATION,
			LayoutSettings.COSINE_ANIMATION,LayoutSettings.DELAY_COSINE_ANIMATION,
			LayoutSettings.LINEAR_ANIMATION };

	private String[] aggregateNames = { LayoutSettings.NUM_TIES,
			LayoutSettings.AVG_TIES, LayoutSettings.SUM_TIES };
	
	private LayoutSettings settings;

	public LayoutSettingsDialog(LayoutSettings set, SoniaController cont, NetDataStructure netData, String msg,
			Frame owner) {
		super(owner,true);
		control = cont;
		data = netData;
		//engine = eng;
		this.settings = set;
	
		
		mainPanel = new JPanel(new BorderLayout());
		controlPanel = new JPanel();
		controlPanel.setBorder(new TitledBorder("Slicing Settings"));

		LayoutType = new JComboBox(layoutNames);
		LayoutType.setBorder(new TitledBorder("layout type:"));
		LayoutType.setSelectedIndex(5);

		sliceSettings = new JPanel(new GridLayout(2, 2));
		sliceSettings.setBorder(new TitledBorder("Layout Slice Settings:"));
		SliceStart = new JTextField(layoutStartTime + "", 8);
		SliceStart.setBorder(new TitledBorder("start time"));
		sliceSettings.add(SliceStart);

		// SliceEndLabel = new JLabel("end time");
		SliceEnd = new JTextField(layoutEndTime + "", 8);
		SliceEnd.setBorder(new TitledBorder("end time"));
		sliceSettings.add(SliceEnd);

		// SliceDurLabel = new JLabel("slice_duration");
		SliceDuration = new JTextField("1.0", 4);
		SliceDuration.setBorder(new TitledBorder("slice duration"));
		sliceSettings.add(SliceDuration);
		// SliceDeltaLabel = new JLabel("slice_delta");
		SliceDelta = new JTextField("1.0", 4);
		SliceDelta.setBorder(new TitledBorder("slice delta"));
		sliceSettings.add(SliceDelta);

		AnimateType = new JComboBox(interpNames);
		AnimateType.setBorder(new TitledBorder("Animation type:"));
		AnimateType.setSelectedIndex(1);

		SliceAggregation = new JComboBox(aggregateNames);
		SliceAggregation.setBorder(new TitledBorder(
				"Within-slice aggregation technique:"));

		OK = new JButton("Create Layout");
		Cancel = new JButton("Cancel");
	//	Plot = new JButton("Phase Plot...");
		saveSettings = new JButton("Save Settings");
		
		if (settings == null) {
			// tell the settings dialog what the start and end times are
			setDataStartDefault(data.getFirstTime());
			setDataEndDefault(data.getLastTime());
			storeSettings();
			
		}
		
		timePlot = new PhasePlot(null,data,settings );

		//layoutOptions = new JPanel();

		GridBagLayout layout = new GridBagLayout();
		controlPanel.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 5, 2, 5);
		// add components to the layout GBlayout using constraints
		// buttons
		// slice info
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		// this.add(SliceInfoLabel,c);
		c.fill = GridBagConstraints.BOTH;
		controlPanel.add(sliceSettings, c);
		c.weightx = .5;
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		
		controlPanel.add(AnimateType, c);
		c.gridx = 3;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		controlPanel.add(SliceAggregation, c);

//layout type
		c.gridx = 4;
		c.gridy = 0;
		c.gridheight = 1;
		controlPanel.add(LayoutType, c);
		c.fill = GridBagConstraints.NONE;
		// buttons
	//	c.gridx = 0;
	//	c.gridy = 1;
	//	c.gridheight = 1;
	//	controlPanel.add(Plot, c);
		c.gridx = 1;
		c.gridy = 1;
		c.gridheight = 1;
		controlPanel.add(saveSettings, c);

		c.gridx = 2;
		c.gridy = 1;
		c.gridheight = 1;
		controlPanel.add(Cancel, c);

		c.gridx =3;
		c.gridy = 1;
		controlPanel.add(OK, c);
		
		//add components to the main panel
		mainPanel.add(controlPanel,BorderLayout.SOUTH);
		mainPanel.add(timePlot.getContentPane(),BorderLayout.CENTER);
		
		

		Cancel.addActionListener(this);
		OK.addActionListener(this);
	//	Plot.addActionListener(this);
		SliceStart.addActionListener(this);
		SliceStart.addFocusListener(this);
		SliceEnd.addActionListener(this);
		SliceEnd.addFocusListener(this);
		SliceDuration.addActionListener(this);
		SliceDuration.addFocusListener(this);
		SliceDelta.addActionListener(this);
		SliceDelta.addFocusListener(this);
		saveSettings.addActionListener(this);

		//this.setBackground(Color.lightGray);
		this.getContentPane().add(mainPanel);
		this.setTitle("Layout (Slicing) Settings for " + msg);
		this.setSize(800, 300);
		// this.show();
		OK.requestFocus();
		if (settings != null){
			readSettings();
		}
		

	}

	public LayoutSettings askUserSettings() {
		
		//wait here for OK, which dismiss dialog and set the settings
		this.setVisible(true);
		return settings;
	}

	/**
	 * Sets the default value to be shown in the layout start field, should be
	 * set to the smallest time value in the data.
	 * 
	 * @param start
	 *            the double default start time
	 */
	public void setDataStartDefault(double start) {
		layoutStartTime = start;
		SliceStart.setText(layoutStartTime + "");
	}

	/**
	 * Sets the default value to be shown in the layout end field, should be set
	 * to the largest time value in the data.
	 * 
	 * @param end
	 *            the double default end time
	 */
	public void setDataEndDefault(double end) {

		layoutEndTime = end;
		SliceEnd.setText(layoutEndTime + "");
	}
	
	private void storeSettings()
	{
		if (settings == null){
			settings = new LayoutSettings();
		}
//
//		// take care of all the settings
		settings.setProperty(LayoutSettings.LAYOUT_TYPE,(String)LayoutType.getSelectedItem());
		settings.setProperty(LayoutSettings.ANIMATE_TYPE,(String)AnimateType.getSelectedItem());
		settings.setProperty(LayoutSettings.SLICE_AGGREGATION,(String)SliceAggregation.getSelectedItem());
		settings.setProperty(LayoutSettings.SLICE_START,SliceStart.getText().trim());
		settings.setProperty(LayoutSettings.SLICE_END,SliceEnd.getText().trim());
		settings.setProperty(LayoutSettings.SLICE_DURATION,SliceDuration.getText().trim());
		settings.setProperty(LayoutSettings.SLICE_DELTA,SliceDelta.getText().trim());
	}
	
	private void readSettings(){
		if (settings == null){
			settings = new LayoutSettings();
		}
		LayoutType.setSelectedItem(settings.getProperty(LayoutSettings.LAYOUT_TYPE));
		AnimateType.setSelectedItem(settings.getProperty(LayoutSettings.ANIMATE_TYPE));
		SliceAggregation.setSelectedItem(settings.getProperty(LayoutSettings.SLICE_AGGREGATION));
		SliceStart.setText(settings.getProperty(LayoutSettings.SLICE_START));
		SliceEnd.setText(settings.getProperty(LayoutSettings.SLICE_END));
		SliceDuration.setText(settings.getProperty(LayoutSettings.SLICE_DURATION));
		SliceDelta.setText(settings.getProperty(LayoutSettings.SLICE_DELTA));
	}

	// ACTION LISTENER //figures out what user did and calls appropriate method
	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals("Create Layout")) {
			storeSettings();
			this.setVisible(false);
			// will this destroy itself, or does it need to be explicit?
			// this.finalize();
		} else if (evt.getActionCommand().equals("Cancel")) {
			this.setVisible(false);
			//TODO: canceling the layout creation throws exception
			if (timePlot != null) {
				timePlot.hide();
				timePlot = null;
			}
			settings = null;
	//	} else if (evt.getSource().equals(Plot)) {
			//TODO: fix phase plot from layout settings dialog
		//	if (timePlot == null) {
			//	engine.showPhasePlot();
			//	timePlot = engine.getPhasePlot();
		//	}
		} else if (evt.getSource().equals(saveSettings)) {
			//TODO:  create better option for choosing output dir for slice settings
			String fileAndPath = control.getCurrentPath()+"SoniaSliceSettings.prp";
			storeSettings();
			try {
				
				FileOutputStream propsOut = new FileOutputStream(fileAndPath);
				settings.store(propsOut,this.getTitle());
				control.showStatus("Saved slice settings to "+fileAndPath);
			} catch (FileNotFoundException e) {
				control.showError("Unable to create or locate layout settings file: "+fileAndPath
						+" "+e.getMessage());
			} catch (IOException e) {
				control.showError("IO error writing settings file: "+fileAndPath
						+" "+e.getMessage());
			}
			
		} else// something happend, so lets assume it was a text area and
				// update the
		// display
		{
			if (timePlot != null) {
				storeSettings();
				repaint();
			}
		}

	}

	/**
	 * @deprecated use settings instead
	 * @return
	 */
	public double getSliceStart() {
		return Double.parseDouble(SliceStart.getText());
	}

	/**
	 * @deprecated use settings instead
	 * @return
	 */
	public double getSliceEnd() {
		return Double.parseDouble(SliceEnd.getText());
	}

	/**
	 * @deprecated use settings instead
	 * @return
	 */
	public double getSliceDuration() {
		return Double.parseDouble(SliceDuration.getText());
	}

	/**
	 * @deprecated use settings instead
	 * @return
	 */
	public double getSliceDelta() {
		return Double.parseDouble(SliceDelta.getText());
	}

	public void focusGained(FocusEvent arg0) {
		//don't do anything special
		((JTextField)arg0.getSource()).selectAll();
	}

	public void focusLost(FocusEvent arg0) {
		if (timePlot != null) {
			storeSettings();
			repaint();
		}
		
	}



}
