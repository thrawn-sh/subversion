/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2019 shadowhunt (dev@shadowhunt.de)
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.ResourceProperty.Key;
import de.shadowhunt.subversion.ResourceProperty.Type;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

/**
 * SVN as support for properties that have a namespace and contain a colon in their name. XML in general does not allow such names. The methods in this class make sure these properties can be read and written by default XML tool by escaping them before the XML processing and reverting the escaping
 * afterwards.
 */
public final class ResourcePropertyUtils {

    public static final Key AUTHOR = new Key(Type.DAV, "creator-displayname");

    private static final String COLON = ":";

    public static final Key CREATION_DATE = new Key(Type.DAV, "creationdate");

    public static final Key LAST_MODIFIED_DATE = new Key(Type.DAV, "getlastmodified");

    public static final Key LOCK_STATUS = new Key(Type.DAV, "lockdiscovery");

    public static final Key LOCK_TOKEN = new Key(Type.DAV, "href");

    static final String MARKER = "__ILLEGAL_COLON_IN_TAG_NAME__";

    public static final Key MD5_HASH = new Key(Type.SUBVERSION_DAV, "md5-checksum");

    private static final Pattern PATTERN = Pattern.compile("<(/?)(\\s*)([\\w\\d]+):([\\w\\d]+):([\\w\\d]+)((?:\\s*[\\w\\d=\"]*\\s*)*)(/?)>");

    public static final Key REPOSITORY_ID = new Key(Type.SUBVERSION_DAV, "repository-uuid");

    public static final Key RESOURCE = new Key(Type.SUBVERSION_DAV, "baseline-relative-path");

    public static final Key RESOURCE_TYPE = new Key(Type.DAV, "resourcetype");

    public static final Comparator<ResourceProperty> TYPE_NAME_COMPARATOR = (rp1, rp2) -> {
        Validate.notNull(rp1, "rp1 must not be null");
        Validate.notNull(rp2, "rp2 must not be null");

        final Key key1 = rp1.getKey();
        final Key key2 = rp2.getKey();
        return key1.compareTo(key2);
    };

    public static final Key VERSION = new Key(Type.DAV, "version-name");

    public static InputStream escapedInputStream(final InputStream inputStream) throws IOException {
        final String rawData = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        inputStream.close();

        final Matcher matcher = PATTERN.matcher(rawData);
        final String escapedData = matcher.replaceAll("<$1$2$3:$4" + MARKER + "$5$6$7>");
        return IOUtils.toInputStream(escapedData, StandardCharsets.UTF_8);
    }

    public static String escapedKeyNameXml(final String name) {
        return name.replace(COLON, MARKER);
    }

    public static String filterMarker(final String xml) {
        return xml.replaceAll(MARKER, COLON);
    }

    static String unescapedKeyNameXml(final String name) {
        return name.replace(MARKER, COLON);
    }

    private ResourcePropertyUtils() {
        // prevent instantiation
    }
}
