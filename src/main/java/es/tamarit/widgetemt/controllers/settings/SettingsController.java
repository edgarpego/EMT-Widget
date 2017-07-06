package es.tamarit.widgetemt.controllers.settings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.tamarit.widgetemt.api.searchstop.SearchStop;
import es.tamarit.widgetemt.api.searchstop.SearchStopImpl;
import es.tamarit.widgetemt.controllers.AbstractController;
import es.tamarit.widgetemt.core.ViewManager;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

public class SettingsController extends AbstractController {
    
    private static final Logger LOGGER = LogManager.getLogger(SettingsController.class);
    public static final String URL_VIEW = "/views/settings/SettingsView.fxml";
    
    private Properties properties;
    private SearchStop searchStop;
    
    @FXML
    private ComboBox<String> busStopCombo;
    @FXML
    private CheckBox alwaysOnTopCheck;
    @FXML
    private CheckBox autoRefreshCheck;
    @FXML
    private ToggleGroup languageGroup;
    @FXML
    private RadioButton spanishRadioButton;
    @FXML
    private RadioButton catalanRadioButton;
    @FXML
    private RadioButton englishRadioButton;
    
    @FXML
    public void initialize() {
        LOGGER.info("Settings");
        
        searchStop = new SearchStopImpl();
        
        try {
            properties = new Properties();
            InputStream inputStream = new FileInputStream(ViewManager.FILE_SETTINGS);
            properties.load(inputStream);
            inputStream.close();
        } catch (IOException e) {
            LOGGER.error("Error trying to load data from properties file.", e);
        }
        
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
    }
    
    @FXML
    private void openWidgetViewConfirm() {
        
        try {
            if (busStopCombo.getValue() != null && !busStopCombo.getValue().isEmpty()) {
                properties.setProperty("bus.stop.name", busStopCombo.getValue());
            }
            
            properties.setProperty("always.on.front", String.valueOf(alwaysOnTopCheck.isSelected()));
            properties.setProperty("auto.refresh.data", String.valueOf(autoRefreshCheck.isSelected()));
            properties.setProperty("application.language.locale", languageGroup.getSelectedToggle().getUserData().toString());
            
            OutputStream output = new FileOutputStream(ViewManager.FILE_SETTINGS);
            properties.store(output, null);
            output.close();
            
        } catch (FileNotFoundException e) {
            LOGGER.error("Error trying open the file.", e);
        } catch (IOException e) {
            LOGGER.error("Error trying to save the new data to the file.", e);
        }
        
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
                    List<String> namesFound = searchStop.getBusStopNames(text);
                    
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
