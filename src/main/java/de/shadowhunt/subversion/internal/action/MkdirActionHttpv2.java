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

import javax.annotation.CheckForNull;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Transaction.Status;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.RepositoryInternal;
import de.shadowhunt.subversion.internal.ResourcePropertyUtils;
import de.shadowhunt.subversion.internal.TransactionInternal;
import de.shadowhunt.subversion.internal.operation.MkdirOperationHttpv1;
import de.shadowhunt.subversion.internal.operation.Operation;
import de.shadowhunt.subversion.internal.operation.SparseInfoOperationHttpv1;

public class MkdirActionHttpv2 implements Action<Void> {

    private final boolean createMissingParents;

    private final RepositoryInternal repository;

    private final Resource rootResource;

    private final TransactionInternal transaction;

    public MkdirActionHttpv2(final RepositoryInternal repository, final TransactionInternal transaction, final Resource rootResource, final boolean createMissingParents) {
        this.repository = repository;
        this.transaction = transaction;
        this.rootResource = rootResource;
        this.createMissingParents = createMissingParents;
    }

    private void createFolder(final Resource resource) {
        final Info sparseInfo = getSparseInfo(resource);
        if (sparseInfo == null) {
            if (createMissingParents) {
                final Resource parent = resource.getParent();
                createFolder(parent);
            }
            createSingleFolder(resource);
            return;
        }

        if (sparseInfo.isFile()) {
            throw new SubversionException("Can not create folder. File with same name already exists: " + resource);
        }
        markAsPartOfTransaction(resource);
    }

    private void createSingleFolder(final Resource resource) {
        final QualifiedResource qualifiedResource = repository.getQualifiedResource(resource);
        final QualifiedResource qualifiedWorkingResource = transaction.getQualifiedWorkingResource(qualifiedResource);
        final Operation<Boolean> mkdirOperation = new MkdirOperationHttpv1(repository, qualifiedWorkingResource);
        mkdirOperation.execute();
        transaction.register(resource, Status.ADDED);
    }

    @CheckForNull
    private Info getSparseInfo(final Resource resource) {
        final QualifiedResource qualifiedResource = repository.getQualifiedResource(resource);
        final Revision headRevision = transaction.getHeadRevision();
        final QualifiedResource resolvedQualifiedResource = repository.resolve(transaction, qualifiedResource, headRevision, false);

        final Operation<Info> infoOperation = new SparseInfoOperationHttpv1(repository, resolvedQualifiedResource, ResourcePropertyUtils.RESOURCE_TYPE);
        return infoOperation.execute();
    }

    private void markAsPartOfTransaction(final Resource resource) {
        if (Resource.ROOT.equals(resource)) {
            return;
        }

        transaction.register(resource, Status.EXISTS);
        final Resource parent = resource.getParent();
        markAsPartOfTransaction(parent);
    }

    @Override
    public Void perform() {
        createFolder(rootResource);
        return null;
    }

}
