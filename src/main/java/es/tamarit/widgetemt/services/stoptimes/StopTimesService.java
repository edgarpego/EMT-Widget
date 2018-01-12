package es.tamarit.widgetemt.services.stoptimes;

import java.io.IOException;

import javafx.collections.ObservableList;

public interface StopTimesService {
    
    /**
     * Get a HTML response as a string with the bus stop times served by the EMT server
     * @return HTML response as string
     * @throws IOException
     */
    public ObservableList<LineTimeData> findByNameAndLineAndAdapted() throws IOException;
}
