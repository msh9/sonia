package sonia;

import java.io.*;

import org.freehep.graphicsio.gif.GIFGraphics2D;
import org.freehep.graphicsio.ImageGraphics2D;
import org.freehep.graphicsio.pdf.PDFGraphics2D;
import org.freehep.graphicsio.ps.PSGraphics2D;
import org.freehep.graphicsio.svg.SVGGraphics2D;
import org.freehep.graphicsio.swf.SWFGraphics2D;
import org.freehep.graphicsio.java.JAVAGraphics2D;
import org.freehep.graphicsio.cgm.CGMGraphics2D;
import org.freehep.graphicsio.emf.EMFGraphics2D;
import org.freehep.util.export.ExportDialog;
import org.freehep.graphicsio.gif.GIFExportFileType;

import java.awt.*;
import java.util.Properties;
import javax.swing.JFrame;



/**
 * dummy class to handle saving svg files
 *
 * @author Eytan Adar
 * @author Joshua Tyler
 * Copyright (c) 2003, Hewlett Packard Labs
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with
 * or without modification, are permitted provided that the following
 * conditions are met:
 *
 *   Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *   Neither the name of the Hewlett Packard nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE TRUSTEES OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
public abstract class HEPWriter {

    public static final int GIF = 0;
    public static final int JPG = 1;
    public static final int PDF = 2;
    public static final int PS = 5;
    public static final int EPS = 5; // same thing for now
    public static final int SVG = 7;
    public static final int SWF = 8;
    public static final int JAVA = 9;
    public static final int CGM = 11;
    public static final int EMF = 12;
    public static final int PNG = 2;

    public static void export(String outputfile, 
			      Component mds,
			      int type) {

  
	try {
	    Rectangle b = mds.getBounds();
	    //ExportFileType epsOut = new EPSExportFileType();
	
	    //File file;
	    //FileOutputStream fos;

	     System.out.println("Saving image...");

	    if (type == PS) {
		PSGraphics2D g = new PSGraphics2D(new File(outputfile),
						  new Dimension((int)b.width,
								(int)b.height));
		//g.setProperties(properties);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				   RenderingHints.VALUE_ANTIALIAS_ON); 
	    
		g.startExport();
		mds.paint(g);
		g.endExport();
	    } else if (type == GIF) {
		GIFGraphics2D g = new GIFGraphics2D(new File(outputfile),
						    new Dimension((int)b.width,
								  (int)b.height));
		//g.setProperties(properties);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				   RenderingHints.VALUE_ANTIALIAS_ON); 
		g.startExport();
		mds.paint(g);
		g.endExport();
//		if (LabNotebook.getNotebook() != null) {
//		    LabNotebook.getNotebook().addImage(outputfile,
//						       (int)b.width,
//						       (int)b.height);
//		}
	    } else if (type == JPG) {
		ImageGraphics2D g = 
		    new ImageGraphics2D(new File(outputfile),
					new Dimension((int)b.width,
						      (int)b.height),"jpg");
		//g.setProperties(properties);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				   RenderingHints.VALUE_ANTIALIAS_ON); 
		g.startExport();
		mds.paint(g);
		g.endExport();
//		if (LabNotebook.getNotebook() != null) {
//		    LabNotebook.getNotebook().addImage(outputfile,
//						       (int)b.width,
//						       (int)b.height);
//		}
	    } else if (type == PNG) {
		ImageGraphics2D g = 
		    new ImageGraphics2D(new File(outputfile),
					new Dimension((int)b.width,
						      (int)b.height),"png");
		//g.setProperties(properties);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				   RenderingHints.VALUE_ANTIALIAS_ON); 
		g.startExport();
		mds.paint(g);
		g.endExport();
//		if (LabNotebook.getNotebook() != null) {
//		    LabNotebook.getNotebook().addImage(outputfile,
//						       (int)b.width,
//						       (int)b.height);
//		}
	    } else if (type == JAVA) {
		JAVAGraphics2D g = new JAVAGraphics2D(new File(outputfile),
						      new Dimension((int)b.width,
								    (int)b.height));
		//g.setProperties(properties);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				   RenderingHints.VALUE_ANTIALIAS_ON); 
		g.startExport();
		mds.paint(g);
		g.endExport();
	    } else if (type == PDF) {
		PDFGraphics2D g = new PDFGraphics2D(new File(outputfile),
						    new Dimension((int)b.width,
								  (int)b.height));
		//g.setProperties(properties);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				   RenderingHints.VALUE_ANTIALIAS_ON); 
		g.startExport();
		mds.paint(g);
		g.endExport();
	    } else if (type == SWF) {
		SWFGraphics2D g = new SWFGraphics2D(new File(outputfile),
						    new Dimension((int)b.width,
								  (int)b.height));
		//g.setProperties(properties);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				   RenderingHints.VALUE_ANTIALIAS_ON); 
		g.startExport();
		mds.paint(g);
		g.endExport();
	    } else if (type == SVG) {
		SVGGraphics2D g = new SVGGraphics2D(new File(outputfile),
						    new Dimension((int)b.width,
								  (int)b.height));
		//g.setProperties(properties);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				   RenderingHints.VALUE_ANTIALIAS_ON); 
		g.startExport();
		mds.paint(g);
		g.endExport();
	    } else if (type == CGM) {
		CGMGraphics2D g = new CGMGraphics2D(new File(outputfile),
						    new Dimension((int)b.width,
								  (int)b.height));
		//g.setProperties(properties);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				   RenderingHints.VALUE_ANTIALIAS_ON); 
		g.startExport();
		mds.paint(g);
		g.endExport();
	    } else if (type == EMF) {
		EMFGraphics2D g = new EMFGraphics2D(new File(outputfile),
						    new Dimension((int)b.width,
								  (int)b.height));
		//g.setProperties(properties);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				   RenderingHints.VALUE_ANTIALIAS_ON); 
		g.startExport();
		mds.paint(g);
		g.endExport();
	    }
	    //debug
	   System.out.println("finished export");
	} catch (Exception e) {
		//debug
	    System.out.println("error exporting image");
	    e.printStackTrace();
	}
    }
}
