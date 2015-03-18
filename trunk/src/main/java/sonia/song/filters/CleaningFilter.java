package sonia.song.filters;

import java.util.ArrayList;
import java.util.Hashtable;

import sonia.song.DataProblem;

/**
 * Interface for filters that will clean and modify input data, possibly deleting rows
 * @author skyebend
 *
 */
public interface CleaningFilter {
	/**
	 * problems may be null
	 * @param data
	 */
	public void process(ArrayList<String[]> data, ArrayList<DataProblem> problems, boolean dataAreArcs);
	public String getName();
	public String getDescription();
	public boolean isActive();
	public void setActive(boolean state);
	public Hashtable<String, String> getProperties();
	public void setProperty(String key, String value);
	public String getStatus();
	public boolean likesArcs();
	public boolean likesNodes();
}
