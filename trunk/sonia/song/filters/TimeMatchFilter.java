package sonia.song.filters;

import java.util.ArrayList;
import java.util.Iterator;

import sonia.parsers.DotSonColumnMap;
import sonia.song.DataProblem;
import sonia.song.Getter;

/**
 * This filter can be used to delete arcs that link node ids where one or more of the 
 * nodes do not have active node attributes for the duration of the arc.
 * @author skyebend
 *
 */
public class TimeMatchFilter extends AbstractFilter implements CleaningFilter {
	private Getter song;
	
	public static final String TO_NOT_MATCH = "TO_NODE_SPELL_DOES_NOT_ENCLOSE_ARC";
	public static final String FROM_NOT_MATCH = "FROM_NODE_SPELL_DOES_NOT_ENCLOSE_ARC";
	
	public TimeMatchFilter(Getter song){
		super();
		this.song = song;
	}
	
	public String getDescription() {
		// TODO Auto-generated method stub
		return "This filter can be used to delete arcs that link node ids where one or more of the  nodes do not have active node attributes for the duration of the arc.";
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Remove arcs with non-matching node times";
	}

	public boolean likesArcs() {
		return true;
	}

	public boolean likesNodes() {
		return false;
	}

	public void process(ArrayList<String[]> data,
			ArrayList<DataProblem> problems, boolean dataAreArcs) {
		
		if (!dataAreArcs){
			return; //don't do anything
		}
		int numProbs = 0;
		
		//find out what cols the times are in 
		int arcStartCol = song.getArcHeaders().indexOf("StartTime");
		int arcEndCol = song.getArcHeaders().indexOf("EndTime");
		int arcFromCol = song.getArcHeaders().indexOf("FromId");
		int arcToCol = song.getArcHeaders().indexOf("ToId");
		int nodeIdCol = song.getNodeHeaders().indexOf("AlphaId");
		int nodeStartCol  = song.getNodeHeaders().indexOf("StartTime");
		int nodeEndCol  = song.getNodeHeaders().indexOf("EndTime");
		
		//loop over each arc row, checking the times
//		ineficient hack, but will have to work for now
		Iterator<String[]> arcRowIter = data.iterator();
		int arcRowCount = 0;
		while (arcRowIter.hasNext()){
			String[] arc = arcRowIter.next();
			boolean fromOk = false;
			boolean toOk = false;
			try{
//				now loop over every single freeking node row to check ids for the FROM node
				Iterator<String[]> nodeRowIter = song.getNodeData().iterator();
				while (nodeRowIter.hasNext() & !fromOk){
					String[] node = nodeRowIter.next();
					//ids match
					if (node[nodeIdCol].equals(arc[arcFromCol])){ 
						//arc start lands within nodes's spell
						if (Double.parseDouble(node[nodeStartCol]) <= Double.parseDouble(arc[arcStartCol]) &
								Double.parseDouble(node[nodeEndCol]) >= Double.parseDouble(arc[arcStartCol])){ 
//						arc end lands within nodes's spell
							if (Double.parseDouble(node[nodeStartCol]) <= Double.parseDouble(arc[arcEndCol]) &
									Double.parseDouble(node[nodeEndCol]) >= Double.parseDouble(arc[arcEndCol])){ 	
							  //yea! it matches, stop looping
								fromOk = true;
							}
						
						}
					}
				}
				//if the from node is not ok, no point in going further we now it is a problem 
				if (!fromOk){
					DataProblem prob = new DataProblem(DataProblem.ARCS,FROM_NOT_MATCH,
							"The activity spell for the arc did not match with a vaild time spell of its FROM node",arcRowCount,
							"");
					prob.setRef(arc);
					problems.add(prob);
					numProbs++;
//					debug 
					//System.out.println(arc[arcFromCol]+"\t"+arc[arcToCol]+"\t"+arc[arcStartCol]+"\t"+arc[arcEndCol]);
				} else {
					//	now loop over every single freeking node row again to check ids for the TO node
					nodeRowIter = song.getNodeData().iterator();
					while (nodeRowIter.hasNext() & !toOk){
						String[] node = nodeRowIter.next();
						//ids match
						if (node[nodeIdCol].equals(arc[arcToCol])){ 
							//arc start lands within nodes's spell
							if (Double.parseDouble(node[nodeStartCol]) <= Double.parseDouble(arc[arcStartCol]) &
									Double.parseDouble(node[nodeEndCol]) >= Double.parseDouble(arc[arcStartCol])){ 
	//							arc end lands within nodes's spell
								if (Double.parseDouble(node[nodeStartCol]) <= Double.parseDouble(arc[arcEndCol]) &
										Double.parseDouble(node[nodeEndCol]) >= Double.parseDouble(arc[arcEndCol])){ 	
								  //yea! it matches, stop looping
									toOk = true;
								}
							
							}
						}
					}
					if (!toOk){
						DataProblem prob = new DataProblem(DataProblem.ARCS,TO_NOT_MATCH,
								"The activity spell for the arc did not match with a vaild time spell of its TO node",arcRowCount,
								"");
						prob.setRef(arc);
						problems.add(prob);
						//debug 
						//System.out.println(arc[arcFromCol]+"\t"+arc[arcToCol]+"\t"+arc[arcStartCol]+"\t"+arc[arcEndCol]);
						numProbs++;
					}
				}
					

			} catch (NumberFormatException e) {
				song.status("Filter failed, error parsing number in filter: "+e);
				return;
			}
			arcRowCount++;
		}
		
		//now that we've found all the problems, go ahead and delete'em
		removeProblemRows(TO_NOT_MATCH, data, problems);
		removeProblemRows(FROM_NOT_MATCH, data, problems);
		song.status("Deleted "+numProbs+" arc rows with activity spells that are not enclosed by their nodes");
		
	}

}
