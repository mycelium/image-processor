package ru.spbstu.amcp.impr.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import ru.spbstu.amcp.impr.server.requests.ImageProcessingRequest;
import ru.spbstu.amcp.impr.server.responses.ImageProcessingResponse;

public class ImageProcessorClient {

    private static final int port = 8888;

    private static final String rootPath = "img";

    public static void main(String[] args) {
        ImageProcessingRequest testRequest = new ImageProcessingRequest()
                .setGetRequest(true)
                .setPathToImage("cat.webp");
        try (Socket clientSocket = new Socket("127.0.0.1", port)) {
            clientSocket.setKeepAlive(true);
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
            oos.writeObject(testRequest);
            oos.flush();
//            oos.close();
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
            ImageProcessingResponse response = (ImageProcessingResponse)ois.readObject();
            processImageProcessingResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void processImageProcessingResponse(ImageProcessingResponse response) {
        System.out.println("I'am client thread: "+ Thread.currentThread().getName());
        ImageProcessingResponse.printMe(response);
    }
}
