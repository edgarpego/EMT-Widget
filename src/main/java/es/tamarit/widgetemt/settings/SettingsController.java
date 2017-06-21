package es.tamarit.widgetemt.settings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.tamarit.widgetemt.core.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public class SettingsController {
	
	private static final Logger LOGGER = LogManager.getLogger(SettingsController.class);
	public static final String URL_VIEW = "/views/settings/SettingsView.fxml";
	
	private ViewManager viewManager;
	
	@FXML
	private ComboBox busStopCombo;
	
	@FXML
	public void initialize() {
		LOGGER.info("Settings");
	}
	
	@FXML
	private void openWidgetViewConfirm() {
		viewManager.loadWidgetView();
	}
	
	@FXML
	private void openWidgetViewCancel() {
		viewManager.loadWidgetView();
	}
	
	public void setViewManager(ViewManager viewManager) {
		this.viewManager = viewManager;
	}
}
