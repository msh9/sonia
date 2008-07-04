package sonia.song.filters;

import java.util.ArrayList;

import sonia.song.DataProblem;

public class AllProblemFilter extends AbstractFilter{

	
	public void process(ArrayList<String[]> data, ArrayList<DataProblem> problems, boolean dataAreArcs) {
		//remove all rows, ignore the type
		removeProblemRows(null, data, problems);
		
	}

	public String getDescription() {
		return "Removes all rows with problems, possibly creating new problems";
	}
	
	public String getName() {
		return "Remove all problem rows";
	}
	
	public boolean likesArcs(){
		return true;
	}
	
	public boolean likesNodes(){
		return true;
	}

}
