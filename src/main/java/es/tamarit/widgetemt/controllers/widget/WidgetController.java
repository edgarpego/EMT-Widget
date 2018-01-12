package es.tamarit.widgetemt.controllers.widget;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.tamarit.widgetemt.controllers.AbstractController;
import es.tamarit.widgetemt.services.favorites.Favorite;
import es.tamarit.widgetemt.services.favorites.FavoritesService;
import es.tamarit.widgetemt.services.favorites.FavoritesServiceImpl;
import es.tamarit.widgetemt.services.stoptimes.LineTimeData;
import es.tamarit.widgetemt.services.stoptimes.StopTimesService;
import es.tamarit.widgetemt.services.stoptimes.StopTimesServiceImpl;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class WidgetController extends AbstractController {
    
    private static final Logger LOGGER = LogManager.getLogger(WidgetController.class);
    public static final String URL_VIEW = "/views/widget/WidgetView.fxml";
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    @FXML
    private TableView<LineTimeData> lineTimesTableView;
    @FXML
    private TableColumn<LineTimeData, String> lineImgColumn;
    @FXML
    private TableColumn<LineTimeData, String> lineNameColumn;
    @FXML
    private TableColumn<LineTimeData, String> lineTimeColumn;
    @FXML
    private Button refreshButton;
    @FXML
    private Button moveButton;
    @FXML
    private Pane helpPane;
    @FXML
    private ComboBox<Favorite> favoritesComboBox;
    
    private StopTimesService stopTimesService;
    private FavoritesService favoriteService;
    
    private static final int MAX_FAILS = 25;
    private int currentFails = 0;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        setMoveListeners();
        
        Platform.runLater(() -> {
            
            initializeTableView();
            
            favoriteService = new FavoritesServiceImpl(viewManager.getProperties());
            
            Boolean alwaysOnFront = Boolean.valueOf(viewManager.getProperties().getProperty("always.on.front").toString());
            viewManager.getSecondaryStage().setAlwaysOnTop(alwaysOnFront);
            
            favoritesComboBox.setItems(favoriteService.getAllFavorites());
            
            if (!favoritesComboBox.getItems().isEmpty()) {
                
                favoritesComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                    
                    if (newSelection != null) {
                        
                        Locale locale = Locale.forLanguageTag(viewManager.getProperties().getProperty("application.language.locale"));
                        String language = locale.getLanguage();
                        stopTimesService = new StopTimesServiceImpl(newSelection.getStopName(), newSelection.getLineFilter(), newSelection.getAdapted(), language);
                        if (lineTimesTableView.getItems() != null) {
                            lineTimesTableView.getItems().clear();
                        }
                        printTimes();
                    }
                });
                
                favoritesComboBox.getSelectionModel().select(0);
                
                if (Boolean.valueOf(viewManager.getProperties().getProperty("auto.refresh.data").toString()) == Boolean.TRUE) {
                    scheduler.scheduleAtFixedRate(() -> printTimes(), 30, 30, TimeUnit.SECONDS);
                }
                
            } else {
                helpPane.setVisible(true);
            }
        });
    }
    
    @FXML
    private void reloadHandler() {
        currentFails = 0;
        printTimes();
    }
    
    private void printTimes() {
        
        if (stopTimesService != null) {
            LOGGER.info("Printing the times");
            
            Platform.runLater(() -> {
                refreshButton.setDisable(true);
                
                new Thread(() -> {
                    try {
                        lineTimesTableView.setItems(stopTimesService.findByNameAndLineAndAdapted());
                        
                    } catch (IOException e) {
                        LOGGER.error("Error trying to get the response from the EMT server", e);
                    } finally {
                        
                        Platform.runLater(() -> {
                            if (lineTimesTableView.getItems() == null) {
                                currentFails++;
                                if (currentFails <= MAX_FAILS) {
                                    printTimes();
                                } else {
                                    refreshButton.setDisable(false);
                                }
                            } else {
                                currentFails = 0;
                                refreshButton.setDisable(false);
                            }
                        });
                    }
                }).start();
                
            });
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
    
    private void initializeTableView() {
        
        lineTimesTableView.setSelectionModel(null);
        
        lineImgColumn.setCellValueFactory(cellData -> cellData.getValue().getLineImgURLProperty());
        lineImgColumn.setCellFactory(column -> {
            return new TableCell<LineTimeData, String>() {
                
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                        setGraphic(null);
                    } else {
                        ImageView image = new ImageView(item);
                        image.setFitWidth(25);
                        image.setFitHeight(25);
                        setGraphic(image);
                    }
                }
            };
        });
        
        lineNameColumn.setCellValueFactory(cellData -> cellData.getValue().getLineNameProperty());
        lineTimeColumn.setCellValueFactory(cellData -> cellData.getValue().getLineTimeProperty());
        lineTimeColumn.setCellFactory(column -> {
            return new TableCell<LineTimeData, String>() {
                
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                        setGraphic(null);
                    } else {
                        
                        Label label = new Label();
                        label.setText(item);
                        label.setWrapText(true);
                        label.setMaxWidth(110);
                        label.setStyle("-fx-text-fill: #000;");
                        if (item.contains("min.")) {
                            if (Integer.valueOf(item.split(" ")[0]) <= 5) {
                                label.setStyle("-fx-text-fill: #a5eb78;");
                            }
                        } else if (item.contains("Pr") || item.contains("Next") || item.contains("Pr")) {
                            label.setStyle("-fx-text-fill: #a5eb78;");
                        } else if (!item.contains(":")) {
                            label.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                        }
                        setGraphic(label);
                    }
                }
            };
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
            
            viewManager.getProperties().setProperty("widget.position.x", String.valueOf(viewManager.getSecondaryStage().getX()));
            viewManager.getProperties().setProperty("widget.position.y", String.valueOf(viewManager.getSecondaryStage().getY()));
            viewManager.getProperties().store();
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
