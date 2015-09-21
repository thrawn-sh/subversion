package de.shadowhunt.subversion.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BuildProperties {

    private static final String FALLBACK = "UNDEFINED";

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildProperties.class);

    private static final String PROPERTIES_RESOURCE = "META-INF/build.properties";

    private static transient volatile Properties properties;

    private static final Properties getProperties() {
        if (properties != null) {
            return properties;
        }

        properties = new Properties();
        final InputStream stream = BuildProperties.class.getResourceAsStream(PROPERTIES_RESOURCE);
        if (stream != null) {
            try {
                properties.load(stream);
            } catch(final IOException e) {
                LOGGER.warn("could not load resource " + PROPERTIES_RESOURCE, e);
            } finally {
                IOUtils.closeQuietly(stream);
            }
        } else {
            LOGGER.warn("could not find resource " + PROPERTIES_RESOURCE);
        }
        return properties;
    }

    public static String getBuildDate() {
        return getProperties().getProperty("build.date", FALLBACK);
    }

    public static String getBuildVersion() {
        return getProperties().getProperty("build.version", FALLBACK);
    }

    public static String getUserAgent() {
        return "SVN/" + getBuildVersion() + " " + getBuildDate() + " (https://dev.shadowhunt.de/subversion)";
    }
}
