package es.tamarit.widgetemt.services.updater;

public interface Updater {
    
    boolean checkForUpdates(String currentVersion);
    
    String getLatestVersion() throws Exception;
    
    String getWhatsNew() throws Exception;
}
