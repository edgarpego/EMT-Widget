package es.tamarit.widgetemt;

import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import es.tamarit.widgetemt.utils.WinRegistry;

public class WinRegistryTests {
    
    private static final Logger LOGGER = LogManager.getLogger(WinRegistryTests.class);
    private static final String KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Run";
    
    @Test
    public void readString() {
        
        try {
            String result = WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER, KEY, "HP ENVY 4520 series");
            
            LOGGER.info("Value: " + result);
            
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("Error trying to read value from the registry", e);
        }
        
    }
}
