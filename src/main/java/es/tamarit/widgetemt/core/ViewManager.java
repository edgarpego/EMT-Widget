package es.tamarit.widgetemt.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.tamarit.widgetemt.controllers.settings.SettingsController;
import es.tamarit.widgetemt.controllers.updater.UpdaterController;
import es.tamarit.widgetemt.controllers.widget.WidgetController;
import es.tamarit.widgetemt.core.language.LanguageManager;
import es.tamarit.widgetemt.services.properties.FilePropertiesService;
import es.tamarit.widgetemt.services.properties.SettingsPropertiesServiceImpl;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ViewManager {

    private static final Logger LOGGER = LogManager.getLogger(ViewManager.class);
    private static final Integer PING_TIMEOUT = 3000;
    private static final String SERVER_URL = "www.google.es";

    private Stage primaryStage;
    private Stage secondaryStage;
    private Scene scene;
    private AnchorPane currentView;
    private FilePropertiesService properties;

    public ViewManager(Stage stage) {

        this.primaryStage = stage;
        this.secondaryStage = new Stage();

        primaryStage.setTitle("EMT Widget");
        primaryStage.initStyle(StageStyle.UTILITY);
        primaryStage.setOpacity(0);
        primaryStage.setWidth(1);
        primaryStage.setHeight(1);

        getResourceBundle();

        if (Boolean.valueOf(properties.getProperty("check.updates.automatically"))) {
            if (checkForUpdates()) {
                loadCheckForUpdatesView();
            } else {
                loadWidgetView();
            }
        } else {
            loadWidgetView();
        }

        //Check if the widget is in some active screen if it is not, the position will be reset.
        ObservableList<Screen> screens = Screen.getScreensForRectangle(Double.valueOf(properties.getProperty("widget.position.x")), Double.valueOf(properties.getProperty("widget.position.y")), 400, 130);
        if (!screens.isEmpty()) {
            primaryStage.setX(Double.valueOf(properties.getProperty("widget.position.x")));
            primaryStage.setY(Double.valueOf(properties.getProperty("widget.position.y")));
            secondaryStage.setX(Double.valueOf(properties.getProperty("widget.position.x")));
            secondaryStage.setY(Double.valueOf(properties.getProperty("widget.position.y")));
        } else {
            primaryStage.setX(50);
            primaryStage.setY(50);
            secondaryStage.setX(50);
            secondaryStage.setY(50);
            properties.setProperty("widget.position.x", "50.0");
            properties.setProperty("widget.position.y", "50.0");
            properties.store();
        }

        secondaryStage.initStyle(StageStyle.TRANSPARENT);
        secondaryStage.initOwner(primaryStage);

        primaryStage.show();
        secondaryStage.show();
    }

    public void loadWidgetView() {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setResources(getResourceBundle());
            currentView = fxmlLoader.load(getClass().getResource(WidgetController.URL_VIEW).openStream());
            WidgetController controller = (WidgetController) fxmlLoader.getController();
            controller.setViewManager(this);

            scene = new Scene(currentView);

            loadStyleSheets();

            secondaryStage.setScene(scene);

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

    private void loadCheckForUpdatesView() {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setResources(getResourceBundle());
            currentView = fxmlLoader.load(getClass().getResource(UpdaterController.URL_VIEW).openStream());
            UpdaterController controller = (UpdaterController) fxmlLoader.getController();
            controller.setViewManager(this);

            scene = new Scene(currentView);

            loadStyleSheets();

            secondaryStage.setScene(scene);

        } catch (IOException e) {
            LOGGER.error("Error trying to load the view.", e);
        }
    }

    private boolean checkForUpdates() {
        try {
            InetAddress address = InetAddress.getByName(SERVER_URL);

            if (address.isReachable(PING_TIMEOUT)) {
                LOGGER.info("Reachable server");
                return true;
            } else {
                LOGGER.info("Unreachable server");
                return false;
            }
        } catch (UnknownHostException e) {
            LOGGER.error("Error trying to contact with the server.", e);
        } catch (IOException e) {
            LOGGER.error("Error trying to reach the server.", e);
        }

        return false;
    }

    private void loadStyleSheets() {

        String[] styleSheets = {
                "/css/style.css",
                "/css/components.css",
                "https://fonts.googleapis.com/css?family=Lato"
        };
        scene.getStylesheets().addAll(styleSheets);
    }

    private ResourceBundle getResourceBundle() {
        try {
            properties = new SettingsPropertiesServiceImpl();

            Locale locale = Locale.forLanguageTag(properties.getProperty("application.language.locale"));
            LanguageManager languageManager = new LanguageManager(locale);

            return languageManager.getResourceBundle();

        } catch (IOException e) {
            LOGGER.error("Error trying to load the properties", e);
        }

        return null;
    }

    public Stage getSecondaryStage() {
        return this.secondaryStage;
    }
}