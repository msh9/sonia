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

import java.util.Comparator;

/**
 * class used for matching indexes and priorities in a priority queue
 * 
 * @author skyebend
 * 
 */
public class PriorityIntTuple implements Comparable{

	public double priority;

	public int index;


	public PriorityIntTuple(double priority, int index) {
		
		this.priority = priority;
		this.index = index;
	}

	/**
	 * returns a compartor that orders by priority. But returns equal only if
	 * both priority and index match THIS COMPARATOR
	 * WILL NOT BE CONSITANT WITH THE OBJECT'S EQUALS METHOD
	 * @author skyebend
	 * @return
	 */



		public int compareTo( Object anotherTuple) {
			PriorityIntTuple other = (PriorityIntTuple)anotherTuple;
			if (priority < other.priority) {
				return -1;
			} else if (priority > other.priority) {
				return 1;
			} else{
				//check if the indices match
				if (index == other.index){
					return 0;
				} else {
					return 1;
				}
			}
		}
	

	

 public String toString() {
		return ("(" + priority + "," + index + ")");
	}

}
