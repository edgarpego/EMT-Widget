package es.tamarit.widgetemt.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class MainApp extends Application {
	
	private static final Logger LOGGER = LogManager.getLogger(MainApp.class.getName());
	
	private Stage primaryStage;
	private BorderPane rootLayout;
	private Scene scene;
	private Properties properties;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		LOGGER.info("Application starting");
		
		this.primaryStage = primaryStage;
		
		primaryStage.setTitle("EMT Widget");
		primaryStage.initStyle(StageStyle.TRANSPARENT);
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				getPrimatyStage().close();
				System.exit(0);
			}
		});
		
		initRootLayout();
		initMainScene();
		initMainView();
		
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	private void initRootLayout() {
		try {
			rootLayout = FXMLLoader.load(MainApp.class.getResource("/views/rootlayout/RootLayout.fxml"));
		} catch (IOException e) {
			LOGGER.error("Error loading the rootLayout", e);
		}
	}
	
	private void initMainScene() {
		
		scene = new Scene(rootLayout);
		
		String[] styleSheets = {
				"/css/style.css",
		};
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
			
			final Delta dragDelta = new Delta();
			rootLayout.setOnMousePressed(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent mouseEvent) {
					// record a delta distance for the drag and drop operation.
					dragDelta.x = getPrimatyStage().getX() - mouseEvent.getScreenX();
					dragDelta.y = getPrimatyStage().getY() - mouseEvent.getScreenY();
					scene.setCursor(Cursor.MOVE);
				}
			});
			rootLayout.setOnMouseReleased(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent mouseEvent) {
					scene.setCursor(Cursor.HAND);
					// LOGGER.info("x: " + getPrimatyStage().getX() + " " + "y: " + getPrimatyStage().getY());
				}
			});
			rootLayout.setOnMouseDragged(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent mouseEvent) {
					getPrimatyStage().setX(mouseEvent.getScreenX() + dragDelta.x);
					getPrimatyStage().setY(mouseEvent.getScreenY() + dragDelta.y);
				}
			});
			rootLayout.setOnMouseEntered(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent mouseEvent) {
					if (!mouseEvent.isPrimaryButtonDown()) {
						scene.setCursor(Cursor.HAND);
					}
				}
			});
			rootLayout.setOnMouseExited(new EventHandler<MouseEvent>() {
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
	
	public static void main(String... varargs) {
		launch(varargs);
	}
	
	public Stage getPrimatyStage() {
		return this.primaryStage;
	}
}

// records relative x and y co-ordinates.
class Delta {
	double x, y;
}
