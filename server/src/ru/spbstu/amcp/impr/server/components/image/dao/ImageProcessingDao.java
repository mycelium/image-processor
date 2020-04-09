package ru.spbstu.amcp.impr.server.components.image.dao;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ru.spbstu.amcp.impr.server.components.image.dao.entity.ImageProcessingTask;
import ru.spbstu.amcp.impr.server.components.image.dao.entity.TaskStatus;

public class ImageProcessingDao {

    private static final String database = "jdbc:sqlite:db/tasks.db";
    private static final boolean isDebug = true;

    private static ImageProcessingDao instance;
    private static final Object monitor = new Object();

    private ImageProcessingDao() {
        super();
        this.createDatabase();
    }

    private void createDatabase() {
        String createTaskTableSqL = "CREATE TABLE IF NOT EXISTS tasks (\n" + "    id text PRIMARY KEY,\n" + "    status text NOT NULL,\n"
                + "    filename text NOT NULL\n" + ");";
        try (Connection conn = DriverManager.getConnection(database); Statement stmt = conn.createStatement()) {
            if (conn != null) {
                stmt.execute(createTaskTableSqL);
                System.out.println("A new task table created");
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        if (isDebug) {
            printDriverInfoAndTables();
        }
    }

    public Optional<ImageProcessingTask> getTaskById(String id) {
        String selectSQL = "SELECT id, status, filename " + "FROM tasks WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(database); PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                ImageProcessingTask task = new ImageProcessingTask();
                task.setId(rs.getString("id"));
                task.setStatus(rs.getString("status"));
                task.setFilename(rs.getString("filename"));
                return Optional.of(task);
            }
            if (rs.next()) {
                throw new SQLException("Multiple records found by id");
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return Optional.empty();
    }

    public void updateTaskStatus(String id, TaskStatus newStatus) {
        String updateSQL = "UPDATE tasks SET status=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(database); PreparedStatement stmt = conn.prepareStatement(updateSQL)) {
            stmt.setString(1, newStatus.toString());
            stmt.setString(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public String createTask(Path pathToImage) {
        String insetSQL = "INSERT INTO tasks(id,status,filename) VALUES(?,?,?)";
        String id = UUID.randomUUID().toString();
        try (Connection conn = DriverManager.getConnection(database); PreparedStatement stmt = conn.prepareStatement(insetSQL)) {
            stmt.setString(1, id);
            stmt.setString(2, TaskStatus.created.toString());
            stmt.setString(3, pathToImage.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return id;
    }

    public List<ImageProcessingTask> getAllTasks() {
        List<ImageProcessingTask> resultList = new LinkedList<>();
        String sql = "SELECT id, status, filename FROM tasks";
        try (Connection conn = DriverManager.getConnection(database);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                resultList.add(new ImageProcessingTask(rs.getString("id"), rs.getString("status"), rs.getString("filename")));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return resultList;
    }

    private void printDriverInfoAndTables() {
        try (Connection conn = DriverManager.getConnection(database)) {
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("The driver name is " + meta.getDriverName());
            ResultSet tables = meta.getTables(null, null, "tasks", null);
            while (tables.next()) {
                System.out.println("Table name: " + tables.getString("Table_NAME"));
                System.out.println("Table type: " + tables.getString("TABLE_TYPE"));
                System.out.println("Table schema: " + tables.getString("TABLE_SCHEM"));
                System.out.println("Table catalog: " + tables.getString("TABLE_CAT"));
                System.out.println(" ");
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static ImageProcessingDao getInstance() {
        if (instance == null) {
            synchronized (monitor) {
                if (instance == null) {
                    instance = new ImageProcessingDao();
                }
            }
        }
        return instance;
    }
}
