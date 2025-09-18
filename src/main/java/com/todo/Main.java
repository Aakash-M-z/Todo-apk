package com.todo;

import com.todo.gui.TodoappGUI;
import com.todo.util.DatabaseConnection;

import javax.swing.SwingUtilities;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        // Test database connection
        try (Connection connection = DatabaseConnection.getConnection()) {
            System.out.println("Connected to the database");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Please ensure MySQL is running and the database 'todo' exists");
            return;
        }

        // Launch GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                new TodoappGUI().setVisible(true);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
    }
}
