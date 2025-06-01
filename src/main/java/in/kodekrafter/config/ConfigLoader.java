package in.kodekrafter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);

    public static int getCacheCapacity() {
        Properties props = new Properties();
        try (InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) {
                props.load(is);
                return Integer.parseInt(props.getProperty("cache.capacity", "16"));
            } else {
                log.warn("config.properties not found in classpath");
                return 16;
            }
        } catch (IOException e) {
            log.warn("Error loading config file", e);
            log.warn("Defaulting cache capacity to 16");
            return 16;
        }
    }
}