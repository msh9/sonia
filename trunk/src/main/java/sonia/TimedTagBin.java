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
import java.util.TreeSet;
import java.util.Vector;

import cern.colt.list.DoubleArrayList;

/**
 * for sort time:key:value associations into bins by time and retreieving them.
 * Not necessarily very efficient, but backed by a TreeMap
 * 
 * @author skyebend
 * 
 */
public class TimedTagBin {

	private TreeMap<Interval,Vector<String[]>> binStorage;
	
	/**
	 * To store all the time points at which the status of the object changes
	 */
	private TreeSet<Double> timeline; 

	public TimedTagBin() {
		binStorage = new TreeMap<Interval,Vector<String[]>>(new Comparator<Object>(){

			public int compare(Object arg0, Object arg1) {
				return ((Interval)arg0).compareTo(((Interval)arg1));
			}});
		timeline = new TreeSet<Double>();
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
			((Vector<String[]>) binStorage.get(timeKey)).add(keyTagValue);
		} else {
			Vector<String[]> bin = new Vector<String[]>();
			bin.add(keyTagValue);
			binStorage.put(timeKey, bin);
		}
		//also add to timeline
		if (!timeline.contains(Double.valueOf(start))){
			timeline.add(Double.valueOf(start));
		}
		if (!timeline.contains(Double.valueOf(end))){
			timeline.add(Double.valueOf(end));
		}
		
	}
	
	/**
	 * put multiple cases of a key-value association into a bin with the in the included set of spells
	 * 
	 * @author skyebend
	 * @param spells  double array of start and end times to add for that vale
	 * @param keyTag
	 * @param value
	 */
	public void addAssociations(double[][] spells, String keyTag, String value) {
		for (int s=0; s<spells.length;s++){
			Interval timeKey = new Interval(spells[s][0],spells[s][1]);
			String[] keyTagValue = new String[] { keyTag, value };
			if (binStorage.containsKey(timeKey)) {
				((Vector<String[]>) binStorage.get(timeKey)).add(keyTagValue);
			} else {
				Vector<String[]> bin = new Vector<String[]>();
				bin.add(keyTagValue);
				binStorage.put(timeKey, bin);
			}
			//also add to timeline
			if (!timeline.contains(Double.valueOf(spells[s][0]))){
				timeline.add(Double.valueOf(spells[s][0]));
			}
			if (!timeline.contains(Double.valueOf(spells[s][1]))){
				timeline.add(Double.valueOf(spells[s][1]));
			}
		}
	}

	/**
	 * return Iterator containing all the bin times as Interval objects in ascending order. WARNING:
	 * modifying this will mess with the map!
	 * 
	 * @author skyebend
	 * @return
	 */
	public Iterator<Interval> getBinTimeIter(){
		return binStorage.keySet().iterator();
	}
	
	/**
	 * Returns a set of adjacent non-overlapping subintervals covering the start and end points of all the stored intervals.  
	 * The values associated with each subinterval include the values of all the intervals it intersects with. 
	 * @return a treemap keyed by Interval objects associated with Vectors of values
	 */
	public TreeMap<Interval,Vector<String[]>> getSubSpellTree(){
		//TODO: this should be done with an interval tree, not on the fly by checking every value
		TreeMap<Interval,Vector<String[]>> subSpells = new TreeMap<Interval,Vector<String[]>>(new Comparator<Object>(){
			public int compare(Object arg0, Object arg1) {
				return ((Interval)arg0).compareTo(((Interval)arg1));
			}});
		//construct a list of spells corresponding to all the subpells
		Iterator<Double> times = timeline.iterator();
		Double first = times.next();
		while (times.hasNext()){
			Double second = times.next();
			Interval sub = new Interval(first.doubleValue(), second.doubleValue());
			Vector<String[]> values = new Vector<String[]>();
			//check all the intervals to find those that intersect with sub
			Iterator<Interval> intervals = binStorage.keySet().iterator();
			while (intervals.hasNext()){
				Interval interval = intervals.next();
				if (sub.within(interval)){
					values.addAll(binStorage.get(interval));
				}
			}
			subSpells.put(sub, values);
			first = second; //shift over to the next interval
			
		}
		return subSpells;
		
	}

	
	/**
	 * return the contents of the bin as a vector, each element of which is a
	 * String[key,value], return null if none match?
	 * 
	 * @author skyebend
	 * @param time
	 * @return
	 */
	public Vector<String[]> getBin(Interval interval) {
       return (Vector<String[]>)binStorage.get(interval);
	}

	@Override
	public String toString() {
		String returnString = "{";
		Iterator<Interval> keyIter = binStorage.keySet().iterator();
		while (keyIter.hasNext()){
			Interval key = (Interval)keyIter.next();
			returnString += "("+key.start+"-"+key.end+")=>(";
			Iterator<String[]> binIter = binStorage.get(key).iterator();
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
