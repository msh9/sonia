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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import quicktime.streaming.SettingsDialog;

import sonia.layouts.NetLayout;
import sonia.movie.MovieMaker;
import sonia.settings.ApplySettings;
import sonia.settings.MovieSettings;
import sonia.ui.LayoutWindow;

/**
 * task for managing the exporting of the movie
 * @author skyebend
 *
 */
public class MovieExportTask implements LongTask {

	private SoniaLayoutEngine engine;
	private MovieMaker maker;
	private String status = "preparing to start...";
	private int currentSlice = 0;
	private boolean stop = false;
	private boolean isError = false;
	private HashSet listeners = new HashSet();


	public MovieExportTask(SoniaLayoutEngine engine, 
			MovieMaker maker) {
		this.engine = engine;
		//movieSettings = settings;
		this.maker = maker;
	}

	public String getTaskName() {
		return "Exporting movie";
	}

	public void stop() {
		stop = true;
	}

	public String getStatusText() {
		return status;
	}

	public boolean isDurationKnown() {
		return true;
	}

	public int maxSteps() {
		return engine.getNumSlices();
	}

	public int currentStep() {
		return currentSlice;
	}

	public boolean isError() {
		return isError;
	}

	public boolean isDone() {
		return stop;
	}

	/**
	 * 
	 */
	public void addTaskEventListener(Object listener) {
		listeners.add(listener);

	}

	private void reportStatus() {
		Iterator listeniter = listeners.iterator();
		while (listeniter.hasNext()) {
			TaskListener listener = (TaskListener) listeniter.next();
			listener.taskStatusChanged(this);
		}
	}



	public void run() {
		try {
		LayoutWindow window = engine.getLayoutWindow();
		int endIndex = engine.getNumSlices();
		
		int numFrames = endIndex * engine.getInterpFrames();
		
			maker.setupMovie(window.getDisplay(), numFrames);
			window.transitionToSlice(0,null);
			maker.captureImage();
			 currentSlice = 0;
			//movie export loop
			while (currentSlice < endIndex-1) {
				 currentSlice++;
				// awkward transition checks if movie is being recorded and
				// saves theframe
				 status = "exporting slice "+currentSlice;
				window.transitionToSlice(currentSlice,maker);
				// make it so that movie recording can be stopped if something
				// goes wrong
				if (stop) {
					break;
				}
			}
			maker.finishMovie();
			maker = null;
			stop = true;
			status = "Movie export finished";
			reportStatus();
		} catch (Exception e) {
			isError = true;
			status = "Movie export error: "+e.getMessage();
			stop = true;
			reportStatus();
		}
		

	}


}
