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
package de.shadowhunt.subversion;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.NavigableSet;
import java.util.UUID;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Interface listing all non modifying operations on a subversion repository.
 */
@ThreadSafe
public interface ReadOnlyRepository {

    /**
     * {@link ProtocolVersion} that represents the version of the subversion server.
     */
    enum ProtocolVersion {

        HTTP_V1, HTTP_V2
    }

    /**
     * Create a new {@link View} to query consistent repository information.
     *
     * @return the new {@link View}
     *
     * @throws SubversionException
     *             if an error occurs while operating on the repository
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    View createView();

    /**
     * Download the resource.
     *
     * @param view
     *            the current valid {@link View}
     * @param resource
     *            the {@link Resource} of the resource (relative to the repository root)
     * @param revision
     *            the {@link Revision} of the resource to retrieve
     *
     * @return {@link InputStream} from which the content can be read (caller has to close the stream properly)
     *
     * @throws java.lang.NullPointerException
     *             if any parameter is {@code null}
     * @throws SubversionException
     *             if an error occurs while operating on the repository
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    InputStream download(View view, Resource resource, Revision revision);

    /**
     * Determine the HTTP download URI for the resource.
     *
     * @param view
     *            the current valid {@link View}
     * @param resource
     *            the {@link Resource} of the resource (relative to the repository root)
     * @param revision
     *            the {@link Revision} of the resource to retrieve
     *
     * @return the HTTP download {@link URI} for the resource
     *
     * @throws java.lang.NullPointerException
     *             if any parameter is {@code null}
     * @throws SubversionException
     *             if an error occurs while operating on the repository
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    URI downloadURI(View view, Resource resource, Revision revision);

    /**
     * Check if the resource already exists in the latest revision of the repository.
     *
     * @param view
     *            the current valid {@link View}
     * @param resource
     *            the {@link Resource} of the resource (relative to the repository root)
     * @param revision
     *            the {@link Revision} of the resource to retrieve
     *
     * @return {@code true} if the resource already exists in the latest revision of the repository otherwise {@code
     * false}
     *
     * @throws java.lang.NullPointerException
     *             if any parameter is {@code null}
     * @throws SubversionException
     *             if an error occurs while operating on the repository
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    boolean exists(View view, Resource resource, Revision revision);

    /**
     * Returns the {@link Resource} to the base for this {@link Repository}.
     *
     * @return the {@link Resource} to the base for this {@link Repository}.
     */
    Resource getBasePath();

    /**
     * Returns the {@link URI} to the root of this {@link Repository}.
     *
     * @return the {@link URI} to the root of this {@link Repository}
     */
    URI getBaseUri();

    /**
     * Returns the {@link ProtocolVersion} of the server running this {@link Repository}.
     *
     * @return the {@link ProtocolVersion} of the server running this {@link Repository}
     */
    ProtocolVersion getProtocolVersion();

    /**
     * Returns the {@link UUID} that identifies the {@link Repository} globally.
     *
     * @return the {@link UUID} that identifies the {@link Repository} globally
     */
    UUID getRepositoryId();

    /**
     * Retrieve information for the resource.
     *
     * @param view
     *            the current valid {@link View}
     * @param resource
     *            the {@link Resource} of the resource (relative to the repository root)
     * @param revision
     *            the {@link Revision} of the resource to retrieve
     *
     * @return {@link Info} for the resource
     *
     * @throws java.lang.NullPointerException
     *             if any parameter is {@code null}
     * @throws SubversionException
     *             if an error occurs while operating on the repository
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    Info info(View view, Resource resource, Revision revision);

    /**
     * Retrieve information for the resource in the given revision and its child resources (depending on depth parameter).
     *
     * @param view
     *            the current valid {@link View}
     * @param resource
     *            the {@link Resource} of the resource (relative to the repository root)
     * @param revision
     *            the {@link Revision} of the resource to retrieve
     * @param depth
     *            whether to retrieve only for the given resource, its children or only part of its children depending on the value of {@link Depth}
     *
     * @return {@link NavigableSet} of {@link Info} for the resource and its child resources (depending on depth parameter)
     *
     * @throws java.lang.NullPointerException
     *             if any parameter is {@code null}
     * @throws SubversionException
     *             if an error occurs while operating on the repository
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    NavigableSet<Info> list(View view, Resource resource, Revision revision, Depth depth);

    /**
     * Retrieve the log information for the revisions between startRevision and endRevision of the resource.
     *
     * @param view
     *            the current valid {@link View}
     * @param resource
     *            the {@link Resource} of the resource (relative to the repository root)
     * @param startRevision
     *            the first {@link Revision} of the resource to retrieve (including)
     * @param endRevision
     *            the last {@link Revision} of the resource to retrieve (including)
     * @param limit
     *            maximal number of {@link LogEntry} entries, if the value is lower or equal to {@code 0} all entries will be returned
     * @param stopOnCopy
     *            do not cross copies while traversing history
     *
     * @return ordered (early to latest) {@link List} of {@link LogEntry} for the revisions between startRevision and endRevision of the resource
     *
     * @throws java.lang.NullPointerException
     *             if any parameter is {@code null}
     * @throws SubversionException
     *             if an error occurs while operating on the repository
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    List<LogEntry> log(View view, Resource resource, Revision startRevision, Revision endRevision, int limit, boolean stopOnCopy);
}
