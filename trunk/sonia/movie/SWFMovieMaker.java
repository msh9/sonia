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
package sonia.movie;


import java.io.IOException;

import sonia.SoniaCanvas;
import sonia.SoniaController;
import sonia.SoniaLayoutEngine;
import sonia.render.SWFRender;
import sonia.settings.MovieSettings;

import com.anotherbigidea.flash.movie.Frame;
import com.anotherbigidea.flash.movie.Movie;
import com.anotherbigidea.flash.movie.Shape;
import com.anotherbigidea.flash.structs.Color;


/**
 * Exports movie as flash SWF vector graphics animation file using the JavaSWF 
 * library.  Still a very rough adaptation, some features not implemented.  
 * Bigest problem is that all the arcs must be redrawn for every interpolated frame
 * and there is a limit of 65535 objects in the flash file.  So for a larger movie, 
 * after a certain point it will begin to recycle the object numbers from ealier 
 * in the movie, rendering out a bunch of garbage
 * @author skyebend
 *
 */
public class SWFMovieMaker implements MovieMaker {

	private Movie movie;

	private SWFRender renderer;

	private Frame currentFrame;

	private SoniaCanvas canvas;

	private String file;

	private SoniaController control;
	
	private SoniaLayoutEngine engine;
	
	private boolean isExporting = false;
	
	private int debugFrameCount = 0;
	



	public SWFMovieMaker(SoniaController control, SoniaLayoutEngine engine, String fileAndPath) {
		this.control = control;
		this.engine = engine;
		this.file = fileAndPath;
	}

	public void setupMovie(SoniaCanvas canvas, int frames) throws Exception {
		control.showStatus("setup flash movie");
		isExporting = true;


		this.canvas = canvas;
		 movie = new Movie();
		 movie.setBackColor(new Color(255, 255, 255));
		 movie.setFrameRate((int)Math.round(1.0/((double)engine.getFrameDelay()/1000.0))); //fps
		 currentFrame = movie.appendFrame();
		renderer = new SWFRender();
		renderer.setDrawingTarget(currentFrame);
		drawBorder();
	}
	
	/**
	 * creats a play and pause button for the movie
	 * @author skyebend
	 */
	private void drawController(){
		Shape pause = new Shape();
		pause.defineFillStyle(new Color(255, 50, 50));
		pause.defineLineStyle(0.5, new Color(50, 50, 50));
		pause.setRightFillStyle(1);
		pause.setLineStyle(1);
		pause.move(-2, -2); // move coords are absolute
		pause.line(2, -2); // line
		pause.line(2, 2 );
		pause.line(-2,2);
		pause.line(-2, -2 );
		currentFrame.placeSymbol(pause, 3,3);	
	}

	/**
	 * draws a frame around the movie area, mostly for debugging
	 * 
	 * @author skyebend
	 * @throws IOException
	 */
	private void drawBorder() throws IOException {
		// --define a shape
		Shape shape = new Shape();
		shape.defineFillStyle(new Color(255, 255, 255));
		shape.defineLineStyle(0.5, new Color(50, 50, 50));
		shape.setRightFillStyle(1);
		shape.setLineStyle(1);
		shape.move(0, 0); // move coords are absolute
		shape.line(canvas.getWidth(), 0); // line
		shape.line(canvas.getWidth(), canvas.getHeight() );
		shape.line(0,canvas.getHeight());
		shape.line(0, 0 );
		currentFrame.placeSymbol(shape, 0,0);
	}

	public void captureImage() {
		renderer.newFrame();
		currentFrame = movie.appendFrame();
		canvas.getRenderSlice().render(currentFrame, canvas, renderer);
		//ask the renderer to remove objects that should no longer be showing
		debugFrameCount++;
	}

	/**
	 * write out the flash file. if there are more than 65535 objects, the end 
	 * of the movie will be gobeldygook. 
	 * 
	 * @author skyebend
	 * @param fileAndPath
	 * @throws IOException
	 */
	public void finishMovie() {
		try {
			boolean compressed = true;
			//clean up remaining events
			//renderer.newFrame();
			// create an action to stop the movie from looping
			currentFrame.stop();
			movie.write(file,compressed);
		} catch (IOException e) {
			control.showError("Error export SWF movie:" + e.getMessage());
			e.printStackTrace();
		}
		
		// debug
		
		control.showStatus("Saved flash file to:" + file);
		control.log("Saved flash file to:" + file);
		// debug more by saving out a human readable deparsed representation
//		FileInputStream in;
//		try {
//			in = new FileInputStream(file);
//			SWFTagDumper dumper = new SWFTagDumper(new FileOutputStream(file
//					+ ".dump.txt"), false, true);
//			TagParser parser = new TagParser(dumper);
//			SWFReader reader = new SWFReader(parser, in);
//			reader.readFile();
//			in.close();
//
//			// must flush - or the output will be lost when the process ends
//			dumper.flush();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		isExporting = false;

	}

	public boolean isExporting() {
		return isExporting;
	}

	public void configure(MovieSettings settings) {
		// TODO Auto-generated method stub
		
	}
	


	

}
