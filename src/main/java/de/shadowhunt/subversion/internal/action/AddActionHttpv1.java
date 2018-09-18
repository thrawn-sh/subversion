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
package de.shadowhunt.subversion.internal.action;

import java.io.InputStream;
import java.util.Optional;

import javax.annotation.CheckForNull;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.LockToken;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction.Status;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.RepositoryInternal;
import de.shadowhunt.subversion.internal.ResourcePropertyUtils;
import de.shadowhunt.subversion.internal.TransactionInternal;
import de.shadowhunt.subversion.internal.operation.Operation;
import de.shadowhunt.subversion.internal.operation.SparseInfoOperationHttpv1;
import de.shadowhunt.subversion.internal.operation.UploadOperationHttpv1;

public class AddActionHttpv1 implements Action<Void> {

    private final InputStream content;

    private final boolean createMissingParents;

    private final RepositoryInternal repository;

    private final Resource resource;

    private final TransactionInternal transaction;

    public AddActionHttpv1(final RepositoryInternal repository, final TransactionInternal transaction, final Resource resource, final boolean createMissingParents, final InputStream content) {
        this.repository = repository;
        this.transaction = transaction;
        this.resource = resource;
        this.createMissingParents = createMissingParents;
        this.content = content;
    }

    @CheckForNull
    private Info getSparseInfo() {
        final QualifiedResource qualifiedResource = repository.getQualifiedResource(resource);
        final Revision revision = transaction.getHeadRevision();
        final QualifiedResource resolvedQualifiedResource = repository.resolve(transaction, qualifiedResource, revision, false);

        final Operation<Info> infoOperation = new SparseInfoOperationHttpv1(repository, resolvedQualifiedResource, ResourcePropertyUtils.LOCK_STATUS);
        return infoOperation.execute();
    }

    @Override
    public Void perform() {
        if (createMissingParents) {
            final Resource parent = resource.getParent();
            repository.mkdir(transaction, parent, true);
        }

        final Info info = getSparseInfo();
        final Optional<LockToken> lockToken;
        final Status status;
        if (info == null) {
            lockToken = Optional.empty();
            status = Status.ADDED;
        } else {
            lockToken = info.getLockToken();
            status = Status.MODIFIED;
        }

        final QualifiedResource qualifiedResource = repository.getQualifiedResource(resource);
        final QualifiedResource qualifiedWorkingResource = transaction.getQualifiedWorkingResource(qualifiedResource);
        final Operation<Void> uploadOperation = new UploadOperationHttpv1(repository, qualifiedWorkingResource, lockToken, content);
        uploadOperation.execute();

        transaction.register(resource, status);
        return null;
    }

}
