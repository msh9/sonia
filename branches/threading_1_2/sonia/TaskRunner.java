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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;



/**
 * maitains a queue of tasks and there dependencies. Controls and monitors their
 * execution and reports status.
 * 
 * @author skyebend
 * 
 */
public class TaskRunner implements TaskListener {

	private LinkedBlockingQueue<LongTask> tasks;

	private LinkedBlockingQueue<LongTask> runningTasks;

	private HashMap<LongTask, LongTask> dependencies;

	private Vector<TaskListener> UIs;

	private static UIRunner uiRunner;

	private static ExecutorService threadService;

	public TaskRunner() {
		tasks = new LinkedBlockingQueue<LongTask>();
		runningTasks = new LinkedBlockingQueue<LongTask>();
		dependencies = new HashMap<LongTask, LongTask>();
		UIs = new Vector<TaskListener>();
		threadService = Executors.newSingleThreadExecutor();

	}

	/**
	 * add a task to the list, start ASAP
	 */
	public void runTask(LongTask task) {
		tasks.add(task);
		task.addTaskEventListener(this);
		checkQueue();
	}

	/**
	 * looks for queued tasks that do not have dependencies and executes them on
	 * a new thread
	 * 
	 * @author skyebend
	 */
	private void checkQueue() {
		if (tasks.size() > 0) {
			Iterator taskiter = tasks.iterator();
			while (taskiter.hasNext()) {
				LongTask task = (LongTask) taskiter.next();
				if (!dependencies.containsKey(task)) {
					tasks.remove(task);
					runningTasks.add(task);
					// start the task on a new thread
					threadService.submit(task);
					// new Thread(task, task.getTaskName()).start();
					// start the ui updating, if appropriate
					runUIupdater();
				}
			}
			Iterator<LongTask> runiter = runningTasks.iterator();
			while (runiter.hasNext()) {
				LongTask task = runiter.next();
				if (task.isDone()) {
					runningTasks.remove(task);
				}
			}
		}
	}

	/**
	 * starts a task after another task has finished
	 */
	public void runTaskAfter(LongTask task, LongTask prevTask) {
		tasks.add(task);
		task.addTaskEventListener(this);
		dependencies.put(task, prevTask);
	}

	/**
	 * Asks all currently running taks to stop
	 * 
	 */
	public void stopAllTasks() {
		tasks.clear();
		Iterator taskiter = runningTasks.iterator();
		while (taskiter.hasNext()) {
			LongTask task = (LongTask) taskiter.next();
			task.stop();
			// TODO: should kill the uiRunner here
		}
		runningTasks.clear();
	}

	public void taskStatusChanged(LongTask task) {
		// check if it is done
		if (task.isDone()) {
			runningTasks.remove(task);
			// check if there is a dependent task
			if (dependencies.containsKey(task)) {
				// if there is, get it and start on a new thread
				LongTask newTask = dependencies.remove(task);

			}
			checkQueue();
		}
		// check if it is error
		// report status

	}

	/**
	 * Puts the ui on a list of objects that need up be updated on status
	 * changes
	 * 
	 * @author skyebend
	 */
	public void addUItoUpdate(TaskListener ui) {
		UIs.add(ui);
	}

	/**
	 * class that loops and sleeps on the ui thread, updating all regestrid ui
	 * stuff
	 * 
	 * @author skyebend
	 * 
	 */
	public class UIRunner implements Runnable {
		private boolean isRunning = false;

		public void run() {
			isRunning = true;
			// while there are still tasks waiting
			while (runningTasks.size() > 0) {
				// for each task waiting or running
				Iterator listeniter = UIs.iterator();
				// loop over listeing uis and update
				while (listeniter.hasNext()) {
					TaskListener listener = (TaskListener) listeniter.next();
					listener.taskStatusChanged(null);
				}
				// }
				try {
					// TODO: UI update thread should stop if tasks paused
					// Thread.yield();
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			isRunning = false;
		}

		public boolean isRunning() {
			return isRunning;
		}
	} // end ui runner inncer class

	/**
	 * runs a process on and independent (not on event thread) to update ui
	 * 
	 * @author skyebend
	 */
	private void runUIupdater() {
		// check if there are any uis registerd
		if (UIs.size() > 0) {
			// launch a thread to check for ui updates every half second
			if (uiRunner == null) {
				uiRunner = new UIRunner();
			}
			if (!uiRunner.isRunning()) {
				// lauch ui runner on on swing Ui thread
				// WTF, doesn't work at all
				// SwingUtilities.invokeLater(uiRunner);
				// threadService.submit(uiRunner); //'causes ordering problems
				// if on a single thread
				// SO, we launch on its own thread so queue doesn't interfere
				// not swing ui, but at least not event distpatch thread
				new Thread(uiRunner, "UIupdater").start();
			}
		}

	}
}
