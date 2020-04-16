package ru.spbstu.amcp.impr.server;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spbstu.amcp.impr.server.components.image.api.socket.ImageSocket;
import ru.spbstu.amcp.impr.server.components.image.dao.ImageProcessingDao;
import ru.spbstu.amcp.impr.server.components.image.dao.entity.ImageProcessingTask;

public class Main {

    private static final int port = 8888;

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        ExecutorService processSocketRequestsThreadPool = Executors.newFixedThreadPool(10);
        ImageSocket socket = new ImageSocket(processSocketRequestsThreadPool, port);
        socket.start();
        printDatabase();

        //        CompletableFuture imageProcessing = CompletableFuture.runAsync(() -> socket.start());
        //        imageProcessing.thenApplyAsync((Object param)->{
        //            return null;
        //        });
    }

    private static void printDatabase() {
        ImageProcessingDao dao = ImageProcessingDao.getInstance();
        List<ImageProcessingTask> allTasks = dao.getAllTasks();
        logger.info(
                allTasks.stream().map(task -> "id: " + task.getId() + "\tstatus" + task.getStatus() + "\tfilename:" + task.getFilename())
                        .collect(Collectors.joining("\n")));
    }

}
