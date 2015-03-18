package sonia.song.filters;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import sonia.song.DataProblem;

public abstract class AbstractFilter implements CleaningFilter {
	
	protected boolean active = true;
	protected String status = "";
	protected Hashtable<String, String> properties = null;

    public Hashtable<String,String> getProperties(){
    	return properties;
    }
    
    public void setProperty(String key, String value){
    	if (properties == null){
    		properties = new Hashtable<String, String>();
    	}
    	properties.put(key, value);
    }

	public boolean isActive() {
		return active;
	}


	public void setActive(boolean state) {
		active = state;

	}
	
	/**
	 * removes all rows linked from problems with the matching problem type
	 * @param type
	 */
	protected void removeProblemRows(String type,ArrayList<String[]> data,
			ArrayList<DataProblem> problems ){
		Iterator<DataProblem> probIter = problems.iterator();
		while (probIter.hasNext()){
			DataProblem prob = probIter.next();
			//check the type of the problem
			if (type == null || prob.getType().equals(type)){
				//remove the row with the problem
				data.remove(prob.getRef());
			}
		}
		
	}

	public String getStatus() {
		return status;
	}
	
	public String toString(){
		return getName();
	}

}
