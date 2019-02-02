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

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.ReadOnlyRepositoryInternal;
import de.shadowhunt.subversion.internal.ViewInternal;
import de.shadowhunt.subversion.internal.operation.InfoOperationHttpv1;
import de.shadowhunt.subversion.internal.operation.Operation;

public class InfoActionHttpv1 implements Action<Info> {

    private final ReadOnlyRepositoryInternal repository;

    private final Resource resource;

    private final Revision revision;

    private final ViewInternal view;

    public InfoActionHttpv1(final ReadOnlyRepositoryInternal repository, final ViewInternal view, final Resource resource, final Revision revision) {
        this.repository = repository;
        this.view = view;
        this.resource = resource;
        this.revision = revision;
    }

    @Override
    public Info perform() {
        final QualifiedResource qualifiedResource = repository.getQualifiedResource(resource);
        final QualifiedResource resolvedQualifiedResource = repository.resolve(view, qualifiedResource, revision, true);

        final Operation<Info> infoOperation = new InfoOperationHttpv1(repository, resolvedQualifiedResource);
        return infoOperation.execute();
    }

}
