package sonia;

import java.util.Iterator;
import java.util.Properties;

/**
 * Stores specifications for sliceing and applying layouts as properties
 * object so can be saved, serialized and reloaded. 
 * @author skyebend
 *
 */
public class LayoutSettings extends Properties {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5025630643803845106L;
	
	public static final String LAYOUT_TYPE = "LAYOUT_TYPE";
	public static final String SLICE_START = "SLICE_START";
	public static final String SLICE_END = "SLICE_END";
	public static final String SLICE_DURATION = "SLICE_DURATION";
	public static final String SLICE_DELTA = "SLICE_DELTA";
	public static final String ANIMATE_TYPE = "ANIMATE_TYPE";
	public static final String SLICE_AGGREGATION = "SLICE_AGGREGATION";
	public static final String LAYOUT_SETTINGS = "LAYOUT_SETTINGS";
	
	/**
	 * overides to string so that each key=value pair is printed on its own line with an indent
	 */
	public String toString(){
		String outString = PropertyBuilder.SETTING_CLASS_CODE+"="+LayoutSettings.class.getName()+"\n";
		Iterator keyIter = this.keySet().iterator();
		while (keyIter.hasNext()){
			String key = (String)keyIter.next();
			outString = outString +"\t"+key+"="+getProperty(key)+"\n";
		}
		return outString;
	}
	
}
