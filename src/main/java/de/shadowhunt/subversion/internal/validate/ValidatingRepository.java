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
package de.shadowhunt.subversion.internal.validate;

import java.io.InputStream;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.ResourceProperty.Key;
import de.shadowhunt.subversion.ResourceProperty.Type;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Transaction;
import org.apache.commons.lang3.Validate;

public class ValidatingRepository extends ValidatingReadOnlyRepository implements Repository {

    private final Repository delegate;

    public ValidatingRepository(final Repository delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public final void add(final Transaction transaction, final Resource resource, final boolean parents, final InputStream content) {
        validateTransaction(transaction);
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(content, "content must not be null");
        delegate.add(transaction, resource, parents, content);
    }

    @Override
    public final void commit(final Transaction transaction, final String message, final boolean releaseLocks) {
        delegate.commit(transaction, message, releaseLocks);
    }

    @Override
    public final void copy(final Transaction transaction, final Resource sourceResource, final Revision sourceRevision, final Resource targetResource, final boolean parents) {
        validateTransaction(transaction);
        Validate.notNull(sourceResource, "sourceResource must not be null");
        Validate.notNull(sourceRevision, "sourceRevision must not be null");
        Validate.notNull(targetResource, "targetResource must not be null");
        validateRevision(transaction, sourceRevision);
        delegate.copy(transaction, sourceResource, sourceRevision, targetResource, parents);
    }

    @Override
    public final Transaction createTransaction() {
        return delegate.createTransaction();
    }

    @Override
    public final void delete(final Transaction transaction, final Resource resource) {
        validateTransaction(transaction);
        Validate.notNull(resource, "resource must not be null");
        delegate.delete(transaction, resource);
    }

    @Override
    public final void lock(final Resource resource, final boolean steal) {
        Validate.notNull(resource, "resource must not be null");
        delegate.lock(resource, steal);
    }

    @Override
    public final void mkdir(final Transaction transaction, final Resource resource, final boolean parents) {
        validateTransaction(transaction);
        Validate.notNull(resource, "resource must not be null");
        delegate.mkdir(transaction, resource, parents);
    }

    @Override
    public final void move(final Transaction transaction, final Resource sourceResource, final Resource targetResource, final boolean parents) {
        validateTransaction(transaction);
        Validate.notNull(sourceResource, "sourceResource must not be null");
        Validate.notNull(targetResource, "targetResource must not be null");
        delegate.move(transaction, sourceResource, targetResource, parents);
    }

    @Override
    public final void propertiesDelete(final Transaction transaction, final Resource resource, final ResourceProperty... properties) {
        validateTransaction(transaction);
        Validate.notNull(resource, "resource must not be null");
        validateProperties(properties);
        delegate.propertiesDelete(transaction, resource, properties);
    }

    @Override
    public final void propertiesSet(final Transaction transaction, final Resource resource, final ResourceProperty... properties) {
        validateTransaction(transaction);
        Validate.notNull(resource, "resource must not be null");
        validateProperties(properties);
        delegate.propertiesSet(transaction, resource, properties);
    }

    @Override
    public final void rollback(final Transaction transaction) {
        validateTransaction(transaction);
        delegate.rollback(transaction);
    }

    @Override
    public final void rollbackIfNotCommitted(final Transaction transaction) {
        Validate.notNull(transaction, "transaction must not be null");
        delegate.rollbackIfNotCommitted(transaction);
    }

    @Override
    public final void unlock(final Resource resource, final boolean force) {
        Validate.notNull(resource, "resource must not be null");
        delegate.unlock(resource, force);
    }

    protected final void validateProperties(final ResourceProperty... properties) {
        Validate.notNull(properties, "properties must not be null");
        for (final ResourceProperty property : properties) {
            final Key key = property.getKey();
            final Type type = key.getType();
            if ((type != Type.SUBVERSION_CUSTOM) && (type != Type.SUBVERSION_SVN)) {
                throw new SubversionException("properties must only contain SUBVERSION_CUSTOM or SUBVERSION_SVN elements");
            }
        }
    }

    protected final void validateTransaction(final Transaction transaction) {
        Validate.notNull(transaction, "transaction must not be null");

        validateView(transaction);
        if (!transaction.isActive()) {
            throw new SubversionException("Transaction invalid: has already been committed or reverted");
        }
    }
}
