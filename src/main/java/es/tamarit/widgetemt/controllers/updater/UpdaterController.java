package es.tamarit.widgetemt.controllers.updater;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.tamarit.widgetemt.controllers.AbstractController;
import es.tamarit.widgetemt.services.updater.Updater;
import es.tamarit.widgetemt.services.updater.UpdaterImpl;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class UpdaterController extends AbstractController {
    
    private static final Logger LOGGER = LogManager.getLogger(UpdaterController.class);
    public static final String URL_VIEW = "/views/updater/UpdaterView.fxml";
    
    @FXML
    private CheckBox remainderCheckbox;
    @FXML
    private WebView webView;
    
    private WebEngine webEngine;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            
            webView.setContextMenuEnabled(false);
            webEngine = webView.getEngine();
            webEngine.setUserStyleSheetLocation(getClass().getResource("/css/webReleases.css").toString());
            
            Updater updater = new UpdaterImpl();
            webEngine.loadContent(updater.getWhatsNew());
            
        } catch (Exception e) {
            LOGGER.error("Error trying to get the release notes.", e);
        }
        
    }
    
    @FXML
    private void openWidgetViewCancel() {
        viewManager.loadWidgetView();
    }
    
    @FXML
    private void openWidgetViewUpdate() {
        
        try {
            Desktop.getDesktop().browse(new URI("http://www.emt-widget.edgartamarit.com"));
            
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("Error trying to open the system web browser", e);
        }
        
        viewManager.loadWidgetView();
    }
}
