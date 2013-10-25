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

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.junit.Assert;
import org.junit.Test;

public class NonValidatingX509TrustManagerTest {

	@Test
	public void checkClientTrusted() throws CertificateException {
		NonValidatingX509TrustManager.INSTANCE.checkClientTrusted(new X509Certificate[1], "RSA");
		Assert.assertTrue("all certficates must pass => no exception", true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkClientTrustedEmptyAuthType() throws CertificateException {
		NonValidatingX509TrustManager.INSTANCE.checkClientTrusted(new X509Certificate[1], "");
		Assert.fail("empty authType must not be allowed");
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkClientTrustedEmptyChain() throws CertificateException {
		NonValidatingX509TrustManager.INSTANCE.checkClientTrusted(new X509Certificate[0], null);
		Assert.fail("empty chain must not be allowed");
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkClientTrustedNullAuthType() throws CertificateException {
		NonValidatingX509TrustManager.INSTANCE.checkClientTrusted(new X509Certificate[1], null);
		Assert.fail("null authType must not be allowed");
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkClientTrustedNullChain() throws CertificateException {
		NonValidatingX509TrustManager.INSTANCE.checkClientTrusted(null, null);
		Assert.fail("null chain must not be allowed");
	}

	@Test
	public void checkServerTrusted() throws CertificateException {
		NonValidatingX509TrustManager.INSTANCE.checkServerTrusted(new X509Certificate[1], "RSA");
		Assert.assertTrue("all certficates must pass => no exception", true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkServerTrustedEmptyAuthType() throws CertificateException {
		NonValidatingX509TrustManager.INSTANCE.checkServerTrusted(new X509Certificate[1], "");
		Assert.fail("empty authType must not be allowed");
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkServerTrustedEmptyChain() throws CertificateException {
		NonValidatingX509TrustManager.INSTANCE.checkServerTrusted(new X509Certificate[0], null);
		Assert.fail("empty chain must not be allowed");
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkServerTrustedNullAuthType() throws CertificateException {
		NonValidatingX509TrustManager.INSTANCE.checkServerTrusted(new X509Certificate[1], null);
		Assert.fail("null authType must not be allowed");
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkServerTrustedNullChain() throws CertificateException {
		NonValidatingX509TrustManager.INSTANCE.checkServerTrusted(null, null);
		Assert.fail("null chain must not be allowed");
	}

	@Test
	public void getAcceptedIssuers() {
		final X509Certificate[] acceptedIssuers = NonValidatingX509TrustManager.INSTANCE.getAcceptedIssuers();
		Assert.assertNotNull("X509Certificate[] must not be null", acceptedIssuers);
		Assert.assertEquals("X509Certificate[] must be empty", 0, acceptedIssuers.length);
	}
}
