/**
 * Copyright © 2013-2018 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.shadowhunt.subversion.internal;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.LockToken;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;

/**
 * Default implementation for {@link Info}.
 */
final class InfoImpl implements Info {

    private static final ResourceProperty[] EMPTY = new ResourceProperty[0];

    private Date creationDate = null;

    private boolean directory = false;

    private Date lastModifiedDate = null;

    // NOTE: not part of xml response but determined by a response header
    private String lockOwner;

    private LockToken lockToken;

    private String md5;

    private ResourceProperty[] properties = EMPTY;

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
        return new Date(creationDate.getTime());
    }

    @Override
    public Date getLastModifiedDate() {
        return new Date(lastModifiedDate.getTime());
    }

    @Override
    public Optional<String> getLockOwner() {
        return Optional.ofNullable(lockOwner);
    }

    @Override
    public Optional<LockToken> getLockToken() {
        return Optional.ofNullable(lockToken);
    }

    @Override
    public Optional<String> getMd5() {
        return Optional.ofNullable(md5);
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

    void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    void setDirectory(final boolean directory) {
        this.directory = directory;
    }

    void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    void setLockOwner(final String lockOwner) {
        this.lockOwner = lockOwner;
    }

    void setLockToken(final LockToken lockToken) {
        this.lockToken = lockToken;
    }

    void setMd5(final String md5) {
        this.md5 = md5;
    }

    void setProperties(final ResourceProperty... properties) {
        if (properties.length == 0) {
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
