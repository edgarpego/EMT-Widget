package es.tamarit.widgetemt.services.stoptimes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import es.tamarit.widgetemt.services.AbstractServerConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class StopTimesServiceImpl extends AbstractServerConnection implements StopTimesService {
    
    private static final Logger LOGGER = LogManager.getLogger(StopTimesServiceImpl.class);
    
    private URL url;
    private String stopName;
    private String lineFilter;
    private String adapted;
    private String language;
    
    public StopTimesServiceImpl(String stopName, String lineFilter, String adapted, String language) {
        
        this.stopName = stopName;
        this.lineFilter = lineFilter;
        this.adapted = adapted;
        this.language = language;
        
        try {
            this.url = new URL("https://www.emtvalencia.es/ciudadano/modules/mod_tiempo/busca_parada.php");
        } catch (MalformedURLException e) {
            LOGGER.error("Error creating the URL", e);
        }
    }
    
    @Override
    public ObservableList<LineTimeData> findByNameAndLineAndAdapted() throws IOException {
        
        Map<String, String> params = new LinkedHashMap<String, String>();
        
        params.put("parada", stopName);
        if (!lineFilter.isEmpty()) {
            params.put("linea", lineFilter);
        }
        params.put("adaptados", adapted);
        params.put("usuario", "anonimo");
        params.put("idioma", language);
        
        String response = getResponse(params, url);
        
        if (response == null || response.isEmpty() || response.contains("No disponible") || response.contains("Out of service")) {
            return null;
        } else {
            
            ObservableList<LineTimeData> linesFound = FXCollections.observableArrayList();
            
            Document doc = Jsoup.parse(response);
            Elements imgsClass = doc.getElementsByTag("img");
            Elements nombresClass = doc.getElementsByClass("llegadaHome");
            
            for (int i = 0; i < imgsClass.size(); i++) {
                LineTimeData line = new LineTimeData();
                line.setLineImgURL(imgsClass.get(i).attr("src"));
                if (nombresClass.get(i).text().contains(" - ")) {
                    line.setLineName(nombresClass.get(i).text().split(" - ")[0].trim());
                    line.setLineTime(nombresClass.get(i).text().split(" - ")[1].trim());
                } else {
                    line.setLineImgURL(null);
                    line.setLineName(nombresClass.get(i).text());
                }
                linesFound.add(line);
            }
            
            return linesFound.isEmpty() ? null : linesFound;
        }
    }
}
