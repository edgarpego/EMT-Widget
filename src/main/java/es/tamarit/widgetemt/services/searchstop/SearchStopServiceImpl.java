package es.tamarit.widgetemt.services.searchstop;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import es.tamarit.widgetemt.services.AbstractServerConnection;

public class SearchStopServiceImpl extends AbstractServerConnection implements SearchStopService {
    
    private static final Logger LOGGER = LogManager.getLogger(SearchStopServiceImpl.class);
    
    private URL url;
    
    public SearchStopServiceImpl() {
        
        try {
            this.url = new URL("https://www.emtvalencia.es/ciudadano/modules/mod_tiempo/sugiere_parada.php");
        } catch (MalformedURLException e) {
            LOGGER.error("Error creating the URL", e);
        }
    }
    
    @Override
    public List<String> findAll(String sugerence) throws IOException {
        
        Map<String, String> params = new LinkedHashMap<String, String>();
        
        if (Character.isDigit(sugerence.charAt(0))) {
            params.put("id_parada", sugerence);
        } else if (Character.isLetter(sugerence.charAt(0))) {
            params.put("parada", sugerence);
        }
        
        String response = getResponse(params, url);
        
        Document doc = Jsoup.parse(response);
        Elements elements = doc.getElementsByTag("li");
        
        List<String> namesFound = new ArrayList<String>();
        elements.forEach((e) -> namesFound.add(e.text()));
        
        return namesFound;
    }
    
}
