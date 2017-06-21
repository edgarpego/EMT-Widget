package es.tamarit.widgetemt.settings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SettingsController {
	
	private static final Logger LOGGER = LogManager.getLogger(SettingsController.class);
	
	@FXML
	private Button confirmButton;
	@FXML
	private Button cancelButton;
	
	@FXML
	public void initialize() {
		LOGGER.info("Settings");
	}
}
