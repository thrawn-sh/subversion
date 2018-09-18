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
package de.shadowhunt.subversion.internal;

import java.util.UUID;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;

public final class TransactionHttpv2 extends AbstractTransaction {

    public TransactionHttpv2(final String id, final UUID repositoryId, final Revision headRevision, final Resource prefix) {
        super(id, repositoryId, headRevision, prefix);
    }

    @Override
    public QualifiedResource getQualifiedCommitMessageResource(final QualifiedResource qualifiedResource) {
        return getQualifiedTransactionResource();
    }

    @Override
    public QualifiedResource getQualifiedTransactionResource() {
        final Resource specialResource = Resource.create("txn" + Resource.SEPARATOR + id);
        return new QualifiedResource(prefix, specialResource);
    }

    @Override
    public QualifiedResource getQualifiedWorkingResource(final QualifiedResource qualifiedResource) {
        final Resource specialResource = Resource.create("txr" + Resource.SEPARATOR + id);
        final Resource base = prefix.append(specialResource);
        final Resource suffix = qualifiedResource.toResource();
        return new QualifiedResource(base, suffix);
    }
}
