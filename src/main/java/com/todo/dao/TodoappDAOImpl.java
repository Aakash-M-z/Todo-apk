package com.todo.dao;

import com.todo.model.Todo;
import com.todo.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TodoappDAOImpl {

    // ===== SQL QUERIES =====
    private static final String SQL_SELECT_BASE = "SELECT id, tittle, description, completed, created_at, updated_at FROM todos";
    private static final String SQL_SELECT_ORDER_BY_CREATED_DESC = " ORDER BY created_at DESC";
    private static final String SQL_INSERT_TODO = "INSERT INTO todos (tittle, description, completed) VALUES (?, ?, ?)";
    private static final String SQL_UPDATE_TODO = "UPDATE todos SET tittle = ?, description = ?, completed = ?, updated_at = ? WHERE id = ?";
    private static final String SQL_DELETE_TODO_BY_ID = "DELETE FROM todos WHERE id = ?";
    private static final String SQL_WHERE_COMPLETED = " WHERE completed = 1";
    private static final String SQL_WHERE_INCOMPLETE = " WHERE completed = 0";

    // ===== PUBLIC METHODS =====
    public List<Todo> getAllTodos() throws SQLException {
        List<Todo> todos = new ArrayList<>();
        String sql = SQL_SELECT_BASE + SQL_SELECT_ORDER_BY_CREATED_DESC;

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
        String sql = SQL_INSERT_TODO;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, todo.getTitle());
            stmt.setString(2, todo.getDescription());
            stmt.setBoolean(3, todo.isCompleted());

            stmt.executeUpdate();
        }
    }

    public void updateTodo(Todo todo) throws SQLException {
        String sql = SQL_UPDATE_TODO;

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
        String sql = SQL_DELETE_TODO_BY_ID;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Todo> getTodosByFilter(String filter) throws SQLException {
        List<Todo> todos = new ArrayList<>();
        String base = SQL_SELECT_BASE;
        String where = "";
        if (filter != null) {
            if ("Completed".equalsIgnoreCase(filter)) {
                where = SQL_WHERE_COMPLETED;
            } else if ("Incomplete".equalsIgnoreCase(filter)) {
                where = SQL_WHERE_INCOMPLETE;
            }
        }
        String sql = base + where + SQL_SELECT_ORDER_BY_CREATED_DESC;

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
}
