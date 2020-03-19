package ru.spbstu.amcp.impr.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ClientInfoStatus;

import ru.spbstu.amcp.impr.server.requests.ImageProcessingRequest;

public class ImageProcessorServer {

    private static final int port = 8888;

    private static final String rootPath = "images";

    public static void main(String[] args) {

        ObjectInputStream ois;
        Object request = null;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                ois = new ObjectInputStream(serverSocket.accept().getInputStream());
                request = ois.readObject();
                ImageProcessingRequest parsedRequest = (ImageProcessingRequest) request;
                processRequest(parsedRequest);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private static void processRequest(ImageProcessingRequest request) {
        ImageProcessingRequest.printMe(request);
    }
}
