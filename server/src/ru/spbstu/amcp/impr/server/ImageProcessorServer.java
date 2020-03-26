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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ru.spbstu.amcp.impr.server.requests.ImageProcessingRequest;
import ru.spbstu.amcp.impr.server.responses.ImageProcessingResponse;

public class ImageProcessorServer {

    private static final int port = 8888;

    private static final String rootPath = "images";

    private static volatile boolean isAlive = true;

    private static Lock lock = new ReentrantLock();

    private static ExecutorService threadPool = Executors.newFixedThreadPool(4);

    private static synchronized boolean isAlive() {
        return isAlive;
    }

    private static synchronized void setAlive(boolean livenessFlag) {
        isAlive = livenessFlag;
    }

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (isAlive()) {
                Socket client = serverSocket.accept();
                threadPool.submit(() -> {
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
                threadPool.shutdown();
            } else {
                e2.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
            Path pathToImage = Paths.get(request.getPathToImage());
            Path imagePath = root.resolve(Thread.currentThread().getName() + "_" + pathToImage.getFileName());
            try {
                Files.copy(pathToImage, imagePath, StandardCopyOption.REPLACE_EXISTING);
                Thread.sleep(10000);
                response.setStatus("Success");
                response.setStatusCode(0);
            } catch (IOException e) {
                response.setStatus(e.getMessage());
                response.setStatusCode(-1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return response;

    }
}
