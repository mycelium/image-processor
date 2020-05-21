package ru.spbstu.amcp.impr.server.components.image.api.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ru.spbstu.amcp.impr.server.components.image.api.dto.ProcessedImage;
import ru.spbstu.amcp.impr.server.components.image.service.ImageProcessingService;

@RestController
@RequestMapping("/image")
public class ImageProcessingController {

    Logger logger = LoggerFactory.getLogger(ImageProcessingController.class);
    
    @Autowired
    ImageProcessingService imageService;
    
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ProcessedImage getImage(@PathVariable String id) {
        return new ProcessedImage().setId("test").setPathToProcessedImage("test");
    }

}
