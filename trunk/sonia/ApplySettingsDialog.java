package sonia;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.ArrayList;
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
 * Provides the controls for specifing layout parameters and how the layots will
 * be applied.  Includes starting coords, hondeling of isolates, animating redraws,
 * post-processing, and options to apply to all the remaining slices.
 */
public class ApplySettingsDialog implements ActionListener
{
  private Dialog settingsDialog;
  private SoniaLayoutEngine engine;
  private SoniaController control;
  private NetLayout layoutAlgorithm;
  private HashMap layoutProperties = new HashMap(); //holds items added by specific layouts

  //container dynamically added layout elements
  private ArrayList propComponents = new ArrayList();
  private Label PropertiesLabel;

  //layout elements
  private Label SliceInfoLabel; //shows the laytout info, type of layout

  private Label StartCoordLabel;
  private CheckboxGroup StartCoords = new CheckboxGroup();
  private Checkbox FromOrigFile;
  private Checkbox Randomize;
  private Checkbox Circle;
  private Checkbox FromPrevSlice;
  private Checkbox Current;

  private Label IsolatesLabel;
  private CheckboxGroup Isolates = new CheckboxGroup();
  private Checkbox Ignore;
  private Checkbox PinToEdge;
  private Checkbox PinToCircle;
  private Checkbox UseOrigCoords;
  private Checkbox PinPrevious;

  private Label InterpLabel;
  private Checkbox Average;
  private Label PrevLabel;
  private Label FutureLabel;
  private TextField PrevField;
  private TextField FutureField;

  private Label CoolingLabel;
  private Checkbox CurrentCooling;
  private Checkbox Repaint;
  private TextField RepaintN;

  private Label DisplayLabel;
  private Checkbox CircleRestrict;
  private Checkbox Recenter;
  private Checkbox EndRecenter;
  private Checkbox EndBarycenter;
  private Checkbox Rescale;
  private Checkbox IsolateExclude;



  private Button Apply;
  private Button Cancel;
  private Button MultiApply;
  private Checkbox ErrorStop;

  //static convience fields
  public static int ORIG_FILE = 0;
  public static int RANDOMIZE = 1;
  public static int CIRCLE  = 2;
  public static int PREV_SLICE = 3;
  public static int CURRENT = 4;

  //isolates
  public static int PIN_CIRCLE = 0;
  public static int PIN_EDGE = 1;
  public static int PIN_ORIG = 2;
  public static int IGNORE = 3;
  public static int PIN_PREVIOUS = 4;

  /**
   * Creates a dilog box with settings to control the various stages of the
   * layout process, including some algorithm specific parameters, which it
   * adds to the window by communicating with the layout algorithm.
   * @param cont the main soniaController
   * @param eng the layout engine which the layout belongs to
   * @param owner the parent frame (the layout window)
   * @param layoutAlg the layout algorithm
   */
  public ApplySettingsDialog(SoniaController cont,
             SoniaLayoutEngine eng,Frame owner,NetLayout layoutAlg)
  {
    control = cont;
    engine = eng;
    layoutAlgorithm = layoutAlg;
    settingsDialog = new Dialog(owner,layoutAlgorithm.getLayoutType()+" Settings",true);
    settingsDialog.setFont(control.getFont());
    SliceInfoLabel = new Label("Apply "+layoutAlgorithm.getLayoutType()+" to slice# "
                               +engine.getCurrentSliceNum()+" ["+
                               engine.getCurrentSlice().getSliceStart()+
                               "-"+engine.getCurrentSlice().getSliceEnd()+
                               "]");
    StartCoordLabel = new Label("Starting Coordinates");
    FromOrigFile = new Checkbox("From original file",StartCoords,false);
    Randomize= new Checkbox("Randomize",StartCoords,true);
    Circle= new Checkbox("Circular",StartCoords,false);
    FromPrevSlice = new Checkbox("From previous slice",StartCoords,false);
    Current = new Checkbox("From current coords",StartCoords,false);

    IsolatesLabel = new Label("Position Isolates");
    Ignore = new Checkbox("Ignore",Isolates,true);
    PinToEdge= new Checkbox("Pin to edge",Isolates,false);
    PinToCircle = new Checkbox("Pin to Circle",Isolates,false);
    UseOrigCoords = new Checkbox("Use coords from org. file",Isolates,false);
    PinPrevious = new Checkbox("Pin to previous",Isolates,false);

    InterpLabel = new Label("Coordinate Interpolation");
    Average = new Checkbox("Avg. of slices",false);
    PrevLabel = new Label("previous");
    FutureLabel = new Label("future");
    PrevField = new TextField("0",2);
    FutureField = new TextField("0",2);

    CoolingLabel = new Label("Iteration Control");
    CurrentCooling = new Checkbox("Use current cooling schedule",true);
    Repaint = new Checkbox("Repaint on Nth layout pass",false);
    RepaintN = new TextField("1",2);
    //add maxes passes field and (pI control)

    DisplayLabel = new Label("Transformations");

    Recenter = new Checkbox("Recenter during layout",false);
    EndRecenter = new Checkbox("Recenter when finished",true);
    EndBarycenter = new Checkbox("Barycenter when finished",false);
    Rescale = new Checkbox("Rescale layout when finished",false);
    IsolateExclude = new Checkbox("Exclude Isolates in transform",false);

    CircleRestrict = new Checkbox("Restrict to bounding circle",false);


    Apply = new Button("Apply");
    Cancel = new Button("Cancel");
    MultiApply = new Button("Apply to Remaining");
    ErrorStop = new Checkbox("Stop on Layout Errors");

    //get any addl components layouts might want to add...
    layoutAlgorithm.setupLayoutProperties(this);
    PropertiesLabel = new Label("Algorithm-Specific Properties");

    GridBagLayout layout = new GridBagLayout();
    settingsDialog.setLayout(layout);
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(2,5,2,5);
    // add components to the layout GBlayout using constraints

    //starting coordinates
    c.anchor = c.WEST;
    c.gridx=0;c.gridy=0;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(StartCoordLabel,c);
    c.gridx=0;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(FromOrigFile,c);
    c.gridx=0;c.gridy=2;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(FromPrevSlice,c);
    c.gridx=0;c.gridy=3;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(Circle,c);
    c.gridx=0;c.gridy=4;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(Randomize,c);
    c.gridx=0;c.gridy=5;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(Current,c);

    //Isolates
    c.gridx=1;c.gridy=0;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(IsolatesLabel,c);
    c.gridx=1;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(Ignore,c);
    c.gridx=1;c.gridy=2;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(PinToEdge,c);
    c.gridx=1;c.gridy=3;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(PinToCircle,c);
    c.gridx=1;c.gridy=4;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(UseOrigCoords,c);
    c.gridx=1;c.gridy=5;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(PinPrevious,c);

    //Interpolation
    /*
    c.gridx=2;c.gridy=0;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(InterpLabel,c);
    c.gridx=2;c.gridy=1;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(Average,c);
    c.gridx=2;c.gridy=2;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(PrevLabel,c);
    c.gridx=3;c.gridy=2;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(PrevField,c);
    c.gridx=2;c.gridy=3;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(FutureLabel,c);
    c.gridx=3;c.gridy=3;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(FutureField,c);
    */
    //iteration controll (same colum)
    c.gridx=2;c.gridy=0;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(CoolingLabel,c);
    c.gridx=2;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(Repaint,c);
    c.gridx=3;c.gridy=1;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(RepaintN,c);
    c.gridx=2;c.gridy=2;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(CurrentCooling,c);

    //display options
    c.gridx=4;c.gridy=0;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(DisplayLabel,c);
    c.gridx=4;c.gridy=1;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(Recenter,c);
    c.gridx=4;c.gridy=2;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(EndRecenter,c);
    c.gridx=4;c.gridy=3;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(EndBarycenter,c);
    c.gridx=4;c.gridy=4;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(Rescale,c);
    c.gridx=4;c.gridy=5;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(IsolateExclude,c);
    c.gridx=4;c.gridy=6;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
    //settingsDialog.add(CircleRestrict,c);


    //slice info
    c.anchor = c.CENTER;
    c.gridx=2;c.gridy=7;c.gridwidth=3;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(SliceInfoLabel,c);

    //buttons

    c.gridx=4;c.gridy=8;c.gridwidth=1;c.gridheight=1;
    settingsDialog.add(Cancel,c);
    c.gridx=2;c.gridy=8;
    settingsDialog.add(Apply,c);
    c.gridx=2;c.gridy=9;
    settingsDialog.add(MultiApply,c);
    c.gridx=3;c.gridy=9;
    settingsDialog.add(ErrorStop,c);

    //add any addl compondents from the layout
    c.gridx=0;c.gridy=6;c.gridwidth=2;c.gridheight=1;c.weightx=0;c.weighty=0;
    settingsDialog.add(PropertiesLabel,c);
    //first colum (alterneat entries in propcomponents)
    int compIndex = 0;
    for (int i = 0; i<(propComponents.size()/2); i++)
    {
      c.anchor = c.EAST;
      c.gridx=0;c.gridy=7+i;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
      settingsDialog.add((Component)propComponents.get(compIndex),c);
      compIndex+=2;
    }
    //2nd colum (alternet entries in propcomponents)
    compIndex = 1;
    for (int i = 0; i<(propComponents.size()/2); i++)
    {
      c.anchor = c.WEST;
      c.gridx=1;c.gridy=7+i;c.gridwidth=1;c.gridheight=1;c.weightx=0;c.weighty=0;
      settingsDialog.add((Component)propComponents.get(compIndex),c);
      compIndex+=2;
    }

    Cancel.addActionListener(this);
    Apply.addActionListener(this);
    MultiApply.addActionListener(this);

    settingsDialog.setBackground(Color.lightGray);
    settingsDialog.setSize(850,400);
  }

  /**
   * displays the dialog on screen
   */
  public void showDialog()
  {
    settingsDialog.show();
    Apply.requestFocus();
  }

  /**
   * called by the layout algorithm during setup so that it can add parameters
   * to the dilog box.
   * @param name the string name of the prameter to display and to use as the key
   * @param value the defualt double value of the parameter
   */
  public void addLayoutProperty(String name, double value)
  {
    //make new GUI objects
    Label PropLabel = new Label(name);
    TextField PropField = new TextField(value+"",3);
    PropField.setName(name);
    //add the pair to the Hashmap
    layoutProperties.put(name, PropField);
    propComponents.add(PropLabel);
    propComponents.add(PropField);
  }

  /**
   * sets the algorithm specific layout pramter to the passed value, after
   * checking that there is a parameter corresponding to name.  Will spit out an
   * error (TO CONSOLE, SHOULD FIX THIS?) if there is no param.
   * @param name the string name of the paramter to set
   * @param value the double value to set it to
   */
  public void setLayoutProperty(String name, double value)
  {
    //make sure it exists
    if (layoutProperties.keySet().contains(name))
    {
      //get the corresponding text field
      TextField propField = (TextField)layoutProperties.get(name);
      //set its text to the value
      propField.setText(value+"");
    }
    else
    {
      System.out.println("unable to match property to name "+name);
    }
  }

  /**
   * gets the user entered value of the algorithm specific layout property, if
   * name matches one of the previously setup parameters.  Otherwise spits an
   * error to console.
   * @param name a string corresponding to the name of the property
   */
  public double getLayoutProperty(String name)
  {
    double value = 0.0;
    //make sure it exists
    if (layoutProperties.keySet().contains(name))
    {
      //find the component with matching name
      TextField propField = (TextField)layoutProperties.get(name);
      value = Double.parseDouble(propField.getText());
    }
    else
    {
      System.out.println("unable to match property to name "+name);
    }
    return value;
  }


  /**
  * gets all the parameters from the settings dialog and sets them in the
  * LayoutEngine
  * */
  public void setValuesFromDialog()
  {
    //repaint
    if (isRepaint())
    {
      engine.setUpdatesN(Integer.parseInt(RepaintN.getText()));
    }
    else
    {
      engine.setShowUpdates(false);
    }
  }


  /**
   * returns the int corresponding to the setting for the starting coordinates
   * (where nodes are positioned before algorithm runs). one of the static ints
   * PREV_SLICE - use the coordinates from the previous slice, CIRCLE - postion
   * the nodes in a circle to start, ORIG_FILE - use the coordinates from the
   * node record, CURRENT - use the current screen coordinates, RANDOMIZE -
   * randomly position the nodes.
   */
  public int getStartCoordCode()
  {
    int returnInt;
    if (StartCoords.getSelectedCheckbox().equals(FromPrevSlice))
    {
      returnInt = PREV_SLICE;
    }
    else if (StartCoords.getSelectedCheckbox().equals(Circle))
    {
      returnInt = CIRCLE;
    }
    else if (StartCoords.getSelectedCheckbox().equals(FromOrigFile))
    {
      returnInt = ORIG_FILE;
    }
    else if (StartCoords.getSelectedCheckbox().equals(Current))
    {
      returnInt = CURRENT;
    }
    else
    {
      returnInt = RANDOMIZE;
    }
    return returnInt;
  }

  /**
   * returns an int indicating the setting for how to handle the position of
   * isolated nodes.  IGNORE - leave them where they are.  PIN_CIRCLE - position
   * them in a ring on the layout.  PIN_EDGE - position them in a line on the
   * bottom of the layout.  PIN_ORIG - use the original coordinates stored in
   * the nodes.  PIN_PREVIOUS - use the position from the previous slice.
   */
  public int getIsolatesCode()
  {
    int returnInt = IGNORE;
    if (Isolates.getSelectedCheckbox().equals(PinToCircle))
    {
      returnInt = PIN_CIRCLE;
    }
    else if (Isolates.getSelectedCheckbox().equals(PinToEdge))
    {
      returnInt = PIN_EDGE;
    }
    else if (Isolates.getSelectedCheckbox().equals(UseOrigCoords))
    {
      returnInt = PIN_ORIG;
    }
    else if (Isolates.getSelectedCheckbox().equals(PinPrevious))
    {
      returnInt = PIN_PREVIOUS;
    }
    return returnInt;
  }

  /**
   * should the layout be rescaled when finsihed?
   */
  public boolean isRescale()
  {
    return Rescale.getState();
  }

  /**
   * should isolates be excluded when rescaling? (often used if the isolates are
   * being positioned seperately)
   */
  public boolean isIsolateExclude()
  {
    return IsolateExclude.getState();
  }

  /**
   * restrict nodes to a bounding circle?  (only works with FR, and not very well)

   */
  public boolean isCircleRestrict()
  {
    return CircleRestrict.getState();
  }

  /**
   * recenter the layout after each pass of the algorithm (so that it won't
   * drift off screen if you are watching the redraws)
   */
  public boolean isRecenter()
  {
    return Recenter.getState();
  }

  /**
   * recenter the layout (max, min) when the algorithm is finished?
   */
  public boolean isEndRecenter()
 {
   return EndRecenter.getState();
  }

  /**
   * recenter the layout the average of the nodes' coordinates when finished?
   */

  public boolean isEndBarycenter()
  {
    return EndBarycenter.getState();
  }

  /**
   * true if the layout is going to be redrawn while algorithm is running
   */
  public boolean isRepaint()
  {
    return Repaint.getState();
  }

  /**
   * if true, the "apply to remaining" thread will stop if a layout has an erorr,
   *  other wise it will just go on to the next layout
   */
  public boolean isStopOnError()
  {
    return ErrorStop.getState();
  }

  /**
   * returns an integer which controls how frequently the network will be
   * redrawn while the layout is in process (redraw every Nth pass)
   */
  public int getRepaintN()
  {
    return Integer.parseInt(RepaintN.getText());
  }

  /**
   * returns a string summarizing the layout settings to record in the log
   */
  public String getSummaryString()
  {
    String layoutParams = layoutAlgorithm.getLayoutInfo();
    String startingPos;
    String isolates;
    String modifications ="";
    //get a string for layout params

    //get a string for starting
    int startPos = getStartCoordCode();
    startingPos = "from ORIGINAL FILE coordinates";
    if(startPos == RANDOMIZE) startingPos = "from RANDOM coordinates";
    if(startPos == CIRCLE) startingPos = "from CIRCULAR coordinates";
    if(startPos == PREV_SLICE) startingPos = "from PREVIOUS SLICE coordinates";
    if(startPos == CURRENT) startingPos = "from CURRENT display coordinates";


    //get a string for isolates
    int isoType = getIsolatesCode();
    isolates = "not adjusted";
    if(isoType == PIN_CIRCLE) isolates = "pinned to CIRCLE";
    if(isoType == PIN_EDGE) isolates = "pinned to EDGE";
    if(isoType == PIN_ORIG) isolates = "pinned to ORIGINAL FILE coords";
    if(isoType == PIN_PREVIOUS) isolates = "pinned to PREVIOUS SLICE";


    //get a string for afterlayout modifications
    if (isCircleRestrict())
    {
      modifications += "restricted to circle,";
    }
    if (isEndRecenter())
    {
      modifications += "recenterd when finished,";
    }
    if (isRecenter())
    {
      modifications += "adjusted to barycenter during layout,";
    }
    if (isRescale())
    {
      modifications += "rescaled to fit,";
      if (isIsolateExclude())
      {
        modifications += "isolates scaled independently,";
      }
    }
    String summary = "Layout Type:"+layoutAlgorithm.getLayoutType()+"\n"+
                     "Layout Parameters:"+layoutParams+"\n"+
                     "Starting Positions:"+startingPos+"\n"+
                     "Isolate Treatment:" +isolates+"\n"+
                     "Modifications:"+modifications;

    return summary;
  }

  //ACTION LISTENER
  /**
   * figures out what user did and calls apropriate method
   * @param evt the ActionEvent to determine the source of the event
   */
  public void actionPerformed(ActionEvent evt)
  {
    if(evt.getSource().equals(Cancel))
    {
      settingsDialog.hide();
    }
    else
    {
      //read the values from the dialog and set them in engine
      setValuesFromDialog();

      //now figure out if is apply or multi
      if (evt.getSource().equals(Apply))
      {
        engine.applyLayoutTo(engine.getCurrentSlice());
        settingsDialog.hide();
      }
      else if(evt.getSource().equals(MultiApply))
      {
        engine.applyLayoutToRemaining();
        settingsDialog.hide();
      }
    }
  }
}
