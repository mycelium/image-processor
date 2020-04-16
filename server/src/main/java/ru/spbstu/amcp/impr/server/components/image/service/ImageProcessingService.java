package ru.spbstu.amcp.impr.server.components.image.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spbstu.amcp.impr.server.components.common.AppConfig;
import ru.spbstu.amcp.impr.server.components.image.api.dto.ProcessedImage;
import ru.spbstu.amcp.impr.server.components.image.dao.ImageProcessingDao;
import ru.spbstu.amcp.impr.server.components.image.dao.entity.ImageProcessingTask;
import ru.spbstu.amcp.impr.server.components.image.dao.entity.TaskStatus;

public class ImageProcessingService {

    private static ImageProcessingService instance;
    private static final Object monitor = new Object();
    private static final Logger logger = LoggerFactory.getLogger(ImageProcessingService.class);

    private ExecutorService processImageThreadPool;
    private Path root;

    private ImageProcessingDao taskDao;

    private ImageProcessingService() {
        super();
        taskDao = ImageProcessingDao.getInstance();
        processImageThreadPool = Executors.newFixedThreadPool(2);
        root = Paths.get(AppConfig.getInstantce().getString("root.path").orElseThrow(RuntimeException::new));
    }

    public ProcessedImage getImageByName(String imageName) {
        ProcessedImage result = new ProcessedImage();
        Path imagePath = root.resolve(imageName);
        if (Files.exists(imagePath) && !Files.isDirectory(imagePath)) {
            result.setPathToProcessedImage(imagePath.toAbsolutePath().toString());
        }
        return result;
    }

    public ProcessedImage processImage(String pathToSourceImage) {
        ProcessedImage result = new ProcessedImage();
        String id = taskDao.createTask(Paths.get(pathToSourceImage));
        result.setId(id);
        processImageThreadPool.submit(() -> {
            Optional<ImageProcessingTask> task = taskDao.getTaskById(id);
            if (task.isPresent()) {
                TaskStatus status = task.get().getStatus();
                if (status == TaskStatus.created) {
                    Path pathToImage = root.resolve(task.get().getFilename());
                    taskDao.updateTaskStatus(id, TaskStatus.processing);
                    Path imagePath = root.resolve(Thread.currentThread().getName() + "_" + pathToImage.getFileName());
                    Path pathToSelectedImage = Paths.get(task.get().getFilename());
                    try {
                        Files.copy(pathToSelectedImage, imagePath, StandardCopyOption.REPLACE_EXISTING);
                        Thread.sleep(10000);
                        taskDao.updateTaskStatus(id, TaskStatus.success);
                    } catch (IOException e) {
                        taskDao.updateTaskStatus(id, TaskStatus.error);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    logger.error("I will not process task(" + task.get().getId() + ") with status: " + status);
                }
            } else {
                logger.error("Can't find task by id: " + id);
            }
        });
        return result;
    }

    public static ImageProcessingService getInstnance() {
        if (instance == null) {
            synchronized (monitor) {
                if (instance == null) {
                    instance = new ImageProcessingService();
                }
            }
        }
        return instance;
    }
}
