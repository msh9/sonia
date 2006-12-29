package sonia.ui;

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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import sonia.SoniaCanvas;
import sonia.SoniaController;
import sonia.SoniaLayoutEngine;
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
 * Displays settings for configuring and modifying the display of the networks
 * in the LayoutWindow. Node and Arc scaling, transparency, ghosting, labels,
 * anti-alias, etc. Also options for numerically resizeing the window. The
 * dialog is displayed by the "View Options" button on the LayoutWindow. The
 * settings are stored in the Canvas. The settings are as follows:
 * <P>
 * Anti-Alias - turns on anti-alias graphics smoothing (see Graphics2D).
 * Defaults to on 'cause it looks much better, there will be some speed
 * improvement by turning it off.
 * </P>
 * <P>
 * Show Stats - Shows information (the layout slice #, the start and end times
 * of the render displayed, the name of the layout algorithm) in small text on
 * the top of the layout. This really only impacts the movie, as it obscured by
 * the title bar in the LayoutWindow, and the information is already present.
 * </P>
 * <P>
 * Ghost Previous Slice - Displays a greyed-out image of the the previously
 * viewd slice under the current slice. Useful for identifying where nodes move
 * to.
 * </P>
 * <P>
 * Layout Width, Layout Height - Specifies the dimensions of the layout area in
 * pixels.
 * </P>
 * <P>
 * Node scale factor - Scales up or down the size of the nodes by multiply their
 * original value
 * </P>
 * <P>
 * Show labels - show the label stored in the NodeAttribute
 * </P>
 * <P>
 * Show Ids - show the id sonia assigned to each node
 * </P>
 * <P>
 * Hide Nodes - hid the nodes completely (but still draw the labels)
 * </P>
 * <P>
 * Arc transparency - how opaque to draw the arcs (0.0 = clear, 1.0 = solid)
 * </P>
 * <P>
 * Arc width factor - Scales up or down the width of the arcs by multiplying
 * their original values
 * </P>
 * <P>
 * Show Arrowheads - Draw the arrowheads and the destination end of the arcs
 * </P>
 * <P>
 * Show arc weights - show the weight of the arc (after aggregation) used by the
 * layot (useful for debugging)
 * </P>
 * <P>
 * Show Arc Labels - draw the label for the arc at the middle of the arc
 * </P>
 */

public class GraphicsSettingsDialog {
	private JDialog graphicsDialog;

	private SoniaLayoutEngine engine;

	private SoniaController control;

	private SoniaCanvas canvas;

	private GraphicsSettings settings = null;

	private JPanel generalPanel;

	private JPanel arcsPanel;

	private JPanel nodesPanel;

	private JCheckBox AntiAlias;


	private JTextField ArcTransField;


	private JTextField NodeTransField; // node transparency value



	private JTextField ArcWidthFactorField;



	private JTextField NodeScaleFactorField;

	// private JCheckBox ShowArcWeights; // should rename as "show layout

	// weights"

	// private JCheckBox ShowArcLabels;

	private JComboBox ArcArrows;

	private JComboBox nodeLabels;

	private JComboBox arcLabels;

	private JTextField ShowLabelsField; // show labels based on node size


	private JCheckBox ShowStats;



	private JCheckBox GhostSlice;



	private JTextField WindowWidth;



	private JTextField WindowHeight;



	private JTextField FlashDuration;

	private JComboBox hideNodes;

	private JComboBox HideArcs;

	private JPanel mainpanel;

	private JButton OK;
	
	private JButton Save;
	private JButton Apply;

	/**
	 * Creates the graphic settings components and sets up the display.
	 */
	public GraphicsSettingsDialog(GraphicsSettings settings,
			SoniaController cont, SoniaLayoutEngine eng, Frame owner,
			SoniaCanvas canv) {
		control = cont;
		engine = eng;
		canvas = canv;
		this.settings = settings;
		graphicsDialog = new JDialog(owner, "Graphics Settings", true);
		mainpanel = new JPanel(new GridBagLayout());
		mainpanel.setBorder(new TitledBorder("Graphics Settings"));
		generalPanel = new JPanel(new GridLayout(6,1));
		generalPanel.setBorder(new TitledBorder("General Options:"));
		nodesPanel = new JPanel(new GridLayout(5,1));
		nodesPanel.setBorder(new TitledBorder("Node Options:"));
		arcsPanel = new JPanel(new GridLayout(5,1));
		arcsPanel.setBorder(new TitledBorder("Arc Options:"));
		nodesPanel.setMinimumSize(new Dimension(150,arcsPanel.getHeight()));

		AntiAlias = new JCheckBox("AntiAlias graphics (slower)", true);

		WindowHeight = new JTextField(engine.getDisplayHeight() + "", 6);
		WindowHeight.setBorder(new TitledBorder("Layout Height"));
		WindowWidth = new JTextField(engine.getDisplayWidth() + "", 6);
		WindowWidth.setBorder(new TitledBorder("Layout Width"));
		//FlashNew = new JCheckBox("Flash new events", false);
		FlashDuration = new JTextField("0.0", 4);
		FlashDuration.setBorder(new TitledBorder("Flash new events for"));
		
		ArcTransField = new JTextField("0.5", 3);
		ArcTransField.setBorder(new TitledBorder("Arc Transparency"));
		NodeTransField = new JTextField("0.5", 3);
		NodeTransField.setBorder(new TitledBorder("Node transparency"));
		nodeLabels = new JComboBox(new String[] { GraphicsSettings.LABELS,
				GraphicsSettings.IDS, GraphicsSettings.NONE });
		nodeLabels.setBorder(new TitledBorder("Node labeling"));
		hideNodes = new JComboBox(new String[] { GraphicsSettings.NONE,
				GraphicsSettings.ALL });
		hideNodes.setBorder(new TitledBorder("Hide nodes"));
		
		ArcWidthFactorField = new JTextField("1.0", 4);
		ArcWidthFactorField.setBorder(new TitledBorder("Arc width factor"));
		NodeScaleFactorField = new JTextField("1.0", 4);
		NodeScaleFactorField.setBorder(new TitledBorder("Node scale factor"));

		ArcArrows = new JComboBox(new String[] { GraphicsSettings.ARROW_END,
				GraphicsSettings.NONE });
		ArcArrows.setBorder(new TitledBorder("Arrow style"));
		arcLabels = new JComboBox(new String[] { GraphicsSettings.NONE,
				GraphicsSettings.LABELS, GraphicsSettings.LAYOUT_WEIGHTS });
		arcLabels.setBorder(new TitledBorder("Arc labeling"));
		ShowLabelsField = new JTextField("0.0", 3);
		ShowLabelsField.setBorder(new TitledBorder("Min. value for label"));

		// NodeLabelGroup

		ShowStats = new JCheckBox("Show stats", true);
	//	ShowAxes = new JCheckBox("Show Axes", false);
		GhostSlice = new JCheckBox("Ghost previous slice");

		HideArcs = new JComboBox(new String[] { GraphicsSettings.NONE,
				GraphicsSettings.ALL });
		HideArcs.setBorder(new TitledBorder("Hide arcs"));

		OK = new JButton("OK");
		Save = new JButton("Save Settings");
		Apply = new JButton("Apply");
		
		GridBagConstraints c = new GridBagConstraints();
		//c.insets = new Insets(2, 5, 2, 5);
		// add components to the layout GBlayout using constraints
		//c.anchor = GridBagConstraints.WEST;
		// first, the colum lables
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = .5;
		c.weighty = .5;
		mainpanel.add(generalPanel, c);
	
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		mainpanel.add(nodesPanel, c);
		c.gridx = 2;
		c.gridy = 0;
		c.weightx = .5;
		c.weighty = .5;
		mainpanel.add(arcsPanel, c);

		generalPanel.add(AntiAlias);
		generalPanel.add(ShowStats);
		generalPanel.add(GhostSlice);
		generalPanel.add(FlashDuration);
		generalPanel.add(WindowWidth);
		generalPanel.add(WindowHeight);

		// node options
		nodesPanel.add(NodeScaleFactorField);
		nodesPanel.add(NodeTransField);
		nodesPanel.add(nodeLabels);
		nodesPanel.add(ShowLabelsField);
		nodesPanel.add(hideNodes);

		// arc options
		arcsPanel.add(ArcTransField);
		arcsPanel.add(ArcWidthFactorField);
		arcsPanel.add(ArcArrows);
		arcsPanel.add(arcLabels);
		arcsPanel.add(HideArcs);

		// c.gridx=0;c.gridy=10;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
		// graphicsDialog.add(ShowAxes,c);
		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		mainpanel.add(OK, c);
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		mainpanel.add(Apply, c);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		mainpanel.add(Save, c);

		OK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// applySettings();
				GraphicsSettings settings = storeSettings();
				canvas.setSettings(settings);
				engine.setDisplayWidth(Integer.parseInt(settings
						.getProperty(GraphicsSettings.LAYOUT_WIDTH)));
				engine.setDisplayHeight(Integer.parseInt(settings
						.getProperty(GraphicsSettings.LAYOUT_HEIGHT)));
				graphicsDialog.setVisible(false);
			}
		});
		Apply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// applySettings();
				GraphicsSettings settings = storeSettings();
				canvas.setSettings(settings);
				engine.setDisplayWidth(Integer.parseInt(settings
						.getProperty(GraphicsSettings.LAYOUT_WIDTH)));
				engine.setDisplayHeight(Integer.parseInt(settings
						.getProperty(GraphicsSettings.LAYOUT_HEIGHT)));
				control.updateDisplays();
			}
		});
		
		Save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// applySettings();
				GraphicsSettings settings = storeSettings();
				// TODO: create better option for choosing output dir for grapics
				// settings
				String fileAndPath = control.getCurrentPath()
						+ "SoniaGraphicsSettings.prp";
				try {

					FileOutputStream propsOut = new FileOutputStream(fileAndPath);
					settings.store(propsOut, graphicsDialog.getTitle());
					control.showStatus("Saved graphics settings to " + fileAndPath);
				} catch (FileNotFoundException e) {
					control
							.showError("Unable to create or locate graphics settings file: "
									+ fileAndPath + " " + e.getMessage());
				} catch (IOException e) {
					control.showError("IO error writing settings file: "
							+ fileAndPath + " " + e.getMessage());
				}
			}
		});
		graphicsDialog.getContentPane().add(mainpanel);
		// graphicsDialog.setBackground(Color.lightGray);
		graphicsDialog.setSize(500, 350);

	}

	public void showDialog() {
		WindowHeight.setText(engine.getDisplayHeight() + "");
		WindowWidth.setText(engine.getDisplayWidth() + "");
		graphicsDialog.setVisible(true);
		readSettings();
		OK.requestFocus();
	}

	/**
	 * copies the graphic settings into the properties object
	 * 
	 * @author skyebend
	 */
	public GraphicsSettings storeSettings() {
		if (settings == null) {
			settings = new GraphicsSettings();
		}
		settings.put(GraphicsSettings.ANTI_ALIAS, AntiAlias.isSelected() + "");
		settings.put(GraphicsSettings.SHOW_STATS, ShowStats.isSelected() + "");
		settings
				.put(GraphicsSettings.GHOST_SLICE, GhostSlice.isSelected() + "");
		settings.put(GraphicsSettings.FLASH_EVENTS, FlashDuration.getText()
				.trim());
		settings.put(GraphicsSettings.LAYOUT_WIDTH, WindowWidth.getText()
				.trim());
		settings.put(GraphicsSettings.LAYOUT_HEIGHT, WindowHeight.getText()
				.trim());
		settings.put(GraphicsSettings.NODE_SCALE_FACTOR, NodeScaleFactorField
				.getText().trim());
		settings.put(GraphicsSettings.NODE_TRANSPARENCY, NodeTransField
				.getText().trim());
		settings
				.put(GraphicsSettings.NODE_LABELS, nodeLabels.getSelectedItem());
		settings.put(GraphicsSettings.NODE_LABEL_CUTOFF, ShowLabelsField
				.getText().trim());
		settings.put(GraphicsSettings.HIDE_NODES, hideNodes.getSelectedItem());
		settings.put(GraphicsSettings.ARC_TRANSPARENCY, ArcTransField.getText()
				.trim());
		settings.put(GraphicsSettings.ARCS_WIDTH_FACTOR, ArcWidthFactorField
				.getText().trim());
		settings.put(GraphicsSettings.ARROW_STYLE, ArcArrows.getSelectedItem());
		settings.put(GraphicsSettings.ARC_LABELS, arcLabels.getSelectedItem());
		settings.put(GraphicsSettings.HIDE_ARCS, HideArcs.getSelectedItem());
		return settings;
	}

	/**
	 * copies the graphic settings from the properties to the gui, using current
	 * gui values as defaults
	 * 
	 * @author skyebend
	 */
	private void readSettings() {
		if (settings == null) {
			settings = new GraphicsSettings();
		}
		AntiAlias.setSelected(Boolean.parseBoolean(settings.getProperty(
				GraphicsSettings.ANTI_ALIAS, AntiAlias.isSelected() + "")));
		ShowStats.setSelected(Boolean.parseBoolean(settings.getProperty(
				GraphicsSettings.SHOW_STATS, ShowStats.isSelected() + "")));
		GhostSlice.setSelected(Boolean.parseBoolean(settings.getProperty(
				GraphicsSettings.GHOST_SLICE, GhostSlice.isSelected() + "")));
		FlashDuration.setText(settings.getProperty(
				GraphicsSettings.FLASH_EVENTS, FlashDuration.getText().trim()));
		WindowWidth.setText(settings.getProperty(GraphicsSettings.LAYOUT_WIDTH,
				WindowWidth.getText().trim()));
		WindowHeight.setText(settings.getProperty(
				GraphicsSettings.LAYOUT_HEIGHT, WindowHeight.getText().trim()));
		NodeScaleFactorField.setText(settings.getProperty(
				GraphicsSettings.NODE_SCALE_FACTOR, NodeScaleFactorField
						.getText().trim()));
		NodeTransField.setText(settings.getProperty(
				GraphicsSettings.NODE_TRANSPARENCY, NodeTransField.getText()
						.trim()));
		nodeLabels.setSelectedItem(settings.getProperty(
				GraphicsSettings.NODE_LABELS, (String) nodeLabels
						.getSelectedItem()));
		ShowLabelsField.setText(settings.getProperty(
				GraphicsSettings.NODE_LABEL_CUTOFF, ShowLabelsField.getText()
						.trim()));
		hideNodes.setSelectedItem(settings.getProperty(
				GraphicsSettings.HIDE_NODES, (String) hideNodes
						.getSelectedItem()));
		ArcTransField.setText(settings.getProperty(
				GraphicsSettings.ARC_TRANSPARENCY, ArcTransField.getText()
						.trim()));
		ArcWidthFactorField.setText(settings.getProperty(
				GraphicsSettings.ARCS_WIDTH_FACTOR, ArcWidthFactorField
						.getText().trim()));
		ArcArrows.setSelectedItem(settings.getProperty(
				GraphicsSettings.ARROW_STYLE, (String) ArcArrows
						.getSelectedItem()));
		arcLabels.setSelectedItem(settings.getProperty(
				GraphicsSettings.ARC_LABELS, (String) arcLabels
						.getSelectedItem()));
		HideArcs.setSelectedItem(settings
				.getProperty(GraphicsSettings.HIDE_ARCS, (String) HideArcs
						.getSelectedItem()));

	}

	/**
	 * Transfers the settings from the display checkboxes to the Canvas.
	 */
	public void applySettings() {
		// canvas.setAntiAlias(AntiAlias.isSelected());
		// canvas.setArcTrans(ArcTrans.isSelected());
		// canvas.setArcTransVal(Float.parseFloat(ArcTransField.getText()));
		// canvas.setNodeTrans(NodeTrans.isSelected());
		// canvas.setNodeTransVal(Float.parseFloat(NodeTransField.getText()));
		// canvas.setArcWidthFact(Float.parseFloat(ArcWidthFactorField.getText()));
		// // canvas.setShowArrows(ShowArrows.isSelected());
		// // canvas.setShowArcWeights(ShowArcWeights.isSelected());
		// // canvas.setShowArcLabels(ShowArcLabels.isSelected());
		// // canvas.setShowLabels(ShowLabels.isSelected());
		// canvas.setShowLabelsVal(Float.parseFloat(ShowLabelsField.getText()));
		// canvas.setShowId(ShowIds.isSelected());
		// canvas.setShowIdsVal(Float.parseFloat(ShowIdsField.getText()));
		// canvas.setShowStats(ShowStats.isSelected());
		// canvas.setGhostSlice(GhostSlice.isSelected());
		// canvas.setFlashNew(FlashNew.isSelected());
		// canvas.setFlashDuration(Double.parseDouble(FlashDuration.getText()));
		// canvas.setNodeScaleFact(Float
		// .parseFloat(NodeScaleFactorField.getText()));
		// // canvas.setHideArcs(HideArcs.isSelected());
		// // canvas.setHideNodes(HideNodes.isSelected());
		// engine.setDisplayWidth(Integer.parseInt(WindowWidth.getText()));
		// engine.setDisplayHeight(Integer.parseInt(WindowHeight.getText()));
		// graphicsDialog.setVisible(false);
		// canvas.repaint();
	}
}
