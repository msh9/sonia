/*
 * AttributeMapperDialog.java
 *
 * Created on 08 April 2005, 13:55
 */
package sonia;
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
/**
 *
 * @author skyebend
 */
public class AttributeMapperDialog extends Object{
    
    private ArrayList unmapped;
    private ColumnMap map;
    private Dialog dialog;
    private JScrollPane sp;
    private JPanel layoutPanel;
    
    private ItemListener choiceListener =  new ItemListener() {
            public void itemStateChanged(ItemEvent e){
                //debug
                System.out.println(((JComboBox)e.getSource()).getName()+":"+ e.getItem());
                map.setValueForFieldName(((JComboBox)e.getSource()).getName(), e.getItem().toString());
                //need to put the deselected item on the list for others to use...
            }
        };
    
    
    /** Creates a new instance of AttributeMapperDialog */
    public AttributeMapperDialog(ColumnMap map, ArrayList unmapped) {
        
        
        this.unmapped = unmapped;
        this.map = map;
        //make the dialog
        dialog = new JDialog(new JFrame(),"Unrecognized column names in the input file",true);
        //make new font to help keep layouts consitant across platforms
        Font textFont = new Font("Monospaced ",Font.PLAIN,10);
        layoutPanel = new JPanel();
        layoutPanel.setFont(textFont);
        layoutPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0,2,0,2);
        //make the components
        JLabel explainLabel = new JLabel("Please choose appropriate column name assignments for each attribute:");
        JLabel attrLabel = new JLabel("Network Attributes:");
        JLabel colLabel = new JLabel("Column Names:");
        JButton ok = new JButton("OK");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
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
        ArrayList mapKeys =map.getMapKeys();
        Iterator attrKeyIter = mapKeys.iterator();
        //loop to add all the attribute labels
        while(attrKeyIter.hasNext()) {
            c.gridx=0;c.gridy=rowNum;c.gridwidth=1;c.gridheight=1;c.weightx=1;c.weighty=1;
            c.anchor=GridBagConstraints.EAST;
            layoutPanel.add(new JLabel((String)attrKeyIter.next()),c);
            rowNum++;
        }
        c.gridx=0;c.gridy=rowNum;c.gridwidth=2;c.gridheight=1;c.weightx=1;c.weighty=1.5;
        
        //loop to add the column choices
        ArrayList values = map.getMapValues();
        for (int i=0;i<values.size();i++) {
            //make a new list of choices
        	unmapped.add(0,(String)values.get(i));
            JComboBox choiceList = new JComboBox(unmapped.toArray());
            choiceList.setName((String)mapKeys.get(i));
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
        dialog.setLayout(new GridBagLayout());
        sp= new JScrollPane(layoutPanel);
        c.gridx=0;c.gridy=0;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;c.fill=GridBagConstraints.NONE;
        dialog.add(explainLabel,c);
        c.gridx=0;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=1;c.weighty=1;c.fill=GridBagConstraints.BOTH;
        dialog.add(sp,c);
        c.gridx=0;c.gridy=2;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
        	c.fill=GridBagConstraints.NONE;c.anchor=GridBagConstraints.CENTER;
        dialog.add(ok,c);
        //dialog.setBackground(Color.lightGray);
        dialog.setSize(400,300);
   
        dialog.setLocation(100,100);
        //dialog.pack();
        dialog.setVisible(true);
    }
    
    
    
}
