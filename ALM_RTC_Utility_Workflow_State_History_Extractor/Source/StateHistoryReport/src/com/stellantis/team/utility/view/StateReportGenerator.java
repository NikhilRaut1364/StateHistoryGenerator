package com.stellantis.team.utility.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.stellantis.team.utility.controller.LoginController;
import com.stellantis.team.utility.model.Choice;
import com.stellantis.team.utility.model.DateTimeFormat;
import com.stellantis.team.utility.model.KeyValuePair;
import com.stellantis.team.utility.model.ProjectAreaPair;
import com.stellantis.team.utility.model.Status;
import com.stellantis.team.utility.service.FetchQueryWorker;
import com.stellantis.team.utility.service.FetchStatesWorker;
import com.stellantis.team.utility.service.FetchWorkItemTypeFromQuery;
import com.stellantis.team.utility.service.GenerateReportWorker;
import com.stellantis.team.utility.service.LoginWorker;
import com.stellantis.team.utility.service.WorkItemTypeWorker;
import com.stellantis.team.utility.utils.CommonUtils;
import com.stellantis.team.utility.utils.CustomLogger;
import com.stellantis.team.utility.utils.ExtractProperties;
import com.stellantis.team.utility.utils.UtilityConstants;

@SuppressWarnings("serial")
public class StateReportGenerator extends JFrame {

	private JPanel contentPane;
	private JTextField txtUsername;
	private JPasswordField txtPassword;
	private JTextField txtSearchStates;
	private JTextField txtSearchSelectedStates;
	private JTextField txtOutputFilePath;
	private JComboBox<KeyValuePair> cmbServerList;
	private JButton btnLoginLogout;
	private JLabel lblLogs;
	private JLabel lblHelp;
	private JComboBox<ProjectAreaPair> cmbProjectArea;
	private JComboBox<KeyValuePair> cmbWorkitemType;
	private JList<String> listStates;
	private DefaultListModel<String> listStatesModel = new DefaultListModel<>();
	private JButton btnAddAll;
	private JButton btnAdd;
	private JButton btnRemove;
	private JButton btnRemoveAll;
	private JList<String> listSelectedStateForReport;
	private DefaultListModel<String> listSelectedStateForReportModel = new DefaultListModel<>();
	private JButton btnChoose;
	private JButton btnCancel;
	private JButton btnGenerate;
	private JTable tblNotification;
	private List<String> lstAllStateOriginal = new ArrayList<>();
	private List<String> lstStateReportOriginal = new ArrayList<>();
	private JPanel pnlProgress;
	private JLabel lblChoice;
	private JComboBox<String> cmbChoice;
	private JLabel lblWorkItemQuery;
	private JComboBox<KeyValuePair> cmbQuery;
	private JComboBox<String> cmbDateFormat;
	private JPanel pnlWorkItemQuery;
	private JPanel pnlWorkitemType;
	private JPanel pnlProjectAndType;
	public static JProgressBar reportProgress;
	private Dimension operationButtonSize = new Dimension(75, 35);
	public static volatile boolean isCancelled = false;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ExtractProperties.getInstance();
					StateReportGenerator frame = new StateReportGenerator();
					frame.setVisible(true);
				} catch (Exception e) {
					CustomLogger.logException(e);
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public StateReportGenerator() {
		CommonUtils.setUIFont(new javax.swing.plaf.FontUIResource(UtilityConstants.FONT_NAME, Font.PLAIN, 18));
		setResizable(false);
		setTitle(UtilityConstants.UTILITY_NAME + " - " + UtilityConstants.UTILITY_VERSION);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1342, 778);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JPanel pnlLogin = new JPanel();
		pnlLogin.setBackground(Color.WHITE);
		pnlLogin.setBorder(
				new TitledBorder(null, UtilityConstants.LOGIN, TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPane.add(pnlLogin, BorderLayout.NORTH);
		GridBagLayout gbl_pnlLogin = new GridBagLayout();
		gbl_pnlLogin.columnWidths = new int[] { 0, 0, 0, 0, 30, 30, 0, 30, 0, 30 };
		gbl_pnlLogin.rowHeights = new int[] { 0, 0 };
		gbl_pnlLogin.columnWeights = new double[] { 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_pnlLogin.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		pnlLogin.setLayout(gbl_pnlLogin);

		cmbServerList = new JComboBox<KeyValuePair>();
		bindServerComboBox(cmbServerList);
		cmbServerList.setToolTipText(UtilityConstants.SELECT_SERVER);
		GridBagConstraints gbc_cmbServerList = new GridBagConstraints();
		gbc_cmbServerList.insets = new Insets(0, 0, 0, 5);
		gbc_cmbServerList.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbServerList.gridx = 0;
		gbc_cmbServerList.gridy = 0;
		pnlLogin.add(cmbServerList, gbc_cmbServerList);

		txtUsername = new JTextField();
		txtUsername.setToolTipText(UtilityConstants.USERNAME);
		GridBagConstraints gbc_txtUsername = new GridBagConstraints();
		gbc_txtUsername.insets = new Insets(0, 0, 0, 5);
		gbc_txtUsername.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtUsername.gridx = 1;
		gbc_txtUsername.gridy = 0;
		pnlLogin.add(txtUsername, gbc_txtUsername);
		txtUsername.setColumns(10);

		txtPassword = new JPasswordField();
		txtPassword.setToolTipText(UtilityConstants.PASSWORD);
		GridBagConstraints gbc_txtPassword = new GridBagConstraints();
		gbc_txtPassword.insets = new Insets(0, 0, 0, 5);
		gbc_txtPassword.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPassword.gridx = 2;
		gbc_txtPassword.gridy = 0;
		pnlLogin.add(txtPassword, gbc_txtPassword);

		btnLoginLogout = new JButton(UtilityConstants.LOGIN);
		btnLoginLogout.setBackground(Color.LIGHT_GRAY);
		CommonUtils.setButtonFontToBold(btnLoginLogout);
		btnLoginLogout.setToolTipText(UtilityConstants.LOGIN);
		GridBagConstraints gbc_btnLoginLogout = new GridBagConstraints();
		gbc_btnLoginLogout.insets = new Insets(0, 0, 0, 5);
		gbc_btnLoginLogout.gridx = 3;
		gbc_btnLoginLogout.gridy = 0;
		pnlLogin.add(btnLoginLogout, gbc_btnLoginLogout);
		componentActionHandler(btnLoginLogout);
		
		lblLogs = new JLabel(UtilityConstants.LOGS);
		lblLogs.setToolTipText(UtilityConstants.LOGS);
		GridBagConstraints gbc_lblLogs = new GridBagConstraints();
		gbc_lblLogs.insets = new Insets(0, 0, 0, 5);
		gbc_lblLogs.gridx = 6;
		gbc_lblLogs.gridy = 0;
		pnlLogin.add(lblLogs, gbc_lblLogs);
		lblLogs.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				lblLogs.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				lblLogs.setCursor(Cursor.getDefaultCursor());
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				CommonUtils.openNotepadFile(UtilityConstants.EXCEPTION_FILE_PATH);
			}
		});

		lblHelp = new JLabel(UtilityConstants.HELP);
		lblHelp.setToolTipText(UtilityConstants.HELP);
		GridBagConstraints gbc_lblHelp = new GridBagConstraints();
		gbc_lblHelp.gridx = 8;
		gbc_lblHelp.gridy = 0;
//		pnlLogin.add(lblHelp, gbc_lblHelp);

		JPanel pnlConatiner = new JPanel();
		pnlConatiner.setBackground(Color.WHITE);
		pnlConatiner.setBorder(new TitledBorder(null, UtilityConstants.REPORT_GENERATOR, TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		contentPane.add(pnlConatiner, BorderLayout.CENTER);
		pnlConatiner.setLayout(new BorderLayout(5, 5));

		pnlProjectAndType = new JPanel();
		pnlProjectAndType.setBackground(Color.WHITE);
		pnlConatiner.add(pnlProjectAndType, BorderLayout.NORTH);
		pnlProjectAndType.setLayout(new GridLayout(1, 0, 5, 5));

		JPanel pnlProjectArea = new JPanel();
		pnlProjectArea.setBackground(Color.WHITE);
		pnlProjectAndType.add(pnlProjectArea);
		pnlProjectArea.setLayout(new BorderLayout(0, 2));

		JLabel lblProjectArea = new JLabel(UtilityConstants.SELECT_PROJECT_AREA);
		pnlProjectArea.add(lblProjectArea, BorderLayout.NORTH);

		cmbProjectArea = new JComboBox<ProjectAreaPair>();
		cmbProjectArea.setToolTipText(UtilityConstants.SELECT_PROJECT_AREA);
		cmbProjectArea.addItem(new ProjectAreaPair(UtilityConstants.SELECT, null));
		pnlProjectArea.add(cmbProjectArea, BorderLayout.SOUTH);
		componentActionHandler(cmbProjectArea);
		
		JPanel pnlChoice = new JPanel();
		pnlChoice.setBackground(Color.WHITE);
		pnlProjectAndType.add(pnlChoice);
		pnlChoice.setLayout(new BorderLayout(0, 0));
		
		lblChoice = new JLabel("Select Input Report Type");
		pnlChoice.add(lblChoice, BorderLayout.NORTH);
		
		cmbChoice = new JComboBox<String>();
		for (Choice choice : Choice.values()) {
			cmbChoice.addItem(choice.getValue());
		}
		cmbChoice.setToolTipText("Select Input Report Type");
		pnlChoice.add(cmbChoice, BorderLayout.SOUTH);
		componentActionHandler(cmbChoice);
		
		
		pnlWorkItemQuery = new JPanel();
		pnlWorkItemQuery.setBackground(Color.WHITE);
//		pnlProjectAndType.add(pnlWorkItemQuery);
		pnlWorkItemQuery.setLayout(new BorderLayout(0, 0));
		
		lblWorkItemQuery = new JLabel("Select WorkItem Query");
		pnlWorkItemQuery.add(lblWorkItemQuery, BorderLayout.NORTH);
		
		cmbQuery = new JComboBox<KeyValuePair>();
		cmbQuery.setToolTipText("Select WorkItem Query");
		pnlWorkItemQuery.add(cmbQuery, BorderLayout.SOUTH);
		cmbQuery.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ProjectAreaPair selectedProjectArea = (ProjectAreaPair) cmbProjectArea.getSelectedItem();
				KeyValuePair selectedQuery = (KeyValuePair) cmbQuery.getSelectedItem();
				if (selectedProjectArea != null && selectedQuery != null) {
					setComponentsEnabled(false, cmbProjectArea, cmbChoice, cmbQuery, cmbWorkitemType);
					FetchWorkItemTypeFromQuery fetchWorkItemTypeFromQuery = new FetchWorkItemTypeFromQuery(selectedProjectArea.getProjectAreaObj(),
							selectedQuery.getValue()) {
						@Override
						protected void done(){
							try {
								boolean isValid = get();
								if (isValid) {
									List<KeyValuePair> workItemTypes = getLstWorkitemTypeFromQuery();
									cmbWorkitemType.removeAllItems();
									cmbWorkitemType.addItem(new KeyValuePair(UtilityConstants.SELECT, null));
									for (KeyValuePair keyValuePair : workItemTypes) {
										cmbWorkitemType.addItem(keyValuePair);
									}
									Notification.addMessage(Status.SUCCESSFUL.toString(),
											"WorkItem Types fetched successfully");
								}
								setComponentsEnabled(true, cmbProjectArea, cmbWorkitemType, cmbChoice, cmbQuery);
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (ExecutionException e) {
								e.printStackTrace();
							}
						}
					};
					fetchWorkItemTypeFromQuery.execute();
				}
			}
		});
		
		pnlWorkitemType = new JPanel();
		pnlWorkitemType.setBackground(Color.WHITE);
//		pnlProjectAndType.add(pnlWorkitemType);
		pnlWorkitemType.setLayout(new BorderLayout(0, 2));

		JLabel lblWorkitemType = new JLabel(UtilityConstants.SELECT_WORKITEM_TYPE);
		pnlWorkitemType.add(lblWorkitemType, BorderLayout.NORTH);

		cmbWorkitemType = new JComboBox<KeyValuePair>();
		cmbWorkitemType.setToolTipText(UtilityConstants.SELECT_WORKITEM_TYPE);
		cmbWorkitemType.addItem(new KeyValuePair(UtilityConstants.SELECT, null));
		pnlWorkitemType.add(cmbWorkitemType, BorderLayout.SOUTH);
		componentActionHandler(cmbWorkitemType);
		
		JPanel pnlStatesGenerator = new JPanel();
		pnlStatesGenerator.setBackground(Color.WHITE);
		pnlConatiner.add(pnlStatesGenerator, BorderLayout.CENTER);
		pnlStatesGenerator.setLayout(new GridLayout(1, 0, 0, 0));

		JPanel pnlState = new JPanel();
		pnlState.setBackground(Color.WHITE);
		pnlState.setBorder(new TitledBorder(null, UtilityConstants.SELECT_STATES, TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		pnlStatesGenerator.add(pnlState);
		pnlState.setLayout(new BorderLayout(0, 3));

		txtSearchStates = new JTextField();
		txtSearchStates.setToolTipText(UtilityConstants.SEARCH_STATES);
		pnlState.add(txtSearchStates, BorderLayout.NORTH);
		txtSearchStates.setColumns(10);
		txtSearchStates.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				filterList(txtSearchStates.getText(), listStatesModel, lstAllStateOriginal);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				filterList(txtSearchStates.getText(), listStatesModel, lstAllStateOriginal);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				filterList(txtSearchStates.getText(), listStatesModel, lstAllStateOriginal);
			}
		});

		listStates = new JList<String>(listStatesModel);
		JScrollPane scrStatePane = new JScrollPane(listStates);
		pnlState.add(scrStatePane, BorderLayout.CENTER);

		JPanel pnlAddButtons = new JPanel();
		pnlAddButtons.setBackground(Color.WHITE);
		pnlStatesGenerator.add(pnlAddButtons);
		pnlAddButtons.setLayout(new BoxLayout(pnlAddButtons, BoxLayout.Y_AXIS));

		int verticalSpace = 20;

		pnlAddButtons.add(Box.createVerticalGlue());
		btnAddAll = new JButton(UtilityConstants.ADD_ALL_ICON);
		btnAddAll.setMaximumSize(operationButtonSize);
		btnAddAll.setBackground(Color.LIGHT_GRAY);
		CommonUtils.setButtonFontToBold(btnAddAll);
		btnAddAll.setToolTipText(UtilityConstants.ADD_ALL);
		btnAddAll.setAlignmentX(Component.CENTER_ALIGNMENT);
		pnlAddButtons.add(btnAddAll);
		pnlAddButtons.add(Box.createVerticalStrut(verticalSpace));
		componentActionHandler(btnAddAll);
		
		btnAdd = new JButton(UtilityConstants.ADD_ICON);
		btnAdd.setMaximumSize(operationButtonSize);
		btnAdd.setBackground(Color.LIGHT_GRAY);
		CommonUtils.setButtonFontToBold(btnAdd);
		btnAdd.setToolTipText(UtilityConstants.ADD);
		btnAdd.setAlignmentX(Component.CENTER_ALIGNMENT);
		pnlAddButtons.add(btnAdd);
		pnlAddButtons.add(Box.createVerticalStrut(verticalSpace));
		componentActionHandler(btnAdd);
		
		btnRemove = new JButton(UtilityConstants.REMOVE_ICON);
		btnRemove.setMaximumSize(operationButtonSize);
		btnRemove.setBackground(Color.LIGHT_GRAY);
		CommonUtils.setButtonFontToBold(btnRemove);
		btnRemove.setToolTipText(UtilityConstants.REMOVE);
		btnRemove.setAlignmentX(Component.CENTER_ALIGNMENT);
		pnlAddButtons.add(btnRemove);
		pnlAddButtons.add(Box.createVerticalStrut(verticalSpace));
		componentActionHandler(btnRemove);
		
		btnRemoveAll = new JButton(UtilityConstants.REMOVE_ALL_ICON);
		btnRemoveAll.setMaximumSize(operationButtonSize);
		btnRemoveAll.setBackground(Color.LIGHT_GRAY);
		CommonUtils.setButtonFontToBold(btnRemoveAll);
		btnRemoveAll.setToolTipText(UtilityConstants.REMOVE_ALL);
		btnRemoveAll.setAlignmentX(Component.CENTER_ALIGNMENT);
		pnlAddButtons.add(btnRemoveAll);
		pnlAddButtons.add(Box.createVerticalGlue());
		componentActionHandler(btnRemoveAll);
		
		JPanel pnlReportGenerationState = new JPanel();
		pnlReportGenerationState.setBackground(Color.WHITE);
		pnlReportGenerationState.setBorder(new TitledBorder(null, UtilityConstants.REPORT_GENERATION_OF_STATES,
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pnlStatesGenerator.add(pnlReportGenerationState);
		pnlReportGenerationState.setLayout(new BorderLayout(0, 3));

		txtSearchSelectedStates = new JTextField();
		txtSearchSelectedStates.setToolTipText(UtilityConstants.SEARCH_STATES);
		pnlReportGenerationState.add(txtSearchSelectedStates, BorderLayout.NORTH);
		txtSearchSelectedStates.setColumns(10);
		txtSearchSelectedStates.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				filterList(txtSearchSelectedStates.getText(), listSelectedStateForReportModel, lstStateReportOriginal);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				filterList(txtSearchSelectedStates.getText(), listSelectedStateForReportModel, lstStateReportOriginal);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				filterList(txtSearchSelectedStates.getText(), listSelectedStateForReportModel, lstStateReportOriginal);
			}
		});

		listSelectedStateForReport = new JList<String>(listSelectedStateForReportModel);
		JScrollPane scrSelectedStateForReportPane = new JScrollPane(listSelectedStateForReport);
		pnlReportGenerationState.add(scrSelectedStateForReportPane, BorderLayout.CENTER);
		
		JPanel pnlDateFormat = new JPanel();
		pnlDateFormat.setBorder(new TitledBorder(null, "Select Date Format for History", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pnlDateFormat.setBackground(Color.WHITE);
		pnlReportGenerationState.add(pnlDateFormat, BorderLayout.SOUTH);
		pnlDateFormat.setLayout(new BorderLayout(0, 0));
		
		cmbDateFormat = new JComboBox<String>();
		cmbDateFormat.setToolTipText("Select Date Format for History");
		for (DateTimeFormat dateTimeFormat : DateTimeFormat.values()) {
			cmbDateFormat.addItem(dateTimeFormat.getValue());
		}
		pnlDateFormat.add(cmbDateFormat, BorderLayout.NORTH);

		JPanel pnlOutputFile = new JPanel();
		pnlOutputFile.setBackground(Color.WHITE);
		pnlOutputFile.setBorder(new TitledBorder(null, UtilityConstants.OUTPUT_FILE_PATH, TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		pnlConatiner.add(pnlOutputFile, BorderLayout.SOUTH);
		pnlOutputFile.setLayout(new BoxLayout(pnlOutputFile, BoxLayout.X_AXIS));

		txtOutputFilePath = new JTextField();
		txtOutputFilePath.setToolTipText(UtilityConstants.OUTPUT_FILE_PATH);
		pnlOutputFile.add(txtOutputFilePath);
		txtOutputFilePath.setColumns(10);
		txtOutputFilePath.setEditable(false);

		btnChoose = new JButton(UtilityConstants.CHOOSE);
		btnChoose.setBackground(Color.LIGHT_GRAY);
		CommonUtils.setButtonFontToBold(btnChoose);
		btnChoose.setToolTipText(UtilityConstants.CHOOSE);
		pnlOutputFile.add(btnChoose);
		componentActionHandler(btnChoose);
		
		JPanel pnlButtonNotification = new JPanel();
		pnlButtonNotification.setBackground(Color.WHITE);
		contentPane.add(pnlButtonNotification, BorderLayout.SOUTH);
		pnlButtonNotification.setLayout(new BorderLayout(0, 0));

		JPanel pnlButtonContainer = new JPanel();
		pnlButtonContainer.setBackground(Color.WHITE);
		pnlButtonNotification.add(pnlButtonContainer, BorderLayout.NORTH);
		pnlButtonContainer.setLayout(new BorderLayout(0, 0));

		JPanel pnlButton = new JPanel();
		pnlButton.setBackground(Color.WHITE);
		pnlButtonContainer.add(pnlButton, BorderLayout.EAST);
		pnlButton.setLayout(new BoxLayout(pnlButton, BoxLayout.X_AXIS));

		btnCancel = new JButton(UtilityConstants.CANCEL);
		btnCancel.setBackground(Color.LIGHT_GRAY);
		CommonUtils.setButtonFontToBold(btnCancel);
		btnCancel.setToolTipText(UtilityConstants.CANCEL);
		pnlButton.add(btnCancel);
		componentActionHandler(btnCancel);
		
		pnlButton.add(Box.createHorizontalStrut(4));

		btnGenerate = new JButton(UtilityConstants.GENERATE);
		btnGenerate.setBackground(Color.LIGHT_GRAY);
		CommonUtils.setButtonFontToBold(btnGenerate);
		btnGenerate.setToolTipText(UtilityConstants.GENERATE);
		pnlButton.add(btnGenerate);
		componentActionHandler(btnGenerate);
		
		pnlProgress = new JPanel();
		pnlProgress.setBorder(new TitledBorder(null, UtilityConstants.REPORT_PROGRESS, TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		pnlProgress.setBackground(Color.WHITE);
		pnlButtonContainer.add(pnlProgress, BorderLayout.CENTER);
		pnlProgress.setLayout(new GridLayout(0, 1, 0, 0));

		reportProgress = new JProgressBar(0, 100);
		reportProgress.setStringPainted(true);
		reportProgress.setBackground(Color.WHITE);
		pnlProgress.add(reportProgress);

		JPanel pnlNotification = new JPanel();
		pnlNotification.setBackground(Color.WHITE);
		pnlNotification.setBorder(new TitledBorder(null, UtilityConstants.NOTIFICATION, TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		pnlButtonNotification.add(pnlNotification, BorderLayout.CENTER);
		pnlNotification.setLayout(new BorderLayout(0, 0));

		tblNotification = new Notification();
		tblNotification.setBackground(Color.WHITE);

		JScrollPane scrNotificationPane = new JScrollPane(tblNotification);
		scrNotificationPane.setPreferredSize(new Dimension(600, 250));
		pnlNotification.add(scrNotificationPane);
		setComponentsEnabled(false, cmbProjectArea, cmbWorkitemType, txtSearchStates, txtSearchSelectedStates,
				listStates, listSelectedStateForReport, btnChoose, btnCancel, btnGenerate, cmbDateFormat, cmbChoice);
	}

	private void bindServerComboBox(JComboBox<KeyValuePair> comboBox) {
		String[] parts = ExtractProperties.getServerURLList().split(";");

		for (String part : parts) {
			String[] keyValue = part.split("=");
			if (keyValue.length == 2) {
				comboBox.addItem(new KeyValuePair(keyValue[0], keyValue[1]));
			}
		}
	}

	private void filterList(String search, DefaultListModel<String> listModel, List<String> items) {
		listModel.clear();
		if (search.isEmpty()) {
			for (String item : items) {
				listModel.addElement(item);
			}
		} else {
			for (String item : items) {
				if (item.toLowerCase().contains(search.toLowerCase())) {
					listModel.addElement(item);
				}
			}
		}
	}

	private void selectFolderUsingNativeDialog(JTextField selectedFolderTextField, String fileName) {
		File selectedFolder = showNativeFolderDialog(fileName);
		if (selectedFolder != null) {
			selectedFolderTextField.setText(selectedFolder.getAbsolutePath());
		}
	}

	private File showNativeFolderDialog(String fileName) {
		FileDialog fileDialog = new FileDialog((Frame) null, "Select Folder", FileDialog.SAVE);
		fileDialog.setDirectory(System.getProperty("user.home"));
		fileDialog.setFile(fileName + ".xls");
		fileDialog.setVisible(true);

		String directory = fileDialog.getDirectory();
		String file = fileDialog.getFile();

		if (directory != null && file != null) {
			return new File(directory, file);
		}

		return null;
	}

	private void setComponentsEnabled(boolean enabled, JComponent... components) {
		for (JComponent component : components) {
			component.setEnabled(enabled);
		}
	}
	
	private void openNotepadFile(String filePath) {
		try {
			File file = new File(filePath);
			if (file.exists()) {
				Desktop desktop = Desktop.getDesktop();
				desktop.open(file);
			} else {
				Notification.addMessage(Status.INFO.toString(), "File donot exist");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void componentActionHandler(JComponent component){
		if(component instanceof JButton){
			if(component.equals(btnLoginLogout)){
				btnLoginLogout.addActionListener(new ActionListener() {

					@SuppressWarnings("deprecation")
					@Override
					public void actionPerformed(ActionEvent e) {
						setComponentsEnabled(false, cmbServerList, txtUsername, txtPassword, btnLoginLogout);
						KeyValuePair selectedServer = (KeyValuePair) cmbServerList.getSelectedItem();
						String username = txtUsername.getText();
						String password = txtPassword.getText();
						if (btnLoginLogout.getText().equals(UtilityConstants.LOGIN)) {
							LoginWorker loginWorker = new LoginWorker(selectedServer, username, password) {
								@Override
								protected void done() {
									try {
										boolean isValid = get();
										if (isValid) {
											List<ProjectAreaPair> allProjectAreas = getFetchAllProjectAreas();
											for (ProjectAreaPair projectAreaPair : allProjectAreas) {
												cmbProjectArea.addItem(projectAreaPair);
											}
											Notification.addMessage(Status.SUCCESSFUL.toString(),
													"Project area fetched successfully. Please select the project area to proceed.");
											setComponentsEnabled(true, cmbProjectArea, cmbWorkitemType, txtSearchStates,
													txtSearchSelectedStates, listStates, listSelectedStateForReport, btnChoose,
													btnGenerate, btnLoginLogout, cmbDateFormat, cmbChoice);

											btnLoginLogout.setText("Logout");
										} else {
											setComponentsEnabled(true, cmbServerList, txtUsername, txtPassword, btnLoginLogout);
										}
									} catch (InterruptedException e) {
										CustomLogger.logException(e);
									} catch (ExecutionException e) {
										CustomLogger.logException(e);
									}
								}
							};
							loginWorker.execute();
						} else if (btnLoginLogout.getText().equals("Logout")) {
							LoginController loginController = new LoginController();
							loginController.logout();
							txtUsername.setText("");
							txtPassword.setText("");

							cmbProjectArea.removeAll();
							cmbProjectArea.addItem(new ProjectAreaPair(UtilityConstants.SELECT, null));
							cmbWorkitemType.removeAll();
							cmbWorkitemType.addItem(new KeyValuePair(UtilityConstants.SELECT, null));
							cmbChoice.setSelectedIndex(0);

							listStatesModel.removeAllElements();
							listSelectedStateForReportModel.removeAllElements();

							txtOutputFilePath.setText("");

							setComponentsEnabled(true, cmbServerList, txtUsername, txtPassword);
							setComponentsEnabled(false, cmbProjectArea, cmbWorkitemType, btnAddAll, btnAdd, btnRemove,
									btnRemoveAll, btnChoose, btnGenerate, cmbDateFormat, cmbChoice);
							btnLoginLogout.setText(UtilityConstants.LOGIN);
						}
					}
				});
			}
			else if(component.equals(btnAddAll)){
				btnAddAll.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (listStatesModel.size() > 0) {
							for (int i = 0; i < listStatesModel.size(); i++) {
								listSelectedStateForReportModel.addElement(listStatesModel.elementAt(i));
								lstStateReportOriginal.add(listStatesModel.elementAt(i));
							}
							listStatesModel.removeAllElements();
							lstAllStateOriginal.clear();
						} else {
							Notification.addMessage(Status.ERROR.toString(), "No Workflow States are present to add");
						}
					}
				});
			}
			else if(component.equals(btnAdd)){
				btnAdd.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						List<String> selectedValues = listStates.getSelectedValuesList();
						if (!selectedValues.isEmpty()) {
							for (String values : selectedValues) {
								listSelectedStateForReportModel.addElement(values);
								lstStateReportOriginal.add(values);
							}
							int[] selectedIndices = listStates.getSelectedIndices();
							for (int i = selectedIndices.length - 1; i >= 0; i--) {
								listStatesModel.remove(selectedIndices[i]);
								lstAllStateOriginal.remove(selectedIndices[i]);
							}
						} else
							Notification.addMessage(Status.ERROR.toString(), "Please select some states to Move");
					}
				});

			}
			else if(component.equals(btnRemove)){
				btnRemove.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						List<String> selectedValues = listSelectedStateForReport.getSelectedValuesList();
						if (!selectedValues.isEmpty()) {
							for (String values : selectedValues) {
								listStatesModel.addElement(values);
								lstAllStateOriginal.add(values);
							}
							int[] selectedIndices = listSelectedStateForReport.getSelectedIndices();
							for (int i = selectedIndices.length - 1; i >= 0; i--) {
								listSelectedStateForReportModel.remove(selectedIndices[i]);
								lstStateReportOriginal.remove(selectedIndices[i]);
							}
						} else
							Notification.addMessage(Status.ERROR.toString(), "Please select some states to Move");
					}
				});
			}
			else if(component.equals(btnRemoveAll)){
				btnRemoveAll.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (listSelectedStateForReportModel.size() > 0) {
							for (int i = 0; i < listSelectedStateForReportModel.size(); i++) {
								listStatesModel.addElement(listSelectedStateForReportModel.elementAt(i));
								lstAllStateOriginal.add(listSelectedStateForReportModel.elementAt(i));
							}
							listSelectedStateForReportModel.removeAllElements();
							lstStateReportOriginal.clear();
						} else {
							Notification.addMessage(Status.ERROR.toString(), "No Workflow States are present to remove");
						}
					}
				});

			}
			else if(component.equals(btnChoose)){
				btnChoose.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (cmbWorkitemType.getSelectedIndex() > 0) {
							KeyValuePair selectedWorkItemType = (KeyValuePair) cmbWorkitemType.getSelectedItem();
							ProjectAreaPair selectedProjectArea = (ProjectAreaPair) cmbProjectArea.getSelectedItem();
							String fileName = selectedProjectArea.getProjectAreaName() + " - "
									+ selectedWorkItemType.getDisplay();
							selectFolderUsingNativeDialog(txtOutputFilePath, fileName);
						}
					}
				});

			}
			else if(component.equals(btnCancel)){
				btnCancel.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						int choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to cancel?",
								"Cancel Confirmation", JOptionPane.YES_NO_OPTION);

						if (choice == JOptionPane.YES_OPTION) {
							Notification.addMessage(Status.INFO.toString(), "Please wait we are Stopping the operation.");
							isCancelled = true;
						}
					}
				});

			}
			else if(component.equals(btnGenerate)){
				btnGenerate.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						ProjectAreaPair selectedProjectArea = (ProjectAreaPair) cmbProjectArea.getSelectedItem();
						KeyValuePair selectedWorkitemType = (KeyValuePair) cmbWorkitemType.getSelectedItem();
						String selectedChoice = (String) cmbChoice.getSelectedItem();
						String dateformat = (String) cmbDateFormat.getSelectedItem();
						String filePath = txtOutputFilePath.getText();
						List<String> lstAllSelectedStates = new ArrayList<>();
						if (listSelectedStateForReportModel.size() > 0) {
							for (int i = 0; i < listSelectedStateForReportModel.size(); i++) {
								lstAllSelectedStates.add(listSelectedStateForReportModel.elementAt(i));
							}
						}
						if (!lstAllSelectedStates.isEmpty()) {
							if (filePath.length() > 0) {
								setComponentsEnabled(true, btnCancel);
								setComponentsEnabled(false, btnLoginLogout, cmbProjectArea, cmbWorkitemType, btnAddAll, btnAdd,
										btnRemoveAll, btnRemove, btnGenerate, btnChoose, cmbDateFormat, cmbChoice, cmbQuery);
								GenerateReportWorker generateReportWorker = new GenerateReportWorker(
										selectedProjectArea.getProjectAreaObj(), selectedWorkitemType.getValue(),
										lstAllSelectedStates, filePath, dateformat, selectedChoice) {
									@Override
									protected void done() {
										try {
											boolean isValid = get();
											if (isValid) {
												cmbProjectArea.setSelectedIndex(0);
												cmbChoice.setSelectedIndex(0);
												cmbWorkitemType.setSelectedIndex(0);
												listStatesModel.removeAllElements();
												listSelectedStateForReportModel.removeAllElements();
												txtOutputFilePath.setText("");
												reportProgress.setValue(0);
												Notification.addMessage(Status.SUCCESSFUL.toString(),
														"Report successfully generated at location: " + filePath);
											} else {
												Notification.addMessage(Status.ERROR.toString(),
														"Report creation unsuccessful.");
											}

											setComponentsEnabled(true, btnLoginLogout, cmbProjectArea, cmbWorkitemType,
													btnAddAll, btnAdd, btnRemoveAll, btnRemove, btnGenerate, btnChoose, cmbDateFormat, cmbChoice, cmbQuery);
											setComponentsEnabled(false, btnCancel);
										} catch (InterruptedException e) {
											CustomLogger.logException(e);
										} catch (ExecutionException e) {
											CustomLogger.logException(e);
										}
									}
								};
								generateReportWorker.execute();
							} else
								Notification.addMessage(Status.ERROR.toString(), "Please select output file path.");
						} else
							Notification.addMessage(Status.ERROR.toString(),
									"Please select add some states to generate the report.");
					}
				});

			}
		}
		else if(component instanceof JComboBox){
			if(component.equals(cmbProjectArea)){
				cmbProjectArea.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						cmbChoice.setSelectedIndex(0);
//						if (cmbProjectArea.getSelectedIndex() > 0) {
//							setComponentsEnabled(false, cmbProjectArea, cmbWorkitemType);
//							ProjectAreaPair selectedProjectArea = (ProjectAreaPair) cmbProjectArea.getSelectedItem();
//							WorkItemTypeWorker workItemTypeWorker = new WorkItemTypeWorker(
//									selectedProjectArea.getProjectAreaObj()) {
//								@Override
//								protected void done() {
//									try {
//										boolean isValid = get();
//										if (isValid) {
//											List<KeyValuePair> listWorkItemTypes = getLstWorkItemType();
//											listStatesModel.removeAllElements();
//											listSelectedStateForReportModel.removeAllElements();
//											cmbWorkitemType.removeAllItems();
//											cmbWorkitemType.addItem(new KeyValuePair(UtilityConstants.SELECT, null));
//											for (KeyValuePair keyValuePair : listWorkItemTypes) {
//												cmbWorkitemType.addItem(keyValuePair);
//											}
//											Notification.addMessage(Status.SUCCESSFUL.toString(),
//													"Work item types fetched successfully. Please select a work item type to retrieve its states.");
//										} else {
//											Notification.addMessage(Status.ERROR.toString(), "Workitem Type not found.");
//										}
//										setComponentsEnabled(true, cmbProjectArea, cmbWorkitemType);
//									} catch (InterruptedException e) {
//										CustomLogger.logException(e);
//									} catch (ExecutionException e) {
//										CustomLogger.logException(e);
//									}
//								}
//							};
//							workItemTypeWorker.execute();
//						}
					}
				});
			}
			else if(component.equals(cmbWorkitemType)){
				cmbWorkitemType.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						setComponentsEnabled(false, cmbProjectArea, cmbWorkitemType);
						if (cmbWorkitemType.getSelectedIndex() > 0) {
							ProjectAreaPair selectedProjectArea = (ProjectAreaPair) cmbProjectArea.getSelectedItem();
							KeyValuePair selectedWorkItemType = (KeyValuePair) cmbWorkitemType.getSelectedItem();
							FetchStatesWorker fetchStatesWorker = new FetchStatesWorker(selectedProjectArea.getProjectAreaObj(),
									selectedWorkItemType.getValue()) {
								@Override
								protected void done() {
									try {
										boolean isValid = get();
										if (isValid) {
											lstAllStateOriginal.clear();
											listStatesModel.removeAllElements();
											listSelectedStateForReportModel.removeAllElements();
											for (String states : getLstWorkflowState()) {
												listStatesModel.addElement(states);
												lstAllStateOriginal.add(states);
											}
											Notification.addMessage(Status.SUCCESSFUL.toString(),
													"Work item states fetched successfully. Please select the states and add them to generate the report.");
										}

										setComponentsEnabled(true, cmbProjectArea, cmbWorkitemType);
									} catch (InterruptedException e) {
										CustomLogger.logException(e);
									} catch (ExecutionException e) {
										CustomLogger.logException(e);
									}
								}
							};
							fetchStatesWorker.execute();
						}
					}
				});
			}
			else if (component.equals(cmbChoice)){
				cmbChoice.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						int selectedProjectAreaIndex = cmbProjectArea.getSelectedIndex();
						if(selectedProjectAreaIndex < 1){
							Notification.addMessage(Status.ERROR.toString(),
									"Please select Project Area to proceed");
							return;
						}
						if(cmbChoice.getSelectedIndex() < 1){
							pnlProjectAndType.remove(pnlWorkItemQuery);
							pnlProjectAndType.remove(pnlWorkitemType);
							revalidate();
							repaint();
						}
						if (cmbChoice.getSelectedIndex() > 0) {
							String selectedItem = (String) cmbChoice.getSelectedItem();
							setComponentsEnabled(false, cmbChoice);
							if (selectedItem.equals(Choice.WORKITEM_QUERY.getValue())) {
								pnlProjectAndType.remove(pnlWorkItemQuery);
								pnlProjectAndType.remove(pnlWorkitemType);
								pnlProjectAndType.add(pnlWorkItemQuery);
								pnlProjectAndType.add(pnlWorkitemType);
								revalidate();
								repaint();
								cmbWorkitemType.removeAllItems();
								ProjectAreaPair projectArea = (ProjectAreaPair) cmbProjectArea.getSelectedItem();
								FetchQueryWorker fetchQueryWorker = new FetchQueryWorker(projectArea.getProjectAreaObj()) {
									@Override
									protected void done() {
										try {
											boolean isValid = get();
											if (isValid) {
												List<KeyValuePair> queryAssignedToProjectArea = getLstQueryAssignedToSelectedProjectArea();
												cmbQuery.removeAllItems();
												cmbQuery.addItem(new KeyValuePair("- Select -", ""));
												for (KeyValuePair keyValuePair : queryAssignedToProjectArea) {
													cmbQuery.addItem(keyValuePair);
												}
											}
											setComponentsEnabled(true, cmbChoice);
										} catch (InterruptedException e) {
											e.printStackTrace();
										} catch (ExecutionException e) {
											e.printStackTrace();
										}
									}
								};
								fetchQueryWorker.execute();
							} else if(selectedItem.equals(Choice.WORKITEM_TYPE.getValue())){
								pnlProjectAndType.remove(pnlWorkItemQuery);
								pnlProjectAndType.remove(pnlWorkitemType);
								pnlProjectAndType.add(pnlWorkitemType);
								revalidate();
								repaint();
								if (cmbProjectArea.getSelectedIndex() > 0) {
									setComponentsEnabled(false, cmbProjectArea, cmbWorkitemType);
									ProjectAreaPair selectedProjectArea = (ProjectAreaPair) cmbProjectArea.getSelectedItem();
									WorkItemTypeWorker workItemTypeWorker = new WorkItemTypeWorker(
											selectedProjectArea.getProjectAreaObj()) {
										@Override
										protected void done() {
											try {
												boolean isValid = get();
												if (isValid) {
													List<KeyValuePair> listWorkItemTypes = getLstWorkItemType();
													listStatesModel.removeAllElements();
													listSelectedStateForReportModel.removeAllElements();
													cmbWorkitemType.removeAllItems();
													cmbWorkitemType.addItem(new KeyValuePair(UtilityConstants.SELECT, null));
													for (KeyValuePair keyValuePair : listWorkItemTypes) {
														cmbWorkitemType.addItem(keyValuePair);
													}
													Notification.addMessage(Status.SUCCESSFUL.toString(),
															"Work item types fetched successfully. Please select a work item type to retrieve its states.");
												} else {
													Notification.addMessage(Status.ERROR.toString(), "Workitem Type not found.");
												}
												setComponentsEnabled(true, cmbProjectArea, cmbWorkitemType, cmbChoice);
											} catch (InterruptedException e) {
												CustomLogger.logException(e);
											} catch (ExecutionException e) {
												CustomLogger.logException(e);
											}
										}
									};
									workItemTypeWorker.execute();
								}
							}
						}
					}
				});
			}
		}
	}
}
