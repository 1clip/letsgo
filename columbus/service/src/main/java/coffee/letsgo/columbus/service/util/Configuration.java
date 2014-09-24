package coffee.letsgo.columbus.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by xbwu on 9/20/14.
 */
public class Configuration {
    private static Properties prop = new Properties();
    private static String propFileName = "columbus.properties";
    private static boolean initialized = false;
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    static {
        InputStream in = Configuration.class.getClassLoader().getResourceAsStream(propFileName);
        if (in == null) {
            logger.error("properties file {} not found", propFileName);
        } else {
            try {
                prop.load(in);
                initialized = true;
            } catch (IOException e) {
                logger.error("failed to load properties from {}", propFileName);
            }
        }
    }

    public static String read(String key) {
        return read(key, null);
    }

    public static String read(String key, String defaultValue) {
        if(!initialized) {
            logger.warn("properties file {} not loaded, returning default value of {}", propFileName, key);
            return defaultValue;
        }
        String val = prop.getProperty(key, defaultValue);
        logger.debug("reading property {}={}", key, val);
        return val;
    }
}