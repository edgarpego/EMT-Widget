package es.tamarit.widgetemt.services.favourites;

import es.tamarit.widgetemt.services.properties.FilePropertiesService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class FavouritesServiceImpl implements FavouritesService {
    
    private FilePropertiesService filePropertiesService;
    
    public FavouritesServiceImpl(FilePropertiesService filePropertiesService) {
        this.filePropertiesService = filePropertiesService;
    }
    
    @Override
    public ObservableList<Favourite> getAllFavourites() {
        
        ObservableList<Favourite> favouriteList = FXCollections.observableArrayList();
        
        String favouritesRAW = filePropertiesService.getProperty("bus.stop.favourites");
        String[] busStopsRAW = favouritesRAW.split(";");
        
        for (String busStopRAW : busStopsRAW) {
            String[] params = busStopRAW.split(":");
            
            Favourite busStop = new Favourite();
            
            switch (params.length) {
                case 3:
                    busStop.setAdapted(params[2]);
                case 2:
                    busStop.setLineFilter(params[1]);
                case 1:
                    busStop.setStopName(params[0]);
                    break;
            }
            
            favouriteList.add(busStop);
        }
        
        return favouriteList;
    }
    
    @Override
    public void setAllFavourites(ObservableList<Favourite> favourites) {
        
        StringBuilder value = new StringBuilder("");
        
        for (Favourite favourite : favourites) {
            value.append(favourite.getStopName() + ":" + favourite.getLineFilter() + ":" + favourite.getAdapted() + ";");
        }
        
        filePropertiesService.setProperty("bus.stop.favourites", value.toString());
    }
    
}
