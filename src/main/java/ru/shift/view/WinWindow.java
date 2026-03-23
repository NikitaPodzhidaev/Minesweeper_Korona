package ru.shift.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class WinWindow extends JDialog {
    private ActionListener newGameListener;
    private ActionListener exitListener;

    private JLabel timeLabel;

    public WinWindow(JFrame owner) {
        super(owner, "Win", true);

        GridBagLayout layout = new GridBagLayout();
        Container contentPane = getContentPane();
        contentPane.setLayout(layout);

        contentPane.add(createWinLabel(layout));
        contentPane.add(createTimeLabel(layout));
        contentPane.add(createNewGameButton(layout));
        contentPane.add(createExitButton(layout));

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(300, 150));
        setResizable(false);
        pack();
        setLocationRelativeTo(owner);
    }

    public void setNewGameListener(ActionListener newGameListener) {
        this.newGameListener = newGameListener;
    }

    public void setExitListener(ActionListener exitListener) {
        this.exitListener = exitListener;
    }

    public void setTimeSeconds(int seconds) {
        if (timeLabel != null) {
            timeLabel.setText("Time: " + formatSeconds(seconds));
        }
    }

    private JLabel createWinLabel(GridBagLayout layout) {
        JLabel label = new JLabel("You win!", SwingConstants.CENTER);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        layout.setConstraints(label, gbc);
        return label;
    }

    private JLabel createTimeLabel(GridBagLayout layout) {
        timeLabel = new JLabel("Time: 00:00", SwingConstants.CENTER);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(8, 0, 0, 0);
        layout.setConstraints(timeLabel, gbc);
        return timeLabel;
    }

    private JButton createNewGameButton(GridBagLayout layout) {
        JButton newGameButton = new JButton("New game");
        newGameButton.setPreferredSize(new Dimension(100, 25));

        newGameButton.addActionListener(e -> {
            dispose();
            if (newGameListener != null) {
                newGameListener.actionPerformed(e);
            }
        });

        getRootPane().setDefaultButton(newGameButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(12, 0, 0, 0);
        layout.setConstraints(newGameButton, gbc);

        return newGameButton;
    }

    private JButton createExitButton(GridBagLayout layout) {
        JButton exitButton = new JButton("Exit");
        exitButton.setPreferredSize(new Dimension(100, 25));

        exitButton.addActionListener(e -> {
            dispose();
            if (exitListener != null) {
                exitListener.actionPerformed(e);
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(12, 5, 0, 0);
        layout.setConstraints(exitButton, gbc);

        return exitButton;
    }

    private static String formatSeconds(int s) {
        if (s < 0) s = 0;
        return String.format("%02d:%02d", s / 60, s % 60);
    }
}
