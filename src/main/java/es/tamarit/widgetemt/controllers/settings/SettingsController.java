package es.tamarit.widgetemt.controllers.settings;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import es.tamarit.widgetemt.controllers.AbstractController;
import es.tamarit.widgetemt.core.ViewManager;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

public class SettingsController extends AbstractController {
    
    private static final Logger LOGGER = LogManager.getLogger(SettingsController.class);
    public static final String URL_VIEW = "/views/settings/SettingsView.fxml";
    
    private String response;
    private Properties properties;
    
    @FXML
    private ComboBox<String> busStopCombo;
    @FXML
    private CheckBox alwaysOnTopCheck;
    @FXML
    private CheckBox autoRefreshCheck;
    @FXML
    private ToggleGroup languageGroup;
    @FXML
    private RadioButton spanishRadioButton;
    @FXML
    private RadioButton catalanRadioButton;
    @FXML
    private RadioButton englishRadioButton;
    
    @FXML
    public void initialize() {
        LOGGER.info("Settings");
        
        try {
            properties = new Properties();
            InputStream inputStream = new FileInputStream(ViewManager.FILE_SETTINGS);
            properties.load(inputStream);
            inputStream.close();
        } catch (IOException e) {
            LOGGER.error("Error trying to load data from properties file.", e);
        }
        
        busStopCombo.getEditor().textProperty().addListener((ChangeListener<String>) (arg0, arg1, arg2) -> textEditor(arg1, arg2));
        alwaysOnTopCheck.setSelected(Boolean.valueOf(properties.getProperty("always.on.front")));
        autoRefreshCheck.setSelected(Boolean.valueOf(properties.getProperty("auto.refresh.data")));
        
        spanishRadioButton.setUserData("es-ES");
        catalanRadioButton.setUserData("ca-ES");
        englishRadioButton.setUserData("en-EN");
        
        String locale = properties.getProperty("application.language.locale");
        switch (locale) {
            case "es-ES":
                spanishRadioButton.setSelected(true);
                break;
            case "ca-ES":
                catalanRadioButton.setSelected(true);
                break;
            case "en-EN":
                englishRadioButton.setSelected(true);
                break;
        }
    }
    
    @FXML
    private void openWidgetViewConfirm() {
        
        try {
            if (busStopCombo.getValue() != null && !busStopCombo.getValue().isEmpty()) {
                properties.setProperty("bus.stop.name", busStopCombo.getValue());
            }
            
            properties.setProperty("always.on.front", String.valueOf(alwaysOnTopCheck.isSelected()));
            properties.setProperty("auto.refresh.data", String.valueOf(autoRefreshCheck.isSelected()));
            properties.setProperty("application.language.locale", languageGroup.getSelectedToggle().getUserData().toString());
            
            OutputStream output = new FileOutputStream(ViewManager.FILE_SETTINGS);
            properties.store(output, null);
            output.close();
            
        } catch (FileNotFoundException e) {
            LOGGER.error("Error trying open the file.", e);
        } catch (IOException e) {
            LOGGER.error("Error trying to save the new data to the file.", e);
        }
        
        viewManager.loadWidgetView();
    }
    
    @FXML
    private void openWidgetViewCancel() {
        viewManager.loadWidgetView();
    }
    
    private void textEditor(String textBefore, String textAfter) {
        
        String text = textAfter;
        
        if (!text.isEmpty()) {
            
            new Thread(() -> {
                try {
                    if (Character.isDigit(text.charAt(0))) {
                        response = getResponse("id_parada", text);
                        
                    } else if (Character.isLetter(text.charAt(0))) {
                        response = getResponse("parada", text);
                    }
                    
                } catch (IOException e) {
                    LOGGER.error("Error trying to get the response from the EMT server", e);
                } finally {
                    
                    Platform.runLater(() -> {
                        if (response != null && !response.isEmpty()) {
                            // LOGGER.info(response);
                            busStopCombo.getItems().clear();
                            busStopCombo.hide();
                            
                            Document doc = Jsoup.parse(response);
                            Elements elements = doc.getElementsByTag("li");
                            for (Element element : elements) {
                                // LOGGER.info(element.text());
                                busStopCombo.getItems().add(element.text());
                            }
                            
                            if (!busStopCombo.getItems().isEmpty()) {
                                busStopCombo.setVisibleRowCount(busStopCombo.getItems().size());
                                busStopCombo.show();
                            }
                        }
                    });
                }
            }).start();
        } else {
            busStopCombo.getItems().clear();
            busStopCombo.hide();
        }
    }
    
    private String getResponse(String tag, String stopName) throws IOException {
        URL url = new URL("https://www.emtvalencia.es/ciudadano/modules/mod_tiempo/sugiere_parada.php"); // URL to your application
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put(tag, stopName); // All parameters, also easy
        
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
}