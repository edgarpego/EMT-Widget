package es.tamarit.widgetemt.services.stoptimes;

import java.io.IOException;

public interface StopTimesService {
    
    /**
     * Get a HTML response as a string with the bus stop times served by the EMT server
     * @return HTML response as string
     * @throws IOException
     */
    public String findByNameAndLineAndAdapted() throws IOException;
}
