package com.infospica.dicom;

import com.infospica.dicom.context.Context;
import com.infospica.dicom.gui.ChangePasswordPanel;
import com.infospica.dicom.gui.ConfigurationPanel;
import com.infospica.dicom.gui.LoginPanel;
import com.infospica.dicom.gui.SplashScreen;
import com.infospica.dicom.gui.event.LoginValidationListener;
import com.infospica.dicom.process.analytics.monitor.ProcessMonitor;
import com.infospica.dicom.process.analytics.monitor.AnalyticsUpdater;
import com.infospica.dicom.service.CommandService;
import com.infospica.dicom.util.EchoServer;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.PortInUseException;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

@SpringBootApplication
public class DicomUploaderCloudApplication extends JFrame {

	private JPanel contentPane;
	private JLabel lblPending;
	private JLabel lblJSONExtracted;
	private JLabel lblModified;
	private JLabel lblCompressed;
	private JLabel lblBad;
	private JLabel lblGood;
	private JLabel lblProcessed;
	private JLabel lblProcessedError;
	private JLabel lblUploadPending;
	private JButton btnStartServer;
	private JButton btnInitProcess;
	private JLabel lblPacsEchoStatus;
	private JLabel lblRemoteEchoStatus;

	@Autowired
	private JobsInitializer jobsInitializer;

	@Autowired
	private CommandService commandService;

	PopupMenu popup = null;
	MenuItem showMenuItem = null;
	TrayIcon trayIcon = null;
	SystemTray tray = null;

	public DicomUploaderCloudApplication() {
		initUI();
	}

	private void initUI() {
		Image iconImage = Toolkit.getDefaultToolkit().getImage(DicomUploaderCloudApplication.class.getClassLoader().getResource("systray-icon.png"));
		setTitle("eCScribe PACS - Dashboard");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE );
		setIconImage(iconImage);
		setPreferredSize(new Dimension(1020, 311));
		setResizable(false);
		setBounds(100, 100, 1020, 311);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnNewMenu = new JMenu("Menu");
		menuBar.add(mnNewMenu);

		JMenuItem mntmNewMenuItem = new JMenuItem("Settings");
		mntmNewMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSettings();
			}
		});
		mnNewMenu.add(mntmNewMenuItem);

		JMenuItem mntmChangePwd = new JMenuItem("Change password");
		mntmChangePwd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!Context.isUploadServiceStarted()) {
					final JDialog dialog = new JDialog(DicomUploaderCloudApplication.this, "Change Password", true);
					dialog.getContentPane().add(new ChangePasswordPanel(DicomUploaderCloudApplication.this, dialog));
					dialog.setPreferredSize(new Dimension(560, 225));
					dialog.pack();
					dialog.setLocationRelativeTo(DicomUploaderCloudApplication.this);
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
				} else {
					JOptionPane.showMessageDialog(DicomUploaderCloudApplication.this, "Please stop the upload service.");
				}
			}
		});
		mnNewMenu.add(mntmChangePwd);

		JSeparator separator = new JSeparator();
		mnNewMenu.add(separator);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doExit();
			}
		});
		mnNewMenu.add(mntmExit);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblNewLabel_1 = new JLabel("<html>Pending <br/>Images</html>");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblNewLabel_1.setBounds(45, 75, 66, 67);
		contentPane.add(lblNewLabel_1);

		lblPending = new JLabel("0");
		lblPending.setFont(new Font("Tahoma", Font.PLAIN, 36));
		lblPending.setForeground(new Color(255, 0, 0));
		lblPending.setHorizontalAlignment(SwingConstants.CENTER);
		lblPending.setBounds(62, 9, 28, 76);
		contentPane.add(lblPending);

		// ****************************** Extracted images *************************************
		JLabel lblNewLabel_1_1_2 = new JLabel("<html>Images </br> To Extract JSON</html>");
		lblNewLabel_1_1_2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1_1_2.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblNewLabel_1_1_2.setBounds(173, 75, 105, 67);
		contentPane.add(lblNewLabel_1_1_2);

		lblJSONExtracted = new JLabel("0");
		lblJSONExtracted.setHorizontalAlignment(SwingConstants.CENTER);
		lblJSONExtracted.setForeground(new Color(255, 128, 0));
		lblJSONExtracted.setFont(new Font("Tahoma", Font.PLAIN, 36));
		lblJSONExtracted.setBounds(204, 9, 28, 76);
		contentPane.add(lblJSONExtracted);

		// ****************************** Modified images *************************************
		JLabel lblNewLabel_1_1 = new JLabel("<html>Images </br> To Anonymize</html>");
		lblNewLabel_1_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1_1.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblNewLabel_1_1.setBounds(321, 75, 105, 67);
		contentPane.add(lblNewLabel_1_1);

		lblModified = new JLabel("0");
		lblModified.setHorizontalAlignment(SwingConstants.CENTER);
		lblModified.setForeground(new Color(128, 0, 64));
		lblModified.setFont(new Font("Tahoma", Font.PLAIN, 36));
		lblModified.setBounds(346, 9, 46, 76);
		contentPane.add(lblModified);

		// ****************************** Compressed images *************************************
		JLabel lblNewLabel_1_1_1 = new JLabel("<html>Images </br> To Compress</html>");
		lblNewLabel_1_1_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1_1_1.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblNewLabel_1_1_1.setBounds(478, 75, 105, 67);
		contentPane.add(lblNewLabel_1_1_1);

		lblCompressed = new JLabel("0");
		lblCompressed.setHorizontalAlignment(SwingConstants.CENTER);
		lblCompressed.setForeground(new Color(234, 234, 0));
		lblCompressed.setFont(new Font("Tahoma", Font.PLAIN, 36));
		lblCompressed.setBounds(499, 9, 46, 76);
		contentPane.add(lblCompressed);

		// ****************************** Upload pending images *************************************
		JLabel lblNewLabel_1_1_1_2_4 = new JLabel("<html>Upload <br/>Pending</html>");
		lblNewLabel_1_1_1_2_4.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1_1_1_2_4.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblNewLabel_1_1_1_2_4.setBounds(620, 75, 105, 67);
		contentPane.add(lblNewLabel_1_1_1_2_4);

		lblUploadPending = new JLabel("0");
		lblUploadPending.setHorizontalAlignment(SwingConstants.CENTER);
		lblUploadPending.setForeground(new Color(0, 128, 0));
		lblUploadPending.setFont(new Font("Tahoma", Font.PLAIN, 36));
		lblUploadPending.setBounds(652, 9, 46, 76);
		contentPane.add(lblUploadPending);

		// ****************************** Good images *************************************
		JLabel lblNewLabel_1_1_1_2 = new JLabel("Good");
		lblNewLabel_1_1_1_2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1_1_1_2.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblNewLabel_1_1_1_2.setBounds(744, 83, 57, 25);
		contentPane.add(lblNewLabel_1_1_1_2);

		lblGood = new JLabel("0");
		lblGood.setHorizontalAlignment(SwingConstants.CENTER);
		lblGood.setForeground(new Color(0, 128, 0));
		lblGood.setFont(new Font("Tahoma", Font.PLAIN, 26));
		lblGood.setBounds(753, 37, 34, 44);
		contentPane.add(lblGood);

		// ****************************** Bad images *************************************
		JLabel lblNewLabel_1_1_1_2_1 = new JLabel("Bad");
		lblNewLabel_1_1_1_2_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1_1_1_2_1.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblNewLabel_1_1_1_2_1.setBounds(811, 83, 57, 25);
		contentPane.add(lblNewLabel_1_1_1_2_1);

		lblBad = new JLabel("0");
		lblBad.setHorizontalAlignment(SwingConstants.CENTER);
		lblBad.setForeground(Color.RED);
		lblBad.setFont(new Font("Tahoma", Font.PLAIN, 26));
		lblBad.setBounds(832, 37, 34, 44);
		contentPane.add(lblBad);

		// ****************************** Processed images *************************************
		JLabel lblNewLabel_1_1_1_1 = new JLabel("<html>Processed <br/>Images</html>");
		lblNewLabel_1_1_1_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1_1_1_1.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblNewLabel_1_1_1_1.setBounds(880, 75, 105, 67);
		contentPane.add(lblNewLabel_1_1_1_1);

		lblProcessed = new JLabel("0");
		lblProcessed.setHorizontalAlignment(SwingConstants.CENTER);
		lblProcessed.setForeground(new Color(0, 128, 0));
		lblProcessed.setFont(new Font("Tahoma", Font.PLAIN, 36));
		lblProcessed.setBounds(900, 9, 46, 76);
		contentPane.add(lblProcessed);

		// ****************************** Execution error *************************************
		JLabel lblNewLabel_1_1_1_5 = new JLabel("<html>Execution <br/>Error</html>");
		lblNewLabel_1_1_1_5.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1_1_1_5.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblNewLabel_1_1_1_5.setBounds(811, 178, 105, 67);
		//contentPane.add(lblNewLabel_1_1_1_5);

		lblProcessedError = new JLabel("0");
		lblProcessedError.setHorizontalAlignment(SwingConstants.CENTER);
		lblProcessedError.setForeground(new Color(0, 128, 65));
		lblProcessedError.setFont(new Font("Tahoma", Font.PLAIN, 26));
		lblProcessedError.setBounds(832, 140, 34, 44);
		//contentPane.add(lblProcessedError);



		JLabel lblPacsEcho_1 = new JLabel("Pacs server: ");
		lblPacsEcho_1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPacsEcho_1.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblPacsEcho_1.setBounds(700, 178, 105, 15);
		contentPane.add(lblPacsEcho_1);

		lblPacsEchoStatus = new JLabel("Not connected");
		lblPacsEchoStatus.setHorizontalAlignment(SwingConstants.LEFT);
		lblPacsEchoStatus.setForeground(new Color(187, 26, 26));
		lblPacsEchoStatus.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblPacsEchoStatus.setBounds(811, 178, 105, 15);
		lblPacsEchoStatus.setSize(lblPacsEchoStatus.getPreferredSize());
		contentPane.add(lblPacsEchoStatus);

		//remote server
		JLabel lblRemoteEcho_1 = new JLabel("Remote server: ");
		lblRemoteEcho_1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblRemoteEcho_1.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblRemoteEcho_1.setBounds(700, 200, 105, 15);
		contentPane.add(lblRemoteEcho_1);

		lblRemoteEchoStatus = new JLabel("Not connected");
		lblRemoteEchoStatus.setHorizontalAlignment(SwingConstants.LEFT);
		lblRemoteEchoStatus.setForeground(new Color(187, 26, 26));
		lblRemoteEchoStatus.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblRemoteEchoStatus.setBounds(811, 200, 105, 15);
		lblRemoteEchoStatus.setSize(lblRemoteEchoStatus.getPreferredSize());
		contentPane.add(lblRemoteEchoStatus);

		//lblRemoteEchoStatus

		btnStartServer = new JButton("Start SCP");
		btnStartServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JButton btn = (JButton) e.getSource();
				if(Context.canStartSCPService() && btn.getText().equalsIgnoreCase("Start SCP")) {
					new Thread(() -> jobsInitializer.startStoreSCP()).start();
					btn.setEnabled(false);
				} else if(btn.getText().equalsIgnoreCase("Stop SCP")) {
					//jobsInitializer.stopStoreSCP();
					SwingUtilities.invokeLater(() -> jobsInitializer.stopStoreSCP());
					btn.setEnabled(false);
				} else {
					JOptionPane.showMessageDialog(DicomUploaderCloudApplication.this, "Unable to start service. " +
							"Please ensure the command/process folders are properly configured.");
				}
			}
		});
		btnStartServer.setBounds(45, 186, 105, 35);
		contentPane.add(btnStartServer);

		btnInitProcess = new JButton("Start Service");
		btnInitProcess.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JButton btn = (JButton)e.getSource();
				btn.setEnabled(false);
				if(btn.getText().equals("Stop Service")) {
					new Thread(() -> {
						jobsInitializer.stopUploadService();
						jobsInitializer.stopBackupCleanService();
					}).start();
				} else {
					if(Context.canStartUploadService()) {
						new Thread(() -> {
							commandService.resetAll();
							jobsInitializer.startUploadService();
							jobsInitializer.startBackupCleanService();
						}).start();
					} else {
						JOptionPane.showMessageDialog(DicomUploaderCloudApplication.this, "Unable to start service. " +
								"Please ensure the command/process folders are properly configured.");
						btn.setEnabled(true);
					}
				}
			}
		});
		btnInitProcess.setBounds(160, 186, 170, 35);
		contentPane.add(btnInitProcess);

		if(SystemUtils.IS_OS_WINDOWS && SystemTray.isSupported()){
			popup = new PopupMenu();
			showMenuItem = new MenuItem("Show");
			showMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MenuItem aMenuItem = (MenuItem)e.getSource();
					if(aMenuItem.getLabel().equals("Show")) {
						restoreMainFrame(aMenuItem);
					} else {
						hideMainFrame(aMenuItem);
					}
				}
			});
			popup.add(showMenuItem);

			MenuItem settingsMenuItem = new MenuItem("Settings");
			settingsMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					doSettings();
				}
			});
			popup.add(settingsMenuItem);

			MenuItem exitMenuItem = new MenuItem("Exit");
			exitMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					doExit();
				}
			});
			popup.add(exitMenuItem);

			tray = SystemTray.getSystemTray();
			trayIcon = new TrayIcon(iconImage, "escribe", popup);
			trayIcon.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
						if(!isVisible()) {
							restoreMainFrame(showMenuItem);
						} else {
							hideMainFrame(showMenuItem);
						}
					}
				}
			});
			trayIcon.setImageAutoSize(true);
		}

		addWindowStateListener(new WindowStateListener() {
			@Override
			public void windowStateChanged(WindowEvent e) {
				if(e.getNewState() == ICONIFIED) {
					hideMainFrame(showMenuItem);
				}
				if(e.getNewState() == MAXIMIZED_BOTH) {
					setVisible(true);
				}
				if(e.getNewState() == NORMAL) {
					setVisible(true);
				}
			}
		});
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				hideMainFrame(showMenuItem);
			}
		});
		new DicomUploadStatisticsMonitor(AnalyticsUpdater.getInstance());
		pack();
	}

	private void doSettings() {
		if(!Context.isUploadServiceStarted()) {
			final JDialog dialog = new JDialog(DicomUploaderCloudApplication.this, "Login", true);
			LoginPanel loginPanel = new LoginPanel(dialog);
			loginPanel.addLoginValidationListener(loginValidationListener);
			dialog.getContentPane().add(loginPanel);
			dialog.setPreferredSize(new Dimension(560, 165));
			dialog.pack();
			dialog.setLocationRelativeTo(DicomUploaderCloudApplication.this);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);

		} else {
			JOptionPane.showMessageDialog(DicomUploaderCloudApplication.this, "Please stop the upload service.");
		}
	}

	private void doExit() {
		int response = JOptionPane.showConfirmDialog(DicomUploaderCloudApplication.this, "Are you sure, do you want to exit?", "Exit", JOptionPane.YES_NO_OPTION);
		if(response == JOptionPane.YES_OPTION) {
			//jobsInitializer.stopUploadService();
			System.exit(0);
		}
	}

	private void hideMainFrame(MenuItem menuItem) {
		if(!SystemUtils.IS_OS_WINDOWS) {
			setExtendedState(JFrame.ICONIFIED);
		} else {
			setVisible(false);
		}
		if(menuItem != null) {
			menuItem.setLabel("Show");
		}
	}

	private void addToTray() {
		try {
			if (tray != null) {
				tray.add(trayIcon);
				setVisible(false);
				return;
			}
			restoreMainFrame(null);
		} catch (AWTException ex) {
			ex.printStackTrace();
		}
	}

	private void restoreMainFrame(MenuItem menuItem) {
		setVisible(true);
		setExtendedState(JFrame.NORMAL);
		if(menuItem != null)
			menuItem.setLabel("Hide");
	}

	protected class DicomUploadStatisticsMonitor extends ProcessMonitor {

		protected DicomUploadStatisticsMonitor(AnalyticsUpdater statisticsUpdater) {
			this.statisticsUpdater = statisticsUpdater;
			this.statisticsUpdater.attach(this);
		}

		@Override
		public void doUpdate() {
			lblPending.setText(Context.getPendingImage().toString());
			lblPending.setSize(lblPending.getPreferredSize());

			lblJSONExtracted.setText(Context.getJsonExtractImage().toString());
			lblJSONExtracted.setSize(lblJSONExtracted.getPreferredSize());

			lblModified.setText(Context.getModifiedImage().toString());
			lblModified.setSize(lblModified.getPreferredSize());

			lblCompressed.setText(Context.getCompressedImage().toString());
			lblCompressed.setSize(lblCompressed.getPreferredSize());

			lblProcessed.setText(Context.getProcessedImage().toString());
			lblProcessed.setSize(lblProcessed.getPreferredSize());

			lblUploadPending.setText(Context.getUploadPendingCount().toString());
			lblUploadPending.setSize(lblUploadPending.getPreferredSize());

			//lblProcessedError.setText(Context.getProcessedErrorImage().toString());
			//lblProcessedError.setSize(lblProcessedError.getPreferredSize());

			lblGood.setText(Context.getGoodImage().toString());
			lblGood.setSize(lblGood.getPreferredSize());

			lblBad.setText(Context.getBadImage().toString());
			lblBad.setSize(lblBad.getPreferredSize());

			if(Context.isUploadServiceStarted()) {
				if(!btnInitProcess.getText().equalsIgnoreCase("Stop Service")) {
					btnInitProcess.setText("Stop Service");
					btnInitProcess.setEnabled(true);
				}
			} else {
				if(!btnInitProcess.getText().equalsIgnoreCase("Start Service")) {
					btnInitProcess.setText("Start Service");
					btnInitProcess.setEnabled(true);
				}
			}

			if(Context.getSCPEchoStatus()) {
				if(!btnStartServer.getText().equalsIgnoreCase("Stop SCP")) {
					btnStartServer.setText("Stop SCP");
					btnStartServer.setEnabled(true);
				}
				lblPacsEchoStatus.setText("Connected");
				lblPacsEchoStatus.setForeground(new Color(22, 157, 22));
			} else {
				if(!btnStartServer.getText().equalsIgnoreCase("Start SCP")) {
					btnStartServer.setText("Start SCP");
					btnStartServer.setEnabled(true);
				}
				lblPacsEchoStatus.setText("Not connected");
				lblPacsEchoStatus.setForeground(new Color(187, 26, 26));
				if(!btnStartServer.isEnabled() && Context.hasSCPEchoResponse()) {
					btnStartServer.setEnabled(true);
				}
			}

			if(Context.getSCUEchoStatus()) {
				lblRemoteEchoStatus.setText("Connected");
				lblRemoteEchoStatus.setForeground(new Color(22, 157, 22));
			} else {
				lblRemoteEchoStatus.setText("Not connected");
				lblRemoteEchoStatus.setForeground(new Color(187, 26, 26));
			}

			/*if(!btnStartServer.isEnabled() && Context.hasSCPEchoResponse()) {
				btnStartServer.setEnabled(true);
			}*/
			/*if(!btnInitProcess.isEnabled()) {
				btnInitProcess.setEnabled(true);
			}*/
		}
	}

	LoginValidationListener loginValidationListener = () -> {
		final JDialog dialog = new JDialog(DicomUploaderCloudApplication.this, "Configure", true);
		dialog.getContentPane().add(new ConfigurationPanel(dialog, commandService));
		dialog.setPreferredSize(new Dimension(795, 567));
		dialog.pack();
		dialog.setLocationRelativeTo(DicomUploaderCloudApplication.this);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	};

	public static void main(String[] args) {
		final SplashScreen splashScreen = new SplashScreen();
		try {
			ConfigurableApplicationContext ctx = new SpringApplicationBuilder(DicomUploaderCloudApplication.class).headless(false).run(args);
			EventQueue.invokeLater(() -> {
				DicomUploaderCloudApplication ex = ctx.getBean(DicomUploaderCloudApplication.class);
				ex.addToTray();
			});
		} catch (ApplicationContextException e) {
                    System.out.println("com.infospica.dicom.DicomUploaderCloudApplication.main()"+e);
			Throwable throwable = DicomUploaderCloudApplication.findCausedExceptionBy(e, PortInUseException.class);
			if(throwable != null) {
				splashScreen.displayErrorMessage("Unable to start the application." + throwable.getMessage());
			}
		} finally {
			splashScreen.setVisible(false);
			splashScreen.dispose();
		}
	}

	public static Throwable findCausedExceptionBy(Throwable caught, Class<? extends Throwable> isOfOrCausedBy) {
		if (caught == null) return null;
		else if (isOfOrCausedBy.isAssignableFrom(caught.getClass())) return caught;
		else return findCausedExceptionBy(caught.getCause(), isOfOrCausedBy);
	}
}
