package com.todo.dao;

import com.todo.model.Todo;
import com.todo.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TodoappDAOImpl {

    public List<Todo> getAllTodos() throws SQLException {
        List<Todo> todos = new ArrayList<>();
        String sql = "SELECT id, tittle, description, completed, created_at, updated_at FROM todos ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Todo todo = new Todo(
                        String.valueOf(rs.getInt("id")),
                        rs.getString("tittle"),
                        rs.getString("description"),
                        rs.getBoolean("completed"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime());
                todos.add(todo);
            }
        }
        return todos;
    }

    public void addTodo(Todo todo) throws SQLException {
        // id, created_at and updated_at are handled by DB (auto_increment/defaults)
        String sql = "INSERT INTO todos (tittle, description, completed) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, todo.getTitle());
            stmt.setString(2, todo.getDescription());
            stmt.setBoolean(3, todo.isCompleted());

            stmt.executeUpdate();
        }
    }

    public void updateTodo(Todo todo) throws SQLException {
        String sql = "UPDATE todos SET tittle = ?, description = ?, completed = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, todo.getTitle());
            stmt.setString(2, todo.getDescription());
            stmt.setBoolean(3, todo.isCompleted());
            stmt.setTimestamp(4, Timestamp.valueOf(todo.getUpdated_at()));
            stmt.setString(5, todo.getId());

            stmt.executeUpdate();
        }
    }

    public void deleteTodo(String id) throws SQLException {
        String sql = "DELETE FROM todos WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }
}
