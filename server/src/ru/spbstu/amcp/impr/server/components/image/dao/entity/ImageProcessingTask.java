package ru.spbstu.amcp.impr.server.components.image.dao.entity;

public class ImageProcessingTask {
    private String id;
    private String status;
    private String filename;
    
    public ImageProcessingTask() {
        super();
    }
    public ImageProcessingTask(String id, String status, String filename) {
        super();
        this.id = id;
        this.status = status;
        this.filename = filename;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return TaskStatus.valueOf(status);
    }

    public void setStatus(TaskStatus status) {
        this.status = status.toString();
    }
    //TODO think about this api later
    public void setStatus(String status) {
        this.status = status.toString();
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
