package com.example.rental.ui;

import com.example.rental.dao.CustomerDAO;
import com.example.rental.dao.RentalDAO;
import com.example.rental.dao.VehicleDAO;
import com.example.rental.model.Customer;
import com.example.rental.model.Rental;
import com.example.rental.model.Vehicle;
import com.example.rental.service.RentalService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class CustomerApp extends JFrame {
    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final RentalDAO rentalDAO = new RentalDAO();
    private final RentalService rentalService = new RentalService();

    private Customer currentCustomer;
    private final DefaultTableModel availableVehiclesModel = new DefaultTableModel(
            new Object[]{"ID", "Plate No", "Brand", "Model", "Rate/Day"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final DefaultTableModel myRentalsModel = new DefaultTableModel(
            new Object[]{"Rental ID", "Vehicle", "Start Date", "Due Date", "Days", "Total Cost", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public CustomerApp() {
        super("Vehicle Rental - Customer Portal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        // Login first
        if (!loginCustomer()) {
            System.exit(0);
        }

        setupUI();
        refreshData();
    }

    private boolean loginCustomer() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField phoneField = new JTextField();
        JTextField nameField = new JTextField();

        panel.add(new JLabel("Phone Number:"));
        panel.add(phoneField);
        panel.add(new JLabel("Name:"));
        panel.add(nameField);

        int result = JOptionPane.showConfirmDialog(null, panel,
                "Customer Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String phone = phoneField.getText().trim();
                String name = nameField.getText().trim();

                if (phone.isEmpty() || name.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
                    return loginCustomer();
                }

                CustomerDAO customerDAO = new CustomerDAO();
                List<Customer> customers = customerDAO.listAll();

                // Find customer by phone
                for (Customer c : customers) {
                    if (c.getPhone().equals(phone)) {
                        currentCustomer = c;
                        return true;
                    }
                }

                // Customer not found, create new
                int create = JOptionPane.showConfirmDialog(null,
                        "Customer not found. Create new account?",
                        "New Customer", JOptionPane.YES_NO_OPTION);

                if (create == JOptionPane.YES_OPTION) {
                    JTextField emailField = new JTextField();
                    JPanel emailPanel = new JPanel(new GridLayout(1, 2, 10, 10));
                    emailPanel.add(new JLabel("Email:"));
                    emailPanel.add(emailField);

                    int emailResult = JOptionPane.showConfirmDialog(null, emailPanel,
                            "Enter Email", JOptionPane.OK_CANCEL_OPTION);

                    if (emailResult == JOptionPane.OK_OPTION) {
                        Customer newCustomer = new Customer();
                        newCustomer.setName(name);
                        newCustomer.setPhone(phone);
                        newCustomer.setEmail(emailField.getText().trim());
                        customerDAO.insert(newCustomer);

                        // Fetch the newly created customer
                        customers = customerDAO.listAll();
                        for (Customer c : customers) {
                            if (c.getPhone().equals(phone)) {
                                currentCustomer = c;
                                JOptionPane.showMessageDialog(null, "Account created successfully!");
                                return true;
                            }
                        }
                    }
                }
                return false;

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return false;
    }

    private void setupUI() {
        setTitle("Vehicle Rental - Welcome " + currentCustomer.getName());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Available Vehicles", createAvailableVehiclesPanel());
        tabbedPane.addTab("My Rentals", createMyRentalsPanel());

        setContentPane(tabbedPane);
    }

    private JPanel createAvailableVehiclesPanel() {
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

        // Rent form
        JPanel rentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        rentPanel.setBorder(BorderFactory.createTitledBorder("Rent a Vehicle"));

        JLabel vehicleLabel = new JLabel("Vehicle ID:");
        JTextField vehicleIdField = new JTextField(8);
        JLabel daysLabel = new JLabel("Number of Days:");
        JTextField daysField = new JTextField(8);
        JButton rentButton = new JButton("Rent Vehicle");
        JButton refreshButton = new JButton("Refresh List");

        rentPanel.add(vehicleLabel);
        rentPanel.add(vehicleIdField);
        rentPanel.add(daysLabel);
        rentPanel.add(daysField);
        rentPanel.add(rentButton);
        rentPanel.add(refreshButton);

        // Auto-fill vehicle ID when row is selected
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    vehicleIdField.setText(table.getValueAt(selectedRow, 0).toString());
                }
            }
        });

        rentButton.addActionListener(e -> {
            try {
                int vehicleId = Integer.parseInt(vehicleIdField.getText().trim());
                int days = Integer.parseInt(daysField.getText().trim());

                if (days <= 0) {
                    JOptionPane.showMessageDialog(this, "Days must be greater than 0",
                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Get vehicle details for confirmation
                Vehicle vehicle = vehicleDAO.findById(vehicleId);
                if (vehicle == null) {
                    JOptionPane.showMessageDialog(this, "Vehicle not found",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double totalCost = vehicle.getDailyRate() * days;
                LocalDate dueDate = LocalDate.now().plusDays(days);

                String message = String.format(
                        "Vehicle: %s %s (%s)\nDays: %d\nDaily Rate: ₹%.2f\nTotal Cost: ₹%.2f\nDue Date: %s\n\nConfirm rental?",
                        vehicle.getBrand(), vehicle.getModel(), vehicle.getPlateNo(),
                        days, vehicle.getDailyRate(), totalCost, dueDate
                );

                int confirm = JOptionPane.showConfirmDialog(this, message,
                        "Confirm Rental", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    rentalService.rentVehicle(currentCustomer.getId(), vehicleId, days);
                    JOptionPane.showMessageDialog(this,
                            "Vehicle rented successfully!\nPlease return by: " + dueDate,
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    vehicleIdField.setText("");
                    daysField.setText("");
                    refreshData();
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Unavailable", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        refreshButton.addActionListener(e -> refreshAvailableVehicles());

        panel.add(rentPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMyRentalsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel title = new JLabel("My Active Rentals", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);

        // Table
        JTable table = new JTable(myRentalsModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Return form
        JPanel returnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        returnPanel.setBorder(BorderFactory.createTitledBorder("Return a Vehicle"));

        JLabel rentalLabel = new JLabel("Rental ID:");
        JTextField rentalIdField = new JTextField(8);
        JLabel feeLabel = new JLabel("Late Fee/Day (₹):");
        JTextField lateFeeField = new JTextField(8);
        lateFeeField.setText("100");
        JButton returnButton = new JButton("Return Vehicle");
        JButton refreshButton = new JButton("Refresh List");

        returnPanel.add(rentalLabel);
        returnPanel.add(rentalIdField);
        returnPanel.add(feeLabel);
        returnPanel.add(lateFeeField);
        returnPanel.add(returnButton);
        returnPanel.add(refreshButton);

        // Auto-fill rental ID when row is selected
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    rentalIdField.setText(table.getValueAt(selectedRow, 0).toString());
                }
            }
        });

        returnButton.addActionListener(e -> {
            try {
                int rentalId = Integer.parseInt(rentalIdField.getText().trim());
                double lateFee = Double.parseDouble(lateFeeField.getText().trim());

                // Get rental details
                Rental rental = rentalDAO.findById(rentalId);
                if (rental == null) {
                    JOptionPane.showMessageDialog(this, "Rental not found",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (rental.getCustomerId() != currentCustomer.getId()) {
                    JOptionPane.showMessageDialog(this, "This rental does not belong to you",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Vehicle vehicle = vehicleDAO.findById(rental.getVehicleId());
                LocalDate today = LocalDate.now();
                long lateDays = Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(rental.getDueDate(), today));
                double lateFeeTotal = lateDays * lateFee;
                double totalCost = rental.getBaseCost() + lateFeeTotal;

                String message = String.format(
                        "Vehicle: %s %s\nRental Period: %s to %s\nBase Cost: ₹%.2f\nLate Days: %d\nLate Fee: ₹%.2f\nTotal Cost: ₹%.2f\n\nConfirm return?",
                        vehicle.getBrand(), vehicle.getModel(),
                        rental.getStartDate(), rental.getDueDate(),
                        rental.getBaseCost(), lateDays, lateFeeTotal, totalCost
                );

                int confirm = JOptionPane.showConfirmDialog(this, message,
                        "Confirm Return", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    rentalService.returnVehicle(rentalId, today, lateFee);
                    JOptionPane.showMessageDialog(this,
                            String.format("Vehicle returned successfully!\nTotal amount: ₹%.2f", totalCost),
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    rentalIdField.setText("");
                    refreshData();
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        refreshButton.addActionListener(e -> refreshMyRentals());

        panel.add(returnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshAvailableVehicles() {
        try {
            availableVehiclesModel.setRowCount(0);
            List<Vehicle> vehicles = vehicleDAO.listAvailable();
            for (Vehicle v : vehicles) {
                availableVehiclesModel.addRow(new Object[]{
                        v.getId(), v.getPlateNo(), v.getBrand(), v.getModel(),
                        String.format("₹%.2f", v.getDailyRate())
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading vehicles: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshMyRentals() {
        try {
            myRentalsModel.setRowCount(0);
            List<Rental> rentals = rentalDAO.listOpen();

            for (Rental r : rentals) {
                if (r.getCustomerId() == currentCustomer.getId()) {
                    Vehicle v = vehicleDAO.findById(r.getVehicleId());
                    String vehicleInfo = v != null ?
                            v.getPlateNo() + " (" + v.getBrand() + " " + v.getModel() + ")" :
                            "Vehicle #" + r.getVehicleId();

                    long days = java.time.temporal.ChronoUnit.DAYS.between(r.getStartDate(), r.getDueDate());

                    myRentalsModel.addRow(new Object[]{
                            r.getId(), vehicleInfo, r.getStartDate(), r.getDueDate(),
                            days, String.format("₹%.2f", r.getTotalCost()), r.getStatus()
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading rentals: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshData() {
        refreshAvailableVehicles();
        refreshMyRentals();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new CustomerApp().setVisible(true));
    }
}