package ru.spbstu.amcp.impr.server.components.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppConfig {

    private static AppConfig instance;
    private static final Object monitor = new Object();
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    private Properties properties;

    private AppConfig() {
        super();
        properties = new Properties();
        try (InputStream is = AppConfig.class.getClassLoader().getResourceAsStream("properties/imp.properties")) {
            properties.load(is);
        } catch (IOException e) {
            logger.error("Property file could not be loaded!", e);
            throw new RuntimeException(e);
        }
    }
    
    // TODO do something with casting exception
    
    public Optional<Integer> getInt(String name) {
        if (properties.containsKey(name)) {
            return Optional.of(Integer.valueOf(properties.getProperty(name)));
        }
        return Optional.empty();
    }

    public Optional<String> getString(String name) {
        if (properties.containsKey(name)) {
            return Optional.of(properties.getProperty(name));
        }
        return Optional.empty();
    }
    
    public boolean getBoolean (String name) {
        if (properties.containsKey(name)) {
            return Boolean.valueOf(properties.getProperty(name));
        }
        return false;
    }

    public static AppConfig getInstantce() {
        if (instance == null) {
            synchronized (monitor) {
                if (instance == null) {
                    instance = new AppConfig();
                }
            }
        }
        return instance;
    }
}
