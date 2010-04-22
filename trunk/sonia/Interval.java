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

/**
 * class for holding start and end data in a form that makes it comparable and
 * gives a sorting order
 * 
 * @author skyebend
 * 
 */
public class Interval implements Comparable {

	public double start;

	public double end;

	public Interval(double start, double end) {
		this.start = start;
		this.end = end;
	}

	public int compareTo(Object arg0) {
		Interval other = (Interval) (arg0);
		if        ((other.start < start) & (other.end < end)) {
			return -1;
		} else if ((other.start < start) & (other.end > end)) {
			return -1;
		} else if ((other.start < start) & (other.end == end)) {
			return -1;
		} else if ((other.start == start) & (other.end > end)) {
			return -1;
		} else if ((other.start == start) & (other.end == end)) {
			return 0;
		} else if ((other.start == start) & (other.end < end)) {
			return 1;
		} else if ((other.start > start) & (other.end == end)) {
			return 1;
		} else if ((other.start > start) & (other.end < end)) {
			return 1;
		} else if ((other.start > start) & (other.end > end)) {
			return 1;
		}
		return 2;  //something is horribly wrong, so fail fast
	}

	public boolean equals(Object obj) {
		Interval other = (Interval) (obj);
		if (this.compareTo(other) == 0){
			return true;
		} else {return false;}
	}
	
	public boolean within(Interval other){
		if ((this.start >= other.start) & (this.start <= other.end)){
			if ((this.end <= other.end) & (this.end >= other.start)){
				return true;
			}
		} 
		return false;
		
	}

	@Override
	public String toString() {
		return "["+start+"-"+end+"]";
	}
	
	

}
