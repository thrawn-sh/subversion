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
package de.shadowhunt.subversion.internal.tracing;

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
import de.shadowhunt.subversion.View;
import org.apache.commons.lang3.time.StopWatch;

public class TracingReadOnlyRepository implements ReadOnlyRepository {

    private final ReadOnlyRepository delegate;

    public TracingReadOnlyRepository(final ReadOnlyRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public final View createView() {
        return delegate.createView();
    }

    @Override
    public final InputStream download(final View view, final Resource resource, final Revision revision) {
        final String method = "download";
        final StopWatch stopWatch = StopWatchLogger.INSTANCE.logStart(method, view, resource, revision);
        try {
            return delegate.download(view, resource, revision);
        } finally {
            StopWatchLogger.INSTANCE.logStop(method, stopWatch);
        }
    }

    @Override
    public final URI downloadURI(final View view, final Resource resource, final Revision revision) {
        final String method = "downloadURI";
        final StopWatch stopWatch = StopWatchLogger.INSTANCE.logStart(method, view, resource, revision);
        try {
            return delegate.downloadURI(view, resource, revision);
        } finally {
            StopWatchLogger.INSTANCE.logStop(method, stopWatch);
        }
    }

    @Override
    public final boolean exists(final View view, final Resource resource, final Revision revision) {
        final String method = "exists";
        final StopWatch stopWatch = StopWatchLogger.INSTANCE.logStart(method, view, resource, revision);
        try {
            return delegate.exists(view, resource, revision);
        } finally {
            StopWatchLogger.INSTANCE.logStop(method, stopWatch);
        }
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
        final String method = "info";
        final StopWatch stopWatch = StopWatchLogger.INSTANCE.logStart(method, view, resource, revision);
        try {
            return delegate.info(view, resource, revision);
        } finally {
            StopWatchLogger.INSTANCE.logStop(method, stopWatch);
        }
    }

    @Override
    public final NavigableSet<Info> list(final View view, final Resource resource, final Revision revision, final Depth depth) {
        final String method = "list";
        final StopWatch stopWatch = StopWatchLogger.INSTANCE.logStart(method, view, resource, revision, depth);
        try {
            return delegate.list(view, resource, revision, depth);
        } finally {
            StopWatchLogger.INSTANCE.logStop(method, stopWatch);
        }
    }

    @Override
    public final List<LogEntry> log(final View view, final Resource resource, final Revision startRevision, final Revision endRevision, final int limit, final boolean stopOnCopy) {
        final String method = "log";
        final StopWatch stopWatch = StopWatchLogger.INSTANCE.logStart(method, view, resource, startRevision, endRevision, limit, stopOnCopy);
        try {
            return delegate.log(view, resource, startRevision, endRevision, limit, stopOnCopy);
        } finally {
            StopWatchLogger.INSTANCE.logStop(method, stopWatch);
        }
    }
}
