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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

/**
 * SVN as support for properties that have a namespace and contain a colon in their name. XML in general does not allow such names. The methods in this class make sure these properties can be read and written by default XML tool by escaping them before the XML processing and reverting the escaping
 * afterwards.
 */
final class ResourcePropertyUtils {

    private static final String COLON = ":";

    static final String MARKER = "__ILLEGAL_COLON_IN_TAG_NAME__";

    private static final Pattern PATTERN = Pattern.compile("<(/?)(\\s*)([\\w\\d]+):([\\w\\d]+):([\\w\\d]+)((?:\\s*[\\w\\d=\"]*\\s*)*)(/?)>");

    static final Charset UTF8 = Charset.forName("UTF-8");

    static InputStream escapedInputStream(final InputStream inputStream) throws IOException {
        final String rawData = IOUtils.toString(inputStream, UTF8);
        inputStream.close();

        final Matcher matcher = PATTERN.matcher(rawData);
        final String escapedData = matcher.replaceAll("<$1$2$3:$4" + MARKER + "$5$6$7>");
        return IOUtils.toInputStream(escapedData, UTF8);
    }

    static String escapedKeyNameXml(final String name) {
        return name.replace(COLON, MARKER);
    }

    static String filterMarker(final String xml) {
        return xml.replaceAll(MARKER, COLON);
    }

    static String unescapedKeyNameXml(final String name) {
        return name.replace(MARKER, COLON);
    }

    private ResourcePropertyUtils() {
        // prevent instantiation
    }

}
