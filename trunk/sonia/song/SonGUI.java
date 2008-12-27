package sonia.song;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import sonia.SoniaController;
import sonia.song.filters.CleaningFilter;

/**
 * user interface for SonG program for constructing sonia .son files from a
 * database
 * 
 * @author skyebend
 * 
 */
public class SonGUI implements WindowListener, ActionListener, Runnable {

	private JFrame frame;

	private Getter song = null;

	private JTextField host;
	
	private JTextField port;

	private JTextField user;

	private JTextField dbName;

	private JTextField password;

	private JButton connect;

	private JTextArea statusText;

	private JPanel queryPanel;

	private JTextArea arcsQuery;

	private JCheckBox generateNodeset;
	
	private JCheckBox generateDateset;

	private JTextArea nodePropsQuery;

	private JScrollPane nodePropsScroller;

	private JTextArea nodesQuery;

	private JScrollPane nodesScroller;

	private JTextArea sonPreview;

	private JButton saveSon;
	
	private JButton launchSonia;

	private JButton validateSon;

	private JPanel previewPanel;

	private JButton runQuery;

	private JPanel dataPanel;

	private JTable nodeDataTable;

	private JScrollPane nodeDataScroller;

	private ResultTableModel nodeTableModel;

	private JTable arcDataTable;

	private JScrollPane arcDataScroller;

	private ResultTableModel arcTableModel;

	private JList problemList;

	private JScrollPane problemScroller;

	private ProblemListModel problemModel;
	
	private JPanel filterPanel;
	
	private JList filterList;
	
	private JButton runFilter;

	private JTabbedPane tabber;

	public SonGUI() {
		song = new Getter(this);
		// set up the ui
		frame = new JFrame("sonG - get relations from a DB into SoNIA files");
		frame.setSize(800, 700);
		frame.addWindowListener(this);
		frame.setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel(new BorderLayout());
		// mainPanel.setMinimumSize(new Dimension(800,600));
		// mainPanel.setSize(800,600);
		// JScrollPane mainScroller = new JScrollPane(mainPanel);
		// frame.add(mainScroller,BorderLayout.CENTER);
		frame.add(mainPanel, BorderLayout.CENTER);

		GridBagConstraints c = new GridBagConstraints();
		// set up the connection ui stuff
		JPanel connectPanel = new JPanel(new GridBagLayout());
		connectPanel.setBorder(new TitledBorder("Connection Settings"));
		host = new JTextField("192.168.2.2", 10);
		host.setBorder(new TitledBorder("Host Address"));
		c.gridy = 0;
		connectPanel.add(host, c);
		port = new JTextField("3306",10);
		port.setBorder(new TitledBorder("Port"));
		c.gridy=1;
		connectPanel.add(port,c);
		user = new JTextField("", 10);
		user.setBorder(new TitledBorder("User Name"));
		c.gridy = 2;
		connectPanel.add(user, c);
		password = new JTextField("", 10);
		password.setBorder(new TitledBorder("Password"));
		c.gridy = 3;
		connectPanel.add(password, c);
		dbName = new JTextField("", 10);
		dbName.setBorder(new TitledBorder("Database Name"));
		c.gridy = 4;
		connectPanel.add(dbName, c);
		connect = new JButton("Connect to DB");
		connect.addActionListener(this);
		c.gridy = 5;
		connectPanel.add(connect, c);

		// status
		statusText = new JTextArea(
				"Connect to database and enter relationship queries.", 4, 20);
		statusText.setEditable(false);
		statusText.setWrapStyleWord(true);
		JScrollPane statusScroller = new JScrollPane(statusText);
		statusScroller.setBorder(new TitledBorder("Status"));
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		mainPanel.add(statusScroller, BorderLayout.PAGE_START);

		c.gridx = 0;
		c.gridy = 1;
		mainPanel.add(connectPanel, BorderLayout.LINE_START);

		// query ui stuff
		queryPanel = new JPanel(new GridBagLayout());
		queryPanel.setBorder(new TitledBorder("Queries to build network file"));
		arcsQuery = new JTextArea(
				"relationship query here", 10, 50);
		JScrollPane arcsQueryScroller = new JScrollPane(arcsQuery);
		arcsQueryScroller
				.setBorder(new TitledBorder("Arc Relationships Query"));
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		queryPanel.add(arcsQueryScroller, c);
		runQuery = new JButton("Run Queries");
		runQuery.addActionListener(this);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		queryPanel.add(runQuery, c);

		generateNodeset = new JCheckBox("Generate node set from relations",
				true);
		generateNodeset.addActionListener(this);
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		queryPanel.add(generateNodeset, c);
		
		generateDateset = new JCheckBox("Also generate times ("+Getter.timeIdTag+")",false);
		generateDateset.addActionListener(this);
		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 1;
		queryPanel.add(generateDateset, c);

		nodePropsQuery = new JTextArea(
				"", 5, 50);
		nodePropsScroller = new JScrollPane(nodePropsQuery);
		nodePropsScroller.setBorder(new TitledBorder(
				"Node Properties Query (use " + Getter.nodeIdTag
						+ " as placeholder for NodeId)"));
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 3;
		queryPanel.add(nodePropsScroller, c);

		// this one is hidden
		nodesQuery = new JTextArea(5, 40);
		nodesScroller = new JScrollPane(nodesQuery);
		nodesScroller.setBorder(new TitledBorder("Node Set Query"));
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 3;
		queryPanel.add(nodesScroller, c);
		// nodesScroller.setVisible(false);

		// DATA VIEW PANEL
		dataPanel = new JPanel(new GridBagLayout());
		dataPanel.setBorder(new TitledBorder("Network Data"));
		nodeTableModel = new ResultTableModel(song.getNodeData(), song
				.getNodeHeaders());
		nodeDataTable = new JTable(nodeTableModel);
		nodeDataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		nodeDataScroller = new JScrollPane(nodeDataTable);
		nodeDataScroller.setBorder(new TitledBorder("Node Data"));
		nodeDataScroller.setMinimumSize(new Dimension(600, 200));
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 0;
		dataPanel.add(nodeDataScroller, c);
		nodeDataScroller.setVisible(false);
		arcTableModel = new ResultTableModel(song.getArcData(), song
				.getArcHeaders());
		arcDataTable = new JTable(arcTableModel);
		arcDataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		arcDataScroller = new JScrollPane(arcDataTable);
		arcDataScroller.setBorder(new TitledBorder("Arc Data"));
		arcDataScroller.setMinimumSize(new Dimension(600, 200));
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 0;
		dataPanel.add(arcDataScroller, c);
		arcDataScroller.setVisible(false);
		// problems
		problemModel = new ProblemListModel(song.getProblems());
		problemList = new JList(problemModel);
		problemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// do actions when list is clicked
		problemList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				// get the probelm object associated with the list item
				showProblem((DataProblem) ((JList) e.getSource())
						.getSelectedValue());
			}

		});
		problemScroller = new JScrollPane(problemList);
		problemScroller.setBorder(new TitledBorder("Data Problems"));
		problemScroller.setMinimumSize(new Dimension(600, 100));
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 0;
		dataPanel.add(problemScroller, c);
		problemScroller.setVisible(false);
		
	//FILTERS pane to hold data cleaning operations	
		filterPanel = new JPanel();
		filterPanel.setBorder(new TitledBorder("Data Cleaning Filters"));
		FilterListModel filterModel = new FilterListModel(song.getAllFilters());
		filterList = new JList(filterModel);
		JScrollPane filterScroller = new JScrollPane(filterList);
		filterScroller.setBorder(new TitledBorder("Availible Filters"));
		filterPanel.add(filterScroller, BorderLayout.CENTER);
		runFilter = new JButton("Run Filters");
		runFilter.addActionListener(this);
		filterPanel.add(runFilter, BorderLayout.SOUTH);

		//PREVIEW pane to hold the preview of the file to be written
		previewPanel = new JPanel();
		sonPreview = new JTextArea(40, 90);
		Font previewFont = new Font("Monospaced", Font.PLAIN, 10);
		sonPreview.setFont(previewFont);
		JScrollPane sonScroller = new JScrollPane(sonPreview);
		sonScroller.setBorder(new TitledBorder("Preview of .son file"));

		// c.gridx=0;c.gridy=0;c.gridwidth=1;
		previewPanel.add(sonScroller, BorderLayout.CENTER);
		validateSon = new JButton("Validate .son");
		validateSon.addActionListener(this);
		previewPanel.add(validateSon, BorderLayout.PAGE_END);
		saveSon = new JButton("Save as .son file");
		saveSon.addActionListener(this);
		previewPanel.add(saveSon, BorderLayout.PAGE_END);
		launchSonia = new JButton("Launch in SoNIA");
		launchSonia.addActionListener(this);
		previewPanel.add(launchSonia, BorderLayout.PAGE_END);

		// tabber
		tabber = new JTabbedPane();
		tabber.addTab("Queries", queryPanel);
		tabber.addTab("Data", dataPanel);
		tabber.addTab("Filters", filterPanel);
		tabber.addTab("Preview", previewPanel);
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 2;
		c.fill = GridBagConstraints.BOTH;
		mainPanel.add(tabber, BorderLayout.CENTER);

	}

	public void run() {
		frame.setVisible(true);
		updateUI();
	}

	public void showStatus(String status) {
		statusText.append(status + "\n");
		statusText.setCaretPosition(statusText.getText().length());
	}

	public void showSonPreview(String text) {
		sonPreview.setText(text);
		tabber.setSelectedComponent(previewPanel);
	}

	/**
	 * also hilites the specified row
	 * 
	 * @param text
	 * @param hiliteRow
	 */
	public void showSonPreview(String text, int row) {
		showSonPreview(text);
		// figure out the correct selection positions for the row number
		int selStart = 0;
		int selEnd = 0;
		for (int r = 0; r < row; r++) {
			selStart = selEnd;
			selEnd = text.indexOf("\n", selStart + 1);
		}
		sonPreview.requestFocus();
		sonPreview.select(selStart, selEnd);
	}

	/**
	 * main event handling method
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == connect) {
			song.connectToDB(host.getText(), port.getText(), dbName.getText(), user.getText(),
					password.getText());
			password.setText("");
		} else if (e.getSource() == runQuery) {
			if (generateNodeset.isSelected()){
				song.getNetwork(arcsQuery.getText(), generateNodeset.isSelected(),
					nodePropsQuery.getText(),generateDateset.isSelected());
			} else {
				song.getNetwork(arcsQuery.getText(), generateNodeset.isSelected(),
						nodesQuery.getText(),generateDateset.isSelected());
			}
		} else if (e.getSource() == validateSon) {
			// TODO:make sure any edits have been saved back
			song.validateSon();
		} else if (e.getSource() == saveSon) {
		    String filename = ".son";
		    JFileChooser fc = new JFileChooser(new File(filename));
		    // Show save dialog; this method does not return until the dialog is closed
		    fc.showSaveDialog(frame);
		    String pickedFile = fc.getSelectedFile().getAbsolutePath();
		    if (pickedFile != null){
		    	song.saveAsSon(pickedFile);
		    }
		    
		} else if (e.getSource() == runFilter){
			song.filterData(filterList.getSelectedValues());
		} else if (e.getSource() == generateNodeset) {
			
		} else if (e.getSource() == launchSonia){
			//launch sonia as an external java application
			//TODO: do this by passing the string or data source diretly
			try {
				String tempfile = File.createTempFile("temp", ".son").getAbsolutePath();
				song.saveAsSon(tempfile);
				String[] args = new String[]{"file:"+tempfile};
				SoniaController.main(args);
			} catch (IOException e1) {
				song.error("Unable to create temp file to pass data to SoNIA");
			}
			song.status("Launched SoNIA with .son data. (Quitting sonG will also quit SoNIA)");
			
		}

		updateUI();
	}

	/**
	 * selects the appropriate row in the appropriate data table
	 * 
	 * @param problem
	 */
	public void showProblem(DataProblem problem) {
		// got to the data window
		tabber.setSelectedComponent(dataPanel);
		if (problem != null) {
			// figure out what kind of error it is
			if (problem.whereFound() == DataProblem.NODES) {
				// hilite the selected row.
				nodeDataTable.requestFocusInWindow();
				nodeDataTable.changeSelection(problem.getRowNumber(), -1,
						false, false);
			} else if (problem.whereFound() == DataProblem.ARCS) {
				// hilite the selected row.
				arcDataTable.requestFocusInWindow();
				arcDataTable.changeSelection(problem.getRowNumber(), -1, false,
						false);
			}
		}
	}

	/**
	 * hide/show the two optional fields for the nodes based on the state of the
	 * toggle button
	 * 
	 */
	public void updateUI() {
		if (generateNodeset.isSelected()) {
			nodePropsScroller.setVisible(true);
			nodesScroller.setVisible(false);
			generateDateset.setVisible(true);
		} else {
			nodePropsScroller.setVisible(false);
			nodesScroller.setVisible(true);
			generateDateset.setVisible(false);
		}
		queryPanel.validate();

		// only show the data tables if there is data to show
		nodeDataScroller.setVisible(song.getNodeHeaders() != null
				&& song.getNodeHeaders().size() > 0);
		nodeTableModel.fireTableStructureChanged();
		nodeTableModel.fireTableDataChanged();
		arcDataScroller.setVisible(song.getArcHeaders() != null
				&& song.getArcHeaders().size() > 0);
		arcTableModel.fireTableStructureChanged();
		arcTableModel.fireTableDataChanged();
		problemScroller.setVisible(song.getProblems() != null
				&& song.getProblems().size() > 0);
		problemModel.refresh();
		dataPanel.validate();
		previewPanel.validate();
	}

	/**
	 * Lanch the GUI for setting up queries
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		SonGUI gui = new SonGUI();
		SwingUtilities.invokeLater(gui);
	}

	public void windowActivated(WindowEvent arg0) {
	}

	public void windowClosed(WindowEvent arg0) {
	}

	public void windowClosing(WindowEvent arg0) {
		int result = JOptionPane.showConfirmDialog(frame,
				"Exiting sonG will discard all unsaved data",
				" Are you sure you want to quit sonG?",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if (result == 0) {
			song.closeDB();
			System.exit(0);
		}
	}

	public void windowDeactivated(WindowEvent arg0) {
	}

	public void windowDeiconified(WindowEvent arg0) {
	}

	public void windowIconified(WindowEvent arg0) {
	}

	public void windowOpened(WindowEvent arg0) {
	}

}
