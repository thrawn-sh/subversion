/**
 * Copyright (C) 2013 shadowhunt (dev@shadowhunt.de)
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
package de.shadowhunt.subversion.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.xml.AbstractSaxExpression;
import de.shadowhunt.subversion.xml.AbstractSaxExpressionHandler;
import de.shadowhunt.subversion.xml.SaxExpression;

import org.xml.sax.SAXException;

final class InfoImplReader {

    private static class InfoExpression extends AbstractSaxExpression<List<Info>> {

        private static final QName[] PATH = { //
                new QName(XmlConstants.DAV_NAMESPACE, "multistatus"), //
                new QName(XmlConstants.DAV_NAMESPACE, "response") //
        };

        private static SaxExpression[] createExpressions() {
            return new SaxExpression[] { //
                    new StringExpression(new QName(XmlConstants.DAV_NAMESPACE, "creationdate")), //
                    new ResourceTypeExpression(),
                    new StringExpression(new QName(XmlConstants.DAV_NAMESPACE, "getlastmodified")), //
                    new StringExpression(new QName(XmlConstants.DAV_NAMESPACE, "lockdiscovery"), new QName(XmlConstants.DAV_NAMESPACE, "activelock"), new QName(XmlConstants.DAV_NAMESPACE, "locktoken"), new QName(XmlConstants.DAV_NAMESPACE, "href")),
                    new StringExpression(new QName(XmlConstants.SUBVERSION_DAV_NAMESPACE, "md5-checksum")), //
                    new PropertyExpression(), //
                    new StringExpression(new QName(XmlConstants.SUBVERSION_DAV_NAMESPACE, "repository-uuid")), //
                    new ResourceExpression(), //
                    new StringExpression(new QName(XmlConstants.DAV_NAMESPACE, "version-name")), //

                    new StatusExpression(), // throws exception on error
            };
        }

        private final List<Info> entries = new ArrayList<>();

        InfoExpression() {
            super(PATH, createExpressions());
        }

        @Override
        public List<Info> getValue() {
            return entries;
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            final InfoImpl info = new InfoImpl();
            info.setCreationDate(DateUtils.parseCreatedDate(((StringExpression) children[0]).getValue()));
            info.setDirectory(((ResourceTypeExpression) children[1]).getValue());
            info.setLastModifiedDate(DateUtils.parseLastModifiedDate(((StringExpression) children[2]).getValue()));
            info.setLockToken(((StringExpression) children[3]).getValue());
            info.setMd5(((StringExpression) children[4]).getValue());
            info.setProperties(((PropertyExpression) children[5]).getValue());
            final String uuid = ((StringExpression) children[6]).getValue();
            if (uuid != null) {
                info.setRepositoryId(UUID.fromString(uuid));
            }
            info.setResource(((ResourceExpression) children[7]).getValue());
            final String revision = ((StringExpression) children[8]).getValue();
            if (revision != null) {
                info.setRevision(Revision.create(Integer.parseInt(revision)));
            }
            entries.add(info);

            children[5].clear();
        }
    }

    private static class InfoHandler extends AbstractSaxExpressionHandler<List<Info>> {

        InfoHandler() {
            super(new InfoExpression());
        }

        @Override
        public List<Info> getValue() {
            return ((InfoExpression) expressions[0]).getValue();
        }
    }

    private static class PropertyExpression extends AbstractSaxExpression<ResourceProperty[]> {

        private static final QName[] PATH = { //
                new QName(XmlConstants.DAV_NAMESPACE, "propstat"), //
                new QName(XmlConstants.DAV_NAMESPACE, "prop"), //
                new QName(XMLConstants.NULL_NS_URI, "*")
        };

        private Set<ResourceProperty> properties = new TreeSet<>(ResourceProperty.TYPE_NAME_COMPARATOR);

        PropertyExpression() {
            super(PATH);
        }

        public void clear() {
            properties = new TreeSet<>(ResourceProperty.TYPE_NAME_COMPARATOR);
        }

        @Override
        public ResourceProperty[] getValue() {
            final ResourceProperty[] resourceProperties = new ResourceProperty[properties.size()];
            int i = 0;
            for (final ResourceProperty property : properties) {
                resourceProperties[i++] = property;
            }
            return resourceProperties;
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            final String propertyName = ResourcePropertyUtils.unescapedKeyNameXml(localName);
            if (XmlConstants.SUBVERSION_CUSTOM_NAMESPACE.equals(nameSpaceUri)) {
                properties.add(new ResourceProperty(ResourceProperty.Type.SUBVERSION_CUSTOM, propertyName, text));
                return;
            }

            if (XmlConstants.SUBVERSION_SVN_NAMESPACE.equals(nameSpaceUri)) {
                properties.add(new ResourceProperty(ResourceProperty.Type.SUBVERSION_SVN, propertyName, text));
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

        private static Resource createResource(final String escapedPath) {
            return Resource.create(escapedPath);
        }

        private Resource resource = null;

        ResourceExpression() {
            super(PATH);
        }

        @Override
        public Resource getValue() {
            return resource;
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            resource = createResource(text);
        }

        @Override
        public void resetHandler() {
            resource = null;
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
        public Boolean getValue() {
            return folder;
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
                new QName(XmlConstants.DAV_NAMESPACE, "status")  //
        };

        StatusExpression() {
            super(PATH);
        }

        @Override
        public Void getValue() {
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

        private String value = null;

        protected StringExpression(final QName... path) {
            super(prefix(path));
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            value = text;
        }

        @Override
        public void resetHandler() {
            value = null;
        }
    }

    /**
     * Reads status information for a single revision of a resource from the given {@link java.io.InputStream}.
     *
     * @param in {@link java.io.InputStream} from which the status information is read (Note: will not be closed)
     *
     * @return {@link InfoImpl} for the resource
     */
    static Info read(final InputStream in) throws IOException {
        final List<Info> infoSet = readAll(in);
        if (infoSet.isEmpty()) {
            throw new SubversionException("Invalid server response: expected content is missing");
        }
        return infoSet.get(0);
    }

    /**
     * Reads a {@link java.util.SortedSet} of status information for a single revision of various resources from the
     * given {@link java.io.InputStream}.
     *
     * @param inputStream {@link java.io.InputStream} from which the status information is read (Note: will not be
     * closed)
     *
     * @return {@link InfoImpl} for the resources
     */
    static List<Info> readAll(final InputStream inputStream) throws IOException {
        final InfoHandler handler = new InfoHandler();
        try {
            final InputStream escapedStream = ResourcePropertyUtils.escapedInputStream(inputStream);
            return handler.parse(escapedStream);
        } catch (final ParserConfigurationException | SAXException e) {
            throw new SubversionException("Invalid server response: could not parse response", e);
        }
    }

    private InfoImplReader() {
        // prevent instantiation
    }
}
