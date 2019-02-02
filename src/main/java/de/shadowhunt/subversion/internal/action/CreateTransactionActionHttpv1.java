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

import java.util.UUID;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.View;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.RepositoryInternal;
import de.shadowhunt.subversion.internal.TransactionHttpv1;
import de.shadowhunt.subversion.internal.TransactionInternal;
import de.shadowhunt.subversion.internal.operation.CheckoutOperationHttpv1;
import de.shadowhunt.subversion.internal.operation.GetTransactionIdOperationHttpv1;
import de.shadowhunt.subversion.internal.operation.Operation;

public class CreateTransactionActionHttpv1 implements Action<TransactionInternal> {

    private final RepositoryInternal repository;

    public CreateTransactionActionHttpv1(final RepositoryInternal repository) {
        this.repository = repository;
    }

    private TransactionInternal createTransaction() {
        final View view = repository.createView();

        final Operation<String> transactionIdOperation = new GetTransactionIdOperationHttpv1(repository);
        final String transactionId = transactionIdOperation.execute();

        final UUID repositoryId = repository.getRepositoryId();
        final Revision headRevision = view.getHeadRevision();
        final String prefix = repository.getPrefix();
        final Resource resource = Resource.create(prefix);
        return new TransactionHttpv1(transactionId, repositoryId, headRevision, resource);
    }

    @Override
    public TransactionInternal perform() {
        final TransactionInternal transaction = createTransaction();

        // transaction resource must be explicitly registered
        registerTransaction(transaction);

        return transaction;
    }

    private void registerTransaction(final TransactionInternal transaction) {
        final String prefix = repository.getPrefix();
        final Resource base = Resource.create(prefix + Resource.SEPARATOR + "vcc");
        final Resource suffix = Resource.create("default");
        final QualifiedResource qualifiedRegisterResource = new QualifiedResource(base, suffix);

        final QualifiedResource qualifiedTransactionResource = transaction.getQualifiedTransactionResource();
        final Operation<Void> checkoutOperation = new CheckoutOperationHttpv1(repository, qualifiedRegisterResource, qualifiedTransactionResource);
        checkoutOperation.execute();
    }

}
