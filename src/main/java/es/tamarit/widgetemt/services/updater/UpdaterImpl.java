package es.tamarit.widgetemt.services.updater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UpdaterImpl implements Updater {
    
    private static final Logger LOGGER = LogManager.getLogger(UpdaterImpl.class);
    private final Integer PING_TIMEOUT = 3000;
    private final String SERVER_URL = "www.edgartamarit.com";
    private final String LAST_VERSION = "http://edgartamarit.com/downloads/emt-widget/version.html";
    private final String HISTORY_URL = "http://edgartamarit.com/downloads/emt-widget/history.html";
    
    @Override
    public boolean checkForUpdates(String currentVersion) {
        
        try {
            InetAddress address = InetAddress.getByName(SERVER_URL);
            
            if (address.isReachable(PING_TIMEOUT)) {
                LOGGER.info("Reachable server");
                if (versionCompare(currentVersion, getLatestVersion()) < 0) {
                    return true;
                } else {
                    return false;
                }
            } else {
                LOGGER.info("Unreachable server");
                return false;
            }
        } catch (UnknownHostException e) {
            LOGGER.error("Error trying to contact with the server.", e);
        } catch (IOException e) {
            LOGGER.error("Error trying to reach the server.", e);
        } catch (Exception e) {
            LOGGER.error("Error trying to get the lastest version file from the server", e);
        }
        
        return false;
        
    }
    
    @Override
    public String getLatestVersion() throws Exception {
        String data = getData(LAST_VERSION);
        return data.substring(data.indexOf("[version]") + 9, data.indexOf("[/version]"));
    }
    
    @Override
    public String getWhatsNew() throws Exception {
        String data = getData(HISTORY_URL);
        return data.substring(data.indexOf("[history]") + 9, data.indexOf("[/history]"));
    }
    
    private String getData(String address) throws Exception {
        
        URL url = new URL(address);
        BufferedReader html = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
        StringBuilder buffer = new StringBuilder("");
        
        int c = 0;
        while (c != -1) {
            c = html.read();
            buffer.append((char) c);
        }
        
        return buffer.toString();
    }
    
    /**
     * Compares two version strings.
     * 
     * Use this instead of String.compareTo() for a non-lexicographical comparison that works for version strings. e.g. "1.10".compareTo("1.6").
     * 
     * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
     * 
     * @param str1 a string of ordinal numbers separated by decimal points.
     * @param str2 a string of ordinal numbers separated by decimal points.
     * @return The result is a negative integer if str1 is _numerically_ less than str2. The result is a positive integer if str1 is _numerically_ greater than str2. The result is zero if the strings are _numerically_ equal.
     */
    private int versionCompare(String str1, String str2) {
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0;
        // set index to first non-equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }
        // compare first non-equal ordinal number
        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }
        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        return Integer.signum(vals1.length - vals2.length);
    }
}
