package com.example.rental.dao;

import com.example.rental.db.DB;
import com.example.rental.model.Vehicle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {
    public List<Vehicle> listAll() throws SQLException {
        String sql = "SELECT id, plate_no, brand, model, daily_rate, status FROM vehicles ORDER BY id";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Vehicle> out = new ArrayList<>();
            while (rs.next()) out.add(map(rs));
            return out;
        }
    }

    public List<Vehicle> listAvailable() throws SQLException {
        String sql = "SELECT id, plate_no, brand, model, daily_rate, status FROM vehicles WHERE status='AVAILABLE' ORDER BY id";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Vehicle> out = new ArrayList<>();
            while (rs.next()) out.add(map(rs));
            return out;
        }
    }

    public Vehicle findById(int id) throws SQLException {
        String sql = "SELECT id, plate_no, brand, model, daily_rate, status FROM vehicles WHERE id=?";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void insert(Vehicle v) throws SQLException {
        String sql = "INSERT INTO vehicles(plate_no, brand, model, daily_rate, status) VALUES(?,?,?,?,?)";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, v.getPlateNo());
            ps.setString(2, v.getBrand());
            ps.setString(3, v.getModel());
            ps.setDouble(4, v.getDailyRate());
            ps.setString(5, v.getStatus());
            ps.executeUpdate();
        }
    }

    public void updateStatus(int vehicleId, String status) throws SQLException {
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE vehicles SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setInt(2, vehicleId);
            ps.executeUpdate();
        }
    }

    private Vehicle map(ResultSet rs) throws SQLException {
        return new Vehicle(
                rs.getInt("id"),
                rs.getString("plate_no"),
                rs.getString("brand"),
                rs.getString("model"),
                rs.getDouble("daily_rate"),
                rs.getString("status")
        );
    }
}
