package com.infospica.dicom.gui;

import com.infospica.dicom.config.StartupConfigurationProperties;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.util.LoggerUtility;
import com.infospica.dicom.util.Security;
import org.apache.commons.lang3.SerializationUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Base64;

public class ChangePasswordPanel extends JPanel {

    private JDialog jdialog;
    private JFrame parent;
    private JPasswordField oldPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    public ChangePasswordPanel(JFrame parent, JDialog jdialog) {
        this.parent = parent;
        this.jdialog = jdialog;
        initComponents();
    }

    private void initComponents() {
        setLayout(null);

        JLabel lblOldPassword = new JLabel("Old Password");
        lblOldPassword.setBounds(23, 27, 107, 15);
        add(lblOldPassword);

        oldPasswordField = new JPasswordField();
        oldPasswordField.setBounds(188, 27, 331, 24);
        add(oldPasswordField);

        JLabel lblNewPassword = new JLabel("New Password");
        lblNewPassword.setBounds(23, 66, 107, 15);
        add(lblNewPassword);

        newPasswordField = new JPasswordField();
        newPasswordField.setBounds(188, 66, 331, 24);
        add(newPasswordField);

        JLabel lblConfirmPassword = new JLabel("Retype Password");
        lblConfirmPassword.setBounds(23, 105, 107, 15);
        add(lblConfirmPassword);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setBounds(188, 105, 331, 24);
        add(confirmPasswordField);

        JButton btnChange = new JButton("Update");
        btnChange.setBounds(25, 148, 117, 25);
        btnChange.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String password = new String(oldPasswordField.getPassword());
                if(password.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(ChangePasswordPanel.this.jdialog, "Please enter the old password.");
                    oldPasswordField.requestFocus();
                    return;
                }
                String newPassword = new String(newPasswordField.getPassword());
                if(newPassword.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(ChangePasswordPanel.this.jdialog, "Please enter the new password.");
                    newPasswordField.requestFocus();
                    return;
                }
                String confirmPassword = new String(confirmPasswordField.getPassword());
                if(confirmPassword.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(ChangePasswordPanel.this.jdialog, "Please retype the password.");
                    confirmPasswordField.requestFocus();
                    return;
                }
                if(isAuthenticated()) {
                    try {
                        updatePassword(confirmPassword);
                        JOptionPane.showMessageDialog(ChangePasswordPanel.this.jdialog, "Password updated successfully.");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(ChangePasswordPanel.this.jdialog, "Error when changing the password.(" + ex.getMessage() + ")");
                    }
                    ChangePasswordPanel.this.jdialog.dispose(); //close this
                } else {
                    JOptionPane.showMessageDialog(ChangePasswordPanel.this.jdialog, "Password does not match.");
                }
            }
        });
        add(btnChange);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setBounds(152, 148, 117, 25);
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ChangePasswordPanel.this.jdialog.dispose();
            }
        });
        add(btnCancel);
    }

    private boolean isAuthenticated()  {
        String salt = Context.getStartupConfigurationProperties().getSalt();
        String password = new String(oldPasswordField.getPassword());
        try {
            return Security.hashMatches(Context.getStartupConfigurationProperties().getPassword(), password + salt);
        } catch (Exception e) {
            LoggerUtility.log(ChangePasswordPanel.class, LoggerUtility.LogLevel.ERROR, e.getMessage());
        }
        return false;
    }

    private void updatePassword(String password) throws Exception {
        String salt = Security.getRandomToken();
        String changePassword = Security.encodeString(password + salt);
        byte[] input1 = SerializationUtils.serialize(Context.getStartupConfigurationProperties());
        String encodedInput1 = Base64.getEncoder().encodeToString(input1);

        Context.getStartupConfigurationProperties().setSalt(salt);
        Context.getStartupConfigurationProperties().setPassword(changePassword);
        try {
            Context.getStartupConfigurationProperties().writeConfiguration();
        } catch (Exception ioe) {
            ioe.printStackTrace();
            byte[] decodedOutput1 = Base64.getDecoder().decode(encodedInput1);
            Object sobj1 = SerializationUtils.deserialize(decodedOutput1);
            Context.setStartupConfigurationProperties((StartupConfigurationProperties) sobj1);
        }
    }
}
