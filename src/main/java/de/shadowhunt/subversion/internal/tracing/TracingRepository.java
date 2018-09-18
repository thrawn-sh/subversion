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
package de.shadowhunt.subversion.internal.tracing;

import java.io.InputStream;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction;
import org.apache.commons.lang3.time.StopWatch;

public class TracingRepository extends TracingReadOnlyRepository implements Repository {

    private final Repository delegate;

    public TracingRepository(final Repository delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public final void add(final Transaction transaction, final Resource resource, final boolean parents, final InputStream content) {
        final String method = "add";
        final Class<? extends InputStream> contentClass = content.getClass();
        final StopWatch stopWatch = StopWatchLogger.INSTANCE.logStart(method, transaction, resource, parents, contentClass);
        try {
            delegate.add(transaction, resource, parents, content);
        } finally {
            StopWatchLogger.INSTANCE.logStop(method, stopWatch);
        }
    }

    @Override
    public final void commit(final Transaction transaction, final String message, final boolean releaseLocks) {
        final String method = "commit";
        final StopWatch stopWatch = StopWatchLogger.INSTANCE.logStart(method, transaction, message, releaseLocks);
        try {
            delegate.commit(transaction, message, releaseLocks);
        } finally {
            StopWatchLogger.INSTANCE.logStop(method, stopWatch);
        }
    }

    @Override
    public final void copy(final Transaction transaction, final Resource sourceResource, final Revision sourceRevision, final Resource targetResource, final boolean parents) {
        final String method = "copy";
        final StopWatch stopWatch = StopWatchLogger.INSTANCE.logStart(method, transaction, sourceResource, sourceRevision, targetResource, parents);
        try {
            delegate.copy(transaction, sourceResource, sourceRevision, targetResource, parents);
        } finally {
            StopWatchLogger.INSTANCE.logStop(method, stopWatch);
        }
    }

    @Override
    public final Transaction createTransaction() {
        return delegate.createTransaction();
    }

    @Override
    public final void delete(final Transaction transaction, final Resource resource) {
        final String method = "delete";
        final StopWatch stopWatch = StopWatchLogger.INSTANCE.logStart(method, transaction, resource);
        try {
            delegate.delete(transaction, resource);
        } finally {
            StopWatchLogger.INSTANCE.logStop(method, stopWatch);
        }
    }

    @Override
    public final void lock(final Resource resource, final boolean steal) {
        final String method = "lock";
        final StopWatch stopWatch = StopWatchLogger.INSTANCE.logStart(method, resource, steal);
        try {
            delegate.lock(resource, steal);
        } finally {
            StopWatchLogger.INSTANCE.logStop(method, stopWatch);
        }
    }

    @Override
    public final void mkdir(final Transaction transaction, final Resource resource, final boolean parents) {
        final String method = "mkdir";
        final StopWatch stopWatch = StopWatchLogger.INSTANCE.logStart(method, transaction, resource, parents);
        try {
            delegate.mkdir(transaction, resource, parents);
        } finally {
            StopWatchLogger.INSTANCE.logStop(method, stopWatch);
        }
    }

    @Override
    public final void move(final Transaction transaction, final Resource sourceResource, final Resource targetResource, final boolean parents) {
        final String method = "move";
        final StopWatch stopWatch = StopWatchLogger.INSTANCE.logStart(method, transaction, sourceResource, targetResource, parents);
        try {
            delegate.move(transaction, sourceResource, targetResource, parents);
        } finally {
            StopWatchLogger.INSTANCE.logStop(method, stopWatch);
        }
    }

    @Override
    public final void propertiesDelete(final Transaction transaction, final Resource resource, final ResourceProperty... properties) {
        final String method = "propertiesDelete";
        final StopWatch stopWatch = StopWatchLogger.INSTANCE.logStart(method, transaction, resource, properties);
        try {
            delegate.propertiesDelete(transaction, resource, properties);
        } finally {
            StopWatchLogger.INSTANCE.logStop(method, stopWatch);
        }
    }

    @Override
    public final void propertiesSet(final Transaction transaction, final Resource resource, final ResourceProperty... properties) {
        final String method = "propertiesSet";
        final StopWatch stopWatch = StopWatchLogger.INSTANCE.logStart(method, transaction, resource, properties);
        try {
            delegate.propertiesSet(transaction, resource, properties);
        } finally {
            StopWatchLogger.INSTANCE.logStop(method, stopWatch);
        }
    }

    @Override
    public final void rollback(final Transaction transaction) {
        final String method = "rollback";
        final StopWatch stopWatch = StopWatchLogger.INSTANCE.logStart(method, transaction);
        try {
            delegate.rollback(transaction);
        } finally {
            StopWatchLogger.INSTANCE.logStop(method, stopWatch);
        }
    }

    @Override
    public final void rollbackIfNotCommitted(final Transaction transaction) {
        final String method = "rollbackIfNotCommitted";
        final StopWatch stopWatch = StopWatchLogger.INSTANCE.logStart(method, transaction);
        try {
            delegate.rollbackIfNotCommitted(transaction);
        } finally {
            StopWatchLogger.INSTANCE.logStop(method, stopWatch);
        }
    }

    @Override
    public final void unlock(final Resource resource, final boolean force) {
        final String method = "unlock";
        final StopWatch stopWatch = StopWatchLogger.INSTANCE.logStart(method, resource, force);
        try {
            delegate.unlock(resource, force);
        } finally {
            StopWatchLogger.INSTANCE.logStop(method, stopWatch);
        }
    }
}
