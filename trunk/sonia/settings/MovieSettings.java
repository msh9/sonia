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
package sonia.settings;


/**
 * holds properties for specifying location and properties of the exported movie
 * @author skyebend
 *
 */
public class MovieSettings extends PropertySettings {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3443808061066683501L;
	/**
	 * key for value giving path (including file name) where movie should be saved. 
	 */
	public static final String OUTPUT_PATH = "OUTPUT_PATH";
	
	/**
	 * key for the file type to be used for movie export
	 */
	public static final String FILE_TYPE = "FILE_TYPE";
	
	/**
	 * key for string containing names of additional format specific paramters
	 */
	public static final String FORMAT_PARAMS = "FORMAT_PARAMS";
	
	/**
	 * file type value indicating a QuickTime movie.  
	 * Note: only works on macs and pcs with quicktime installed
	 */
	public static final String QUICKTIME_FILE_TYPE = "mov";
	
	/**
	 * file type value indicating a flash movie. 
	 */
	public static final String FLASH_FILE_TYPE = "swf";
	
	

}
