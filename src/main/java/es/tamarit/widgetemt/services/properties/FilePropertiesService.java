package es.tamarit.widgetemt.services.properties;

/**
 * Interface to Read/Write in properties file
 * @author Edgar
 *
 */
public interface FilePropertiesService {
    
    public String getProperty(String key);
    
    public void setProperty(String key, String value);
    
    public void store();
    
}
