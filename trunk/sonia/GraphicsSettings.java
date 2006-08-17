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
/**
 * this code was originally written for SoNIA by skyebend in 2006 
 */
package sonia;


/**
 * @author skyebend
 * 
 */
public class GraphicsSettings extends PropertySettings {
	
	/**
	 * key specifying if graphics should be anti-aliased (slower but smoother).
	 * value should be true or false 
	 */
	public static final String ANTI_ALIAS = "AntiAlias graphics";
	
	
	/**
	 * key specifying if information (frame time, etc) should be renderd on the
	 * layout window. value should be true or false
	 */
	public static final String SHOW_STATS = "show stats";
	
	/**
	 * key specifying if a transparent (greyed out) version of the previous
	 * slice should be renderd under the current view for comparison. boolean
	 * value
	 */
	public static final String GHOST_SLICE = "ghost previous slice";
	
	/**
	 * key indicating if new events should be highlighted the first time the are
	 * drawn, value gives duration of hilite.
	 */
	public static final String FLASH_EVENTS = "flash new events";
	
	/**
	 * key for screen width of the layout in pixels. integer value
	 */
	public static final String LAYOUT_WIDTH = "layout width";
	
	/**
	 * key for screen hight of the layout in pixels, integer value
	 */
	public static final String LAYOUT_HEIGHT = "layout height";
	
	/**
	 * key for value indicating how much the node sizes should be
	 * increased/descreased. double value
	 */
	public static final String NODE_SCALE_FACTOR = "node scale factor";
	
	/**
	 * key for value indicating the opacity of nodes (float, 0 to 1.0)
	 */
	public static final String NODE_TRANSPARENCY = "node transparency";
	
	/**
	 * key indicating of labels should be drawn, value is NONE, LABELS or ID
	 */
	public static final String NODE_LABELS = "show node labels";
	
	/**
	 * key for value indicating a size cutoff for drawing node labels
	 */
	public static final String NODE_LABEL_CUTOFF = "node label cutoff";
	
	/**
	 * key for value indicating the font size for labels (not yet used)
	 */
	public static final String NODE_LABEL_FONTSIZE = "node label fontsize";
	
	/**
	 * key indicating if nodes should be hidden. value NONE or ALL, etc..
	 */
	public static final String HIDE_NODES = "hide nodes";
	
	/**
	 * key for value indicating how much the arc widths should be
	 * increased/descreased. double value
	 */
	public static final String ARCS_WIDTH_FACTOR = "arc width factor";
	
	/**
	 * key for value indicating the opacity of arcs (float, 0 to 1.0)
	 */
	public static final String ARC_TRANSPARENCY = "arc transparency";
	
	/**
	 * key for value indicating if and how arrows should be drawn: none, arrow
	 */
	public static final String ARROW_STYLE = "arrow style";
	
	
	/**
	 * key indicating which arcs should be hidden: NONE, ALL..
	 */
	public static final String HIDE_ARCS = "hide arcs";
	
	/**
	 * key indicating if arc labels should be drawn: NONE, LABELS, Layout weights, 
	 */
	public static final String ARC_LABELS = "arc labels";
	
	//-------------------values-----------------
	
	public static final String NONE = "none";
	public static final String ALL = "all";
	public static final String LABELS = "labels";
	public static final String IDS = "IDs";
	
	/**
	 * value for labeling arcs with the weight for the relation used by the layout algorithm
	 */
	public static final String LAYOUT_WEIGHTS= "layout weights";
	
	/**
	 * value for arrow style indicating arrows should be drawn at the target end of the arc
	 */
	public static final String ARROW_END="arrow at end";
}
