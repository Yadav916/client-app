package com.infospica.dicom.gui;

import com.infospica.dicom.DicomUploaderCloudApplication;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

public class SplashScreen extends JWindow {

    Image splashScreen;
    ImageIcon imageIcon;
    BufferedImage bufferedImage;
    public SplashScreen() {
        splashScreen = Toolkit.getDefaultToolkit().getImage(DicomUploaderCloudApplication.class.getClassLoader().getResource("splash-screen.png"));
        imageIcon = new ImageIcon(splashScreen);
        setSize(imageIcon.getIconWidth(), imageIcon.getIconHeight());
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width-getSize().width)/2;
        int y = (screenSize.height-getSize().height)/2;
        setLocation(x, y);
        setBackground(Color.BLACK);
        setVisible(true);
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    public void paint(Graphics g) {
        Graphics2D graphics2D = (Graphics2D)g;
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        bufferedImage = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
        drawFirst(bufferedImage.getGraphics());
        graphics2D.drawImage(bufferedImage, 0, 0, null);
    }

    public void drawFirst(Graphics g) {
        super.paint(g);
        Graphics2D graphics2D = (Graphics2D)g;
        graphics2D.drawImage(splashScreen, 0, 0, this);
        graphics2D.setColor(Color.LIGHT_GRAY);
        graphics2D.setFont(new Font("TimesRoman", Font.TRUETYPE_FONT, 10));
        graphics2D.drawString("Version: 1.1.2", imageIcon.getIconWidth() - 70, imageIcon.getIconHeight() - 20);
    }

    public void displayErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}
