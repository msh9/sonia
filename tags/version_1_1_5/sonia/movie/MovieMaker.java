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

package sonia.movie;

import sonia.SoniaCanvas;
import sonia.settings.MovieSettings;

public interface MovieMaker {

	/**
	 * Asks the movie to do any necessary setup, open a file on disk, etc
	 */
	public abstract void setupMovie(SoniaCanvas canvas, int frames)
			throws Exception;

	/**
	 * Records the current slice of network to the movie file.
	 */
	public abstract void captureImage();

	/**
	 * Asks the movie to do any cleanup, flush buffers, and close file
	 * @author skyebend
	 */
	public abstract void finishMovie();

	public abstract boolean isExporting();
	
	/**
	 * Asks the movie to read the settings and configure appropriately
	 * @author skyebend
	 * @param settings
	 */
	public abstract void configure(MovieSettings settings);

}