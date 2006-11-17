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
package sonia;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import com.anotherbigidea.flash.SWFConstants;
import com.anotherbigidea.flash.interfaces.SWFActionBlock;
import com.anotherbigidea.flash.interfaces.SWFActions;
import com.anotherbigidea.flash.interfaces.SWFShape;
import com.anotherbigidea.flash.movie.Font;
import com.anotherbigidea.flash.movie.FontDefinition;
import com.anotherbigidea.flash.movie.FontLoader;
import com.anotherbigidea.flash.movie.Frame;
import com.anotherbigidea.flash.movie.Instance;
import com.anotherbigidea.flash.movie.Movie;
import com.anotherbigidea.flash.movie.Shape;
import com.anotherbigidea.flash.readers.SWFReader;
import com.anotherbigidea.flash.readers.TagParser;
import com.anotherbigidea.flash.structs.Color;
import com.anotherbigidea.flash.structs.Matrix;
import com.anotherbigidea.flash.structs.Rect;
import com.anotherbigidea.flash.writers.SWFTagDumper;
import com.anotherbigidea.flash.writers.SWFWriter;
import com.anotherbigidea.flash.writers.TagWriter;

public class SWFMovieMaker implements MovieMaker {

	private Movie movie;

	private Graphics2dSWF gSWF;

	private SWFRender renderer;

	private TagWriter swf;

	private Frame currentFrame;

	private SoniaCanvas canvas;

	private String file;

	private SoniaController control;

	private Render oldRender;

	private SWFWriter writer;
	
	

	public SWFMovieMaker(SoniaController control, String fileAndPath) {
		this.control = control;
		this.file = fileAndPath;
	}

	public void setupMovie(SoniaCanvas canvas, int frames) throws Exception {
		// debug
		System.out.println("setup flash movie");
		


		this.canvas = canvas;
		 movie = new Movie();
		 movie.setBackColor(new Color(255, 255, 255));
//		 try{
//				fontdef = FontLoader.loadFont(this.getClass().getResourceAsStream("image/VerdanaFont.swf"));
//				font  = new Font(fontdef);
//				font.loadAllGlyphs();
//				
//				//swf.tagDefineFont2(1,0,"font",font.getGlyphList().size(),fontdef.getAscent(),fontdef.getDescent(),fontdef.getLeading(),null,fontdef.);
//				} catch (IOException e){
//					System.out.println("Error loading font for swf: "+e.toString());
//					e.printStackTrace();
//				}
		 currentFrame = movie.appendFrame();
		// gSWF = new
		// Graphics2dSWF((Graphics2D)canvas.getGraphics(),currentFrame);
		renderer = new SWFRender(movie);
		renderer.setDrawingTarget(currentFrame);
		//writer = new SWFWriter(file);
	//	swf = new TagWriter(writer);

//		swf.header(5, // Flash version
//				-1, // unknown length
//				canvas.getWidth() * SWFConstants.TWIPS, // width in twips
//				canvas.getHeight() * SWFConstants.TWIPS, // height in twips
//				1, // frames per sec
//				-1); // unknown frame count

		//swf.tagSetBackgroundColor(new Color(255, 255, 255));
		drawBorder();
		//renderer.newFrame();
		//swf.tagShowFrame();

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
		canvas.getRenderSlice().render(currentFrame, canvas, renderer);
		renderer.newFrame();
		currentFrame = movie.appendFrame();
	}

	/**
	 * write out the flash file
	 * 
	 * @author skyebend
	 * @param fileAndPath
	 * @throws IOException
	 */
	public void finishMovie() {
		try {
			 
			// create an action to stop the movie from looping
			currentFrame.stop();
			movie.write(file);
			
//			SWFActions actions = swf.tagDoAction();
//			SWFActionBlock ab = actions.start(0);
//			ab.stop();
//			ab.end();
//			actions.done();
//			swf.tagEnd();
//			writer.close();

		} catch (IOException e) {
			control.showError("Error export SWF movie:" + e.getMessage());
			e.printStackTrace();
		}
		

		// debug
		System.out.println("Saved flash file to:" + file);
		// debug more by saving out a deparsed representation
		FileInputStream in;
		try {
			in = new FileInputStream(file);
			SWFTagDumper dumper = new SWFTagDumper(new FileOutputStream(file
					+ ".dump.txt"), false, true);
			TagParser parser = new TagParser(dumper);
			SWFReader reader = new SWFReader(parser, in);
			reader.readFile();
			in.close();

			// must flush - or the output will be lost when the process ends
			dumper.flush();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public boolean isExporting() {
		return false;
	}

}
