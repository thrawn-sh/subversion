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
package de.shadowhunt.subversion.internal.validate;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.NavigableSet;
import java.util.UUID;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.LogEntry;
import de.shadowhunt.subversion.ReadOnlyRepository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.View;
import org.apache.commons.lang3.Validate;

public class ValidatingReadOnlyRepository implements ReadOnlyRepository {

    private final ReadOnlyRepository delegate;

    public ValidatingReadOnlyRepository(final ReadOnlyRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public final View createView() {
        return delegate.createView();
    }

    @Override
    public final InputStream download(final View view, final Resource resource, final Revision revision) {
        validateView(view);
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(revision, "revision must not be null");
        validateRevision(view, revision);
        return delegate.download(view, resource, revision);
    }

    @Override
    public final URI downloadURI(final View view, final Resource resource, final Revision revision) {
        validateView(view);
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(revision, "revision must not be null");
        validateRevision(view, revision);
        return delegate.downloadURI(view, resource, revision);
    }

    @Override
    public final boolean exists(final View view, final Resource resource, final Revision revision) {
        validateView(view);
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(revision, "revision must not be null");
        validateRevision(view, revision);
        return delegate.exists(view, resource, revision);
    }

    @Override
    public final Resource getBasePath() {
        return delegate.getBasePath();
    }

    @Override
    public final URI getBaseUri() {
        return delegate.getBaseUri();
    }

    @Override
    public final ProtocolVersion getProtocolVersion() {
        return delegate.getProtocolVersion();
    }

    @Override
    public final UUID getRepositoryId() {
        return delegate.getRepositoryId();
    }

    @Override
    public final Info info(final View view, final Resource resource, final Revision revision) {
        validateView(view);
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(revision, "revision must not be null");
        validateRevision(view, revision);
        return delegate.info(view, resource, revision);
    }

    @Override
    public final NavigableSet<Info> list(final View view, final Resource resource, final Revision revision, final Depth depth) {
        validateView(view);
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(revision, "revision must not be null");
        Validate.notNull(depth, "depth must not be null");
        validateRevision(view, revision);
        return delegate.list(view, resource, revision, depth);
    }

    @Override
    public final List<LogEntry> log(final View view, final Resource resource, final Revision startRevision, final Revision endRevision, final int limit, final boolean stopOnCopy) {
        validateView(view);
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(startRevision, "startRevision must not be null");
        Validate.notNull(endRevision, "endRevision must not be null");
        validateRevision(view, startRevision);
        validateRevision(view, endRevision);
        return delegate.log(view, resource, startRevision, endRevision, limit, stopOnCopy);
    }

    protected void validateRevision(final View view, final Revision revision) {
        if (Revision.HEAD.equals(revision)) {
            return;
        }

        final Revision headRevision = view.getHeadRevision();
        if (headRevision.compareTo(revision) < 0) {
            throw new SubversionException("revision " + revision + " is to new for Transaction/View with head revision " + headRevision);
        }
    }

    protected void validateView(final View view) {
        Validate.notNull(view, "view must not be null");

        final UUID repositoryId = delegate.getRepositoryId();
        final UUID viewRepositoryId = view.getRepositoryId();
        if (!repositoryId.equals(viewRepositoryId)) {
            throw new SubversionException("View invalid: does not belong to this repository");
        }
    }

}
