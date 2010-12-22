package sonia.parsers;

import java.util.HashSet;

/**
 * Interface for parsers that are able to recognize and supply nodes that will have user data attached to them. 
 * Includes the method to let the data structures know what keys are used to store the data.
 * @author skyebend
 *
 */
public interface NodeDataParser {

	public HashSet<String> getNodeDataKeys();
	
}
