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
import org.apache.commons.lang3.Validate;

public abstract class AbstractTransaction extends ViewImpl implements TransactionInternal, ViewInternal {

    private boolean active = true;

    private final Map<Resource, Status> changeSet = new TreeMap<>();

    protected final String id;

    protected final Resource prefix;

    protected AbstractTransaction(final String id, final UUID repositoryId, final Revision headRevision, final Resource prefix) {
        super(repositoryId, headRevision);
        this.id = id;
        this.prefix = prefix;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractTransaction other = (AbstractTransaction) obj;
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
    public final Map<Resource, Status> getChangeSet() {
        synchronized (changeSet) {
            return new TreeMap<>(changeSet);
        }
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((id == null) ? 0 : id.hashCode());
        result = (prime * result) + ((repositoryId == null) ? 0 : repositoryId.hashCode());
        return result;
    }

    @Override
    public final void invalidate() {
        active = false;
        synchronized (changeSet) {
            changeSet.clear();
        }
    }

    @Override
    public final boolean isActive() {
        return active;
    }

    @Override
    public final boolean isChangeSetEmpty() {
        synchronized (changeSet) {
            for (final Status status : changeSet.values()) {
                if (status != Status.EXISTS) {
                    return false;
                }
            }
            // no modifications to repository
            return true;
        }
    }

    @Override
    public final boolean register(final Resource resource, final Status status) {
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(status, "status must not be null");

        synchronized (changeSet) {
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
    }

    @Override
    public final String toString() {
        return "Transaction [id=" + id + ", repositoryId=" + repositoryId + ", active=" + active + "]";
    }
}
