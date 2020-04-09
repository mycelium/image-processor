package ru.spbstu.amcp.impr.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import ru.spbstu.amcp.impr.server.components.image.api.dto.ProcessedImage;
import ru.spbstu.amcp.impr.server.components.image.dao.ImageProcessingDao;
import ru.spbstu.amcp.impr.server.components.image.dao.entity.ImageProcessingTask;
import ru.spbstu.amcp.impr.server.components.image.service.ImageProcessingService;
import ru.spbstu.amcp.impr.server.requests.ImageProcessingRequest;
import ru.spbstu.amcp.impr.server.responses.ImageProcessingResponse;

public class ImageProcessorServer {

    private static final int port = 8888;
    private static volatile boolean isAlive = true;

    private static ExecutorService processSocketRequestsThreadPool = Executors.newFixedThreadPool(10);

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
        ImageProcessingDao dao = ImageProcessingDao.getInstance();
        List<ImageProcessingTask> allTasks = dao.getAllTasks();
        System.out.println(
                allTasks.stream().map(task -> "id: " + task.getId() + "\tstatus" + task.getStatus() + "\tfilename:" + task.getFilename())
                        .collect(Collectors.joining("\n")));
    }

    private static ImageProcessingResponse processRequest(ImageProcessingRequest request) {
        ImageProcessingService service = ImageProcessingService.getInstnance();
        System.out.println("I'am server thread: " + Thread.currentThread().getName());
        ImageProcessingRequest.printMe(request);
        ProcessedImage result = null;
        if (request.isGetRequest()) {
            result = service.getImageByName(request.getPathToImage());
        } else {
            result = service.processImage(request.getPathToImage());
        }
        return mapToImageProcessingRespoonse(request, result);

    }

    private static ImageProcessingResponse mapToImageProcessingRespoonse(ImageProcessingRequest request, ProcessedImage result) {
        ImageProcessingResponse response = new ImageProcessingResponse();
        response.setId(result.getId());
        response.setPathToProcessedImage(result.getPathToProcessedImage());
        response.setResponseToGetRequest(request.isGetRequest());
        //TODO error processing
        response.setStatus("Success");
        response.setStatusCode(0);
        return response;
    }

}
