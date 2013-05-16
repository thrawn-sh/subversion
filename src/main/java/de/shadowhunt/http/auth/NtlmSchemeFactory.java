package de.shadowhunt.http.auth;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.impl.auth.NTLMScheme;
import org.apache.http.params.HttpParams;

import de.shadowhunt.jcifs.JcifsEngine;

/**
 * Factory for {@link NTLMScheme} with {@link JcifsEngine} implementations
 */
@ThreadSafe
public class NtlmSchemeFactory implements AuthSchemeFactory {

	/**
	 * Singleton instance
	 */
	public static final AuthSchemeFactory INSTANCE = new NtlmSchemeFactory();

	@Override
	public AuthScheme newInstance(final HttpParams params) {
		return new NTLMScheme(JcifsEngine.INSTANCE);
	}
}
