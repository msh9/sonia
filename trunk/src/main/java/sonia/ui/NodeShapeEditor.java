package sonia.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import sonia.ShapeFactory;

/**
 * 
 * <p>Description:Animates layouts of time-based networks
 * <p>Copyright: CopyLeft  2010: GNU GPL</p>
 * @author Skye Bender-deMoll unascribed
 * @version 1.1
 */

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

@SuppressWarnings("serial")
public class NodeShapeEditor extends AbstractCellEditor implements TableCellEditor
 {
	
	private JComboBox choices;
	
	public NodeShapeEditor(){
		super();
		choices = new JComboBox(ShapeFactory.shapeNames);
	
	}
	

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		return choices;
	}

	@Override
	public Object getCellEditorValue() {
		return choices.getSelectedItem();
	}

	

}
