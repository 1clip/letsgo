package coffee.letsgo.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.google.common.base.Verify.verifyNotNull;

/**
 * Created by xbwu on 9/25/14.
 */
public class ConfigurationReader {
    private final Properties prop = new Properties();
    private final String propFileName;
    private boolean initialized = false;
    private final Logger logger = LoggerFactory.getLogger(ConfigurationReader.class);

    public ConfigurationReader(String propFileName) {
        verifyNotNull(propFileName, "property file name");
        this.propFileName = String.format("%s.properties", propFileName.trim());
        loadProperties();
    }

    private void loadProperties() {
        InputStream in = ConfigurationReader.class.getClassLoader().getResourceAsStream(propFileName);
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

    public String read(String key) {
        return read(key, null);
    }

    public String read(String key, String defaultValue) {
        if (!initialized) {
            logger.warn("properties file {} not loaded, returning default value for {}", propFileName, key);
            return defaultValue;
        }
        String val = prop.getProperty(key, defaultValue);
        logger.debug("reading property {}={}", key, val);
        return val;
    }

    public Properties prop() {
        return (Properties) prop.clone();
    }
}
