package sonia.parsers;

import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.Color;
import java.awt.geom.*;

import sonia.ArcAttribute;
import sonia.NodeAttribute;
import sonia.settings.PropertySettings;
import sonia.ui.AttributeMapperDialog;

/**
 * <p>Title:SoNIA (Social Network Image Animator) </p>
 * <p>Description:Animates layouts of time-based networks
 * <p>Copyright: CopyLeft  2004: GNU GPL</p>
 * <p>Company: none</p>
 * @author Skye Bender-deMoll unascribed
 * @version 1.1
 */

/**
 * Parser for reading XML files in the GraphML format. http://graphml.graphdrawing.org/primer/graphml-primer.html Should be able to 
 * read fairly genearic graphML files, but at the moment only reads those
 * that have been exported by sonia.
 */
public class GraphMLParser implements Parser{
	
	private Vector nodeList;

	private Vector arcList;
	
	private int maxNodes = 0;
	
	private String parseInfo ="";
	
	

	public void configureParser(PropertySettings settings) {
		// TODO Auto-generated method stub
		
	}

	public Vector getArcList() {
		return arcList;
	}

	public int getMaxNumNodes() {
		return maxNodes;
	}

	public String getNetInfo() {
		return parseInfo;
	}

	public Vector getNodeList() {
		return nodeList;
	}

	public int getNumArcEvents() {
		return arcList.size();
	}

	public int getNumNodeEvents() {
		return nodeList.size();
	}

	public String getParserInfo() {
		return "GraphML XML parser";
	}

	public void parseNetwork(String fileAndPath) throws IOException {
		
		
	}
	
	
}