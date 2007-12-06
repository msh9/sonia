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
package sonia.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import sonia.NetUtils;
import sonia.SoniaLayoutEngine;
import sonia.layouts.MultiCompKKLayout;

public class StressTimeline extends JPanel {
	
	private SoniaLayoutEngine engine;
	private PlotPanel plot;
	private int sidePad = 20;
	private int xAxisPad = 20;
	private double[] stressPoints = null;
	private JButton calcAll = new JButton("Calc all");
	
	
	
	public StressTimeline(SoniaLayoutEngine eng){
		super();
		this.engine = eng;
		plot = new PlotPanel();
		plot.setBorder(new TitledBorder("stress timeline"));
		calcAll.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				calcStresses();
				
			}
		});
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;c.weightx=1;c.weighty=1;
		this.add(plot,c);
		c.fill = GridBagConstraints.NONE;c.weightx=0;c.weighty=0;
		this.add(calcAll,c);
	}
	
	private class PlotPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public PlotPanel(){
		 super();
		}
		
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			if (stressPoints != null){
			Graphics2D graph = (Graphics2D) g;
			int topPad = sidePad + 10;
			int xAxis = this.getHeight() - xAxisPad;
			int plotHeight = xAxis - topPad;
			int plotWidth = this.getWidth() - (2 * sidePad);
			double sMax = 0;
			double sMin = 0;//Double.POSITIVE_INFINITY;
			double sMean = 0;
			for (int i = 0; i < stressPoints.length; i++) {
				if (!Double.isNaN(stressPoints[i])){
					sMax = Math.max(sMax,stressPoints[i]);
				//	sMin = Math.min(sMin,stressPoints[i]);
					sMean += stressPoints[i];
				}
			}
			sMean = sMean / stressPoints.length;
			double xSF = (double) plotWidth / stressPoints.length;
			double ySF = (double) plotHeight / (sMax-sMin);;

			graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			//draw axes
			graph.setColor(Color.gray);
			graph.drawLine(sidePad,plotHeight+topPad,sidePad+plotWidth,plotHeight+topPad);
			graph.drawLine(sidePad,plotHeight+topPad,sidePad,topPad);
			graph.drawString(""+sMax,2,topPad);
			graph.drawString("0.0",2,topPad+plotHeight);
			//drawMean
			graph.setColor(Color.lightGray);
			graph.drawLine(sidePad,plotHeight+topPad -(int)(sMean*ySF),
					sidePad+plotWidth,plotHeight+topPad -(int)(sMean*ySF));
			//draw points
			graph.setColor(Color.CYAN);
			int x = sidePad;
			int y=plotHeight+topPad;
			for (int i = 0; i < stressPoints.length; i++) {
				int xOld = x;
				int yOld = y;
				x = (int)(xSF*i)+sidePad;
				y = plotHeight+topPad -(int)(ySF*stressPoints[i]);
				graph.drawLine(xOld,yOld,x,y);
			}
		}
		}
	}
	
	public void calcStresses(){
		if( stressPoints == null){
			stressPoints = new double[engine.getNumSlices()];
		}
		if (engine.getLayout() instanceof MultiCompKKLayout){
			double scaleFactor = Double.parseDouble(
					engine.getCurrentApplySettings().getProperty(MultiCompKKLayout.OPT_DIST));
			for (int i = 0; i < stressPoints.length; i++) {
				stressPoints[i] = NetUtils.getStress(engine.getSlice(i),scaleFactor,engine);
			}
		}
		System.out.println(" stress for each slice:\n"+Arrays.toString(stressPoints));
		repaint();
	}
	
	

}
