/**
 * Copyright © 2013-2018 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.shadowhunt.subversion;

import org.apache.commons.lang3.StringUtils;

/**
 * Provides information about the subversion client lib.
 */
public final class BuildProperties {

    public static final String UNDEFINED = "UNDEFINED";

    private static final String VERSION;

    static {
        final Package packageObject = BuildProperties.class.getPackage();
        final String version = packageObject.getImplementationVersion();
        if (StringUtils.isEmpty(version)) {
            VERSION = UNDEFINED;
        } else {
            VERSION = version;
        }
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
        return "SVN/" + VERSION + " (https://dev.shadowhunt.de/subversion)";
    }

    private BuildProperties() {
        // prevent instantiation
    }
}
