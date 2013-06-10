package de.shadowhunt.http.conn.ssl;

import java.security.cert.X509Certificate;

import javax.annotation.concurrent.ThreadSafe;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * {@link NonValidatingX509TrustManager} trusts every X509 certificate, regardless of trusted certificate authorities, certificate revocation lists, online status checking or other means.
 */
@ThreadSafe
public final class NonValidatingX509TrustManager implements X509TrustManager {

	/**
	 * Singleton instance
	 */
	public static final TrustManager INSTANCE = new NonValidatingX509TrustManager();

	@Override
	public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
		// trust every certificate
	}

	@Override
	public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
		// trust every certificate
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[0];
	}
}
