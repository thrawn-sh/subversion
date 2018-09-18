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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.LockToken;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.xml.AbstractSaxExpression;
import de.shadowhunt.subversion.xml.AbstractSaxExpressionHandler;
import de.shadowhunt.subversion.xml.SaxExpression;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

final class InfoImplReader {

    private static class InfoExpression extends AbstractSaxExpression<List<Info>> {

        private static final QName[] PATH;

        static {
            final QName multistatusQName = new QName(XmlConstants.DAV_NAMESPACE, "multistatus");
            final QName resopnseQName = new QName(XmlConstants.DAV_NAMESPACE, "response");
            PATH = new QName[] { multistatusQName, resopnseQName };
        }

        private static SaxExpression<?>[] createExpressions(final Resource basePath) {
            final QName creatationDateQName = new QName(XmlConstants.DAV_NAMESPACE, "creationdate");
            final QName lastModifiedQName = new QName(XmlConstants.DAV_NAMESPACE, "getlastmodified");
            final QName lockDiscoveryQName = new QName(XmlConstants.DAV_NAMESPACE, "lockdiscovery");
            final QName activeLockQName = new QName(XmlConstants.DAV_NAMESPACE, "activelock");
            final QName lockTokenQName = new QName(XmlConstants.DAV_NAMESPACE, "locktoken");
            final QName hrefQName = new QName(XmlConstants.DAV_NAMESPACE, "href");
            final QName md5ChecksumQName = new QName(XmlConstants.SUBVERSION_DAV_NAMESPACE, "md5-checksum");
            final QName reposiotryUuidQName = new QName(XmlConstants.SUBVERSION_DAV_NAMESPACE, "repository-uuid");
            final QName versionNameQName = new QName(XmlConstants.DAV_NAMESPACE, "version-name");

            final StringExpression creatationDateExpression = new StringExpression(creatationDateQName);
            final ResourceTypeExpression resourceTypeExpression = new ResourceTypeExpression();
            final StringExpression lastModifiedExpression = new StringExpression(lastModifiedQName);
            final StringExpression lockExpression = new StringExpression(lockDiscoveryQName, activeLockQName, lockTokenQName, hrefQName);
            final StringExpression md5ChecksumExpression = new StringExpression(md5ChecksumQName);
            final PropertyExpression propertyExpression = new PropertyExpression();
            final StringExpression repositoryUuidExpression = new StringExpression(reposiotryUuidQName);
            final ResourceExpression resourceExpression = new ResourceExpression(basePath);
            final StringExpression versionNameExpression = new StringExpression(versionNameQName);
            final StatusExpression statusExpression = new StatusExpression(); // throws exception on error
            return new SaxExpression[] { creatationDateExpression, resourceTypeExpression, lastModifiedExpression, lockExpression, md5ChecksumExpression, propertyExpression, repositoryUuidExpression, resourceExpression, versionNameExpression, statusExpression, };
        }

        private final List<Info> entries = new ArrayList<>();

        InfoExpression(final Resource basePath) {
            super(PATH, createExpressions(basePath));
        }

        @Override
        public Optional<List<Info>> getValue() {
            return Optional.of(entries);
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            final InfoImpl info = new InfoImpl();

            final Optional<String> creationDate = ((StringExpression) children[0]).getValue();
            creationDate.ifPresent(x -> {
                final Date createdDate = DateUtils.parseCreatedDate(x);
                info.setCreationDate(createdDate);
            });

            final Optional<Boolean> directory = ((ResourceTypeExpression) children[1]).getValue();
            final Boolean directoryValue = directory.get();
            info.setDirectory(directoryValue);

            final Optional<String> modificationDate = ((StringExpression) children[2]).getValue();
            modificationDate.ifPresent(x -> {
                final Date lastModifiedDate = DateUtils.parseLastModifiedDate(x);
                info.setLastModifiedDate(lastModifiedDate);
            });

            final Optional<String> token = ((StringExpression) children[3]).getValue();
            token.ifPresent(x -> {
                final LockToken lockToken = new LockToken(x);
                info.setLockToken(lockToken);
            });

            final Optional<String> hash = ((StringExpression) children[4]).getValue();
            hash.ifPresent(info::setMd5);

            final Optional<ResourceProperty[]> properties = ((PropertyExpression) children[5]).getValue();
            final ResourceProperty[] propertiesValue = properties.get();
            info.setProperties(propertiesValue);

            final Optional<String> uuid = ((StringExpression) children[6]).getValue();
            uuid.ifPresent(x -> {
                final UUID repositoryId = UUID.fromString(x);
                info.setRepositoryId(repositoryId);
            });

            final Optional<Resource> resource = ((ResourceExpression) children[7]).getValue();
            resource.ifPresent(info::setResource);

            final Optional<String> revision = ((StringExpression) children[8]).getValue();
            revision.ifPresent(x -> {
                final int intRevision = Integer.parseInt(x);
                final Revision create = Revision.create(intRevision);
                info.setRevision(create);
            });

            entries.add(info);

            children[5].clear();
        }
    }

    private static class InfoHandler extends AbstractSaxExpressionHandler<List<Info>> {

        InfoHandler(final Resource basePath) {
            super(new InfoExpression(basePath));
        }

        @Override
        public Optional<List<Info>> getValue() {
            return ((InfoExpression) expressions[0]).getValue();
        }
    }

    private static class PropertyExpression extends AbstractSaxExpression<ResourceProperty[]> {

        private static final QName[] PATH = { //
                new QName(XmlConstants.DAV_NAMESPACE, "propstat"), //
                new QName(XmlConstants.DAV_NAMESPACE, "prop"), //
                new QName(XMLConstants.NULL_NS_URI, "*") };

        private Set<ResourceProperty> properties = new TreeSet<>(ResourceProperty.TYPE_NAME_COMPARATOR);

        PropertyExpression() {
            super(PATH);
        }

        @Override
        public void clear() {
            properties = new TreeSet<>(ResourceProperty.TYPE_NAME_COMPARATOR);
        }

        @Override
        public Optional<ResourceProperty[]> getValue() {
            final ResourceProperty[] resourceProperties = new ResourceProperty[properties.size()];
            int i = 0;
            for (final ResourceProperty property : properties) {
                resourceProperties[i++] = property;
            }
            return Optional.of(resourceProperties);
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            final String propertyName = ResourcePropertyUtils.unescapedKeyNameXml(localName);
            if (XmlConstants.SUBVERSION_CUSTOM_NAMESPACE.equals(nameSpaceUri)) {
                final ResourceProperty property = new ResourceProperty(ResourceProperty.Type.SUBVERSION_CUSTOM, propertyName, text);
                properties.add(property);
                return;
            }

            if (XmlConstants.SUBVERSION_SVN_NAMESPACE.equals(nameSpaceUri)) {
                final ResourceProperty property = new ResourceProperty(ResourceProperty.Type.SUBVERSION_SVN, propertyName, text);
                properties.add(property);
                return;
            }
        }
    }

    private static class ResourceExpression extends AbstractSaxExpression<Resource> {

        private static final QName[] PATH = { //
                new QName(XmlConstants.DAV_NAMESPACE, "propstat"), //
                new QName(XmlConstants.DAV_NAMESPACE, "prop"), //
                new QName(XmlConstants.SUBVERSION_DAV_NAMESPACE, "baseline-relative-path") //
        };

        private final Resource basePath;

        private Optional<Resource> resource = Optional.empty();

        ResourceExpression(final Resource basePath) {
            super(PATH);
            this.basePath = basePath;
        }

        @Override
        public Optional<Resource> getValue() {
            return resource;
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            final Resource qualifiedResource = Resource.create(text);
            final String base = basePath.getValue();
            final String value = qualifiedResource.getValue();
            final String relative = StringUtils.removeStart(value, base);
            final Resource result = Resource.create(relative);
            resource = Optional.of(result);
        }

        @Override
        public void resetHandler() {
            resource = Optional.empty();
        }
    }

    private static class ResourceTypeExpression extends AbstractSaxExpression<Boolean> {

        private static final QName[] PATH = { //
                new QName(XmlConstants.DAV_NAMESPACE, "propstat"), //
                new QName(XmlConstants.DAV_NAMESPACE, "prop"), //
                new QName(XmlConstants.DAV_NAMESPACE, "resourcetype"), //
                new QName(XmlConstants.DAV_NAMESPACE, "collection") //
        };

        private boolean folder = false;

        protected ResourceTypeExpression() {
            super(PATH);
        }

        @Override
        public Optional<Boolean> getValue() {
            return Optional.of(folder);
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            folder = true;
        }

        @Override
        public void resetHandler() {
            folder = false;
        }
    }

    private static class StatusExpression extends AbstractSaxExpression<Void> {

        public static final QName[] PATH = { //
                new QName(XmlConstants.DAV_NAMESPACE, "propstat"), //
                new QName(XmlConstants.DAV_NAMESPACE, "status") //
        };

        StatusExpression() {
            super(PATH);
        }

        @Override
        public Optional<Void> getValue() {
            throw new UnsupportedOperationException("no value available");
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            if (!"HTTP/1.1 200 OK".equals(text)) {
                throw new SubversionException("properties are missing");
            }
        }
    }

    private static class StringExpression extends AbstractSaxExpression<String> {

        private static final QName PROP = new QName(XmlConstants.DAV_NAMESPACE, "prop");

        private static final QName PROPSTAT = new QName(XmlConstants.DAV_NAMESPACE, "propstat");

        private static QName[] prefix(final QName... path) {
            final QName[] prefixPath = new QName[path.length + 2];
            int i = 0;
            prefixPath[i++] = PROPSTAT;
            prefixPath[i++] = PROP;
            for (final QName name : path) {
                prefixPath[i++] = name;
            }
            return prefixPath;
        }

        private Optional<String> value = Optional.empty();

        protected StringExpression(final QName... path) {
            super(prefix(path));
        }

        @Override
        public Optional<String> getValue() {
            return value;
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            value = Optional.ofNullable(text);
        }

        @Override
        public void resetHandler() {
            value = Optional.empty();
        }
    }

    /**
     * Reads status information for a single revision of a resource from the given {@link java.io.InputStream}.
     *
     * @param in
     *            {@link java.io.InputStream} from which the status information is read (Note: will not be closed)
     *
     * @return {@link InfoImpl} for the resource
     */
    static Info read(final InputStream in, final Resource basePath) throws IOException {
        final List<Info> infoSet = readAll(in, basePath);
        if (infoSet.isEmpty()) {
            throw new SubversionException("Invalid server response: expected content is missing");
        }
        return infoSet.get(0);
    }

    /**
     * Reads a {@link java.util.SortedSet} of status information for a single revision of various resources from the given {@link java.io.InputStream}.
     *
     * @param inputStream
     *            {@link java.io.InputStream} from which the status information is read (Note: will not be closed)
     *
     * @return {@link InfoImpl} for the resources
     */
    static List<Info> readAll(final InputStream inputStream, final Resource basePath) throws IOException {
        final InfoHandler handler = new InfoHandler(basePath);
        try {
            final InputStream escapedStream = ResourcePropertyUtils.escapedInputStream(inputStream);
            final Optional<List<Info>> list = handler.parse(escapedStream);
            return list.get();
        } catch (final ParserConfigurationException | SAXException e) {
            throw new SubversionException("Invalid server response: could not parse response", e);
        }
    }

    private InfoImplReader() {
        // prevent instantiation
    }
}
