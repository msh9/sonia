package sonia.song.filters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import sonia.parsers.DotSonColumnMap;
import sonia.song.DataProblem;
import sonia.song.Getter;

public class SqlDateToDecimal extends AbstractFilter {
    
	public static final String COL_INDEX = "Date Column";
	private Getter song;
	private static SimpleDateFormat sqlStyle = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat yearStyle = new SimpleDateFormat("yyyy");
	private static SimpleDateFormat dayOfYearStyle = new SimpleDateFormat("D");
	
	public SqlDateToDecimal(Getter song){
		super();
		this.song = song;
		//TODO: eventually set this up to use properties
		setProperty(COL_INDEX, "1"); //defualt to process the first colum
	}
	public String getDescription() {
		return "Converts SQL-formated dates in StartTime and EndTime to decimal years";
	}

	public String getName() {
		return "SQL_date to decimal";
	}

	public void process(ArrayList<String[]> data,
			ArrayList<DataProblem> problems, boolean dataAreArcs) {
		//figure out which colum is to be fixed
		int start = -1;
		int end = -1;
		if (dataAreArcs){
			start = song.getArcHeaders().indexOf("StartTime");
			end = song.getArcHeaders().indexOf("EndTime");
		} else {
			start = song.getNodeHeaders().indexOf("StartTime");
			end = song.getNodeHeaders().indexOf("EndTime");
		}
		//loop over 'em
		Iterator<String[]> rowIter = data.iterator();
		while (rowIter.hasNext()){
			String[] row = rowIter.next();
			if (start >= 0){
				row[start] = sqlToDecimal(row[start]);
			}
			if (end >= 0){
				row[end] = sqlToDecimal(row[end]);
			}
		}

	}
	
	/**
	 * if unable to parse, returns initial value
	 * @param sqldate
	 * @return
	 */
	private String sqlToDecimal(String sqldate){
		String date = sqldate;
		//assume 2007-12-31
		//TODO: need to catch and deal with null values
		try {
			Date sql = sqlStyle.parse(sqldate);
			double decimal = Double.parseDouble(yearStyle.format(sql)) 
			+Double.parseDouble(dayOfYearStyle.format(sql))/365.0;
			date = ""+decimal;
		} catch (ParseException e) {
			//dont do anything
		}
		return date;
	}
	
	public boolean likesArcs(){
		return true;
	}
	
	public boolean likesNodes(){
		return true;
	}
	

}
