/*
 * AttributeMapperDialog.java
 *
 * Created on 08 April 2005, 13:55
 */
package sonia;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
/**
 *
 * @author skyebend
 */
public class AttributeMapperDialog extends Object{
    
    private ArrayList unmapped;
    private ColumnMap map;
    private Dialog dialog;
    
    private ItemListener choiceListener =  new ItemListener() {
            public void itemStateChanged(ItemEvent e){
                //debug
                System.out.println(((Choice)e.getSource()).getName()+":"+ e.getItem());
                map.setValueForFieldName(((Choice)e.getSource()).getName(), e.getItem().toString());
                //need to put the deselected item on the list for others to use...
            }
        };
    
    
    /** Creates a new instance of AttributeMapperDialog */
    public AttributeMapperDialog(ColumnMap map, ArrayList unmapped) {
        
        
        this.unmapped = unmapped;
        this.map = map;
        //make the dialog
        dialog = new Dialog(new Frame(),"Unrecognized column names in the input file",true);
        //make new font to help keep layouts consitant across platforms
        Font textFont = new Font("Monospaced ",Font.PLAIN,10);
        dialog.setFont(textFont);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0,2,0,2);
        //make the components
        Label explainLabel = new Label("Please choose appropriate column name assignments for each attribute:");
        Label attrLabel = new Label("Network Attributes:");
        Label colLabel = new Label("Column Names:");
        Button ok = new Button("OK");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                dialog.hide();
                dialog = null;
            }
        });
        c.gridx=0;c.gridy=0;c.gridwidth=2;c.gridheight=1;c.weightx=1;c.weighty=1.5;
        dialog.add(explainLabel,c);
        c.gridx=0;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=1;c.weighty=1;
        dialog.add(attrLabel,c);
        c.gridx=1;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=1;c.weighty=1;
        dialog.add(colLabel,c);
        int rowNum = 2;
        ArrayList mapKeys =map.getMapKeys();
        Iterator attrKeyIter = mapKeys.iterator();
        //loop to add all the attribute labels
        while(attrKeyIter.hasNext()) {
            c.gridx=0;c.gridy=rowNum;c.gridwidth=1;c.gridheight=1;c.weightx=1;c.weighty=1;
            c.anchor=c.EAST;
            dialog.add(new Label((String)attrKeyIter.next()),c);
            rowNum++;
        }
        c.gridx=0;c.gridy=rowNum;c.gridwidth=2;c.gridheight=1;c.weightx=1;c.weighty=1.5;
        dialog.add(ok,c);
        //loop to add the column choices
        ArrayList values = map.getMapValues();
        for (int i=0;i<values.size();i++) {
            //make a new list of choices
            Choice choiceList = new Choice();
            choiceList.setName((String)mapKeys.get(i));
            choiceList.addItemListener(choiceListener);
            choiceList.add((String)values.get(i));
            //add the items to the list
            Iterator unmappedIter = unmapped.iterator();
            while (unmappedIter.hasNext()) {
                choiceList.add((String)unmappedIter.next());
            }
            c.gridx=1;c.gridy=2+i;c.gridwidth=1;c.gridheight=1;c.weightx=1;c.weighty=1;
            c.anchor=c.WEST;
            dialog.add(choiceList,c);
        }
        
        dialog.setBackground(Color.lightGray);
        //dialog.setSize(200,300);
        dialog.setLocation(100,100);
        dialog.pack();
        dialog.show();
    }
    
    
    
}
