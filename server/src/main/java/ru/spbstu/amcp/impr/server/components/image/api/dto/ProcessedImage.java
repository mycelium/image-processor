package ru.spbstu.amcp.impr.server.components.image.api.dto;

public class ProcessedImage {
    private String pathToProcessedImage;
    private String id;
    
    public String getPathToProcessedImage() {
        return pathToProcessedImage;
    }
    public void setPathToProcessedImage(String pathToProcessedImage) {
        this.pathToProcessedImage = pathToProcessedImage;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    
}
