package de.shadowhunt.http.client;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import org.apache.http.annotation.ThreadSafe;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;

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
		return credentialsMap.get(authscope);
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
