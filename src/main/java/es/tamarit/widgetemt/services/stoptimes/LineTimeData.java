package es.tamarit.widgetemt.services.stoptimes;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LineTimeData {
    
    private StringProperty lineImgURL;
    private StringProperty lineName;
    private StringProperty lineTime;
    
    public LineTimeData() {
        lineImgURL = new SimpleStringProperty("");
        lineName = new SimpleStringProperty("");
        lineTime = new SimpleStringProperty("");
    }
    
    public String getLineImgURL() {
        return lineImgURL.get();
    }
    
    public void setLineImgURL(String lineImgURL) {
        this.lineImgURL.set(lineImgURL);
    }
    
    public StringProperty getLineImgURLProperty() {
        return lineImgURL;
    }
    
    public String getLineName() {
        return lineName.get();
    }
    
    public void setLineName(String lineName) {
        this.lineName.set(lineName);
    }
    
    public StringProperty getLineNameProperty() {
        return lineName;
    }
    
    public String getLineTime() {
        return lineTime.get();
    }
    
    public void setLineTime(String lineTime) {
        this.lineTime.set(lineTime);
    }
    
    public StringProperty getLineTimeProperty() {
        return lineTime;
    }
    
}
