package ru.spbstu.amcp.impr.server.components.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import ru.spbstu.amcp.impr.server.components.image.dao.ImageProcessingDao;
import ru.spbstu.amcp.impr.server.components.image.service.ImageProcessingService;

@Configuration
@PropertySource("classpath:properties/imp.properties")
@ComponentScan(basePackages = "ru.amcp.impr.server")
@Import({ImageProcessingDao.class, ImageProcessingService.class})
public class AppConfig {
    
    @Value("${port}")
    private int port;
    @Value("${socket.threads}")
    private int socketThreads;
    
    
    public int getPort() {
        return port;
    }
    public int getSocketThreads() {
        return socketThreads;
    }
    
}
