package ru.spbstu.amcp.impr.server.components.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan("ru.spbstu.amcp.impr.server.components.image.api.http")
public class WebConfig {

}
