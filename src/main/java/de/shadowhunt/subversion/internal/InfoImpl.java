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

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.LockToken;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;

public final class InfoImpl implements Info {

    private Date creationDate;

    private boolean directory;

    private Date lastModifiedDate;

    private String lockOwner;

    private LockToken lockToken;

    private String md5;

    private ResourceProperty[] properties = new ResourceProperty[0];

    private UUID repositoryId;

    private Resource resource;

    private Revision revision;

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
    public Date getCreationDate() {
        if (creationDate == null) {
            throw new SubversionException("access to incomplete Info");
        }
        final long time = creationDate.getTime();
        return new Date(time);
    }

    @Override
    public Date getLastModifiedDate() {
        if (lastModifiedDate == null) {
            throw new SubversionException("access to incomplete Info");
        }
        final long time = lastModifiedDate.getTime();
        return new Date(time);
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
        if (repositoryId == null) {
            throw new SubversionException("access to incomplete Info");
        }
        return repositoryId;
    }

    @Override
    public Resource getResource() {
        if (resource == null) {
            throw new SubversionException("access to incomplete Info");
        }
        return resource;
    }

    @Override
    public Revision getRevision() {
        if (revision == null) {
            throw new SubversionException("access to incomplete Info");
        }
        return revision;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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

    public void setCreationDate(final Date creationDate) {
        final long time = creationDate.getTime();
        this.creationDate = new Date(time);
    }

    public void setDirectory(final boolean directory) {
        this.directory = directory;
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        final long time = lastModifiedDate.getTime();
        this.lastModifiedDate = new Date(time);
    }

    public void setLockOwner(final String lockOwner) {
        this.lockOwner = lockOwner;
    }

    public void setLockToken(final LockToken lockToken) {
        this.lockToken = lockToken;
    }

    public void setMd5(final String md5) {
        this.md5 = md5;
    }

    public void setProperties(final ResourceProperty... properties) {
        this.properties = Arrays.copyOf(properties, properties.length);
    }

    public void setRepositoryId(final UUID repositoryId) {
        this.repositoryId = repositoryId;
    }

    public void setResource(final Resource resource) {
        this.resource = resource;
    }

    public void setRevision(final Revision revision) {
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
        final String propertiesArray = Arrays.toString(properties);
        builder.append(propertiesArray);
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
