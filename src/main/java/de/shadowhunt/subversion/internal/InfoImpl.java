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

import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.ResourceProperty.Type;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;

/**
 * Default implementation for {@link Info}
 */
final class InfoImpl implements Info {

    private static final ResourceProperty[] EMPTY = new ResourceProperty[0];

    private static class SubversionInfoHandler extends BasicHandler {

        private boolean checkedin = false;

        private InfoImpl current = null;

        private final SortedSet<InfoImpl> infos = new TreeSet<InfoImpl>(Info.RESOURCE_COMPARATOR);

        private boolean locktoken = false;

        private final VersionParser parser;

        private Set<ResourceProperty> properties = null;

        private boolean resourceType = false;

        SubversionInfoHandler(final VersionParser parser) {
            this.parser = parser;
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) {
            final String name = getNameFromQName(qName);

            if (current == null) {
                return;
            }

            if ("response".equals(name)) {
                if (current.getResource() != null) {
                    current.setProperties(properties.toArray(new ResourceProperty[properties.size()]));
                    infos.add(current);
                }
                properties = null;
                current = null;
                return;
            }

            if ("baseline-relative-path".equals(name)) {
                final Resource resource = Resource.create(getText());
                current.setResource(resource);
                return;
            }

            if (resourceType && "collection".equals(name)) {
                current.setDirectory(true);
                resourceType = false;
                return;
            }

            if (checkedin && "href".equals(name)) {
                final String text = getText();
                final Revision revision = parser.getRevisionFromPath(text);
                current.setRevision(revision);
                checkedin = false;
                return;
            }

            if (locktoken && "href".equals(name)) {
                current.setLockToken(getText());
                locktoken = false;
                return;
            }

            if ("md5-checksum".equals(name)) {
                current.setMd5(getText());
                return;
            }

            if ("repository-uuid".equals(name)) {
                current.setRepositoryId(UUID.fromString(getText()));
                return;
            }

            final String namespace = getNamespaceFromQName(qName);
            if ("C".equals(namespace)) {
                final ResourceProperty property = new ResourceProperty(Type.CUSTOM, name, getText());
                properties.add(property);
                return;
            }
            if ("S".equals(namespace)) {
                final ResourceProperty property = new ResourceProperty(Type.SVN, name, getText());
                properties.add(property);
                return;
            }
        }

        SortedSet<InfoImpl> getInfos() {
            return infos;
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
            clearText();

            final String name = getNameFromQName(qName);

            if ("checked-in".equals(name)) {
                checkedin = true;
                return;
            }

            if ("response".equals(name)) {
                current = new InfoImpl();
                locktoken = false;
                resourceType = false;
                properties = new TreeSet<ResourceProperty>(ResourceProperty.TYPE_NAME_COMPARATOR);
                return;
            }

            if ("locktoken".equals(name)) {
                locktoken = true;
                return;
            }

            if ("resourcetype".equals(name)) {
                resourceType = true;
                return;
            }
        }
    }

    /**
     * Reads status information for a single revision of a resource from the given {@link InputStream}
     *
     * @param in {@link InputStream} from which the status information is read (Note: will not be closed)
     * @param parser {@link VersionParser} that is used to retrieve the version information from the server response
     *
     * @return {@link InfoImpl} for the resource
     */
    static InfoImpl read(final InputStream in, final VersionParser parser) {
        final SortedSet<InfoImpl> infos = readAll(in, parser);
        if (infos.isEmpty()) {
            throw new SubversionException("Invalid server response: expected content is missing");
        }
        return infos.first();
    }

    /**
     * Reads a {@link SortedSet} of status information for a single revision of various resources from the given {@link InputStream}
     *
     * @param in {@link InputStream} from which the status information is read (Note: will not be closed)
     * @param parser {@link VersionParser} that is used to retrieve the version information from the server response
     *
     * @return {@link InfoImpl} for the resources
     */
    static SortedSet<InfoImpl> readAll(final InputStream in, final VersionParser parser) {
        try {
            final SAXParser saxParser = BasicHandler.FACTORY.newSAXParser();
            final SubversionInfoHandler handler = new SubversionInfoHandler(parser);

            saxParser.parse(in, handler);
            return handler.getInfos();
        } catch (final Exception e) {
            throw new SubversionException("Invalid server response: could not parse response", e);
        }
    }

    private boolean directory = false;

    // NOTE: not part of xml response but determined by a response header
    private String lockOwner = null;

    private String lockToken = null;

    private String md5 = null;

    private ResourceProperty[] properties = EMPTY;

    private UUID repositoryId = null;

    private Resource resource = null;

    private Revision revision = null;

    InfoImpl() {
        // prevent direct instantiation
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final InfoImpl other = (InfoImpl) obj;
        if (directory != other.directory) {
            return false;
        }
        if (lockOwner == null) {
            if (other.lockOwner != null) {
                return false;
            }
        } else if (!lockOwner.equals(other.lockOwner)) {
            return false;
        }
        if (lockToken == null) {
            if (other.lockToken != null) {
                return false;
            }
        } else if (!lockToken.equals(other.lockToken)) {
            return false;
        }
        if (md5 == null) {
            if (other.md5 != null) {
                return false;
            }
        } else if (!md5.equals(other.md5)) {
            return false;
        }
        if (repositoryId == null) {
            if (other.repositoryId != null) {
                return false;
            }
        } else if (!repositoryId.equals(other.repositoryId)) {
            return false;
        }
        if (resource == null) {
            if (other.resource != null) {
                return false;
            }
        } else if (!resource.equals(other.resource)) {
            return false;
        }
        if (revision == null) {
            if (other.revision != null) {
                return false;
            }
        } else if (!revision.equals(other.revision)) {
            return false;
        }
        return true;
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
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (directory ? 1231 : 1237);
        result = (prime * result) + ((lockOwner == null) ? 0 : lockOwner.hashCode());
        result = (prime * result) + ((lockToken == null) ? 0 : lockToken.hashCode());
        result = (prime * result) + ((md5 == null) ? 0 : md5.hashCode());
        result = (prime * result) + ((repositoryId == null) ? 0 : repositoryId.hashCode());
        result = (prime * result) + ((resource == null) ? 0 : resource.hashCode());
        result = (prime * result) + ((revision == null) ? 0 : revision.hashCode());
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

    void setDirectory(final boolean directory) {
        this.directory = directory;
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
