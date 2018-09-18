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
package de.shadowhunt.subversion.internal.jaxb;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

import de.shadowhunt.subversion.ReadOnlyRepository.ProtocolVersion;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.internal.ProbeResult;
import de.shadowhunt.subversion.internal.XmlConstants;

@XmlRootElement(namespace = XmlConstants.DAV_NAMESPACE, name = "options-response")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProbeParser {

    static class Helper {

        @XmlElement(namespace = XmlConstants.DAV_NAMESPACE, name = "href", required = true)
        String href;
    }

    private static final JAXBContext CONTEXT;

    private static final Pattern PATH_PATTERN = Pattern.compile(Resource.SEPARATOR);

    static {
        try {
            CONTEXT = JAXBContext.newInstance(ProbeParser.class);
        } catch (final JAXBException e) {
            throw new SubversionException("can not create context", e);
        }
    }

    private static ProbeResult covert(final String path, final ProtocolVersion version) {
        if ((ProtocolVersion.HTTP_V1 == version) || (ProtocolVersion.HTTP_V2 == version)) {
            // .../${svn}/act/
            // ____^^^^^^ <- prefix
            // ^^^ <- part of baseUri
            final String[] segments = PATH_PATTERN.split(path);
            final int prefixIndex = segments.length - 2;
            final String prefix = segments[prefixIndex];

            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < prefixIndex; i++) {
                sb.append(Resource.SEPARATOR_CHAR);
                sb.append(segments[i]);
            }
            final String value = sb.toString();
            final Resource basePath = Resource.create(value);
            return new ProbeResult(version, basePath, prefix);
        }
        throw new SubversionException("version not supported");
    }

    public static ProbeResult parse(final InputStream input, final ProtocolVersion version) throws IOException {
        try {
            final Unmarshaller unmarshaller = CONTEXT.createUnmarshaller();
            final StreamSource source = new StreamSource(input);
            final ProbeParser parser = (ProbeParser) unmarshaller.unmarshal(source);
            return covert(parser.collection.href, version);
        } catch (final JAXBException e) {
            throw new IOException("can not read input", e);
        }
    }

    @XmlElement(namespace = XmlConstants.DAV_NAMESPACE, name = "activity-collection-set", required = true)
    private Helper collection;

}
