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

import java.util.NavigableSet;
import java.util.TreeSet;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.ReadOnlyRepositoryInternal;
import de.shadowhunt.subversion.internal.ViewInternal;
import de.shadowhunt.subversion.internal.operation.ListOperationHttpv1;
import de.shadowhunt.subversion.internal.operation.Operation;

public class ListActionHttpv1 implements Action<NavigableSet<Info>> {

    private final Depth depth;

    private final ReadOnlyRepositoryInternal repository;

    private final Resource resource;

    private final Revision revision;

    private final ViewInternal view;

    public ListActionHttpv1(final ReadOnlyRepositoryInternal repository, final ViewInternal view, final Resource resource, final Revision revision, final Depth depth) {
        this.repository = repository;
        this.view = view;
        this.resource = resource;
        this.revision = revision;
        this.depth = depth;
    }

    private NavigableSet<Info> list(final QualifiedResource qualifiedResource, final Depth depthLevel) {
        final QualifiedResource resolvedQualifiedResource = repository.resolve(view, qualifiedResource, revision, true);

        final Operation<NavigableSet<Info>> listOperation = new ListOperationHttpv1(repository, resolvedQualifiedResource, depthLevel);
        return listOperation.execute();
    }

    private NavigableSet<Info> listRecursively(final QualifiedResource qualifiedResource, final NavigableSet<Info> result) {
        for (final Info info : list(qualifiedResource, Depth.IMMEDIATES)) {
            if (!result.add(info)) {
                continue;
            }

            final Resource suffix = qualifiedResource.getSuffix();
            final Resource infoResource = info.getResource();
            if (info.isDirectory() && !suffix.equals(infoResource)) {
                final QualifiedResource qualifiedResourceNext = repository.getQualifiedResource(infoResource);
                listRecursively(qualifiedResourceNext, result);
            }
        }
        return result;
    }

    @Override
    public NavigableSet<Info> perform() {
        final QualifiedResource qualifiedResource = repository.getQualifiedResource(resource);

        if (Depth.INFINITY == depth) {
            final NavigableSet<Info> result = new TreeSet<>(Info.RESOURCE_COMPARATOR);
            return listRecursively(qualifiedResource, result);
        }

        return list(qualifiedResource, depth);
    }
}
