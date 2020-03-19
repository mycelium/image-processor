package ru.spbstu.amcp.impr.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import ru.spbstu.amcp.impr.server.requests.ImageProcessingRequest;

public class ImageProcessorClient {

    private static final int port = 8888;

    private static final String rootPath = "images";

    public static void main(String[] args) {
        ImageProcessingRequest testRequest = new ImageProcessingRequest()
                .setGetRequest(true)
                .setPathToImage("Hello");
        try (Socket clientSocket = new Socket("127.0.0.1", port)) {
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
            oos.writeObject(testRequest);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
