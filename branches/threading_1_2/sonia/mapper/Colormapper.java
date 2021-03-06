package sonia.mapper;

import java.awt.Color;
import java.util.Set;
import java.util.Vector;

public abstract class Colormapper extends Object {
	
	protected Object attributeKey = null;
	/**
	 * get the color this value is mapped to 
	 * @param value
	 * @return
	 */
	public abstract Color getColorFor(Object value);
	
	/**
	 * sets the mapping for a specific value
	 * @param value
	 * @param color
	 */
	public abstract void mapColor(Object value, Color color);
	
	/**
	 * takes a set of objects and creates a default mapping of values to colors
	 * @param values
	 */
	public abstract void createMapping(Set<Object> values);
	
	/**
	 * returns the set of values that have mappings.  may be an empty set if the mapper uses
	 * some function of the data to compute the color.  
	 * @return
	 */
	public abstract Set getValues();
	
	/**
	 * sets the key to be used when looking up data from node attributs
	 * @param key
	 */
	public void setKey(Object key){
		attributeKey = key;
	}
	/**
	 * gets the object used as a key for looking up data elements of node attributes that are mapped to colors
	 * @return
	 */
	public Object getKey(){
		return attributeKey;
	}
	
}
