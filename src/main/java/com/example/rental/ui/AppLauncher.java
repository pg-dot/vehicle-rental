package com.example.rental.ui;

import javax.swing.*;
import java.awt.*;

public class AppLauncher extends JFrame {

    public AppLauncher() {
        super("Vehicle Rental System - Launcher");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setResizable(false);

        setupUI();
    }

    private void setupUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(new Color(245, 245, 245));

        // Title
        JLabel titleLabel = new JLabel("Vehicle Rental System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(51, 51, 51));

        JLabel subtitleLabel = new JLabel("Select Application to Launch", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(102, 102, 102));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 10, 5));
        titlePanel.setBackground(new Color(245, 245, 245));
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);

        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new GridLayout(3, 1, 15, 15));
        buttonsPanel.setBackground(new Color(245, 245, 245));

        // Customer Portal Button
        JButton customerButton = createStyledButton(
                "🚗 Customer Portal",
                "For customers to browse and rent vehicles",
                new Color(52, 152, 219)
        );
        customerButton.addActionListener(e -> launchCustomerApp());

        // Admin Panel Button
        JButton adminButton = createStyledButton(
                "⚙️ Admin Panel",
                "For administrators to manage the system",
                new Color(231, 76, 60)
        );
        adminButton.addActionListener(e -> launchAdminApp());

        // Main Dashboard Button
        JButton mainButton = createStyledButton(
                "📊 Main Dashboard",
                "Original combined management interface",
                new Color(46, 204, 113)
        );
        mainButton.addActionListener(e -> launchMainApp());

        buttonsPanel.add(customerButton);
        buttonsPanel.add(adminButton);
        buttonsPanel.add(mainButton);

        // Footer
        JLabel footerLabel = new JLabel("Choose the interface that suits your needs", SwingConstants.CENTER);
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        footerLabel.setForeground(new Color(150, 150, 150));

        // Add all to main panel
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(buttonsPanel, BorderLayout.CENTER);
        mainPanel.add(footerLabel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JButton createStyledButton(String title, String description, Color color) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout(10, 5));
        button.setPreferredSize(new Dimension(400, 80));
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(color);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descLabel.setForeground(new Color(102, 102, 102));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setBackground(Color.WHITE);
        textPanel.add(titleLabel);
        textPanel.add(descLabel);

        button.add(textPanel, BorderLayout.CENTER);

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(250, 250, 250));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
            }
        });

        return button;
    }

    private void launchCustomerApp() {
        SwingUtilities.invokeLater(() -> {
            CustomerApp app = new CustomerApp();
            app.setVisible(true);
        });
        showLaunchMessage("Customer Portal launched!");
    }

    private void launchAdminApp() {
        SwingUtilities.invokeLater(() -> {
            AdminApp app = new AdminApp();
            app.setVisible(true);
        });
        showLaunchMessage("Admin Panel launched!");
    }

    private void launchMainApp() {
        SwingUtilities.invokeLater(() -> {
            MainApp app = new MainApp();
            app.setVisible(true);
        });
        showLaunchMessage("Main Dashboard launched!");
    }

    private void showLaunchMessage(String message) {
        JOptionPane.showMessageDialog(this,
                message + "\n\nYou can launch multiple applications.\nThis launcher will remain open.",
                "Application Launched",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            AppLauncher launcher = new AppLauncher();
            launcher.setVisible(true);
        });
    }
}