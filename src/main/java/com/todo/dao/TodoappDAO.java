package com.todo.dao;

import com.todo.model.Todo;
import java.sql.SQLException;
import java.util.List;

public interface TodoappDAO {
    List<Todo> getAllTodos() throws SQLException;

    void addTodo(Todo todo) throws SQLException;

    void updateTodo(Todo todo) throws SQLException;

    void deleteTodo(String id) throws SQLException;
}
