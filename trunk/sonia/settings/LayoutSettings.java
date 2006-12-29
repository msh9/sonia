package sonia.settings;



/**
 * Stores specifications for sliceing and applying layouts as properties
 * object so can be saved, serialized and reloaded. 
 * @author skyebend
 *
 */
public class LayoutSettings extends PropertySettings {

	
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
	/**
	 * Names of additional parameters for certain algorithms
	 */
	public static final String LAYOUT_SETTINGS = "LAYOUT_SETTINGS";
	
	
	
}
