package org.example;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.Objects;

public class GUI extends JFrame {

    public static final int WIDTH = 550;
    public static final int HEIGHT = 450;

    public JPanel rootPanel;
    public JButton onButton;
    public JTextPane eventsArea;
    public JButton siteButton;
    public JTextArea updateArea;
    public JProgressBar progressBar1;
    public JCheckBox checkBox1;
    public JButton loginButton;
    public JCheckBox checkBox2;

    public GUI() {
        setSize(WIDTH, HEIGHT);
        setIconImage(Objects.requireNonNull(createIcon("/bell-icon.png")).getImage());
        setLocation(520, 50);
        setTitle("Ферма дачи");
        setContentPane(rootPanel);
        setVisible(true);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private static ImageIcon createIcon(String path) {
        URL imgURL = Main.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("File not found " + path);
            return null;
        }
    }

    public JButton getOnButton() {
        return onButton;
    }

    public JButton getSiteButton() {
        return siteButton;
    }

    public JTextPane getEventsArea() {
        return eventsArea;
    }

    public JTextArea getUpdateArea() {
        return updateArea;
    }

    public JProgressBar getProgressBar1() {
        return progressBar1;
    }

    public JCheckBox getCheckBox1() {
        return checkBox1;
    }

    public JButton getLoginButton() {
        return loginButton;
    }

    public JCheckBox getCheckBox2() {
        return checkBox2;
    }
}
