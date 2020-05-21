package ru.spbstu.amcp.impr.server.components.image.api.dto;

public class ProcessedImage {
    private String pathToProcessedImage;
    private String id;
    
    public String getPathToProcessedImage() {
        return pathToProcessedImage;
    }
    public ProcessedImage setPathToProcessedImage(String pathToProcessedImage) {
        this.pathToProcessedImage = pathToProcessedImage;
        return this;
    }
    public String getId() {
        return id;
    }
    public ProcessedImage setId(String id) {
        this.id = id;
        return this;
    }
    
}
