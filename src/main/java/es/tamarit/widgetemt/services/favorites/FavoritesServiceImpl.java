package es.tamarit.widgetemt.services.favorites;

import es.tamarit.widgetemt.services.properties.FilePropertiesService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class FavoritesServiceImpl implements FavoritesService {
    
    private FilePropertiesService filePropertiesService;
    
    public FavoritesServiceImpl(FilePropertiesService filePropertiesService) {
        this.filePropertiesService = filePropertiesService;
    }
    
    @Override
    public ObservableList<Favorite> getAllFavorites() {
        
        ObservableList<Favorite> favoriteList = FXCollections.observableArrayList();
        
        String favoritesRAW = filePropertiesService.getProperty("bus.stop.favorites");
        String[] busStopsRAW = favoritesRAW.split(";");
        
        if (!busStopsRAW[0].isEmpty()) {
            
            for (String busStopRAW : busStopsRAW) {
                String[] params = busStopRAW.split(":");
                
                Favorite busStop = new Favorite();
                
                switch (params.length) {
                    case 3:
                        busStop.setAdapted(params[2]);
                    case 2:
                        busStop.setLineFilter(params[1]);
                    case 1:
                        busStop.setStopName(params[0]);
                        break;
                }
                
                favoriteList.add(busStop);
            }
        }
        
        return favoriteList;
    }
    
    @Override
    public void setAllFavorites(ObservableList<Favorite> favorites) {
        
        StringBuilder value = new StringBuilder("");
        
        for (Favorite favorite : favorites) {
            value.append(favorite.getStopName() + ":" + favorite.getLineFilter() + ":" + favorite.getAdapted() + ";");
        }
        
        filePropertiesService.setProperty("bus.stop.favorites", value.toString());
    }
    
}
