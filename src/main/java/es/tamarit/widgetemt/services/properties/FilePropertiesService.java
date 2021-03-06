package es.tamarit.widgetemt.services.properties;

/**
 * Interface to Read/Write in properties files
 */
public interface FilePropertiesService {
    
    public String getProperty(String key);
    
    public void setProperty(String key, String value);
    
    public void store();
    
}
