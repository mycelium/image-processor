package ru.spbstu.amcp.impr.server.requests;

import java.io.Serializable;

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
    
    public static void printMe(ImageProcessingRequest request) {
        System.out.println("{");
        System.out.println("\t\"pathToImage\":\""+request.pathToImage+"\"");
        System.out.println("\t'getRequest':"+request.getRequest);
        System.out.println("}");
    }
    public boolean isPoisoned() {
        return poisoned;
    }
    public ImageProcessingRequest setPoisoned(boolean poisoned) {
        this.poisoned = poisoned;
        return this;
    }
    
}
