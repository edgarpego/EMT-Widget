package es.tamarit.widgetemt.controllers;

import es.tamarit.widgetemt.core.ViewManager;

public abstract class AbstractController {
    
    protected ViewManager viewManager;
    
    public void setViewManager(ViewManager viewManager) {
        this.viewManager = viewManager;
    }
    
}
