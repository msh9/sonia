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

/**
 * used to provide control flow and status update information for operations that 
 * will take some time to complete
 * @author skyebend
 *
 */
public interface LongTask extends Runnable {
	
	/**
	 * name of the task, probably used in the ui
	 * @author skyebend
	 * @return
	 */
	public String getTaskName();
	
	/**
	 * tell task to stop asap
	 * @author skyebend
	 */
	public void stop();
	
	/**
	 * returns status text for the running process
	 * @author skyebend
	 * @return
	 */
	public String getStatusText();
	
	/**
	 * true if it is possible to calculate at least an estimate of the number
	 * of steps in the task, false if task is indeterminate
	 * @author skyebend
	 * @return
	 */
	public boolean isDurationKnown();
	
	/**
	 * how many steps the task can take
	 */
	public int maxSteps();
	
	/**
	 * what step the task is on
	 * @author skyebend
	 * @return
	 */
	public int currentStep();
	
	/**
	 * is the task in an error state?
	 * @author skyebend
	 * @return
	 */
	public boolean isError();
	
	/**
	 * has the task completed / stopped?
	 * @author skyebend
	 * @return
	 */
	public boolean isDone();
	
	/**
	 * is the task still executing?
	 * @author skyebend
	 * @return
	 */
	//public boolean isRunning();
	
	/**
	 * give the task an object to notify when changes occur
	 * @author skyebend
	 * @param listener
	 */
	public void addTaskEventListener(Object listener);
	
}
