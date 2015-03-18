package sonia.mapper;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Creates an arbitrary mapping of objects to colors, with colors distributed
 * From red to blue
 * 
 * @author skyebend
 * 
 */
public class BlueToRedColors extends Colormapper {

	public static String MAPPER_NAME = "Blue to Red (Sorted)";

	private HashMap<Object, Color> colormap = new HashMap<Object, Color>();;

	public void createMapping(Set<Object> values) {
		TreeSet<Object> sortedValues = new TreeSet<Object>(values) {
		};
		colormap.clear();
		// figure out how many colors
		Iterator<Object> valiter = sortedValues.iterator();
		float step = 0.0f;
		Color startColor = Color.blue;
		Color endColor = Color.red;
		while (valiter.hasNext()) {
			Object value = valiter.next();
			float ratio = step / (float) (sortedValues.size()-1);
			int red = (int) (endColor.getRed() * ratio + startColor.getRed() * (1 - ratio));
			int green = (int) (endColor.getGreen() * ratio + startColor.getGreen()
					* (1 - ratio));
			int blue = (int) (endColor.getBlue() * ratio + startColor.getBlue() * (1 - ratio));
			Color newColor = new Color(red, green, blue);
			colormap.put(value, newColor);
			step += 1.0f;
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
