package es.tamarit.widgetemt.api.searchstop;

import java.io.IOException;
import java.util.List;

public interface SearchStop {
    
    /**
     * Gets the bus stop names that contains a part of the string passed as parameter.
     * @param sugerence - the string suggested by the user
     * @return a strings list of bus stop names found by the server
     * @throws IOException
     */
    public List<String> getBusStopNames(String sugerence) throws IOException;
}
