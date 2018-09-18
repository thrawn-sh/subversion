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

import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.ReadOnlyRepositoryInternal;
import de.shadowhunt.subversion.internal.ViewInternal;
import de.shadowhunt.subversion.internal.operation.Operation;
import de.shadowhunt.subversion.internal.operation.ResolveOperationHttpv1;

public class ResolveActionHttpv1 implements Action<QualifiedResource> {

    private final QualifiedResource qualifiedResource;

    private final ReadOnlyRepositoryInternal repository;

    private final boolean resolve;

    private final Revision revision;

    private final ViewInternal view;

    public ResolveActionHttpv1(final ReadOnlyRepositoryInternal repository, final ViewInternal view, final QualifiedResource qualifiedResource, final Revision revision, final boolean resolve) {
        this.repository = repository;
        this.view = view;
        this.qualifiedResource = qualifiedResource;
        this.revision = revision;
        this.resolve = resolve;
    }

    @Override
    public QualifiedResource perform() {
        final Revision concreteRevision = view.getConcreteRevision(revision);
        final Revision headRevision = view.getHeadRevision();
        if (concreteRevision.compareTo(headRevision) > 0) {
            throw new SubversionException("requested version is to new for this view/transaction");
        }

        if (Revision.HEAD.equals(revision)) {
            if (resolve) {
                return repository.getQualifiedVersionedResource(qualifiedResource, headRevision);
            }
            return qualifiedResource;
        }

        final QualifiedResource versionedResource = repository.getQualifiedVersionedResource(qualifiedResource, revision);
        if (resolve) {
            final Operation<QualifiedResource> resolveOperation = new ResolveOperationHttpv1(repository, qualifiedResource, headRevision, revision);
            return resolveOperation.execute();
        }
        return versionedResource;
    }
}
