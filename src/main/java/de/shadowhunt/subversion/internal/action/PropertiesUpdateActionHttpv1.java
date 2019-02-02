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
package de.shadowhunt.subversion.internal.action;

import java.util.Arrays;
import java.util.Optional;

import javax.annotation.CheckForNull;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.LockToken;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction.Status;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.RepositoryInternal;
import de.shadowhunt.subversion.internal.ResourcePropertyUtils;
import de.shadowhunt.subversion.internal.TransactionInternal;
import de.shadowhunt.subversion.internal.operation.Operation;
import de.shadowhunt.subversion.internal.operation.PropertiesUpdateOperationHttpv1;
import de.shadowhunt.subversion.internal.operation.SparseInfoOperationHttpv1;

public class PropertiesUpdateActionHttpv1 implements Action<Void> {

    private final ResourceProperty[] properties;

    private final RepositoryInternal repository;

    private final Resource resource;

    private final boolean setProperties;

    private final TransactionInternal transaction;

    public PropertiesUpdateActionHttpv1(final RepositoryInternal repository, final TransactionInternal transaction, final Resource resource, final boolean setProperties, final ResourceProperty... properties) {
        this.repository = repository;
        this.transaction = transaction;
        this.resource = resource;
        this.setProperties = setProperties;
        this.properties = Arrays.copyOf(properties, properties.length);
    }

    @CheckForNull
    private Info getSparseInfo() {
        final QualifiedResource qualifiedResource = repository.getQualifiedResource(resource);
        final Revision headRevision = transaction.getHeadRevision();
        final QualifiedResource resolvedQualifiedResource = repository.resolve(transaction, qualifiedResource, headRevision, false);

        final Operation<Info> infoOperation = new SparseInfoOperationHttpv1(repository, resolvedQualifiedResource, ResourcePropertyUtils.LOCK_STATUS);
        return infoOperation.execute();
    }

    @Override
    public Void perform() {
        if (properties.length == 0) {
            return null;
        }

        final Optional<LockToken> lockToken;
        final Info info = getSparseInfo();
        if (info != null) {
            lockToken = info.getLockToken();
        } else {
            lockToken = Optional.empty();
        }

        final QualifiedResource qualifiedResource = repository.getQualifiedResource(resource);
        final QualifiedResource qualifiedWorkingResource = transaction.getQualifiedWorkingResource(qualifiedResource);
        final Operation<Void> operation = new PropertiesUpdateOperationHttpv1(repository, qualifiedWorkingResource, setProperties, lockToken, properties);
        operation.execute();

        transaction.register(resource, Status.MODIFIED);
        return null;
    }

}
