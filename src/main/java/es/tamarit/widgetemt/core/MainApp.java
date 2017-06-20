package es.tamarit.widgetemt.core;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.swing.ImageIcon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {

    private static final Logger LOGGER = LogManager.getLogger(MainApp.class.getName());
    public static final String URL_IMG_APP_16 = "/images/icon.png";

    private Stage primaryStage;
    private Stage secondaryStage;
    private BorderPane rootLayout;
    private Scene scene;
    private Properties properties;
    private TrayIcon trayIcon;

    @Override
    public void start(Stage primaryStage) throws Exception {
        LOGGER.info("Application starting");

        //Add tryIcon
        createTrayIcon(primaryStage);

        this.primaryStage = primaryStage;
        this.secondaryStage = new Stage();

        primaryStage.setTitle("EMT Widget");
        primaryStage.initStyle(StageStyle.UTILITY);
        primaryStage.setOpacity(0);
        primaryStage.setWidth(1);
        primaryStage.setHeight(1);

        initRootLayout();

        scene = new Scene(rootLayout);

        loadStyleSheets();
        initMainView();

        secondaryStage.setScene(scene);
        secondaryStage.initStyle(StageStyle.TRANSPARENT);
        secondaryStage.initOwner(primaryStage);

        primaryStage.show();
        secondaryStage.show();
    }

    private void initRootLayout() {
        try {
            rootLayout = FXMLLoader.load(MainApp.class.getResource("/views/rootlayout/RootLayout.fxml"));
        } catch (IOException e) {
            LOGGER.error("Error loading the rootLayout", e);
        }
    }

    private void loadStyleSheets() {

        String[] styleSheets = { "/css/style.css", };
        scene.getStylesheets().addAll(styleSheets);
    }

    private void initMainView() {
        try {
            AnchorPane mainView = FXMLLoader.load(MainApp.class.getResource("/views/widget/WidgetView.fxml"));
            rootLayout.setCenter(mainView);

            properties = new Properties();
            InputStream inputStream = getClass().getResourceAsStream("/properties/application.properties");
            properties.load(inputStream);
            inputStream.close();

            getPrimatyStage().setX(Integer.valueOf(properties.getProperty("widget.position.x")));
            getPrimatyStage().setY(Integer.valueOf(properties.getProperty("widget.position.y")));
            secondaryStage.setX(Integer.valueOf(properties.getProperty("widget.position.x")));
            secondaryStage.setY(Integer.valueOf(properties.getProperty("widget.position.y")));

            final Delta dragDelta = new Delta();
            rootLayout.setOnMousePressed(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent mouseEvent) {
                    // record a delta distance for the drag and drop operation.
                    dragDelta.x = secondaryStage.getX() - mouseEvent.getScreenX();
                    dragDelta.y = secondaryStage.getY() - mouseEvent.getScreenY();
                    scene.setCursor(Cursor.MOVE);
                }
            });
            rootLayout.setOnMouseReleased(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent mouseEvent) {
                    scene.setCursor(Cursor.HAND);
                    // LOGGER.info("x: " + getPrimatyStage().getX() + " " + "y: " + getPrimatyStage().getY());
                }
            });
            rootLayout.setOnMouseDragged(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent mouseEvent) {
                    secondaryStage.setX(mouseEvent.getScreenX() + dragDelta.x);
                    secondaryStage.setY(mouseEvent.getScreenY() + dragDelta.y);
                }
            });
            rootLayout.setOnMouseEntered(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (!mouseEvent.isPrimaryButtonDown()) {
                        scene.setCursor(Cursor.HAND);
                    }
                }
            });
            rootLayout.setOnMouseExited(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (!mouseEvent.isPrimaryButtonDown()) {
                        scene.setCursor(Cursor.DEFAULT);
                    }
                }
            });

        } catch (IOException e) {
            LOGGER.error("Error loading the grid view", e);
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
                    secondaryStage.close();
                    getPrimatyStage().close();
                });

                System.exit(0);
            });

            popup.add(openItem);
            popup.addSeparator();
            popup.add(hideItem);
            popup.addSeparator();
            popup.add(exitItem);

            trayIcon = new TrayIcon(createImage(URL_IMG_APP_16, "Icon"), "EMT Widget", popup);

            /*Listener double click on trayIcon*/
            trayIcon.addActionListener(event -> Platform.runLater(() -> {

                if (stage.isIconified()) {
                    stage.setIconified(false);
                    stage.show();
                    stage.toFront();
                } else {
                    stage.setIconified(true);
                }
            }));

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

    public Stage getPrimatyStage() {
        return this.primaryStage;
    }

    public static void main(String... varargs) {
        launch(varargs);
    }
}

// records relative x and y co-ordinates.
class Delta {

    double x, y;
}
