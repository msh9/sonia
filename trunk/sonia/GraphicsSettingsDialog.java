package sonia;

import java.awt.*;
import java.awt.event.*;

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
  private Dialog graphicsDialog;
  private SoniaLayoutEngine engine;
  private SoniaController control;
  private SoniaCanvas canvas;

  private Label GeneralLabel;
  private Label ArcsLabel;
  private Label NodesLabel;
  private Checkbox AntiAlias;
  private Checkbox ArcTrans;
  private TextField ArcTransField;
  private Label ArcWidthFactorLabel;
  private TextField ArcWidthFactorField;
  private Label NodeScaleFactorLabel;
  private TextField NodeScaleFactorField;
  private Checkbox ShowArcWeights; //should rename as "show layout weights"
  private Checkbox ShowArcLabels;
  private Checkbox ShowArrows;
  private CheckboxGroup NodeLabelGroup;
  private Checkbox ShowLabels;
  private Checkbox ShowIds;
  private Checkbox ShowStats;
  private Checkbox ShowAxes;
  private Checkbox GhostSlice;
  private Label WindowWidthLabel;
  private TextField WindowWidth;
  private Label WindowHeightLabel;
  private TextField WindowHeight;
  private Checkbox FlashNew;
  private TextField FlashDuration;
  private Checkbox HideNodes;
  private Checkbox HideArcs;

  private Button OK;

  /**
   * Creates the graphic settings components and sets up the display.
   */
  public GraphicsSettingsDialog(SoniaController cont,
             SoniaLayoutEngine eng,Frame owner,SoniaCanvas canv)
  {
    control = cont;
    engine = eng;
    canvas = canv;
    graphicsDialog = new Dialog(owner,"Graphics Settings",true);

    GeneralLabel = new Label("General Options:");
    NodesLabel = new Label ("Node Options:");
    ArcsLabel = new Label ("Arc Options:");

    AntiAlias = new Checkbox("AntiAlias graphics (slower)",true);
    ArcTrans = new Checkbox("Arc transparency",true);
    ArcTransField = new TextField("0.5",3);
    ArcWidthFactorLabel = new Label("Arc width factor");
    ArcWidthFactorField = new TextField("1.0",3);
    NodeScaleFactorLabel = new Label("Node scale factor");
    NodeScaleFactorField = new TextField("1.0",3);
    ShowArcWeights = new Checkbox("Show arc weights",false);
    ShowArcLabels = new Checkbox("Show Arc Labels",false);
    ShowArrows = new Checkbox("Show arrowheads",true);
    ShowLabels = new Checkbox("Show labels",true);
    ShowIds = new Checkbox("Show node Ids",false);
    
    //NodeLabelGroup

    ShowStats = new Checkbox("Show stats",true);
    ShowAxes = new Checkbox("Show Axes",false);
    GhostSlice = new Checkbox("Ghost previous slice");

    WindowHeightLabel = new Label("Layout Height");
    WindowWidthLabel = new Label("Layout Width");
    WindowHeight = new TextField(engine.getDisplayHeight()+"",4);
    WindowWidth = new TextField(engine.getDisplayWidth()+"", 4);
    FlashNew = new Checkbox("Flash new events",false);
    FlashDuration = new TextField("0.1",4);
    HideNodes = new Checkbox("Hide Nodes", false);
    HideArcs = new Checkbox("Hide Arcs",false);

    OK = new Button("OK");

    GridBagLayout layout = new GridBagLayout();
   graphicsDialog.setLayout(layout);
   GridBagConstraints c = new GridBagConstraints();
   c.insets = new Insets(2,5,2,5);
   // add components to the layout GBlayout using constraints
   c.anchor = c.WEST;
   //first, the colum lables
   c.gridx=0;c.gridy=0;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(GeneralLabel,c);
   c.gridx=2;c.gridy=0;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(NodesLabel,c);
   c.gridx=4;c.gridy=0;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(ArcsLabel,c);
   //general options
   c.gridx=0;c.gridy=1;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(AntiAlias,c);
   c.gridx=0;c.gridy=2;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
  graphicsDialog.add(ShowStats,c);
  c.gridx=0;c.gridy=3;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(GhostSlice,c);
   c.gridx=0;c.gridy=4;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(FlashNew,c);
   c.gridx=1;c.gridy=4;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(FlashDuration,c);
   c.gridx=0;c.gridy=5;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(WindowWidthLabel, c);
   c.gridx=1;c.gridy=5;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(WindowWidth, c);
   c.gridx=0;c.gridy=6;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(WindowHeightLabel, c);
   c.gridx=1;c.gridy=6;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(WindowHeight,c);
   

   // node options
   c.gridx=2;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(NodeScaleFactorLabel,c);
   c.gridx=3;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(NodeScaleFactorField,c);
   c.gridx=2;c.gridy=2;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(ShowLabels,c);
   c.gridx=2;c.gridy=3;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(ShowIds,c);
   c.gridx=2;c.gridy=4;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(HideNodes,c);
   // arc options
   c.gridx=4;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(ArcTrans,c);
   c.gridx=5;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(ArcTransField,c);
   c.gridx=4;c.gridy=2;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(ArcWidthFactorLabel,c);
   c.gridx=5;c.gridy=2;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(ArcWidthFactorField,c);
   c.gridx=4;c.gridy=3;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(ShowArrows,c);
   c.gridx=4;c.gridy=4;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(ShowArcWeights,c);
   c.gridx=4;c.gridy=5;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(HideArcs,c);

   c.gridx=4;c.gridy=6;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(ShowArcLabels,c);



   //c.gridx=0;c.gridy=10;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   //graphicsDialog.add(ShowAxes,c);
   c.gridx=2;c.gridy=6;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
   graphicsDialog.add(OK,c);

   OK.addActionListener(new ActionListener(){
     public void actionPerformed(ActionEvent evt)
     {
       applySettings();
     }
   });

   graphicsDialog.setBackground(Color.lightGray);
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
    canvas.setAntiAlias(AntiAlias.getState());
    canvas.setArcTrans(ArcTrans.getState());
    canvas.setArcTransVal(Float.parseFloat(ArcTransField.getText()));
    canvas.setArcWidthFact(Float.parseFloat(ArcWidthFactorField.getText()));
    canvas.setShowArrows(ShowArrows.getState());
    canvas.setShowArcWeights(ShowArcWeights.getState());
    canvas.setShowArcLabels(ShowArcLabels.getState());
    canvas.setShowLabels(ShowLabels.getState());
    canvas.setShowId(ShowIds.getState());
    canvas.setShowStats(ShowStats.getState());
    canvas.setGhostSlice(GhostSlice.getState());
    canvas.setFlashNew(FlashNew.getState());
    canvas.setFlashDuration(Double.parseDouble(FlashDuration.getText()));
    canvas.setNodeScaleFact(Float.parseFloat(NodeScaleFactorField.getText()));
    canvas.setHideArcs(HideArcs.getState());
    canvas.setHideNodes(HideNodes.getState());
    engine.setDisplayWidth(Integer.parseInt(WindowWidth.getText()));
    engine.setDisplayHeight(Integer.parseInt(WindowHeight.getText()));
    graphicsDialog.hide();
    canvas.repaint();
  }
}
