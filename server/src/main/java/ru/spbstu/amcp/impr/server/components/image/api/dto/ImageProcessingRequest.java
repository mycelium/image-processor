package ru.spbstu.amcp.impr.server.components.image.api.dto;

import java.io.Serializable;

import com.google.gson.Gson;

public class ImageProcessingRequest implements Serializable {
    
    private String pathToImage;
    private boolean getRequest;
    private boolean poisoned;
    
    public String getPathToImage() {
        return pathToImage;
    }
    public ImageProcessingRequest setPathToImage(String pathToImage) {
        this.pathToImage = pathToImage;
        return this;
    }
    public boolean isGetRequest() {
        return getRequest;
    }
    public ImageProcessingRequest setGetRequest(boolean getRequest) {
        this.getRequest = getRequest;
        return this;
    }
    
    public boolean isPoisoned() {
        return poisoned;
    }
    public ImageProcessingRequest setPoisoned(boolean poisoned) {
        this.poisoned = poisoned;
        return this;
    }
    
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
