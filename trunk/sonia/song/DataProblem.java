package sonia.song;

/**
 * class holds information about a specific data problem so it can be reviewd and corrected
 * @author skyebend
 *
 */
public class DataProblem {
	
	public static final String NO_DATA_FOR_NODE_ID = "NO_DATA_FOR_NODE_ID";
	public static final String NO_NODE_FOR_FROM_ID = "NO_NODE_FOR_FROM_ID";
	public static final String NO_NODE_FOR_TO_ID = "NO_NODE_FOR_TO_ID";
	public static final String BAD_ID_STRING = "BAD_ID_STRING";
	
	private String type;
	private String description;
	private int rowNumber;
	private String query;
	private Object ref;

	/**
	 * returns an object that this error is linked to
	 */
	public Object getRef() {
		return ref;
	}

	public void setRef(Object ref) {
		this.ref = ref;
	}

	public DataProblem(String type, String description, int rowNum, String query){
		this.type = type;
		this.description = description;
		this.rowNumber = rowNum;
		this.query = query;
	}

	public String getDescription() {
		return description;
	}

	public String getQuery() {
		return query;
	}

	public int getRowNumber() {
		return rowNumber;
	}

	public String getType() {
		return type;
	}
	
	public String toString() {
		return type + ": "+description;
	}
}
