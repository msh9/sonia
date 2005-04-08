/*
 * ColumnMap.java
 *
 * Created on 08 April 2005, 10:46
 */
package sonia;
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
import java.util.*;
import java.lang.reflect.Field;
/**
 * Class for associating the columnames of the .son input file with their intented
 *uses.  Assigments are availible as public variables. (eventually this could be 
 *replaced by a class that reads an input file...)
 * @author skyebend
 */
public class ColumnMap extends Object{
    
      //defualt colum name mappings
    public String NODE_ID = "NodeId";
    public String ALPHA_ID =   "AlphaId";
    public String NODE_STARTIME =   "StartTime";
    public String NODE_ENDTIME = "EndTime";
    public String NODE_X_COORD = "X";
    public String NODE_Y_COORD = "Y";
    public String NODE_LABEL = "Label";
    public String NODE_SIZE = "NodeSize";
    public String NODE_SHAPE = "NodeShape";
    public String NODE_LABEL_COLOR_NAME = "LabelColor";
    public String NODE_BORDER_COLOR_NAME = "BorderColor";
    public String NODE_BORDER_WIDTH = "BorderWidth";
    public String NODE_COLOR_NAME = "ColorName";
    public String NODE_RED_RGB = "RedRGB";
    public String NODE_GREEN_RGB = "GreenRGB";
    public String NODE_BLUE_RGB = "BlueRGB";

    public String ARC_STARTIME =   "StartTime";
    public String ARC_ENDTIME= "EndTime";
    public String FROM_ID = "FromId";
    public String TO_ID = "ToId";
    public String ARC_WEIGHT = "ArcWeight";
    public String ARC_WIDTH = "ArcWidth";
    public String ARC_COLOR_NAME = "ColorName";
    public String ARC_LABEL = "Label";
     public String ARC_RED_RGB = "RedRGB";
    public String ARC_GREEN_RGB = "GreenRGB";
    public String ARC_BLUE_RGB = "BlueRGB";
  
    
    
    /** Creates a new instance of ColumnMap with defualt mappings. 
     * mappings are public variables to be changed. 
     */
    public ColumnMap() {
    }
    
    /**
     * uses introspection to return a list of the names of all the fields ("keys")
     * Order not determined?
     */
    public ArrayList getMapKeys()
    {
        ArrayList keyNames = new ArrayList();
        try
        {
            Field[] fields = ColumnMap.class.getDeclaredFields();
           for(int i = 0; i<fields.length;i++)
           {
            keyNames.add(fields[i].getName());    
           }
        }
        catch(SecurityException e)
        {
            e.printStackTrace();
        }
        return keyNames;
    }
    
    /**
     * returns a list of the values of all the fields
     * order may not be determined, but will match the order of the keys
     */
    public ArrayList getMapValues()
    {
        ArrayList values = new ArrayList();
        Iterator keyIter = getMapKeys().iterator();
        while (keyIter.hasNext())
        {
            values.add(getValueForFieldName((String)keyIter.next()));
        }
        return values;
    }
    
    /**
     * gets the value of the passed field name for this instance.
     * returns null if the field does not exist or if there is a security exception
     */
    public String getValueForFieldName(String name)
    {
        String value = null;
        //get the field associated with the name
        try
        {
        Field f = this.getClass().getField(name);
        //get the value of the field for this instance of the object
        value = (String)f.get(this); 
        }
        catch (Exception e)
        {
            //debug
            e.printStackTrace();
        }
        return value;
    }
    
    /**
     *sets the value for the passed field name. If field is not found, or
     * var is the wrong type, does nothing
     */
      public void setValueForFieldName(String fieldName, String value)
    {
        //get the field associated with the name
        try
        {
        Field f = this.getClass().getField(fieldName);
        //get the value of the field for this instance of the object
        f.set(this,value); 
        }
        catch (Exception e)
        {
            //debug
            e.printStackTrace();
        }
    }
    
}
