package com.todo.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.todo.model.Todo;
import com.todo.dao.TodoappDAOImpl;

public class TodoappGUI extends JFrame {
    // Database connection
    private TodoappDAOImpl todoappDAO;

    // Table components
    private JTable todoTable;
    private DefaultTableModel tableModel;

    // Input components
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JCheckBox completedCheckbox;

    // Buttons
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton refreshButton;

    // Filter
    private JComboBox<String> filterComboBox;

    // Data storage
    private List<Todo> allTodos = new ArrayList<>();

    public TodoappGUI() {
        // Initialize database connection
        this.todoappDAO = new TodoappDAOImpl();

        // Setup the GUI
        setupWindow();
        createComponents();
        setupLayout();
        setupEventListeners();

        // Load initial data
        loadAllTodos();
    }

    // Setup basic window properties
    private void setupWindow() {
        setTitle("Simple Todo App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    // Create all GUI components
    private void createComponents() {
        // Create table
        String[] columns = { "ID", "Title", "Description", "Completed", "Created At", "Updated At" };
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        todoTable = new JTable(tableModel);
        todoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Create input fields
        titleField = new JTextField(20);
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);0
        completedCheckbox = new JCheckBox("Completed");

        // Create buttons
        addButton = new JButton("Add Todo");
        updateButton = new JButton("Update Todo");
        deleteButton = new JButton("Delete Todo");
        refreshButton = new JButton("Refresh");

        // Create filter
        String[] filterOptions = { "All", "Completed", "Incomplete" };
        filterComboBox = new JComboBox<>(filterOptions);
    }

    // Setup the layout using simple BorderLayout
    private void setupLayout() {
        setLayout(new BorderLayout());

        // Top panel with form and buttons
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Center panel with table
        JScrollPane tableScrollPane = new JScrollPane(todoTable);
        add(tableScrollPane, BorderLayout.CENTER);
    }

    // Create the top panel with form and buttons
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form panel
        JPanel formPanel = createFormPanel();
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(formPanel);

        // Add some space
        topPanel.add(Box.createVerticalStrut(10));

        // Button panel
        JPanel buttonPanel = createButtonPanel();
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(buttonPanel);

        // Add some space
        topPanel.add(Box.createVerticalStrut(10));

        // Filter panel
        JPanel filterPanel = createFilterPanel();
        filterPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(filterPanel);

        return topPanel;
    }

    // Create form panel with input fields
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Todo Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Title row
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Title:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        titleField.setPreferredSize(new Dimension(200, 25));
        formPanel.add(titleField, gbc);

        // Description row
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Description:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setPreferredSize(new Dimension(200, 80));
        descScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        descScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        formPanel.add(descScrollPane, gbc);

        // Completed checkbox row
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(completedCheckbox, gbc);

        return formPanel;
    }

    // Create button panel
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        return buttonPanel;
    }

    // Create filter panel
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterComboBox);
        return filterPanel;
    }

    // Setup all event listeners
    private void setupEventListeners() {
        // Button listeners
        addButton.addActionListener(e -> addNewTodo());
        updateButton.addActionListener(e -> updateTodo());
        deleteButton.addActionListener(e -> deleteTodo());
        refreshButton.addActionListener(e -> loadAllTodos());

        // Filter listener
        filterComboBox.addActionListener(e -> applyFilter());

        // Table click listener
        todoTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                loadSelectedTodoToForm();
            }
        });
    }

    // Load all todos from database
    private void loadAllTodos() {
        try {
            allTodos = todoappDAO.getAllTodos();
            applyFilter();
        } catch (SQLException ex) {
            showError("Failed to load todos: " + ex.getMessage());
        }
    }

    // Apply filter and refresh table
    private void applyFilter() {
        String selectedFilter = (String) filterComboBox.getSelectedItem();
        List<Todo> filteredTodos = new ArrayList<>();

        if (selectedFilter == null || selectedFilter.equals("All")) {
            filteredTodos = allTodos;
        } else if (selectedFilter.equals("Completed")) {
            for (Todo todo : allTodos) {
                if (todo.isCompleted()) {
                    filteredTodos.add(todo);
                }
            }
        } else if (selectedFilter.equals("Incomplete")) {
            for (Todo todo : allTodos) {
                if (!todo.isCompleted()) {
                    filteredTodos.add(todo);
                }
            }
        }

        displayTodosInTable(filteredTodos);
    }

    // Display todos in the table
    private void displayTodosInTable(List<Todo> todos) {
        // Clear existing rows
        tableModel.setRowCount(0);

        // Add each todo as a row
        for (Todo todo : todos) {
            Object[] row = {
                    todo.getId(),
                    todo.getTitle(),
                    todo.getDescription(),
                    todo.isCompleted(),
                    todo.getCreated_at(),
                    todo.getUpdated_at()
            };
            tableModel.addRow(row);
        }
    }

    // Load selected todo data into form fields
    private void loadSelectedTodoToForm() {
        int selectedRow = todoTable.getSelectedRow();
        if (selectedRow < 0) {
            return; // No row selected
        }

        // Get data from selected row
        String title = (String) tableModel.getValueAt(selectedRow, 1);
        String description = (String) tableModel.getValueAt(selectedRow, 2);
        Boolean completed = (Boolean) tableModel.getValueAt(selectedRow, 3);

        // Set form fields
        titleField.setText(title);
        descriptionArea.setText(description);
        completedCheckbox.setSelected(completed != null && completed);
    }

    // Add a new todo
    private void addNewTodo() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        boolean completed = completedCheckbox.isSelected();

        // Validate input
        if (title.isEmpty()) {
            showWarning("Please enter a title for the todo");
            return;
        }

        // Create new todo
        Todo newTodo = new Todo(
                UUID.randomUUID().toString(),
                title,
                description,
                completed,
                LocalDateTime.now(),
                LocalDateTime.now());

        try {
            todoappDAO.addTodo(newTodo);
            clearForm();
            loadAllTodos();
            showInfo("Todo added successfully!");
        } catch (SQLException ex) {
            showError("Failed to add todo: " + ex.getMessage());
        }
    }

    // Update selected todo
    private void updateTodo() {
        int selectedRow = todoTable.getSelectedRow();
        if (selectedRow < 0) {
            showWarning("Please select a todo to update");
            return;
        }

        String id = (String) tableModel.getValueAt(selectedRow, 0);
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        boolean completed = completedCheckbox.isSelected();

        // Validate input
        if (title.isEmpty()) {
            showWarning("Please enter a title for the todo");
            return;
        }

        // Get original creation date
        LocalDateTime createdDate = (LocalDateTime) tableModel.getValueAt(selectedRow, 4);

        // Create updated todo
        Todo updatedTodo = new Todo(
                id,
                title,
                description,
                completed,
                createdDate,
                LocalDateTime.now());

        try {
            todoappDAO.updateTodo(updatedTodo);
            loadAllTodos();
            showInfo("Todo updated successfully!");
        } catch (SQLException ex) {
            showError("Failed to update todo: " + ex.getMessage());
        }
    }

    // Delete selected todo
    private void deleteTodo() {
        int selectedRow = todoTable.getSelectedRow();
        if (selectedRow < 0) {
            showWarning("Please select a todo to delete");
            return;
        }

        // Confirm deletion
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this todo?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String id = (String) tableModel.getValueAt(selectedRow, 0);

        try {
            todoappDAO.deleteTodo(id);
            clearForm();
            loadAllTodos();
            showInfo("Todo deleted successfully!");
        } catch (SQLException ex) {
            showError("Failed to delete todo: " + ex.getMessage());
        }
    }

    // Clear all form fields
    private void clearForm() {
        titleField.setText("");
        descriptionArea.setText("");
        completedCheckbox.setSelected(false);
    }

    // Helper methods for showing messages
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}