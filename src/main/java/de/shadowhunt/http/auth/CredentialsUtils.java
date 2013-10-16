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
package de.shadowhunt.http.auth;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;

/**
 * {@link CredentialsUtils} provides convenience methods to create and manipulate {@link Credentials}
 */
public final class CredentialsUtils {

	/**
	 * Creates {@link Credentials} from the given data
	 * @param user the username may include the domain to authenticate with "DOMAIN\\username"
	 * @param password the password to use during authentication
	 * @param workstation the computer name the authentication request is originating from
	 * @return {@link Credentials} from the given data
	 */
	@CheckForNull
	public static Credentials creteCredentials(@Nullable final String user, @Nullable final String password, @Nullable final String workstation) {
		if (user == null) {
			return null;
		}

		final String username;
		final String domain;
		final int index = user.indexOf('\\');
		if ((index >= 0) || (workstation != null)) {
			username = user.substring(index + 1);
			domain = user.substring(0, Math.max(index, 0));
			return new NTCredentials(username, password, workstation, domain);
		}
		return new UsernamePasswordCredentials(user, password);
	}

	private CredentialsUtils() {
		// prevent instantiation
	}
}
