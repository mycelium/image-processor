package ru.spbstu.amcp.impr.server.components.image.api.dto;

import java.io.Serializable;

import com.google.gson.Gson;

public class ImageProcessingResponse implements Serializable {

    private String status;
    private int statusCode;
    private boolean responseToGetRequest;
    private String pathToProcessedImage;
    private String id;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getPathToProcessedImage() {
        return pathToProcessedImage;
    }

    public void setPathToProcessedImage(String pathToProcessedImage) {
        this.pathToProcessedImage = pathToProcessedImage;
    }

    public boolean isResponseToGetRequest() {
        return responseToGetRequest;
    }

    public void setResponseToGetRequest(boolean responseToGetRequest) {
        this.responseToGetRequest = responseToGetRequest;
    }
    
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
