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
package de.shadowhunt.subversion.internal.jaxb;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.transform.stream.StreamSource;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.Resolve;
import de.shadowhunt.subversion.internal.XmlConstants;
import de.shadowhunt.subversion.internal.jaxb.converter.RevisionAdapter;
import org.apache.commons.lang3.StringUtils;

@XmlRootElement(namespace = XmlConstants.SVN_NAMESPACE, name = "get-locations-report")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResolveParser {

    static class Location {

        @XmlAttribute(name = "path")
        String path;

        @XmlAttribute(name = "rev")
        @XmlJavaTypeAdapter(RevisionAdapter.class)
        Revision revision;
    }

    private static final JAXBContext CONTEXT;

    static {
        try {
            CONTEXT = JAXBContext.newInstance(ResolveParser.class);
        } catch (final JAXBException e) {
            throw new SubversionException("can not create context", e);
        }
    }

    private static Resolve convert(final Location location, final Resource basePath) {
        final Resource fullResource = Resource.create(location.path);
        final String full = fullResource.getValue();
        final String prefix = basePath.getValue();
        final String resource = StringUtils.removeStart(full, prefix);
        final Resource suffix = Resource.create(resource);
        final QualifiedResource qualifiedResource = new QualifiedResource(basePath, suffix);
        return new Resolve(qualifiedResource, location.revision);
    }

    public static Resolve parse(final InputStream input, final Resource basePath) throws IOException {
        try {
            final Unmarshaller unmarshaller = CONTEXT.createUnmarshaller();
            final StreamSource source = new StreamSource(input);
            final ResolveParser parser = (ResolveParser) unmarshaller.unmarshal(source);
            return convert(parser.location, basePath);
        } catch (final JAXBException e) {
            throw new IOException("can not read input", e);
        }
    }

    @XmlElement(namespace = XmlConstants.SVN_NAMESPACE, name = "location", required = true)
    private Location location;
}
