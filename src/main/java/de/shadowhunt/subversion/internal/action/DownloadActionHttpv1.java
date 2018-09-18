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

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.ReadOnlyRepositoryInternal;
import de.shadowhunt.subversion.internal.ViewInternal;
import de.shadowhunt.subversion.internal.operation.DownloadOperationHttpv1;
import de.shadowhunt.subversion.internal.operation.Operation;

public class DownloadActionHttpv1 implements Action<InputStream> {

    private final ReadOnlyRepositoryInternal repository;

    private final Resource resource;

    private final Revision revision;

    private final ViewInternal view;

    public DownloadActionHttpv1(final ReadOnlyRepositoryInternal repository, final ViewInternal view, final Resource resource, final Revision revision) {
        this.repository = repository;
        this.view = view;
        this.resource = resource;
        this.revision = revision;
    }

    @Override
    public InputStream perform() {
        final QualifiedResource qualifiedResource = repository.getQualifiedResource(resource);
        final QualifiedResource resolvedQualifiedResource = repository.resolve(view, qualifiedResource, revision, false);

        final Operation<InputStream> downloadOperation = new DownloadOperationHttpv1(repository, resolvedQualifiedResource);
        return downloadOperation.execute();
    }

}
