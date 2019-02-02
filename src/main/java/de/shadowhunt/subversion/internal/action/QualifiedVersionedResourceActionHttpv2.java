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

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.ReadOnlyRepositoryInternal;

public class QualifiedVersionedResourceActionHttpv2 implements Action<QualifiedResource> {

    private final QualifiedResource qualifiedResource;

    private final ReadOnlyRepositoryInternal repository;

    private final Revision revision;

    public QualifiedVersionedResourceActionHttpv2(final ReadOnlyRepositoryInternal repository, final QualifiedResource qualifiedResource, final Revision revision) {
        this.repository = repository;
        this.qualifiedResource = qualifiedResource;
        this.revision = revision;
    }

    @Override
    public QualifiedResource perform() {
        assert (!Revision.HEAD.equals(revision)) : "must not be HEAD revision";

        final String prefix = repository.getPrefix();
        final Resource base = Resource.create(prefix + Resource.SEPARATOR + "rvr" + Resource.SEPARATOR + revision);
        final Resource resource = qualifiedResource.toResource();
        return new QualifiedResource(base, resource);
    }
}
