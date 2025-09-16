package com.todo;

import com.todo.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            System.out.println("Connected to the database");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
