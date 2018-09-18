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
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.RepositoryInternal;
import de.shadowhunt.subversion.internal.operation.LockOperationHttpv1;
import de.shadowhunt.subversion.internal.operation.Operation;

public class LockActionHttpv1 implements Action<Void> {

    private final RepositoryInternal repository;

    private final Resource resource;

    private final boolean stealLock;

    public LockActionHttpv1(final RepositoryInternal repository, final Resource resource, final boolean stealLock) {
        this.repository = repository;
        this.resource = resource;
        this.stealLock = stealLock;
    }

    @Override
    public Void perform() {
        final QualifiedResource qualifiedResource = repository.getQualifiedResource(resource);

        final Operation<Void> lockOperation = new LockOperationHttpv1(repository, qualifiedResource, stealLock);
        return lockOperation.execute();
    }

}
