package com.example.rental.service;

import com.example.rental.dao.RentalDAO;
import com.example.rental.dao.VehicleDAO;
import com.example.rental.model.Rental;
import com.example.rental.model.Vehicle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.example.rental.db.DB;

public class RentalService {
    private final RentalDAO rentalDAO = new RentalDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();

    public void rentVehicle(int customerId, int vehicleId, int days) throws SQLException {
        if (days <= 0) throw new IllegalArgumentException("days must be > 0");
        try (Connection c = DB.getConnection()) {
            c.setAutoCommit(false);
            try {
                Vehicle v = vehicleDAO.findById(vehicleId);
                if (v == null) throw new IllegalArgumentException("Vehicle not found");
                if (!"AVAILABLE".equals(v.getStatus())) throw new IllegalStateException("Vehicle not available");

                LocalDate start = LocalDate.now();
                LocalDate due = start.plusDays(days);
                double base = v.getDailyRate() * days;

                String sql = "INSERT INTO rentals(customer_id, vehicle_id, start_date, due_date, base_cost, late_fee, total_cost, status) VALUES(?,?,?,?,?,?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setInt(1, customerId);
                    ps.setInt(2, vehicleId);
                    ps.setDate(3, java.sql.Date.valueOf(start));
                    ps.setDate(4, java.sql.Date.valueOf(due));
                    ps.setBigDecimal(5, java.math.BigDecimal.valueOf(base));
                    ps.setBigDecimal(6, java.math.BigDecimal.valueOf(0));
                    ps.setBigDecimal(7, java.math.BigDecimal.valueOf(base));
                    ps.setString(8, "OPEN");
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = c.prepareStatement("UPDATE vehicles SET status='RENTED' WHERE id=?")) {
                    ps.setInt(1, vehicleId);
                    ps.executeUpdate();
                }

                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public void returnVehicle(int rentalId, LocalDate returnDate, double lateFeePerDay) throws SQLException {
        Rental r = rentalDAO.findById(rentalId);
        if (r == null) throw new IllegalArgumentException("Rental not found");
        if (!"OPEN".equals(r.getStatus())) throw new IllegalStateException("Rental not open");

        long lateDays = Math.max(0, ChronoUnit.DAYS.between(r.getDueDate(), returnDate));
        double lateFee = lateDays * lateFeePerDay;
        double total = r.getBaseCost() + lateFee;

        try (Connection c = DB.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement("UPDATE rentals SET return_date=?, late_fee=?, total_cost=?, status='RETURNED' WHERE id=?")) {
                    ps.setDate(1, java.sql.Date.valueOf(returnDate));
                    ps.setBigDecimal(2, java.math.BigDecimal.valueOf(lateFee));
                    ps.setBigDecimal(3, java.math.BigDecimal.valueOf(total));
                    ps.setInt(4, rentalId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement("UPDATE vehicles SET status='AVAILABLE' WHERE id=?")) {
                    ps.setInt(1, r.getVehicleId());
                    ps.executeUpdate();
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }
}
