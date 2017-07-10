package es.tamarit.widgetemt.services.favourites;

import javafx.collections.ObservableList;

public interface FavouritesService {
    
    public ObservableList<Favourite> getAllFavourites();
    
    public void setAllFavourites(ObservableList<Favourite> favourites);
}
