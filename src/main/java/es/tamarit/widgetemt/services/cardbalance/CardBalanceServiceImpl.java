package es.tamarit.widgetemt.services.cardbalance;

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

public class CardBalanceServiceImpl extends AbstractServerConnection implements CardBalanceService {
    
    private static final Logger LOGGER = LogManager.getLogger(CardBalanceServiceImpl.class);
    
    private URL url;
    private String language;
    
    public CardBalanceServiceImpl(String language) {
        
        try {
            this.language = language;
            this.url = new URL("https://www.emtvalencia.es/ciudadano/modules/mod_saldo/busca_saldo.php");
        } catch (MalformedURLException e) {
            LOGGER.error("Error creating the URL", e);
        }
    }
    
    @Override
    public String findByCardNumber(String cardNumber) throws IOException {
        
        Map<String, String> params = new LinkedHashMap<String, String>();
        
        params.put("numero", cardNumber);
        params.put("idioma", language);
        
        String response = getResponse(params, url);
        
        Document doc = Jsoup.parse(response);
        Elements elements = doc.getElementsByTag("strong");
        
        return elements.size() > 0 ? elements.get(0).text() : null;
    }
    
}
