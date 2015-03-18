package sonia.mapper;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Creates an arbitrary mapping of objects to colors, with colors distributed through the white to black color space
 * @author skyebend
 *
 */
public class GrayscaleColors extends Colormapper {
	
	public static String MAPPER_NAME = "Grayscale (Sorted)";
	
	private HashMap<Object, Color> colormap = new HashMap<Object, Color>();;

	public void createMapping(Set<Object> values) {
		//TODO: need to sort values into some kind of order
		TreeSet<Object> sortedValues = new TreeSet<Object>(values) {
		};
		colormap.clear();
		//figure out how many colors
		float brightness = 0f;
		Iterator<Object> valiter = sortedValues.iterator();
		while (valiter.hasNext()){
			
			Object value = valiter.next();
			Color col = Color.getHSBColor(0.0f, 0.0f, brightness);
			colormap.put(value, col);
			brightness += 1f/values.size();
			
		}
		

	}

	public Color getColorFor(Object value) {
		return colormap.get(value);
	}

	public void mapColor(Object value, Color color) {
		colormap.put(value, color);

	}

	public Set getValues() {
		return colormap.keySet();
	}

	@Override
	public String getMapperName() {
		return MAPPER_NAME;
	}

}
