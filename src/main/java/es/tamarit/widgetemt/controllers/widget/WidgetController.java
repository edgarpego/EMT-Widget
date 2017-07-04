package es.tamarit.widgetemt.controllers.widget;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.tamarit.widgetemt.controllers.AbstractController;
import es.tamarit.widgetemt.core.ViewManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class WidgetController extends AbstractController {
    
    private static final Logger LOGGER = LogManager.getLogger(WidgetController.class);
    public static final String URL_VIEW = "/views/widget/WidgetView.fxml";
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    @FXML
    private WebView myWebView;
    @FXML
    private ProgressIndicator progress;
    @FXML
    private Button refreshButton;
    
    private WebEngine myWebEngine;
    private Properties properties;
    private String response;
    
    @FXML
    public void initialize() {
        try {
            myWebView.setContextMenuEnabled(false);
            myWebEngine = myWebView.getEngine();
            myWebEngine.setUserStyleSheetLocation(getClass().getResource("/css/webstyle.css").toString());
            
            properties = new Properties();
            InputStream inputStream = new FileInputStream(ViewManager.FILE_SETTINGS);
            properties.load(inputStream);
            inputStream.close();
            
            if (Boolean.valueOf(properties.get("auto.refresh.data").toString()) == Boolean.TRUE) {
                scheduler.scheduleAtFixedRate(() -> printTimes(), 0, 30, TimeUnit.SECONDS);
            } else {
                printTimes();
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Error trying to get the properties file", e);
        } catch (IOException e) {
            LOGGER.error("Error trying to load the properties", e);
        }
    }
    
    @FXML
    private void reloadHandler() {
        printTimes();
    }
    
    private void printTimes() {
        LOGGER.info("Printing the times");
        
        Platform.runLater(() -> {
            progress.setVisible(true);
            refreshButton.setDisable(true);
            myWebEngine.loadContent("");
        });
        
        new Thread(() -> {
            try {
                String stopName = properties.getProperty("bus.stop.name");
                Locale locale = Locale.forLanguageTag(properties.getProperty("application.language.locale"));
                String language = locale.getLanguage();
                
                response = getResponse(stopName, language);
                
            } catch (IOException e) {
                LOGGER.error("Error trying to get the response from the EMT server", e);
            } finally {
                
                Platform.runLater(() -> {
                    if (response != null && !response.isEmpty()) {
                        if (response.contains("No disponible. Actualiza en unos segundos.")) {
                            printTimes();
                        } else {
                            myWebEngine.loadContent("<html><body>" + response + "</body></html>");
                            refreshButton.setDisable(false);
                            progress.setVisible(false);
                        }
                    } else {
                        myWebEngine.loadContent("<html><body><h3>There was an error, Try again in a few seconds!</h3></body></html>");
                        refreshButton.setDisable(false);
                        progress.setVisible(false);
                    }
                });
            }
        }).start();
    }
    
    private String getResponse(String stopName, String language) throws IOException {
        URL url = new URL("https://www.emtvalencia.es/ciudadano/modules/mod_tiempo/busca_parada.php"); // URL to your application
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("parada", stopName); // All parameters, also easy
        params.put("adaptados", "0");
        params.put("usuario", "Anonimo");
        params.put("idioma", language);
        
        StringBuilder postData = new StringBuilder();
        // POST as URL encoded is basically key-value pairs, as with GET
        // This creates key=value&key=value&... pairs
        for (Map.Entry<String, String> param : params.entrySet()) {
            if (postData.length() != 0)
                postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        
        // Convert string to byte array, as it should be sent
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");
        
        // Connect, easy
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        // Tell server that this is POST and in which format is the data
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);
        
        // This gets the output from your server
        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        
        StringBuilder sb = new StringBuilder();
        for (int c; (c = in.read()) >= 0;) {
            sb.append((char) c);
            // System.out.print((char) c);
        }
        
        in.close();
        
        return sb.toString();
    }
    
    @FXML
    private void openSettingsWindow() {
        scheduler.shutdown();
        viewManager.loadSettingsView();
    }
}
