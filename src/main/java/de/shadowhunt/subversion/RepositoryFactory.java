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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ServiceLoader;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.lang3.Validate;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

/**
 * {@link RepositoryFactory} creates a new {@link Repository}.
 */
@ThreadSafe
public abstract class RepositoryFactory {

    private static final String DEFAULT_FRAGMENT = null;

    private static final String DEFAULT_QUERY = null;

    private static final String DEFAULT_USER_INFO = null;

    /**
     * Create a new {@link RepositoryFactory} instance each time the method is called.
     *
     * @return the new {@link RepositoryFactory} instance
     *
     * @throws SubversionException
     *             if no {@link RepositoryFactory} can be created
     */
    public static RepositoryFactory getInstance() throws SubversionException {
        for (final RepositoryFactory factory : ServiceLoader.load(RepositoryFactory.class)) {
            return factory;
        }
        throw new SubversionException("Can not find a RepositoryFactory");
    }

    private static URI sanitise(final URI uri, final Resource path) {
        try {
            final String scheme = uri.getScheme();
            final String host = uri.getHost();
            final int port = uri.getPort();
            final String pathValue = path.getValue();
            return new URI(scheme, DEFAULT_USER_INFO, host, port, pathValue, DEFAULT_QUERY, DEFAULT_FRAGMENT);
        } catch (final URISyntaxException e) {
            final String message = e.getMessage();
            throw new IllegalArgumentException(message, e);
        }
    }

    /**
     * Create a new {@link ReadOnlyRepository} for given {@link URI} and use the given {@link HttpClient} with the {@link HttpContext} to connect to the server.
     *
     * @param uri
     *            {@link URI} to the root of the repository (e.g: http://repository.example.net/svn/test_repo/trunk/folder)
     * @param client
     *            {@link HttpClient} that will handle all requests for this repository
     * @param context
     *            {@link HttpContext} that will be used by all requests to this repository
     * @param validate
     *            {@code true} check the given parameters during creation, {@code false} check during first usage
     *
     * @return a new {@link Repository} for given {@link URI}
     *
     * @throws NullPointerException
     *             if any parameter is {@code null}
     * @throws SubversionException
     *             if no {@link Repository} can be created
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    public final ReadOnlyRepository createReadOnlyRepository(final URI uri, final HttpClient client, final HttpContext context, final boolean validate) {
        Validate.notNull(uri, "uri must not be null");
        Validate.notNull(client, "client must not be null");
        Validate.notNull(context, "context must not be null");

        final String path = uri.getPath();
        final Resource resource = Resource.create(path);
        final URI saneUri = sanitise(uri, resource);
        return createReadOnlyRepository0(saneUri, client, context, validate);
    }

    protected abstract Repository createReadOnlyRepository0(final URI saneUri, final HttpClient client, final HttpContext context, final boolean validate);

    /**
     * Create a new {@link Repository} for given {@link URI} and use the given {@link HttpClient} with the {@link HttpContext} to connect to the server.
     *
     * @param uri
     *            {@link URI} to the root of the repository (e.g: http://repository.example.net/svn/test_repo/trunk/folder)
     * @param client
     *            {@link HttpClient} that will handle all requests for this repository
     * @param context
     *            {@link HttpContext} that will be used by all requests to this repository
     * @param validate
     *            {@code true} check the given parameters during creation, {@code false} check during first usage
     *
     * @return a new {@link Repository} for given {@link URI}
     *
     * @throws NullPointerException
     *             if any parameter is {@code null}
     * @throws SubversionException
     *             if no {@link Repository} can be created
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    public final Repository createRepository(final URI uri, final HttpClient client, final HttpContext context, final boolean validate) {
        Validate.notNull(uri, "uri must not be null");
        Validate.notNull(client, "client must not be null");
        Validate.notNull(context, "context must not be null");

        final String path = uri.getPath();
        final Resource resource = Resource.create(path);
        final URI saneUri = sanitise(uri, resource);
        return createRepository0(saneUri, client, context, validate);
    }

    protected abstract Repository createRepository0(final URI saneUri, final HttpClient client, final HttpContext context, final boolean validate);
}
