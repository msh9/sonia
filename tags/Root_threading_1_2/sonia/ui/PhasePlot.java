package sonia.ui;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;

import cern.colt.list.ObjectArrayList;
import java.util.*;
import java.text.NumberFormat;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import sonia.NetDataStructure;
import sonia.NetworkEvent;
import sonia.SoniaLayoutEngine;
import sonia.settings.LayoutSettings;

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
 * Gives a "phase-space" graphical representation of the network data and the
 * settings selected in the layoutSettings dialog, indicates the current slice
 * and render window, allows navigation by clicking on slices. Indicates the
 * time coordinates of the cursor location in the plot window. If the phasePlot
 * is displayed, pressing enter in any field of the LayoutSettingsDialog will
 * show the slice widths (shown in white) and network events. ArcEvents are
 * shown in blue, NodeEvents are shown in cyan. Their position indicates the
 * start time, with the length of the horizontal bar indicating the duration of
 * the event. The x position in normaly determined randomly (so the point clouds
 * create the effect of a pseudo-histogram) but they can be sorted in
 * chronological order by check the "Sort Events" option. <BR>
 * <BR>
 * When a LayoutWindow is displayed, the PhasePlot also serves as a navigation
 * tool. Clicking on the slice will ask the layout engine to change to the
 * slice. The current slice is outlined in red, and the region of time being
 * displayed by the current RenderSlice is shown in orange. Keeping the plot
 * displayed will slow animation somewhat.
 * 
 */
public class PhasePlot extends ExportableFrame implements
		InternalFrameListener, MouseMotionListener, MouseListener {
	// private Canvas drawArea;
	private PhasePlotPanel drawArea;
	private JPanel mainPanel;

	private JCheckBox sortBox;

	private JLabel MouseTime;

	private SoniaLayoutEngine engine;

	private NetDataStructure data;

	private LayoutSettings settings;

	private LayoutWindow currentLayout; // so it can talk to the display

	private boolean eventsSorted = false;

	private ObjectArrayList events;

	private NumberFormat formater;

	// display constants
	private int sidePad = 10;

	private int xAxisPad = 20;

	private double renderStart = -1;

	private double renderEnd = -1;

	public PhasePlot(SoniaLayoutEngine eng, NetDataStructure dat,
			LayoutSettings set) {
		// init the data
		engine = eng;
		data = dat;
		settings = set;
		if (eng != null){
			currentLayout = engine.getLayoutWindow();
		}
		
		double plotStart = Double.parseDouble(settings.getProperty(LayoutSettings.SLICE_START));
		double plotEnd = Double.parseDouble(settings.getProperty(LayoutSettings.SLICE_END));
		// fudge the end time to make sure things get included..
		// DOESN'T WORK WHEN TIME EXTENDS TO INFINITY!!!
		events = new ObjectArrayList(data.getEventsFromTo(plotStart, plotEnd)
				.toArray());
		events.shuffleFromTo(0, events.size() - 1);

		// number formating
		formater = NumberFormat.getInstance(Locale.ENGLISH);
		formater.setMaximumFractionDigits(3);
		formater.setMinimumFractionDigits(3);
		;

		// make the gui
		// drawArea = new Canvas();
		mainPanel = new JPanel(new GridBagLayout());
		drawArea = new PhasePlotPanel();
		drawArea.setBorder(new TitledBorder("Timeline view of network"));
		sortBox = new JCheckBox("Sort Events", eventsSorted);
		MouseTime = new JLabel("");
		// MouseTime.setBackground(Color.lightGray);
		//MouseTime.setEditable(false);
		
		super.setResizable(true);
		super.setMaximizable(true);
		super.setIconifiable(true);
		super.setClosable(true);

		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		mainPanel.add(drawArea, c);
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.SOUTH;
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.1;
		mainPanel.add(sortBox, c);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.1;
		mainPanel.add(MouseTime, c);
		this.getContentPane().add(mainPanel);
		// add the listerners
		this.addInternalFrameListener(this);
		drawArea.addMouseMotionListener(this);
		drawArea.addMouseListener(this);
		sortBox.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				drawArea.repaint();
			}
		
		});
		this.getContentPane().setName("timeline");
		mainPanel.setName("timeline");

		// this.setBackground(Color.lightGray);
		this.setSize(700, 175);
		if (engine != null){
			this.setTitle("Phase Plot for " + engine.toString());
		}
		this.setLocation(200, 200);
		// this.setVisible(true);
	}

	/**
	 * records the start and end time so the render slice can be drawn on the
	 * plot
	 */
	public void showRenderSliceTimes(double start, double end) {
		renderStart = start;
		renderEnd = end;
		repaint();
	}

	private class PhasePlotPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D graph = (Graphics2D) g;
			int topPad = sidePad + 10;
			int xAxis = this.getHeight() - xAxisPad;
			int plotHeight = xAxis - topPad;
			int plotWidth = this.getWidth() - (2 * sidePad);
			double plotStart = Double.parseDouble(settings.getProperty(LayoutSettings.SLICE_START));
			double plotEnd = Double.parseDouble(settings.getProperty(LayoutSettings.SLICE_END));

			double sliceDuration = Double.parseDouble(settings.getProperty(LayoutSettings.SLICE_DURATION));
			double sliceDelta = Double.parseDouble(settings.getProperty(LayoutSettings.SLICE_DELTA));
			// check for infinate values
			
			if (plotStart == Double.NEGATIVE_INFINITY
					| plotEnd == Double.POSITIVE_INFINITY) {
				graph.setColor(Color.red);
				graph.drawString(
						"Cannot display phase plot with infinate start or end",
						sidePad, xAxis - 30);
				return;
			}

			double dataOffset = plotStart;
			double yStep = (double) plotHeight / (double) events.size();
			double scaleFactor = (double) plotWidth / (plotEnd - plotStart);

			graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			graph.setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, 0.5f));
			// drawSlices
			boolean[] errorSlices = null;
			if (engine != null){
				engine.getErrorSlices();
			}
			int numSlices = (int) Math.ceil((plotEnd - plotStart) / sliceDelta);
			// figure out slices' start and end times
			double sliceStart = plotStart - dataOffset;
			// rect's coords are to upper left corner, x,y,w,h
			Rectangle2D.Double sliceRect = new Rectangle2D.Double();
			// lines to indicate start and finish
			Line2D.Double startLine = new Line2D.Double();
			Line2D.Double endLine = new Line2D.Double();
			for (int s = 0; s < numSlices; s++) {
				// set the back ground to white, but red if slice has error
				graph.setColor(Color.white);
				if (errorSlices != null) {
					if (errorSlices[s] == true) {
						graph.setColor(Color.red);
					}
				}
				sliceRect.setRect(sliceStart * scaleFactor + sidePad,
						(double) topPad, sliceDuration * scaleFactor,
						(double) plotHeight);
				graph.fill(sliceRect);
				// draw the starting line for the slice
				graph.setColor(Color.green);
				startLine.setLine(sliceStart * scaleFactor + sidePad,
						(double) topPad, sliceStart * scaleFactor + sidePad,
						(double) xAxis);
				graph.draw(startLine);
				// draw the ending line for the slice
				graph.setColor(Color.red);
				endLine.setLine((sliceStart + sliceDuration) * scaleFactor
						+ sidePad, (double) topPad,
						(sliceStart + sliceDuration) * scaleFactor + sidePad,
						(double) xAxis);
				graph.draw(endLine);
				sliceStart += sliceDelta;
			}

			// check if events should be set up in time order
			if (sortBox.isSelected()) {
				// sort them if they arn't already
				if (!eventsSorted) {
					// uses event start times according to compartor interface
					events.sort();
					eventsSorted = true;
				}
			} else // box is unchecked
			{
				// shuffle them if sorted
				if (eventsSorted) {
					events.shuffleFromTo(0, events.size() - 1);
					eventsSorted = false;
				}
			}

			graph.setStroke(new BasicStroke(2.0f));

			Line2D phaseBar = new Line2D.Double();
			// draw the events on the window
			for (int i = 0; i < events.size(); i++) {
				NetworkEvent event = (NetworkEvent) events.get(i);
				// get the start and end times and scale to fit screen
				double from = (event.getObsTime() - dataOffset) * scaleFactor
						+ sidePad;
				double to = (event.getEndTime() - dataOffset) * scaleFactor
						+ sidePad;
				// if it is two small to see, make it bigger
				if ((to - from) < 1.0) {
					to = from + 1.0;
				}
				// move to new postion
				phaseBar.setLine(from, (xAxis - yStep * i), to, (xAxis - yStep
						* i));
				// set color based on if node or arc

				if (event.getClass().getName().endsWith("NodeAttribute")) {
					graph.setColor(Color.cyan);
				} else {
					graph.setColor(Color.blue);
				}
				graph.draw(phaseBar);
			}

			graph.setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, 0.9f));
			graph.setStroke(new BasicStroke(3.0f));

			if ((renderStart >= 0) & (renderEnd >= 0)) {
				// draw the current render indicator
				graph.setColor(Color.orange);
				sliceRect.setRect((renderStart - dataOffset) * scaleFactor
						+ sidePad, (double) topPad - 3.0,
						(renderEnd - renderStart) * scaleFactor,
						(double) plotHeight + 6.0);
				graph.draw(sliceRect);
			}

			// draw the current slice indicator
			
			int currentSliceIndex = 0;
			if (engine != null) {
				currentSliceIndex = engine.getCurrentSliceNum();
			}
			graph.setColor(Color.magenta);
			sliceRect.setRect((currentSliceIndex * sliceDelta) * scaleFactor
					+ sidePad, (double) topPad - 1,
					sliceDuration * scaleFactor, (double) plotHeight + 2);
			graph.draw(sliceRect);

			// and draw the scale on to check things
			graph.setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, 0.5f));
			graph.setColor(Color.black);
			graph.drawString(plotStart + "", sidePad, xAxis + 10);
			graph.drawString(plotEnd + "", this.getWidth() - 40, xAxis + 10);

		}
	}

	/**
	 * specifies which component should be exported, in this case just the graph
	 * window
	 */
	protected JComponent getGraphicContent() {
		return drawArea;
	}

	// figures out which slice the mouse is over
	private int getSliceIndexFromTime(double time) {
		int sliceIndex = -1;
		double plotStart = Double.parseDouble(settings.getProperty(LayoutSettings.SLICE_START));
		double plotEnd = Double.parseDouble(settings.getProperty(LayoutSettings.SLICE_END));
		double sliceDuration = Double.parseDouble(settings.getProperty(LayoutSettings.SLICE_DURATION));
		double sliceDelta = Double.parseDouble(settings.getProperty(LayoutSettings.SLICE_DELTA));
		int numSlices = (int) Math.ceil((plotEnd - plotStart) / sliceDelta);
		// loop over the slice intervals to see if one matches
		// will pick the topmost (latest) slice
		for (int s = 0; s < numSlices; s++) {
			if ((time >= plotStart + s * sliceDelta)
					& (time < (plotStart + s * sliceDelta) + sliceDuration)) {
				sliceIndex = s;
			}
		}
		return sliceIndex;
	}

	// Mouse listeners
	/**
	 * Shows the time coordinates of the mouse in the plot window
	 */
	public void mouseMoved(MouseEvent event) {
		int mouseX = event.getX();
		double plotStart = Double.parseDouble(settings.getProperty(LayoutSettings.SLICE_START));
		double plotEnd = Double.parseDouble(settings.getProperty(LayoutSettings.SLICE_END));
		int plotWidth = drawArea.getWidth() - (2 * sidePad);
		double dataOffset = plotStart;

		double scaleFactor = (double) plotWidth / (plotEnd - plotStart);
		// compute the data-time where the mouse is
		double mouseTime = ((double) mouseX - sidePad) / scaleFactor
				+ dataOffset;
		int sliceN = 0;
		if (engine != null){
			sliceN = engine.getCurrentSliceNum();
		}
		MouseTime.setText("Slice#:" + sliceN
				+ "  Time at Cursor:" + formater.format(mouseTime));
	}

	public void mouseDragged(MouseEvent event) {
	}

	public void mouseEntered(MouseEvent event) {
	}

	public void mouseReleased(MouseEvent event) {
	}

	public void mousePressed(MouseEvent event) {
	}

	/**
	 * Determines which slice was clicked, and asks the layout to change to it,
	 * if it is ready
	 */
	public void mouseClicked(MouseEvent event) {
		// first, check to see if the layout is ready
		if (engine != null && engine.getErrorSlices() != null) {
			currentLayout = engine.getLayoutWindow();
			int mouseX = event.getX();
			double plotStart = Double.parseDouble(settings.getProperty(LayoutSettings.SLICE_START));
			double plotEnd = Double.parseDouble(settings.getProperty(LayoutSettings.SLICE_END));
			int plotWidth = drawArea.getWidth() - (2 * sidePad);
			double dataOffset = plotStart;
			double scaleFactor = (double) plotWidth / (plotEnd - plotStart);
			// compute the data-time where the mouse is
			double mouseTime = ((double) mouseX - sidePad) / scaleFactor
					+ dataOffset;
			// get the slice index
			int index = getSliceIndexFromTime(mouseTime);
			// check if it is valid
			if (index >= 0) {
				// ask layout to change the slice
				currentLayout.goToSlice(index);
			}
			drawArea.repaint();
		}
	}

	public void mouseExited(MouseEvent event) {
	}

	public void internalFrameOpened(InternalFrameEvent e) {
	}

	public void internalFrameClosing(InternalFrameEvent e) {

	}

	public void internalFrameClosed(InternalFrameEvent e) {
		if (engine != null){
			engine.disposePhasePlot();
		}

	}

	public void internalFrameIconified(InternalFrameEvent e) {

	}

	public void internalFrameDeiconified(InternalFrameEvent e) {

	}

	public void internalFrameActivated(InternalFrameEvent e) {

	}

	public void internalFrameDeactivated(InternalFrameEvent e) {

	}
}