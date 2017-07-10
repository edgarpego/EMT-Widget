package es.tamarit.widgetemt.controllers.widget;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.tamarit.widgetemt.controllers.AbstractController;
import es.tamarit.widgetemt.services.favourites.Favourite;
import es.tamarit.widgetemt.services.favourites.FavouritesService;
import es.tamarit.widgetemt.services.favourites.FavouritesServiceImpl;
import es.tamarit.widgetemt.services.properties.FilePropertiesService;
import es.tamarit.widgetemt.services.properties.SettingsPropertiesServiceImpl;
import es.tamarit.widgetemt.services.stoptimes.StopTimesService;
import es.tamarit.widgetemt.services.stoptimes.StopTimesServiceImpl;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class WidgetController extends AbstractController {
    
    private static final Logger LOGGER = LogManager.getLogger(WidgetController.class);
    public static final String URL_VIEW = "/views/widget/WidgetView.fxml";
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    @FXML
    private ProgressIndicator progress;
    @FXML
    private Button refreshButton;
    @FXML
    private Button moveButton;
    @FXML
    private Pane helpPane;
    @FXML
    private WebView myWebView;
    @FXML
    private ComboBox<Favourite> favouritesComboBox;
    
    private WebEngine myWebEngine;
    private FilePropertiesService properties;
    private String response;
    private StopTimesService stopTimesService;
    private FavouritesService favouriteService;
    
    @FXML
    public void initialize() {
        setMoveListeners();
        
        try {
            properties = new SettingsPropertiesServiceImpl();
            favouriteService = new FavouritesServiceImpl(properties);
            
            Platform.runLater(() -> {
                Boolean alwaysOnFront = Boolean.valueOf(properties.getProperty("always.on.front").toString());
                viewManager.getSecondaryStage().setAlwaysOnTop(alwaysOnFront);
            });
            
            favouritesComboBox.setItems(favouriteService.getAllFavourites());
            favouritesComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    Locale locale = Locale.forLanguageTag(properties.getProperty("application.language.locale"));
                    String language = locale.getLanguage();
                    stopTimesService = new StopTimesServiceImpl(newSelection.getStopName(), newSelection.getLineFilter(), newSelection.getAdapted(), language);
                    printTimes();
                }
            });
            
            String stopFilter = properties.getProperty("bus.stop.name");
            String[] parts = stopFilter.split(":");
            
            String stopName = null;
            String lineFilter = "";
            String adapted = "false";
            
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
                
                stopTimesService = new StopTimesServiceImpl(stopName, lineFilter, adapted, language);
                
                if (Boolean.valueOf(properties.getProperty("auto.refresh.data").toString()) == Boolean.TRUE) {
                    scheduler.scheduleAtFixedRate(() -> printTimes(), 0, 30, TimeUnit.SECONDS);
                } else {
                    printTimes();
                }
            } else {
                helpPane.setVisible(true);
            }
        } catch (IOException e) {
            LOGGER.error("Error trying to load the properties", e);
        }
    }
    
    @FXML
    private void reloadHandler() {
        printTimes();
    }
    
    private void printTimes() {
        
        if (stopTimesService != null) {
            LOGGER.info("Printing the times");
            
            Platform.runLater(() -> {
                progress.setVisible(true);
                refreshButton.setDisable(true);
                myWebEngine.loadContent("");
            });
            
            new Thread(() -> {
                try {
                    
                    response = stopTimesService.findByNameAndLineAndAdapted();
                    
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
    
    @FXML
    private void closeApplication() {
        Platform.runLater(() -> {
            viewManager.getSecondaryStage().close();
            System.exit(0);
        });
    }
    
    private void setMoveListeners() {
        
        final Delta dragDelta = new Delta();
        moveButton.setOnMousePressed(mouseEvent -> {
            moveButton.getScene().setCursor(Cursor.MOVE);
            dragDelta.x = viewManager.getSecondaryStage().getX() - mouseEvent.getScreenX();
            dragDelta.y = viewManager.getSecondaryStage().getY() - mouseEvent.getScreenY();
        });
        
        moveButton.setOnMouseReleased(mouseEvent -> {
            moveButton.getScene().setCursor(Cursor.HAND);
            
            properties.setProperty("widget.position.x", String.valueOf(viewManager.getSecondaryStage().getX()));
            properties.setProperty("widget.position.y", String.valueOf(viewManager.getSecondaryStage().getY()));
            properties.store();
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
