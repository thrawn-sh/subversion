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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.transform.stream.StreamSource;

import de.shadowhunt.subversion.LockToken;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.internal.InfoImpl;
import de.shadowhunt.subversion.internal.ResourcePropertyUtils;
import de.shadowhunt.subversion.internal.XmlConstants;
import de.shadowhunt.subversion.internal.jaxb.converter.CreatedDateAdapter;
import de.shadowhunt.subversion.internal.jaxb.converter.LastModifedDateAdapter;
import de.shadowhunt.subversion.internal.jaxb.converter.LockTokenAdapter;
import de.shadowhunt.subversion.internal.jaxb.converter.ResourceAdapter;
import de.shadowhunt.subversion.internal.jaxb.converter.ResourcePropertyAdapter;
import de.shadowhunt.subversion.internal.jaxb.converter.RevisionAdapter;
import de.shadowhunt.subversion.internal.jaxb.converter.UuidAdapter;
import org.apache.commons.lang3.StringUtils;

@XmlRootElement(namespace = XmlConstants.DAV_NAMESPACE, name = "multistatus")
@XmlAccessorType(XmlAccessType.FIELD)
public class InfoParser {

    @XmlAccessorType(XmlAccessType.FIELD)
    static class ActiveLock {

        @XmlElement(namespace = XmlConstants.DAV_NAMESPACE, name = "locktoken")
        Token token;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class LockDiscovery {

        @XmlElement(namespace = XmlConstants.DAV_NAMESPACE, name = "activelock")
        ActiveLock activeLock;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class Properties {

        @XmlElement(namespace = XmlConstants.DAV_NAMESPACE, name = "creationdate")
        @XmlJavaTypeAdapter(CreatedDateAdapter.class)
        Date creationDate;

        @XmlElement(namespace = XmlConstants.DAV_NAMESPACE, name = "getlastmodified")
        @XmlJavaTypeAdapter(LastModifedDateAdapter.class)
        Date lastModifedDate;

        @XmlElement(namespace = XmlConstants.DAV_NAMESPACE, name = "lockdiscovery")
        LockDiscovery lockDiscovery;

        @XmlElement(namespace = XmlConstants.SUBVERSION_DAV_NAMESPACE, name = "md5-checksum")
        String md5Hash;

        @XmlElement(namespace = XmlConstants.SUBVERSION_DAV_NAMESPACE, name = "baseline-relative-path")
        @XmlJavaTypeAdapter(ResourceAdapter.class)
        Resource relativeResource;

        @XmlElement(namespace = XmlConstants.SUBVERSION_DAV_NAMESPACE, name = "repository-uuid")
        @XmlJavaTypeAdapter(UuidAdapter.class)
        UUID repositoryUuid;

        @XmlAnyElement
        @XmlJavaTypeAdapter(ResourcePropertyAdapter.class)
        List<ResourceProperty> resourceProperties;

        @XmlElement(namespace = XmlConstants.DAV_NAMESPACE, name = "resourcetype")
        ResourceType resourceType;

        @XmlElement(namespace = XmlConstants.DAV_NAMESPACE, name = "version-name")
        @XmlJavaTypeAdapter(RevisionAdapter.class)
        Revision revision;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class Propstat {

        @XmlElement(namespace = XmlConstants.DAV_NAMESPACE, name = "prop")
        Properties properties;

        @XmlElement(namespace = XmlConstants.DAV_NAMESPACE, name = "status", required = true)
        String status;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class ResourceType {

        @XmlElement(namespace = XmlConstants.DAV_NAMESPACE, name = "collection")
        String collection;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class Response {

        @XmlElement(namespace = XmlConstants.DAV_NAMESPACE, name = "propstat")
        Propstat propstat;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class Token {

        @XmlElement(namespace = XmlConstants.DAV_NAMESPACE, name = "href")
        @XmlJavaTypeAdapter(LockTokenAdapter.class)
        LockToken lockToken;
    }

    private static final JAXBContext CONTEXT;

    static {
        try {
            CONTEXT = JAXBContext.newInstance(InfoParser.class);
        } catch (final JAXBException e) {
            throw new SubversionException("can not create context", e);
        }
    }

    private static List<InfoImpl> convert(final List<Response> responses, final Resource basePath) throws ParseException {
        final int size = responses.size();
        final List<InfoImpl> result = new ArrayList<>(size);
        for (final Response response : responses) {
            final Propstat propstat = response.propstat;
            if (!"HTTP/1.1 200 OK".equals(propstat.status)) {
                throw new SubversionException("properties are missing");
            }
            final InfoImpl impl = convert(propstat.properties, basePath);
            result.add(impl);
        }
        return result;
    }

    private static InfoImpl convert(final Properties properties, final Resource basePath) {
        final InfoImpl impl = new InfoImpl();
        if (properties.creationDate != null) {
            impl.setCreationDate(properties.creationDate);
        }
        if (properties.resourceType != null) {
            impl.setDirectory(properties.resourceType.collection != null);
        }
        if (properties.lastModifedDate != null) {
            impl.setLastModifiedDate(properties.lastModifedDate);
        }
        if (properties.lockDiscovery != null) {
            if (properties.lockDiscovery.activeLock != null) {
                if (properties.lockDiscovery.activeLock.token != null) {
                    impl.setLockToken(properties.lockDiscovery.activeLock.token.lockToken);
                }
            }
        }
        if (properties.md5Hash != null) {
            impl.setMd5(properties.md5Hash);
        }
        if (properties.resourceProperties != null) {
            final List<ResourceProperty> propertiesList = properties.resourceProperties;
            propertiesList.removeIf(Objects::isNull);
            propertiesList.sort(ResourcePropertyUtils.TYPE_NAME_COMPARATOR);
            final ResourceProperty[] resourceProperties = propertiesList.toArray(new ResourceProperty[0]);
            impl.setProperties(resourceProperties);
        }
        if (properties.repositoryUuid != null) {
            impl.setRepositoryId(properties.repositoryUuid);
        }
        if (properties.relativeResource != null) {
            final Resource resource = convert(properties.relativeResource, basePath);
            impl.setResource(resource);
        }
        if (properties.revision != null) {
            impl.setRevision(properties.revision);
        }
        return impl;
    }

    private static Resource convert(final Resource relativeResource, final Resource basePath) {
        final String value = relativeResource.getValue();
        final String prefix = basePath.getValue();
        final String resource = StringUtils.removeStart(value, prefix);
        return Resource.create(resource);
    }

    public static List<InfoImpl> parse(final InputStream input, final Resource basePath) throws IOException {
        try {
            final Unmarshaller unmarshaller = CONTEXT.createUnmarshaller();
            final InputStream escapedInputStream = ResourcePropertyUtils.escapedInputStream(input);
            final StreamSource source = new StreamSource(escapedInputStream);
            final InfoParser parser = (InfoParser) unmarshaller.unmarshal(source);
            if (parser.responses == null) {
                return Collections.emptyList();
            }
            return convert(parser.responses, basePath);
        } catch (final JAXBException | ParseException e) {
            throw new IOException("can not read input", e);
        }
    }

    @XmlElement(namespace = XmlConstants.DAV_NAMESPACE, name = "response")
    private List<Response> responses;
}
