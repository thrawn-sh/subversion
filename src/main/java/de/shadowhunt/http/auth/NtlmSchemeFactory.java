package de.shadowhunt.http.auth;

import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.impl.auth.NTLMScheme;
import org.apache.http.params.HttpParams;

import de.shadowhunt.jcifs.JcifsEngine;

public class NtlmSchemeFactory implements AuthSchemeFactory {

	@Override
	public AuthScheme newInstance(final HttpParams params) {
		return new NTLMScheme(new JcifsEngine());
	}
}
