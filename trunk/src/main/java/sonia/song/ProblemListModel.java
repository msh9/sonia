package sonia.song;

import java.util.ArrayList;

import javax.swing.AbstractListModel;

public class ProblemListModel extends AbstractListModel {
	
	private ArrayList<DataProblem> probs;
	
	public ProblemListModel(ArrayList<DataProblem> problems){
		probs = problems;
	}

	public Object getElementAt(int arg0) {
		if (probs == null){
			return null;
		}
		return probs.get(arg0);
	}

	public int getSize() {
		if (probs == null){
			return 0;
		}
		return probs.size();
	}

	/**
	 * tells the list that the underlying data has been modified and it needs to recompute
	 *
	 */
	public void refresh(){
		fireContentsChanged(this, 0, probs.size());
	}
}
