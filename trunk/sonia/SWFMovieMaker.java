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

import com.anotherbigidea.flash.movie.Frame;
import com.anotherbigidea.flash.movie.Instance;
import com.anotherbigidea.flash.movie.Movie;
import com.anotherbigidea.flash.readers.SWFReader;
import com.anotherbigidea.flash.readers.TagParser;
import com.anotherbigidea.flash.writers.SWFTagDumper;

public class SWFMovieMaker implements MovieMaker {
	
	private Movie movie;
	private Graphics2dSWF gSWF;
	private Frame currentFrame;
	private SoniaCanvas canvas;
	private String file;
	private SoniaController control;
	
	
	public SWFMovieMaker(SoniaController control, String fileAndPath){
		this.control = control;
		this.file = fileAndPath;
	}
	
	public void setupMovie(SoniaCanvas canvas, int frames) throws Exception {
		this.canvas = canvas;
		movie = new Movie();
		currentFrame = movie.appendFrame();
		gSWF = new Graphics2dSWF((Graphics2D)canvas.getGraphics(),currentFrame);
		
	}
	

	public void captureImage() {
		//debug
		//draw bounding box for movie
		Rectangle2D bounds = canvas.getBounds();
		gSWF.draw(bounds);
		//get the canvas to draw on the "fake" graphics context
		canvas.getRenderSlice().paint(gSWF,canvas);
		currentFrame  = movie.appendFrame();
		Iterator instIter = gSWF.getInstances().iterator();
		while (instIter.hasNext()){
			Instance inst = (Instance)instIter.next();
			currentFrame.remove(inst);
		}
	}
	/**
	 * write out the flash file
	 * @author skyebend
	 * @param fileAndPath
	 * @throws IOException
	 */
	public void finishMovie() {
			try {
				movie.write(file);
			} catch (IOException e) {
				control.showError("Error export SWF movie:"+e.getMessage());
				e.printStackTrace();
			}
			
//			debug
			System.out.println("Saved flash file to:"+file);
			//debug more by saving out a deparsed representation
			  FileInputStream in;
			try {
				in = new FileInputStream( file );
				 SWFTagDumper dumper = new SWFTagDumper(new FileOutputStream(file+".dump.txt") ,false, true );
			        TagParser parser = new TagParser( dumper );
			        SWFReader reader = new SWFReader( parser, in );
			        reader.readFile();
			        in.close();
			        
			        //must flush - or the output will be lost when the process ends
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
