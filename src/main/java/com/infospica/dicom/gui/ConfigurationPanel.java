/**
 * Licensed to the Cirakas Consulting Pvt Ltd under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.cirakas.com/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.infospica.dicom.gui;

import com.infospica.dicom.DicomUploaderCloudApplication;
import com.infospica.dicom.JobsInitializer;
import com.infospica.dicom.config.StartupConfigurationProperties;
import com.infospica.dicom.config.StoreSCPConfigurationProperties;
import com.infospica.dicom.config.StoreSCUConfigurationProperties;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.gui.event.LoginValidationListener;
import com.infospica.dicom.service.CommandService;
import com.infospica.dicom.util.EchoServer;
import org.apache.commons.lang3.SerializationUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

/**
 * @author arun.vs
 */
public class ConfigurationPanel extends JPanel {

    private JTextField scpAETitle;
    private JTextField scpDestPath;
    private JTextField scpLogFolder;
    private JTextField scpCentreStatusApiUrl;
    private JComboBox scpLogLevel;
    private JTextField textField_5;
    private JTextField scuHost;
    private JTextField scuPort;
    private JTextField textField_8;
    private JTextField textField_9;
    private JTextField textField_10;
    private JTextField textKeepImagesFor;
    private JTextField textImageCleanMinute;
    private JTextField textField_13;
    private JTextField textField_14;
    private JTextField textField_15;
    private JTextField textField_16;
    private JTextField scpPort;
    private JTextField scuAETitle;
    private JTextField scuBackup;
    private JTextField scpHost;
    private JTextField scuCallingHost;
    private JTextField scuCallingAETitle;
    private JComboBox cmbCompression;
    private JComboBox cmbStartupType;
    private JTextField txtCommandBin;
    private JTextField scuMaxFilesPerUpload;
    private JComboBox cmbEcho;
    private JCheckBox chkEchoServer;
    private JCheckBox chckbxCompressImagesBefore;
    private JCheckBox chckbxAnnonymizeInstitutionName;
    private JCheckBox chckbxAnnonymizeInstitutionAddress;
    private JCheckBox chckbxAnnonymizePatientId;
    private JCheckBox chckbxAnnonymizePatientName;
    private JCheckBox chckbxAnnonymizeReferringPhysician;
    private JCheckBox chkXMLFromDicom;

    private String passwordSalt = null;
    private String passwordString = null;

    private final JDialog jdialog;
    private final CommandService commandService;

    public ConfigurationPanel(JDialog jdialog, CommandService commandService) {
        this.jdialog = jdialog;
        this.commandService = commandService;
        initComponents();
    }

    private void initComponents() {
        setBounds(-14, -23, 777, 541);
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setLayout(null);

        JTabbedPane tp = new JTabbedPane();
        tp.setBounds(12, 12, 753, 460);

        // Panel 1
        JPanel panel1 = new JPanel();
        tp.add("StartUp", panel1);
        panel1.setLayout(null);

        JLabel lblStartupType = new JLabel("Startup Type");
        lblStartupType.setBounds(36, 27, 107, 15);
        panel1.add(lblStartupType);

        cmbStartupType = new JComboBox();
        cmbStartupType.setModel(new DefaultComboBoxModel(new String[] { "Automatic", "Manual" }));
        cmbStartupType.setSelectedIndex(0);
        cmbStartupType.setBounds(273, 22, 331, 24);
        //cmbStartupType.setSelectedItem(Context.getStartupConfigurationProperties().getStartupType());
        panel1.add(cmbStartupType);

        JLabel lblEchoIntervalIn = new JLabel("Echo Interval in seconds ");
        lblEchoIntervalIn.setBounds(36, 64, 196, 15);
        panel1.add(lblEchoIntervalIn);

        cmbEcho = new JComboBox();
        cmbEcho.setModel(new DefaultComboBoxModel(new String[] {"60", "600", "1800", "3600"}));
        cmbEcho.setSelectedIndex(0);
        cmbEcho.setBounds(273, 59, 331, 24);
        //cmbEcho.setSelectedItem(Context.getStartupConfigurationProperties().getEchoInterval());
        panel1.add(cmbEcho);

        chkEchoServer = new JCheckBox("Echo DICOM Server");
        chkEchoServer.setBounds(36, 109, 192, 23);
        //chkEchoServer.setSelected(Context.getStartupConfigurationProperties().getEchoServer());
        panel1.add(chkEchoServer);

        chckbxCompressImagesBefore = new JCheckBox("Compress Images Before Uploading");
        chckbxCompressImagesBefore.setBounds(307, 109, 353, 23);
        //chckbxCompressImagesBefore.setSelected(Context.getStoreSCPConfigurationProperties().getDoCompression());
        chckbxCompressImagesBefore.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cmbCompression.setEnabled(chckbxCompressImagesBefore.isSelected());
            }
        });
        panel1.add(chckbxCompressImagesBefore);

        chckbxAnnonymizeInstitutionName = new JCheckBox("Anonymize Institution Name");
        chckbxAnnonymizeInstitutionName.setBounds(36, 149, 250, 23);
        //chckbxAnnonymizeInstitutionName.setSelected(Context.getStoreSCPConfigurationProperties().getAnonymizeInstitutionName());
        panel1.add(chckbxAnnonymizeInstitutionName);

        chckbxAnnonymizeInstitutionAddress = new JCheckBox("Anonymize Institution Address");
        chckbxAnnonymizeInstitutionAddress.setBounds(307, 149, 308, 23);
        //chckbxAnnonymizeInstitutionAddress.setSelected(Context.getStoreSCPConfigurationProperties().getAnonymizeInstitutionAddress());
        panel1.add(chckbxAnnonymizeInstitutionAddress);

        chckbxAnnonymizePatientId = new JCheckBox("Anonymize Patient Id");
        chckbxAnnonymizePatientId.setBounds(36, 187, 250, 23);
        //chckbxAnnonymizePatientId.setSelected(Context.getStoreSCPConfigurationProperties().getAnonymizePatientId());
        panel1.add(chckbxAnnonymizePatientId);

        chckbxAnnonymizePatientName = new JCheckBox("Anonymize Patient Name");
        chckbxAnnonymizePatientName.setBounds(307, 187, 308, 23);
        //chckbxAnnonymizePatientName.setSelected(Context.getStoreSCPConfigurationProperties().getAnonymizePatientName());
        panel1.add(chckbxAnnonymizePatientName);

        chckbxAnnonymizeReferringPhysician = new JCheckBox("Anonymize Referring Physician");
        chckbxAnnonymizeReferringPhysician.setBounds(36, 225, 250, 23);
        //chckbxAnnonymizeReferringPhysician.setSelected(Context.getStoreSCPConfigurationProperties().getAnonymizeReferringPhysician());
        panel1.add(chckbxAnnonymizeReferringPhysician);

        chkXMLFromDicom = new JCheckBox("Extract JSON from DICOM");
        chkXMLFromDicom.setBounds(307, 225, 308, 23);
        //chkXMLFromDicom.setSelected(Context.getStoreSCPConfigurationProperties().getJsonFromDicom());
        panel1.add(chkXMLFromDicom);

        JLabel lblCompressionMethods = new JLabel("Compression Methods");
        lblCompressionMethods.setBounds(36, 279, 196, 15);
        panel1.add(lblCompressionMethods);

        cmbCompression = new JComboBox();
        cmbCompression.setModel(new DefaultComboBoxModel(new String[] { "NONE", "JPEG 2000 Lossless", "JPEG LS Lossless","JPEG 2000 Lossy" }));
        cmbCompression.setSelectedIndex(0);
        cmbCompression.setBounds(273, 274, 331, 24);
        //cmbCompression.setSelectedItem(Context.getStoreSCPConfigurationProperties().getCompressionMethod());
        panel1.add(cmbCompression);

        JLabel lblDicomToolkitVendor = new JLabel("DICOM ToolKit Vendor");
        lblDicomToolkitVendor.setBounds(36, 319, 196, 15);
        panel1.add(lblDicomToolkitVendor);

        JComboBox comboBox_1_1_1_1 = new JComboBox();
        comboBox_1_1_1_1.setEnabled(false);
        comboBox_1_1_1_1.setBounds(273, 314, 331, 24);
        comboBox_1_1_1_1.addItem("DCM4CHE");
        panel1.add(comboBox_1_1_1_1);

        JLabel lblStoreScpExecutable_1_1_2_1_1_2 = new JLabel("DCM4CHE Toolkit Bin Path");
        lblStoreScpExecutable_1_1_2_1_1_2.setBounds(36, 364, 204, 15);
        panel1.add(lblStoreScpExecutable_1_1_2_1_1_2);

        txtCommandBin = new JTextField();
        //txtCommandBin.setText(Context.getStoreSCPConfigurationProperties().getCommandFolder());
        txtCommandBin.setFocusable(false);
        txtCommandBin.setColumns(10);
        txtCommandBin.setBounds(273, 359, 331, 25);
        panel1.add(txtCommandBin);

        JButton btnCommandBin = new JButton("Browse");
        btnCommandBin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser();
                if(!txtCommandBin.getText().isEmpty()) {
                    jfc.setCurrentDirectory(new File(txtCommandBin.getText()));
                } else if(!Context.getStoreSCPConfigurationProperties().getCommandFolder().trim().isEmpty()) {
                    File cfile = new File(Context.getStoreSCPConfigurationProperties().getCommandFolder());
                    if(cfile.exists()) {
                        jfc.setCurrentDirectory(cfile);
                    }
                }
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int option = jfc.showOpenDialog(ConfigurationPanel.this.getParent());
                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = jfc.getSelectedFile();
                    txtCommandBin.setText(file.getAbsolutePath());
                }
            }
        });
        btnCommandBin.setBounds(614, 360, 117, 25);
        panel1.add(btnCommandBin);

        JLabel lblCentreStatusApiUrl = new JLabel("Application Base API URL");
        lblCentreStatusApiUrl.setBounds(36, 404, 204, 15);
        panel1.add(lblCentreStatusApiUrl);

        scpCentreStatusApiUrl = new JTextField();
        scpCentreStatusApiUrl.setColumns(10);
        scpCentreStatusApiUrl.setBounds(273, 399, 458, 25);
        panel1.add(scpCentreStatusApiUrl);

        JButton btnSave = new JButton("Save");
        btnSave.setBounds(25, 485, 117, 25);
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(textKeepImagesFor.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(ConfigurationPanel.this.jdialog, "Keep images duration should not be empty. Enter -1 for skip backup.");
                    return;
                }
                int keepImagesFor = Integer.parseInt(textKeepImagesFor.getText());
                if(keepImagesFor < -1) {
                    JOptionPane.showMessageDialog(ConfigurationPanel.this.jdialog, "Keep images duration should be -1 or 0 to skip backup files.");
                    tp.setSelectedIndex(3);
                    return;
                }
                if(keepImagesFor > 0 && textImageCleanMinute.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(ConfigurationPanel.this.jdialog, "Enter schedule minute [1 - 60].");
                    tp.setSelectedIndex(3);
                    return;
                }
                int imageCleanMinute = Integer.parseInt(textImageCleanMinute.getText());
                if(imageCleanMinute < 1 || imageCleanMinute > 60) {
                    JOptionPane.showMessageDialog(ConfigurationPanel.this.jdialog, "Clean start minute should be between [1 - 60].");
                    tp.setSelectedIndex(3);
                    return;
                }
                int maxUploadCount = scuMaxFilesPerUpload.getText().equalsIgnoreCase("") ? -1 : Integer.parseInt(scuMaxFilesPerUpload.getText());
                if(maxUploadCount < 1) {
                    JOptionPane.showMessageDialog(ConfigurationPanel.this.jdialog, "Maximum files per upload value should not be blank or zero");
                    tp.setSelectedIndex(2);
                    return;
                }

                final JDialog dialog = new JDialog(ConfigurationPanel.this.jdialog, "Login", true);
                LoginPanel loginPanel = new LoginPanel(dialog);
                loginPanel.addLoginValidationListener(loginValidationListener);
                dialog.getContentPane().add(loginPanel);
                dialog.setPreferredSize(new Dimension(560, 165));
                dialog.pack();
                dialog.setLocationRelativeTo(ConfigurationPanel.this.jdialog);
                dialog.setVisible(true);
            }
        });
        add(btnSave);

        JButton btnCancel = new JButton("Close");
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ConfigurationPanel.this.jdialog.dispose();
            }
        });
        btnCancel.setBounds(152, 485, 117, 25);
        add(btnCancel);

        JButton btnReset = new JButton("Reset");
        btnReset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int response = JOptionPane.showConfirmDialog(ConfigurationPanel.this.jdialog, "Are you sure, do you want to reset the configuration?", "Reset configuration", JOptionPane.YES_NO_OPTION);
                if(response == JOptionPane.YES_OPTION) {
                    updateUIValue(Context.getDefaultStartupConfigurationProperties(), Context.getDefaultStoreSCPConfigurationProperties(), Context.getDefaultStoreSCUConfigurationProperties(), true);
                }
            }
        });
        btnReset.setBounds(279, 485, 117, 25);
        add(btnReset);


        ///////////////////////////////////////////////////////////////// END PANEL 1
        ///////////////////////////////////////////////////////////////// /////////////////////////////////////////////////////////

        // Panel 2 starts here
        JPanel panel2 = new JPanel();
        tp.add("Store SCP", panel2);
        panel2.setLayout(null);

        JLabel lblStoreScpExecutable = new JLabel("StoreSCP Executable FIle");
        lblStoreScpExecutable.setBounds(36, 27, 204, 15);
        panel2.add(lblStoreScpExecutable);

        JComboBox comboBox_2 = new JComboBox();
        comboBox_2.setModel(new DefaultComboBoxModel(new String[] {"storescp"}));
        comboBox_2.setSelectedIndex(0);
        comboBox_2.setBounds(273, 22, 331, 25);
        panel2.add(comboBox_2);

        JLabel lblStoreScpExecutable_1 = new JLabel("StoreSCP Executable Port");
        lblStoreScpExecutable_1.setBounds(36, 99, 204, 15);
        panel2.add(lblStoreScpExecutable_1);

        JLabel lblStoreScpExecutable_1_1 = new JLabel("StoreSCP  AE Title");
        lblStoreScpExecutable_1_1.setBounds(36, 135, 204, 15);
        panel2.add(lblStoreScpExecutable_1_1);

        scpAETitle = new JTextField();
        scpAETitle.setBounds(273, 130, 331, 25);
        scpAETitle.setColumns(10);
        //scpAETitle.setText(Context.getStoreSCPConfigurationProperties().getAetName());
        panel2.add(scpAETitle);

        JLabel lblStoreScpExecutable_1_1_1 = new JLabel("StoreSCP  Destination Path");
        lblStoreScpExecutable_1_1_1.setBounds(36, 171, 204, 15);
        panel2.add(lblStoreScpExecutable_1_1_1);

        scpDestPath = new JTextField();
        scpDestPath.setFocusable(false);
        scpDestPath.setColumns(10);
        scpDestPath.setBounds(273, 166, 331, 25);
        //scpDestPath.setText(Context.getStoreSCPConfigurationProperties().getDestinationFolder());
        panel2.add(scpDestPath);

        JButton btnBrowse = new JButton("Browse");
        btnBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser jfc = new JFileChooser();
                if(!scpDestPath.getText().isEmpty()) {
                    jfc.setCurrentDirectory(new File(scpDestPath.getText()));
                } else if(!Context.getStoreSCPConfigurationProperties().getDestinationFolder().trim().isEmpty()) {
                    jfc.setCurrentDirectory(new File(Context.getStoreSCPConfigurationProperties().getDestinationFolder()));
                }
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int option = jfc.showOpenDialog(ConfigurationPanel.this.getParent());
                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = jfc.getSelectedFile();
                    scpDestPath.setText(file.getAbsolutePath());
                }
            }
        });

        btnBrowse.setBounds(614, 166, 117, 25);
        panel2.add(btnBrowse);

        JLabel lblStoreScpExecutable_1_1_2_1 = new JLabel("StoreSCP log level");
        lblStoreScpExecutable_1_1_2_1.setBounds(36, 244, 204, 15);
        //panel2.add(lblStoreScpExecutable_1_1_2_1);

        JLabel lblStoreScpExecutable_1_1_2_1_1 = new JLabel("StoreSCP log folder");
        lblStoreScpExecutable_1_1_2_1_1.setBounds(36, 207, 204, 15);
        //lblStoreScpExecutable_1_1_2_1_1.setBounds(36, 282, 204, 15);
        panel2.add(lblStoreScpExecutable_1_1_2_1_1);

        scpLogFolder = new JTextField();
        scpLogFolder.setFocusable(false);
        scpLogFolder.setColumns(10);
        //scpLogFolder.setBounds(273, 277, 331, 25);
        scpLogFolder.setBounds(273, 202, 331, 25);
        //scpLogFolder.setText(Context.getStoreSCPConfigurationProperties().getLogFolder());
        panel2.add(scpLogFolder);

        JButton btnBrowse_2 = new JButton("Browse");
        btnBrowse_2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser();
                if(!scpLogFolder.getText().isEmpty()) {
                    jfc.setCurrentDirectory(new File(scpLogFolder.getText()));
                } else if(!Context.getStoreSCPConfigurationProperties().getLogFolder().trim().isEmpty()) {
                    jfc.setCurrentDirectory(new File(Context.getStoreSCPConfigurationProperties().getLogFolder()));
                }
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int option = jfc.showOpenDialog(ConfigurationPanel.this.getParent());
                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = jfc.getSelectedFile();
                    scpLogFolder.setText(file.getAbsolutePath());
                }
            }
        });
        //btnBrowse_2.setBounds(614, 278, 117, 25);
        btnBrowse_2.setBounds(614, 202, 117, 25);
        panel2.add(btnBrowse_2);

        scpLogLevel = new JComboBox();
        scpLogLevel.setModel(new DefaultComboBoxModel(new String[] { "Info", "Debug", "Error" }));
        scpLogLevel.setBounds(273, 240, 331, 25);
        scpLogLevel.setSelectedItem(Context.getStoreSCPConfigurationProperties().getDebugLevel());
        //panel2.add(scpLogLevel);

        scpPort = new JTextField();
        scpPort.setBounds(273, 96, 331, 25);
        scpPort.setColumns(10);
        //scpPort.setText(String.valueOf(Context.getStoreSCPConfigurationProperties().getPort()));
        scpPort.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                acceptNumbersOnly(e.getSource(), 6, e, false);
            }
        });
        panel2.add(scpPort);

        JLabel lblStoreScpExecutable_1_3 = new JLabel("StoreSCP Host");
        lblStoreScpExecutable_1_3.setBounds(36, 61, 204, 15);
        panel2.add(lblStoreScpExecutable_1_3);

        scpHost = new JTextField();
        scpHost.setColumns(10);
        scpHost.setBounds(273, 58, 331, 25);
        //scpHost.setText(Context.getStoreSCPConfigurationProperties().getHostName());
        panel2.add(scpHost);

        if(Context.getSCPEchoStatus()) {
            btnBrowse.setEnabled(false);
            scpAETitle.setEnabled(false);
            scpPort.setEnabled(false);
            scpHost.setEnabled(false);
        }

        // panel 3 starts here
        JPanel panel3 = new JPanel();

        tp.add("Store SCU", panel3);
        panel3.setLayout(null);

        JLabel lblStorescuExecutableFile = new JLabel("StoreSCU Executable FIle");
        lblStorescuExecutableFile.setBounds(36, 26, 204, 15);
        panel3.add(lblStorescuExecutableFile);

        JComboBox cmbScuEF = new JComboBox();
        cmbScuEF.setModel(new DefaultComboBoxModel(new String[] {"storescu"}));
        cmbScuEF.setSelectedIndex(0);
        cmbScuEF.setBounds(273, 21, 331, 25);
        panel3.add(cmbScuEF);

        JLabel lblStoreScpExecutable_1_2 = new JLabel("StoreSCU peer Host/IP");
        lblStoreScpExecutable_1_2.setBounds(36, 65, 204, 15);
        panel3.add(lblStoreScpExecutable_1_2);

        scuHost = new JTextField();
        scuHost.setBounds(273, 60, 331, 25);
        scuHost.setColumns(10);
        //scuHost.setText(Context.getStoreSCUConfigurationProperties().getHostName());
        panel3.add(scuHost);

        JLabel lblStoreScpExecutable_1_2_1 = new JLabel("StoreSCU peer port");
        lblStoreScpExecutable_1_2_1.setBounds(36, 101, 204, 15);
        panel3.add(lblStoreScpExecutable_1_2_1);

        scuPort = new JTextField();
        scuPort.setColumns(10);
        scuPort.setBounds(273, 96, 331, 25);
        //scuPort.setText(String.valueOf(Context.getStoreSCUConfigurationProperties().getPort()));
        scuPort.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                acceptNumbersOnly(e.getSource(), 6, e, false);
            }
        });
        panel3.add(scuPort);

        JLabel lblStoreScpExecutable_1_2_1_1 = new JLabel("StoreSCU called AE Title");
        lblStoreScpExecutable_1_2_1_1.setBounds(36, 137, 204, 15);
        panel3.add(lblStoreScpExecutable_1_2_1_1);

        scuAETitle = new JTextField();
        scuAETitle.setColumns(10);
        scuAETitle.setBounds(273, 132, 331, 25);
        //scuAETitle.setText(Context.getStoreSCUConfigurationProperties().getAetName());
        panel3.add(scuAETitle);

        JLabel lblStoreScpExecutable1212 = new JLabel("StoreSCU calling host");
        lblStoreScpExecutable1212.setBounds(36, 171, 204, 15);
        panel3.add(lblStoreScpExecutable1212);

        scuCallingHost = new JTextField();
        scuCallingHost.setColumns(10);
        scuCallingHost.setBounds(273, 166, 331, 25);
        scuCallingHost.setText("");
        scuCallingHost.setEnabled(false);
        panel3.add(scuCallingHost);

        JLabel lblStoreScpExecutable1213 = new JLabel("StoreSCU calling AE Title");
        lblStoreScpExecutable1213.setBounds(36, 205, 204, 15);
        panel3.add(lblStoreScpExecutable1213);

        scuCallingAETitle = new JTextField();
        scuCallingAETitle.setColumns(10);
        scuCallingAETitle.setBounds(273, 200, 331, 25);
        //scuCallingAETitle.setText(Context.getStoreSCUConfigurationProperties().getCallingAetName());
        panel3.add(scuCallingAETitle);

        JLabel lblStoreScpExecutable_1_1_2_1_1_1 = new JLabel("Backup folder");
        lblStoreScpExecutable_1_1_2_1_1_1.setBounds(36, 239, 204, 15);
        panel3.add(lblStoreScpExecutable_1_1_2_1_1_1);

        scuBackup = new JTextField();
        scuBackup.setFocusable(false);
        scuBackup.setColumns(10);
        scuBackup.setBounds(273, 234, 331, 25);
        //scuBackup.setText(Context.getStoreSCUConfigurationProperties().getUploadBackup());
        panel3.add(scuBackup);

        JButton btnBrowse_2_1 = new JButton("Browse");
        btnBrowse_2_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser();
                if(!scuBackup.getText().isEmpty()) {
                    jfc.setCurrentDirectory(new File(scuBackup.getText()));
                } else if(!Context.getStoreSCUConfigurationProperties().getUploadBackup().trim().isEmpty()) {
                    jfc.setCurrentDirectory(new File(Context.getStoreSCUConfigurationProperties().getUploadBackup()));
                }
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int option = jfc.showOpenDialog(ConfigurationPanel.this.getParent());
                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = jfc.getSelectedFile();
                    scuBackup.setText(file.getAbsolutePath());
                }
            }
        });
        btnBrowse_2_1.setBounds(610, 234, 117, 25);
        panel3.add(btnBrowse_2_1);

        JLabel lblFilePickCount = new JLabel("Max files per upload");
        lblFilePickCount.setBounds(36, 278, 204, 15);
        panel3.add(lblFilePickCount);

        scuMaxFilesPerUpload = new JTextField();
        scuMaxFilesPerUpload.setColumns(10);
        scuMaxFilesPerUpload.setBounds(273, 273, 331, 25);
        scuMaxFilesPerUpload.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                acceptNumbersOnly(e.getSource(), 3, e, false);
            }
        });
        panel3.add(scuMaxFilesPerUpload);

        // Panel 4 starts here
        JPanel panel4 = new JPanel();
        //tp.add("Application Logs ", panel4);
        panel4.setLayout(null);

        JLabel lblProcessLogDirectory = new JLabel("Process Log Directory");
        lblProcessLogDirectory.setBounds(36, 38, 190, 15);
        panel4.add(lblProcessLogDirectory);

        textField_8 = new JTextField();
        textField_8.setFocusable(false);
        textField_8.setBounds(273, 36, 331, 25);
        panel4.add(textField_8);
        textField_8.setColumns(10);

        JButton btnBrowse_1 = new JButton("Browse");
        btnBrowse_1.setBounds(614, 33, 117, 25);
        panel4.add(btnBrowse_1);

        JLabel lblErrorLogDirectory = new JLabel("Error Log Directory");
        lblErrorLogDirectory.setBounds(22, 87, 190, 15);
        panel4.add(lblErrorLogDirectory);

        textField_9 = new JTextField();
        textField_9.setFocusable(false);
        textField_9.setColumns(10);
        textField_9.setBounds(211, 85, 396, 19);
        panel4.add(textField_9);

        JButton btnBrowse_1_1 = new JButton("Browse");
        btnBrowse_1_1.setBounds(619, 82, 117, 25);
        panel4.add(btnBrowse_1_1);

        JButton btnSave_1_2 = new JButton("Save");
        btnSave_1_2.setBounds(73, 420, 117, 25);
        panel4.add(btnSave_1_2);

        JButton btnSave_1_1_2 = new JButton("Reset");
        btnSave_1_1_2.setBounds(299, 420, 117, 25);
        panel4.add(btnSave_1_1_2);

        JButton btnSave_1_1_1_1 = new JButton("Close");
        btnSave_1_1_1_1.setBounds(538, 420, 117, 25);
        panel4.add(btnSave_1_1_1_1);

        // panel 5 starts here
        JPanel panel5 = new JPanel();

        tp.add("Image Cleaner", panel5);
        panel5.setLayout(null);

        JLabel lblKeepImageFor = new JLabel("Keep Image for N days");
        lblKeepImageFor.setBounds(32, 26, 204, 15);
        panel5.add(lblKeepImageFor);

        textKeepImagesFor = new JTextField();
        textKeepImagesFor.setColumns(3);
        textKeepImagesFor.setBounds(269, 21, 331, 25);
        //textKeepImagesFor.setText(String.valueOf(Context.getStoreSCUConfigurationProperties().getKeepImagesFor()));
        textKeepImagesFor.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                acceptNumbersOnly(e.getSource(), 3, e, true);
            }
        });
        panel5.add(textKeepImagesFor);

        JLabel lblBeginCleanupAt = new JLabel("Schedule cleanup intervals in \"mm\"");
        lblBeginCleanupAt.setBounds(32, 65, 204, 15);
        panel5.add(lblBeginCleanupAt);

        textImageCleanMinute = new JTextField();
        textImageCleanMinute.setColumns(2);
        //textImageCleanMinute.setText(String.valueOf(Context.getStoreSCUConfigurationProperties().getImageCleanupMinute()));
        textImageCleanMinute.setBounds(269, 60, 331, 25);
        textImageCleanMinute.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                acceptNumbersOnly(e.getSource(), 2, e, false);
            }
        });
        panel5.add(textImageCleanMinute);

        if(Context.isBackupCleanStarted()) {
            JLabel lblMessage = new JLabel("Cannot change the values, backup clean is in progress.");
            lblMessage.setFont(new Font("Courier", Font.BOLD, 12));
            lblMessage.setForeground(Color.red);
            lblMessage.setBounds(32, 410, 400, 15);
            panel5.add(lblMessage);
        }
        /*JButton btnSave_1_2_1 = new JButton("Save");
        btnSave_1_2_1.setBounds(32, 441, 117, 25);
        if(Context.isBackupCleanStarted()) {
            btnSave_1_2_1.setEnabled(false);
        }*/

        // panel 6 starts here
        JPanel panel6 = new JPanel();
        //tp.add("System Information", panel6);
        panel6.setLayout(null);

        JLabel lblNetworkInformation = new JLabel("Network Information");
        lblNetworkInformation.setBounds(28, 25, 169, 15);
        panel6.add(lblNetworkInformation);

        textField_13 = new JTextField();
        textField_13.setBounds(38, 52, 675, 83);
        panel6.add(textField_13);
        textField_13.setColumns(10);

        JLabel lblAvailableDiskSpace = new JLabel("Available DIsk Space ");
        lblAvailableDiskSpace.setBounds(28, 175, 169, 15);
        panel6.add(lblAvailableDiskSpace);

        textField_14 = new JTextField();
        textField_14.setColumns(10);
        textField_14.setBounds(38, 202, 675, 83);
        panel6.add(textField_14);

        JLabel lblUnprocessedImageSpace = new JLabel("Unprocessed Image Space ");
        lblUnprocessedImageSpace.setBounds(39, 323, 222, 15);
        panel6.add(lblUnprocessedImageSpace);

        textField_15 = new JTextField();
        textField_15.setColumns(10);
        textField_15.setBounds(279, 321, 345, 19);
        panel6.add(textField_15);

        JLabel lblProcessedImageSpace = new JLabel("processed Image Space ");
        lblProcessedImageSpace.setBounds(39, 366, 222, 15);
        panel6.add(lblProcessedImageSpace);

        textField_16 = new JTextField();
        textField_16.setColumns(10);
        textField_16.setBounds(279, 364, 345, 19);
        panel6.add(textField_16);

        JButton btnSave_1_2_1_1 = new JButton("Save");
        btnSave_1_2_1_1.setBounds(55, 427, 117, 25);
        panel6.add(btnSave_1_2_1_1);

        JButton btnSave_1_1_2_1_1 = new JButton("Reset");
        btnSave_1_1_2_1_1.setBounds(281, 427, 117, 25);
        panel6.add(btnSave_1_1_2_1_1);

        JButton btnSave_1_1_1_1_1_1 = new JButton("Close");
        btnSave_1_1_1_1_1_1.setBounds(520, 427, 117, 25);
        panel6.add(btnSave_1_1_1_1_1_1);
        add(tp);

        //update the ui value
        updateUIValue(Context.getStartupConfigurationProperties(), Context.getStoreSCPConfigurationProperties(), Context.getStoreSCUConfigurationProperties(), false);
    }

    private void updateUIValue(StartupConfigurationProperties startupConfigurationProperties, StoreSCPConfigurationProperties storeSCPConfigurationProperties,
                               StoreSCUConfigurationProperties storeSCUConfigurationProperties, boolean reset) {
        cmbStartupType.setSelectedItem(startupConfigurationProperties.getStartupType());
        cmbEcho.setSelectedItem(startupConfigurationProperties.getEchoInterval());
        chkEchoServer.setSelected(startupConfigurationProperties.getEchoServer());
        
        // Centre Status API URL from startup configuration
        String apiUrl = startupConfigurationProperties.getCentreStatusApiUrl();
        scpCentreStatusApiUrl.setText(apiUrl != null ? apiUrl : "");
        
        chckbxCompressImagesBefore.setSelected(storeSCPConfigurationProperties.getDoCompression());
        chckbxAnnonymizeInstitutionName.setSelected(storeSCPConfigurationProperties.getAnonymizeInstitutionName());
        chckbxAnnonymizeInstitutionAddress.setSelected(storeSCPConfigurationProperties.getAnonymizeInstitutionAddress());
        chckbxAnnonymizePatientId.setSelected(storeSCPConfigurationProperties.getAnonymizePatientId());
        chckbxAnnonymizePatientName.setSelected(storeSCPConfigurationProperties.getAnonymizePatientName());
        chckbxAnnonymizeReferringPhysician.setSelected(storeSCPConfigurationProperties.getAnonymizeReferringPhysician());
        chkXMLFromDicom.setSelected(storeSCPConfigurationProperties.getJsonFromDicom());
        cmbCompression.setSelectedItem(storeSCPConfigurationProperties.getCompressionMethod());
        try {
            String commandFolder = new File(storeSCPConfigurationProperties.getCommandFolder()).getCanonicalPath();
            txtCommandBin.setText(commandFolder);
        } catch(IOException ioe) {}

        //2
        scpAETitle.setText(storeSCPConfigurationProperties.getAetName());
        try {
            String destinationFolder = new File(storeSCPConfigurationProperties.getDestinationFolder()).getCanonicalPath();
            scpDestPath.setText(destinationFolder);
        } catch(IOException ioe) {}

        try {
            String logFolder = new File(storeSCPConfigurationProperties.getLogFolder()).getCanonicalPath();
            scpLogFolder.setText(logFolder);
        } catch(IOException ioe) {}

        scpPort.setText(String.valueOf(storeSCPConfigurationProperties.getPort()));
        scpHost.setText(storeSCPConfigurationProperties.getHostName());

        //3
        scuHost.setText(storeSCUConfigurationProperties.getHostName());
        scuPort.setText(String.valueOf(storeSCUConfigurationProperties.getPort()));
        scuAETitle.setText(storeSCUConfigurationProperties.getAetName());
        //scuCallingHost.setText(storeSCUConfigurationProperties.getCallingHost());
        scuCallingAETitle.setText(storeSCUConfigurationProperties.getCallingAetName());
        try {
            String scuBackupFolder = new File(storeSCUConfigurationProperties.getUploadBackup()).getCanonicalPath();
            scuBackup.setText(scuBackupFolder);
        } catch(IOException ioe) {}
        scuMaxFilesPerUpload.setText(String.valueOf(storeSCUConfigurationProperties.getMaxFilesPerUpload()));

        //5
        textKeepImagesFor.setText(String.valueOf(storeSCUConfigurationProperties.getKeepImagesFor()));
        textImageCleanMinute.setText(String.valueOf(storeSCUConfigurationProperties.getImageCleanupMinute()));

        if(reset) {
            passwordSalt = startupConfigurationProperties.getSalt();
            passwordString = startupConfigurationProperties.getPassword();
        }
    }

    protected void acceptNumbersOnly(Object textObj, int length, KeyEvent e, boolean acceptNegative) {
        JTextField textField = (JTextField)textObj;
        if (textField.getText().length() >= length) {
            e.consume();
        }
        char c = e.getKeyChar();
        if (!((c >= '0') && (c <= '9') || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE) || (acceptNegative && c == KeyEvent.VK_MINUS))) {
            e.consume();
        }
        if(acceptNegative) {
            int cursorPos = textField.getCaretPosition();
            if(c == KeyEvent.VK_MINUS && cursorPos != 0) {
                e.consume();
            }
        }
    }
    LoginValidationListener loginValidationListener = () -> {
        saveConfiguration();
        //ConfigurationPanel.this.jdialog.dispose();
    };

    public void saveConfiguration() {
        // serialize the object
        byte[] input1 = SerializationUtils.serialize(Context.getStartupConfigurationProperties());
        byte[] input2 = SerializationUtils.serialize(Context.getStoreSCPConfigurationProperties());
        byte[] input3 = SerializationUtils.serialize(Context.getStoreSCUConfigurationProperties());

        String encodedInput1 = Base64.getEncoder().encodeToString(input1);
        String encodedInput2 = Base64.getEncoder().encodeToString(input2);
        String encodedInput3 = Base64.getEncoder().encodeToString(input3);

        Context.getStartupConfigurationProperties().setConfigurationRequired(false);
        Context.getStartupConfigurationProperties().setStartupType(cmbStartupType.getSelectedItem().toString());
        if(passwordString != null && passwordSalt != null) {
            Context.getStartupConfigurationProperties().setSalt(passwordSalt);
            Context.getStartupConfigurationProperties().setPassword(passwordString);
        }
        //Context.getStartupConfigurationProperties().setRetryInterval(Integer.valueOf(cmbStartupRetry.getSelectedItem().toString()));
        Context.getStartupConfigurationProperties().setEchoInterval(Integer.valueOf(cmbEcho.getSelectedItem().toString()));
        Context.getStartupConfigurationProperties().setEchoServer(chkEchoServer.isSelected());
        Context.getStoreSCPConfigurationProperties().setDoCompression(chckbxCompressImagesBefore.isSelected());
        Context.getStoreSCPConfigurationProperties().setAnonymizeInstitutionName(chckbxAnnonymizeInstitutionName.isSelected());
        Context.getStoreSCPConfigurationProperties().setAnonymizeInstitutionAddress(chckbxAnnonymizeInstitutionAddress.isSelected());
        Context.getStoreSCPConfigurationProperties().setAnonymizePatientId(chckbxAnnonymizePatientId.isSelected());
        Context.getStoreSCPConfigurationProperties().setAnonymizePatientName(chckbxAnnonymizePatientName.isSelected());
        Context.getStoreSCPConfigurationProperties().setJsonFromDicom(chkXMLFromDicom.isSelected());
        Context.getStoreSCPConfigurationProperties().setAnonymizeReferringPhysician(chckbxAnnonymizeReferringPhysician.isSelected());
        Context.getStoreSCPConfigurationProperties().setCompressionMethod(cmbCompression.getSelectedItem().toString());

        String commandBinFolder = txtCommandBin.getText();
        if(!commandBinFolder.endsWith(File.separator)) commandBinFolder = commandBinFolder.concat(File.separator);
        Context.getStoreSCPConfigurationProperties().setCommandFolder(commandBinFolder);
        
        // Set commandWorkingFolder to the same as commandFolder if not already set
        if(Context.getStoreSCPConfigurationProperties().getCommandWorkingFolder() == null || 
           Context.getStoreSCPConfigurationProperties().getCommandWorkingFolder().trim().isEmpty()) {
            Context.getStoreSCPConfigurationProperties().setCommandWorkingFolder(commandBinFolder);
        }

        //2
        Context.getStartupConfigurationProperties().setConfigurationRequired(false);
        Context.getStoreSCPConfigurationProperties().setHostName(scpHost.getText());
        Context.getStoreSCPConfigurationProperties().setPort(Integer.valueOf(scpPort.getText()));
        Context.getStoreSCPConfigurationProperties().setAetName(scpAETitle.getText());
        String destinationPath = scpDestPath.getText();
        if(!destinationPath.endsWith(File.separator)) destinationPath = destinationPath.concat(File.separator);
        Context.getStoreSCPConfigurationProperties().setDestinationFolder(destinationPath);

        File destinationFolder = new File(destinationPath);
        String suggestedProcessFolder = new File(destinationFolder.getParentFile(), "process").getAbsolutePath();
        if(!suggestedProcessFolder.endsWith(File.separator)) suggestedProcessFolder = suggestedProcessFolder.concat(File.separator);
        Context.getStoreSCPConfigurationProperties().setProcessFolder(suggestedProcessFolder);

        //Context.getStoreSCPConfigurationProperties().setAllowFork(scpAllowFork.getSelectedItem().toString());
        Context.getStoreSCPConfigurationProperties().setDebugLevel(scpLogLevel.getSelectedItem().toString());
        String logFolder = scpLogFolder.getText();
        if(!logFolder.endsWith(File.separator)) logFolder = logFolder.concat(File.separator);
        Context.getStoreSCPConfigurationProperties().setLogFolder(logFolder);

        // 3
        Context.getStartupConfigurationProperties().setConfigurationRequired(false);
        
        // Centre Status API URL - save to startup configuration
        String centreStatusApiUrl = scpCentreStatusApiUrl.getText();
        if(centreStatusApiUrl != null && !centreStatusApiUrl.trim().isEmpty()) {
            Context.getStartupConfigurationProperties().setCentreStatusApiUrl(centreStatusApiUrl.trim());
        }
        Context.getStoreSCUConfigurationProperties().setHostName(scuHost.getText());
        Context.getStoreSCUConfigurationProperties().setPort(Integer.valueOf(scuPort.getText()));
        Context.getStoreSCUConfigurationProperties().setAetName(scuAETitle.getText());
        Context.getStoreSCUConfigurationProperties().setCallingAetName(scuCallingAETitle.getText());
        //Context.getStoreSCUConfigurationProperties().setCallingHost(scuCallingHost.getText());
        Context.getStoreSCUConfigurationProperties().setCallingHost("");
        Context.getStoreSCUConfigurationProperties().setUploadBackup(scuBackup.getText());
        Context.getStoreSCUConfigurationProperties().setMaxFilesPerUpload(Integer.valueOf(scuMaxFilesPerUpload.getText()));

        // 4
        Context.getStartupConfigurationProperties().setConfigurationRequired(false);
        Context.getStoreSCUConfigurationProperties().setKeepImagesFor(Integer.valueOf(textKeepImagesFor.getText()));
        Context.getStoreSCUConfigurationProperties().setImageCleanupMinute(Integer.valueOf(textImageCleanMinute.getText()));
        try {
            Context.getStartupConfigurationProperties().writeConfiguration();
            Context.getStoreSCPConfigurationProperties().writeConfiguration();
            Context.getStoreSCUConfigurationProperties().writeConfiguration();
        } catch (Exception ioe) {
            ioe.printStackTrace();
            byte[] decodedOutput1 = Base64.getDecoder().decode(encodedInput1);
            Object sobj1 = SerializationUtils.deserialize(decodedOutput1);
            Context.setStartupConfigurationProperties((StartupConfigurationProperties)sobj1);

            byte[] decodedOutput2 = Base64.getDecoder().decode(encodedInput2);
            Object sobj2 = SerializationUtils.deserialize(decodedOutput2);
            Context.setStoreSCPConfigurationProperties((StoreSCPConfigurationProperties)sobj2);

            byte[] decodedOutput3 = Base64.getDecoder().decode(encodedInput3);
            Object sobj3 = SerializationUtils.deserialize(decodedOutput3);
            Context.setStoreSCUConfigurationProperties((StoreSCUConfigurationProperties)sobj3);
        } finally {
            Context.refreshFolderContext();
            commandService.resetAll();
        }
    }
}
