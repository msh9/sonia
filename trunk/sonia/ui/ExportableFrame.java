package sonia.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.PrintJob;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.RepaintManager;
import javax.swing.event.InternalFrameEvent;

import org.freehep.util.export.ExportDialog;


/**
 * 
 * Creates a frame with a menu item for exporting or printing graphic content.
 * Any window that needs the ability to export or print graphcis only needs to
 * extend this class rather than JFrame and overide the getGraphicContent()
 * method to specify exacatly what component to export. (otherwise whole frame
 * will export) Uses the FreeHep library for graphics export and some code from
 * the GUESS package
 */

public class ExportableFrame extends JInternalFrame {

	protected JMenuBar menuBar;

	protected JMenu exportMenu;

	public ExportableFrame() {
		super();
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);
		menuBar = new JMenuBar();
		//menuBar = super.getJMenuBar();
		exportMenu = new JMenu("Export");
		// add an action to show the printing option
		exportMenu.add(new AbstractAction("Print...") {
			public void actionPerformed(ActionEvent arg0) {
				printContent();
			}
		});
		exportMenu.add(new AbstractAction("Export Image...") {
			public void actionPerformed(ActionEvent arg0) {
				exportContent();
			}
		});
		setJMenuBar(menuBar);
		menuBar.add(exportMenu);
		
	}

	/**
	 * each subclasse may overide the method to return the specific component or
	 * panel that should be exported, otherwise will export the whole frame
	 * 
	 * @return the component to be renderd or printed
	 */
	protected JComponent getGraphicContent() {
		return getRootPane();
	}

	/**
	 * opens print dialog to print the component specified by getGraphcisContent
	 * 
	 */
	public void printContent() {
		PrinterJob job = PrinterJob.getPrinterJob();
		if (job.printDialog()) {
			try {
				job.setPrintable(new componentPrinter() {
				});

				job.print();
			} catch (PrinterException e) {
				System.out.println("printing error:");
				e.printStackTrace();
				// TODO: need a way to report printing errors properly with
				// dialog!
			}
		}
	}

	/**
	 * This class provides the guts for simple printing, based on code from the
	 * java tutorials and
	 * http://www.javaworld.com/javaworld/jw-12-2000/print/listing1_code.html
	 * 
	 * @author skyebend
	 * 
	 */
	private class componentPrinter implements Printable {
		public int print(Graphics printerGraphics, PageFormat pageFormat,
				int pageIndex) throws PrinterException {

			// debug
			System.out.println("printing page " + pageIndex);
			if (pageIndex == 0) // only print one page
			{
				JComponent compToPrint = getGraphicContent();
				Graphics2D graphics2D = (Graphics2D) printerGraphics;
				graphics2D.translate(pageFormat.getImageableX(), pageFormat
						.getImageableY());
				disableDoubleBuffering(compToPrint);
				compToPrint.repaint();
				compToPrint.paint(graphics2D);
				enableDoubleBuffering(compToPrint);
				return Printable.PAGE_EXISTS;

			} else {
				return Printable.NO_SUCH_PAGE;
			}
		}

	}

	/**
	 * opens export dialog to choose file format and directory save out graphics
	 * of component specified by get GraphicsContent
	 * 
	 */
	public void exportContent() {
		JComponent compToPrint = getGraphicContent();
		disableDoubleBuffering(compToPrint);
		compToPrint.repaint();
		HEPDialog exportDialog = new HEPDialog("SoNIA");
		exportDialog.showHEPDialog(this, "Chose file format for export",
				compToPrint, "NetworkPic");
		
		//THIS IS TO USE VERSION freehep 2.1.1
		//AS SOON AS THEY FIX THE JPG EXPORT PROBLEM
		//ExportDialog exportDialog = new ExportDialog();
	// exportDialog.showExportDialog(compToPrint, 
		//		"Chose file format for export...", compToPrint, "export");
		//enableDoubleBuffering(compToPrint);
	}

	protected void disableDoubleBuffering(JComponent comp) {
		RepaintManager currentManager = RepaintManager.currentManager(comp);
		currentManager.setDoubleBufferingEnabled(false);
	}

	protected void enableDoubleBuffering(JComponent comp) {
		RepaintManager currentManager = RepaintManager.currentManager(comp);
		currentManager.setDoubleBufferingEnabled(true);
	}
	
	/**
	 * sub classes should overide these internal frame methods as needed
	 * 
	 */
	

	public void internalFrameOpened(InternalFrameEvent e) {
	}

	public void internalFrameClosing(InternalFrameEvent e) {
		
	}

	public void internalFrameClosed(InternalFrameEvent e) {

	}

	public void internalFrameIconified(InternalFrameEvent e) {
	}

	public void internalFrameDeiconified(InternalFrameEvent e) {

	}

	public void internalFrameActivated(InternalFrameEvent e) {

	}

	public void internalFrameDeactivated(InternalFrameEvent e) {

	}

}
