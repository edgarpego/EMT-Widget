package es.tamarit.widgetemt.services.favorites;

import javafx.collections.ObservableList;

public interface FavoritesService {
    
    public ObservableList<Favorite> getAllFavorites();
    
    public void setAllFavorites(ObservableList<Favorite> favorites);
}
