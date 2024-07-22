package org.example;

import javax.swing.*;
import java.awt.*;

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
        setLocation(520, 50);
        setTitle("Ферма дачи");
        setContentPane(rootPanel);
        setVisible(true);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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
