package sonia.mapper;

import java.awt.Color;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Scanner;

import sonia.settings.ColormapperSettings;
import sonia.settings.PropertySettings;

/**
 * Creates, saves, and recreates color and shape mappers of the appropriate
 * classes
 * 
 * @author skyebend
 * 
 */
public class MapperFactory {

	/**
	 * All color mapper classes need to be listed here to show up in the UI
	 */
	public static String[] knownMappers = new String[] {
			DefaultColors.MAPPER_NAME, GrayscaleColors.MAPPER_NAME,
			RedtoBlueColors.MAPPER_NAME };

	/**
	 * Factory method to create and store the mapper appropriate for the string
	 */
	public static Colormapper getMapperFor(String mapperName) {
		Colormapper mapper = null;
		if (mapperName.equals(DefaultColors.MAPPER_NAME)) {
			mapper = new DefaultColors();
		} else if (mapperName.equals(GrayscaleColors.MAPPER_NAME)) {
			mapper = new GrayscaleColors();
		} else if (mapperName.equals(RedtoBlueColors.MAPPER_NAME)) {
			mapper = new RedtoBlueColors();
		}
		return mapper;
	}

	/**
	 * convert mapper to a properties object so it can be saved
	 * 
	 * @param mapper
	 * @return
	 */
	public static ColormapperSettings asProperties(Colormapper mapper) {
		ColormapperSettings props = new ColormapperSettings();
		props.setProperty(ColormapperSettings.MAPPER_NAME, mapper
				.getMapperName());
		props.setProperty(ColormapperSettings.DATA_KEY, mapper.getKey()
				.toString());
		Iterator valsiter = mapper.getValues().iterator();
		while (valsiter.hasNext()) {
			Object value = valsiter.next();
			if (value != null) {
				Color c = mapper.getColorFor(value);
				props.setProperty(value.toString(), "rgb(" + c.getRed() + ","
						+ c.getGreen() + "," + c.getBlue() + ")");
			}
		}
		return props;
	}

	/**
	 * recreate a colormapper based on a properties object
	 * 
	 * @param props
	 * @return
	 */
	public static Colormapper restoreMapperFrom(ColormapperSettings props) {
		String mapName = props.getProperty(ColormapperSettings.MAPPER_NAME);
		String dataKey = props.getProperty(ColormapperSettings.DATA_KEY);
		Colormapper mapper = getMapperFor(mapName);
		mapper.setKey(dataKey);
		HashSet<String> ignoreKeys = new HashSet<String>(Arrays
				.asList(new String[] { ColormapperSettings.MAPPER_NAME,
						ColormapperSettings.DATA_KEY,PropertySettings.SETTING_CLASS_CODE }));
		Enumeration<Object> keys = props.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			if (!ignoreKeys.contains(key)) {
				//should look like rgb(51,255,51) 
				//drop first 4 chars and use use "non-digits as delimeter"
				Scanner intfinder = new Scanner(props.getProperty(key).substring(3)).useDelimiter("\\D");
				int r = intfinder.nextInt();
				int g = intfinder.nextInt();
				int b = intfinder.nextInt();
				Color c = new Color(r, g, b);
				mapper.mapColor(key, c);
			}
		}
		return mapper;
	}

}
