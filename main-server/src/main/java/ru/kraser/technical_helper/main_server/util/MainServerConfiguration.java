package ru.kraser.technical_helper.main_server.util;

import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class MainServerConfiguration {

    private static final String URL = "jdbc:postgresql://localhost:5432/technical_helper";
    private static final String USER = "sa";
    private static final String PASSWORD = "sapassword";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
