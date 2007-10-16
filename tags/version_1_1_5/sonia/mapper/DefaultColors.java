package sonia.mapper;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Creates an arbitrary mapping of objects to colors, with colors distributed through the color space
 * @author skyebend
 *
 */
public class DefaultColors extends Colormapper {
	
	private HashMap<Object, Color> colormap = new HashMap<Object, Color>();;

	public void createMapping(Set<Object> values) {
		colormap.clear();
		//figure out how many colors
		float hue = 0f;
		Iterator<Object> valiter = values.iterator();
		while (valiter.hasNext()){
			
			Object value = valiter.next();
			Color col = Color.getHSBColor(hue, 1.0f, 1f);
			colormap.put(value, col);
			hue += 1f/values.size();
			
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

}
