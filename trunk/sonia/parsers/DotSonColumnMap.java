/*
 * ColumnMap.java
 *
 * Created on 08 April 2005, 10:46
 */
package sonia.parsers;

/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
import java.util.*;
import java.lang.reflect.Field;

import sonia.settings.PropertySettings;

/**
 * Class for associating the columnames of the .son input file with their
 * intented uses. Assigments are availible as public variables. (eventually this
 * could be replaced by a class that reads an input file...)
 * 
 * @author skyebend
 */
public class DotSonColumnMap extends PropertySettings {

	// defualt colum name mappings
	//TODO:  these constants should be moved to the DotSonParser class and documented there
	public static final String NODE_ID = "NODE_ID";

	public static final String ALPHA_ID = "ALPHA_ID";

	public static final String NODE_STARTIME = "NODE_STARTIME";

	public static final String NODE_ENDTIME = "NODE_ENDTIME";

	public static final String NODE_X_COORD = "NODE_X_COORD";

	public static final String NODE_Y_COORD = "NODE_Y_COORD";

	public static final String NODE_LABEL = "NODE_LABEL";

	public static final String NODE_SIZE = "NODE_SIZE";

	public static final String NODE_SHAPE = "NODE_SHAPE";

	public static final String NODE_LABEL_COLOR_NAME = "NODE_LABEL_COLOR_NAME";
	
	public static final String NODE_LABEL_SIZE = "NODE_LABEL_SIZE";

	public static final String NODE_BORDER_COLOR_NAME = "NODE_BORDER_COLOR_NAME";

	public static final String NODE_BORDER_WIDTH = "NODE_BORDER_WIDTH";

	public static final String NODE_COLOR_NAME = "NODE_COLOR_NAME";

	public static final String NODE_RED_RGB = "NODE_RED_RGB";

	public static final String NODE_GREEN_RGB = "NODE_GREEN_RGB";

	public static final String NODE_BLUE_RGB = "NODE_BLUE_RGB";
        
    public static final String NODE_ICON_URL = "NODE_ICON_URL";

	public static final String ARC_STARTIME = "ARC_STARTIME";

	public static final String ARC_ENDTIME = "ARC_ENDTIME";

	public static final String FROM_ID = "FROM_ID";

	public static final String TO_ID = "TO_ID";

	public static final String ARC_WEIGHT = "ARC_WEIGHT";

	public static final String ARC_WIDTH = "ARC_WIDTH";

	public static final String ARC_COLOR_NAME = "ARC_COLOR_NAME";

	public static final String ARC_LABEL = "ARC_LABEL";

	public static final String ARC_RED_RGB = "ARC_RED_RGB";

	public static final String ARC_GREEN_RGB = "ARC_GREEN_RGB";

	public static final String ARC_BLUE_RGB = "ARC_BLUE_RGB";
	
	public static final String NODE_DATA_KEYS = "NODE_DATA_KEYS";
	// keys for cluster related cols
	public static final String CLUSTER_ID = "CLUSTER_ID";
	public static final String CLUSTER_WEIGHT = "CLUSTER_WEIGHT";
	public static final String PARENT = "PARENT";
	public static final String CHILDREN = "CHILDREN";
	public static final String NODE_IDS = "NODE_IDS";
	public static final String CLUSTER_STARTTIME = "CLUSTER_STARTTIME";
	public static final String CLUSTER_ENDTIME = "CLUSTER_ENDTIME";
	public static final String CLUSTER_FILL_COLOR = "CLUSTER_FILL_COLOR";
	public static final String CLUSTER_BORDER_COLOR = "CLUSTER_BORDER_COLOR";
	
	

	/**
	 * Creates a new instance of ColumnMap with defualt mappings. mappings are
	 * public variables to be changed.
	 */
	public DotSonColumnMap() {
		super();
		//set up the default key names and values
		setProperty(NODE_ID , "NodeId");
		setProperty(ALPHA_ID , "AlphaId");
		setProperty(NODE_STARTIME , "StartTime");
		setProperty(NODE_ENDTIME , "EndTime");
		setProperty(NODE_X_COORD , "X");
		setProperty(NODE_Y_COORD , "Y");
		setProperty(NODE_LABEL , "Label");
		setProperty(NODE_SIZE , "NodeSize");
		setProperty(NODE_SHAPE , "NodeShape");
		setProperty(NODE_LABEL_COLOR_NAME , "LabelColor");
		setProperty(NODE_LABEL_SIZE, "LabelSize");
		setProperty(NODE_BORDER_COLOR_NAME , "BorderColor");
		setProperty(NODE_BORDER_WIDTH , "BorderWidth");
		setProperty(NODE_COLOR_NAME , "ColorName");
		setProperty(NODE_RED_RGB , "RedRGB");
		setProperty(NODE_GREEN_RGB , "GreenRGB");
		setProperty(NODE_BLUE_RGB , "BlueRGB");
	    setProperty(NODE_ICON_URL , "IconURL");
		setProperty(ARC_STARTIME , "StartTime");
		setProperty(ARC_ENDTIME , "EndTime");
		setProperty(FROM_ID , "FromId");
		setProperty(TO_ID , "ToId");
		setProperty(ARC_WEIGHT , "ArcWeight");
		setProperty(ARC_WIDTH , "ArcWidth");
		setProperty(ARC_COLOR_NAME , "ColorName");
		setProperty(ARC_LABEL , "Label");
		setProperty(ARC_RED_RGB , "RedRGB");
		setProperty(ARC_GREEN_RGB , "GreenRGB");
		setProperty(ARC_BLUE_RGB , "BlueRGB");
		//clusters
		setProperty(CLUSTER_ID,"ClusterId");
		setProperty(CLUSTER_WEIGHT,"ClusterWeight");
		setProperty(PARENT,"Parent");
		setProperty(CHILDREN,"Children");
		setProperty(NODE_IDS,"NodeIds");
		setProperty(CLUSTER_STARTTIME,"StartTime");
		setProperty(CLUSTER_ENDTIME,"EndTime");
		setProperty(CLUSTER_FILL_COLOR,"Color");
		setProperty(CLUSTER_BORDER_COLOR,"BorderColor");
		
//		Iterator keyIter = getMapKeys().iterator();
//		while (keyIter.hasNext()){
//			String key = (String)keyIter.next();
//			setProperty(key,getValueForFieldName(key));
//		}

	}

	/**
	 * uses introspection to return a list of the names of all the fields
	 * ("keys") Order not determined?
	 */
	private ArrayList<String> getMapKeys() {
		ArrayList<String> keyNames = new ArrayList<String>();
		try {
			Field[] fields = DotSonColumnMap.class.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				String name = fields[i].getName();

				if (!name.startsWith("class")) {
					keyNames.add(name);
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return keyNames;
	}

	/**
	 * returns a list of the values of all the fields order may not be
	 * determined, but will match the order of the keys
	 */
//	public ArrayList getMapValues() {
//		ArrayList values = new ArrayList();
//		Iterator keyIter = getMapKeys().iterator();
//		while (keyIter.hasNext()) {
//
//			values.add(getValueForFieldName((String) keyIter.next()));
//
//		}
//		return values;
//	}
//
	/**
	 * gets the value of the passed field name for this instance. returns null
	 * if the field does not exist or if there is a security exception
	 */
	private String getValueForFieldName(String name) {
		String value = null;
		// get the field associated with the name
		try {
			Field f = this.getClass().getField(name);
			// get the value of the field for this instance of the object
			value = (String) f.get(this);
		} catch (Exception e) {
			// debug
			System.out.println("error reading field value in column map class");
			e.printStackTrace();
		}
		return value;
	}
//
//	/**
//	 * sets the value for the passed field name. If field is not found, or var
//	 * is the wrong type, does nothing
//	 */
//	public void setValueForFieldName(String fieldName, String value) {
//		// get the field associated with the name
//		try {
//			Field f = this.getClass().getField(fieldName);
//			// get the value of the field for this instance of the object
//			f.set(this, value);
//		} catch (Exception e) {
//			// debug
//			e.printStackTrace();
//		}
//	}

}
