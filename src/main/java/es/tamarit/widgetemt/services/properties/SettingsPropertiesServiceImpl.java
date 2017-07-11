package es.tamarit.widgetemt.services.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SettingsPropertiesServiceImpl implements FilePropertiesService {
    
    private static final Logger LOGGER = LogManager.getLogger(SettingsPropertiesServiceImpl.class);
    private static final String FILE_SETTINGS = System.getProperty("user.home") + File.separator + ".EMTWidget" + File.separator + "settings.emt";
    private static final String CODIFICATION = "UTF-8";
    
    private Properties properties;
    
    public SettingsPropertiesServiceImpl() throws IOException {
        newFileIfNotExists();
        
        properties = new Properties();
        Reader reader = new InputStreamReader(new FileInputStream(FILE_SETTINGS), CODIFICATION);
        properties.load(reader);
        reader.close();
        
        checkPropertiesIntegrity();
    }
    
    private void newFileIfNotExists() {
        try {
            File file = new File(FILE_SETTINGS);
            
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                
                Writer writer = new OutputStreamWriter(new FileOutputStream(FILE_SETTINGS), CODIFICATION);
                writer.write("widget.position.x=50");
                writer.write("widget.position.y=50");
                writer.write("bus.stop.name=");
                writer.write("bus.stop.favorites=");
                writer.write("always.on.front=false");
                writer.write("auto.refresh.data=false");
                writer.write("application.language.locale=es-ES");
                writer.write("number.mobilis.card=");
                writer.close();
            }
        } catch (IOException e) {
            LOGGER.error("Error trying to create the settings file.", e);
        }
    }
    
    private void checkPropertiesIntegrity() {
        
        boolean neededRestore = false;
        
        if (properties.getProperty("widget.position.x") == null || properties.getProperty("widget.position.x").isEmpty()) {
            properties.setProperty("widget.position.x", "50");
            neededRestore = true;
        }
        if (properties.getProperty("widget.position.y") == null || properties.getProperty("widget.position.y").isEmpty()) {
            properties.setProperty("widget.position.y", "50");
            neededRestore = true;
        }
        if (properties.getProperty("always.on.front") == null || properties.getProperty("always.on.front").isEmpty()) {
            properties.setProperty("always.on.front", "false");
            neededRestore = true;
        }
        if (properties.getProperty("bus.stop.name") == null || properties.getProperty("bus.stop.name").isEmpty()) {
            properties.setProperty("bus.stop.name", "");
            neededRestore = true;
        }
        if (properties.getProperty("bus.stop.favorites") == null || properties.getProperty("bus.stop.favorites").isEmpty()) {
            properties.setProperty("bus.stop.favorites", "");
            neededRestore = true;
        }
        if (properties.getProperty("auto.refresh.data") == null || properties.getProperty("auto.refresh.data").isEmpty()) {
            properties.setProperty("auto.refresh.data", "false");
            neededRestore = true;
        }
        if (properties.getProperty("application.language.locale") == null || properties.getProperty("application.language.locale").isEmpty()) {
            properties.setProperty("application.language.locale", "es-ES");
            neededRestore = true;
        }
        if (properties.getProperty("number.mobilis.card") == null || properties.getProperty("number.mobilis.card").isEmpty()) {
            properties.setProperty("number.mobilis.card", "");
            neededRestore = true;
        }
        
        if (neededRestore) {
            store();
        }
    }
    
    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    @Override
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
    
    @Override
    public void store() {
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(FILE_SETTINGS), CODIFICATION);
            properties.store(writer, null);
            writer.close();
        } catch (FileNotFoundException e) {
            LOGGER.error("Error trying open the file.", e);
        } catch (IOException e) {
            LOGGER.error("Error trying to save the new data to the file.", e);
        }
    }
}
