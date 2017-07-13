package es.tamarit.widgetemt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class CommandBuilderTest {
    
    private static final Logger LOGGER = LogManager.getLogger(CommandBuilderTest.class);
    
    private static final String SERVICE_NAME = "AxInstSV";
    
    @Test
    public void searchService() throws IOException {
        
        String command = "sc query " + SERVICE_NAME;
        
        Process process = Runtime.getRuntime().exec("cmd /c " + command);
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
        }
        String result = builder.toString();
        
        LOGGER.info(result);
    }
    
}
