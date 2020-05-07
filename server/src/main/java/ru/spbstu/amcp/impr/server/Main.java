package ru.spbstu.amcp.impr.server;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import ru.spbstu.amcp.impr.server.components.config.AppConfig;
import ru.spbstu.amcp.impr.server.components.image.api.socket.ImageSocket;
import ru.spbstu.amcp.impr.server.components.image.dao.ImageProcessingDao;
import ru.spbstu.amcp.impr.server.components.image.dao.entity.ImageProcessingTask;
import ru.spbstu.amcp.impr.server.components.image.service.ImageProcessingService;
import ru.spbstu.amcp.impr.server.components.image.service.ImageProcessingTaskDao;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        
        // Initialize Spring context
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        AppConfig config = context.getBean(AppConfig.class);
        ImageProcessingTaskDao dao = context.getBean(ImageProcessingDao.class);
        ImageProcessingService service = context.getBean(ImageProcessingService.class);

        
        int port = config.getPort();
        int socketThreads = config.getSocketThreads();
        ExecutorService processSocketRequestsThreadPool = Executors.newFixedThreadPool(socketThreads);
        ImageSocket socket = new ImageSocket(processSocketRequestsThreadPool, port);
        socket.start();
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
        printDatabase(dao);

        //        CompletableFuture imageProcessing = CompletableFuture.runAsync(() -> socket.start());
        //        imageProcessing.thenApplyAsync((Object param)->{
        //            return null;
        //        });
    }

    private static void printDatabase(ImageProcessingTaskDao dao) {
        List<ImageProcessingTask> allTasks = dao.getAllTasks();
        logger.info(
                allTasks.stream().map(task -> "id: " + task.getId() + "\tstatus" + task.getStatus() + "\tfilename:" + task.getFilename())
                        .collect(Collectors.joining("\n")));
    }

}
