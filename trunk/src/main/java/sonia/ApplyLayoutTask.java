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

import sonia.layouts.MultiCompKKLayout;
import sonia.layouts.NetLayout;
import sonia.settings.ApplySettings;

public class ApplyLayoutTask implements LongTask {

	private SoniaLayoutEngine engine;

	//private String name = "Apply ";


	private Boolean stopped = false;

	private HashSet listeners = new HashSet();
	private NetLayout layout;
	private int width;
	private int height;
	private ApplySettings settings;
	private LayoutSlice slice;
	private int maxSteps = -1;
	private SoniaController control;
	private String status = "";


	public ApplyLayoutTask(SoniaController cont, SoniaLayoutEngine engine, NetLayout layout,
			int width, int height, LayoutSlice slice, ApplySettings settings) {
		this.engine = engine;
		this.layout = layout;
		this.width = width;
		this.height = height;
		this.slice = slice;
		this.settings = settings;
		this.control = cont;
	}

	public String getTaskName() {
		return "Apply "+layout.getLayoutType()+" to slice "
		+slice.getSliceStart()+"-"+slice.getSliceEnd();
	}


	public void stop() {
		layout.pause();
		stopped = true;

	}

	public String getStatusText() {
		return status+layout.getLayoutInfo();
	}

	public boolean isDurationKnown() {
		if (layout instanceof MultiCompKKLayout){
			return true;
		};
		return false;
	}

	public int maxSteps() {
		if (layout instanceof MultiCompKKLayout){
			return ((MultiCompKKLayout)layout).maxSteps();
			
		}
		return maxSteps;
	}

	public int currentStep() {
		if (layout instanceof MultiCompKKLayout){
			return ((MultiCompKKLayout)layout).currentStep();
		}
		return 0;
	}

	public boolean isError() {
		return slice.isError();
	}

	public boolean isDone() {
		return slice.isLayoutFinished();
	}

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
			layout.applyLayoutTo(slice,width,height,settings);
			
		} catch (Throwable t){
			String er = " Error during layout: "
				+t.getClass().getSimpleName()+": "+t.getCause();
			t.printStackTrace();
			control.showError(er);
			status = er;
			slice.setError(true);
			slice.setLayoutFinished(true);
			
		}
		reportStatus();
	}

	

}
