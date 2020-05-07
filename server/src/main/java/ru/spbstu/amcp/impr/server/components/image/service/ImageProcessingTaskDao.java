package ru.spbstu.amcp.impr.server.components.image.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import ru.spbstu.amcp.impr.server.components.image.dao.entity.ImageProcessingTask;
import ru.spbstu.amcp.impr.server.components.image.dao.entity.TaskStatus;

public interface ImageProcessingTaskDao {
    
    public String createTask(Path pathToImage);
    
    public void updateTaskStatus(String id, TaskStatus newStatus);
    
    public List<ImageProcessingTask> getAllTasks();
    
    public Optional<ImageProcessingTask> getTaskById(String id);
    
}
