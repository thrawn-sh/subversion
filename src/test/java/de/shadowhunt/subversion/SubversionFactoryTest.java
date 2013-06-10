package de.shadowhunt.subversion;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.ServerVersion;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.SubversionFactory;

public class SubversionFactoryTest {

	@Test(expected = SubversionException.class)
	public void assertSupportedSchemeFileTest() {
		final URI uri = URI.create("file:///svn/test-repo");
		SubversionFactory.assertSupportedScheme(uri);
		Assert.fail("uri must not be supported");
	}

	@Test
	public void assertSupportedSchemeHttpTest() {
		final URI uri = URI.create("http://subversion.example.net/svn/test-repo");
		SubversionFactory.assertSupportedScheme(uri);
		Assert.assertTrue("http is supported", true);
	}

	@Test
	public void assertSupportedSchemeHttpsTest() {
		final URI uri = URI.create("https://subversion.example.net/svn/test-repo");
		SubversionFactory.assertSupportedScheme(uri);
		Assert.assertTrue("https is supported", true);
	}

	@Test(expected = SubversionException.class)
	public void getInstanceUnsupportedServerVersionTest() {
		final URI uri = URI.create("http://subversion.example.net/svn/test-repo");
		final Repository repository = SubversionFactory.getInstance(uri, true, null);

		Assert.assertNotNull("SubversionRepository must not be null", repository);
	}

	@Test
	public void getInstanceTest() {
		final URI uri = URI.create("http://subversion.example.net/svn/test-repo");
		final Repository repository = SubversionFactory.getInstance(uri, true, ServerVersion.V1_6);

		Assert.assertNotNull("SubversionRepository must not be null", repository);
	}
}
