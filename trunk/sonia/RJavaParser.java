package sonia;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
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
	  private boolean isBipartite = false;
	  private HashMap galTagMap;
	  int maxNodes = 0;
	  int numEdges = 0;
	  

	/**
	 * String is the output of the paste() command for a network object in R.  Will be
	 * a set of lists "mel" : Master Edge List, "gal" : Graph Attribute List,
	 * "val" : Vertex Attribute List,  "iel" : In Edge List,  "oel", Out Edge List.
	 */
	public void parseNetwork(String netString) throws IOException {
		try {
			Vector mainElements = parseList(netString);
			String mel = (String)mainElements.get(0);
			String gal = (String)mainElements.get(1);
			String val = (String)mainElements.get(2);
			String iel = (String)mainElements.get(3);
			String oel = (String)mainElements.get(4);
			
			parseGal(gal);

			
		} catch (Exception e) {
			String error = "Unable to parse text representation of R network object: "+
			 e.toString();
			throw(new IOException(error));
		}

	}
	
	/**
	 * puts the elements of gal into map by variable names
	 *  gal = list(n = 10, mnext = 46, directed = TRUE,     hyper = FALSE, loops = FALS
E, multiple = FALSE, bipartite = FALSE)
	 * @param gal
	 * @throws Exception
	 */
	private void parseGal(String gal) throws Exception{
		galTagMap = new HashMap();
		//should start with "gal = list(..."
		if (!gal.startsWith("gal = ")){
			throw new Exception("Graph Attribute List does not start with 'gal = '");
		}
		Vector attributeTokens = parseList(gal.substring(6));
		//put all the name = value pairs into a map
		for (int i = 0; i < attributeTokens.size(); i++) {
			String tag = (String)attributeTokens.get(i);
			galTagMap.put(tag.substring(0, tag.indexOf("=")-1).trim(), tag
					.substring(tag.indexOf("=")+1).trim());
		}
		maxNodes = Integer.parseInt((String)galTagMap.get("n"));
		numEdges =Integer.parseInt((String)galTagMap.get("mnext"))-1;
		isDirected = parseRBoolIsTrue((String)galTagMap.get("directed"));
		isHyperGraph = parseRBoolIsTrue((String)galTagMap.get("hyper"));
		hasLoops = parseRBoolIsTrue((String)galTagMap.get("loops"));
		isMultiplex = parseRBoolIsTrue((String)galTagMap.get("multiple"));
		isBipartite = parseRBoolIsTrue((String)galTagMap.get("bipartite"));
		
	}
	
	/*
	 * takes an R-style boolean (T TRUE F FALSE) and evaluates it 
	 */
	private boolean parseRBoolIsTrue(String bool)
	{
		return (bool.equals("T") | bool.equals("TRUE"));
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
		list = list.substring(5,list.length() -1 );
		//items are delimted by ",", but not if inside ()
		//so scan accross the string, breaking at each "," but ignore if open
		int chunkStart = 0;
		int chunkEnd = 0;
		int openParen = 0;
		
		//find the first (
		//count ( and ) until find matich )
		while (chunkEnd < list.length()){
			String checkChar = list.substring(chunkEnd,chunkEnd+1);
			if (checkChar.equals(",")){
				if (openParen == 0){  //ignore the comma if it is inside parens
				  listContents.add(list.substring(chunkStart,chunkEnd).trim());
				  chunkStart = chunkEnd+1;  //need to remove the comma?
				}
			} else if (checkChar.equals("(") || checkChar.equals("[")){
				openParen ++;
			}
			else if (checkChar.equals(")") || checkChar.equals("]")){
				openParen --;

			}
			chunkEnd ++;
		}
		listContents.add(list.substring(chunkStart,chunkEnd).trim());
		
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
