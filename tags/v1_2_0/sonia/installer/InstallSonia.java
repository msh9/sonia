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
package sonia.installer;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.sound.sampled.TargetDataLine;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

public class InstallSonia {

	//public static final String SONIA_DOWNLOAD_URL = "http://csde.washington.edu/~skyebend/rsonia/current/sonia_1_1_3_unstable.jar";
	public static final String SONIA_DOWNLOAD_URL = "http://internap.dl.sourceforge.net/sourceforge/sonia/";
	
	public static final String SONIA_VERSION_NAME = "sonia_1_2_0.jar";
	
	public static final String GPL_URL = "http://www.gnu.org/licenses/gpl.txt";

	public static final String LGPL_URL = "http://www.gnu.org/licenses/lgpl.txt";

	public static final String COLT_DOWNLOAD_URL = "http://dsd.lbl.gov/~hoschek/colt-download/releases/colt-1.2.0.zip";

	public static final String COLT_LICENSE_URL = "http://dsd.lbl.gov/~hoschek/colt-download/releases/license.html";

	public static final String QT_DOWNLOAD_URL = "http://www.apple.com/quicktime/download/";

	public static final String FREEHEP_DOWNLOAD_URL = "ftp://ftp.slac.stanford.edu/software/freehep/release/v1.2.2/freehep-v1.2.2.zip";

	public static final String JAVASWF_DOWNLOAD_URL = "http://internap.dl.sourceforge.net/sourceforge/javaswf/javaswf-binary-baseline.zip";
	
	
	private JFrame baseFrame;

	private JTextArea status;

	private String defaultPath = null;

	private String currentStatus = "";

	private Timer progressTimer;

	protected boolean breakNow = false;

	private static InstallSonia installer = null;

	/**
	 * @author skyebend
	 * @param args
	 */
	public static void main(String[] args) {
		String path = null;
		if (args.length > 0) {
			path = args[0];
			// System.exit(installer.install(path));
		}

		installer = new InstallSonia(path);

	}

	public InstallSonia getInstaller() {
		// singleton
		if (installer == null) {
			installer = new InstallSonia(null);
		}
		return installer;
	}

	private InstallSonia(String path) {

		defaultPath = path;
		baseFrame = new JFrame(
				"SoNIA package installer:" +SONIA_VERSION_NAME);
		// set the icon for the program
		baseFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(
				this.getClass().getResource("soniaLogo16.jpg")));
		baseFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//baseFrame.setLocationByPlatform(true); 
		ImageIcon soniaIcon = new ImageIcon(Toolkit.getDefaultToolkit()
				.getImage(this.getClass().getResource("soniaLogo64.jpg")));
		String installText = "<html>This installer will download and unpack SoNIA "
				+ "<br> and the required libraries "
				+ "<br>from their respective websites. </html>";
		JLabel message = new JLabel(installText);
		message.setText(installText);
		message.setBackground(baseFrame.getBackground());
		JButton install = new JButton("Install...");
		install.addActionListener(new ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent e) {
				install(defaultPath);
			};
		});
		JButton exit = new JButton("Exit");
		exit.addActionListener(new ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent e) {
				System.exit(1);
			};
		});
		JButton soniaInfo = new JButton("About SoNIA..");
		soniaInfo.addActionListener(new ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent e) {
				openURL("http://sonia.stanford.edu/");
			};
		});
		JButton coltInfo = new JButton("About Colt..");
		coltInfo.addActionListener(new ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent e) {
				openURL("http://dsd.lbl.gov/~hoschek/colt/");
			};
		});
		JButton freeHepInfo = new JButton("About FreeHep..");
		freeHepInfo.addActionListener(new ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent e) {
				openURL("http://java.freehep.org/");
			};
		});
		status = new JTextArea(5, 40);
		status.setWrapStyleWord(true);
		status.setEditable(false);
		status.setBackground(baseFrame.getBackground());
		JScrollPane statusScroller = new JScrollPane(status);
		statusScroller.setBorder(new TitledBorder("Installer status:"));
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		 c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(5, 5, 5, 5);
		c.gridx = 0;
		c.gridy = 0;
		mainPanel.add(new JLabel(soniaIcon), c);
		c.gridwidth = 3;
		c.gridx = 1;
		c.gridy = 0;
		mainPanel.add(message, c);
		c.gridx = 0;
		c.gridy = 1;
		c.gridheight = 2;
		mainPanel.add(statusScroller, c);
		c.gridx = 3;
		c.gridy = 1;
		c.gridheight = 1;
		c.gridwidth = 1;
		mainPanel.add(install, c);
		c.gridx = 3;
		c.gridy = 2;
		mainPanel.add(exit, c);
		c.gridx = 0;
		c.gridy = 3;
		c.gridheight = 1;
		mainPanel.add(soniaInfo, c);
		c.gridx = 1;
		c.gridy = 3;
		mainPanel.add(coltInfo, c);
		c.gridx = 2;
		c.gridy = 3;
		mainPanel.add(freeHepInfo, c);

		baseFrame.getContentPane().add(mainPanel);
		baseFrame.setSize(600, 400);
		baseFrame.setVisible(true);
	}

	// URL url = new URL(linkUrl);
	// URLConnection connection = url.openConnection();
	public void install(String targetPath) {
		class InstallRunner extends Thread {
			String targetPath = null;

			public InstallRunner(String path) {
				targetPath = path;
			}

			public void run() {
				// check java version
				// String javaVer System.getProperties
				showProgress("java version: "
						+ System.getProperty("java.vm.version"));
				// show sonia info
				// choose an install dir
				if (targetPath == null) {
					//THIS DOES NOT WORK ON THE MAC BECAUSE THE FILECHOSER IS BROKEN
//					JFileChooser chooser = new JFileChooser();
//					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//					int returnVal = chooser.showSaveDialog(baseFrame);
//					if (returnVal == JFileChooser.APPROVE_OPTION) {
//						targetPath = chooser.getSelectedFile()
//								.getAbsolutePath();
//					}
					FileDialog locateOutput = new FileDialog(baseFrame, 
							"Please choose a directory to install SoNIA", FileDialog.SAVE);
					locateOutput.setVisible(true);
					targetPath = locateOutput.getDirectory();
			
				}
				// if it still equals null..
				if (targetPath == null) {
					showError("No directory choosen for installation");
					return;
				}
				showProgress("install dir:" + targetPath);
				// show sonia license
				// "C:/Documents and
				// Settings/skyebend/workspace/SoNIA/soniainstaller/GPL2_0_license.txt"
				try {
					boolean accept = showLicense("SoNIA", GPL_URL);
					if (!accept) {
						showError("License declined");
						return;
					}
				} catch (IOException ioe) {
					showError("Unable to load license");
					ioe.printStackTrace();
					return;
				}

				// get the most current version of sonia
				try {
					downloadFile(SONIA_DOWNLOAD_URL+SONIA_VERSION_NAME, targetPath);
				} catch (Exception e) {
					showError("error downloading sonia.jar: " + e.toString());
					e.printStackTrace();
				}
				// download colt license
				try {
					downloadFile(COLT_LICENSE_URL, targetPath);
				} catch (Exception e) {
					showError("error downloading colt libraries: "
							+ e.toString());
					e.printStackTrace();
				}
				// create the lib directory
				File libdir = new File(targetPath + "/lib");
				if (!libdir.exists()) {
					boolean success = libdir.mkdir();
					if (!success) {
						// Directory creation failed
						showError("Unable to create lib directory");
						return;
					}
				}

				// show colt info and license
				try {
					boolean accept = showLicense("Colt Java Numerics",
							COLT_LICENSE_URL);
					if (!accept) {
						showError("License declined");
						return;
					}
				} catch (IOException ioe) {
					showError("Unable to load license");
					ioe.printStackTrace();
					return;
				}
				// get colt archive (.zip file)
				try {
					downloadFile(COLT_DOWNLOAD_URL, targetPath);
				} catch (Exception e) {
					showError("error downloading colt libraries: "
							+ e.toString());
					e.printStackTrace();
				}
				// extract colt.jar
				showProgress("Extracting contents of " + getShortFileName(COLT_DOWNLOAD_URL));
				extractFile(targetPath + "/colt-1.2.0.zip", "colt.jar",
						targetPath + "/lib");

				// show freehep info and licesnse
				try {
					boolean accept = showLicense("freeHEP VectorGraphics",
							LGPL_URL);
					if (!accept) {
						showError("License declined");
						return;
					}
				} catch (IOException ioe) {
					showError("Unable to load license");
					ioe.printStackTrace();
					return;
				}
				// get freehep archive (.zip file)
				try {
					downloadFile(FREEHEP_DOWNLOAD_URL, targetPath);
				} catch (Exception e) {
					showError("error downloading freehep libraries: "
							+ e.toString());
					e.printStackTrace();
				}
				// extract freehep jars.
				showProgress("Extracting contents of " + getShortFileName(FREEHEP_DOWNLOAD_URL));
				extractFile(targetPath + "/freehep-v1.2.2.zip",
						"freehep-base.jar", targetPath + "/lib");
				extractFile(targetPath + "/freehep-v1.2.2.zip",
						"freehep-graphics2d.jar", targetPath + "/lib");
				extractFile(targetPath + "/freehep-v1.2.2.zip",
						"freehep-graphicsio.jar", targetPath + "/lib");
				extractFile(targetPath + "/freehep-v1.2.2.zip",
						"freehep-graphicsio-cgm.jar", targetPath + "/lib");
				extractFile(targetPath + "/freehep-v1.2.2.zip",
						"freehep-graphicsio-emf.jar", targetPath + "/lib");
				extractFile(targetPath + "/freehep-v1.2.2.zip",
						"freehep-graphicsio-gif.jar", targetPath + "/lib");
				extractFile(targetPath + "/freehep-v1.2.2.zip",
						"freehep-graphicsio-java.jar", targetPath + "/lib");
				extractFile(targetPath + "/freehep-v1.2.2.zip",
						"freehep-graphicsio-pdf.jar", targetPath + "/lib");
				extractFile(targetPath + "/freehep-v1.2.2.zip",
						"freehep-graphicsio-ppm.jar", targetPath + "/lib");
				extractFile(targetPath + "/freehep-v1.2.2.zip",
						"freehep-graphicsio-ps.jar", targetPath + "/lib");
				extractFile(targetPath + "/freehep-v1.2.2.zip",
						"freehep-graphicsio-svg.jar", targetPath + "/lib");
				extractFile(targetPath + "/freehep-v1.2.2.zip",
						"freehep-graphicsio-swf.jar", targetPath + "/lib");
				extractFile(targetPath + "/freehep-v1.2.2.zip",
						"freehep-hep.jar", targetPath + "/lib");
				
				//get javaswf archive (.zip file)
				try {
					downloadFile(JAVASWF_DOWNLOAD_URL, targetPath);
				} catch (Exception e) {
					showError("error downloading javaSWF libraries: "
							+ e.toString());
					e.printStackTrace();
				}
				
				showProgress("Extracting contents of " + getShortFileName(JAVASWF_DOWNLOAD_URL));
				//extrac javaswf.jar
				extractFile(targetPath + "/javaswf-binary-baseline.zip",
						"javaswf.jar", targetPath + "/lib");

				// show quicktime info
				String QTMessage = "<html>SoNIA currently exports animations in QuickTime .mov format <BR>"
						+ " using the QTJava libraries from AppleComputer. <br>"
						+ "Please download and run the QuickTime installer from:<br> "
						+ QT_DOWNLOAD_URL
						+ "<br>"
						+ "  Would you like to open the download page in your browser now?</html>";
				JLabel QTBlurb = new JLabel(QTMessage);

				int openQT = JOptionPane.showConfirmDialog(baseFrame,
						QTMessage, "Downloading QTJava installer",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (openQT == JOptionPane.OK_OPTION) {
					openURL(QT_DOWNLOAD_URL);
				}
				// open web page to quicktime installer
				showProgress("Installation complete.\n"+
						"SoNIA "+SONIA_VERSION_NAME+"has been installed in the directroy:\n"+
						targetPath);

				return;
			}
		}
		InstallRunner runner = new InstallRunner(targetPath);
		runner.start();

	}

	/**
	 * extracts the first file of a certain name independent of its position in
	 * the zip archive. runs an ExtractRunner in its own thread
	 * 
	 * @author skyebend
	 * @param zipFileName
	 * @param wantedFile
	 * @param targetDir
	 */
	private void extractFile(String zipFileName, String wantedFile,
			String targetDir) {

		final int BUFFER = 2048;

		try {
			BufferedOutputStream dest = null;
			FileInputStream fis = new FileInputStream(zipFileName);
			ZipInputStream zis = new ZipInputStream(
					new BufferedInputStream(fis));
			ZipEntry entry;
			showProgress("Searching zip file for " + wantedFile);
			while (((entry = zis.getNextEntry()) != null) & !breakNow) {
				// we are looking for a certain entry
				if (entry.getName().endsWith(wantedFile)) {
					showProgress("Extracting: " + wantedFile);
					int count;
					byte data[] = new byte[BUFFER];
					// write the files to the disk
					FileOutputStream fos = new FileOutputStream(targetDir + "/"
							+ getShortFileName(entry.getName()));
					dest = new BufferedOutputStream(fos, BUFFER);
					while ((count = zis.read(data, 0, BUFFER)) != -1) {
						dest.write(data, 0, count);
					}
					dest.flush();
					dest.close();
					break;
				}
			}
			zis.close();
		} catch (Exception e) {
			showError("Error unziping file");
			e.printStackTrace();
		}

	}

	public void showError(String error) {
		if (progressTimer == null) {
			progressTimer = new Timer(500, new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					status.setText(currentStatus);
					status.repaint();
				};
			});
			progressTimer.start();
		}
		currentStatus = error;
		System.out.println(error);
	}

	public void showProgress(String message) {
		if (progressTimer == null) {
			progressTimer = new Timer(500, new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					status.setText(currentStatus);
					status.repaint();
				};
			});
			progressTimer.start();
		}
		currentStatus = message;
		//System.out.println("\t" + message);
	}

	/**
	 * shows the license with the path and ok
	 * 
	 * @author skyebend
	 * @param path
	 *            path to text to load.
	 * @return true if use agrees
	 * @throws IOException
	 */
	private boolean showLicense(String componentName, String file)
			throws IOException {
		boolean agree = false;
		JComponent licenseText;
		// if (file.endsWith(".html") | file.endsWith(".htm")) {
		licenseText = new JTextPane();
		((JTextPane) licenseText).setPage(file);
		licenseText.setSize(400, 300);
		// } else {
		// // open connection to file
		// LineNumberReader reader = new LineNumberReader(new FileReader(file));
		// String text = "";
		// String line = reader.readLine();
		// while (line != null) {
		// text += line + "\n";
		// line = reader.readLine();
		// }
		// reader.close();
		// licenseText = new JTextArea(text, 20, 40);
		// }
		JScrollPane scroller = new JScrollPane(licenseText,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.setPreferredSize(new Dimension(600, 400));
		scroller
				.setBorder(new TitledBorder(
						"The requierd "
								+ componentName
								+ " library will be installed under the following conditions:"));
		JPanel licenseArea = new JPanel();
		licenseArea.add(scroller);
		int result = JOptionPane
				.showConfirmDialog(
						baseFrame,
						scroller,
						"Installing the component means you have read and agree to the terms of this license:",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE);

		if (result == JOptionPane.OK_OPTION) {
			agree = true;
		}
		return agree;
	}

	/**
	 * launches a thread to download the file from remote url
	 * 
	 * @author skyebend
	 * @param remoteUrl
	 * @param savePath
	 * 
	 */
	private void downloadFile(String remoteUrl, String savePath) {

		OutputStream out = null;
		URLConnection conn = null;
		InputStream in = null;

		try {

			URL url = new URL(remoteUrl);
			String fileName = savePath + "/" + getShortFileName(remoteUrl);
			showProgress("Opening connection to " + remoteUrl);
			out = new BufferedOutputStream(new FileOutputStream(fileName));
			conn = url.openConnection();
			in = conn.getInputStream();
			byte[] buffer = new byte[1024];
			int numRead;
			long numWritten = 0;
			while ((numRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, numRead);
				numWritten += numRead;
				showProgress("Downloaded " + numWritten + " bytes of file "
						+ getShortFileName(remoteUrl));
			}
		} catch (Exception e) {
			getInstaller().showError(e.getMessage());
			e.printStackTrace();
		}

		try {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		getInstaller().showProgress(
				getShortFileName(remoteUrl) + " download complete.");

	}

	private String getShortFileName(String address) {
		int lastSlashIndex = address.lastIndexOf('/');
		if (lastSlashIndex >= 0 && lastSlashIndex < address.length() - 1) {
			return address.substring(lastSlashIndex + 1);
		} else {
			return address;
		}
	}

	// ///////////////////////////////////////////////////////
	// Bare Bones Browser Launch //
	// Version 1.5 //
	// December 10, 2005 //
	// Supports: Mac OS X, GNU/Linux, Unix, Windows XP //
	// Example Usage: //
	// String url = "http://www.centerkey.com/"; //
	// BareBonesBrowserLaunch.openURL(url); //
	// Public Domain Software -- Free to Use as You Like //
	// ///////////////////////////////////////////////////////

	public void openURL(String url) {
		final String errMsg = "Error attempting to launch web browser";
		String osName = System.getProperty("os.name");
		try {
			if (osName.startsWith("Mac OS")) {
				Class fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL",
						new Class[] { String.class });
				openURL.invoke(null, new Object[] { url });
			} else if (osName.startsWith("Windows"))
				Runtime.getRuntime().exec(
						"rundll32 url.dll,FileProtocolHandler " + url);
			else { // assume Unix or Linux
				String[] browsers = { "firefox", "opera", "konqueror",
						"epiphany", "mozilla", "netscape" };
				String browser = null;
				for (int count = 0; count < browsers.length && browser == null; count++)
					if (Runtime.getRuntime().exec(
							new String[] { "which", browsers[count] })
							.waitFor() == 0)
						browser = browsers[count];
				if (browser == null)
					throw new Exception("Could not find web browser");
				else
					Runtime.getRuntime().exec(new String[] { browser, url });
			}
		} catch (Exception e) {
			showError(errMsg + ":\n" + e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

}
