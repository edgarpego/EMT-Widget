package es.tamarit.widgetemt.widget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class WidgetController {

    private static final Logger LOGGER = LogManager.getLogger(WidgetController.class);

    @FXML
    private WebView myWebView;

    private WebEngine myWebEngine;
    private Properties properties;

    @FXML
    public void initialize() throws IOException {
        myWebEngine = myWebView.getEngine();

        properties = new Properties();
        InputStream inputStream = getClass().getResourceAsStream("/properties/application.properties");
        properties.load(inputStream);
        inputStream.close();

        printTimes();
    }

    @FXML
    private void reloadHandler() throws IOException {
        printTimes();
    }

    private void printTimes() throws IOException {
        LOGGER.info("Printing the times");

        String stopName = properties.getProperty("bus.stop.name");
        myWebEngine.loadContent("<html><body>" + getResponse(stopName) + "</body></html>");
    }

    private String getResponse(String stopName) throws IOException {
        URL url = new URL("https://www.emtvalencia.es/ciudadano/modules/mod_tiempo/busca_parada.php"); // URL to your application
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("parada", stopName); // All parameters, also easy
        params.put("adaptados", "0");
        params.put("usuario", "Anonimo");
        params.put("idioma", "es");

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
