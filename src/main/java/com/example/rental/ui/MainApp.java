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

public class MainApp extends JFrame {
    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final RentalDAO rentalDAO = new RentalDAO();
    private final RentalService rentalService = new RentalService();

    private final DefaultTableModel vehiclesModel = new DefaultTableModel(new Object[]{"ID","Plate","Brand","Model","Rate","Status"}, 0);
    private final DefaultTableModel customersModel = new DefaultTableModel(new Object[]{"ID","Name","Phone","Email"}, 0);
    private final DefaultTableModel rentalsModel = new DefaultTableModel(new Object[]{"ID","Customer","Vehicle","Start","Due","Status","Total"}, 0);

    public MainApp() {
        super("Vehicle Rental System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Vehicles", vehiclesPanel());
        tabs.add("Customers", customersPanel());
        tabs.add("Rentals", rentalsPanel());
        setContentPane(tabs);
        refreshAll();
    }

    private JPanel vehiclesPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JTable table = new JTable(vehiclesModel);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField plate = new JTextField(8);
        JTextField brand = new JTextField(8);
        JTextField model = new JTextField(8);
        JTextField rate = new JTextField(6);
        JComboBox<String> status = new JComboBox<>(new String[]{"AVAILABLE","RENTED","MAINTENANCE"});
        JButton add = new JButton("Add Vehicle");
        form.add(new JLabel("Plate:")); form.add(plate);
        form.add(new JLabel("Brand:")); form.add(brand);
        form.add(new JLabel("Model:")); form.add(model);
        form.add(new JLabel("Rate/d:")); form.add(rate);
        form.add(new JLabel("Status:")); form.add(status);
        form.add(add);
        add.addActionListener(e -> {
            try {
                Vehicle v = new Vehicle();
                v.setPlateNo(plate.getText());
                v.setBrand(brand.getText());
                v.setModel(model.getText());
                v.setDailyRate(Double.parseDouble(rate.getText()));
                v.setStatus((String) status.getSelectedItem());
                new VehicleDAO().insert(v);
                refreshVehicles();
                plate.setText(""); brand.setText(""); model.setText(""); rate.setText("");
            } catch (Exception ex) { showErr(ex); }
        });
        p.add(form, BorderLayout.NORTH);
        return p;
    }

    private JPanel customersPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JTable table = new JTable(customersModel);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField name = new JTextField(12);
        JTextField phone = new JTextField(10);
        JTextField email = new JTextField(14);
        JButton add = new JButton("Add Customer");
        form.add(new JLabel("Name:")); form.add(name);
        form.add(new JLabel("Phone:")); form.add(phone);
        form.add(new JLabel("Email:")); form.add(email);
        form.add(add);
        add.addActionListener(e -> {
            try {
                Customer c = new Customer();
                c.setName(name.getText());
                c.setPhone(phone.getText());
                c.setEmail(email.getText());
                new CustomerDAO().insert(c);
                refreshCustomers();
                name.setText(""); phone.setText(""); email.setText("");
            } catch (Exception ex) { showErr(ex); }
        });
        p.add(form, BorderLayout.NORTH);
        return p;
    }

    private JPanel rentalsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JTable table = new JTable(rentalsModel);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField custId = new JTextField(4);
        JTextField vehId = new JTextField(4);
        JTextField days = new JTextField(3);
        JButton rent = new JButton("Rent");
        JTextField rentalId = new JTextField(4);
        JTextField lateFeePerDay = new JTextField(4);
        JButton ret = new JButton("Return");

        form.add(new JLabel("CustomerID:")); form.add(custId);
        form.add(new JLabel("VehicleID:")); form.add(vehId);
        form.add(new JLabel("Days:")); form.add(days);
        form.add(rent);
        form.add(new JLabel("RentalID:")); form.add(rentalId);
        form.add(new JLabel("LateFee/Day:")); form.add(lateFeePerDay);
        form.add(ret);

        rent.addActionListener(e -> {
            try {
                rentalService.rentVehicle(
                        Integer.parseInt(custId.getText()),
                        Integer.parseInt(vehId.getText()),
                        Integer.parseInt(days.getText())
                );
                refreshAll();
                custId.setText(""); vehId.setText(""); days.setText("");
            } catch (Exception ex) { showErr(ex); }
        });

        ret.addActionListener(e -> {
            try {
                int id = Integer.parseInt(rentalId.getText());
                double fee = Double.parseDouble(lateFeePerDay.getText());
                rentalService.returnVehicle(id, LocalDate.now(), fee);
                refreshAll();
                rentalId.setText(""); lateFeePerDay.setText("");
            } catch (Exception ex) { showErr(ex); }
        });

        p.add(form, BorderLayout.NORTH);
        return p;
    }

    private void refreshVehicles() {
        try {
            vehiclesModel.setRowCount(0);
            for (Vehicle v : vehicleDAO.listAll()) {
                vehiclesModel.addRow(new Object[]{v.getId(), v.getPlateNo(), v.getBrand(), v.getModel(), v.getDailyRate(), v.getStatus()});
            }
        } catch (SQLException e) { showErr(e); }
    }

    private void refreshCustomers() {
        try {
            customersModel.setRowCount(0);
            for (Customer c : customerDAO.listAll()) {
                customersModel.addRow(new Object[]{c.getId(), c.getName(), c.getPhone(), c.getEmail()});
            }
        } catch (SQLException e) { showErr(e); }
    }

    private void refreshRentals() {
        try {
            rentalsModel.setRowCount(0);
            for (Rental r : rentalDAO.listOpen()) {
                rentalsModel.addRow(new Object[]{r.getId(), r.getCustomerId(), r.getVehicleId(), r.getStartDate(), r.getDueDate(), r.getStatus(), r.getTotalCost()});
            }
        } catch (SQLException e) { showErr(e); }
    }

    private void refreshAll() {
        refreshVehicles();
        refreshCustomers();
        refreshRentals();
    }

    private void showErr(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }
}

