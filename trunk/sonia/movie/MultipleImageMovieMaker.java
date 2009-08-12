package sonia.movie;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import sonia.SoniaCanvas;
import sonia.SoniaController;
import sonia.SoniaLayoutEngine;
import sonia.render.Graphics2DRender;
import sonia.settings.MovieSettings;

public class MultipleImageMovieMaker implements MovieMaker {
	
	private SoniaCanvas canvas;

	private String outputDir; //the path where output files will be written
	private String filetype;

	private SoniaController control;
	
	private SoniaLayoutEngine engine;
	
	private Graphics2DRender renderer;
	
	private boolean isExporting = false;
	
	private int frameNumber;
	
	private Throwable error = null;
	
	public MultipleImageMovieMaker(SoniaController control, SoniaLayoutEngine engine, String fileAndPath) {
		this.control = control;
		this.engine = engine;
		//TODO: strip off file extension
		outputDir = fileAndPath.substring(0,fileAndPath.indexOf("."));
		filetype = fileAndPath.substring(fileAndPath.indexOf(".")+1,fileAndPath.length());
		renderer = new Graphics2DRender();
		
		//String names[] = ImageIO.getWriterFormatNames();

		
	}

	public void captureImage() {
		//create a new buffered image
		BufferedImage bufferedImage = new BufferedImage(
				canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
		//fill a white background into the image HACK
		Graphics g = bufferedImage.createGraphics();
		//ask the canvas to render the image
		canvas.updateDisplay(g, canvas.isGhostSlice());
		//canvas.getRenderSlice().render(g, canvas, renderer);
		// create a new file in the output dir
		File file = new File(outputDir+"_"+frameNumber+"."+filetype);
		//write out the image contents
		try {
			ImageIO.write(bufferedImage,filetype,file);
			frameNumber++;
		} catch (IOException e) {
			control.showError("Error exporting movie images:"+e.getMessage());
			error = e;
		}
	}

	public void configure(MovieSettings settings) {
		// TODO Auto-generated method stub

	}

	public void finishMovie() {
		// all the image files should already have been exported, so just report done
		control.showStatus("Finished exporting movie images to "+outputDir);
	}

	public boolean isExporting() {

		return isExporting;
	}

	public void setupMovie(SoniaCanvas canvas, int frames) throws Exception {
		this.canvas = canvas; 
		isExporting = true;
		frameNumber = 1;
		control.showStatus("Configured multipleimage export");

	}

	public Throwable getError() {
		return error;
	}

}
