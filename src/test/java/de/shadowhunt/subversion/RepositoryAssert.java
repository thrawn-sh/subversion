package de.shadowhunt.subversion;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;

public final class RepositoryAssert {

	public static void assertContent(final Repository repository, final Path resource, final String expectedContent) throws IOException {
		Assert.assertTrue("created resource must exist", repository.exists(resource, Revision.HEAD));

		final InputStream download = repository.download(resource, Revision.HEAD);
		Assert.assertNotNull("InputStream must not be null", download);
		Assert.assertEquals("content must match", expectedContent, RepositoryUtils.retrieveContent(download));
		IOUtils.closeQuietly(download);
	}

	public static void assertLastLog(final Repository repository, final Path resource, final String expectedMessage, final String expectedUser) {
		final LogEntry log = repository.lastLog(resource);
		Assert.assertNotNull("LogEntry must not be null", log);
		Assert.assertEquals("message must match", expectedMessage, log.getComment());
		Assert.assertEquals("user must match", expectedUser, log.getUser());
	}

	public static void assertLocked(final Repository repository, final Path resource, final String expectedLockOwner) {
		final InfoEntry info = repository.info(resource, Revision.HEAD, false);
		Assert.assertNotNull("InfoEntry must not be null", info);
		Assert.assertTrue(info.isLocked());
		Assert.assertEquals("locked => lock owner", expectedLockOwner, info.getLockOwner());
		Assert.assertNotNull("locked => lock token", info.getLockToken());
	}

	public static void assertNotLocked(final Repository repository, final Path resource) {
		final InfoEntry info = repository.info(resource, Revision.HEAD, false);
		Assert.assertNotNull("InfoEntry must not be null", info);
		Assert.assertFalse(info.isLocked());
		Assert.assertNull("not locked => no lock owner", info.getLockOwner());
		Assert.assertNull("not locked => no lock token", info.getLockToken());
	}

	public static void assertResourceProperties(final Repository repository, final Path resource, final ResourceProperty... expectedProperties) {
		final InfoEntry info = repository.info(resource, Revision.HEAD, true);
		Assert.assertNotNull("InfoEntry must not be null", info);

		final ResourceProperty[] customProperties = info.getCustomProperties();
		Assert.assertEquals("ResourceProperties are missing", expectedProperties.length, customProperties.length);

		Arrays.sort(expectedProperties, ResourceProperty.NAME_COMPARATOR);
		Arrays.sort(customProperties, ResourceProperty.NAME_COMPARATOR);
		for (int i = 0; i < customProperties.length; i++) {
			Assert.assertEquals("ResourceProperty must match", expectedProperties[i], customProperties[i]);
		}
	}

	public static void assertUpload(final Repository repository, final Path resource, final String expectedContent, final String expectedMessage, final String expectedUser, final ResourceProperty... expectedProperties) throws IOException {
		RepositoryAssert.assertContent(repository, resource, expectedContent);
		RepositoryAssert.assertResourceProperties(repository, resource, expectedProperties);
		RepositoryAssert.assertLastLog(repository, resource, expectedMessage, expectedUser);
		RepositoryAssert.assertNotLocked(repository, resource);
	}

	private RepositoryAssert() {
		// prevent instantiation
	}
}
