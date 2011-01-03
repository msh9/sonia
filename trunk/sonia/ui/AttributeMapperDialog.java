/*
 * AttributeMapperDialog.java
 *
 * Created on 08 April 2005, 13:55
 */
package sonia.ui;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import sonia.parsers.DotSonColumnMap;
/**
 *
 * @author skyebend
 */
public class AttributeMapperDialog extends Object{
    
    private ArrayList<String> unmapped;
    private Object[] headers;
    private ArrayList<Boolean> states;
    private DotSonColumnMap map;
    private Dialog dialog;
    private JScrollPane sp;
    private JPanel layoutPanel;
    private JTable unmappedTable;

    
    
    private ItemListener choiceListener =  new ItemListener() {
            public void itemStateChanged(ItemEvent e){
                
                map.setProperty(((JComboBox)e.getSource()).getName(), e.getItem().toString());
                //need to put the deselected item on the list for others to use...
            }
        };
    
    
    /** Creates a new instance of AttributeMapperDialog */
    public AttributeMapperDialog(DotSonColumnMap map, ArrayList<String> unmapped,
    		Set<String> headers) {
        
        this.unmapped = unmapped;
        this.map = map;
        this.headers = headers.toArray();
        Arrays.sort(this.headers);
        states = new ArrayList<Boolean>(unmapped.size());
        //make the dialog
        dialog = new JDialog(new JFrame(),"Unrecognized column names in the input file",true);
        //make new font to help keep layouts consitant across platforms
        layoutPanel = new JPanel();
       
        layoutPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0,2,0,2);
        //make the components
        layoutPanel.setBorder(new TitledBorder("Choose or re-assign columns to attributes"));
        JLabel attrLabel = new JLabel("Network Attributes:");
        JLabel colLabel = new JLabel("Column Names:");
        JButton ok = new JButton("OK");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	storeUdata();
                dialog.setVisible(false);
                dialog = null;
            }
        });
        c.gridx=0;c.gridy=0;c.gridwidth=2;c.gridheight=1;c.weightx=1;c.weighty=1.5;
        //layoutPanel.add(explainLabel,c);
        c.gridx=0;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=1;c.weighty=1;
        layoutPanel.add(attrLabel,c);
        c.gridx=1;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=1;c.weighty=1;
        layoutPanel.add(colLabel,c);
        int rowNum = 2;
        Set<Object> mapKeys =map.keySet();
        Iterator<Object> attrKeyIter = mapKeys.iterator();
        //loop to add all the attribute labels
        while(attrKeyIter.hasNext()) {
            c.gridx=0;c.gridy=rowNum;c.gridwidth=1;c.gridheight=1;c.weightx=1;c.weighty=1;
            c.anchor=GridBagConstraints.EAST;
            layoutPanel.add(new JLabel((String)attrKeyIter.next()),c);
            rowNum++;
        }
        c.gridx=0;c.gridy=rowNum;c.gridwidth=2;c.gridheight=1;c.weightx=1;c.weighty=1.5;
        //TODO: clean all this up, it is messy
        //loop to add the column choices
        Object[] values = map.values().toArray();
        Object[] keys = mapKeys.toArray();
        for (int i=0;i<values.length;i++) {
            //make a new list of choices
        	unmapped.add(0,values[i].toString());
            JComboBox choiceList = new JComboBox(unmapped.toArray());
            choiceList.setName(keys[i].toString());
            choiceList.addItemListener(choiceListener);
            unmapped.remove(0);
            //add the items to the list
            //Iterator unmappedIter = unmapped.iterator();
           // while (unmappedIter.hasNext()) {
           //     choiceList.addItem((String)unmappedIter.next());
           // }
            c.gridx=1;c.gridy=2+i;c.gridwidth=1;c.gridheight=1;c.weightx=1;c.weighty=1;
            c.anchor=GridBagConstraints.WEST;
            layoutPanel.add(choiceList,c);
        }
       dialog.setLayout(new BorderLayout());
        
        sp= new JScrollPane(layoutPanel);
        c.gridx=0;c.gridy=0;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;c.fill=GridBagConstraints.NONE;
        c.gridx=0;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=1;c.weighty=1;c.fill=GridBagConstraints.BOTH;
       // dialog.add(sp,c);
        c.gridx=0;c.gridy=2;c.gridwidth=1;c.gridheight=1;c.weightx=1;c.weighty=1;
    	c.fill=GridBagConstraints.BOTH;c.anchor=GridBagConstraints.CENTER;
        unmappedTable = new JTable(fillArray(unmapped),new Object[]{"Input Column Name","Attach to node as user data?"});
        unmappedTable.setShowGrid(true);
        unmappedTable.setShowHorizontalLines(true);
        unmappedTable.setShowVerticalLines(true);
        unmappedTable.setModel(new udataTableModel());
        JScrollPane tableScroll = new JScrollPane(unmappedTable);
        tableScroll.setBorder(new TitledBorder("Select attributes to be User Data"));
       // dialog.add(tableScroll,c);
        JSplitPane divider = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,sp,tableScroll);
        divider.setDividerLocation(400);
        dialog.add(divider,BorderLayout.CENTER);
        c.gridx=0;c.gridy=3;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
        	c.fill=GridBagConstraints.NONE;c.anchor=GridBagConstraints.CENTER;
        JPanel okHolder = new JPanel();
        okHolder.add(ok);
        dialog.add(okHolder,BorderLayout.SOUTH);
        //dialog.setBackground(Color.lightGray);
        dialog.setSize(800,400);
   
        dialog.setLocation(100,100);
        //dialog.pack();
        dialog.setVisible(true);
    }
    
    /**
     * store the selected user data items to the dotSonColumnMap
     * @author skyebend
     */
    private void storeUdata(){
    	String nodeDataKeys = "";
    	for (int i = 0; i < unmappedTable.getModel().getRowCount(); i++) {
    		if (((Boolean)unmappedTable.getModel().getValueAt(i,1)).booleanValue()){ //if it is checked
    			nodeDataKeys += unmappedTable.getModel().getValueAt(i,0)+",";
    		}
		}
    	//drob the last comma
    	nodeDataKeys = nodeDataKeys.substring(0,nodeDataKeys.length()-1);
    	map.put(DotSonColumnMap.NODE_DATA_KEYS,nodeDataKeys);
    }
    
    private class udataTableModel extends AbstractTableModel{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		@Override
		public String getColumnName(int column) {
			if (column == 0){
				return "Input Column Name";
			} 
			return "Attach to node as user data?";
		}

		public int getRowCount() {
			return headers.length;
		}

		public int getColumnCount() {
			return 2;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex==0){
			return headers[rowIndex];
		} else if(columnIndex ==1){
			return states.get(rowIndex);
		}else{
			return null;
		}
		}

		public Class<?> getColumnClass(int columnIndex) {
			return getValueAt(0, columnIndex).getClass();
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 1){
				return true;
			}
			return false;
		}
		 public void setValueAt(Object value, int row, int col) {
		        states.set(row,(Boolean)value);
		        storeUdata();
		        fireTableCellUpdated(row, col);
		    }
		
    	
    }
    
    private Object[][] fillArray(ArrayList<String> unmapped){
    	Object[][] array = new Object[headers.length][2];
    	states.clear();
    	//Arrays.sort(headArray);
    	
    	for (int i = 0; i < headers.length; i++) {
			array[i][0]= headers[i];
		    array[i][1] =  new Boolean(unmapped.contains(headers[i]));
			states.add(i,(Boolean)array[i][1]);
		}
    	return array;
    }
    
    
    
}
