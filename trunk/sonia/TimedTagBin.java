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
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

/**
 * for sort time:key:value associations into bins by time and retriving them.
 * Not necesairly very efficient, but backed by a TreeMap
 * 
 * @author skyebend
 * 
 */
public class TimedTagBin {

	private TreeMap<Interval,Vector> binStorage;

	public TimedTagBin() {
		binStorage = new TreeMap<Interval,Vector>(new Comparator(){

			public int compare(Object arg0, Object arg1) {
				return ((Interval)arg0).compareTo(((Interval)arg1));
			}});
	}

	/**
	 * put they key value association into a bin with the time
	 * 
	 * @author skyebend
	 * @param time
	 * @param keyTag
	 * @param value
	 */
	public void addAssociation(double start, double end, String keyTag, String value) {
		Interval timeKey = new Interval(start,end);
		String[] keyTagValue = new String[] { keyTag, value };
		if (binStorage.containsKey(timeKey)) {
			((Vector) binStorage.get(timeKey)).add(keyTagValue);
		} else {
			Vector bin = new Vector();
			bin.add(keyTagValue);
			binStorage.put(timeKey, bin);
		}
	}

	/**
	 * return Iterator containing all the bin times as Interval objects in acending order. WARNING:
	 * modifying this will mess with the map!
	 * 
	 * @author skyebend
	 * @return
	 */
	public Iterator getBinTimeIter(){
		return binStorage.keySet().iterator();
	}

	
	/**
	 * return the contents of the bin as a vector, each element of which is a
	 * String[key,value], return null if none match?
	 * 
	 * @author skyebend
	 * @param time
	 * @return
	 */
	public Vector getBin(Interval interval) {
       return (Vector)binStorage.get(interval);
	}

	@Override
	public String toString() {
		String returnString = "{";
		Iterator keyIter = binStorage.keySet().iterator();
		while (keyIter.hasNext()){
			Interval key = (Interval)keyIter.next();
			returnString += "("+key.start+"-"+key.end+")=>(";
			Iterator binIter = binStorage.get(key).iterator();
			while(binIter.hasNext()){
				 String[] value = (String[])binIter.next();
				 returnString += value[0]+"="+value[1]+",";
				
			}
			returnString = returnString.substring(0,returnString.length()-1)+"),";
		}
	returnString = returnString.substring(0,returnString.length()-1)+"}";
		return returnString;
	}
	
	

}
