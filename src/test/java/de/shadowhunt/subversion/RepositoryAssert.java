package de.shadowhunt.subversion;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;

public final class RepositoryAssert {

	private RepositoryAssert() {
		// prevent instantiation
	}

	public static void assertLocked(final Repository repository, final Path resource, final String username) {
		final InfoEntry afterLock = repository.info(resource, Revision.HEAD, false);
		Assert.assertNotNull("InfoEntry must not be null", afterLock);
		Assert.assertTrue(afterLock.isLocked());
		Assert.assertEquals("locked => lock owner", username, afterLock.getLockOwner());
		Assert.assertNotNull("locked => lock token", afterLock.getLockToken());
	}

	public static void assertNotLocked(final Repository repository, final Path resource) {
		final InfoEntry afterLock = repository.info(resource, Revision.HEAD, false);
		Assert.assertNotNull("InfoEntry must not be null", afterLock);
		Assert.assertFalse(afterLock.isLocked());
		Assert.assertNull("not locked => no lock owner", afterLock.getLockOwner());
		Assert.assertNull("not locked => no lock token", afterLock.getLockToken());
	}

	public static void assertLastLog(final Repository repository, final Path resource, final String message, final String user) {
		final LogEntry logEntry = repository.lastLog(resource);
		Assert.assertNotNull("LogEntry must not be null", logEntry);
		Assert.assertEquals("message must match", message, logEntry.getComment());
		Assert.assertEquals("user must match", user, logEntry.getUser());
	}

	public static void assertResourceProperties(final Repository repository, final Path resource, final ResourceProperty... properties) {
		final InfoEntry info = repository.info(resource, Revision.HEAD, true);
		Assert.assertNotNull("InfoEntry must not be null", info);

		final ResourceProperty[] customProperties = info.getCustomProperties();
		Assert.assertEquals("ResourceProperties are missing", properties.length, customProperties.length);

		Arrays.sort(properties, ResourceProperty.NAME_COMPARATOR);
		Arrays.sort(customProperties, ResourceProperty.NAME_COMPARATOR);
		for (int i = 0; i < customProperties.length; i++) {
			Assert.assertEquals("ResourceProperty must match", properties[i], customProperties[i]);
		}
	}

	public static void assertUpload(final Repository repository, final Path resource, final String content) throws IOException {
		Assert.assertTrue("created resource must exist", repository.exists(resource, Revision.HEAD));

		final InputStream download = repository.download(resource, Revision.HEAD);
		Assert.assertNotNull("InputStream must not be null", download);
		Assert.assertEquals("content must match", content, RepositoryUtils.retrieveContent(download));
		IOUtils.closeQuietly(download);
	}
}
