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
package de.shadowhunt.http.client;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import org.apache.http.annotation.ThreadSafe;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;

/**
 * {@link ThreadLocalCredentialsProvider} maintains a collection of user credentials. It uses {@link ThreadLocal} to ensure thread-safety.
 */
@ThreadSafe
public class ThreadLocalCredentialsProvider implements CredentialsProvider {

	private final ThreadLocal<Map<AuthScope, Credentials>> threadLocalCredentials = new ThreadLocal<Map<AuthScope, Credentials>>();

	@Override
	public void clear() {
		final Map<AuthScope, Credentials> credentialsMap = getCredentialsMap();
		credentialsMap.clear();
	}

	@Override
	@CheckForNull
	public Credentials getCredentials(final AuthScope authscope) {
		final Map<AuthScope, Credentials> credentialsMap = getCredentialsMap();
		final Credentials directHit = credentialsMap.get(authscope);
		if (directHit != null) {
			return directHit;
		}

		// Do a full scan
		int bestMatchFactor = Integer.MIN_VALUE;
		AuthScope bestMatch = null;
		for (final AuthScope current : credentialsMap.keySet()) {
			final int factor = authscope.match(current);
			if (factor > bestMatchFactor) {
				bestMatchFactor = factor;
				bestMatch = current;
			}
		}

		return credentialsMap.get(bestMatch);
	}

	protected Map<AuthScope, Credentials> getCredentialsMap() {
		Map<AuthScope, Credentials> credentials = threadLocalCredentials.get();
		if (credentials == null) {
			credentials = new HashMap<AuthScope, Credentials>();
			threadLocalCredentials.set(credentials);
		}
		return credentials;
	}

	@Override
	public void setCredentials(final AuthScope authscope, @Nullable final Credentials credentials) {
		if (authscope == null) {
			throw new IllegalArgumentException("Authentication scope may not be null");
		}
		final Map<AuthScope, Credentials> credentialsMap = getCredentialsMap();
		credentialsMap.put(authscope, credentials);
	}
}
