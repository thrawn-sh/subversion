package de.shadowhunt.subversion;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides information about the subversion client lib.
 */
public final class BuildProperties {

    public static final String UNDEFINED = "UNDEFINED";

    public static final String DATE_PATTERN = "yyyy-MM-dd";

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildProperties.class);

    private static final String PROPERTIES_RESOURCE = "META-INF/build.properties";

    private static transient volatile Properties properties;

    private BuildProperties() {
        // prevent instantiation
    }

    private static Properties getProperties() {
        if (properties != null) {
            return properties;
        }

        properties = new Properties();
        final InputStream stream = BuildProperties.class.getResourceAsStream(PROPERTIES_RESOURCE);
        if (stream != null) {
            try {
                properties.load(stream);
            } catch (final IOException e) {
                LOGGER.warn("could not load resource " + PROPERTIES_RESOURCE, e);
            } finally {
                IOUtils.closeQuietly(stream);
            }
        } else {
            LOGGER.warn("could not find resource " + PROPERTIES_RESOURCE);
        }
        return properties;
    }

    /**
     * Returns the {@link Date} of the build.
     *
     * @return {@link Date} of the build
     */
    public static Optional<Date> getBuildDate() {
        final String value = getBuildDate0();
        if (UNDEFINED.equals(value)) {
            return Optional.empty();
        }

        final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        try {
            return Optional.of(dateFormat.parse(value));
        } catch (final ParseException e) {
            LOGGER.debug("could not parse date {} with pattern {}", value, DATE_PATTERN, e);
        }
        return Optional.empty();
    }

    private static String getBuildDate0() {
        return getProperties().getProperty("build.date", UNDEFINED);
    }

    /**
     * Returns the build version ({MAJOR}.{MINOR}.{PATCH} with -SNAPSHOT suffix, if it's an internal release).
     *
     * @return version or UNDEFINED if the version could not be determined
     */
    public static String getBuildVersion() {
        return getProperties().getProperty("build.version", UNDEFINED);
    }

    /**
     * Returns the User-Agent identifier that is sent by each request to the server.
     *
     * @return User-Agent identifier
     */
    public static String getUserAgent() {
        return "SVN/" + getBuildVersion() + " " + getBuildDate0() + " (https://dev.shadowhunt.de/subversion)";
    }
}
