package de.shadowhunt.http.conn.ssl;

import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public final class NonValidatingX509TrustManager implements X509TrustManager {

	private static final X509Certificate[] EMPTY = new X509Certificate[0];

	public static final TrustManager INSTANCE = new NonValidatingX509TrustManager();

	@Override
	public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
		// allow every certificate
	}

	@Override
	public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
		// allow every certificate
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return EMPTY;
	}
}
