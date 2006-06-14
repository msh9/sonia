package sonia;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Instead of reading a file as input, this class takes sets of arrays passed
 * in via the rJava calling methods from R
 * [1] "list(list(inl = 1, outl = 4, atl = list(na = FALSE)), list(inl = 1, outl = 5, atl = list(na = FALSE)), list(inl = 2, outl = 3, atl = list(na = FALSE)), list(inl = 2, outl = 4, atl = list(na = FALSE)), list(inl = 3, outl = 1, atl = list(na = FALSE)), list(inl = 3, outl = 4, atl = list(na = FALSE)), list(inl = 4, outl = 1, atl = list(na = FALSE)), list(inl = 4, outl = 2, atl = list(na = FALSE)), list(inl = 4, outl = 3, atl = list(na = FALSE)), list(inl = 4, outl = 5, atl = list(na = FALSE)), list(inl = 5, "
[2] "list(n = 5, mnext = 15, directed = TRUE, hyper = FALSE, loops = FALSE, multiple = FALSE, bipartite = FALSE)"                                                                                                                                                                                                                                                                                                                                                                                                                  
[3] "list(list(na = FALSE), list(na = FALSE), list(na = FALSE), list(na = FALSE), list(na = FALSE))"                                                                                                                                                                                                                                                                                                                                                                                                                               
[4] "list(c(2, 1), c(4, 3), c(6, 5), c(7, 8, 10, 9), c(11, 12, 13, 14))"                                                                                                                                                                                                                                                                                                                                                                                                                                                           
[5] "list(c(11, 7, 5), c(12, 8), c(3, 13, 9), c(1, 4, 14, 6), c(2, 10))"    
 * @author skyebend
 *
 */
public class RJavaParser implements Parser {
	
	  private Vector nodeList;
	  private Vector arcList;
	  private boolean isDirected = true;
	  private boolean isHyperGraph = false;
	  private boolean hasLoops = false;
	  private boolean isMultiplex = false;
	  private boolean isBipartie = false;
	  

	/**
	 * String is the output of the paste() command for a network object in R.  Will be
	 * a set of lists "mel" : Master Edge List, "gal" : Graph Attribute List,
	 * "val" : Vertex Attribute List,  "iel" : In Edge List,  "oel", Out Edge List.
	 */
	public void parseNetwork(String netString) throws IOException {
		StringTokenizer linekonizer = new StringTokenizer(netString,"\n"); 
		try {
			String mel = linekonizer.nextToken();
			String gal = linekonizer.nextToken();
			String val = linekonizer.nextToken();
			String iel = linekonizer.nextToken();
			String oel = linekonizer.nextToken();
			
			parseGal(gal);
			
		} catch (Exception e) {
			String error = "Unable to parse text representation of R network object: "+
			 e.toString();
			throw(new IOException(error));
		}

	}
	
	private void parseGal(String gal) throws IOException{
		// debug, throw exception so we can see it
		throw (new IOException(gal));
	}
	
	/**
	 * returns a vector with the elments at the first level of the passed list
	 * @param list
	 * @return  A vector of strings, or null if argument was not a list
	 */
	private Vector parseList(String list){
		//list(list(na = FALSE), list(na = FALSE), list(na = FALSE), list(na = FALSE))
		Vector listContents = new Vector();
		//check if it really is a list
		if (!list.startsWith("list(")) return null;
		//strip off the outside () wrappers
		list = list.substring(4,list.length() -1 );
		//items are delimted by ",", but not if inside ()
		
		int openIndex = list.indexOf("(");
		int openCount = 1;
		int closeIndex = -1;
		int closeCount = 0;
		//find the first (
		//count ( and ) until find matich )
		while (openCount > closeCount){
			
		}
		
		return listContents;
	}
	
	private void parseMel(String mel)
	{
		
	}

	public int getMaxNumNodes() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getNumNodeEvents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getNumArcEvents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Vector getNodeList() {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector getArcList() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNetInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getParserInfo() {
		// TODO Auto-generated method stub
		return null;
	}

}
