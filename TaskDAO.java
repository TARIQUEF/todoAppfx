package todo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {
    private static final String DATABASE_URL = "jdbc:sqlite:" + getDatabasePath();

    // Initialize the database
    static {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS tasks ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "description TEXT NOT NULL, "
                    + "completed BOOLEAN NOT NULL)";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String getDatabasePath() {
        // Create a temporary file to hold the database
        File tempDbFile = new File(System.getProperty("java.io.tmpdir"), "tasks.db");

        if (!tempDbFile.exists()) {
            try (InputStream in = TaskDAO.class.getResourceAsStream("/tasks.db");
                 FileOutputStream out = new FileOutputStream(tempDbFile)) {
                if (in != null) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                } else {
                    throw new RuntimeException("Database file not found in resources.");
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to copy database file", e);
            }
        }

        return tempDbFile.getAbsolutePath();
    }

    // Get a connection
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    // Save a task
    public void saveTask(Task task) {
        String sql = "INSERT INTO tasks (description, completed) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, task.getDescription());
            pstmt.setBoolean(2, task.isCompleted());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    task.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete a task
    public void deleteTask(int taskId) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update a task
    public void updateTask(Task task) {
        String sql = "UPDATE tasks SET description = ?, completed = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.getDescription());
            pstmt.setBoolean(2, task.isCompleted());
            pstmt.setInt(3, task.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Load all tasks from database into an ArrayList
    public List<Task> loadTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT id, description, completed FROM tasks";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getInt("id"));
                task.setDescription(rs.getString("description"));
                task.setCompleted(rs.getBoolean("completed"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    // Debugging
    public void printDB() {
        String sql = "SELECT * FROM tasks";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            System.out.println("Current database state:");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") +
                        ", Description: " + rs.getString("description") +
                        ", Completed: " + rs.getBoolean("completed"));
            }
            System.out.println("---------");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
