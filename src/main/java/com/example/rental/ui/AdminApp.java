package com.example.rental.ui;

import com.example.rental.dao.CustomerDAO;
import com.example.rental.dao.RentalDAO;
import com.example.rental.dao.VehicleDAO;
import com.example.rental.model.Customer;
import com.example.rental.model.Rental;
import com.example.rental.model.Vehicle;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.example.rental.db.DB;

public class AdminApp extends JFrame {
    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final RentalDAO rentalDAO = new RentalDAO();

    private final DefaultTableModel availableVehiclesModel = new DefaultTableModel(
            new Object[]{"ID", "Plate No", "Brand", "Model", "Rate/Day", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final DefaultTableModel allVehiclesModel = new DefaultTableModel(
            new Object[]{"ID", "Plate No", "Brand", "Model", "Rate/Day", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final DefaultTableModel customersModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Phone", "Email"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final DefaultTableModel rentalsModel = new DefaultTableModel(
            new Object[]{"Rental ID", "Customer", "Vehicle", "Start", "Due", "Return", "Base Cost", "Late Fee", "Total", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public AdminApp() {
        super("Vehicle Rental System - Admin Panel");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        // Simple admin authentication
        if (!authenticateAdmin()) {
            System.exit(0);
        }

        setupUI();
        refreshAllData();
    }

    private boolean authenticateAdmin() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        JTextField usernameField = new JTextField("admin");
        JPasswordField passwordField = new JPasswordField("admin123");

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(null, panel,
                "Admin Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            // Simple hardcoded authentication (replace with proper authentication in production)
            if ("admin".equals(username) && "admin123".equals(password)) {
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Invalid credentials",
                        "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                return authenticateAdmin();
            }
        }
        return false;
    }

    private void setupUI() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manage Vehicles", createManageVehiclesPanel());
        tabbedPane.addTab("View All Vehicles", createAllVehiclesPanel());
        tabbedPane.addTab("Customer Database", createCustomerDatabasePanel());
        tabbedPane.addTab("All Rentals", createAllRentalsPanel());

        setContentPane(tabbedPane);
    }

    private JPanel createManageVehiclesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel title = new JLabel("Available Vehicles for Rent", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);

        // Table
        JTable table = new JTable(availableVehiclesModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Action Panel
        JPanel actionPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        // Add vehicle form
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        addPanel.setBorder(BorderFactory.createTitledBorder("Add New Vehicle"));

        JLabel plateLabel = new JLabel("Plate No:");
        JTextField plateField = new JTextField(10);
        JLabel brandLabel = new JLabel("Brand:");
        JTextField brandField = new JTextField(10);
        JLabel modelLabel = new JLabel("Model:");
        JTextField modelField = new JTextField(10);
        JLabel rateLabel = new JLabel("Daily Rate (₹):");
        JTextField rateField = new JTextField(8);
        JButton addButton = new JButton("Add Vehicle");

        addPanel.add(plateLabel);
        addPanel.add(plateField);
        addPanel.add(brandLabel);
        addPanel.add(brandField);
        addPanel.add(modelLabel);
        addPanel.add(modelField);
        addPanel.add(rateLabel);
        addPanel.add(rateField);
        addPanel.add(addButton);

        addButton.addActionListener(e -> {
            try {
                String plate = plateField.getText().trim();
                String brand = brandField.getText().trim();
                String model = modelField.getText().trim();
                String rateStr = rateField.getText().trim();

                if (plate.isEmpty() || brand.isEmpty() || model.isEmpty() || rateStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill all fields",
                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double rate = Double.parseDouble(rateStr);
                if (rate < 0) {
                    JOptionPane.showMessageDialog(this, "Rate must be positive",
                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Vehicle vehicle = new Vehicle();
                vehicle.setPlateNo(plate);
                vehicle.setBrand(brand);
                vehicle.setModel(model);
                vehicle.setDailyRate(rate);
                vehicle.setStatus("AVAILABLE");

                vehicleDAO.insert(vehicle);
                JOptionPane.showMessageDialog(this, "Vehicle added successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                plateField.setText("");
                brandField.setText("");
                modelField.setText("");
                rateField.setText("");

                refreshAllData();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid rate",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Remove vehicle form
        JPanel removePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        removePanel.setBorder(BorderFactory.createTitledBorder("Remove Vehicle"));

        JLabel removeLabel = new JLabel("Vehicle ID:");
        JTextField removeIdField = new JTextField(8);
        JButton removeButton = new JButton("Remove Vehicle");
        JButton refreshButton = new JButton("Refresh List");

        removePanel.add(removeLabel);
        removePanel.add(removeIdField);
        removePanel.add(removeButton);
        removePanel.add(refreshButton);

        // Auto-fill ID when row is selected
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    removeIdField.setText(table.getValueAt(selectedRow, 0).toString());
                }
            }
        });

        removeButton.addActionListener(e -> {
            try {
                int vehicleId = Integer.parseInt(removeIdField.getText().trim());

                Vehicle vehicle = vehicleDAO.findById(vehicleId);
                if (vehicle == null) {
                    JOptionPane.showMessageDialog(this, "Vehicle not found",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check if vehicle is currently rented
                if ("RENTED".equals(vehicle.getStatus())) {
                    JOptionPane.showMessageDialog(this,
                            "Cannot remove a vehicle that is currently rented",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String message = String.format(
                        "Are you sure you want to remove this vehicle?\n\nPlate: %s\nBrand: %s %s",
                        vehicle.getPlateNo(), vehicle.getBrand(), vehicle.getModel()
                );

                int confirm = JOptionPane.showConfirmDialog(this, message,
                        "Confirm Removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    deleteVehicle(vehicleId);
                    JOptionPane.showMessageDialog(this, "Vehicle removed successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    removeIdField.setText("");
                    refreshAllData();
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid vehicle ID",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        refreshButton.addActionListener(e -> refreshAvailableVehicles());

        actionPanel.add(addPanel);
        actionPanel.add(removePanel);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAllVehiclesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel title = new JLabel("All Vehicles in Database", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);

        // Table
        JTable table = new JTable(allVehiclesModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Status update panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        statusPanel.setBorder(BorderFactory.createTitledBorder("Update Vehicle Status"));

        JLabel idLabel = new JLabel("Vehicle ID:");
        JTextField idField = new JTextField(8);
        JLabel statusLabel = new JLabel("New Status:");
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"AVAILABLE", "RENTED", "MAINTENANCE"});
        JButton updateButton = new JButton("Update Status");
        JButton refreshButton = new JButton("Refresh List");

        statusPanel.add(idLabel);
        statusPanel.add(idField);
        statusPanel.add(statusLabel);
        statusPanel.add(statusCombo);
        statusPanel.add(updateButton);
        statusPanel.add(refreshButton);

        // Auto-fill ID when row is selected
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    idField.setText(table.getValueAt(selectedRow, 0).toString());
                    statusCombo.setSelectedItem(table.getValueAt(selectedRow, 5).toString());
                }
            }
        });

        updateButton.addActionListener(e -> {
            try {
                int vehicleId = Integer.parseInt(idField.getText().trim());
                String newStatus = (String) statusCombo.getSelectedItem();

                vehicleDAO.updateStatus(vehicleId, newStatus);
                JOptionPane.showMessageDialog(this, "Status updated successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                idField.setText("");
                refreshAllVehicles();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid vehicle ID",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        refreshButton.addActionListener(e -> refreshAllVehicles());

        panel.add(statusPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCustomerDatabasePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel title = new JLabel("Customer Database", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);

        // Table
        JTable table = new JTable(customersModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with stats and refresh
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));

        JLabel statsLabel = new JLabel("Total Customers: 0", SwingConstants.LEFT);
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statsLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh List");
        JButton viewDetailsButton = new JButton("View Customer Details");

        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(refreshButton);

        bottomPanel.add(statsLabel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        refreshButton.addActionListener(e -> {
            refreshCustomers();
            try {
                List<Customer> customers = customerDAO.listAll();
                statsLabel.setText("Total Customers: " + customers.size());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        viewDetailsButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a customer",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int customerId = (int) table.getValueAt(selectedRow, 0);
            showCustomerDetails(customerId);
        });

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAllRentalsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel title = new JLabel("All Rental Records", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);

        // Table
        JTable table = new JTable(rentalsModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(70);  // Rental ID
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // Customer
        table.getColumnModel().getColumn(2).setPreferredWidth(150); // Vehicle
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // Start
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Due
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Return
        table.getColumnModel().getColumn(6).setPreferredWidth(90);  // Base Cost
        table.getColumnModel().getColumn(7).setPreferredWidth(90);  // Late Fee
        table.getColumnModel().getColumn(8).setPreferredWidth(90);  // Total
        table.getColumnModel().getColumn(9).setPreferredWidth(100); // Status

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));

        JLabel statusFilterLabel = new JLabel("Status:");
        JComboBox<String> statusFilterCombo = new JComboBox<>(new String[]{"ALL", "OPEN", "RETURNED"});
        JButton applyFilterButton = new JButton("Apply Filter");
        JButton refreshButton = new JButton("Refresh All");

        filterPanel.add(statusFilterLabel);
        filterPanel.add(statusFilterCombo);
        filterPanel.add(applyFilterButton);
        filterPanel.add(refreshButton);

        applyFilterButton.addActionListener(e -> {
            String filter = (String) statusFilterCombo.getSelectedItem();
            refreshRentalsWithFilter(filter);
        });

        refreshButton.addActionListener(e -> refreshAllRentals());

        panel.add(filterPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void showCustomerDetails(int customerId) {
        try {
            Customer customer = null;
            for (Customer c : customerDAO.listAll()) {
                if (c.getId() == customerId) {
                    customer = c;
                    break;
                }
            }

            if (customer == null) {
                JOptionPane.showMessageDialog(this, "Customer not found",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Count rentals for this customer
            int totalRentals = 0;
            int openRentals = 0;

            try (Connection c = DB.getConnection();
                 PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) as cnt, status FROM rentals WHERE customer_id=? GROUP BY status")) {
                ps.setInt(1, customerId);
                var rs = ps.executeQuery();
                while (rs.next()) {
                    int cnt = rs.getInt("cnt");
                    String status = rs.getString("status");
                    totalRentals += cnt;
                    if ("OPEN".equals(status)) {
                        openRentals = cnt;
                    }
                }
            }

            String details = String.format(
                    "Customer Details\n\n" +
                            "ID: %d\n" +
                            "Name: %s\n" +
                            "Phone: %s\n" +
                            "Email: %s\n\n" +
                            "Rental Statistics:\n" +
                            "Total Rentals: %d\n" +
                            "Active Rentals: %d\n" +
                            "Completed Rentals: %d",
                    customer.getId(), customer.getName(), customer.getPhone(),
                    customer.getEmail(), totalRentals, openRentals, totalRentals - openRentals
            );

            JOptionPane.showMessageDialog(this, details,
                    "Customer Details", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading customer details: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteVehicle(int vehicleId) throws SQLException {
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM vehicles WHERE id=?")) {
            ps.setInt(1, vehicleId);
            ps.executeUpdate();
        }
    }

    private void refreshAvailableVehicles() {
        try {
            availableVehiclesModel.setRowCount(0);
            List<Vehicle> vehicles = vehicleDAO.listAvailable();
            for (Vehicle v : vehicles) {
                availableVehiclesModel.addRow(new Object[]{
                        v.getId(), v.getPlateNo(), v.getBrand(), v.getModel(),
                        String.format("₹%.2f", v.getDailyRate()), v.getStatus()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading vehicles: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshAllVehicles() {
        try {
            allVehiclesModel.setRowCount(0);
            List<Vehicle> vehicles = vehicleDAO.listAll();
            for (Vehicle v : vehicles) {
                allVehiclesModel.addRow(new Object[]{
                        v.getId(), v.getPlateNo(), v.getBrand(), v.getModel(),
                        String.format("₹%.2f", v.getDailyRate()), v.getStatus()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading vehicles: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshCustomers() {
        try {
            customersModel.setRowCount(0);
            List<Customer> customers = customerDAO.listAll();
            for (Customer c : customers) {
                customersModel.addRow(new Object[]{
                        c.getId(), c.getName(), c.getPhone(), c.getEmail()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading customers: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshAllRentals() {
        refreshRentalsWithFilter("ALL");
    }

    private void refreshRentalsWithFilter(String statusFilter) {
        try {
            rentalsModel.setRowCount(0);

            String sql = statusFilter.equals("ALL") ?
                    "SELECT * FROM rentals ORDER BY id DESC" :
                    "SELECT * FROM rentals WHERE status=? ORDER BY id DESC";

            try (Connection c = DB.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {

                if (!statusFilter.equals("ALL")) {
                    ps.setString(1, statusFilter);
                }

                var rs = ps.executeQuery();
                while (rs.next()) {
                    int customerId = rs.getInt("customer_id");
                    int vehicleId = rs.getInt("vehicle_id");

                    // Get customer name
                    String customerName = "Customer #" + customerId;
                    for (Customer customer : customerDAO.listAll()) {
                        if (customer.getId() == customerId) {
                            customerName = customer.getName();
                            break;
                        }
                    }

                    // Get vehicle info
                    String vehicleInfo = "Vehicle #" + vehicleId;
                    Vehicle v = vehicleDAO.findById(vehicleId);
                    if (v != null) {
                        vehicleInfo = v.getPlateNo() + " (" + v.getBrand() + " " + v.getModel() + ")";
                    }

                    String returnDate = rs.getDate("return_date") != null ?
                            rs.getDate("return_date").toString() : "-";

                    rentalsModel.addRow(new Object[]{
                            rs.getInt("id"),
                            customerName,
                            vehicleInfo,
                            rs.getDate("start_date"),
                            rs.getDate("due_date"),
                            returnDate,
                            String.format("₹%.2f", rs.getBigDecimal("base_cost")),
                            String.format("₹%.2f", rs.getBigDecimal("late_fee")),
                            String.format("₹%.2f", rs.getBigDecimal("total_cost")),
                            rs.getString("status")
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading rentals: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshAllData() {
        refreshAvailableVehicles();
        refreshAllVehicles();
        refreshCustomers();
        refreshAllRentals();

        try {
            List<Customer> customers = customerDAO.listAll();
            // Update stats if the panel is visible
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new AdminApp().setVisible(true));
    }
}