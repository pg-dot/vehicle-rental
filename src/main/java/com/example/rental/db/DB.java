package com.example.rental.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DB {
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        try (InputStream in = DB.class.getClassLoader().getResourceAsStream("db.properties")) {
            Properties props = new Properties();
            if (in == null) throw new RuntimeException("db.properties not found");
            props.load(in);
            URL = props.getProperty("jdbc.url");
            USER = props.getProperty("jdbc.user");
            PASSWORD = props.getProperty("jdbc.password");
            Class.forName("org.postgresql.Driver");
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

