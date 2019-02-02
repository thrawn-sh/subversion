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

import java.util.Optional;

import javax.annotation.CheckForNull;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.LockToken;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty.Key;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Transaction.Status;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.RepositoryInternal;
import de.shadowhunt.subversion.internal.ResourcePropertyUtils;
import de.shadowhunt.subversion.internal.TransactionInternal;
import de.shadowhunt.subversion.internal.operation.CheckoutOperationHttpv1;
import de.shadowhunt.subversion.internal.operation.CopyOperationHttpv1;
import de.shadowhunt.subversion.internal.operation.Operation;
import de.shadowhunt.subversion.internal.operation.SparseInfoOperationHttpv1;

public class CopyActionHttpv1 implements Action<Void> {

    private final boolean createMissingParents;

    private final RepositoryInternal repository;

    private final Resource sourceResource;

    private final Revision sourceRevision;

    private final Resource targetResource;

    private final TransactionInternal transaction;

    public CopyActionHttpv1(final RepositoryInternal repository, final TransactionInternal transaction, final Resource sourceResource, final Revision sourceRevision, final Resource targetResource, final boolean createMissingParents) {
        this.repository = repository;
        this.transaction = transaction;
        this.sourceResource = sourceResource;
        this.sourceRevision = sourceRevision;
        this.targetResource = targetResource;
        this.createMissingParents = createMissingParents;
    }

    private QualifiedResource getQualifiedSourceResource() {
        final Info info = getSparseInfo(sourceResource, sourceRevision, ResourcePropertyUtils.VERSION);
        if (info == null) {
            throw new SubversionException("can not copy missing " + sourceResource + "@" + sourceRevision);
        }

        final QualifiedResource qualifiedResource = repository.getQualifiedResource(sourceResource);
        final Revision sourceInfoRevision = info.getRevision();
        return repository.getQualifiedVersionedResource(qualifiedResource, sourceInfoRevision);
    }

    private QualifiedResource getQualifiedTargetResource() {
        final QualifiedResource qualifiedResource = repository.getQualifiedResource(targetResource);
        return transaction.getQualifiedWorkingResource(qualifiedResource);
    }

    @CheckForNull
    private Info getSparseInfo(final Resource resource, final Revision revision, final Key... keys) {
        final QualifiedResource qualifiedResource = repository.getQualifiedResource(resource);
        final Revision concreteRevision = transaction.getConcreteRevision(revision);
        final QualifiedResource resolvedQualifiedResource = repository.resolve(transaction, qualifiedResource, concreteRevision, false);

        final Operation<Info> infoOperation = new SparseInfoOperationHttpv1(repository, resolvedQualifiedResource, keys);
        return infoOperation.execute();
    }

    private void markAsPartOfTransaction(final Resource resource, final Revision revision) {
        final QualifiedResource qualifiedResource = repository.getQualifiedResource(resource);
        final Action<QualifiedResource> action = new QualifiedRegisterResourceActionHttpv1(repository, qualifiedResource, revision);
        final QualifiedResource existingResource = action.perform();

        final QualifiedResource qualifiedTransactionResource = transaction.getQualifiedTransactionResource();
        final Operation<Void> checkoutOperation = new CheckoutOperationHttpv1(repository, existingResource, qualifiedTransactionResource);
        checkoutOperation.execute();
    }

    @Override
    public Void perform() {
        if (createMissingParents) {
            final Resource targetParent = targetResource.getParent();
            repository.mkdir(transaction, targetParent, true);
        } else {
            final Resource targetParent = targetResource.getParent();
            final Revision headRevision = transaction.getHeadRevision();
            markAsPartOfTransaction(targetParent, headRevision);
        }

        final QualifiedResource qualifiedSourceResource = getQualifiedSourceResource();
        final QualifiedResource qualifiedTargetResource = getQualifiedTargetResource();

        final Info targetInfo = getSparseInfo(targetResource, Revision.HEAD, ResourcePropertyUtils.LOCK_STATUS);
        final Optional<LockToken> lockToken;
        final Status status;
        if (targetInfo == null) {
            lockToken = Optional.empty();
            status = Status.ADDED;
        } else {
            lockToken = targetInfo.getLockToken();
            status = Status.MODIFIED;
        }

        final Operation<Void> copyOperation = new CopyOperationHttpv1(repository, qualifiedSourceResource, qualifiedTargetResource, lockToken);
        copyOperation.execute();

        transaction.register(targetResource, status);
        return null;
    }

}
