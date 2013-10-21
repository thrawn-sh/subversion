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
package de.shadowhunt.http.conn.ssl;

import java.security.cert.X509Certificate;

import javax.annotation.concurrent.ThreadSafe;
import javax.net.ssl.X509TrustManager;

/**
 * {@link NonValidatingX509TrustManager} trusts every X509 certificate, regardless of trusted certificate authorities, certificate revocation lists, online status checking or other means.
 */
@ThreadSafe
public final class NonValidatingX509TrustManager implements X509TrustManager {

	/**
	 * Singleton instance
	 */
	public static final X509TrustManager INSTANCE = new NonValidatingX509TrustManager();

	@Override
	public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
		if ((chain == null) || (chain.length == 0)) {
			throw new IllegalArgumentException("chain must not be null or zero-length");
		}
		if ((authType == null) || (authType.length() == 0)) {
			throw new IllegalArgumentException("authType must not be null or zero-length");
		}
		// trust every certificate
	}

	@Override
	public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
		if ((chain == null) || (chain.length == 0)) {
			throw new IllegalArgumentException("chain must not be null or zero-length");
		}
		if ((authType == null) || (authType.length() == 0)) {
			throw new IllegalArgumentException("authType must not be null or zero-length");
		}
		// trust every certificate
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[0];
	}
}
