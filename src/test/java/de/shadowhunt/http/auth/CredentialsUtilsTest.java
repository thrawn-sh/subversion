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
