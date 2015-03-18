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
/**
 * this code was originally written for SoNIA by skyebend in 2006 
 */
package sonia.settings;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * This class is the parent for the properties objects used to store save and
 * view settings in sonia. Provides some common functionality
 * 
 * @author skyebend
 * 
 */
public abstract class PropertySettings extends Properties {
	
	public static final String SETTING_CLASS_CODE = "SettingsClass";

	/**
	 * overides toString() so that each key=value pair is printed on its own line
	 * with an indent
	 */
	public String toString() {
		String outString = SETTING_CLASS_CODE + "="
				+ getSettingsClassCode() + "\n";
		Iterator keyIter = this.keySet().iterator();
		while (keyIter.hasNext()) {
			String key = (String) keyIter.next();
			outString = outString + "\t" + key + "=" + getProperty(key) + "\n";
		}
		return outString;
	}
	
	public String getSettingsClassCode() {
		return getClass().getName();
	}
	
	/**
	 * Calls setProperty in super class, but first tries to parse value argument as a double
	 * this is included as fail-fast for property values. 
	 * @author skyebend
	 * @param key
	 * @param value
	 */
	public void setDoubleProperty(String key, String value){
		Double.parseDouble(value);
		setProperty(key,value);
	}
	

}
