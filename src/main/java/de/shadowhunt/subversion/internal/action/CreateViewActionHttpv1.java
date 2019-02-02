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
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.ReadOnlyRepositoryInternal;
import de.shadowhunt.subversion.internal.ViewImpl;
import de.shadowhunt.subversion.internal.ViewInternal;
import de.shadowhunt.subversion.internal.operation.HeadRevisionOperationHttpv1;
import de.shadowhunt.subversion.internal.operation.Operation;

public class CreateViewActionHttpv1 implements Action<ViewInternal> {

    private final ReadOnlyRepositoryInternal repository;

    public CreateViewActionHttpv1(final ReadOnlyRepositoryInternal repository) {
        this.repository = repository;
    }

    @Override
    public ViewInternal perform() {
        final QualifiedResource qualifiedResource = repository.getQualifiedResource(Resource.ROOT);

        final UUID repositoryId = repository.getRepositoryId();
        final Operation<Revision> headRevisionOperation = new HeadRevisionOperationHttpv1(repository, qualifiedResource);
        final Revision headRevision = headRevisionOperation.execute();

        return new ViewImpl(repositoryId, headRevision);
    }

}
