/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2018 shadowhunt (dev@shadowhunt.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.shadowhunt.subversion;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang3.StringUtils;

/**
 * Provides information about the subversion client lib.
 */
@Immutable
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
     * @return version or {@code UNDEFINED} if the version could not be determined
     */
    public static String getBuildVersion() {
        return VERSION;
    }

    private BuildProperties() {
        // prevent instantiation
    }
}
