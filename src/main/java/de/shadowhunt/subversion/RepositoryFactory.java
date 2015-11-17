/**
 * Copyright (C) 2013-2015 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
@SuppressWarnings("checkstyle:abstractclassname")
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
     * @throws SubversionException if no {@link RepositoryFactory} can be created
     */
    @SuppressWarnings("PMD.AvoidBranchingStatementAsLastInLoop")
    public static RepositoryFactory getInstance() throws SubversionException {
        for (final RepositoryFactory factory : ServiceLoader.load(RepositoryFactory.class)) {
            return factory;
        }
        throw new SubversionException("Can not find a RepositoryFactory");
    }

    private static URI sanitise(final URI uri, final Resource path) {
        try {
            return new URI(uri.getScheme(), DEFAULT_USER_INFO, uri.getHost(), uri.getPort(), path.getValue(), DEFAULT_QUERY, DEFAULT_FRAGMENT);
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /**
     * Create a new {@link Repository} for given {@link URI} and use the given {@link HttpClient} with the {@link
     * HttpClient} to connect to the server.
     *
     * @param repository {@link URI} to the root of the repository (e.g: http://repository.example.net/svn/test_repo)
     * @param client {@link HttpClient} that will handle all requests for this repository
     * @param context {@link HttpContext} that will be used by all requests to this repository
     *
     * @return a new {@link Repository} for given {@link URI}
     *
     * @throws NullPointerException if any parameter is {@code null}
     * @throws SubversionException if no {@link Repository} can be created
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    public final Repository createRepository(final URI repository, final HttpClient client, final HttpContext context) {
        Validate.notNull(repository, "repository must not be null");
        Validate.notNull(client, "client must not be null");
        Validate.notNull(context, "context must not be null");

        final URI saneUri = sanitise(repository, Resource.create(repository.getPath()));
        return createRepository0(saneUri, client, context);
    }

    protected abstract Repository createRepository0(final URI saneUri, final HttpClient client, final HttpContext context);
}
