package es.tamarit.widgetemt.services.searchstop;

import java.io.IOException;
import java.util.List;

public interface SearchStopService {
    
    /**
     * Gets the bus stop names that contains a part of the string passed as parameter.
     * @param sugerence - the string suggested by the user
     * @return a strings list of bus stop names found by the server
     * @throws IOException
     */
    public List<String> findAll(String sugerence) throws IOException;
}
