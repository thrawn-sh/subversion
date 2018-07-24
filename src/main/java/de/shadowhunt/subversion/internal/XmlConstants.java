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
package de.shadowhunt.subversion.internal;

public final class XmlConstants {

    public static final String DAV_NAMESPACE = "DAV:";

    public static final String DAV_PREFIX = "D";

    public static final String ENCODING = "UTF-8";

    public static final String SUBVERSION_CUSTOM_NAMESPACE = "http://subversion.tigris.org/xmlns/custom/";

    public static final String SUBVERSION_CUSTOM_PREFIX = "C";

    public static final String SUBVERSION_DAV_NAMESPACE = "http://subversion.tigris.org/xmlns/dav/";

    public static final String SUBVERSION_DAV_PREFIX = "V";

    public static final String SUBVERSION_SVN_NAMESPACE = "http://subversion.tigris.org/xmlns/svn/";

    public static final String SUBVERSION_SVN_PREFIX = "S";

    public static final String SVN_NAMESPACE = "svn:";

    public static final String SVN_PREFIX = "s";

    public static final String VERSION_1_0 = "1.0";

    private XmlConstants() {
        // prevent instantiation
    }
}
