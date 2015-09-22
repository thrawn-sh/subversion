/**
 * Copyright (C) 2013-2015 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private static final String PROPERTIES_RESOURCE = "META-INF/build.PROPERTIES";

    private static final String VERSION;

    private static final String BUILD_DATE;

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildProperties.class);

    static {
        final Properties properties = new Properties();

        final InputStream stream = BuildProperties.class.getResourceAsStream(PROPERTIES_RESOURCE);
        if (stream != null) {
            try {
                properties.load(stream);
            } catch (final IOException e) {
                // ignore errors
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }

        BUILD_DATE = properties.getProperty("build.date", UNDEFINED);
        VERSION = properties.getProperty("build.version", UNDEFINED);
    }

    private BuildProperties() {
        // prevent instantiation
    }

    /**
     * Returns the {@link Date} of the build.
     *
     * @return {@link Date} of the build
     */
    public static Optional<Date> getBuildDate() {
        if (UNDEFINED.equals(BUILD_DATE)) {
            return Optional.empty();
        }

        final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        try {
            return Optional.of(dateFormat.parse(BUILD_DATE));
        } catch (final ParseException e) {
            LOGGER.debug("could not parse date {} with pattern {}", BUILD_DATE, DATE_PATTERN, e);
        }
        return Optional.empty();
    }

    /**
     * Returns the build version ({MAJOR}.{MINOR}.{PATCH} with -SNAPSHOT suffix, if it's an internal release).
     *
     * @return version or UNDEFINED if the version could not be determined
     */
    public static String getBuildVersion() {
        return VERSION;
    }

    /**
     * Returns the User-Agent identifier that is sent by each request to the server.
     *
     * @return User-Agent identifier
     */
    public static String getUserAgent() {
        return "SVN/" + VERSION + " " + BUILD_DATE + " (https://dev.shadowhunt.de/subversion)";
    }
}
