
import javax.swing.JOptionPane;

public class AlreadyRunning {
    public static void main(String[] args) {
        showMessageDialog("Another instance of application is already running.");
        System.exit(0);
    }

    public static void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(null, message, "eCScribe PACS", JOptionPane.ERROR_MESSAGE);
    }
}