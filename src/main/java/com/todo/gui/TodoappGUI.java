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
import com.todo.dao.TodoappDAO;
import com.todo.dao.TodoappDAOImpl;

public class TodoappGUI extends JFrame {
    private TodoappDAO todoappDAO;
    private JTable todoTable;
    private DefaultTableModel tableModel;
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JCheckBox completedCheckbox;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JComboBox<String> filterComboBox;
    private List<Todo> allTodos = new ArrayList<>();

    public TodoappGUI() {
        this.todoappDAO = new TodoappDAOImpl();
        initializeComponents();
        setupLayout();
    }

    public void initializeComponents() {
        setTitle("Todo App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        String[] columns = { "ID", "Title", "Description", "Completed", "Created At", "Updated At" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        todoTable = new JTable(tableModel);
        todoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        todoTable.getSelectionModel().addListSelectionListener(
                (e) -> {
                    if (!e.getValueIsAdjusting()) {
                        // load the selected todo
                    }
                });

        titleField = new JTextField(20);
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        completedCheckbox = new JCheckBox("Completed");
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        refreshButton = new JButton("Refresh");

        String[] filterOptions = { "All", "Completed", "Incomplete" };
        filterComboBox = new JComboBox<>(filterOptions);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Panels
        JPanel formPanel = new JPanel(new GridBagLayout());
        JPanel buttonGrid = new JPanel(new GridLayout(2, 2, 12, 6));
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 2, 6));

        // Form (top centered)
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
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
        JScrollPane descScrollPane = new JScrollPane(descriptionArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        descScrollPane.setPreferredSize(new Dimension(250, 80));
        formPanel.add(descScrollPane, gbc);

        // Completed row
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(completedCheckbox, gbc);

        // Buttons 2x2 grid centered under the form
        Dimension btnSize = new Dimension(130, 26);
        addButton.setPreferredSize(btnSize);
        updateButton.setPreferredSize(btnSize);
        deleteButton.setPreferredSize(btnSize);
        refreshButton.setPreferredSize(btnSize);
        buttonGrid.add(addButton);
        buttonGrid.add(updateButton);
        buttonGrid.add(deleteButton);
        buttonGrid.add(refreshButton);
        JPanel buttonsWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        buttonsWrap.add(buttonGrid);

        // Filter row
        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterComboBox);

        // Compose top
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonsWrap.setAlignmentX(Component.CENTER_ALIGNMENT);
        filterPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(formPanel);
        topPanel.add(Box.createVerticalStrut(8));
        topPanel.add(buttonsWrap);
        topPanel.add(filterPanel);
        add(topPanel, BorderLayout.NORTH);

        // Table center
        add(new JScrollPane(todoTable), BorderLayout.CENTER);

        setupeventlisteners();
        loadTodosAndRefreshTable();
    }

    private void setupeventlisteners() {
        addButton.addActionListener(e -> addTodo());
        updateButton.addActionListener(e -> updateSelectedTodo());
        deleteButton.addActionListener(e -> deleteSelectedTodo());
        refreshButton.addActionListener(e -> loadTodosAndRefreshTable());
        filterComboBox.addActionListener(e -> applyFilterAndRefresh());

        todoTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                populateInputsFromSelection();
            }
        });
    }

    private void loadTodosAndRefreshTable() {
        try {
            allTodos = todoappDAO.getAllTodos();
            applyFilterAndRefresh();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load todos: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilterAndRefresh() {
        String selected = (String) filterComboBox.getSelectedItem();
        List<Todo> filtered = new ArrayList<>();
        if (selected == null || selected.equals("All")) {
            filtered = allTodos;
        } else if (selected.equals("Completed")) {
            for (Todo t : allTodos)
                if (t.isCompleted())
                    filtered.add(t);
        } else if (selected.equals("Incomplete")) {
            for (Todo t : allTodos)
                if (!t.isCompleted())
                    filtered.add(t);
        }
        populateTable(filtered);
    }

    private void populateTable(List<Todo> todos) {
        tableModel.setRowCount(0);
        for (Todo t : todos) {
            tableModel.addRow(new Object[] {
                    t.getId(),
                    t.getTitle(),
                    t.getDescription(),
                    t.isCompleted(),
                    t.getCreated_at(),
                    t.getUpdated_at()
            });
        }
    }

    private void populateInputsFromSelection() {
        int row = todoTable.getSelectedRow();
        if (row < 0)
            return;
        titleField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        descriptionArea.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        completedCheckbox.setSelected(Boolean.TRUE.equals(tableModel.getValueAt(row, 3)));
    }

    private void addTodo() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        boolean completed = completedCheckbox.isSelected();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title is required", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Todo todo = new Todo(UUID.randomUUID().toString(), title, description, completed, LocalDateTime.now(),
                LocalDateTime.now());
        try {
            todoappDAO.addTodo(todo);
            clearInputs();
            loadTodosAndRefreshTable();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to add todo: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSelectedTodo() {
        int row = todoTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a row to update", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id = String.valueOf(tableModel.getValueAt(row, 0));
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        boolean completed = completedCheckbox.isSelected();
        Todo todo = new Todo(id, title, description, completed, (LocalDateTime) tableModel.getValueAt(row, 4),
                LocalDateTime.now());
        try {
            todoappDAO.updateTodo(todo);
            loadTodosAndRefreshTable();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to update todo: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedTodo() {
        int row = todoTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a row to delete", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id = String.valueOf(tableModel.getValueAt(row, 0));
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected todo?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;
        try {
            todoappDAO.deleteTodo(id);
            clearInputs();
            loadTodosAndRefreshTable();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to delete todo: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearInputs() {
        titleField.setText("");
        descriptionArea.setText("");
        completedCheckbox.setSelected(false);
    }
}
