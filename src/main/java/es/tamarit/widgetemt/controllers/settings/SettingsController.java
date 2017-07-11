package es.tamarit.widgetemt.controllers.settings;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.tamarit.widgetemt.controllers.AbstractController;
import es.tamarit.widgetemt.services.cardbalance.CardBalanceService;
import es.tamarit.widgetemt.services.cardbalance.CardBalanceServiceImpl;
import es.tamarit.widgetemt.services.favorites.Favorite;
import es.tamarit.widgetemt.services.favorites.FavoritesService;
import es.tamarit.widgetemt.services.favorites.FavoritesServiceImpl;
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
import javafx.scene.control.Label;
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
    
    private String errorString;
    
    private FilePropertiesService properties;
    private SearchStopService searchStopService;
    private FavoritesService favoriteService;
    private CardBalanceService cardBalanceService;
    
    @FXML
    private TableView<Favorite> favoritesTableView;
    @FXML
    private TableColumn<Favorite, String> busStopColumn;
    @FXML
    private TableColumn<Favorite, String> lineFilterColumn;
    @FXML
    private TableColumn<Favorite, String> adaptedColumn;
    
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
    private TextField cardNumberText;
    @FXML
    private Label cardBalanceLabel;
    
    @FXML
    public void initialize() {
        LOGGER.info("Settings");
        
        try {
            properties = new SettingsPropertiesServiceImpl();
            searchStopService = new SearchStopServiceImpl();
            favoriteService = new FavoritesServiceImpl(properties);
            
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
            cardNumberText.setText(properties.getProperty("number.mobilis.card"));
            
            spanishRadioButton.setUserData("es-ES");
            catalanRadioButton.setUserData("ca-ES");
            englishRadioButton.setUserData("en-EN");
            
            String locale = properties.getProperty("application.language.locale");
            switch (locale) {
                case "es-ES":
                    spanishRadioButton.setSelected(true);
                    cardBalanceService = new CardBalanceServiceImpl("es");
                    break;
                case "ca-ES":
                    catalanRadioButton.setSelected(true);
                    cardBalanceService = new CardBalanceServiceImpl("ca");
                    break;
                case "en-EN":
                    englishRadioButton.setSelected(true);
                    cardBalanceService = new CardBalanceServiceImpl("en");
                    break;
            }
            
            errorString = cardBalanceLabel.getText();
            
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
            return new TableCell<Favorite, String>() {
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
        
        favoritesTableView.setItems(favoriteService.getAllFavorites());
        favoritesTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                deleteButton.setDisable(false);
            }
        });
    }
    
    @FXML
    private void addToFavorites() {
        
        String stopName = busStopCombo.getValue();
        String lineFilter = lineFilterText.getText();
        String adapted = String.valueOf(adaptedCheck.isSelected());
        
        Favorite newData = new Favorite();
        newData.setStopName(stopName);
        newData.setLineFilter(lineFilter);
        newData.setAdapted(adapted);
        favoritesTableView.getItems().add(newData);
        
        busStopCombo.getEditor().clear();
        lineFilterText.clear();
        adaptedCheck.setSelected(false);
    }
    
    @FXML
    private void deleteRowSelected() {
        Favorite selectedItem = favoritesTableView.getSelectionModel().getSelectedItem();
        favoritesTableView.getItems().remove(selectedItem);
    }
    
    @FXML
    private void hideTheCadBalanceLabel() {
        cardBalanceLabel.setVisible(false);
    }
    
    @FXML
    private void knowTheCardBalance() {
        try {
            if (!cardNumberText.getText().isEmpty()) {
                
                String balance = cardBalanceService.findByCardNumber(cardNumberText.getText());
                
                if (balance != null && !balance.isEmpty()) {
                    cardBalanceLabel.setText(balance);
                    cardBalanceLabel.setVisible(true);
                } else {
                    cardBalanceLabel.setText(errorString);
                    cardBalanceLabel.setVisible(true);
                }
                
                properties.setProperty("number.mobilis.card", cardNumberText.getText());
                properties.store();
            }
        } catch (IOException e) {
            LOGGER.error("Error trying to get the response from the EMT server", e);
        }
    }
    
    @FXML
    private void openWidgetViewConfirm() {
        
        if (busStopCombo.getValue() != null && !busStopCombo.getValue().isEmpty()) {
            properties.setProperty("bus.stop.name", busStopCombo.getValue());
        }
        
        properties.setProperty("always.on.front", String.valueOf(alwaysOnTopCheck.isSelected()));
        properties.setProperty("auto.refresh.data", String.valueOf(autoRefreshCheck.isSelected()));
        properties.setProperty("application.language.locale", languageGroup.getSelectedToggle().getUserData().toString());
        
        favoriteService.setAllFavorites(favoritesTableView.getItems());
        
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
