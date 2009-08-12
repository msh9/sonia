package sonia.parsers;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import sonia.ArcAttribute;
import sonia.NodeAttribute;
import sonia.settings.PropertySettings;

public class BotDumpParser implements Parser {
	
	private LineNumberReader reader;
	
	private Vector nodeList;

	private Vector arcList;
	
	private HashMap<String, Integer> ipIdMap;
	
	private String originalFile; // path and name of the file it was loaded from

	private String infoString = " test of BotNet parser";
	

	public void configureParser(PropertySettings settings) {

	}

	public Vector getArcList() {
		return arcList;
	}

	public int getMaxNumNodes() {

		return ipIdMap.size();
	}

	public String getNetInfo() {
		return originalFile;
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
		return infoString;
	}

	public void parseNetwork(String fileAndPath) throws IOException {
		originalFile = fileAndPath;

		nodeList = new Vector();
		arcList = new Vector();
		ipIdMap = new HashMap<String,Integer>();
//		 open connection to file
		reader = new LineNumberReader(new FileReader(fileAndPath));
		String line = reader.readLine();
		while (line != null){
			//we are looking for blocks that start with "Peer:"
			if (line.startsWith("Peer: ")){
				//read the peer ip
				String ip = line.substring(5, line.length());
				//go to next line and read the time
				line = reader.readLine();
				double time = Double.parseDouble(line.substring(5,line.length()));
				//create the node
				createNode(ip,time,originalFile+" line "+reader.getLineNumber());
				//skip Version:
				line = reader.readLine();
				//skip Status:   TODO: should this turn off the node?
				line = reader.readLine();
				//read the CurrentPeers:
				line = reader.readLine();
				//this line is either a list of peers, or something we don't want
//			split out the list of ips
				String[] peers = (line.substring(13,line.length())).split(",");
				//loop over peers, creating nodes and edges
				for (int i = 0; i < peers.length; i++) {
					String peer = peers[i];
					//TODO; should check if there is already a node with this time value
					createNode(peer,time,originalFile+" line "+reader.getLineNumber());
					//create the arc
					createArc(ip,peer,time);
				}
				
			}
			line = reader.readLine();
		}

		//debug
		System.out.println("parsed "+nodeList.size()+" node events and "+arcList.size()+" arc events describing "+ipIdMap.size()+" ip addresses.");
	}
	
	private void createArc(String source, String target, double time){
		ArcAttribute arc = new ArcAttribute(time,time,ipIdMap.get(source).intValue(),ipIdMap.get(target).intValue(),1,0);
		arcList.add(arc);
	}
	
	private void createNode(String ip, double time,String fileLoc){
		//TODO: strip port number of ip?
		if (!ipIdMap.containsKey(ip)){
			ipIdMap.put(ip, Integer.valueOf(ipIdMap.size()+1));
		} 
		NodeAttribute node = new NodeAttribute(ipIdMap.get(ip).intValue(),ip,0,0,time,time,fileLoc);
		nodeList.add(node);
	}

}
