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
 * this class holds the settings that control the view of the network and the
 * animation, render slice offset, interpolation speed, and eventually zoom,
 * etc.
 * 
 * @author skyebend
 * 
 */
public class BrowsingSettings extends PropertySettings {
	
	/**
	 * key for duration of render slice, value is double
	 */
	public static final String RENDER_DURATION = "render duration";
	/**
	 * key for ofset of render slice, value is float, 0 means back of slice, 1 means front
	 */
	public static final String RENDER_OFFSET = "render offset";
	
	/**
	 * key for number of frames to be interpolated when transitioning between slices, value is int
	 */
	public static final String INTERP_FRAMES = "num interpolated frames";
	
	/**
	 * key for delay between frame in interpolation (allso controls movie) value is double
	 */
	public static final String FRAME_DELAY = "frame delay";

}
