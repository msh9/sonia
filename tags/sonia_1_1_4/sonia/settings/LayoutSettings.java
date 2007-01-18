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
	
	
	//------------ values --
	
	public static final String COORD_ORIG =  "coordinates from original file";
	public static final String CIRCULAR = "circular layout";
	public static final String FR = "FR layout";
	public static final String RAND_FR = "random FR layout";
			
	public static final String RUB_FR = "Rubber-Band FR Layout";
	public static final String MULTI_KK = "MultiComp KK Layout";
	public static final String PI = "Moody PI layout";
	public static final String METRIC_MDS = "MetricMDS (SVD)?" ;

	public static final String NO_ANIMATION = "none";
	public static final String COSITE_ANIMATION = "cosine animation" ;

	public static final String NUM_TIES =  "Number  of i->j ties";
	public static final String AVG_TIES = "Avg of i->j ties";
	public static final String SUM_TIES = "Sum  of i->j ties" ;
	//TODO: should add a log weight aggregation option
	
	
	
}
