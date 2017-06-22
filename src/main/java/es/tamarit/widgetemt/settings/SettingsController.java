package es.tamarit.widgetemt.settings;

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

import es.tamarit.widgetemt.core.ViewManager;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class SettingsController {
	
	private static final Logger LOGGER = LogManager.getLogger(SettingsController.class);
	public static final String URL_VIEW = "/views/settings/SettingsView.fxml";
	
	private ViewManager viewManager;
	private String response;
	
	@FXML
	private TextField textField;
	@FXML
	private ComboBox<String> busStopCombo;
	
	@FXML
	public void initialize() {
		LOGGER.info("Settings");
		
		textField.textProperty().addListener((ChangeListener<String>) (arg0, arg1, arg2) -> textEditor(arg1, arg2));
	}
	
	@FXML
	private void openWidgetViewConfirm() {
		
		if (busStopCombo.getValue() == null) {
			openWidgetViewCancel();
		} else {
			if (busStopCombo.getValue().isEmpty()) {
				openWidgetViewCancel();
			} else {
				try {
					Properties properties = new Properties();
					InputStream inputStream = new FileInputStream(ViewManager.FILE_SETTINGS);
					properties.load(inputStream);
					inputStream.close();
					
					OutputStream output = new FileOutputStream(ViewManager.FILE_SETTINGS);
					properties.setProperty("bus.stop.name", busStopCombo.getValue());
					properties.store(output, null);
					output.close();
				} catch (FileNotFoundException e) {
					LOGGER.error("Error trying open the file.", e);
				} catch (IOException e) {
					LOGGER.error("Error trying to save the new data to the file.", e);
				}
				
				viewManager.loadWidgetView();
			}
		}
	}
	
	@FXML
	private void openWidgetViewCancel() {
		viewManager.loadWidgetView();
	}
	
	private void textEditor(String textBefore, String textAfter) {
		
		busStopCombo.getItems().clear();
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
						if (response != null) {
							// LOGGER.info(response);
							Document doc = Jsoup.parse(response);
							Elements elements = doc.getElementsByTag("li");
							for (Element element : elements) {
								// LOGGER.info(element.text());
								busStopCombo.getItems().add(element.text());
							}
							
							busStopCombo.setVisible(false);
							busStopCombo.hide();
							if (!busStopCombo.getItems().isEmpty()) {
								busStopCombo.setVisibleRowCount(busStopCombo.getItems().size());
								busStopCombo.setVisible(true);
								busStopCombo.show();
							}
						}
					});
				}
			}).start();
		} else {
			busStopCombo.hide();
			busStopCombo.setVisible(false);
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
	
	public void setViewManager(ViewManager viewManager) {
		this.viewManager = viewManager;
	}
}
