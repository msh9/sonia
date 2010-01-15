package sonia.layouts;

import mdsj.DistanceScaling;
import mdsj.MDSJ;


import com.sun.tools.javac.code.Type.ForAll;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;

import sonia.CoolingSchedule;
import sonia.LayoutSlice;
import sonia.LayoutUtils;
import sonia.NetUtils;
import sonia.SoniaController;
import sonia.SoniaLayoutEngine;
import sonia.settings.ApplySettings;
import sonia.ui.ApplySettingsDialog;

/**
 * This layout relies on the MDSJ Ð Multidimensional Scaling for Java package.  
 * http://www.inf.uni-konstanz.de/algo/software/mdsj/
 * 
 * It is being developed at the University of Konstanz, Department of Computer & Information Science, Algorithmics Group. It is free for academic and research purposes; no registration is required. 
 * 
 * License Conditions: MDSJ is available under the terms and conditions of the Creative Commons License "by-nc-sa" 3.0. This means that you can
http://creativecommons.org/licenses/by-nc-sa/3.0/
 *   - freely copy, share, and distribute the library at no cost and
 *   - re-use the library as a component in your software,

as long as you

 *   - include a clear reference to the author of MDSJ, "University of Konstanz, Department of Computer & Information Science, Algorithmics Group" together with a reference to this WWW site ("by"),
 *   - do not use MDSJ for any commercial purposes ("nc"), and
  *  - apply these conditions to all your pieces of software that make use of MDSJ ("sa"). 

* Correspondence with developer Christian Pich stated that they were OK with the library being used as a 
* linked library withen a GPL project provided that the project gave clear warnings of the non-comerical use
* restriction when the algorithms are used.
* 
*  (description below is edited from Cristian's email, miss-spellings are Skye's): 
* 
* Generally, distance scaling is about minimizing
* 
*   stress(x_1,...,x_n) = \sum_{i<j}w_ij (d_ij - ||x_i-x_j||)^2
*   
* In contrast to Kamada-Kawai (where stress is optimized with a gradient descent algorithm) 
* the variant implemented in MDSJ uses majorization, 
* tries to minimize a different function, which bounds the stress from 
* above (i.e., is a majorizer), and which is much easier to handle; it can 
* be thought of as a multidimensional parabola, which has only one global 
* minimum (the whole approach is thus called "convex optimization").  By 
* iteratively pressing such a parabola down into the stress landscape, a 
* minimizer for the stress function is found.
*
* The default scaling exponent is -2, which increases the impact of smaller 
* distances (=emphasizes local structures) and decreases the impact of 
* larger distances.  Using  +2, would reverse this effect. 
 */
public class MDSJLayout implements NetLayout {
	
	private SoniaController control;

	private SoniaLayoutEngine engine;
	
	private String layoutInfo;
	
	private ApplySettings settings;
	
	private double replaceWeight = 10;
	
	private double scalingExp = -2;
	
	private int iterations = 50;
	
	private double xOffset;
	private double yOffset;
	private double scaleFactor = 20;
	
	/**
	 * value used to replace the infinate distances between disconnected nodes. 
	 */
	public static final String COMP_CONN ="comp connect value";
	
	/**
	* The default scaling exponent is -2, which increases the impact of smaller 
	* distances (=emphasizes local structures) and decreases the impact of 
	* larger distances.  Using  +2, would reverse this effect. 
    */
	public static final String DIST_SCALE_EXP ="dist scale exp";
	
	/**
	 * Factor by which to scale up the MDS results, roughly equivilent to length of unit edge in pixels
	 */
	public static final String SCALE_FACTOR ="scale factor";
	
	/**
	 * Number of iterations of the stress majorization algorithm
	 */
	public static final String ITERATIONS = "num iterations";
	
	public MDSJLayout(SoniaController cont, SoniaLayoutEngine eng) {
		control = cont;
		engine = eng;
	
	}

	public void applyLayoutTo(LayoutSlice slice, int width, int height,
			ApplySettings settings) {
		this.settings = settings;
		//read out the settings and validate
		if (settings.getProperty(COMP_CONN)==null){
			control.showStatus("Layout setting for "+COMP_CONN+" not specified, using default of "+replaceWeight);
		} else {
			try {
				replaceWeight = Double.parseDouble(settings.getProperty(COMP_CONN,replaceWeight+""));
			} catch (Exception e) {
				control.showError("Unable to parse layout setting value of "+COMP_CONN+" : "+e);
			}
		}
		if (settings.getProperty(DIST_SCALE_EXP)==null){
			control.showStatus("Layout setting for "+DIST_SCALE_EXP+" not specified, using default of "+scalingExp);
		} else {
			try {
				scalingExp = Double.parseDouble(settings.getProperty(DIST_SCALE_EXP,scalingExp+""));
			} catch (Exception e) {
				control.showError("Unable to parse layout setting value of "+DIST_SCALE_EXP+" : "+e);
			}
		}
		if (settings.getProperty(SCALE_FACTOR)==null){
			control.showStatus("Layout setting for "+SCALE_FACTOR+" not specified, using default of "+scaleFactor);
		} else {
			try {
				scaleFactor = Double.parseDouble(settings.getProperty(SCALE_FACTOR,scaleFactor+""));
			} catch (Exception e) {
				control.showError("Unable to parse layout setting value of "+SCALE_FACTOR+" : "+e);
			}
		}
		if (settings.getProperty(ITERATIONS)==null){
			control.showStatus("Layout setting for "+ITERATIONS+" not specified, using default of "+iterations);
		} else {
			try {
				iterations = (int)Double.parseDouble(settings.getProperty(ITERATIONS,iterations+""));
			} catch (Exception e) {
				control.showError("Unable to parse layout setting value of "+ITERATIONS+" : "+e);
			}
		}
		xOffset = engine.getLayoutWidth()/2;
		yOffset = engine.getLayoutWidth()/2;
		
		//get acknoledgement of non-comercial use
		control.showStatus("MDSJ library and layouts are for non-comericial use only.");
		slice.setLayoutFinished(false);
		double[] sliceXCoords = slice.getXCoords(); // slice's object
		double[] sliceYCoords = slice.getYCoords(); // modifications will move nodes
		//get the network from the slice
		//get all shortest path distnaces
		//convert to dissimilarity
		layoutInfo = "Calculating shortest path distances...";
		DenseDoubleMatrix2D distMatrix = NetUtils
		.getFastFastAllShortPathMatrix(NetUtils.getSymMaxMatrix(slice),
				engine.getMaxMatrixVal(), engine
				.getMinMatrixValue(),false);
		//if network was disconnected, replace infinate values with a new value
		for (int i = 0; i < distMatrix.rows(); i++) {
			for (int j = 0; j < distMatrix.columns(); j++) {
				if (distMatrix.getQuick(i,j)== Double.POSITIVE_INFINITY){
					distMatrix.setQuick(i,j,replaceWeight);
				}
			}
		}
		
		//convert distnace matrix to an array
		double[][] distArray = distMatrix.toArray();
		
		//make an array to hold the output
		double[][] newCoords = new double[2][slice.getMaxNumNodes()];
		//	copy the slice coords
		for (int i=0;i<sliceXCoords.length;i++){
			newCoords[0][i] = (sliceXCoords[i]/scaleFactor)-xOffset;
			newCoords[1][i] = (sliceYCoords[i]/scaleFactor)-yOffset;
		}
		
		//compute a weight matrix
		double[][] weights = DistanceScaling.weightMatrix(distArray, scalingExp);
		//run the  mds calculation
		layoutInfo = "Starting MDS distance optimization...";
		//MDSJ.distanceScaling(distArray, newCoords,scalingExp);
		doOptimization(newCoords,distArray,weights);
		
		//normalize, recenter, dialate coordinates to fit display area
		//update the slice coords
		for (int i=0;i<sliceXCoords.length;i++){
			sliceXCoords[i] = (newCoords[0][i]*scaleFactor)+xOffset;
			sliceYCoords[i] = (newCoords[1][i]*scaleFactor)+yOffset;
			//debug
			//System.out.println(i+":"+sliceXCoords[i]+","+sliceYCoords[i]  );
		}
		//LayoutUtils.rescalePositions(slice, engine.getLayoutWidth(), 
		//		engine.getLayoutHeight(), sliceXCoords, sliceYCoords, false);
		
		engine.finishLayout(settings, this, slice, engine.getLayoutWidth(), engine.getLayoutHeight());
		slice.setLayoutFinished(true);
		layoutInfo = "Finished MDS calculation.";
		
	}
	
	private void doOptimization(double[][] coords,double[][] dists,double[][] weights){
		//TODO: launch this on a thread
		//debug
		control.log("mdsj starting stress: "+DistanceScaling.stress(dists, weights, coords));
		DistanceScaling.majorize(coords, dists, weights, iterations);
//		debug
		control.log("mdsj ending stress: "+DistanceScaling.stress(dists, weights, coords));
	}

	public void disposeLayout() {

	}

	public String getLayoutInfo() {
		return layoutInfo;
	}

	public String getLayoutType() {
		return "MDSJ distance scaling";
	}

	public void pause() {
		// TODO Auto-generated method stub

	}

	public void resume() {
		// TODO Auto-generated method stub

	}

	public void setupLayoutProperties(ApplySettingsDialog settings) {
		settings.addLayoutProperty(COMP_CONN,replaceWeight);
		settings.addLayoutProperty(DIST_SCALE_EXP, scalingExp);
		settings.addLayoutProperty(SCALE_FACTOR, scaleFactor);
		settings.addLayoutProperty(ITERATIONS, iterations);

	}

}
