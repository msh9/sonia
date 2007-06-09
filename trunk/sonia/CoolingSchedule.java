package sonia;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import sonia.layouts.NetLayout;
import sonia.ui.ExportableFrame;
import sonia.ui.TextPrompter;

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
 * CoolingSchedule is both the data structure for controlling the cooling of a
 * "simulated anealing" processes layout, methods for providing acesses to the
 * processes, and the UI for the user, and feedback from the layout process. It
 * also provides the window for displaying all this. Among other things cooling
 * schedule is used to determine the maximum number of iterations permited by
 * algorithms like KK and FR. This is specified by the position of the last
 * (right most) controlpoint on the plot. This is initially positioned at the
 * end of the plot, so the value is determined by the entry in the "Max Passes"
 * field. Changing the value in this field and hitting enter will cause the plot
 * to be rescaled. The control points can be dragged on the plot to specify the
 * shape of the cooling function, and will impact the running of the plot in
 * real time.
 * 
 */
public class CoolingSchedule extends ExportableFrame implements MouseListener,
		MouseMotionListener, ActionListener, InternalFrameListener {
	private NetLayout layout;

	private int bendPoints; // number of control points

	private double[][] ctlValues; // [pointNumber][pass][fractionOfStart]

	private int[] pointsX;

	private int[] pointsY;

	private int graphWidth;

	private int graphHeight;

	private int pad = 10;

	private int selectedIndex = -1;

	private int maxPasses = 500;

	private double maxYvalue = 1.0;

	private int lastPassValue = -1;

	private int lastYvalue = -1;

	private double[] convergePlot;

	private double[] convergeValues;

	private int numConvergeValues = -1;

	private JButton Set;

	// private Canvas filler;
	private CoolingPlot plot;

	// private JLabel MaxPassLabel;
	private JTextField MaxPassField;

	// should also provide constructor with list of points
	/**
	 * Creates a cooling schedule (and gui) withe the specified number of
	 * control points
	 * 
	 * @param numPoints
	 *            the number of control points, should >= 2 (start and end)
	 */
	public CoolingSchedule(int numPoints) {
		super.setResizable(true);
		super.setMaximizable(true);
		super.setIconifiable(true);
		super.setClosable(true);
		bendPoints = numPoints;
		ctlValues = new double[bendPoints][2];
		pointsX = new int[bendPoints];
		pointsY = new int[bendPoints];
		makeGUI();
		reset();
		defaultSetup();
		screenPointsToCtlValues();
	}

	

	/**
	 * creates the gui, creating the layout, adding components, etc. Adds button
	 * for bringing up the control values dialog, and field for max passes.
	 */
	private void makeGUI() {
		// set up fonts for labels
		// Font textFont = new Font("SanSerif",Font.PLAIN,10);
		// this.setFont(textFont);

		MaxPassField = new JTextField(maxPasses + "", 8);
		MaxPassField.setBorder(new TitledBorder("Max. Passes"));
		// MaxPassLabel = new JLabel("Max. Passes");
		// OptDistField = new TextField(optDistance+"",3);
		// OptDistLabel = new Label("Optimum Distance");
		Set = new JButton("Values...");

		Set.addActionListener(this);
		this.addInternalFrameListener(this);
		// filler = new Canvas();
		plot = new CoolingPlot();
		plot.setBorder(new TitledBorder(""));

		GridBagLayout layout = new GridBagLayout();
		this.getContentPane().setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 2;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		// this.add(filler,c);
		this.getContentPane().add(plot, c);
		c.fill = GridBagConstraints.NONE;
		// c.gridx=0;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
		// c.anchor=c.EAST;
		// this.add(MaxPassLabel,c);
		// c.anchor=c.WEST;
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.1;
		//this.getContentPane().add(MaxPassField, c);
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.1;
		c.weighty = 0.1;
		this.getContentPane().add(Set, c);

		plot.addMouseMotionListener(this);
		plot.addMouseListener(this);
		MaxPassField.addActionListener(this);

		// this.setBackground(Color.lightGray);
		this.setSize(480, 150);
		this.setTitle("Schedule");
		this.setLocation(0, 380);
		this.setVisible(true);

	}

	/**
	 * Calculates and returns the "simulated aneeling" control value apropriate
	 * for the pass value. Essentialy evaluates the segmented function, defined
	 * by the control points, for the pass value. Draws the pass value on the
	 * gui
	 * 
	 * @param pass
	 *            value at which to evaluate the control function.
	 * @return tempFactor value between 0 and 1 indicating the fraction of the
	 *         inital value.
	 */
	public double getTempFactor(int pass) {
		double tempFactor = 0;
		int index = 0;
		// normalize the pass to the graph range
		int passX = (int) Math.round(((double) pass / (double) maxPasses)
				* (graphWidth))
				+ pad;

		// figue out the index for the segment of the pice-wise function pass is
		// in
		for (int i = 0; i < bendPoints - 1; i++) {
			if ((passX >= pointsX[i]) & (passX < pointsX[i + 1]))
				index = i;
		}
		// height of the little triang plus hight of starting point
		double rawHeight = (double) ((pointsY[index + 1] - pointsY[index]) * (passX - pointsX[index]))
				/ (double) (pointsX[index + 1] - pointsX[index])
				+ (pointsY[index] - pad);

		tempFactor = (maxYvalue / (double) graphHeight)
				* ((double) graphHeight - rawHeight);
		// clear the plot
		// this.repaint();
		// draw the point on the plot to show it was done
		Graphics g = plot.getGraphics();
		g.setColor(Color.red);
		g.drawRect(passX, (int) (rawHeight + pad), 2, 2);
		return tempFactor;

	}

	/**
	 * Returns an array of doubles containing the screen coordinates for the
	 * control points. The first elemt [pointIndex][coordinate] is the index for
	 * the point. So [2][0] gives the pass value of the 2nd point, and [2][1]
	 * gives the height. (IS THIS RIGHT?)
	 * 
	 * @return 2D array of doubles containing the [pointIndex][x,y] control
	 *         values
	 */
	public double[][] getCtlValues() {
		return ctlValues;
	}

	/**
	 * Parses a delimited string and sets the control points accordingly, ask to
	 * have them painted on the screen. The string should be in the format
	 * "(pass,fraction) (pass,fraction) ...". Example: "(0,1.0) (17,0.21)
	 * (30,0.12) (100,0.0)" Calls parseValuePair on each pair seperated by
	 * whitespace.
	 * 
	 * @param values
	 *            the string of control values to be tokenized and parsed.
	 */
	public void parseCtlValueString(String values) {
		// assume (pass,factor) (pass,factor) etc...
		StringTokenizer pairTokens = new StringTokenizer(values, " ");
		bendPoints = pairTokens.countTokens();
		ctlValues = new double[bendPoints][2];
		// SHOULD CHECK RANGES..
		for (int i = 0; i < bendPoints; i++) {
			double[] value = parseValuePair(pairTokens.nextToken());
			ctlValues[i][0] = Math.round(value[0]);
			// not sure why this extra step is necessary,
			// perhaps the compiler is too smart for me?
			// Idea is to shorten to 2 significant digits
			double roundValue = Math.round(value[1] * 100);
			ctlValues[i][1] = roundValue / 100;
		}
		pointsX = new int[bendPoints];
		pointsY = new int[bendPoints];
		reset();
		ctlValuesToScreenPoints();
		repaint();

	}

	/**
	 * Parses the passed string into the values for a control point. The string
	 * is expected to be in the form "(pointIndex,fractionValue)" with the
	 * values seperated by a comma. Example: "(17,0.21)". All though doubles are
	 * returned, the first is parsed to be an int. WILL IT THROW EXCEPTION ON
	 * PARSE ERROR?
	 * 
	 * @param pair
	 *            the string to be parsed into control values
	 * @return value double[] giving the [pass][value] for the control point
	 */
	private double[] parseValuePair(String pair) {
		// asume string was "(pass,factor)"
		double[] value = new double[2];
		// parse the substrings on either side of the comma
		int commaIndex = pair.indexOf(",");
		// but first make sure there was a comma
		if (commaIndex > 0) {
			value[0] = Integer.parseInt(pair.substring(1, commaIndex));
			value[1] = Double.parseDouble(pair.substring(commaIndex + 1, pair
					.length() - 1));
		} else {
			// /should spit to control, but cooling schedule doesn't have and
			// instance...
			System.out.println("ERROR parsing cooling schedule string " + pair);
		}
		return value;
	}

	/**
	 * Returns a string giving the control points in a form suitable for editing
	 * or logging, and reparseing. String will be in the format "(pass,fraction)
	 * (pass,fraction) ...". Example: "(0,1.0) (17,0.21) (30,0.12) (100,0.0)"
	 * 
	 * @return values String with the formated control values
	 */
	public String getCtlValString() {
		String values = "";
		for (int i = 0; i < bendPoints; i++) {
			values += "(" + (int) ctlValues[i][0] + "," + ctlValues[i][1]
					+ ") ";
		}

		return values;
	}

	/**
	 * Draws a horisontal line on the plot at a height corresponding to the
	 * passed double value when scaled to the plot.
	 * 
	 * @param value
	 *            the data value to draw the line for.
	 */
	public void showYValue(double value) {
		int plotValue = graphHeight
				- (int) Math.round((value / maxYvalue) * graphHeight) + pad;
		// draw a horizontal line at the plot value
		lastYvalue = plotValue;
		repaint();
	}

	/**
	 * Plots a point to show the convergance of the algorithm, the difference
	 * between the value at the previous time and the ccurrent value.
	 * 
	 * @param pass
	 *            the the current pass number
	 * @param value
	 *            the value at the current pass
	 */
	public void showConvergance(int pass, double value) {
		// NEED TO DO SOMETHING SO IT DOESN'T KEEP EXTENDING..

		// compute a crude convergeance slope "rise / run"
		double slope = (value - convergeValues[pass]);
		// add slope to record
		convergePlot[pass + 1] = value;
		numConvergeValues++;
	}

	/**
	 * Draws a virtical line on the plot to indicate the current pass value
	 * 
	 * @param pass
	 *            the pass indicating where the line should be drawn
	 */
	public void showPassValue(int pass) {
		// normalize the pass to the graph range
		int passX = (int) Math.round(((double) pass / (double) maxPasses)
				* (graphWidth))
				+ pad;

		lastPassValue = passX;
		repaint();

	}

	/**
	 * Converts the stored pass,fractionOfStart control value pairs to screen
	 * x,y pairs for plotting and stores the results in the plots cash for
	 * redraws.
	 */
	private void ctlValuesToScreenPoints() {
		int screenX;
		int screenY;
		for (int i = 0; i < bendPoints; i++) {
			// figure out what x,y coords should be based on graph width and
			// height
			screenX = (int) Math.round((ctlValues[i][0] / (double) maxPasses)
					* (graphWidth))
					+ pad;
			screenY = (int) Math.round(((1 - ctlValues[i][1]) / maxYvalue)
					* graphHeight)
					+ pad;
			pointsX[i] = screenX;
			pointsY[i] = screenY;
		}
	}

	/**
	 * Converts the current ploted screen points of the control points to the
	 * internal pass, fractionOfStart control points and stores them. Used to
	 * read values when adjusted by user in the gui.
	 */
	private void screenPointsToCtlValues() {
		// NEED TO SET TO FEWER SIGNIFICANT DIGITS
		double yValue;
		for (int i = 0; i < bendPoints; i++) {
			// figure out what x,y coords should be based on graph width and
			// height
			ctlValues[i][0] = Math.round((double) (pointsX[i] - pad)
					/ (double) graphWidth * (double) maxPasses);
			yValue = (double) (pointsY[i] - pad) / (double) graphHeight;
			// strange hack to make sure they only have 2 significant digits
			// multiply by 10, round, divide by ten
			yValue = Math.round(yValue * 100);
			ctlValues[i][1] = ((100 - yValue) / 100);
		}
	}

	/**
	 * Resents the plots stored values to the default initial values
	 */
	public void reset() {

		lastPassValue = -1;
		lastYvalue = -1;
		convergePlot = new double[maxPasses + 2];
		convergeValues = new double[maxPasses + 2];
		convergePlot[0] = 0;// intialize the first value
		convergeValues[0] = 0;
		numConvergeValues = 1;
	}

	/**
	 * does the internal calcs necessary to configure the plot when it is setup
	 * without specs.
	 */

	private void defaultSetup() {
		// load the text field values
		maxPasses = Integer.parseInt(MaxPassField.getText());
		// optDistance = Double.parseDouble(OptDistField.getText());
		// figure out the area which will actually be used to draw the graph
		graphWidth = plot.getWidth() - (2 * pad);
		graphHeight = plot.getHeight() - (2 * pad + 10);

		// calculate initial positions for the points
		for (int i = 0; i < bendPoints; i++) {
			pointsX[i] = (int) Math.round(i
					* ((double) graphWidth / ((double) bendPoints - 1)))
					+ pad;
			pointsY[i] = +pad
					+ (int) Math
							.round(i
									* ((double) graphHeight / ((double) bendPoints - 1)));
		}
		lastPassValue = -1;
		lastYvalue = -1;
		repaint();
	}

	/**
	 * Calls TextPropmter with a string of control values from getCtlValString()
	 * to let the user modify or record them, parses the result and sents the
	 * points accordingly.
	 */
	public void setValues() {
		TextPrompter prompter = new TextPrompter(null, getCtlValString(),
				"Enter schedule points (pass,factor)");
		parseCtlValueString(prompter.getUserString());
	}

	/**
	 * implements the plot as a Swing component
	 * 
	 * @author skyebend
	 * 
	 */
	private class CoolingPlot extends JPanel {
		/**
		 * Paints the plot data and information to the passed graphics context.
		 * First, axes and labels are drawn, then the conrol line segments and
		 * points, the current pass value, the current pass value and y value
		 * and the convergance points.
		 * 
		 * @param g
		 *            the graphics context the plot will be drawn on.
		 */
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			// check that the values for calcing the plot are current
			graphWidth = plot.getWidth() - (2 * pad);
			graphHeight = plot.getHeight()
					- (2 * pad + g.getFontMetrics().getHeight());
			ctlValuesToScreenPoints();

			Graphics2D graphics = (Graphics2D) g;
			// graphics.clearRect(0,0,super.getWidth(),super.getHeight());
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.setStroke(new BasicStroke(1.5f));
			// draw axes and their labels
			graphics.setColor(Color.darkGray);
			// x axis
			graphics.drawLine(pad, (graphHeight + pad), (graphWidth + pad),
					graphHeight + pad);
			graphics.drawString("" + maxPasses, graphWidth - 20, graphHeight
					+ pad + 10);
			// y axis
			graphics.drawLine(pad, (graphHeight + pad), pad, pad);
			graphics.drawString("1.0 (" + maxYvalue + ")", pad + 3, pad + 3);

			// draw line segments and points
			graphics.setColor(Color.white);
			for (int i = 0; i < bendPoints - 1; i++) {
				graphics.drawLine(pointsX[i], pointsY[i], pointsX[i + 1],
						pointsY[i + 1]);
			}
			// startpoint
			graphics.setColor(Color.green);
			graphics.drawOval(pointsX[0] - 2, pointsY[0] - 2, 5, 5);
			// rest of the points
			graphics.setColor(Color.darkGray);
			for (int i = 1; i < bendPoints - 1; i++) {
				graphics.drawOval(pointsX[i] - 2, pointsY[i] - 2, 5, 5);
				// also draw the pass value of the point at the bottom of the
				// graph
				String passVal = (int) Math.round(ctlValues[i][0]) + "";
				graphics.drawString(passVal, pointsX[i] - 2, graphHeight + pad
						+ 10);
			}
			// endPoint
			graphics.setColor(Color.red);
			graphics.drawOval(pointsX[bendPoints - 1] - 2,
					pointsY[bendPoints - 1] - 2, 5, 5);
			graphics.drawString("end:" + getMaxUsrPasses(),
					pointsX[bendPoints - 1] - 2, pointsY[bendPoints - 1] + 10);
			// draw the value lines if set

			if (lastYvalue >= 0) {
				graphics.setColor(Color.red);
				graphics.drawLine(pad, lastYvalue, (graphWidth + pad),
						lastYvalue);
			}
			if (lastPassValue >= 0) {
				graphics.setColor(Color.blue);
				graphics.drawLine(lastPassValue, (graphHeight + pad),
						lastPassValue, pad);
			}
			// do convergance plot
			graphics.setColor(Color.green);
			for (int i = 0; i < numConvergeValues; i++) {
				// rescale the value to fit, assume max and min values of 100
				// and shift so that 0 will be in middle of the graph
				double rescaleFact = graphHeight / 20;
				int plotVal = graphHeight + pad
						- (int) Math.round((convergePlot[i] * rescaleFact));
				int plotX = (int) Math.round(((double) i / (double) maxPasses)
						* (graphWidth))
						+ pad;
				graphics.drawRect(plotX, plotVal, 1, 1);

			}

			graphics.setColor(Color.red);
			this.invalidate();

		}
	}

	// /**
	// * Paints the plot data and information to the passed graphics context.
	// First,
	// * axes and labels are drawn, then the conrol line segments and points,
	// the
	// * current pass value, the current pass value and y value and the
	// convergance points.
	// * @param g the graphics context the plot will be drawn on.
	// */
	// public void paint(Graphics g)
	// {
	//
	// Graphics2D graphics = (Graphics2D)g;
	// graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	// RenderingHints.VALUE_ANTIALIAS_ON);
	// graphics.setStroke(new BasicStroke(1.5f));
	// //draw axes and their labels
	// graphics.setColor(Color.darkGray);
	// //x axis
	// graphics.drawLine(pad,(graphHeight+pad+getInsets().top),
	// (graphWidth+pad),graphHeight+pad+getInsets().top);
	// graphics.drawString(""+maxPasses,
	// graphWidth-20,graphHeight+pad+getInsets().top+10);
	// //y axis
	// graphics.drawLine(pad,(graphHeight+pad+getInsets().top),
	// pad,pad+getInsets().top);
	// graphics.drawString("1.0 ("+maxYvalue+")",pad+3,pad+getInsets().top+3);
	//
	// //draw line segments and points
	// graphics.setColor(Color.white);
	// for (int i=0; i<bendPoints-1;i++)
	// {
	// graphics.drawLine(pointsX[i],pointsY[i],pointsX[i+1],pointsY[i+1]);
	// }
	// //startpoint
	// graphics.setColor(Color.green);
	// graphics.drawOval(pointsX[0]-2,pointsY[0]-2,5,5);
	// //rest of the points
	// graphics.setColor(Color.darkGray);
	// for (int i=1; i<bendPoints-1;i++)
	// {
	// graphics.drawOval(pointsX[i]-2,pointsY[i]-2,5,5);
	// //also draw the pass value of the point at the bottom of the graph
	// String passVal = (int)Math.round(ctlValues[i][0])+"";
	// graphics.drawString(passVal,pointsX[i]-2,graphHeight+pad+getInsets().top+10);
	// }
	// //endPoint
	// graphics.setColor(Color.red);
	// graphics.drawOval(pointsX[bendPoints-1]-2,pointsY[bendPoints-1]-2,5,5);
	// graphics.drawString("end:"+getMaxUsrPasses(),
	// pointsX[bendPoints-1]-2,pointsY[bendPoints-1]+10);
	// //draw the value lines if set
	//
	// if (lastYvalue >= 0)
	// {
	// graphics.setColor(Color.red);
	// graphics.drawLine(pad,lastYvalue,
	// (graphWidth+pad),lastYvalue);
	// }
	// if (lastPassValue >= 0)
	// {
	// graphics.setColor(Color.blue);
	// graphics.drawLine(lastPassValue,(graphHeight+pad+getInsets().top),
	// lastPassValue,pad+getInsets().top);
	// }
	// //do convergance plot
	// graphics.setColor(Color.green);
	// for (int i = 0;i<numConvergeValues ;i++ )
	// {
	// //rescale the value to fit, assume max and min values of 100
	// //and shift so that 0 will be in middle of the graph
	// double rescaleFact = graphHeight/20;
	// int plotVal =graphHeight+getInsets().top+pad
	// -(int)Math.round((convergePlot[i]*rescaleFact));
	// int plotX = (int)Math.round(((double)i / (double)maxPasses)
	// * (graphWidth))+pad;
	// graphics.drawRect(plotX,plotVal,1,1);
	//
	// }
	//
	// graphics.setColor(Color.red);
	// this.invalidate();
	//
	// }

	/**
	 * figure out if the mouse is over a control point and return the number
	 */
	private int getPointAt(int x, int y) {
		int pointNum = -1;
		// figure out if it is over a control point, and return the number
		for (int i = 0; i < bendPoints; i++) {
			if (((x > pointsX[i] - 5) & (x < pointsX[i] + 5))
					& (((y > pointsY[i] - 5) & (y < pointsY[i] + 5)))) {
				pointNum = i;
			}
		}
		return pointNum;
	}

	// mouse listener
	/**
	 * If there is a point selected (from the mouse pressed event), drags the
	 * control point around on the screen, adjusting the control values in real
	 * time.
	 * 
	 * @param e
	 *            the mouse event to query for coordinates.
	 */
	public void mouseDragged(MouseEvent e) {
		// if it was over a point
		if (selectedIndex >= 0) {
			// if it is the left most, only allow y to be changed
			if (selectedIndex == 0) {
				pointsY[selectedIndex] = e.getY();
			}
			// if it is the last one, only allow x to be changed
			// and rescale other xes
			else if (selectedIndex == bendPoints - 1) {
				pointsX[selectedIndex] = e.getX();
			}
			// other wise let the point move
			// SHOULD COME UP WITH SOME WAY OF ENSUREING A FUNCTION!!!
			else {
				pointsX[selectedIndex] = e.getX();
				pointsY[selectedIndex] = e.getY();
			}
			// Graphics g = this.getGraphics();
			// plot.getGraphics().clearRect(plot.getX(),plot.getY(),plot.getWidth(),plot.getHeight());
			plot.repaint();
			// g.setColor(Color.white);
			// g.drawString(e.getX()+","+e.getY(),e.getX(),e.getY());

			// make sure the new vlues are in ctlPoints
			screenPointsToCtlValues();
		}
	}

	public void mouseMoved(MouseEvent e) {
	}

	/**
	 * Checks if mouse is over a control point, and sents the slected index
	 */
	public void mousePressed(MouseEvent e) {
		selectedIndex = getPointAt(e.getX(), e.getY());
	}

	public void mouseReleased(MouseEvent e) {
		selectedIndex = -1;
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	// button listener
	/**
	 * Checks which button is pressed and takes the appropriate action.
	 */
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource().equals(Set)) {
			setValues();
		}
		if (evt.getSource().equals(MaxPassField)) {
			setMaxPasses(Integer.parseInt(MaxPassField.getText()));
			screenPointsToCtlValues();
			repaint();
		}
	}


	// accessors----------
	/**
	 * Returns the user-specified maximum number of passes for the layout
	 * algorithm, as defined by the the horisonal position of the last control
	 * point.
	 * 
	 * @return int the maximum number of passes the algorithm should run
	 */
	public int getMaxUsrPasses() {
		int max = (int) Math.round(maxPasses * (pointsX[bendPoints - 1] - pad)
				/ (double) graphWidth);
		return max;
	}

	/**
	 * Sets the maximum number of passes permited. Not that the user value
	 * retuned can be less than this value. This essentialy sents the
	 * horrisontal scale for the cooling schedule.
	 * 
	 * @param passes
	 *            the maximum number of passes
	 */
	public void setMaxPasses(int passes) {
		maxPasses = passes;
		// need to make it rescale the bend points...
		MaxPassField.setText(maxPasses + "");
		reset();
	}

	/**
	 * Sets the maximum y value for the plot, essentialy sets the vertical
	 * scale.
	 * 
	 * @param value
	 *            the double value corresponding to the maximum value of the
	 *            plot.
	 */
	public void setMaxYvalue(double value) {
		maxYvalue = value;
		repaint();
	}

	/**
	 * Returns the maximum y value of the plot, which all data will be scaled
	 * to.
	 * 
	 * @return the maximum value (vertical scale of the plot)
	 */
	public double getMaxYvalue() {
		return maxYvalue;
	}

	/**
	 * returns only the plot part of the window
	 */
	protected JComponent getGraphicContent() {

		return plot;
	}

	public void internalFrameOpened(InternalFrameEvent e) {

	}

	public void internalFrameClosing(InternalFrameEvent e) {
	}

	public void internalFrameClosed(InternalFrameEvent e) {

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