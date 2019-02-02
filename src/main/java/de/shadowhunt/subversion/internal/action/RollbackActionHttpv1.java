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

import de.shadowhunt.subversion.LockToken;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.RepositoryInternal;
import de.shadowhunt.subversion.internal.TransactionInternal;
import de.shadowhunt.subversion.internal.operation.DeleteOperationHttpv1;
import de.shadowhunt.subversion.internal.operation.Operation;

public class RollbackActionHttpv1 implements Action<Void> {

    private final RepositoryInternal repository;

    private final TransactionInternal transaction;

    public RollbackActionHttpv1(final RepositoryInternal repository, final TransactionInternal transaction) {
        this.repository = repository;
        this.transaction = transaction;
    }

    @Override
    public Void perform() {
        try {
            final QualifiedResource qualifiedTransactionResource = transaction.getQualifiedTransactionResource();
            final Optional<LockToken> token = Optional.empty();
            final Operation<Void> operation = new DeleteOperationHttpv1(repository, qualifiedTransactionResource, token);
            return operation.execute();
        } finally {
            transaction.invalidate();
        }
    }

}
