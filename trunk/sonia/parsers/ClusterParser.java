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
package sonia.parsers;

import java.util.Vector;

/**
 * Interfrace for parsers and data importers that can load and information
 * about clusters and groupings of nodes in the form of lists of NodeClusterAttributes
 * @author skyebend
 *
 */
public interface ClusterParser extends Parser {
	/**
	 * return list of NodeClusterAttributes defining groupings and hierarchy
	 * of nodes
	 * @author skyebend
	 * @return
	 */
	Vector getClusterList();
}
