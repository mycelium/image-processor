package ru.spbstu.amcp.impr.server.components.image.api.socket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spbstu.amcp.impr.server.components.image.api.dto.ImageProcessingRequest;
import ru.spbstu.amcp.impr.server.components.image.api.dto.ImageProcessingResponse;
import ru.spbstu.amcp.impr.server.components.image.api.dto.ProcessedImage;
import ru.spbstu.amcp.impr.server.components.image.service.ImageProcessingService;

public class ImageSocket {

    private ExecutorService processSocketRequestsThreadPool;
    private int port;
    private static volatile boolean isAlive = true;
    private static final Logger logger = LoggerFactory.getLogger(ImageSocket.class);
    
    public ImageSocket(ExecutorService processSocketRequestsThreadPool, int port) {
        super();
        this.processSocketRequestsThreadPool = processSocketRequestsThreadPool;
        this.port = port;
    }

    public void start() {
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
                            logger.warn("Catch poison pill");
                            setAlive(false);
                            serverSocket.close();
                        } else {
                            ImageProcessingResponse response = processRequest(parsedRequest);
                            logger.debug(response.toString());
                            oos = new ObjectOutputStream(client.getOutputStream());
                            oos.writeObject(response);
                            oos.flush();
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        logger.error(e.getMessage());
                    }
                });
            }
        } catch (SocketException e2) {
            if (!isAlive()) {
                logger.warn("I'am poisoned...dying");
                processSocketRequestsThreadPool.shutdown();
            } else {
                logger.error(e2.getMessage());
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private static ImageProcessingResponse processRequest(ImageProcessingRequest request) {
        ImageProcessingService service = ImageProcessingService.getInstnance();
        logger.debug("I'am server thread: " + Thread.currentThread().getName());
        logger.debug(request.toString());
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

    private static synchronized boolean isAlive() {
        return isAlive;
    }

    private static synchronized void setAlive(boolean livenessFlag) {
        isAlive = livenessFlag;
    }
}
