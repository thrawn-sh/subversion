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

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.LockToken;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.View;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.RepositoryInternal;
import de.shadowhunt.subversion.internal.operation.Operation;
import de.shadowhunt.subversion.internal.operation.UnlockOperationHttpv1;

public class UnlockActionHttpv1 implements Action<Void> {

    private final boolean breakLock;

    private final RepositoryInternal repository;

    private final Resource resource;

    public UnlockActionHttpv1(final RepositoryInternal repository, final Resource resource, final boolean breakLock) {
        this.repository = repository;
        this.resource = resource;
        this.breakLock = breakLock;
    }

    @Override
    public Void perform() {
        final QualifiedResource qualifiedResource = repository.getQualifiedResource(resource);

        final View view = repository.createView();
        final Revision headRevision = view.getHeadRevision();
        final Info info = repository.info(view, resource, headRevision);
        final Optional<LockToken> lockToken = info.getLockToken();
        if (!lockToken.isPresent()) {
            return null;
        }

        final LockToken lockTokenValue = lockToken.get();
        final Operation<Void> lockOperation = new UnlockOperationHttpv1(repository, qualifiedResource, lockTokenValue, breakLock);
        return lockOperation.execute();
    }

}
