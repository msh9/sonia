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
 * Displays the controls and parameters for specifying the range of the network
 * data, how to bin it into slices, the aggregation and interpolation techniques,
 * and choosing which layout algorithm will be used.  Also provides the ability
 * to preview the network data and slice parameters in a "phase-space" plot.
 */
public class LayoutSettingsDialog extends Frame implements ActionListener
{
  //private Dialog settingsDialog;
  private SoniaLayoutEngine engine;
  private SoniaController control;

  private PhasePlot timePlot;

  //layout elements
  private Label SliceInfoLabel;
  private Label LayoutTypeLabel;
  private List LayoutType;  //for choosing which kind of layout
  private Label SliceStartLabel; //how to set up slices
  private TextField SliceStart;
  private Label SliceEndLabel;
  private TextField SliceEnd;
  private Label SliceDurLabel;
  private TextField SliceDuration;
  private Label SliceDeltaLabel;
  private TextField SliceDelta;
  private Label AnimateLabel;
  private List AnimateType;

  private Label AggregateLabel;
  private CheckboxGroup SliceAggregation;
  private Checkbox SumTies;
  private Checkbox AvgTies;
  private Checkbox NumTies;

  private Panel layoutOptions;   //for holding elements that are layout specifc
  private Button OK;
  private Button Cancel;
  private Button Plot;

  //holds the defualt values for start and endtimes
  private double layoutStartTime = 0.0;
  private double layoutEndTime = 10.0;

  public LayoutSettingsDialog(SoniaController cont,
                              SoniaLayoutEngine eng,Frame owner)
  {
    control = cont;
    engine = eng;
    this.setFont(cont.getFont());

    LayoutTypeLabel = new Label("layout type:",Label.RIGHT);
    LayoutType = new List(10);
    LayoutType.add("coordinates from original file");
    LayoutType.add("circular layout");
    LayoutType.add("FR layout");
    LayoutType.add("random FR layout");
    LayoutType.add("Rubber-Band FR Layout");
    LayoutType.add("MultiComp KK Layout");
    LayoutType.add("Moody PI layout");
    LayoutType.add("MetricMDS (SVD)?");
    LayoutType.setBackground(Color.white);
    LayoutType.select(5);

    SliceInfoLabel = new Label("Layout Slice Settings:");
    SliceStartLabel = new Label("start time");
    SliceStart = new TextField(layoutStartTime+"",8);
    SliceEndLabel = new Label("end time");
    SliceEnd = new TextField(layoutEndTime+"",8);
    SliceDurLabel = new Label("slice_duration");
    SliceDuration = new TextField("1.0",4);
    SliceDeltaLabel = new Label("slice_delta");
    SliceDelta = new TextField("1.0",4);

    AnimateLabel = new Label("Animation type:");
    AnimateType = new List(2);
    AnimateType.add("none");
    AnimateType.add("cosine animation");
    AnimateType.setBackground(Color.white);
    AnimateType.select(1);

    AggregateLabel = new Label("Within-slice aggregation technique:");
    SliceAggregation = new CheckboxGroup();
    SumTies = new Checkbox("Sum  of i->j ties", false, SliceAggregation);
    AvgTies = new Checkbox("Avg of i->j ties", true, SliceAggregation);
    NumTies = new Checkbox("Number  of i->j ties", false, SliceAggregation);


    OK = new Button("Create Layout");
    Cancel = new Button("Cancel");
    Plot = new Button("Phase Plot...");

    layoutOptions = new Panel();

    GridBagLayout layout = new GridBagLayout();
    this.setLayout(layout);
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(2,5,2,5);
    // add components to the layout GBlayout using constraints
    //buttons
    //slice info
    c.gridx=0;c.gridy=0;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
    this.add(SliceInfoLabel,c);
    //first col labels
    c.gridx=0;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
    this.add(SliceStartLabel,c);
    c.gridx=0;c.gridy=2;
    this.add(SliceEndLabel,c);
    c.gridx=0;c.gridy=3;
    this.add(SliceDurLabel,c);
    c.gridx=0;c.gridy=4;
    this.add(SliceDeltaLabel,c);
    //second col, slice fields
    c.gridx=1;c.gridy=1;
    this.add(SliceStart,c);
    c.gridx=1;c.gridy=2;
    this.add(SliceEnd,c);
    c.gridx=1;c.gridy=3;
    this.add(SliceDuration,c);
    c.gridx=1;c.gridy=4;
    this.add(SliceDelta,c);

    // (interpolation)
    c.gridx=2;c.gridy=0;c.gridwidth=1;
    this.add(AnimateLabel,c);
    c.gridx=2;c.gridy=1;c.gridwidth=1;c.gridheight=2;
    this.add(AnimateType,c);c.gridheight=1;
    c.gridx=2;c.gridy=3;c.gridwidth=1;
    this.add(AggregateLabel,c);
    c.gridx=2;c.gridy=4;
    this.add(AvgTies,c);
    c.gridx=2;c.gridy=5;
    this.add(SumTies,c);
    c.gridx=2;c.gridy=6;
    this.add(NumTies,c);

    //layout type
    c.gridx=4;c.gridy=0;c.gridwidth=1;
    this.add(LayoutTypeLabel,c);
    c.gridx=4;c.gridy=1;c.gridheight=6;
    this.add(LayoutType,c);
    //buttons
    c.gridx=0;c.gridy=6;c.gridheight=1;
    this.add(Plot,c);

    c.gridx=6;c.gridy=4;c.gridheight=1;
    this.add(Cancel,c);

    c.gridx=6;c.gridy=5;
    this.add(OK,c);


    Cancel.addActionListener(this);
    OK.addActionListener(this);
    Plot.addActionListener(this);
    SliceStart.addActionListener(this);
    SliceEnd.addActionListener(this);
    SliceDuration.addActionListener(this);
    SliceDelta.addActionListener(this);


    this.setBackground(Color.lightGray);
    this.setTitle("Layout (Slicing) Settings for "+engine.toString());
    this.setSize(800,300);
    //this.show();
    OK.requestFocus();

  }



  public void showDialog()
  {
    this.show();
  }

  /**
   * Sets the default value to be shown in the layout start field, should be set
   * to the smallest time value in the data.
   * @param start the double default start time
   */
  public void setDataStartDefault(double start)
  {
    layoutStartTime = start;
    SliceStart.setText(layoutStartTime+"");
  }

  /**
  * Sets the default value to be shown in the layout end field, should be set
  * to the largest time value in the data.
  * @param end the double default end time
   */
  public void setDataEndDefault(double end)
 {

    layoutEndTime=end;
    SliceEnd.setText(layoutEndTime+"");
  }



  //ACTION LISTENER  //figures out what user did and calls apropriate method
  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getActionCommand().equals("Create Layout"))
    {

      //take care of all the settings
      //make the right kinds of layout
      NetLayout theLayout;
      if (LayoutType.getSelectedItem().equals("circular layout"))
      {
        theLayout = new CircleLayout(control,engine);
      }
      else if (LayoutType.getSelectedItem().equals("FR layout"))
      {
        theLayout = new FRLayout(control,engine);
      }
      else if (LayoutType.getSelectedItem().equals("random FR layout"))
      {
        theLayout = new RandomFRLayout(control,engine);
      }
      else if (LayoutType.getSelectedItem().equals("Rubber-Band FR Layout"))
      {
        theLayout = new RubBandFRLayout(control,engine);
      }
      else if (LayoutType.getSelectedItem().equals( "MultiComp KK Layout"))
      {
        theLayout = new MultiCompKKLayout(control,engine);
      }
      else if (LayoutType.getSelectedItem().equals("Moody PI layout"))
      {
        theLayout = new PILayout(control,engine);
      }
      else if (LayoutType.getSelectedItem().equals("MetricMDS (SVD)?"))
      {
        theLayout = new MetricMDSLayout(control,engine);
      }
      else
      {
        theLayout = new OrigCoordLayout(control,engine);
      }
      //make the right kind of coordinate interpolator
      CoordInterpolator interpolator;
      if (AnimateType.getSelectedItem().equals("cosine animation"))
      {
        interpolator = new CosineInterpolation();
      }
      else
      {
        interpolator = new NoInterpolation();
        engine.setInterpFrames(0);
      }

      //figure out what kind of aggreation will be used
      Checkbox selected  = SliceAggregation.getSelectedCheckbox();
      int aggregateType = 0;   //sum ties
      if (selected.equals(AvgTies))
      {
        aggregateType = 1;
      }
      else if (selected.equals(NumTies))
      {
        aggregateType = 2;
      }

      //tell the engine to setup (get slices from netData, etc
      engine.setupLayout(Double.parseDouble(SliceStart.getText()),
                         Double.parseDouble(SliceEnd.getText()),
                         Double.parseDouble(SliceDuration.getText()),
                         Double.parseDouble(SliceDelta.getText()),
                         aggregateType,
                         theLayout,interpolator);

      this.hide();


      //will this distroyitself, or does it need to be explicit?
      //this.finalize();
    }
    else if(evt.getActionCommand().equals("Cancel"))
    {
      this.hide();
      if (timePlot != null)
      {
        timePlot.hide();
        timePlot = null;
      }
    }
    else if(evt.getSource().equals(Plot))
    {
      if (timePlot == null)
      {
        engine.showPhasePlot();
        timePlot = engine.getPhasePlot();
      }
    }
    else//something happend, so lets assume it was a text area and update the display
    {
      if (timePlot != null)
      {
        timePlot.repaint();
      }
    }

  }

  public double getSliceStart()
  {
    return Double.parseDouble(SliceStart.getText());
  }
  public double getSliceEnd()
  {
    return Double.parseDouble(SliceEnd.getText());
  }
  public double getSliceDuration()
 {
   return Double.parseDouble(SliceDuration.getText());
  }
  public double getSliceDelta()
 {
   return Double.parseDouble(SliceDelta.getText());
  }

}
