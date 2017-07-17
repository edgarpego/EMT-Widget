package es.tamarit.widgetemt.controllers;

import es.tamarit.widgetemt.core.ViewManager;
import javafx.fxml.Initializable;

public abstract class AbstractController implements Initializable {
    
    protected ViewManager viewManager;
    
    public void setViewManager(ViewManager viewManager) {
        this.viewManager = viewManager;
    }
    
}
