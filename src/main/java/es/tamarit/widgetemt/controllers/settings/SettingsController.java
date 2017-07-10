package es.tamarit.widgetemt.controllers.settings;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.tamarit.widgetemt.controllers.AbstractController;
import es.tamarit.widgetemt.services.favourites.Favourite;
import es.tamarit.widgetemt.services.favourites.FavouritesService;
import es.tamarit.widgetemt.services.favourites.FavouritesServiceImpl;
import es.tamarit.widgetemt.services.properties.FilePropertiesService;
import es.tamarit.widgetemt.services.properties.SettingsPropertiesServiceImpl;
import es.tamarit.widgetemt.services.searchstop.SearchStopService;
import es.tamarit.widgetemt.services.searchstop.SearchStopServiceImpl;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;

public class SettingsController extends AbstractController {
    
    private static final Logger LOGGER = LogManager.getLogger(SettingsController.class);
    public static final String URL_VIEW = "/views/settings/SettingsView.fxml";
    
    private FilePropertiesService properties;
    private SearchStopService searchStopService;
    private FavouritesService favouriteService;
    
    @FXML
    private TableView<Favourite> favouritesTableView;
    @FXML
    private TableColumn<Favourite, String> busStopColumn;
    @FXML
    private TableColumn<Favourite, String> lineFilterColumn;
    @FXML
    private TableColumn<Favourite, String> adaptedColumn;
    
    @FXML
    private ComboBox<String> busStopCombo;
    @FXML
    private CheckBox alwaysOnTopCheck;
    @FXML
    private CheckBox autoRefreshCheck;
    @FXML
    private CheckBox adaptedCheck;
    @FXML
    private ToggleGroup languageGroup;
    @FXML
    private RadioButton spanishRadioButton;
    @FXML
    private RadioButton catalanRadioButton;
    @FXML
    private RadioButton englishRadioButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button addButton;
    @FXML
    private TextField lineFilterText;
    
    @FXML
    public void initialize() {
        LOGGER.info("Settings");
        
        try {
            properties = new SettingsPropertiesServiceImpl();
            searchStopService = new SearchStopServiceImpl();
            favouriteService = new FavouritesServiceImpl(properties);
            
            busStopCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    addButton.setDisable(false);
                } else {
                    addButton.setDisable(true);
                }
            });
            
            busStopCombo.getEditor().textProperty().addListener((ChangeListener<String>) (arg0, arg1, arg2) -> textEditor(arg1, arg2));
            alwaysOnTopCheck.setSelected(Boolean.valueOf(properties.getProperty("always.on.front")));
            autoRefreshCheck.setSelected(Boolean.valueOf(properties.getProperty("auto.refresh.data")));
            
            spanishRadioButton.setUserData("es-ES");
            catalanRadioButton.setUserData("ca-ES");
            englishRadioButton.setUserData("en-EN");
            
            String locale = properties.getProperty("application.language.locale");
            switch (locale) {
                case "es-ES":
                    spanishRadioButton.setSelected(true);
                    break;
                case "ca-ES":
                    catalanRadioButton.setSelected(true);
                    break;
                case "en-EN":
                    englishRadioButton.setSelected(true);
                    break;
            }
            
            Platform.runLater(() -> initializeTableView());
            
        } catch (IOException e) {
            LOGGER.error("Error trying to load the properties", e);
        }
    }
    
    private void initializeTableView() {
        
        busStopColumn.setCellValueFactory(cellData -> cellData.getValue().stopNameProperty());
        lineFilterColumn.setCellValueFactory(cellData -> cellData.getValue().lineFilterProperty());
        adaptedColumn.setCellValueFactory(cellData -> cellData.getValue().adaptedProperty());
        adaptedColumn.setCellFactory(column -> {
            return new TableCell<Favourite, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (item == null || empty) {
                        setText(null);
                        // setStyle("");
                        setGraphic(null);
                    } else {
                        
                        Pane image = new Pane();
                        image.setMaxWidth(15);
                        image.setMaxHeight(15);
                        
                        if (item.equals("false")) {
                            image.setStyle("-fx-background-color: #e74c3c;");
                        } else {
                            image.setStyle("-fx-background-color: #2ecc71;");
                        }
                        setGraphic(image);
                    }
                }
            };
        });
        
        favouritesTableView.setItems(favouriteService.getAllFavourites());
        favouritesTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                deleteButton.setDisable(false);
            }
        });
    }
    
    @FXML
    private void addToFavourites() {
        
        String stopName = busStopCombo.getValue();
        String lineFilter = lineFilterText.getText();
        String adapted = String.valueOf(adaptedCheck.isSelected());
        
        Favourite newData = new Favourite();
        newData.setStopName(stopName);
        newData.setLineFilter(lineFilter);
        newData.setAdapted(adapted);
        favouritesTableView.getItems().add(newData);
        
        busStopCombo.getEditor().clear();
        lineFilterText.clear();
        adaptedCheck.setSelected(false);
    }
    
    @FXML
    private void deleteRowSelected() {
        
        Favourite selectedItem = favouritesTableView.getSelectionModel().getSelectedItem();
        favouritesTableView.getItems().remove(selectedItem);
    }
    
    @FXML
    private void openWidgetViewConfirm() {
        
        if (busStopCombo.getValue() != null && !busStopCombo.getValue().isEmpty()) {
            properties.setProperty("bus.stop.name", busStopCombo.getValue());
        }
        
        properties.setProperty("always.on.front", String.valueOf(alwaysOnTopCheck.isSelected()));
        properties.setProperty("auto.refresh.data", String.valueOf(autoRefreshCheck.isSelected()));
        properties.setProperty("application.language.locale", languageGroup.getSelectedToggle().getUserData().toString());
        
        favouriteService.setAllFavourites(favouritesTableView.getItems());
        
        properties.store();
        
        viewManager.loadWidgetView();
    }
    
    @FXML
    private void openWidgetViewCancel() {
        viewManager.loadWidgetView();
    }
    
    private void textEditor(String textBefore, String textAfter) {
        
        String text = textAfter;
        
        if (!text.isEmpty()) {
            
            new Thread(() -> {
                
                try {
                    List<String> namesFound = searchStopService.findAll(text);
                    
                    Platform.runLater(() -> {
                        
                        if (namesFound != null && !namesFound.isEmpty()) {
                            // LOGGER.info(response);
                            busStopCombo.getItems().clear();
                            busStopCombo.hide();
                            
                            busStopCombo.getItems().addAll(namesFound);
                            
                            if (!busStopCombo.getItems().isEmpty()) {
                                busStopCombo.setVisibleRowCount(namesFound.size() > 10 ? 10 : namesFound.size());
                                busStopCombo.show();
                            }
                        }
                    });
                    
                } catch (IOException e) {
                    LOGGER.error("Error trying to get the response from the EMT server", e);
                }
            }).start();
        } else {
            busStopCombo.getItems().clear();
            busStopCombo.hide();
        }
    }
}
