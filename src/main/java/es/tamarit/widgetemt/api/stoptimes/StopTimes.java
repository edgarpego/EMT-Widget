package es.tamarit.widgetemt.api.stoptimes;

import java.io.IOException;

public interface StopTimes {
    
    /**
     * Get a HTML response as a string with the bus stop times served by the EMT server
     * @return HTML response as string
     * @throws IOException
     */
    public String getStopTimes() throws IOException;
}
