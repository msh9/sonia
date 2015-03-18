package sonia.movie;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.bric.qt.*;

import sonia.SoniaCanvas;
import sonia.SoniaController;
import sonia.SoniaLayoutEngine;
import sonia.render.Graphics2DRender;
import sonia.settings.MovieSettings;

/**
 * This movie output uses code adapted from Jeremy Wood http://javagraphics.blogspot.com
 * It uses his JPEGMovieAimation library located at https://javagraphics.dev.java.net/jars/JPEGMovieAnimation-bin.jar and made availible under a Modified BSD License contained within the jar file.
 * The purpose of this exporter is to provide a lowest-common-denominator all-java means of exporting animations from sonia.  The animation files produced from this will be HUGE and probably need to be compressed by another application before they are useful. 
 * @author skyebend
 *
 */
public class JPEGMovieMaker implements MovieMaker {
	
	private SoniaCanvas canvas;

	private String outputDir; //the path where output files will be written

	private SoniaController control;
	
	private SoniaLayoutEngine engine;
	
	private Graphics2DRender renderer;
	
	private boolean isExporting = false;
	
	
	private Throwable error = null;
	
	private BufferedImage bufferedImage;
	
	private Graphics movieGraphics;
	
	private JPEGMovieAnimation animation;
	
	private int width = -1, height = -1;
	private float imageQuality = 0.9f;
	
	public JPEGMovieMaker(SoniaController control, SoniaLayoutEngine engine, String fileAndPath) {
		this.control = control;
		this.engine = engine;
		outputDir = fileAndPath;
		renderer = new Graphics2DRender();
	

	
	}
	
	public void setupMovie(SoniaCanvas canvas, int frames) throws Exception {
		this.canvas = canvas; 
		isExporting = true;
		width = canvas.getWidth();
		height = canvas.getHeight();
//		 create a new file in the output dir
		File file = new File(outputDir);
		//create the animation to write images to
		animation = new JPEGMovieAnimation(file);
//		create a new buffered image
		bufferedImage = new BufferedImage(
				width, height, BufferedImage.TYPE_INT_RGB);
//		fill a white background into the image HACK
		movieGraphics = bufferedImage.createGraphics();


	}

	public void captureImage() {
		
		movieGraphics.setColor(canvas.getBackground());
		movieGraphics.fillRect(0, 0, width,height);
		//ask the canvas to render the image
		canvas.updateDisplay(movieGraphics, canvas.isGhostSlice());
		//canvas.getRenderSlice().render(g, canvas, renderer);
		try {
			animation.addFrame((float)engine.getFrameDelay()/1000.0f,bufferedImage, 1f);//delay, qualty
		} catch (IOException e) {
			control.showError("Error writing frame to JPEG Movie animation:"+e.getMessage());
			error = e;
		}
		
	}

	public void configure(MovieSettings settings) {
		// TODO adding settings for compression quality

	}
	
	/** This finishes writing the movie file.
	 * <P>This is responsible for writing the structure of the
	 * movie data, and finishing all IO operations to the file.
	 * @throws IOException
	 */
	public void finishMovie() {
		try {
			animation.close();
		} catch (IOException e) {
			control.showError("Error closing JPEG Movie animation:"+e.getMessage());
			error = e;
		}
		// all the image files should already have been exported, so just report done
		control.showStatus("Finished exporting JPEG movie images to "+outputDir);
	}
	

	public boolean isExporting() {

		return isExporting;
	}



	public Throwable getError() {
		return error;
	}
	
	

	
	

}
