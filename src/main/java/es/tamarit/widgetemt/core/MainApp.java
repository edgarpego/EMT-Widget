package es.tamarit.widgetemt.core;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {
	
	private static final Logger LOGGER = LogManager.getLogger(MainApp.class);
	private static final String TRY_ICON_16 = "/images/icon.png";
	
	private Stage primaryStage;
	private Stage secondaryStage;
	private Scene scene;
	
	private AnchorPane widgetView;
	
	private Properties properties;
	
	private TrayIcon trayIcon;
	
	@Override
	public void start(Stage primaryStage) {
		LOGGER.info("Application starting");
		
		try {
			// Add tryIcon
			createTrayIcon(primaryStage);
			
			this.primaryStage = primaryStage;
			this.secondaryStage = new Stage();
			
			primaryStage.setTitle("EMT Widget");
			primaryStage.initStyle(StageStyle.UTILITY);
			primaryStage.setOpacity(0);
			primaryStage.setWidth(1);
			primaryStage.setHeight(1);
			
			widgetView = FXMLLoader.load(MainApp.class.getResource("/views/widget/WidgetView.fxml"));
			scene = new Scene(widgetView);
			
			loadStyleSheets();
			loadDataFromProperties();
			setListeners();
			
			secondaryStage.setScene(scene);
			secondaryStage.initStyle(StageStyle.TRANSPARENT);
			secondaryStage.initOwner(primaryStage);
			
			primaryStage.show();
			secondaryStage.show();
			
		} catch (AWTException e) {
			LOGGER.error("Error trying to create the tryicon.", e);
		} catch (IOException e) {
			LOGGER.error("Error trying to load the WidgetView.", e);
		}
	}
	
	private void loadStyleSheets() {
		
		String[] styleSheets = {
				"/css/style.css",
		};
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
		widgetView.setOnMousePressed(new EventHandler<MouseEvent>() {
			
			@Override
			public void handle(MouseEvent mouseEvent) {
				// record a delta distance for the drag and drop operation.
				dragDelta.x = getSecondaryStage().getX() - mouseEvent.getScreenX();
				dragDelta.y = getSecondaryStage().getY() - mouseEvent.getScreenY();
				scene.setCursor(Cursor.MOVE);
			}
		});
		widgetView.setOnMouseReleased(new EventHandler<MouseEvent>() {
			
			@Override
			public void handle(MouseEvent mouseEvent) {
				scene.setCursor(Cursor.HAND);
				LOGGER.info("x: " + getPrimatyStage().getX() + " " + "y: " + getPrimatyStage().getY());
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
		widgetView.setOnMouseDragged(new EventHandler<MouseEvent>() {
			
			@Override
			public void handle(MouseEvent mouseEvent) {
				getSecondaryStage().setX(mouseEvent.getScreenX() + dragDelta.x);
				getSecondaryStage().setY(mouseEvent.getScreenY() + dragDelta.y);
			}
		});
		widgetView.setOnMouseEntered(new EventHandler<MouseEvent>() {
			
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (!mouseEvent.isPrimaryButtonDown()) {
					scene.setCursor(Cursor.HAND);
				}
			}
		});
		widgetView.setOnMouseExited(new EventHandler<MouseEvent>() {
			
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (!mouseEvent.isPrimaryButtonDown()) {
					scene.setCursor(Cursor.DEFAULT);
				}
			}
		});
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
					getSecondaryStage().close();
					getPrimatyStage().close();
					System.exit(0);
				});
			});
			
			popup.add(openItem);
			popup.addSeparator();
			popup.add(hideItem);
			popup.addSeparator();
			popup.add(exitItem);
			
			trayIcon = new TrayIcon(createImage(TRY_ICON_16, "Icon"), "EMT Widget", popup);
			
			/* Listener simple and double click on trayIcon */
			trayIcon.addMouseListener(new java.awt.event.MouseAdapter() {
				
				@Override
				public void mouseClicked(java.awt.event.MouseEvent evt) {
					Platform.runLater(() -> {
						if (evt.getClickCount() == 1) {
							// Simple
							stage.toFront();
						} else if (evt.getClickCount() == 2) {
							// Double
							if (stage.isIconified()) {
								// Maximize it
								stage.setIconified(false);
								stage.show();
								stage.toFront();
							} else {
								// Minimize it
								stage.setIconified(true);
							}
						}
					});
				}
				
			});
			
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
	
	public Stage getSecondaryStage() {
		return this.secondaryStage;
	}
	
	public static void main(String... varargs) {
		launch(varargs);
	}
}

// records relative x and y co-ordinates.
class Delta {
	
	double x, y;
}
