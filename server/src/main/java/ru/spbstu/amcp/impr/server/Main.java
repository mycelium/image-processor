package ru.spbstu.amcp.impr.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spbstu.amcp.impr.server.components.image.dao.entity.ImageProcessingTask;
import ru.spbstu.amcp.impr.server.components.image.service.ImageProcessingTaskDao;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {

        // Initialize Spring context
        //        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        //        AppConfig config = context.getBean(AppConfig.class);
        //        ImageProcessingTaskDao dao = context.getBean(ImageProcessingDao.class);
        //        ImageProcessingService service = context.getBean(ImageProcessingService.class);

        int port = 8081;
        //        int port = config.getPort();
        //        int socketThreads = config.getSocketThreads();

        // Spring webmvc with tomcat
        Tomcat server = new Tomcat();
        server.setPort(port);
        String appBase = ".";
        Path tmpDir = Files.createTempDirectory("tomcat_image_proc");
        tmpDir.toFile().mkdir();
        server.setBaseDir(tmpDir.toAbsolutePath().toString());
        //        server.setHostname("127.0.0.1");
        server.getHost().setAppBase(appBase);
        server.addWebapp("", appBase);
        try {
            server.start();
            server.getServer().await();
        } catch (LifecycleException e2) {
            e2.printStackTrace();
        }

        // Socket based
        //        ExecutorService processSocketRequestsThreadPool = Executors.newFixedThreadPool(socketThreads);
        //        ImageSocket socket = new ImageSocket(processSocketRequestsThreadPool, port);
        //        socket.start();
        //
        //        service.shutdown();
        //        try {
        //            service.waitUntilShutdowned(10, TimeUnit.SECONDS);
        //        } catch (InterruptedException e) {
        //            if (!service.isShutdown()) {
        //                try {
        //                    service.waitUntilShutdowned(10, TimeUnit.SECONDS);
        //                } catch (InterruptedException e1) {
        //                    logger.error(e1.getMessage());
        //                }
        //            }
        //        }
        //        printDatabase(dao);

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
