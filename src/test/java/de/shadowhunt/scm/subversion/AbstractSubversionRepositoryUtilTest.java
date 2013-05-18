package de.shadowhunt.scm.subversion;

import org.apache.http.auth.NTCredentials;
import org.junit.Assert;
import org.junit.Test;

public class AbstractSubversionRepositoryUtilTest {

	@Test
	public void containsTest() {
		Assert.assertTrue("all status codes", AbstractSubversionRepository.contains(400, null));
		Assert.assertTrue("all status codes", AbstractSubversionRepository.contains(400));

		Assert.assertTrue("found status code", AbstractSubversionRepository.contains(400, 400));
		Assert.assertTrue("found status code", AbstractSubversionRepository.contains(400, 800, 600, 400));

		Assert.assertFalse("missing status code", AbstractSubversionRepository.contains(400, 800, 600));
	}

	@Test
	public void creteCredentialsTest() {
		Assert.assertNull("null credentials", AbstractSubversionRepository.creteCredentials(null, null, null));

		final String user = "user";
		final String domain = "domain";
		final String domainUser = domain + "\\" + user;
		final String workstation = "localhost";
		final String password = "secret";

		Assert.assertEquals("BASIC", new NTCredentials(user, password, null, ""), AbstractSubversionRepository.creteCredentials(user, password, null));

		Assert.assertEquals("NTLM default domain", new NTCredentials(user, password, workstation, ""), AbstractSubversionRepository.creteCredentials(user, password, workstation));
		Assert.assertEquals("NTLM domain", new NTCredentials(user, password, workstation, domain), AbstractSubversionRepository.creteCredentials(domainUser, password, workstation));
	}

	@Test
	public void normalizeResourceTest() {
		Assert.assertEquals("empty resource", "/", AbstractSubversionRepository.normalizeResource(""));

		Assert.assertEquals("root resource", "/", AbstractSubversionRepository.normalizeResource("/"));

		Assert.assertEquals("relative resource", "/foo/bar", AbstractSubversionRepository.normalizeResource("foo/bar"));
		Assert.assertEquals("relative resource with tailing", "/foo/bar", AbstractSubversionRepository.normalizeResource("foo/bar/"));

		Assert.assertEquals("absolute resource", "/foo/bar", AbstractSubversionRepository.normalizeResource("/foo/bar"));
		Assert.assertEquals("absolute resource with tailing", "/foo/bar", AbstractSubversionRepository.normalizeResource("/foo/bar/"));

		Assert.assertEquals("multiple separatpor within", "/foo/bar", AbstractSubversionRepository.normalizeResource("foo//bar"));
		Assert.assertEquals("multiple separatpor beginning", "/foo/bar", AbstractSubversionRepository.normalizeResource("//foo/bar"));
		Assert.assertEquals("multiple separatpor ending", "/foo/bar", AbstractSubversionRepository.normalizeResource("/foo/bar//"));
	}
}
