package ru.spbstu.amcp.impr.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.ClientInfoStatus;

import ru.spbstu.amcp.impr.server.requests.ImageProcessingRequest;
import ru.spbstu.amcp.impr.server.responses.ImageProcessingResponse;

public class ImageProcessorServer {

    private static final int port = 8888;

    private static final String rootPath = "images";

    public static void main(String[] args) {

        ObjectInputStream ois;
        ObjectOutputStream oos;
        Object request = null;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket client = serverSocket.accept();
                ois = new ObjectInputStream(client.getInputStream());
                request = ois.readObject();
                ImageProcessingRequest parsedRequest = (ImageProcessingRequest) request;
                ImageProcessingResponse response = processRequest(parsedRequest);
                oos = new ObjectOutputStream(client.getOutputStream());
                oos.writeObject(response);
                oos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private static ImageProcessingResponse processRequest(ImageProcessingRequest request) {

        ImageProcessingResponse response = new ImageProcessingResponse();
        System.out.println("I'am server thread: "+ Thread.currentThread().getName());
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
            Path imagePath = root.resolve(pathToImage.getFileName());
            try {
                Files.copy(pathToImage, imagePath, StandardCopyOption.REPLACE_EXISTING);
                response.setStatus("Success");
                response.setStatusCode(0);
            } catch (IOException e) {
                response.setStatus(e.getMessage());
                response.setStatusCode(-1);
            }
        }
        return response;

    }
}
