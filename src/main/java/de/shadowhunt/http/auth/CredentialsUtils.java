package de.shadowhunt.http.auth;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;

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
		if (index >= 0) {
			username = user.substring(index + 1);
			domain = user.substring(0, index);
			return new NTCredentials(username, password, workstation, domain);
		}
		return new UsernamePasswordCredentials(user, password);
	}
}
