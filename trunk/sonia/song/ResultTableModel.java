package sonia.song;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

/**
 * table model used to display the network data in the UI
 * @author skyebend
 *
 */
public class ResultTableModel extends AbstractTableModel {
	
	private ArrayList<String[]> data;
	private ArrayList<String> headers;
	
	public ResultTableModel(ArrayList<String[]> tableData, ArrayList<String> tableHeader){
		headers=tableHeader;
		data =tableData;
	}

	public int getColumnCount() {
		if (headers != null){
			return headers.size();
		}
		return 0;
	}

	public int getRowCount() {
		if (data != null){
		return data.size();
		}
		return 0;
	}

	public Object getValueAt(int row, int col) {
		return data.get(row)[col];
	}
	
	public boolean isCellEditable(int rowIndex,
            int columnIndex){
		return true; //fix this to inclue ranges
	}
	
	public void setValueAt(Object aValue,
            int rowIndex,
            int columnIndex){
		if (data != null){
		data.get(rowIndex)[columnIndex] = (String)aValue;
		}
		
	}
	

	@Override
	public String getColumnName(int column) {
		return headers.get(column);
	}

	public void setData(ArrayList<String[]> data) {
		this.data = data;
	}

	public void setHeaders(ArrayList<String> headers) {
		this.headers = headers;
	}
	
	

}
