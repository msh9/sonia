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

import java.util.HashSet;
import java.util.Iterator;

import sonia.ui.LayoutWindow;

/**
 * task for managing the exporting of the movie
 * @author skyebend
 *
 */
public class PlayAnimationTask implements LongTask {

	private SoniaLayoutEngine engine;
	private String status = "preparing to start...";
	private int currentSlice = 0;
	private boolean stop = false;
	private boolean isError = false;
	private HashSet listeners = new HashSet();


	public PlayAnimationTask(SoniaLayoutEngine engine) {
		this.engine = engine;
		//movieSettings = settings;
	}

	public String getTaskName() {
		return "playing movie";
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
		try{
		LayoutWindow window = engine.getLayoutWindow();
		currentSlice = engine.getCurrentSliceNum();
		int endIndex = engine.getNumSlices();
		engine.setTransitionActive(true);
		while (currentSlice < endIndex) {
			// check for pause
			
			System.out.println("slice:" + currentSlice);
			if (!stop) {
				window.transitionToSlice(currentSlice,null);
				status = "showing slice "+currentSlice;
				currentSlice++;
			} else {
				engine.setTransitionActive(false);
				reportStatus();
				break;
			}
		}
		} catch (Exception e){
			status = "Error in play thread: "+e.getMessage();
			e.printStackTrace();
		}
		stop=true;
		engine.setTransitionActive(false);
		reportStatus();
		

	}


}
