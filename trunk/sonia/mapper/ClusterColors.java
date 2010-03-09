/* This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package sonia.mapper;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import sonia.LayoutSlice;
import sonia.NetDataStructure;

/**
 * maps node clusters into node colors
 * @author skyebend
 *
 */
public class ClusterColors extends Colormapper {

	private HashMap<Object, Color> colormap = new HashMap<Object, Color>();
	
	public void mapClustersToColors(){
		LayoutSlice slice = new LayoutSlice(0,0,0);
		//sslice.g
	}

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
