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

/**
 * this class holds the settings that control how variables read from the 
 * r datastructure by the RJavaParser should be mapped to vertex properties
 * 
 * @author skyebend
 * 
 */
public class RParserSettings extends PropertySettings {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2741177210767534091L;

	public static final String NODE_LABEL = "NODE_LABEL";

	public static final String NODE_COLOR = "NODE_COLOR";

	public static final String NODE_SIZE = "NODE_SIZE";
	
	public static final String NODE_SHAPE = "NODE_SHAPE";
	
	public static final String NODE_X = "NODE_X";
	
	public static final String NODE_Y = "NODE_Y";

}
