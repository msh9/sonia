package sonia.song;

import java.util.ArrayList;

import javax.swing.AbstractListModel;

import sonia.song.filters.CleaningFilter;

public class FilterListModel extends AbstractListModel {
	
	private ArrayList<CleaningFilter> filters;
	
	public FilterListModel(ArrayList<CleaningFilter> filters){
		this.filters = filters;
	}

	public Object getElementAt(int arg0) {
		if (filters == null){
			return null;
		}
		return filters.get(arg0);
	}

	public int getSize() {
		if (filters == null){
			return 0;
		}
		return filters.size();
	}

	/**
	 * tells the list that the underlying data has been modified and it needs to recompute
	 *
	 */
	public void refresh(){
		fireContentsChanged(this, 0, filters.size());
	}
}
