package sonia;

import java.awt.*;
import java.awt.event.*;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * <p>Title:SoNIA (Social Network Image Animator) </p>
 * <p>Description:Animates layouts of time-based networks
 * <p>Copyright: CopyLeft  2004: GNU GPL</p>
 * <p>Company: none</p>
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

/**
 * Displays settings for configuring and modifying the display of the networks
 * in the LayoutWindow. Node and Arc scaling, transparency, ghosting, labels,
 * anti-alias, etc.  Also options for numerically resizeing the window.  The
 * dialog is displayed by the "View Options" button on the LayoutWindow. The
 * settings are stored in the Canvas. The settings are as follows:
 * <P>
 * Anti-Alias - turns on anti-alias graphics smoothing (see Graphics2D).  Defaults
 * to on 'cause it looks much better, there will be some speed improvement by
 * turning it off.
 * </P><P>
 * Show Stats - Shows information (the layout slice #, the start and end times of
 * the render displayed, the name of the layout algorithm) in small text on the
 * top of the layout.  This really only impacts the movie, as it obscured by the
 * title bar in the LayoutWindow, and the information is already present.
 * </P><P>
 * Ghost Previous Slice - Displays a greyed-out image of the the previously viewd
 * slice under the current slice.  Useful for identifying where nodes move to.
 * </P><P>
 * Layout Width, Layout Height - Specifies the dimensions of the layout area in
 * pixels.
 * </P><P>
 * Node scale factor - Scales up or down the size of the nodes by multiply their
 * original value
 * </P><P>
 * Show labels - show the label stored in the NodeAttribute
 * </P><P>
 * Show Ids - show the id sonia assigned to each node
 * </P><P>
 * Hide Nodes - hid the nodes completely (but still draw the labels)
 * </P><P>
 * Arc transparency - how opaque to draw the arcs (0.0 = clear, 1.0 = solid)
 * </P><P>
 * Arc width factor - Scales up or down the width of the arcs by multiplying
 * their original values
 * </P><P>
 * Show Arrowheads - Draw the arrowheads and the destination end of the arcs
 * </P><P>
 * Show arc weights - show the weight of the arc (after aggregation) used by the
 * layot (useful for debugging)
 * </P><P>
 * Show Arc Labels - draw the label for the arc at the middle of the arc
 *</P>
 */

public class GraphicsSettingsDialog
{
  private JDialog graphicsDialog;
  private SoniaLayoutEngine engine;
  private SoniaController control;
  private SoniaCanvas canvas;

  private JLabel GeneralLabel;
  private JLabel ArcsLabel;
  private JLabel NodesLabel;
  private JCheckBox AntiAlias;
  private JCheckBox ArcTrans;
  private JTextField ArcTransField;
  private JCheckBox NodeTrans;       // node transparency enable
  private JTextField NodeTransField;  // node transparency value
  private JLabel ArcWidthFactorLabel;
  private JTextField ArcWidthFactorField;
  private JLabel NodeScaleFactorLabel;
  private JTextField NodeScaleFactorField;
  private JCheckBox ShowArcWeights; //should rename as "show layout weights"
  private JCheckBox ShowArcLabels;
  private JCheckBox ShowArrows;
  private ButtonGroup NodeLabelGroup;
  private JCheckBox ShowLabels;
  private JTextField ShowLabelsField;  // show labels based on node size
  private JCheckBox ShowIds;
  private JTextField ShowIdsField;  // show ID's based on node size
  private JCheckBox ShowStats;
  private JCheckBox ShowAxes;
  private JCheckBox GhostSlice;
  private JLabel WindowWidthLabel;
  private JTextField WindowWidth;
  private JLabel WindowHeightLabel;
  private JTextField WindowHeight;
  private JCheckBox FlashNew;
  private JTextField FlashDuration;
  private JCheckBox HideNodes;
  private JCheckBox HideArcs;
  private JPanel mainpanel;

  private JButton OK;

  /**
   * Creates the graphic settings components and sets up the display.
   */
  public GraphicsSettingsDialog(SoniaController cont,
             SoniaLayoutEngine eng,Frame owner,SoniaCanvas canv)
  {
    control = cont;
    engine = eng;
    canvas = canv;
    graphicsDialog = new JDialog(owner,"Graphics Settings",true);
    mainpanel = new JPanel();

    GeneralLabel = new JLabel("General Options:");
    NodesLabel = new JLabel ("Node Options:");
    ArcsLabel = new JLabel ("Arc Options:");

    AntiAlias = new JCheckBox("AntiAlias graphics (slower)",true);
    ArcTrans = new JCheckBox("Arc transparency",true);
    ArcTransField = new JTextField("0.5",3);
    NodeTrans = new JCheckBox("Node transparency",true);
    NodeTransField = new JTextField("0.5",3);
    ArcWidthFactorLabel = new JLabel("Arc width factor");
    ArcWidthFactorField = new JTextField("1.0",3);
    NodeScaleFactorLabel = new JLabel("Node scale factor");
    NodeScaleFactorField = new JTextField("1.0",3);
    ShowArcWeights = new JCheckBox("Show arc weights",false);
    ShowArcLabels = new JCheckBox("Show Arc Labels",false);
    ShowArrows = new JCheckBox("Show arrowheads",true);
    ShowLabels = new JCheckBox("Show labels",true);
    ShowLabelsField = new JTextField("10.0",3);
    ShowIds = new JCheckBox("Show node Ids",false);
    ShowIdsField = new JTextField("10.0",3);
    
    //NodeLabelGroup

    ShowStats = new JCheckBox("Show stats",true);
    ShowAxes = new JCheckBox("Show Axes",false);
    GhostSlice = new JCheckBox("Ghost previous slice");

    WindowHeightLabel = new JLabel("Layout Height");
    WindowWidthLabel = new JLabel("Layout Width");
    WindowHeight = new JTextField(engine.getDisplayHeight()+"",4);
    WindowWidth = new JTextField(engine.getDisplayWidth()+"", 4);
    FlashNew = new JCheckBox("Flash new events",false);
    FlashDuration = new JTextField("0.1",4);
    HideNodes = new JCheckBox("Hide Nodes", false);
    HideArcs = new JCheckBox("Hide Arcs",false);

    OK = new JButton("OK");

    GridBagLayout layout = new GridBagLayout();
   mainpanel.setLayout(layout);
   GridBagConstraints c = new GridBagConstraints();
   c.insets = new Insets(2,5,2,5);
   // add components to the layout GBlayout using constraints
   c.anchor = GridBagConstraints.WEST;
   //first, the colum lables
   c.gridx=0;c.gridy=0;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(GeneralLabel,c);
   c.gridx=2;c.gridy=0;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(NodesLabel,c);
   c.gridx=4;c.gridy=0;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(ArcsLabel,c);
   //general options
   c.gridx=0;c.gridy=1;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(AntiAlias,c);
   c.gridx=0;c.gridy=2;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(ShowStats,c);
  c.gridx=0;c.gridy=3;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
  mainpanel.add(GhostSlice,c);
   c.gridx=0;c.gridy=4;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(FlashNew,c);
   c.gridx=1;c.gridy=4;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(FlashDuration,c);
   c.gridx=0;c.gridy=5;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(WindowWidthLabel, c);
   c.gridx=1;c.gridy=5;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(WindowWidth, c);
   c.gridx=0;c.gridy=6;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(WindowHeightLabel, c);
   c.gridx=1;c.gridy=6;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(WindowHeight,c);
   

   // node options
   c.gridx=2;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(NodeScaleFactorLabel,c);
   c.gridx=3;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(NodeScaleFactorField,c);
   
   c.gridx=2;c.gridy=2;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(NodeTrans,c);
   c.gridx=3;c.gridy=2;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(NodeTransField,c);
   
   c.gridx=2;c.gridy=3;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(ShowLabels,c);
   c.gridx=3;c.gridy=3;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(ShowLabelsField,c);
   c.gridx=2;c.gridy=4;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(ShowIds,c);
   c.gridx=3;c.gridy=4;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(ShowIdsField,c);
   c.gridx=2;c.gridy=5;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(HideNodes,c);
   
   // arc options
   c.gridx=4;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(ArcTrans,c);
   c.gridx=5;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(ArcTransField,c);
   c.gridx=4;c.gridy=2;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(ArcWidthFactorLabel,c);
   c.gridx=5;c.gridy=2;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(ArcWidthFactorField,c);
   c.gridx=4;c.gridy=3;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(ShowArrows,c);
   c.gridx=4;c.gridy=4;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(ShowArcWeights,c);
   c.gridx=4;c.gridy=5;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(HideArcs,c);

   c.gridx=4;c.gridy=6;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(ShowArcLabels,c);



   //c.gridx=0;c.gridy=10;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   //graphicsDialog.add(ShowAxes,c);
   c.gridx=2;c.gridy=6;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   mainpanel.add(OK,c);

   OK.addActionListener(new ActionListener(){
     public void actionPerformed(ActionEvent evt)
     {
       applySettings();
     }
   });
graphicsDialog.getContentPane().add(mainpanel);
  // graphicsDialog.setBackground(Color.lightGray);
   graphicsDialog.setSize(700,400);
 }

 public void showDialog()
 {
   WindowHeight.setText(engine.getDisplayHeight()+"");
   WindowWidth.setText(engine.getDisplayWidth()+"");
   graphicsDialog.show();
   OK.requestFocus();
  }

  /**
   * Transfers the settings from the display checkboxes to the Canvas.
   */
  public void applySettings()
  {
    canvas.setAntiAlias(AntiAlias.isSelected());
    canvas.setArcTrans(ArcTrans.isSelected());
    canvas.setArcTransVal(Float.parseFloat(ArcTransField.getText()));
    canvas.setNodeTrans(NodeTrans.isSelected());
    canvas.setNodeTransVal(Float.parseFloat(NodeTransField.getText()));
    canvas.setArcWidthFact(Float.parseFloat(ArcWidthFactorField.getText()));
    canvas.setShowArrows(ShowArrows.isSelected());
    canvas.setShowArcWeights(ShowArcWeights.isSelected());
    canvas.setShowArcLabels(ShowArcLabels.isSelected());
    canvas.setShowLabels(ShowLabels.isSelected());
    canvas.setShowLabelsVal(Float.parseFloat(ShowLabelsField.getText()));
    canvas.setShowId(ShowIds.isSelected());
    canvas.setShowIdsVal(Float.parseFloat(ShowIdsField.getText()));
    canvas.setShowStats(ShowStats.isSelected());
    canvas.setGhostSlice(GhostSlice.isSelected());
    canvas.setFlashNew(FlashNew.isSelected());
    canvas.setFlashDuration(Double.parseDouble(FlashDuration.getText()));
    canvas.setNodeScaleFact(Float.parseFloat(NodeScaleFactorField.getText()));
    canvas.setHideArcs(HideArcs.isSelected());
    canvas.setHideNodes(HideNodes.isSelected());
    engine.setDisplayWidth(Integer.parseInt(WindowWidth.getText()));
    engine.setDisplayHeight(Integer.parseInt(WindowHeight.getText()));
    graphicsDialog.setVisible(false);
    canvas.repaint();
  }
}
