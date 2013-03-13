package de.shadowhunt.scm.subversion;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public abstract class AbstractSubversionRepositoryTest {

	protected static String BASE;

	protected static SubversionRepository REPO;

	@Test
	public void testExistingResource() throws IOException {
		final String resource = BASE + "/existing.txt";
		upload(resource, "exisiting");
		Assert.assertTrue("resource does not exist", REPO.exisits(resource));
	}

	@Test
	public void testNonExistingResource() {
		final String resource = BASE + "/nonexisting.txt";
		Assert.assertFalse("resource already exists", REPO.exisits(resource));
	}

	@Test
	@Ignore
	public void testResourceLocking() throws IOException {
		final String resource = BASE + "/lock.txt";
		upload(resource, "locking");

		final SubversionInfo beforeLock = REPO.info(resource, false);
		Assert.assertNull("resource is locked", beforeLock.getLockToken());

		REPO.lock(resource);
		final SubversionInfo afterLock = REPO.info(resource, false);
		Assert.assertNotNull("resource is not locked", afterLock.getLockToken());

		REPO.unlock(resource, afterLock);
		final SubversionInfo afterUnlock = REPO.info(resource, false);
		Assert.assertNull("resource is locked", afterUnlock.getLockToken());
	}

	@Test
	public void testUpload() throws IOException {
		final String resource = BASE + "/upload.txt";
		upload(resource, "upload");
	}

	protected void upload(final String resource, final String content) throws IOException {
		final InputStream is = new ByteArrayInputStream(content.getBytes());
		try {
			System.out.println(resource);
			REPO.upload(resource, "create", is);
		} finally {
			is.close();
		}
	}
}
