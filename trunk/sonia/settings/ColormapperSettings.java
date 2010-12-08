package sonia.settings;

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
 * Stores settings to map specific values of node user data to Colors. 
 * Probably will cause problems if data is not the same when reloaded.
 * All of the properties other than those named with keys will be data values
 */
public class ColormapperSettings extends PropertySettings {

	

	/**
	 * 
	 */
	private static final long serialVersionUID = 2556263959644778150L;
	
	/**
	 * key for mapper short name, indicates which class to create
	 */
	public static final String MAPPER_NAME = "MAPPER_NAME";
	
	/**
	 * Key for storing the name of the user data key for the data the mapper will be coloring
	 */
	public static final String DATA_KEY = "DATA_KEY";



}
