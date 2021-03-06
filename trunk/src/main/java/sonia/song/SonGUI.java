package sonia.song;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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

	public static int DB_MODE = 0;
	public static int TEXT_MODE = 1;

	private JFrame frame;

	private Getter song = null;

	private int mode = DB_MODE;

	private JTabbedPane sourcePanel;

	private JPanel filePanel;

	private JTextField host;

	private JTextField port;

	private JTextField user;

	private JTextField dbName;

	private JTextField password;

	private JButton connect;

	private JTextArea statusText;

	private JPanel queryPanel;

	private JCheckBox crawlNetwork;

	private JCheckBox crawlTimes;

	private JPanel crawlPanel;

	private JTextArea seedQuery;

	private JTextArea timeSetQuery;

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

	private JPanel textMappingPanel;

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

	private JProgressBar progressBar;

	private JDialog progressDialog;

	private JButton cancel;

	private String propertiesFileName = "SonGUIProperties";

	private Properties props;

	private JButton openArcFile;

	private JTextField arcFileLocation;

	private JButton openNodeFile;

	private JTextField nodeFileLocation;

	private JCheckBox attachNodeProps;

	// private JTextField delimeter;
	private JComboBox delimeter;

	private JButton parse;

	private JPanel arcHeaderColumnUI;

	private JPanel nodeHeaderColumnUI;

	private JCheckBox parserGenerateNodeSet;

	private JCheckBox parserGenerateNodeTimes;

	private static String[] delimiters = { ",", "\t", "|", " " };
	private static String[] delimterNames = { ", comma", "\t tab", "| pipe",
			" single space" };

	private static String IGNORE_STRING = "<ignore>";

	public SonGUI() {
		song = new Getter(this);
		// set up the ui
		frame = new JFrame(
				"sonG "+song.VERSION+" - get relations from a DB or text files into SoNIA network format");
		frame.setSize(800, 700);
		frame.addWindowListener(this);
		frame.setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel(new BorderLayout());
		// mainPanel.setMinimumSize(new Dimension(800,600));
		// mainPanel.setSize(800,600);
		// JScrollPane mainScroller = new JScrollPane(mainPanel);
		// frame.add(mainScroller,BorderLayout.CENTER);
		frame.add(mainPanel, BorderLayout.CENTER);

		sourcePanel = new JTabbedPane();

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 0, 0, 0);

		// set up the connection ui stuff
		JPanel connectPanel = new JPanel(new GridBagLayout());
		connectPanel.setBorder(new TitledBorder("Connection Settings"));
		host = new JTextField("", 10);
		host.setBorder(new TitledBorder("Host Address"));
		c.gridy = 0;
		connectPanel.add(host, c);
		port = new JTextField("3306", 10);
		port.setBorder(new TitledBorder("Port"));
		c.gridy = 1;
		connectPanel.add(port, c);
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

		// set up the file loading ui stuff
		filePanel = new JPanel(new GridBagLayout());
		filePanel.setBorder(new TitledBorder("File Settings"));
		arcFileLocation = new JTextField("relationData.csv", 12);
		arcFileLocation.setBorder(new TitledBorder("Edge file path"));
		arcFileLocation.setEditable(false);
		c.gridy = 0;
		filePanel.add(arcFileLocation, c);
		openArcFile = new JButton("Open Arcs File");
		openArcFile.addActionListener(this);
		c.gridy = 1;
		filePanel.add(openArcFile, c);
		// delimeter = new JTextField(",", 10);
		delimeter = new JComboBox(delimterNames);
		delimeter.setBorder(new TitledBorder("Delimiter"));
		c.gridy = 2;
		filePanel.add(delimeter, c);
		nodeFileLocation = new JTextField("nodeData.csv", 12);
		nodeFileLocation.setBorder(new TitledBorder("Node file path"));
		nodeFileLocation.setEditable(false);
		c.gridy = 3;
		filePanel.add(nodeFileLocation, c);
		openNodeFile = new JButton("Open Node File");
		openNodeFile.addActionListener(this);
		c.gridy = 4;
		filePanel.add(openNodeFile, c);

		// status
		statusText = new JTextArea(
				"Please choose input source, then connect to database or open input files",
				4, 20);
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

		sourcePanel.setBorder(new TitledBorder("Input Source"));
		sourcePanel.addTab("Database", connectPanel);
		sourcePanel.addTab("File", filePanel);
		mainPanel.add(sourcePanel, BorderLayout.LINE_START);

		// pay attention to which table selected to know what kind of data
		// source
		sourcePanel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (sourcePanel.getSelectedComponent().equals(filePanel)) {
					mode = TEXT_MODE;
				} else {
					mode = DB_MODE;
				}
				updateUI();
				tabber.setSelectedIndex(0);
			}
		});

		// query ui stuff
		queryPanel = new JPanel(new GridBagLayout());
		queryPanel.setBorder(new TitledBorder("Queries to build network file"));

		// hierarchy
		crawlNetwork = new JCheckBox("Crawl relationships to define network",
				false);
		crawlNetwork.addActionListener(this);
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.weighty = 0.1;
		c.fill = GridBagConstraints.NONE;
		queryPanel.add(crawlNetwork, c);
		crawlTimes = new JCheckBox("Also use time values in crawl", false);
		crawlTimes.addActionListener(this);
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		queryPanel.add(crawlTimes, c);

		crawlPanel = new JPanel(new GridLayout(2, 1));
		crawlPanel.setBorder(new TitledBorder("Crawling parameters"));
		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 1;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.BOTH;
		queryPanel.add(crawlPanel, c);
		seedQuery = new JTextArea(
				"put query for initial seed set for crawling here", 2, 50);
		seedQuery.setBorder(new TitledBorder("Seed Node Set Query"));
		crawlPanel.add(seedQuery);
		timeSetQuery = new JTextArea(2, 50);
		timeSetQuery.setBorder(new TitledBorder("Time Values Query"));
		crawlPanel.add(timeSetQuery, c);

		// main arcs query
		arcsQuery = new JTextArea(
				"put relationship query here. \n(when crawling, use "
						+ Getter.nodeIdTag + " for node id value and, "
						+ Getter.timeIdTag + " for time value in query", 8, 50);
		JScrollPane arcsQueryScroller = new JScrollPane(arcsQuery);
		arcsQueryScroller
				.setBorder(new TitledBorder("Arc Relationships Query"));
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 3;
		queryPanel.add(arcsQueryScroller, c);
		runQuery = new JButton("Run Queries");
		runQuery.addActionListener(this);
		c.weighty = 0.1;
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		queryPanel.add(runQuery, c);

		generateNodeset = new JCheckBox("Generate node set from relations",
				true);
		generateNodeset.addActionListener(this);
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 1;
		queryPanel.add(generateNodeset, c);

		generateDateset = new JCheckBox("Also generate times ("
				+ Getter.timeIdTag + ")", false);
		generateDateset.addActionListener(this);
		c.gridx = 2;
		c.gridy = 3;
		c.gridwidth = 1;
		queryPanel.add(generateDateset, c);

		nodePropsQuery = new JTextArea("", 5, 50);
		nodePropsScroller = new JScrollPane(nodePropsQuery);
		nodePropsScroller.setBorder(new TitledBorder(
				"Node Properties Query (use " + Getter.nodeIdTag
						+ " as placeholder for NodeId)"));
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 3;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		queryPanel.add(nodePropsScroller, c);

		// this one is hidden
		nodesQuery = new JTextArea(5, 40);
		nodesScroller = new JScrollPane(nodesQuery);
		nodesScroller.setBorder(new TitledBorder("Node Set Query"));
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 3;
		queryPanel.add(nodesScroller, c);
		// nodesScroller.setVisible(false);

		// TEXT COLUMN MAPPING PANEL

		textMappingPanel = new JPanel(new GridBagLayout());
		textMappingPanel.setBorder(new TitledBorder("Text Column Assignments"));
		arcHeaderColumnUI = updateArcHeaderUI(arcHeaderColumnUI);
		c.gridy = 0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.BOTH;
		textMappingPanel.add(arcHeaderColumnUI, c);
		parserGenerateNodeSet = new JCheckBox("Generate nodes from arc ids",
				true);
		parserGenerateNodeSet.addActionListener(this);
		c.gridwidth = 1;
		c.gridy = 1;
		textMappingPanel.add(parserGenerateNodeSet, c);
		parserGenerateNodeTimes = new JCheckBox("Generate node times", false);
		c.gridx = 1;
		textMappingPanel.add(parserGenerateNodeTimes, c);
		attachNodeProps = new JCheckBox("Attach node properties");
		attachNodeProps.addActionListener(this);
		c.gridx = 2;
		textMappingPanel.add(attachNodeProps, c);

		nodeHeaderColumnUI = updateNodeHeaderUI(nodeHeaderColumnUI);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 3;
		textMappingPanel.add(nodeHeaderColumnUI, c);
		parse = new JButton("Parse File");
		parse.addActionListener(this);
		c.gridy = 3;
		c.fill = GridBagConstraints.NONE;
		textMappingPanel.add(parse, c);

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

		// FILTERS pane to hold data cleaning operations
		filterPanel = new JPanel();
		filterPanel.setBorder(new TitledBorder("Data Cleaning Filters"));
		FilterListModel filterModel = new FilterListModel(song.getAllFilters());
		filterList = new JList(filterModel);
		JScrollPane filterScroller = new JScrollPane(filterList);
		filterScroller.setBorder(new TitledBorder("Available Filters"));
		filterPanel.add(filterScroller, BorderLayout.CENTER);
		runFilter = new JButton("Run Filters");
		runFilter.addActionListener(this);
		filterPanel.add(runFilter, BorderLayout.SOUTH);

		// PREVIEW pane to hold the preview of the file to be written
		previewPanel = new JPanel(new GridBagLayout());
		sonPreview = new JTextArea();
		Font previewFont = new Font("Monospaced", Font.PLAIN, 10);
		sonPreview.setFont(previewFont);
		JScrollPane sonScroller = new JScrollPane(sonPreview);
		sonScroller.setBorder(new TitledBorder("Preview of .son file"));
		// sonScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		previewPanel.add(sonScroller, c);
		JPanel previewButtonPanel = new JPanel();
		validateSon = new JButton("Validate .son");
		validateSon.addActionListener(this);
		previewButtonPanel.add(validateSon);
		saveSon = new JButton("Save as .son file");
		saveSon.addActionListener(this);
		previewButtonPanel.add(saveSon);
		launchSonia = new JButton("Launch in SoNIA");
		launchSonia.addActionListener(this);
		previewButtonPanel.add(launchSonia);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.weighty = 0.0;
		previewPanel.add(previewButtonPanel, c);

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

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setPreferredSize(new Dimension(450, 50));
		progressDialog = new JDialog(frame, "Processing data...", false);
		progressDialog.setLayout(new GridBagLayout());
		progressDialog.setSize(500, 200);
		progressDialog.setLocationRelativeTo(null);
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		progressDialog.add(progressBar, c);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		c.gridy = 1;
		c.fill = GridBagConstraints.NONE;
		progressDialog.add(cancel, c);

		// read default properteis for db connection saved from last session
		readProperties();
		updateUI();
	}

	public void showDialog() {
		frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		progressDialog.setVisible(true);
		// progressBar.setIndeterminate(true);
	}

	public void hideDialog() {
		progressDialog.setVisible(false);
		frame.setCursor(Cursor.getDefaultCursor());
	}

	public void showProgress(int max, int current, String status) {
		if (max == -1 & current == -1) {
			// progressBar.setIndeterminate(true);
		} else {
			// progressBar.setIndeterminate(false);
			progressBar.setMaximum(max);
			progressBar.setValue(current);
			progressBar.setString(status);
		}
	}

	public void run() {
		frame.setVisible(true);
		updateUI();
	}

	public void showStatus(String status) {
		statusText.append(status + "\n");
		statusText.setCaretPosition(statusText.getText().length());
		if (progressDialog.isVisible()) {
			progressBar.setString(status);
		}
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
			song.connectToDB(host.getText(), port.getText(), dbName.getText(),
					user.getText(), password.getText());
			password.setText("");
			updateUI();
		} else if (e.getSource() == openArcFile) {
			JFileChooser fc = new JFileChooser(new File(arcFileLocation
					.getText()));
			// Show Open dialog; this method does not return until the dialog is
			// closed
			int state = fc.showOpenDialog(frame);
			if (state == JFileChooser.APPROVE_OPTION) {
				String pickedFile = fc.getSelectedFile().getAbsolutePath();
				arcFileLocation.setText(pickedFile);
				arcFileLocation.setToolTipText(pickedFile);
				song.connectToFile(pickedFile, getDelimiter(), true);
			}
			updateUI();
		} else if (e.getSource() == openNodeFile) {
			JFileChooser fc = new JFileChooser(new File(nodeFileLocation
					.getText()));
			// Show Open dialog; this method does not return until the dialog is
			// closed
			int state = fc.showOpenDialog(frame);
			if (state == JFileChooser.APPROVE_OPTION) {
				String pickedFile = fc.getSelectedFile().getAbsolutePath();
				nodeFileLocation.setText(pickedFile);
				nodeFileLocation.setToolTipText(pickedFile);
				song.connectToFile(pickedFile, getDelimiter(), false);
			}
			updateUI();
		} else if (e.getSource() == runQuery) {
			Thread task = song.getFetchThread();
			task.start();
		} else if (e.getSource() == parse) {
			Thread task = song.getParseThread();
			task.start();
		} else if (e.getSource() == validateSon) {
			// TODO:make sure any edits have been saved back
			song.validateSon();
			updateUI();
		} else if (e.getSource() == saveSon) {
			String filename = ".son";
			JFileChooser fc = new JFileChooser(new File(filename));
			// Show save dialog; this method does not return until the dialog is
			// closed
			fc.showSaveDialog(frame);
			String pickedFile = fc.getSelectedFile().getAbsolutePath();
			if (pickedFile != null) {
				song.saveAsSon(pickedFile);
			}
			updateUI();
		} else if (e.getSource() == runFilter) {
			song.filterData(filterList.getSelectedValues());
			updateUI();
		} else if (e.getSource() == generateNodeset) {
			updateUI();
		} else if (e.getSource() == cancel) {
			song.stop();
			updateUI();
		} else if (e.getSource() == launchSonia) {
			// launch sonia as an external java application
			// TODO: do this by passing the string or data source diretly
			try {
				String tempfile = File.createTempFile("temp", ".son")
						.getAbsolutePath();
				song.saveAsSon(tempfile);
				String[] args = new String[] { "file:" + tempfile };
				SoniaController.main(args);
			} catch (IOException e1) {
				song.error("Unable to create temp file to pass data to SoNIA");
			}
			song
					.status("Launched SoNIA with .son data. (Quitting sonG will also quit SoNIA)");
			updateUI();
		} else {
			updateUI();
		}

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

		if (mode == DB_MODE) {
			// check if we need to switch panels
			if (tabber.getComponentAt(0) != queryPanel) {
				tabber.remove(textMappingPanel);
				tabber.insertTab("Queries", null, queryPanel, null, 0);
			}
			if (generateNodeset.isSelected()) {
				nodePropsScroller.setVisible(true);
				nodesScroller.setVisible(false);
				generateDateset.setVisible(true);
			} else {
				nodePropsScroller.setVisible(false);
				nodesScroller.setVisible(true);
				generateDateset.setVisible(false);
			}
			if (crawlNetwork.isSelected()) {
				crawlPanel.setVisible(true);
				crawlTimes.setVisible(true);
			} else {
				crawlPanel.setVisible(false);
				crawlTimes.setVisible(false);
			}
			if (crawlTimes.isSelected()) {
				timeSetQuery.setVisible(true);
			} else {
				timeSetQuery.setVisible(false);
			}
			crawlPanel.validate();
			queryPanel.validate();
		} else {
			// check if we need to switch modes
			if (tabber.getComponentAt(0) != textMappingPanel) {
				tabber.remove(queryPanel);
				tabber.insertTab("Input Columns", null, textMappingPanel, null,
						0);
			}
			if (parserGenerateNodeSet.isSelected()) {
				parserGenerateNodeTimes.setVisible(true);
				attachNodeProps.setVisible(true);
			} else {
				parserGenerateNodeTimes.setVisible(false);
				attachNodeProps.setVisible(false);
			}
			// hide and show the nodes panel depending if its needed
			if (!parserGenerateNodeSet.isSelected()
					| attachNodeProps.isSelected()) {
				nodeHeaderColumnUI = updateNodeHeaderUI(nodeHeaderColumnUI);
				nodeHeaderColumnUI.setVisible(true);
			} else {
				nodeHeaderColumnUI.setVisible(false);
			}
			arcHeaderColumnUI = updateArcHeaderUI(arcHeaderColumnUI);

			textMappingPanel.validate();
		}

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
		dataPanel.validate();
	}

	/**
	 * create the component used to display arc file headers and mappings
	 */
	private JPanel updateArcHeaderUI(JPanel arcHeaderColPanel) {
		if (arcHeaderColPanel == null) {
			arcHeaderColPanel = new JPanel();
		}
		arcHeaderColPanel.removeAll();
		arcHeaderColPanel.setBorder(new TitledBorder("Arc File Headers"));
		// loop over headers and create one for each
		Iterator<String> headerIter = song.getArcHeaders().iterator();
		// check if there are none
		if (!headerIter.hasNext()) {
			JLabel noData = new JLabel(
					"Locate a file to import relationship data from");
			arcHeaderColPanel.add(noData);
		} else {
			while (headerIter.hasNext()) {
				String header = headerIter.next();
				JComboBox colUI = new JComboBox(new String[] { header,
						IGNORE_STRING });
				colUI.setName(header);
				arcHeaderColPanel.add(colUI);
			}
		}

		return arcHeaderColPanel;
	}

	/**
	 * create the component used to display arc file headers and mappings
	 */
	private JPanel updateNodeHeaderUI(JPanel nodeHeaderColPanel) {
		if (nodeHeaderColPanel == null) {
			nodeHeaderColPanel = new JPanel();
		}
		nodeHeaderColPanel.removeAll();
		nodeHeaderColPanel.setBorder(new TitledBorder("Node File Headers"));
		// loop over headers and create one for each
		Iterator<String> headerIter = song.getNodeHeaders().iterator();
		// check if there are none
		if (!headerIter.hasNext()) {
			JLabel noData = new JLabel("Locate a file to import node data from");
			nodeHeaderColPanel.add(noData);
		} else {
			while (headerIter.hasNext()) {
				String header = headerIter.next();
				JComboBox colUI = new JComboBox(new String[] { header,
						IGNORE_STRING });
				colUI.setName(header);
				nodeHeaderColPanel.add(colUI);
			}
		}

		return nodeHeaderColPanel;
	}

	public HashSet<String> getNodeIgnoreCols() {
		HashSet<String> ignoreSet = new HashSet<String>();
		Component[] arcCols = nodeHeaderColumnUI.getComponents();
		for (int c = 0; c < arcCols.length; c++) {
			if (arcCols[c] instanceof JComboBox) {
				JComboBox col = (JComboBox) arcCols[c];
				if (col.getSelectedItem().equals(IGNORE_STRING)) {
					ignoreSet.add(col.getName());
				}
			}
		}
		return ignoreSet;
	}

	public HashSet<String> getArcIgnoreCols() {
		HashSet<String> ignoreSet = new HashSet<String>();
		Component[] nodeCols = arcHeaderColumnUI.getComponents();
		for (int c = 0; c < nodeCols.length; c++) {
			if (nodeCols[c] instanceof JComboBox) {
				JComboBox col = (JComboBox) nodeCols[c];
				if (col.getSelectedItem().equals(IGNORE_STRING)) {
					ignoreSet.add(col.getName());
				}
			}
		}
		return ignoreSet;
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
			// save out database properties to use next time
			saveProperties();
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

	public String getArcsQuery() {
		return arcsQuery.getText();
	}

	public boolean isGenerateNodeset() {
		if (mode == DB_MODE) {
			return generateNodeset.isSelected();
		} else {
			return parserGenerateNodeSet.isSelected();
		}
	}

	public String getArcPath() {
		return arcFileLocation.getText();
	}

	public String getNodePath() {
		return nodeFileLocation.getText();
	}

	public String getNodePropsQuery() {
		return nodePropsQuery.getText();
	}

	public String getNodesQuery() {
		return nodesQuery.getText();
	}

	public boolean isGenerateDateset() {
		if (mode == DB_MODE) {
			return generateDateset.isSelected();
		} else {
			return parserGenerateNodeTimes.isSelected();
		}
	}

	public boolean isGenerateNodeProps() {
		return attachNodeProps.isSelected();
	}

	public String getTimeSetQuery() {
		return timeSetQuery.getText();
	}

	public boolean isCrawlNetwork() {
		return crawlNetwork.isSelected();
	}

	public boolean isCrawlTimes() {
		return crawlTimes.isSelected();
	}

	public String getSeedQuery() {
		return seedQuery.getText();
	}

	public String getDelimiter() {
		return delimiters[delimeter.getSelectedIndex()];
	}

	/**
	 * write out a properties object to save data between sessions
	 * 
	 */
	public void saveProperties() {
		props = new Properties();
		props.setProperty("db_host", host.getText());
		props.setProperty("db_port", port.getText());
		props.setProperty("db_name", dbName.getText());
		props.setProperty("db_user", user.getText());
		props.setProperty("arcs_query", arcsQuery.getText());
		props.setProperty("node_properties_query", nodePropsQuery.getText());
		props.setProperty("nodes_query", nodesQuery.getText());
		props.setProperty("seed_set_query", seedQuery.getText());
		props.setProperty("time_values_query", timeSetQuery.getText());
		// also read in boolean vals

		// save any file-related properties
		props.setProperty("edge_file_path", arcFileLocation.getText());
		props.setProperty("delimiter", delimeter.getSelectedItem().toString());
		props.setProperty("node_file_path", nodeFileLocation.getText());

		FileOutputStream out;
		try {
			out = new FileOutputStream(propertiesFileName);
			props.store(out, "default values for the database connection");
			out.close();
		} catch (FileNotFoundException e) {
			showStatus("Error saving properties: " + e.getCause());
		} catch (IOException e) {
			showStatus("Error saving properties: " + e.getCause());
		}
	}

	/**
	 * read in stored properties from previous session
	 * 
	 */
	public void readProperties() {
		// create and load default properties
		props = new Properties();
		FileInputStream in;
		try {
			in = new FileInputStream(propertiesFileName);
			props.load(in);
			in.close();
			host.setText(props.getProperty("db_host"));
			port.setText(props.getProperty("db_port"));
			dbName.setText(props.getProperty("db_name"));
			user.setText(props.getProperty("db_user"));
			arcsQuery.setText(props.getProperty("arcs_query"));
			nodePropsQuery.setText(props.getProperty("node_properties_query"));
			nodesQuery.setText(props.getProperty("nodes_query"));
			seedQuery.setText(props.getProperty("seed_set_query"));
			timeSetQuery.setText(props.getProperty("time_values_query"));
			// do file related props
			arcFileLocation.setText(props.getProperty("edge_file_path"));
			if (props.getProperty("delimiter") != null) {
				delimeter.setSelectedItem(props.getProperty("delimiter"));
			}
			nodeFileLocation.setText(props.getProperty("node_file_path"));
		} catch (FileNotFoundException e) {
			showStatus("Error location default properties file: "
					+ e.getCause());
		} catch (IOException e) {
			showStatus("Error reading default properties: " + e.getCause());
		}

	}

}
