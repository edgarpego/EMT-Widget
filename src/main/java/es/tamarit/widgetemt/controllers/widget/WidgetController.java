package es.tamarit.widgetemt.controllers.widget;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.tamarit.widgetemt.controllers.AbstractController;
import es.tamarit.widgetemt.core.ViewManager;
import es.tamarit.widgetemt.services.stoptimes.StopTimes;
import es.tamarit.widgetemt.services.stoptimes.StopTimesImpl;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class WidgetController extends AbstractController {
    
    private static final Logger LOGGER = LogManager.getLogger(WidgetController.class);
    public static final String URL_VIEW = "/views/widget/WidgetView.fxml";
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    @FXML
    private WebView myWebView;
    @FXML
    private ProgressIndicator progress;
    @FXML
    private Button refreshButton;
    @FXML
    private Button moveButton;
    
    private WebEngine myWebEngine;
    private Properties properties;
    private String response;
    private StopTimes stopTimes;
    
    @FXML
    public void initialize() {
        try {
            setListeners();
            
            properties = new Properties();
            InputStream inputStream = new FileInputStream(ViewManager.FILE_SETTINGS);
            properties.load(inputStream);
            inputStream.close();
            
            String stopFilter = properties.getProperty("bus.stop.name");
            String[] parts = stopFilter.split(":");
            
            String stopName = null;
            String lineFilter = "none";
            String adapted = "0";
            
            switch (parts.length) {
                case 3:
                    adapted = parts[2];
                case 2:
                    lineFilter = parts[1];
                case 1:
                    stopName = parts[0];
                    break;
            }
            
            if (stopName != null && !stopName.isEmpty()) {
                
                myWebView.setContextMenuEnabled(false);
                myWebEngine = myWebView.getEngine();
                myWebEngine.setUserStyleSheetLocation(getClass().getResource("/css/webstyle.css").toString());
                
                Locale locale = Locale.forLanguageTag(properties.getProperty("application.language.locale"));
                String language = locale.getLanguage();
                
                stopTimes = new StopTimesImpl(stopName, lineFilter, adapted, language);
                
                if (Boolean.valueOf(properties.get("auto.refresh.data").toString()) == Boolean.TRUE) {
                    scheduler.scheduleAtFixedRate(() -> printTimes(), 0, 30, TimeUnit.SECONDS);
                } else {
                    printTimes();
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Error trying to get the properties file", e);
        } catch (IOException e) {
            LOGGER.error("Error trying to load the properties", e);
        }
    }
    
    @FXML
    private void reloadHandler() {
        printTimes();
    }
    
    private void printTimes() {
        
        if (stopTimes != null) {
            LOGGER.info("Printing the times");
            
            Platform.runLater(() -> {
                progress.setVisible(true);
                refreshButton.setDisable(true);
                myWebEngine.loadContent("");
            });
            
            new Thread(() -> {
                try {
                    
                    response = stopTimes.findByNameAndLineAndAdapted();
                    
                } catch (IOException e) {
                    LOGGER.error("Error trying to get the response from the EMT server", e);
                } finally {
                    
                    Platform.runLater(() -> {
                        if (response != null && !response.isEmpty()) {
                            if (response.contains("No disponible") || response.contains("Out of service")) {
                                printTimes();
                            } else {
                                myWebEngine.loadContent("<html><body>" + response + "</body></html>");
                                refreshButton.setDisable(false);
                                progress.setVisible(false);
                            }
                        } else {
                            myWebEngine.loadContent("<html><body><h3>There was an error, Try again in a few seconds!</h3></body></html>");
                            refreshButton.setDisable(false);
                            progress.setVisible(false);
                        }
                    });
                }
            }).start();
        }
    }
    
    @FXML
    private void openSettingsWindow() {
        scheduler.shutdown();
        viewManager.loadSettingsView();
    }
    
    private void setListeners() {
        
        final Delta dragDelta = new Delta();
        moveButton.setOnMousePressed(mouseEvent -> {
            moveButton.getScene().setCursor(Cursor.MOVE);
            dragDelta.x = viewManager.getSecondaryStage().getX() - mouseEvent.getScreenX();
            dragDelta.y = viewManager.getSecondaryStage().getY() - mouseEvent.getScreenY();
        });
        
        moveButton.setOnMouseReleased(mouseEvent -> {
            try {
                moveButton.getScene().setCursor(Cursor.HAND);
                OutputStream output = new FileOutputStream(ViewManager.FILE_SETTINGS);
                properties.setProperty("widget.position.x", String.valueOf(viewManager.getSecondaryStage().getX()));
                properties.setProperty("widget.position.y", String.valueOf(viewManager.getSecondaryStage().getY()));
                properties.store(output, null);
                output.close();
            } catch (FileNotFoundException e) {
                LOGGER.error("Error trying open the file.", e);
            } catch (IOException e) {
                LOGGER.error("Error trying to save the new data to the file.", e);
            }
        });
        
        moveButton.setOnMouseDragged(mouseEvent -> {
            moveButton.getScene().setCursor(Cursor.MOVE);
            viewManager.getSecondaryStage().setX(mouseEvent.getScreenX() + dragDelta.x);
            viewManager.getSecondaryStage().setY(mouseEvent.getScreenY() + dragDelta.y);
        });
        
        moveButton.setOnMouseEntered(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                moveButton.getScene().setCursor(Cursor.HAND);
            }
        });
        
        moveButton.setOnMouseExited(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                moveButton.getScene().setCursor(Cursor.DEFAULT);
            }
        });
    }
}

class Delta {
    
    double x, y;
}
