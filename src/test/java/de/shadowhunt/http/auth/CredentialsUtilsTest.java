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

import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.junit.Assert;
import org.junit.Test;

public class CredentialsUtilsTest {

	@Test
	public void creteCredentialsTest() {
		Assert.assertNull("null credentials", CredentialsUtils.creteCredentials(null, null, null));

		final String user = "user";
		final String domain = "domain";
		final String domainUser = domain + "\\" + user;
		final String workstation = "localhost";
		final String password = "secret";

		Assert.assertEquals("BASIC", new UsernamePasswordCredentials(user, password), CredentialsUtils.creteCredentials(user, password, null));

		Assert.assertEquals("NTLM default domain", new NTCredentials(user, password, workstation, ""), CredentialsUtils.creteCredentials(user, password, workstation));
		Assert.assertEquals("NTLM domain", new NTCredentials(user, password, workstation, domain), CredentialsUtils.creteCredentials(domainUser, password, workstation));
	}
}
