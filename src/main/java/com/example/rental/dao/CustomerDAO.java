package com.example.rental.dao;

import com.example.rental.db.DB;
import com.example.rental.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    public List<Customer> listAll() throws SQLException {
        String sql = "SELECT id, name, phone, email FROM customers ORDER BY id";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Customer> out = new ArrayList<>();
            while (rs.next()) out.add(map(rs));
            return out;
        }
    }

    public void insert(Customer cu) throws SQLException {
        String sql = "INSERT INTO customers(name, phone, email) VALUES(?,?,?)";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cu.getName());
            ps.setString(2, cu.getPhone());
            ps.setString(3, cu.getEmail());
            ps.executeUpdate();
        }
    }

    private Customer map(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email")
        );
    }
}
