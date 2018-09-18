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

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Log;
import de.shadowhunt.subversion.ReadOnlyRepository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty.Key;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.View;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TracingReadOnlyRepository implements ReadOnlyRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(TracingRepository.class);

    private final ReadOnlyRepository delegate;

    TracingReadOnlyRepository(final ReadOnlyRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public View createView() {
        final StopWatch watch = StopWatch.createStarted();
        try {
            LOGGER.trace("starting createView");
            return delegate.createView();
        } finally {
            watch.stop();
            final long milliSeconds = watch.getTime(TimeUnit.MILLISECONDS);
            LOGGER.trace("completed createView took {}ms", milliSeconds);
        }
    }

    @Override
    public InputStream download(final View view, final Resource resource, final Revision revision) {
        final StopWatch watch = StopWatch.createStarted();
        try {
            LOGGER.trace("starting download with {} {} {}", view, resource, revision);
            return delegate.download(view, resource, revision);
        } finally {
            watch.stop();
            final long milliSeconds = watch.getTime(TimeUnit.MILLISECONDS);
            LOGGER.trace("completed download took {}ms", milliSeconds);
        }
    }

    @Override
    public URI downloadURI(final View view, final Resource resource, final Revision revision) {
        final StopWatch watch = StopWatch.createStarted();
        try {
            LOGGER.trace("starting downloadURI with {} {} {}", view, resource, revision);
            return delegate.downloadURI(view, resource, revision);
        } finally {
            watch.stop();
            final long milliSeconds = watch.getTime(TimeUnit.MILLISECONDS);
            LOGGER.trace("completed downloadURI took {}ms", milliSeconds);
        }
    }

    @Override
    public boolean exists(final View view, final Resource resource, final Revision revision) {
        final StopWatch watch = StopWatch.createStarted();
        try {
            LOGGER.trace("starting exists with {} {} {}", view, resource, revision);
            return delegate.exists(view, resource, revision);
        } finally {
            watch.stop();
            final long milliSeconds = watch.getTime(TimeUnit.MILLISECONDS);
            LOGGER.trace("completed exists took {}ms", milliSeconds);
        }
    }

    @Override
    public Resource getBasePath() {
        final StopWatch watch = StopWatch.createStarted();
        try {
            LOGGER.trace("starting getBasePath");
            return delegate.getBasePath();
        } finally {
            watch.stop();
            final long milliSeconds = watch.getTime(TimeUnit.MILLISECONDS);
            LOGGER.trace("completed getBasePath took {}ms", milliSeconds);
        }
    }

    @Override
    public URI getBaseUri() {
        final StopWatch watch = StopWatch.createStarted();
        try {
            LOGGER.trace("starting getBaseUri");
            return delegate.getBaseUri();
        } finally {
            watch.stop();
            final long milliSeconds = watch.getTime(TimeUnit.MILLISECONDS);
            LOGGER.trace("completed getBaseUri took {}ms", milliSeconds);
        }
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        final StopWatch watch = StopWatch.createStarted();
        try {
            LOGGER.trace("starting getProtocolVersion");
            return delegate.getProtocolVersion();
        } finally {
            watch.stop();
            final long milliSeconds = watch.getTime(TimeUnit.MILLISECONDS);
            LOGGER.trace("completed getProtocolVersion took {}ms", milliSeconds);
        }
    }

    @Override
    public UUID getRepositoryId() {
        final StopWatch watch = StopWatch.createStarted();
        try {
            LOGGER.trace("starting getRepositoryId");
            return delegate.getRepositoryId();
        } finally {
            watch.stop();
            final long milliSeconds = watch.getTime(TimeUnit.MILLISECONDS);
            LOGGER.trace("completed getRepositoryId took {}ms", milliSeconds);
        }
    }

    @Override
    public Info info(final View view, final Resource resource, final Revision revision, final Key... keys) {
        final StopWatch watch = StopWatch.createStarted();
        try {
            LOGGER.trace("starting info with {} {} {} {}", view, resource, revision, keys);
            return delegate.info(view, resource, revision, keys);
        } finally {
            watch.stop();
            final long milliSeconds = watch.getTime(TimeUnit.MILLISECONDS);
            LOGGER.trace("completed info took {}ms", milliSeconds);
        }
    }

    @Override
    public Set<Info> list(final View view, final Resource resource, final Revision revision, final Depth depth, final Key... keys) {
        final StopWatch watch = StopWatch.createStarted();
        try {
            LOGGER.trace("starting list with {} {} {} {} {}", view, resource, revision, depth, keys);
            return delegate.list(view, resource, revision, depth, keys);
        } finally {
            watch.stop();
            final long milliSeconds = watch.getTime(TimeUnit.MILLISECONDS);
            LOGGER.trace("completed list in {}ms", milliSeconds);
        }
    }

    @Override
    public List<Log> log(final View view, final Resource resource, final Revision startRevision, final Revision endRevision, final int limit, final boolean stopOnCopy) {
        final StopWatch watch = StopWatch.createStarted();
        try {
            LOGGER.trace("starting info with {} {} {} {} {} {}", view, resource, startRevision, endRevision, limit, stopOnCopy);
            return delegate.log(view, resource, startRevision, endRevision, limit, stopOnCopy);
        } finally {
            watch.stop();
            final long milliSeconds = watch.getTime(TimeUnit.MILLISECONDS);
            LOGGER.trace("completed log in {}ms", milliSeconds);
        }
    }
}
