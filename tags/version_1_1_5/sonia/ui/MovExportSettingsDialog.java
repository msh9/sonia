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
package sonia.ui;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import sonia.movie.QTMovieMaker;

public class MovExportSettingsDialog {

	private JDialog settingsDialog;
	private JPanel panel;
	private JButton ok;
	private QTMovieMaker maker;
	
	public MovExportSettingsDialog(SoniaInterface ui,QTMovieMaker moviemaker){
		maker = moviemaker;
		settingsDialog = new JDialog((Frame)ui,"Export Options",true);
		panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		ok = new JButton("OK");
		ok.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				settingsDialog.setVisible(false);
			};
			});
		c.gridy=0;
		panel.add(getOptionComponent(),c);
		c.gridy=1;
		panel.add(ok);
		settingsDialog.getContentPane().add(panel);
		settingsDialog.setSize(300,200);
		
	}
	
	public void showDialog(){
		settingsDialog.setVisible(true);
	}
	
	private JComponent getOptionComponent() {
		JPanel optionPanel = new JPanel();
		optionPanel.setBorder(new TitledBorder("QuickTime Export Options:"));
		JComboBox codec = new JComboBox(QTMovieMaker.codecs);
		codec.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				maker.setCodec((String) ((JComboBox) e.getSource()).getSelectedItem());
			};

		});
		codec.setSelectedItem(QTMovieMaker.codecs[1]);
		codec.setBorder(new TitledBorder("Encoding:"));

		JComboBox quality = new JComboBox(QTMovieMaker.qualityStrings);
		quality.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				maker.setCodecQuality((String) ((JComboBox) e.getSource())
						.getSelectedItem());
			};

		});
		quality.setBorder(new TitledBorder("Quality:"));
		quality.setSelectedItem(QTMovieMaker.qualityStrings[2]);
		optionPanel.add(codec);
		optionPanel.add(quality);
		//JComboBox exportType = new JComboBox(maker.getExportFormats());
		//optionPanel.add(exportType);

		return optionPanel;

	}

}
