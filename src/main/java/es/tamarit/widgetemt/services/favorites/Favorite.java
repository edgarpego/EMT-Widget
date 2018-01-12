package es.tamarit.widgetemt.services.favorites;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Favorite {
    
    private StringProperty stopName;
    private StringProperty lineFilter;
    private StringProperty adapted;
    
    public Favorite() {
        this.stopName = new SimpleStringProperty("");
        this.lineFilter = new SimpleStringProperty("none");
        this.adapted = new SimpleStringProperty("0");
    }
    
    public String getStopName() {
        return stopName.get();
    }
    
    public void setStopName(String stopName) {
        this.stopName.set(stopName);
    }
    
    public StringProperty stopNameProperty() {
        return stopName;
    }
    
    public String getLineFilter() {
        return lineFilter.get();
    }
    
    public void setLineFilter(String lineFilter) {
        this.lineFilter.set(lineFilter);
    }
    
    public StringProperty lineFilterProperty() {
        return lineFilter;
    }
    
    public String getAdapted() {
        return adapted.get();
    }
    
    public void setAdapted(String adapted) {
        this.adapted.set(adapted);
    }
    
    public StringProperty adaptedProperty() {
        return adapted;
    }
    
    @Override
    public String toString() {
        return stopName.get();
    }
}
