package com.example.rental.dao;

import com.example.rental.db.DB;
import com.example.rental.model.Rental;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RentalDAO {
    public void insert(Rental r) throws SQLException {
        String sql = "INSERT INTO rentals(customer_id, vehicle_id, start_date, due_date, base_cost, late_fee, total_cost, status) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, r.getCustomerId());
            ps.setInt(2, r.getVehicleId());
            ps.setDate(3, Date.valueOf(r.getStartDate()));
            ps.setDate(4, Date.valueOf(r.getDueDate()));
            ps.setBigDecimal(5, java.math.BigDecimal.valueOf(r.getBaseCost()));
            ps.setBigDecimal(6, java.math.BigDecimal.valueOf(r.getLateFee()));
            ps.setBigDecimal(7, java.math.BigDecimal.valueOf(r.getTotalCost()));
            ps.setString(8, r.getStatus());
            ps.executeUpdate();
        }
    }

    public void markReturned(int rentalId, LocalDate returnDate, double lateFee, double totalCost) throws SQLException {
        String sql = "UPDATE rentals SET return_date=?, late_fee=?, total_cost=?, status='RETURNED' WHERE id=?";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(returnDate));
            ps.setBigDecimal(2, java.math.BigDecimal.valueOf(lateFee));
            ps.setBigDecimal(3, java.math.BigDecimal.valueOf(totalCost));
            ps.setInt(4, rentalId);
            ps.executeUpdate();
        }
    }

    public List<Rental> listOpen() throws SQLException {
        String sql = "SELECT * FROM rentals WHERE status='OPEN' ORDER BY id";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<Rental> out = new ArrayList<>();
            while (rs.next()) out.add(map(rs));
            return out;
        }
    }

    public Rental findById(int id) throws SQLException {
        String sql = "SELECT * FROM rentals WHERE id=?";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private Rental map(ResultSet rs) throws SQLException {
        Rental r = new Rental();
        r.setId(rs.getInt("id"));
        r.setCustomerId(rs.getInt("customer_id"));
        r.setVehicleId(rs.getInt("vehicle_id"));
        r.setStartDate(rs.getDate("start_date").toLocalDate());
        r.setDueDate(rs.getDate("due_date").toLocalDate());
        Date rd = rs.getDate("return_date");
        if (rd != null) r.setReturnDate(rd.toLocalDate());
        r.setBaseCost(rs.getBigDecimal("base_cost").doubleValue());
        r.setLateFee(rs.getBigDecimal("late_fee").doubleValue());
        r.setTotalCost(rs.getBigDecimal("total_cost").doubleValue());
        r.setStatus(rs.getString("status"));
        return r;
    }
}

