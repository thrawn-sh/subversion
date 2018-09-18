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

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.View;
import org.apache.commons.lang3.Validate;

/**
 * Default implementation for {@link Transaction}.
 */
public final class TransactionImpl implements Transaction, View {

    private boolean active = true;

    private final Map<Resource, Status> changeSet = new TreeMap<>();

    private final Revision headRevision;

    private final String id;

    private final UUID repositoryId;

    public TransactionImpl(final String id, final UUID repositoryId, final Revision headRevision) {
        Validate.notNull(id, "id must not be null");
        Validate.notNull(repositoryId, "repositoryId must not be null");
        Validate.notNull(headRevision, "headRevision must not be null");

        this.id = id;
        this.repositoryId = repositoryId;
        this.headRevision = headRevision;
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
        final TransactionImpl other = (TransactionImpl) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (repositoryId == null) {
            if (other.repositoryId != null) {
                return false;
            }
        } else if (!repositoryId.equals(other.repositoryId)) {
            return false;
        }
        return true;
    }

    @Override
    public Map<Resource, Status> getChangeSet() {
        return new TreeMap<>(changeSet);
    }

    @Override
    public Revision getHeadRevision() {
        return headRevision;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public UUID getRepositoryId() {
        return repositoryId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((id == null) ? 0 : id.hashCode());
        result = (prime * result) + ((repositoryId == null) ? 0 : repositoryId.hashCode());
        return result;
    }

    @Override
    public void invalidate() {
        active = false;
        changeSet.clear();
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isChangeSetEmpty() {
        if (changeSet.isEmpty()) {
            return true;
        }
        for (final Status status : changeSet.values()) {
            if (status != Status.EXISTS) {
                return false;
            }
        }
        // no modifications to repository
        return true;
    }

    @Override
    public boolean register(final Resource resource, final Status status) {
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(status, "status must not be null");

        final Status old = changeSet.put(resource, status);
        if (old != null) {
            // if we delete an newly added resource, we remove completely
            // remove the resource from the change set
            if ((Status.ADDED == old) && (Status.DELETED == status)) {
                changeSet.remove(resource);
                return true;
            }

            // previous value had higher order, and must therefore be preserved
            if (old.order > status.order) {
                changeSet.put(resource, old);
                return false;
            }
        }
        return true;
    }

    public Status status(final Resource resource) {
        Validate.notNull(resource, "resource must not be null");

        final Status status = changeSet.get(resource);
        if (status != null) {
            return status;
        }
        return Status.NOT_TRACKED;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Transaction [");
        sb.append("id=");
        sb.append(id);
        sb.append(", repositoryId=");
        sb.append(repositoryId);
        sb.append(", active=");
        sb.append(active);
        sb.append(']');
        return sb.toString();
    }
}
