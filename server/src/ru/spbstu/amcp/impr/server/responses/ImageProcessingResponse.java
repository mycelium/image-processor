package ru.spbstu.amcp.impr.server.responses;

import java.io.Serializable;

public class ImageProcessingResponse implements Serializable {

    private String status;
    private int statusCode;
    private boolean responseToGetRequest;
    private String pathToProcessedImage;

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

    public static void printMe(ImageProcessingResponse response) {
        System.out.println("{");
        System.out.println("\t\"pathToProcessedImage\":\"" + response.pathToProcessedImage + "\"");
        System.out.println("\t\"status\":\"" + response.status + "\"");
        System.out.println("\t'statusCode':" + response.statusCode);
        System.out.println("\t'responseToGetRequest':" + response.responseToGetRequest);
        System.out.println("}");
    }

}
