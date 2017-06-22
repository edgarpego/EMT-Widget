package es.tamarit.widgetemt.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.tamarit.widgetemt.settings.SettingsController;
import es.tamarit.widgetemt.widget.WidgetController;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ViewManager {

    private static final Logger LOGGER = LogManager.getLogger(ViewManager.class);

    private Properties properties;

    private Stage primaryStage;
    private Stage secondaryStage;
    private Scene scene;

    private AnchorPane currentView;

    public ViewManager(Stage stage) {

        try {
            this.primaryStage = stage;
            this.secondaryStage = new Stage();

            primaryStage.setTitle("EMT Widget");
            primaryStage.initStyle(StageStyle.UTILITY);
            primaryStage.setOpacity(0);
            primaryStage.setWidth(1);
            primaryStage.setHeight(1);

            FXMLLoader fxmlLoader = new FXMLLoader();
            currentView = fxmlLoader.load(getClass().getResource(WidgetController.URL_VIEW).openStream());
            WidgetController controller = (WidgetController) fxmlLoader.getController();
            controller.setViewManager(this);

            scene = new Scene(currentView);

            loadStyleSheets();
            loadDataFromProperties();
            setListeners();

            secondaryStage.setScene(scene);
            secondaryStage.initStyle(StageStyle.TRANSPARENT);
            secondaryStage.initOwner(primaryStage);

            loadWidgetView();

            primaryStage.show();
            secondaryStage.show();
        } catch (

        IOException e) {
            LOGGER.error("Error trying to load the WidgetView.", e);
        }
    }

    public void loadWidgetView() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
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

        String[] styleSheets = { "/css/style.css", };
        scene.getStylesheets().addAll(styleSheets);
    }

    private void loadDataFromProperties() {

        try {
            properties = new Properties();
            InputStream inputStream = new FileInputStream("application.properties");
            properties.load(inputStream);
            inputStream.close();

            getPrimatyStage().setX(Double.valueOf(properties.getProperty("widget.position.x")));
            getPrimatyStage().setY(Double.valueOf(properties.getProperty("widget.position.y")));
            getSecondaryStage().setX(Double.valueOf(properties.getProperty("widget.position.x")));
            getSecondaryStage().setY(Double.valueOf(properties.getProperty("widget.position.y")));
        } catch (IOException e) {
            LOGGER.error("Error trying to load data from properties file.", e);
        }
    }

    private void setListeners() {

        final Delta dragDelta = new Delta();
        currentView.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent mouseEvent) {
                // record a delta distance for the drag and drop operation.
                dragDelta.x = getSecondaryStage().getX() - mouseEvent.getScreenX();
                dragDelta.y = getSecondaryStage().getY() - mouseEvent.getScreenY();
                scene.setCursor(Cursor.MOVE);
            }
        });
        currentView.setOnMouseReleased(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent mouseEvent) {
                scene.setCursor(Cursor.HAND);
                //LOGGER.info("x: " + getPrimatyStage().getX() + " " + "y: " + getPrimatyStage().getY());
                try {
                    OutputStream output = new FileOutputStream("application.properties");
                    properties.setProperty("widget.position.x", String.valueOf(getSecondaryStage().getX()));
                    properties.setProperty("widget.position.y", String.valueOf(getSecondaryStage().getY()));
                    properties.store(output, null);
                    output.close();
                } catch (FileNotFoundException e) {
                    LOGGER.error("Error trying open the file.", e);
                } catch (IOException e) {
                    LOGGER.error("Error trying to save the new data to the file.", e);
                }
            }
        });
        currentView.setOnMouseDragged(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent mouseEvent) {
                getSecondaryStage().setX(mouseEvent.getScreenX() + dragDelta.x);
                getSecondaryStage().setY(mouseEvent.getScreenY() + dragDelta.y);
            }
        });
        currentView.setOnMouseEntered(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent mouseEvent) {
                if (!mouseEvent.isPrimaryButtonDown()) {
                    scene.setCursor(Cursor.HAND);
                }
            }
        });
        currentView.setOnMouseExited(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent mouseEvent) {
                if (!mouseEvent.isPrimaryButtonDown()) {
                    scene.setCursor(Cursor.DEFAULT);
                }
            }
        });
    }

    private Stage getPrimatyStage() {
        return this.primaryStage;
    }

    private Stage getSecondaryStage() {
        return this.secondaryStage;
    }
}

// records relative x and y co-ordinates.
class Delta {

    double x, y;
}
