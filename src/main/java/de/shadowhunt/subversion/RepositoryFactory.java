/*
 * #%L
 * Shadowhunt Subversion
 * %%
 * Copyright (C) 2013 shadowhunt
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.shadowhunt.subversion;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

import de.shadowhunt.subversion.internal.RepositoryFactoryImpl;

/**
 * {@link RepositoryFactory} creates a new {@link Repository}
 */
@ThreadSafe
public abstract class RepositoryFactory {

	private static final String DEFAULT_FRAGMENT = null;

	private static final String DEFAULT_QUERY = null;

	private static final String DEFAULT_USER_INFO = null;

	public static final RepositoryFactory getInstance() {
		return new RepositoryFactoryImpl();
	}

	protected static URI sanatize(final URI uri) {
		final Resource path = Resource.create(uri.getPath());

		try {
			return new URI(uri.getScheme(), DEFAULT_USER_INFO, uri.getHost(), uri.getPort(), path.getValue(), DEFAULT_QUERY, DEFAULT_FRAGMENT);
		} catch (final URISyntaxException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}

	/**
	 * Create a new {@link Repository} for given {@link URI} and {@link Version}
	 *
	 * @param repository {@link URI} to the root of the repository (e.g: http://repository.example.net/svn/test_repo)
	 * @param client {@link HttpClient} that will handle all requests for this repository
	 * @param context {@link HttpContext} that will be used by all requests to this repository
	 *
	 * @return a new {@link Repository} for given {@link URI}
	 */
	public abstract Repository createRepository(final URI repository, final HttpClient client, final HttpContext context) throws SubversionException;
}
