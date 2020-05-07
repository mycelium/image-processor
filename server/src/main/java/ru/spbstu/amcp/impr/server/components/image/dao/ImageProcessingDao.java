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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import ru.spbstu.amcp.impr.server.components.image.dao.entity.ImageProcessingTask;
import ru.spbstu.amcp.impr.server.components.image.dao.entity.TaskStatus;
import ru.spbstu.amcp.impr.server.components.image.service.ImageProcessingTaskDao;

@Repository
public class ImageProcessingDao implements ImageProcessingTaskDao {

    private static final Logger logger = LoggerFactory.getLogger(ImageProcessingDao.class);
    @Value("${database.url}")
    private String database;
    @Value("${isDebug}")
    private boolean isDebug;

    private ImageProcessingDao() {
        super();
        this.createDatabase();
    }

    private void createDatabase() {
        String createTaskTableSqL = "CREATE TABLE IF NOT EXISTS tasks (\n" + "    id text PRIMARY KEY,\n" + "    status text NOT NULL,\n"
                + "    filename text NOT NULL,\n" + "    updated_at INTEGER NOT NULL\n" + ");";
        try (Connection conn = DriverManager.getConnection(database); Statement stmt = conn.createStatement()) {
            if (conn != null) {
                stmt.execute(createTaskTableSqL);
                logger.debug("A new task table created");
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        if (isDebug) {
            printDriverInfoAndTables();
        }
    }

    public Optional<ImageProcessingTask> getTaskById(String id) {
        String selectSQL = "SELECT id, status, filename, updated_at " + "FROM tasks WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(database); PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                ImageProcessingTask task = new ImageProcessingTask();
                task.setId(rs.getString("id"));
                task.setStatus(rs.getString("status"));
                task.setFilename(rs.getString("filename"));
                task.setUpdated(rs.getLong("updated_at"));
                return Optional.of(task);
            }
            if (rs.next()) {
                throw new SQLException("Multiple records found by id");
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return Optional.empty();
    }

    public void updateTaskStatus(String id, TaskStatus newStatus) {
        String updateSQL = "UPDATE tasks SET status=?, updated_at=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(database); PreparedStatement stmt = conn.prepareStatement(updateSQL)) {
            stmt.setString(1, newStatus.toString());
            stmt.setLong(2, System.currentTimeMillis());
            stmt.setString(3, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    public String createTask(Path pathToImage) {
        String insetSQL = "INSERT INTO tasks(id,status,filename, updated_at) VALUES(?,?,?,?)";
        String id = UUID.randomUUID().toString();
        try (Connection conn = DriverManager.getConnection(database); PreparedStatement stmt = conn.prepareStatement(insetSQL)) {
            stmt.setString(1, id);
            stmt.setString(2, TaskStatus.created.toString());
            stmt.setString(3, pathToImage.toString());
            stmt.setLong(4, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return id;
    }

    public List<ImageProcessingTask> getAllTasks() {
        List<ImageProcessingTask> resultList = new LinkedList<>();
        String sql = "SELECT id, status, filename, updated_at FROM tasks ORDER BY updated_at ASC";
        try (Connection conn = DriverManager.getConnection(database);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                resultList.add(new ImageProcessingTask(rs.getString("id"), rs.getString("status"), rs.getString("filename"),
                        rs.getLong("updated_at")));
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return resultList;
    }

    private void printDriverInfoAndTables() {
        try (Connection conn = DriverManager.getConnection(database)) {
            DatabaseMetaData meta = conn.getMetaData();
            logger.debug("The driver name is " + meta.getDriverName());
            ResultSet tables = meta.getTables(null, null, "tasks", null);
            while (tables.next()) {
                logger.debug("Table name: " + tables.getString("Table_NAME"));
                logger.debug("Table type: " + tables.getString("TABLE_TYPE"));
                logger.debug("Table schema: " + tables.getString("TABLE_SCHEM"));
                logger.debug("Table catalog: " + tables.getString("TABLE_CAT"));
                logger.debug(" ");
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }
}
