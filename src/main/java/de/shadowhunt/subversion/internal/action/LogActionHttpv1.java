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

import java.util.List;

import de.shadowhunt.subversion.LogEntry;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.ReadOnlyRepositoryInternal;
import de.shadowhunt.subversion.internal.ViewInternal;
import de.shadowhunt.subversion.internal.operation.LogOperationHttpv1;
import de.shadowhunt.subversion.internal.operation.Operation;

public class LogActionHttpv1 implements Action<List<LogEntry>> {

    private final Revision endRevision;

    private final int limit;

    private final ReadOnlyRepositoryInternal repository;

    private final Resource resource;

    private final Revision startRevision;

    private final boolean stopOnCopy;

    private final ViewInternal view;

    public LogActionHttpv1(final ReadOnlyRepositoryInternal repository, final ViewInternal view, final Resource resource, final Revision startRevision, final Revision endRevision, final int limit, final boolean stopOnCopy) {
        this.repository = repository;
        this.view = view;
        this.resource = resource;
        this.startRevision = startRevision;
        this.endRevision = endRevision;
        this.limit = limit;
        this.stopOnCopy = stopOnCopy;
    }

    @Override
    public List<LogEntry> perform() {
        final QualifiedResource qualifiedResource = repository.getQualifiedResource(resource);

        final Revision concreteStartRevision = view.getConcreteRevision(startRevision);
        final Revision concreteEndRevision = view.getConcreteRevision(endRevision);

        final Revision resolvedRevision;
        if (concreteStartRevision.compareTo(concreteEndRevision) > 0) {
            resolvedRevision = concreteStartRevision;
        } else {
            resolvedRevision = concreteEndRevision;
        }

        final QualifiedResource resolvedQualifiedResource = repository.resolve(view, qualifiedResource, resolvedRevision, false);

        final Operation<List<LogEntry>> logOperation = new LogOperationHttpv1(repository, resolvedQualifiedResource, concreteStartRevision, concreteEndRevision, limit, stopOnCopy);
        return logOperation.execute();
    }

}
