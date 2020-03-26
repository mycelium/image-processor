package ru.spbstu.amcp.impr.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import ru.spbstu.amcp.impr.server.requests.ImageProcessingRequest;
import ru.spbstu.amcp.impr.server.responses.ImageProcessingResponse;

public class ImageProcessorClient {

    private static final int port = 8888;

    private static final String rootPath = "img";

    public static void main(String[] args) {
        ImageProcessingRequest testRequest = new ImageProcessingRequest().setGetRequest(false)
                .setPathToImage(Paths.get(rootPath, "cat.webp").toAbsolutePath().toString());
        long start = System.currentTimeMillis();
        CompletableFuture[] tasks = new CompletableFuture[5];
        for (int i = 0; i < tasks.length; i++) {
            tasks[i] = CompletableFuture.runAsync(() -> {
                try (Socket clientSocket = new Socket("127.0.0.1", port)) {
                    clientSocket.setKeepAlive(true);
                    ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                    oos.writeObject(testRequest);
                    oos.flush();
                    ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                    ImageProcessingResponse response = (ImageProcessingResponse) ois.readObject();
                    processImageProcessingResponse(response);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
        }
        ImageProcessingRequest poisonPill = new ImageProcessingRequest().setPoisoned(true);
        try (Socket clientSocket = new Socket("127.0.0.1", port)) {
            //            clientSocket.setKeepAlive(true);
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
            oos.writeObject(poisonPill);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            CompletableFuture.allOf(tasks).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("Time to process all requests: " + (System.currentTimeMillis() - start));
    }

    private static void processImageProcessingResponse(ImageProcessingResponse response) {
        System.out.println("I'am client thread: " + Thread.currentThread().getName());
        ImageProcessingResponse.printMe(response);
    }
}
