package org.example.config;

import javafx.fxml.FXMLLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import java.util.logging.Logger;
import java.util.logging.Level;

@Configuration
public class JavaFXConfiguration {
    private static final Logger LOGGER = Logger.getLogger(JavaFXConfiguration.class.getName());

    @Bean
    public FXMLLoader fxmlLoader(ApplicationContext applicationContext) {
        LOGGER.info("Creating new FXMLLoader instance");
        FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(clazz -> {
            LOGGER.info("Creating controller instance for class: " + clazz.getName());
            try {
                // Get the actual class (remove CGLIB proxy if present)
                Class<?> actualClass = clazz;
                if (AopUtils.isCglibProxy(clazz)) {
                    actualClass = AopProxyUtils.ultimateTargetClass(clazz);
                    LOGGER.info("Detected CGLIB proxy class, using actual class: " + actualClass.getName());
                }
                
                Object controller = applicationContext.getBean(actualClass);
                if (AopUtils.isCglibProxy(controller)) {
                    controller = AopProxyUtils.getSingletonTarget(controller);
                    LOGGER.info("Retrieved singleton target from proxy: " + controller);
                }
                
                LOGGER.info("Controller created successfully: " + controller);
                return controller;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to create controller for class: " + clazz.getName(), e);
                throw new RuntimeException("Failed to create controller", e);
            }
        });
        return loader;
    }

    @Bean
    public SpringFXMLLoader springFXMLLoader(ApplicationContext applicationContext) {
        LOGGER.info("Creating new SpringFXMLLoader instance");
        try {
            SpringFXMLLoader loader = new SpringFXMLLoader(applicationContext);
            LOGGER.info("SpringFXMLLoader created successfully");
            return loader;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create SpringFXMLLoader", e);
            throw new RuntimeException("Failed to create SpringFXMLLoader", e);
        }
    }
} 