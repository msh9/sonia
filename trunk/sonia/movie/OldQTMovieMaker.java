package sonia.movie;

import java.io.*;
import java.awt.*;

import javax.swing.JFrame;
import javax.swing.JPanel;

import quicktime.qd.*;
import quicktime.*;
import quicktime.std.*;
import quicktime.io.*;
import quicktime.std.image.*;
import quicktime.std.movies.*;
import quicktime.std.movies.media.*;
import quicktime.util.*;

import quicktime.app.display.*;
import quicktime.app.image.*;
import quicktime.app.QTFactory;
import sonia.SoniaCanvas;
import sonia.SoniaController;
import sonia.SoniaLayoutEngine;
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
 * Sets up connection to QuickTime for Java libraries to export the network
 * animation as a movie.  Unfortunately, the libraries are only availible for
 * windows and macs.  Even worse, apple broke the mac version in the recent java
 * releases, so if you want it to work you will need to run under java 1.3.1.
 * The whole thing is pretty kludgy to begin with, needs to be redone, and perhaps
 * support for Flash files so we could do vector based animation.
 * <BR><BR>
 *
 * <BR><BR>
 * THIS PACKAGE REQUIRES THE QUICKTIME FOR JAVA LIBRARIES AVAILIBLE FORM APPLE COMPUTER
 */
public class OldQTMovieMaker extends JFrame implements StdQTConstants, Errors, MovieMaker
{
  private QTImageDrawer imageDrawer;
  private QTCanvas QCanvas;
  private QTFile outFile;
  private NetPainter painter;
  private int numFrames = 60;
  private int width;
  private int height;

  private Movie theMovie;
  private QDRect clipRect;
  private QDGraphics quickDraw;
  private RawEncodedImage compressedImage;
  private VideoMedia vidMedia;
  private Track vidTrack;
  private QTHandle imageHandle;
  private CSequence seq;
  private ImageDescription desc;

  private SoniaLayoutEngine engine;
  private SoniaController control;
  private String fileName = null;
  private boolean exporting = false;
  

  public OldQTMovieMaker(SoniaController cont, SoniaLayoutEngine eng,String file)
  {
    control = cont;
    engine = eng;
    fileName = file;
  }

  /* (non-Javadoc)
 * @see sonia.MovieMaker#setupMovie(sonia.SoniaCanvas, int)
 */
  public void setupMovie(SoniaCanvas canvas,int frames) throws Exception
  {
    //control.showStatus("starting movie export...");
  //  try
   // {
      //
      // show save-as dialog, create movie file & empty movie
      //
    	//check if we already have a name
    if (fileName == null){
      FileDialog dialog = new FileDialog (new Frame(), "Save Network Movie As...",
          FileDialog.SAVE);

      dialog.show();
      if(dialog.getFile() == null)
      {
       //dont do anything
    	  return;
      }
      else
      {
    	  fileName = dialog.getDirectory()+dialog.getFile();
      }
    }
      if (fileName != null){
      outFile = new QTFile(fileName);

exporting = true;
        QTSession.open();  //links to c stubbs?
        width = engine.getDisplayWidth(); //need to add pads?
        height = engine.getDisplayHeight();
        numFrames = frames;

        //frame stuff so we can visually debug
       QCanvas = new QTCanvas(QTCanvas.kInitialSize, 0.5F, 0.5F);
       JPanel holder = new JPanel();
       holder.setSize(width,height);
       holder.add(QCanvas);
       this.add(holder);
       this.setBackground(Color.black);
       this.setLocation(300,300);
       this.pack();
       this.setSize(width+50,height+50);
       this.setTitle("Exporting movie to "+outFile.getName()+" ...");
        this.show();

        painter = new NetPainter(canvas);
        imageDrawer = new QTImageDrawer(painter,
                                        new Dimension(width,height),
                                        Redrawable.kMultiFrame);
        QCanvas.setClient (imageDrawer, true);
        imageDrawer.setRedrawing(true);//so it will get new data from image source
        theMovie = Movie.createMovieFile (outFile,
            kMoviePlayer,
            createMovieFileDeleteCurFile | createMovieFileDontCreateResFile);
        int kNoVolume	= 0;
        int kVidTimeScale = 600;

        vidTrack = theMovie.addTrack (width, height, kNoVolume);
        vidMedia = new VideoMedia (vidTrack, kVidTimeScale);

        //begin QT editing
        vidMedia.beginEdits();
        clipRect = new QDRect (width, height);
        quickDraw = new QDGraphics (clipRect);


        int size = QTImage.getMaxCompressionSize (quickDraw,
            clipRect,
            8,//quickDraw.getPixMap().getPixelSize(),  the color depth
            codecHighQuality,   //recomended quality
            kAnimationCodecType,
            CodecComponent.bestFidelityCodec);   //the compressor
        imageHandle = new QTHandle (size, true);
        imageHandle.lock();
        compressedImage = RawEncodedImage.fromQTHandle(imageHandle);
       seq = new CSequence (quickDraw,
                                       clipRect,
                                       8,//quickDraw.getPixMap().getPixelSize(),
                                       kAnimationCodecType,
                                     //  CodecComponent.bestCompressionCodec,
                                       CodecComponent.bestFidelityCodec,
                                       codecHighQuality,
                                       codecHighQuality,
                                       numFrames,	//1 key frame
                                       null, //cTab,
                                       0);

        desc = seq.getDescription();

        //redraw first...
        painter.paint(QCanvas.getGraphics());
        imageDrawer.redraw(null);

        imageDrawer.setGWorld (quickDraw);
        imageDrawer.setDisplayBounds (clipRect);
      }
   // }
   // catch (Exception e)
   // {
      //debug
     // control.showError("Error saving quicktime movie"+e.toString());
     // e.printStackTrace();
   // }
  }

  /* (non-Javadoc)
 * @see sonia.MovieMaker#captureImage()
 */
 public void captureImage()
 {
   //should check that movie is setup
   if (theMovie != null)
   {
     try
     {
   //get the renderslices and everything ready
   //painter.setUpNextFrame();
   painter.paint(QCanvas.getGraphics());
   imageDrawer.redraw(null);
   CompressedFrameInfo info = seq.compressFrame(quickDraw,
       clipRect,
       codecFlagUpdatePrevious,
       compressedImage);
   boolean isKeyFrame = info.getSimilarity() == 0;
   //System.out.println ("f#:" + curSample + ",kf=" + isKeyFrame + ",sim=" + info.getSimilarity());
   vidMedia.addSample (imageHandle,
                       0, // dataOffset,
                       info.getDataSize(),
                       engine.getFrameDelay(), // frameDuration in 600ths of seconds, 60/600 = 1/10 of a second, desired time per frame
                       desc,
                       1, // one sample
                       (isKeyFrame ? 0 : mediaSampleNotSync)); // no flags
     }
     catch (Exception e)
     {
       control.showError("problem adding slice frame "+e.toString());
       e.printStackTrace();
     }
   }
 }

 /* (non-Javadoc)
 * @see sonia.MovieMaker#finishMovie()
 */
public void finishMovie()
 {
   try
   {
     //print out ImageDescription for the last video media data ->
     //this has a sample count of 1 because we add each "frame" as an individual media sample
     control.log(desc.toString()+"\n");

     //end QT editing
     vidMedia.endEdits();

     int kTrackStart	= 0;
     int kMediaTime 	= 0;
     int kMediaRate	= 1;
     vidTrack.insertMedia (kTrackStart, kMediaTime, vidMedia.getDuration(), kMediaRate);


     //
     // save movie to file
     //
     OpenMovieFile outStream = OpenMovieFile.asWrite (outFile);
     theMovie.addResource( outStream, movieInDataForkResID, outFile.getName() );
     outStream.close();

     QTSession.close();
     control.showStatus("Movie saved to file "+outFile.toString());
     exporting=false;
     this.setVisible(false);
     this.dispose();
   }
   catch (Exception e)
   {
     control.showError("ERROR with movie export "+e.toString());
     e.printStackTrace();
     exporting = false;
     this.setVisible(false);
     this.dispose();
   }
 }
 
 /* (non-Javadoc)
 * @see sonia.MovieMaker#isExporting()
 */
public boolean isExporting(){
	 return exporting;
 }


  class NetPainter implements Paintable
  {
    private SoniaCanvas canvas;
    private int frameNum;

    public NetPainter(SoniaCanvas canv)
    {
      canvas=canv;
    }

    //implments quicktimes paing commands
    public Rectangle[] paint(Graphics g)
    {
      Rectangle[] clipRegions = new Rectangle[1];
      Rectangle clipRect = new Rectangle(0,0,width,height);
      clipRegions[0]=clipRect;
      //calls the original sonia canvas do draw the net
      //need to update the display first, 'cause paint will just redraw the
      //previous image
      canvas.updateDisplay(g,canvas.isGhostSlice());
      canvas.paint(g);

      return clipRegions;
    }

    public void newSizeNotified(QTImageDrawer drawer, Dimension d)
    {
       width=d.width;
       height=d.height;
    }

  }

}