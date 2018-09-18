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
import java.util.ServiceLoader;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

/**
 * {@link RepositoryFactory} creates a new {@link Repository}.
 */
@ThreadSafe
public interface RepositoryFactory {

    /**
     * Create a new {@link RepositoryFactory} instance each time the method is called.
     *
     * @return the new {@link RepositoryFactory} instance
     *
     * @throws SubversionException
     *             if no {@link RepositoryFactory} can be created
     */
    static RepositoryFactory getInstance() throws SubversionException {
        for (final RepositoryFactory factory : ServiceLoader.load(RepositoryFactory.class)) {
            return factory;
        }
        throw new SubversionException("Can not find a RepositoryFactory");
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
     * @return a new {@link Repository} for given {@link URI}
     *
     * @throws NullPointerException
     *             if any parameter is {@code null}
     * @throws SubversionException
     *             if no {@link Repository} can be created
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    ReadOnlyRepository createReadOnlyRepository(URI uri, HttpClient client, HttpContext context);

    /**
     * Create a new {@link Repository} for given {@link URI} and use the given {@link HttpClient} with the {@link HttpContext} to connect to the server.
     *
     * @param uri
     *            {@link URI} to the root of the repository (e.g: http://repository.example.net/svn/test_repo/trunk/folder)
     * @param client
     *            {@link HttpClient} that will handle all requests for this repository
     * @param context
     *            {@link HttpContext} that will be used by all requests to this repository
     * @return a new {@link Repository} for given {@link URI}
     *
     * @throws NullPointerException
     *             if any parameter is {@code null}
     * @throws SubversionException
     *             if no {@link Repository} can be created
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    Repository createRepository(URI uri, HttpClient client, HttpContext context);
}
