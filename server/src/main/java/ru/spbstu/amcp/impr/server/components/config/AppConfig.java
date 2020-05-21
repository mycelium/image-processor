package ru.spbstu.amcp.impr.server.components.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import ru.spbstu.amcp.impr.server.components.image.dao.ImageProcessingDao;
import ru.spbstu.amcp.impr.server.components.image.service.ImageProcessingService;

@Configuration
@PropertySource("classpath:properties/imp.properties")
@ComponentScan(value = { "ru.amcp.impr.server" },
excludeFilters = {@ComponentScan.Filter(type = FilterType.REGEX, pattern = "ru.amcp.impr.server.components.*.api.http")})
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
