package es.tamarit.widgetemt.api.stoptimes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.tamarit.widgetemt.api.AbstractServerConnection;

public class StopTimesImpl extends AbstractServerConnection implements StopTimes {
    
    private static final Logger LOGGER = LogManager.getLogger(StopTimesImpl.class);
    
    private URL url;
    private String stopName;
    private String lineFilter;
    private String adapted;
    private String language;
    
    public StopTimesImpl(String stopName, String lineFilter, String adapted, String language) {
        
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
    public String getStopTimes() throws IOException {
        
        Map<String, String> params = new LinkedHashMap<String, String>();
        
        params.put("parada", stopName);
        if (!lineFilter.equals("none")) {
            params.put("linea", lineFilter);
        }
        params.put("adaptados", adapted);
        params.put("usuario", "Anonimo");
        params.put("idioma", language);
        
        return getResponse(params, url);
    }
}