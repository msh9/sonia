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

package sonia;

public interface MovieMaker {

	/**
	 * opens a file dialog asking where to save the movie, opens a "QuickTime session"
	 * throws up a new window with the same size as the layout, and plays the layout,
	 * recording each frame to the new window, and into the movie file.
	 */
	public abstract void setupMovie(SoniaCanvas canvas, int frames)
			throws Exception;

	/**
	 * Records the currently displayed image to the movie file.
	 */
	public abstract void captureImage();

	public abstract void finishMovie();

	public abstract boolean isExporting();

}