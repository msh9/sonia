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
package sonia.ui;

import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

import sonia.LongTask;
import sonia.TaskListener;

public class TaskProgressBar extends JPanel {

	private LongTask task;

	private JProgressBar progress;
	private JButton close;

	/**
	 * progressbar that acts as a renter for a task
	 * 
	 * @param task
	 */
	public TaskProgressBar(LongTask mytask) {
		super();
		setLayout(new GridLayout(1, 2));
		task = mytask;
		progress = new JProgressBar();
		progress.setStringPainted(true);
		if (!task.isDurationKnown()) {
			progress.setIndeterminate(true);
		}
		setBorder(new TitledBorder(task.getTaskName()));
		// ADD THIS AS A LISTENER TO FORCE REPANTS ON STATUS CHANGE?
		//task.addTaskEventListener(this);
		add(progress);
		
	}
	//overide paint to check status of task
	@Override
	public void paint(Graphics g) {
		if (task.isDone()){
			setVisible(false);
			getParent().remove(this);
		} else {
		progress.setString("tsk_"+task.hashCode()+":"+task.getStatusText());
		progress.setIndeterminate(true);

		progress.setIndeterminate(false);
		progress.setMaximum(task.maxSteps());
		progress.setMinimum(0);
		progress.setValue(task.currentStep());
		}
		/*
		if (task.isDone()) {
			progress.setString("<done> " + task.getStatusText());
			progress.setIndeterminate(false);
			progress.setMaximum(1);
			progress.setMinimum(0);
			progress.setValue(1);
			progress.setEnabled(false);
		}
		*/
		super.paint(g);
	}

	public LongTask getTask() {
		return task;
	}
	
	



}
