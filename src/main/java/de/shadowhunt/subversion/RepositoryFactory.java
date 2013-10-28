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

import javax.annotation.concurrent.ThreadSafe;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

import de.shadowhunt.subversion.internal.ProbeServerOperation;
import de.shadowhunt.subversion.internal.RepositoryConfig;

/**
 * {@link RepositoryFactory} creates a new {@link Repository}
 */
@ThreadSafe
public final class RepositoryFactory {

	/**
	 * Create a new {@link Repository} for given {@link URI} and {@link Version}
	 *
	 * @param repository {@link URI} to the root of the repository (e.g: http://repository.example.net/svn/test_repo), only http and https scheme are supported
	 *
	 * @return a new {@link Repository} for given {@link URI} and {@link Version}
	 */
	public static final Repository createRepository(final URI repository, final HttpClient client, final HttpContext context) {
		final URI cleaned = removeEndingSlash(repository);

		final ProbeServerOperation operation = new ProbeServerOperation(cleaned);
		final RepositoryConfig config = operation.execute(client, context);
		return config.create(cleaned, client, context);
	}

	static URI removeEndingSlash(final URI uri) {
		final String string = uri.toString();
		final int lastChar = string.length() - 1;
		if (string.charAt(lastChar) == '/') {
			return URI.create(string.substring(0, lastChar));
		}
		return uri;
	}

	private RepositoryFactory() {
		// prevent instantiation
	}
}
