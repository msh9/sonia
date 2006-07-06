package sonia;

import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import sonia.layouts.CircleLayout;
import sonia.layouts.FRLayout;
import sonia.layouts.MetricMDSLayout;
import sonia.layouts.MultiCompKKLayout;
import sonia.layouts.OrigCoordLayout;
import sonia.layouts.PILayout;
import sonia.layouts.RandomFRLayout;
import sonia.layouts.RubBandFRLayout;

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
public class LayoutSettingsDialog extends JDialog implements ActionListener {
	// private Dialog settingsDialog;
	private SoniaLayoutEngine engine;

	private SoniaController control;

	private PhasePlot timePlot;

	// layout elements
	//private JOptionPane dialoger;
	
	private JPanel mainPanel;
	
	private JLabel LayoutTypeLabel;

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

	private JButton Plot;

	// holds the defualt values for start and endtimes
	private double layoutStartTime = 0.0;

	private double layoutEndTime = 10.0;

	private String[] layoutNames = { "coordinates from original file",
			"circular layout", "FR layout", "random FR layout",
			"Rubber-Band FR Layout", "MultiComp KK Layout", "Moody PI layout",
			"MetricMDS (SVD)?" };

	private String[] interpNames = { "none", "cosine animation" };

	private String[] aggregateNames = { "Number  of i->j ties",
			"Avg of i->j ties", "Sum  of i->j ties" };
	
	private LayoutSettings settings;

	public LayoutSettingsDialog(LayoutSettings settings, SoniaController cont, SoniaLayoutEngine eng,
			Frame owner) {
		super(owner,true);
		control = cont;
		engine = eng;
		this.settings = settings;
		
		mainPanel = new JPanel();

		LayoutType = new JComboBox(layoutNames);
		LayoutType.setBorder(new TitledBorder("layout type:"));
		LayoutType.setBackground(Color.white);
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
		AnimateType.setBackground(Color.white);
		AnimateType.setSelectedIndex(1);

		SliceAggregation = new JComboBox(aggregateNames);
		SliceAggregation.setBorder(new TitledBorder(
				"Within-slice aggregation technique:"));

		OK = new JButton("Create Layout");
		Cancel = new JButton("Cancel");
		Plot = new JButton("Phase Plot...");
		saveSettings = new JButton("Save Settings");

		//layoutOptions = new JPanel();

		GridBagLayout layout = new GridBagLayout();
		mainPanel.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 5, 2, 5);
		// add components to the layout GBlayout using constraints
		// buttons
		// slice info
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 0;
		// this.add(SliceInfoLabel,c);
		mainPanel.add(sliceSettings, c);

		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		mainPanel.add(AnimateType, c);
		c.gridx = 3;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		mainPanel.add(SliceAggregation, c);

//layout type
		c.gridx = 4;
		c.gridy = 0;
		c.gridheight = 1;
		mainPanel.add(LayoutType, c);
		// buttons
		c.gridx = 0;
		c.gridy = 6;
		c.gridheight = 1;
		mainPanel.add(Plot, c);
		c.gridx = 1;
		c.gridy = 6;
		c.gridheight = 1;
		mainPanel.add(saveSettings, c);

		c.gridx = 6;
		c.gridy = 4;
		c.gridheight = 1;
		mainPanel.add(Cancel, c);

		c.gridx = 6;
		c.gridy = 5;
		mainPanel.add(OK, c);

		Cancel.addActionListener(this);
		OK.addActionListener(this);
		Plot.addActionListener(this);
		SliceStart.addActionListener(this);
		SliceEnd.addActionListener(this);
		SliceDuration.addActionListener(this);
		SliceDelta.addActionListener(this);
		saveSettings.addActionListener(this);

		//this.setBackground(Color.lightGray);
		this.add(mainPanel);
		this.setTitle("Layout (Slicing) Settings for " + engine.toString());
		this.setSize(800, 300);
		// this.show();
		OK.requestFocus();

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

	// ACTION LISTENER //figures out what user did and calls apropriate method
	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals("Create Layout")) {
			storeSettings();
			this.setVisible(false);
			// will this distroyitself, or does it need to be explicit?
			// this.finalize();
		} else if (evt.getActionCommand().equals("Cancel")) {
			this.setVisible(false);
			if (timePlot != null) {
				timePlot.hide();
				timePlot = null;
			}
		} else if (evt.getSource().equals(Plot)) {
			//TODO: fix phase plot from layout settings dialog
			if (timePlot == null) {
				engine.showPhasePlot();
				timePlot = engine.getPhasePlot();
			}
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
				timePlot.repaint();
			}
		}

	}

	public double getSliceStart() {
		return Double.parseDouble(SliceStart.getText());
	}

	public double getSliceEnd() {
		return Double.parseDouble(SliceEnd.getText());
	}

	public double getSliceDuration() {
		return Double.parseDouble(SliceDuration.getText());
	}

	public double getSliceDelta() {
		return Double.parseDouble(SliceDelta.getText());
	}

}
