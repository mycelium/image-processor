package ru.spbstu.amcp.impr.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spbstu.amcp.impr.server.components.common.AppConfig;
import ru.spbstu.amcp.impr.server.components.image.api.socket.ImageSocket;
import ru.spbstu.amcp.impr.server.components.image.dao.ImageProcessingDao;
import ru.spbstu.amcp.impr.server.components.image.dao.entity.ImageProcessingTask;
import ru.spbstu.amcp.impr.server.components.image.service.ImageProcessingService;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
//        props.load(Files.newInputStream(Paths.get("properties", "imp.properties")));
        AppConfig config = AppConfig.getInstantce();
        int port = config.getInt("port").get();
        int socketThreads = config.getInt("socket.threads").orElse(1);
        ExecutorService processSocketRequestsThreadPool = Executors.newFixedThreadPool(socketThreads);
        ImageSocket socket = new ImageSocket(processSocketRequestsThreadPool, port);
        socket.start();
        ImageProcessingService service = ImageProcessingService.getInstnance();
        service.shutdown();
        try {
            service.waitUntilShutdowned(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            if (!service.isShutdown()) {
                try {
                    service.waitUntilShutdowned(10, TimeUnit.SECONDS);
                } catch (InterruptedException e1) {
                    logger.error(e1.getMessage());
                }
            }
        }
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
