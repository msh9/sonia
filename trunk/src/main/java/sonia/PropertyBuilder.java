package sonia;

import java.util.Properties;
import java.util.StringTokenizer;

import sonia.parsers.DotSonColumnMap;
import sonia.settings.ApplySettings;
import sonia.settings.BrowsingSettings;
import sonia.settings.ColormapperSettings;
import sonia.settings.GraphicsSettings;
import sonia.settings.LayoutSettings;
import sonia.settings.MovieSettings;
import sonia.settings.PropertySettings;
import sonia.settings.RParserSettings;

/**
 * parses a chuck of text with blocks containing key value associations for 
 * various  sonia property settings objects.  Returns those objects with
 * values set as request. 
 * @author skyebend
 *
 */
public class PropertyBuilder {
	
	
	
    private String[] settingsBlocks;
    
	public PropertyBuilder(String compoundProperties){
		// parse the string into blocks to find the class headers later
		 settingsBlocks  = compoundProperties.split(PropertySettings.SETTING_CLASS_CODE+"=");
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
	 * @return an LayoutSettings object with values initialized, or null
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
	
	/**
	 * looks for an ColormapperSettings block in the compoundProperties string,
	 * initializes and returns it, or null if none found
	 * 
	 * @return a ColormapperSettings object with values initialized, or null
	 */
	public ColormapperSettings getColormapperSettings(){
		// look through the blocks for one with a matching header
		ColormapperSettings settings = null;
		for (int i = 0; i < settingsBlocks.length; i++) {
			if (settingsBlocks[i].trim().startsWith(ColormapperSettings.class.getName())){
				
				settings = (ColormapperSettings)addProps(new ColormapperSettings(),settingsBlocks[i]);
				break;
			}
			
		}
		return settings;
	}
	
	/**
	 * looks for an GraphicsSettings block in the compoundProperties string,
	 * initializes and returns it, or null if none foud
	 * 
	 * @return an GraphicsSettings object with values initialized, or null
	 */
	public GraphicsSettings getGraphicsSettings(){
		// look through the blocks for one with a matching header
		GraphicsSettings settings = null;
		for (int i = 0; i < settingsBlocks.length; i++) {
			if (settingsBlocks[i].trim().startsWith(GraphicsSettings.class.getName())){
				
				settings = (GraphicsSettings)addProps(new GraphicsSettings(),settingsBlocks[i]);
				break;
			}
			
		}
		return settings;
	}
	
	/**
	 * looks for an BrowsingSettings block in the compoundProperties string,
	 * initializes and returns it, or null if none foud
	 * 
	 * @return an BrowsingSettings object with values initialized, or null
	 */
	public BrowsingSettings getBrowsingSettings(){
		// look through the blocks for one with a matching header
		BrowsingSettings settings = null;
		for (int i = 0; i < settingsBlocks.length; i++) {
			if (settingsBlocks[i].trim().startsWith(BrowsingSettings.class.getName())){
				
				settings = (BrowsingSettings)addProps(new BrowsingSettings(),settingsBlocks[i]);
				break;
			}
			
		}
		return settings;
	}
	
	/**
	 * looks for an PropertySettings block in the compoundProperties string corresponding
	 * to the class MovieSettings initializes and returns it, or null if none found
	 * 
	 * @return an PropertySettings object with values initialized, or null
	 */
	public MovieSettings getMovieSettings(){
//		 look through the blocks for one with a matching header
		MovieSettings settings = null;
		for (int i = 0; i < settingsBlocks.length; i++) {
			if (settingsBlocks[i].trim().startsWith(MovieSettings.class.getName())){
				settings = (MovieSettings)addProps(new MovieSettings(),settingsBlocks[i]);
				break;
			}
		}
		return settings;
	}
	
	/**
	 * looks for an PropertySettings block in the compoundProperties string corresponding
	 * to one of the classes of parser settings (DotSonColumnMap, RJavaParserSettigns, etc)
	 * initializes and returns it, or null if none foud
	 * 
	 * @return an PropertySettings object with values initialized, or null
	 */
	public PropertySettings getParserSettings(){
		// look through the blocks for one with a matching header
		PropertySettings settings = null;
		for (int i = 0; i < settingsBlocks.length; i++) {
			if (settingsBlocks[i].trim().startsWith(DotSonColumnMap.class.getName())){
				
				settings = (DotSonColumnMap)addProps(new DotSonColumnMap(),settingsBlocks[i]);
				break;
			}
			else if (settingsBlocks[i].trim().startsWith(RParserSettings.class.getName())){
				
				settings = (RParserSettings)addProps(new RParserSettings(),settingsBlocks[i]);
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
