package ru.spbstu.amcp.impr.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.spbstu.amcp.impr.server.requests.ImageProcessingRequest;
import ru.spbstu.amcp.impr.server.requests.TaskStatus;
import ru.spbstu.amcp.impr.server.responses.ImageProcessingResponse;

public class ImageProcessorServer {

    private static final int port = 8888;

    private static final String rootPath = "images";

    private static final String database = "jdbc:sqlite:db/tasks.db";

    private static volatile boolean isAlive = true;

    private static ExecutorService processSocketRequestsThreadPool = Executors.newFixedThreadPool(10);
    private static ExecutorService processImageThreadPool = Executors.newFixedThreadPool(2);

    private static synchronized boolean isAlive() {
        return isAlive;
    }

    private static synchronized void setAlive(boolean livenessFlag) {
        isAlive = livenessFlag;
    }

    public static void main(String[] args) {

        createDatabase();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (isAlive()) {
                Socket client = serverSocket.accept();
                processSocketRequestsThreadPool.submit(() -> {
                    ObjectInputStream ois;
                    ObjectOutputStream oos;
                    Object request = null;
                    try {
                        ois = new ObjectInputStream(client.getInputStream());
                        request = ois.readObject();
                        ImageProcessingRequest parsedRequest = (ImageProcessingRequest) request;
                        if (parsedRequest.isPoisoned()) {
                            System.err.println("Catch poison pill");
                            setAlive(false);
                            serverSocket.close();
                        } else {
                            ImageProcessingResponse response = processRequest(parsedRequest);
                            oos = new ObjectOutputStream(client.getOutputStream());
                            oos.writeObject(response);
                            oos.flush();
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (SocketException e2) {
            if (!isAlive()) {
                System.err.println("I'am poisoned...dying");
                processSocketRequestsThreadPool.shutdown();
            } else {
                e2.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        printDatabase();
    }

    private static void printDatabase() {
        String sql = "SELECT id, status, filename FROM tasks";

        try (Connection conn = DriverManager.getConnection(database);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println(rs.getString("id") + "\t" + rs.getString("status") + "\t" + rs.getString("filename"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void createDatabase() {

        String createTaskTableSqL = "CREATE TABLE IF NOT EXISTS tasks (\n" + "    id text PRIMARY KEY,\n" + "    status text NOT NULL,\n"
                + "    filename text NOT NULL\n" + ");";

        try (Connection conn = DriverManager.getConnection(database); Statement stmt = conn.createStatement()) {
            if (conn != null) {
                stmt.execute(createTaskTableSqL);
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                ResultSet tables = meta.getTables(null, null, "tasks", null);
                while (tables.next()) {
                    System.out.println("Table name: "+tables.getString("Table_NAME"));
                    System.out.println("Table type: "+tables.getString("TABLE_TYPE"));
                    System.out.println("Table schema: "+tables.getString("TABLE_SCHEM"));
                    System.out.println("Table catalog: "+tables.getString("TABLE_CAT"));
                    System.out.println(" ");
                 }
                System.out.println("A new database has been created.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static ImageProcessingResponse processRequest(ImageProcessingRequest request) {
        ImageProcessingResponse response = new ImageProcessingResponse();
        System.out.println("I'am server thread: " + Thread.currentThread().getName());
        ImageProcessingRequest.printMe(request);
        Path root = Paths.get(rootPath);
        if (request.isGetRequest()) {
            response.setResponseToGetRequest(true);
            Path imagePath = root.resolve(request.getPathToImage());
            if (Files.exists(imagePath) && !Files.isDirectory(imagePath)) {
                response.setStatus("Success");
                response.setStatusCode(0);
                response.setPathToProcessedImage(imagePath.toAbsolutePath().toString());
            } else {
                response.setStatus("Can't find processed image");
                response.setStatusCode(-1);
            }
        } else {
            response.setResponseToGetRequest(false);
            String insetSQL = "INSERT INTO tasks(id,status,filename) VALUES(?,?,?)";
            String id = UUID.randomUUID().toString();
            Path pathToImage = Paths.get(request.getPathToImage());
            try (Connection conn = DriverManager.getConnection(database); PreparedStatement stmt = conn.prepareStatement(insetSQL)) {
                stmt.setString(1, id);
                stmt.setString(2, TaskStatus.created.toString());
                stmt.setString(3, pathToImage.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            response.setId(id);
            response.setStatus("success");
            response.setStatusCode(0);
            processImageThreadPool.submit(() -> {
                String selectSQL = "SELECT id, status, filename " + "FROM tasks WHERE id = ?";

                try (Connection conn = DriverManager.getConnection(database); PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
                    pstmt.setString(1, id);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        String selectedId = rs.getString("id");
                        String selectedStatus = rs.getString("status");
                        String filename = rs.getString("filename");
                        TaskStatus status = TaskStatus.valueOf(selectedStatus);
                        if (status == TaskStatus.created) {
                            String updateSQL = "UPDATE tasks SET status=? WHERE id=?";
                            try (PreparedStatement stmt = conn.prepareStatement(updateSQL)) {
                                stmt.setString(1, TaskStatus.processing.toString());
                                stmt.setString(2, id);
                                stmt.executeUpdate();
                            } catch (SQLException e) {
                                System.out.println(e.getMessage());
                            }
                            Path imagePath = root.resolve(Thread.currentThread().getName() + "_" + pathToImage.getFileName());
                            Path pathToSelectedImage = Paths.get(filename);
                            try {
                                Files.copy(pathToSelectedImage, imagePath, StandardCopyOption.REPLACE_EXISTING);
                                Thread.sleep(10000);
                                try (PreparedStatement stmt = conn.prepareStatement(updateSQL)) {
                                    stmt.setString(1, TaskStatus.success.toString());
                                    stmt.setString(2, id);
                                    stmt.executeUpdate();
                                } catch (SQLException e) {
                                    System.out.println(e.getMessage());
                                }
                            } catch (IOException e) {
                                try (PreparedStatement stmt = conn.prepareStatement(updateSQL)) {
                                    stmt.setString(1, TaskStatus.error.toString());
                                    stmt.setString(2, id);
                                    stmt.executeUpdate();
                                } catch (SQLException ex) {
                                    System.out.println(e.getMessage());
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            System.err.println("I will not process task(" + selectedId + ") with status: " + status);
                        }
                    } else {
                        System.err.println("Can't find task by id: " + id);
                    }
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            });
        }
        return response;

    }
}
