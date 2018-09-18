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

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.internal.RepositoryInternal;
import de.shadowhunt.subversion.internal.TransactionInternal;

public class MoveActionHttpv1 implements Action<Void> {

    private final boolean createMissingParents;

    private final RepositoryInternal repository;

    private final Resource sourceResource;

    private final Resource targetResource;

    private final TransactionInternal transaction;

    public MoveActionHttpv1(final RepositoryInternal repository, final TransactionInternal transaction, final Resource sourceResource, final Resource targetResource, final boolean createMissingParents) {
        this.repository = repository;
        this.transaction = transaction;
        this.sourceResource = sourceResource;
        this.targetResource = targetResource;
        this.createMissingParents = createMissingParents;
    }

    @Override
    public Void perform() {
        final Revision headRevision = transaction.getHeadRevision();
        repository.copy(transaction, sourceResource, headRevision, targetResource, createMissingParents);
        repository.delete(transaction, sourceResource);
        return null;
    }

}
