package es.tamarit.widgetemt.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.tamarit.widgetemt.controllers.settings.SettingsController;
import es.tamarit.widgetemt.controllers.widget.WidgetController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ViewManager {

    private static final Logger LOGGER = LogManager.getLogger(ViewManager.class);
    public static final String FILE_SETTINGS = System.getProperty("user.home") + File.separator + ".EMTWidget" + File.separator + "settings.emt";

    private Properties properties;

    private Stage primaryStage;
    private Stage secondaryStage;
    private Scene scene;
    private AnchorPane currentView;

    public ViewManager(Stage stage) {

        this.primaryStage = stage;
        this.secondaryStage = new Stage();

        primaryStage.setTitle("EMT Widget");
        primaryStage.initStyle(StageStyle.UTILITY);
        primaryStage.setOpacity(0);
        primaryStage.setWidth(1);
        primaryStage.setHeight(1);

        loadWidgetView();

        primaryStage.setX(Double.valueOf(properties.getProperty("widget.position.x")));
        primaryStage.setY(Double.valueOf(properties.getProperty("widget.position.y")));
        secondaryStage.setX(Double.valueOf(properties.getProperty("widget.position.x")));
        secondaryStage.setY(Double.valueOf(properties.getProperty("widget.position.y")));
        secondaryStage.initStyle(StageStyle.TRANSPARENT);
        secondaryStage.initOwner(primaryStage);

        primaryStage.show();
        secondaryStage.show();
    }

    public void loadWidgetView() {
        try {
            loadDataFromProperties();

            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setResources(getResourceBundle());
            currentView = fxmlLoader.load(getClass().getResource(WidgetController.URL_VIEW).openStream());
            WidgetController controller = (WidgetController) fxmlLoader.getController();
            controller.setViewManager(this);

            scene = new Scene(currentView);

            loadStyleSheets();

            secondaryStage.setScene(scene);
            secondaryStage.setAlwaysOnTop(Boolean.valueOf(properties.get("always.on.front").toString()));

        } catch (IOException e) {
            LOGGER.error("Error trying to load the view.", e);
        }
    }

    public void loadSettingsView() {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setResources(getResourceBundle());
            currentView = fxmlLoader.load(getClass().getResource(SettingsController.URL_VIEW).openStream());
            SettingsController controller = (SettingsController) fxmlLoader.getController();
            controller.setViewManager(this);

            scene = new Scene(currentView);

            loadStyleSheets();

            secondaryStage.setScene(scene);

        } catch (IOException e) {
            LOGGER.error("Error trying to load the view.", e);
        }
    }

    private void loadStyleSheets() {

        String[] styleSheets = {
                "/css/style.css",
                "/css/components.css",
                "https://fonts.googleapis.com/css?family=Lato"
        };
        scene.getStylesheets().addAll(styleSheets);
    }

    private void loadDataFromProperties() {

        try {
            newFileIfNotExists();

            properties = new Properties();
            InputStream inputStream = new FileInputStream(FILE_SETTINGS);
            properties.load(inputStream);
            inputStream.close();

            checkPropertiesIntegrity();

        } catch (IOException e) {
            LOGGER.error("Error trying to load data from properties file.", e);
        }
    }

    private void checkPropertiesIntegrity() {

        try {
            boolean neededRestore = false;

            if (properties.get("widget.position.x") == null) {
                properties.setProperty("widget.position.x", "50");
                neededRestore = true;
            }
            if (properties.get("widget.position.y") == null) {
                properties.setProperty("widget.position.y", "50");
                neededRestore = true;
            }
            if (properties.get("bus.stop.name") == null) {
                properties.setProperty("bus.stop.name", "824");
                neededRestore = true;
            }
            if (properties.get("always.on.front") == null) {
                properties.setProperty("always.on.front", "false");
                neededRestore = true;
            }
            if (properties.get("auto.refresh.data") == null) {
                properties.setProperty("auto.refresh.data", "false");
                neededRestore = true;
            }
            if (properties.get("application.language.locale") == null) {
                properties.setProperty("application.language.locale", "es-ES");
                neededRestore = true;
            }

            if (neededRestore) {
                OutputStream output = new FileOutputStream(ViewManager.FILE_SETTINGS);
                properties.store(output, null);
                output.close();
            }

        } catch (FileNotFoundException e) {
            LOGGER.error("Error trying open the file.", e);
        } catch (IOException e) {
            LOGGER.error("Error trying to save the new data to the file.", e);
        }
    }

    private void newFileIfNotExists() {
        try {
            File file = new File(FILE_SETTINGS);

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                PrintWriter writer = new PrintWriter(file);
                writer.println("widget.position.x=50");
                writer.println("widget.position.y=50");
                writer.println("bus.stop.name=824");
                writer.println("always.on.front=false");
                writer.println("auto.refresh.data=false");
                writer.println("application.language.locale=es-ES");
                writer.close();
            }
        } catch (IOException e) {
            LOGGER.error("Error trying to create the settings file.", e);
        }
    }

    private ResourceBundle getResourceBundle() {

        Locale locale = Locale.forLanguageTag(properties.getProperty("application.language.locale"));
        LanguageManager languageManager = new LanguageManager(locale);

        return languageManager.getResourceBundle();
    }

    public Stage getSecondaryStage() {
        return this.secondaryStage;
    }
}