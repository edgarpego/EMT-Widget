package es.tamarit.widgetemt.core;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.ImageIcon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.javafx.PlatformUtil;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
public class MainApp extends Application {
    
    private static final Logger LOGGER = LogManager.getLogger(MainApp.class);
    private static final String TRY_ICON_DEFAULT_16 = "/images/icon.png";
    private static final String TRY_ICON_DARK_16 = "/images/darkIcon.png";
    private static final String TRY_ICON_CLEAR_16 = "/images/clearIcon.png";
    
    private TrayIcon trayIcon;
    
    @Override
    public void start(Stage primaryStage) {
        LOGGER.info("Application starting");
        
        try {
            createTrayIcon(primaryStage);
            new ViewManager(primaryStage);
            
        } catch (AWTException e) {
            LOGGER.error("Error trying to create the tryicon.", e);
        }
    }
    
    private void createTrayIcon(final Stage stage) throws AWTException {
        if (SystemTray.isSupported()) {
            
            // get the SystemTray instance
            SystemTray tray = SystemTray.getSystemTray();
            
            // create a popup menu
            PopupMenu popup = new PopupMenu();
            
            MenuItem openItem = new java.awt.MenuItem("Show widget");
            openItem.addActionListener(event -> Platform.runLater(() -> {
                
                if (stage.isIconified()) {
                    stage.setIconified(false);
                    stage.show();
                    stage.toFront();
                }
            }));
            
            MenuItem hideItem = new java.awt.MenuItem("Hide widget");
            hideItem.addActionListener(event -> Platform.runLater(() -> {
                stage.setIconified(true);
            }));
            
            MenuItem exitItem = new java.awt.MenuItem("Close widget");
            exitItem.addActionListener(event -> {
                Platform.runLater(() -> {
                    stage.close();
                    System.exit(0);
                });
            });
            
            popup.add(openItem);
            popup.addSeparator();
            popup.add(hideItem);
            popup.addSeparator();
            popup.add(exitItem);
            
            if (PlatformUtil.isMac()) {
                if (isMacMenuBarDarkMode()) {
                    trayIcon = new TrayIcon(createImage(TRY_ICON_CLEAR_16, "Icon"), "EMT Widget", popup);
                } else {
                    trayIcon = new TrayIcon(createImage(TRY_ICON_DARK_16, "Icon"), "EMT Widget", popup);
                }
            } else {
                trayIcon = new TrayIcon(createImage(TRY_ICON_DEFAULT_16, "Icon"), "EMT Widget", popup);
            }
            
            /* Listener simple and double click on trayIcon */
            trayIcon.addMouseListener(new java.awt.event.MouseAdapter() {
                
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    Platform.runLater(() -> {
                        if (evt.getClickCount() == 1) {
                            // Simple
                            stage.toFront();
                        } else if (evt.getClickCount() == 2) {
                            // Double
                            if (stage.isIconified()) {
                                // Maximize it
                                stage.setIconified(false);
                                stage.show();
                                stage.toFront();
                            } else {
                                // Minimize it
                                stage.setIconified(true);
                            }
                        }
                    });
                }
                
            });
            
            tray.add(trayIcon);
        }
    }
    
    /**
     * Get the image for the trayIcon
     * @param path to file
     * @param description of the image
     * @return a java.awt.Image object
     */
    private static java.awt.Image createImage(String path, String description) {
        java.awt.Image ret = null;
        URL imageURL = MainApp.class.getResource(path);
        
        if (imageURL != null) {
            ret = (new ImageIcon(imageURL, description)).getImage();
        }
        
        return ret;
    }
    
    private boolean isMacMenuBarDarkMode() {
        try {
            String command = "defaults read -g AppleInterfaceStyle";
            Process process = Runtime.getRuntime().exec(command);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("dark")) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            LOGGER.warn("Could not determine, whether 'dark mode' is being used. Falling back to default (light) mode.");
        }
        return false;
    }
    
    public static void main(String... varargs) {
        launch(varargs);
    }
}
