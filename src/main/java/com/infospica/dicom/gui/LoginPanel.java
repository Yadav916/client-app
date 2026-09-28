package com.infospica.dicom.gui;

import com.infospica.dicom.JobsInitializer;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.gui.event.LoginValidationListener;
import com.infospica.dicom.util.LoggerUtility;
import com.infospica.dicom.util.Security;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginPanel extends JPanel {

    private JPasswordField passwordField;
    private LoginValidationListener loginValidationListener;
    private final JDialog jdialog;

    public LoginPanel(JDialog jdialog) {
        this.jdialog = jdialog;
        initComponents();
    }

    private void initComponents() {
        setLayout(null);

        JLabel lblPassword = new JLabel("Password");
        lblPassword.setBounds(23, 27, 107, 15);
        add(lblPassword);

        passwordField = new JPasswordField();
        passwordField.setBounds(188, 27, 331, 24);
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doAuthenticate();
                }
            }
        });
        add(passwordField);

        JButton btnLogin = new JButton("Login");
        btnLogin.setBounds(25, 87, 117, 25);
        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doAuthenticate();
            }
        });
        add(btnLogin);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setBounds(152, 87, 117, 25);
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LoginPanel.this.jdialog.dispose();
            }
        });
        add(btnCancel);
    }
    private void doAuthenticate() {
        String password = new String(passwordField.getPassword());
        if(password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(LoginPanel.this.jdialog, "Please enter the password.");
            passwordField.requestFocus();
            return;
        }
        if(isAuthenticated()) {
            LoginPanel.this.jdialog.dispose(); //close this
            if(loginValidationListener != null) {
                loginValidationListener.onSuccess();
            }
        } else {
            JOptionPane.showMessageDialog(LoginPanel.this.jdialog, "Password does not match.");
        }
    }

    public void addLoginValidationListener(LoginValidationListener loginValidationListener) {
        this.loginValidationListener = loginValidationListener;
    }

    private boolean isAuthenticated()  {
        String salt = Context.getStartupConfigurationProperties().getSalt();
        String password = new String(passwordField.getPassword());
        try {
            return Security.hashMatches(Context.getStartupConfigurationProperties().getPassword(), password + salt);
        } catch (Exception e) {
            LoggerUtility.log(LoginPanel.class, LoggerUtility.LogLevel.ERROR, e.getMessage());
        }
        return false;
    }
}
