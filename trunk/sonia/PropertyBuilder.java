package sonia;

import java.util.Properties;
import java.util.StringTokenizer;

public class PropertyBuilder {
	
	public static final String SETTING_CLASS_CODE = "SettingsClass";
	
    private String[] settingsBlocks;
    
	public PropertyBuilder(String compoundProperties){
		// parse the string into blocks to find the class headers later
		 settingsBlocks  = compoundProperties.split(SETTING_CLASS_CODE+"=");
	}
	
	
	/**
	 * looks for an apply settings block in the compoundProperties string,
	 * initializes and returns it, or null if none foud
	 * 
	 * @return an ApplySettings object with values initialized, or null
	 */
	public ApplySettings getApplySettings(){
		// look through the blocks for one with a matching header
		ApplySettings settings = null;
		for (int i = 0; i < settingsBlocks.length; i++) {
			if (settingsBlocks[i].startsWith(ApplySettings.class.getName())){
				settings = (ApplySettings)addProps(new ApplySettings(),settingsBlocks[i]);
				break;
			}
			
		}
		return settings;
	}
	
	/**
	 * looks for an LayoutSettings block in the compoundProperties string,
	 * initializes and returns it, or null if none foud
	 * 
	 * @return an ApplySettings object with values initialized, or null
	 */
	public LayoutSettings getLayoutSettings(){
		// look through the blocks for one with a matching header
		LayoutSettings settings = null;
		for (int i = 0; i < settingsBlocks.length; i++) {
			if (settingsBlocks[i].trim().startsWith(LayoutSettings.class.getName())){
				
				settings = (LayoutSettings)addProps(new LayoutSettings(),settingsBlocks[i]);
				break;
			}
			
		}
		return settings;
	}
	
	private Properties addProps(Properties settings, String values){
		// tokenize by newline
		StringTokenizer propIter = new StringTokenizer(values,"\n");
		// skip the first line because it is the class name
		propIter.nextToken();
		while (propIter.hasMoreTokens()){
			String[] keyValue = propIter.nextToken().split("=");
			settings.put(keyValue[0].trim(),keyValue[1].trim());
		}
		return settings;
	}

}
