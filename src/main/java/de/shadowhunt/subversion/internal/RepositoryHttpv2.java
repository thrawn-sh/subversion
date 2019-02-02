/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2019 shadowhunt (dev@shadowhunt.de)
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
import java.net.URI;
import java.util.UUID;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.internal.action.Action;
import de.shadowhunt.subversion.internal.action.AddActionHttpv1;
import de.shadowhunt.subversion.internal.action.CommitActionHttpv2;
import de.shadowhunt.subversion.internal.action.CopyActionHttpv2;
import de.shadowhunt.subversion.internal.action.CreateTransactionActionHttpv2;
import de.shadowhunt.subversion.internal.action.DeleteActionHttpv1;
import de.shadowhunt.subversion.internal.action.LockActionHttpv1;
import de.shadowhunt.subversion.internal.action.MkdirActionHttpv2;
import de.shadowhunt.subversion.internal.action.MoveActionHttpv1;
import de.shadowhunt.subversion.internal.action.PropertiesUpdateActionHttpv1;
import de.shadowhunt.subversion.internal.action.RollbackActionHttpv1;
import de.shadowhunt.subversion.internal.action.UnlockActionHttpv1;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

public class RepositoryHttpv2 extends ReadOnlyRepositoryHttpv2 implements RepositoryInternal {

    public RepositoryHttpv2(final URI baseUri, final Resource basePath, final UUID repositoryId, final String prefix, final HttpClient client, final HttpContext context) {
        super(baseUri, basePath, repositoryId, prefix, client, context);
    }

    @Override
    public void add(final Transaction transaction, final Resource resource, final boolean parents, final InputStream content) {
        final TransactionInternal transactionInternal = TransactionInternal.from(transaction);
        final Action<Void> action = new AddActionHttpv1(this, transactionInternal, resource, parents, content);
        action.perform();
    }

    @Override
    public void commit(final Transaction transaction, final String message, final boolean releaseLocks) {
        final TransactionInternal transactionInternal = TransactionInternal.from(transaction);
        final Action<Void> action = new CommitActionHttpv2(this, transactionInternal, message, releaseLocks);
        action.perform();
    }

    @Override
    public void copy(final Transaction transaction, final Resource sourceResource, final Revision sourceRevision, final Resource targetResource, final boolean parents) {
        final TransactionInternal transactionInternal = TransactionInternal.from(transaction);
        final Action<Void> action = new CopyActionHttpv2(this, transactionInternal, sourceResource, sourceRevision, targetResource, parents);
        action.perform();
    }

    @Override
    public Transaction createTransaction() {
        final Action<TransactionInternal> action = new CreateTransactionActionHttpv2(this);
        return action.perform();
    }

    @Override
    public void delete(final Transaction transaction, final Resource resource) {
        final TransactionInternal transactionInternal = TransactionInternal.from(transaction);
        final Action<Void> action = new DeleteActionHttpv1(this, transactionInternal, resource);
        action.perform();
    }

    @Override
    public void lock(final Resource resource, final boolean steal) {
        final Action<Void> action = new LockActionHttpv1(this, resource, steal);
        action.perform();
    }

    @Override
    public void mkdir(final Transaction transaction, final Resource resource, final boolean parents) {
        final TransactionInternal transactionInternal = TransactionInternal.from(transaction);
        final Action<Void> action = new MkdirActionHttpv2(this, transactionInternal, resource, parents);
        action.perform();
    }

    @Override
    public void move(final Transaction transaction, final Resource sourceResource, final Resource targetResource, final boolean parents) {
        final TransactionInternal transactionInternal = TransactionInternal.from(transaction);
        final Action<Void> action = new MoveActionHttpv1(this, transactionInternal, sourceResource, targetResource, parents);
        action.perform();
    }

    @Override
    public void propertiesDelete(final Transaction transaction, final Resource resource, final ResourceProperty... properties) {
        final TransactionInternal transactionInternal = TransactionInternal.from(transaction);
        final Action<Void> action = new PropertiesUpdateActionHttpv1(this, transactionInternal, resource, false, properties);
        action.perform();
    }

    @Override
    public void propertiesSet(final Transaction transaction, final Resource resource, final ResourceProperty... properties) {
        final TransactionInternal transactionInternal = TransactionInternal.from(transaction);
        final Action<Void> action = new PropertiesUpdateActionHttpv1(this, transactionInternal, resource, true, properties);
        action.perform();
    }

    @Override
    public void rollback(final Transaction transaction) {
        final TransactionInternal transactionInternal = TransactionInternal.from(transaction);
        final Action<Void> action = new RollbackActionHttpv1(this, transactionInternal);
        action.perform();
    }

    @Override
    public void rollbackIfNotCommitted(final Transaction transaction) {
        if (transaction.isActive()) {
            rollback(transaction);
        }
    }

    @Override
    public void unlock(final Resource resource, final boolean force) {
        final Action<Void> action = new UnlockActionHttpv1(this, resource, force);
        action.perform();
    }

}
