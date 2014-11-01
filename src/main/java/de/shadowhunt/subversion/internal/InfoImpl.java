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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.ResourceProperty.Type;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.xml.AbstractSaxExpression;
import de.shadowhunt.subversion.xml.AbstractSaxExpressionHandler;
import de.shadowhunt.subversion.xml.SaxExpression;

/**
 * Default implementation for {@link Info}
 */
final class InfoImpl implements Info {

    private static final ResourceProperty[] EMPTY = new ResourceProperty[0];

    private ResourceProperty[] properties = EMPTY;

    private static class InfoExpression extends AbstractSaxExpression<SortedSet<Info>> {

        private static final QName[] PATH = { //
                new QName(XmlConstants.DAV_NAMESPACE, "multistatus"), //
                new QName(XmlConstants.DAV_NAMESPACE, "response") //
        };

        private static SaxExpression[] createExpressions(final String base, final String marker) {
            return new SaxExpression[] { //
                    new StringExpression(new QName(XmlConstants.DAV_NAMESPACE, "creationdate")), //
                    new ResourceTypeExpression(),
                    new StringExpression(new QName(XmlConstants.DAV_NAMESPACE, "getlastmodified")), //
                    new StringExpression(new QName(XmlConstants.DAV_NAMESPACE, "lockdiscovery"), new QName(XmlConstants.DAV_NAMESPACE, "activelock"), new QName(XmlConstants.DAV_NAMESPACE, "locktoken"), new QName(XmlConstants.DAV_NAMESPACE, "href")),
                    new StringExpression(new QName(XmlConstants.SVN_DAV_NAMESPACE, "md5-checksum")), //
                    new PropertyExpression(), //
                    new StringExpression(new QName(XmlConstants.SVN_DAV_NAMESPACE, "repository-uuid")), //
                    new ResourceExpression(base, marker), //
                    new StringExpression(new QName(XmlConstants.DAV_NAMESPACE, "version-name")), //

                    new StatusExpression(), // throws exception on error
            };
        }

        private final SortedSet<Info> entries = new TreeSet<Info>(Info.RESOURCE_COMPARATOR);

        InfoExpression(final String base, final String marker) {
            super(PATH, createExpressions(base, marker));
        }

        @Override
        public SortedSet<Info> getValue() {
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
            info.setRepositoryId(UUID.fromString(((StringExpression) children[6]).getValue()));
            info.setResource(((ResourceExpression) children[7]).getValue());
            final int revision = Integer.parseInt(((StringExpression) children[8]).getValue());
            info.setRevision(Revision.create(revision));
            entries.add(info);

            children[5].clear();
        }
    }

    private static class InfoHandler extends AbstractSaxExpressionHandler<SortedSet<Info>> {

        InfoHandler(final String base, final String marker) {
            super(new InfoExpression(base, marker));
        }

        @Override
        public SortedSet<Info> getValue() {
            return ((InfoExpression) expressions[0]).getValue();
        }
    }

    private static class PropertyExpression extends AbstractSaxExpression<ResourceProperty[]> {

        private static final QName[] PATH = { //
                new QName(XmlConstants.DAV_NAMESPACE, "propstat"), //
                new QName(XmlConstants.DAV_NAMESPACE, "prop"), //
                new QName(XMLConstants.NULL_NS_URI, "*")
        };

        private Set<ResourceProperty> properties = new TreeSet<ResourceProperty>(ResourceProperty.TYPE_NAME_COMPARATOR);

        PropertyExpression() {
            super(PATH);
        }

        public void clear() {
            properties = new TreeSet<ResourceProperty>(ResourceProperty.TYPE_NAME_COMPARATOR);
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
            if (XmlConstants.CUSTOM_PROPERTIES_NAMESPACE.equals(nameSpaceUri)) {
                properties.add(new ResourceProperty(Type.CUSTOM, localName, text));
                return;
            }

            if (XmlConstants.SVN_PROPERTIES_NAMESPACE.equals(nameSpaceUri)) {
                properties.add(new ResourceProperty(Type.SVN, localName, text));
                return;
            }
        }
    }

    private static class ResourceExpression extends AbstractSaxExpression<Resource> {

        private static final QName[] PATH = { //
                new QName(XmlConstants.DAV_NAMESPACE, "propstat"), //
                new QName(XmlConstants.DAV_NAMESPACE, "prop"), //
                new QName(XmlConstants.DAV_NAMESPACE, "checked-in"), //
                new QName(XmlConstants.DAV_NAMESPACE, "href") //
        };

        private final String base;

        private final String marker;

        private Resource resource = null;

        ResourceExpression(final String base, final String marker) {
            super(PATH);
            this.base = base;
            this.marker = marker;
        }

        @Override
        public Resource getValue() {
            return resource;
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            final String path = StringUtils.removeStart(text, base);

            if (path.startsWith(marker)) {
                int part = marker.length() + 1;
                for (int i = 0; i < 2; i++) {
                    part = path.indexOf(Resource.SEPARATOR_CHAR, part) + 1;
                }
                resource = Resource.create(path.substring(part));
            } else {
                resource = Resource.create(path);
            }
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
            return null;
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            if (!"HTTP/1.1 200 OK".equals(text)) {
                throw new SubversionException("properties are missing");
            }
        }
    }

    private static class StringExpression extends AbstractSaxExpression<String> {

        private String value = null;

        private static final QName PROPSTAT = new QName(XmlConstants.DAV_NAMESPACE, "propstat");

        private static final QName PROP = new QName(XmlConstants.DAV_NAMESPACE, "prop");

        private static final QName[] prefix(final QName... path) {
            final QName[] prefixPath = new QName[path.length + 2];
            int i=0;
            prefixPath[i++] = PROPSTAT;
            prefixPath[i++] = PROP;
            for (final QName name : path) {
                prefixPath[i++] = name;
            }
            return prefixPath;
        }

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
     * Reads status information for a single revision of a resource from the given {@link InputStream}
     *
     * @param in {@link java.io.InputStream} from which the status information is read (Note: will not be closed)
     * @param parser {@link de.shadowhunt.subversion.internal.VersionParser} that is used to retrieve the version information from the server response
     * @param base base path of the repository
     * @param marker path marker for special subversion directory (default: !svn)
     *
     * @return {@link InfoImpl} for the resource
     */
    static Info read(final InputStream in, final VersionParser parser, final String base, final String marker) {
        final SortedSet<Info> infoSet = readAll(in, parser, base, marker);
        if (infoSet.isEmpty()) {
            throw new SubversionException("Invalid server response: expected content is missing");
        }
        return infoSet.first();
    }

    /**
     * Reads a {@link SortedSet} of status information for a single revision of various resources from the given {@link InputStream}
     *
     * @param inputStream {@link InputStream} from which the status information is read (Note: will not be closed)
     * @param parser {@link VersionParser} that is used to retrieve the version information from the server response
     * @param base base path of the repository
     * @param marker path marker for special subversion directory (default: !svn)
     *
     * @return {@link InfoImpl} for the resources
     */
    static SortedSet<Info> readAll(final InputStream inputStream, final VersionParser parser, final String base, final String marker) {
        try {
            final InfoHandler handler = new InfoHandler(base, marker);
            return handler.parse(inputStream);
        } catch (final Exception e) {
            throw new SubversionException("Invalid server response: could not parse response", e);
        }
    }

    private Date creationDate = null;

    private boolean directory = false;

    private Date lastModifiedDate = null;

    // NOTE: not part of xml response but determined by a response header
    private String lockOwner = null;

    private String lockToken = null;

    private String md5 = null;

    private UUID repositoryId = null;

    private Resource resource = null;

    private Revision revision = null;

    InfoImpl() {
        // prevent direct instantiation
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InfoImpl)) {
            return false;
        }

        final InfoImpl info = (InfoImpl) o;

        if (!repositoryId.equals(info.repositoryId)) {
            return false;
        }
        if (!resource.equals(info.resource)) {
            return false;
        }
        if (!revision.equals(info.revision)) {
            return false;
        }

        return true;
    }

    @Override
    public Date getCreationDate() {
        return (creationDate == null) ? null : new Date(creationDate.getTime());
    }

    public Date getLastModifiedDate() {
        return (lastModifiedDate == null) ? null : new Date(lastModifiedDate.getTime());
    }

    @Override
    public String getLockOwner() {
        return lockOwner;
    }

    @Override
    public String getLockToken() {
        return lockToken;
    }

    @Override
    public String getMd5() {
        return md5;
    }

    @Override
    public ResourceProperty[] getProperties() {
        return Arrays.copyOf(properties, properties.length);
    }

    @Override
    public UUID getRepositoryId() {
        return repositoryId;
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public Revision getRevision() {
        return revision;
    }

    @Override
    public int hashCode() {
        int result = repositoryId.hashCode();
        result = 31 * result + resource.hashCode();
        result = 31 * result + revision.hashCode();
        return result;
    }

    @Override
    public boolean isDirectory() {
        return directory;
    }

    @Override
    public boolean isFile() {
        return !directory;
    }

    @Override
    public boolean isLocked() {
        return lockToken != null;
    }

    void setCreationDate(@Nullable final Date creationDate) {
        this.creationDate = creationDate;
    }

    void setDirectory(final boolean directory) {
        this.directory = directory;
    }

    void setLastModifiedDate(@Nullable final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    void setLockOwner(@Nullable final String lockOwner) {
        this.lockOwner = lockOwner;
    }

    void setLockToken(@Nullable final String lockToken) {
        this.lockToken = lockToken;
    }

    void setMd5(@Nullable final String md5) {
        this.md5 = md5;
    }

    void setProperties(@Nullable final ResourceProperty[] properties) {
        if ((properties == null) || (properties.length == 0)) {
            this.properties = EMPTY;
        } else {
            this.properties = Arrays.copyOf(properties, properties.length);
        }
    }

    void setRepositoryId(final UUID repositoryId) {
        this.repositoryId = repositoryId;
    }

    void setResource(final Resource resource) {
        this.resource = resource;
    }

    void setRevision(final Revision revision) {
        this.revision = revision;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Info [directory=");
        builder.append(directory);
        builder.append(", lockOwner=");
        builder.append(lockOwner);
        builder.append(", lockToken=");
        builder.append(lockToken);
        builder.append(", md5=");
        builder.append(md5);
        builder.append(", properties=");
        builder.append(Arrays.toString(properties));
        builder.append(", repositoryId=");
        builder.append(repositoryId);
        builder.append(", resource=");
        builder.append(resource);
        builder.append(", revision=");
        builder.append(revision);
        builder.append(']');
        return builder.toString();
    }
}
