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

import java.io.InputStream;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TracingRepository extends TracingReadOnlyRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(TracingRepository.class);

    private final Repository delegate;

    TracingRepository(final Repository delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    public void add(final Transaction transaction, final Resource resource, final boolean parents, final InputStream content) {
        delegate.add(transaction, resource, parents, content);
    }

    public void commit(final Transaction transaction, final String message, final boolean releaseLocks) {
        delegate.commit(transaction, message, releaseLocks);
    }

    public void copy(final Transaction transaction, final Resource srcResource, final Revision srcRevision, final Resource targetResource, final boolean parents) {
        delegate.copy(transaction, srcResource, srcRevision, targetResource, parents);
    }

    public Transaction createTransaction() {
        return delegate.createTransaction();
    }

    public void delete(final Transaction transaction, final Resource resource) {
        delegate.delete(transaction, resource);
    }

    public void lock(final Resource resource, final boolean steal) {
        delegate.lock(resource, steal);
    }

    public void mkdir(final Transaction transaction, final Resource resource, final boolean parents) {
        delegate.mkdir(transaction, resource, parents);
    }

    public void move(final Transaction transaction, final Resource srcResource, final Resource targetResource, final boolean parents) {
        delegate.move(transaction, srcResource, targetResource, parents);
    }

    public void propertiesDelete(final Transaction transaction, final Resource resource, final ResourceProperty... properties) {
        delegate.propertiesDelete(transaction, resource, properties);
    }

    public void propertiesSet(final Transaction transaction, final Resource resource, final ResourceProperty... properties) {
        delegate.propertiesSet(transaction, resource, properties);
    }

    public void rollback(final Transaction transaction) {
        delegate.rollback(transaction);
    }

    public void rollbackIfNotCommitted(final Transaction transaction) {
        delegate.rollbackIfNotCommitted(transaction);
    }

    public void unlock(final Resource resource, final boolean force) {
        delegate.unlock(resource, force);
    }
}
