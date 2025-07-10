package org.example.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.logging.Level;

@Component
public class SpringFXMLLoader {
    private static final Logger LOGGER = Logger.getLogger(SpringFXMLLoader.class.getName());
    private final ApplicationContext applicationContext;

    public SpringFXMLLoader(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        if (applicationContext == null) {
            throw new IllegalArgumentException("ApplicationContext cannot be null");
        }
        LOGGER.info("SpringFXMLLoader initialized with ApplicationContext: " + applicationContext);
    }

    public Parent load(String fxmlPath) throws IOException {
        LOGGER.info("Loading FXML from path: " + fxmlPath);
        
        // First try to load from the class path
        URL resource = getClass().getResource(fxmlPath);
        
        // If not found, try with a leading slash
        if (resource == null && !fxmlPath.startsWith("/")) {
            resource = getClass().getResource("/" + fxmlPath);
            LOGGER.info("Trying with leading slash: /" + fxmlPath);
        }
        
        // If still not found, try with the class loader
        if (resource == null) {
            resource = getClass().getClassLoader().getResource(fxmlPath);
            LOGGER.info("Trying with ClassLoader: " + fxmlPath);
        }
        
        if (resource == null) {
            String error = "Cannot find FXML file: " + fxmlPath;
            LOGGER.severe(error);
            throw new IOException(error);
        }
        
        LOGGER.info("Found resource at: " + resource);

        FXMLLoader loader = new FXMLLoader(resource);
        
        // Set up the controller factory to use Spring context
        loader.setControllerFactory(clazz -> {
            LOGGER.info("Creating controller for class: " + clazz.getName());
            try {
                // Get the actual controller class (remove CGLIB proxy if present)
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
                
                LOGGER.info("Controller created from Spring context: " + controller);
                return controller;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to create controller for " + clazz.getName(), e);
                throw new RuntimeException("Failed to create controller", e);
            }
        });

        try {
            Parent root = loader.load();
            Object controller = loader.getController();
            
            if (controller == null) {
                LOGGER.severe("Controller was not created for FXML: " + fxmlPath);
                throw new RuntimeException("Controller was not created for FXML: " + fxmlPath);
            }
            
            // Store the loader in the root's properties
            root.getProperties().put(FXMLLoader.class.getName(), loader);
            
            LOGGER.info("FXML loaded successfully. Controller: " + controller.getClass().getName());
            verifyFXMLInjection(controller);
            
            return root;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading FXML: " + fxmlPath, e);
            throw new IOException("Error loading FXML: " + fxmlPath, e);
        }
    }

    private void verifyFXMLInjection(Object controller) {
        LOGGER.info("Verifying FXML injection for controller: " + controller.getClass().getName());
        try {
            // Get the actual class (remove CGLIB proxy if present)
            Class<?> actualClass = controller.getClass();
            if (AopUtils.isCglibProxy(controller)) {
                actualClass = AopProxyUtils.ultimateTargetClass(controller.getClass());
                controller = AopProxyUtils.getSingletonTarget(controller);
                LOGGER.info("Detected proxy class, using actual class: " + actualClass.getName());
            }
            
            java.lang.reflect.Field[] fields = actualClass.getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                if (field.isAnnotationPresent(javafx.fxml.FXML.class)) {
                    field.setAccessible(true);
                    Object value = field.get(controller);
                    LOGGER.info("FXML field " + field.getName() + ": " + (value != null ? "INJECTED" : "NULL"));
                    if (value == null) {
                        LOGGER.warning("FXML injection failed for field: " + field.getName());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error verifying FXML injection", e);
        }
    }

    public <T> T loadAndGetController(String fxmlPath, Class<T> controllerClass) {
        try {
        LOGGER.info("Loading FXML and getting controller from path: " + fxmlPath);
            
            // First try to load from the class path
            URL resource = getClass().getResource(fxmlPath);
            
            // If not found, try with a leading slash
            if (resource == null && !fxmlPath.startsWith("/")) {
                resource = getClass().getResource("/" + fxmlPath);
                LOGGER.info("Trying with leading slash: /" + fxmlPath);
            }
            
            // If still not found, try with the class loader
            if (resource == null) {
                resource = getClass().getClassLoader().getResource(fxmlPath);
                LOGGER.info("Trying with ClassLoader: " + fxmlPath);
            }
            
            if (resource == null) {
                String error = "Cannot find FXML file: " + fxmlPath;
                LOGGER.severe(error);
                throw new RuntimeException(error);
            }
            
            LOGGER.info("Found resource at: " + resource);
            
            FXMLLoader loader = new FXMLLoader(resource);
            
            // Set up the controller factory to use Spring context (same as load method)
            loader.setControllerFactory(clazz -> {
                LOGGER.info("Creating controller for class: " + clazz.getName());
                try {
                    // Get the actual controller class (remove CGLIB proxy if present)
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
                    
                    LOGGER.info("Controller created from Spring context: " + controller);
                    return controller;
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to create controller for " + clazz.getName(), e);
                    throw new RuntimeException("Failed to create controller", e);
        }
            });
            
            Parent root = loader.load();
        T controller = loader.getController();
            
        if (controller == null) {
                LOGGER.severe("Controller was not created for FXML: " + fxmlPath);
                throw new RuntimeException("Controller was not created for FXML: " + fxmlPath);
        }
        
            // Set root if method exists
            try {
                controller.getClass().getMethod("setRoot", javafx.scene.Parent.class).invoke(controller, root);
                LOGGER.info("Successfully set root for controller: " + controller.getClass().getName());
            } catch (Exception e) {
                LOGGER.warning("Controller does not have a setRoot method or it could not be called: " + controller.getClass().getName() + ".setRoot(javafx.scene.Parent)");
            }
            
            LOGGER.info("FXML loaded successfully. Controller: " + controller.getClass().getName());
            verifyFXMLInjection(controller);
            
            return controller;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load FXML: " + fxmlPath, e);
            throw new RuntimeException("Failed to load FXML: " + fxmlPath, e);
        }
    }
}