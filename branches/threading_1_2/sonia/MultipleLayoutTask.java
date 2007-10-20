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
import java.util.Vector;

import sonia.layouts.NetLayout;
import sonia.settings.ApplySettings;

public class MultipleLayoutTask implements LongTask, TaskListener{

	private SoniaLayoutEngine engine;
	private ApplySettings applySettings;
	private String status = "preparing to start...";
	private int startSlice = 0;
	private int currentSlice = 0;
	private int slicesLeft =0;
	private int errorSlices = 0;
	private boolean stop = false;
	private boolean isError = false;
	private HashSet listeners = new HashSet();
	
	public MultipleLayoutTask(SoniaLayoutEngine engine){
		this.engine = engine;
		applySettings = engine.getCurrentApplySettings();
	}
	
	public String getTaskName() {
		return "Applying layout to multiple slices";
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
		return engine.getNumSlices() - startSlice;
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
	
	private void nextSlice(){
		if ((slicesLeft > 0) & (!stop)){
			status = "Applying layout to slice " + currentSlice ;
			currentSlice++;
			slicesLeft--;
			//engine.changeToSliceNum(currentSlice);
			LayoutSlice slice = engine.getCurrentSlice();
			//layout will run in new thread..
			engine.applyLayoutTo(applySettings, slice);
		} else {
			status = "Layouts complete with " + errorSlices
			+ " error slices";
			stop =true;
			reportStatus();
		}
	}

	public void run() {
// layout to all subsequent layouts
		// makesure there is a layout chosen
		if (engine.getLayout() instanceof LongTask){
			((LongTask)engine.getLayout()).addTaskEventListener(this);
		}
		errorSlices = 0;
		startSlice = engine.getCurrentSliceNum();
		currentSlice = startSlice;
		 slicesLeft = engine.getNumSlices()- startSlice;
		// get the settings
		if (applySettings == null) {
			applySettings = engine.getCurrentApplySettings();
		}
		stop = false;
		nextSlice();
	
	}

	/**
	 * asumes it was posted by a completing layout, checks if layout is done
	 * so that the next one can be started
	 */
	public void taskStatusChanged(LongTask task) {
		// we assume task update was from the layout
		
		if (engine.getSlice(currentSlice).isLayoutFinished()){
			if (engine.getSlice(currentSlice).isError()) {
				errorSlices ++;
				if (applySettings.get(ApplySettings.STOP_ON_ERROR).equals(
						Boolean.toString(true))) {
					slicesLeft = 0;
					isError = true;
					stop=true;
					status = "Stopped from error on slice "+currentSlice;
					reportStatus();
				}
			} else {
				nextSlice();
			}
			
		}
		
	}

}
