package de.shadowhunt.subversion;

import org.junit.Assert;
import org.junit.Test;

public class RepositoryAuthenticatedIT extends RepositoryReadOnlyIT {

	protected static void assertLocked(final Path resource, final String username) {
		final InfoEntry afterLock = repository.info(resource, Revision.HEAD, false);
		Assert.assertNotNull("InfoEntry must not be null", afterLock);
		Assert.assertTrue(afterLock.isLocked());
		Assert.assertEquals("locked => lock owner", username, afterLock.getLockOwner());
		Assert.assertNotNull("locked => lock token", afterLock.getLockToken());
	}

	protected static void assertNotLocked(final Path resource) {
		final InfoEntry afterLock = repository.info(resource, Revision.HEAD, false);
		Assert.assertNotNull("InfoEntry must not be null", afterLock);
		Assert.assertFalse(afterLock.isLocked());
		Assert.assertNull("not locked => no lock owner", afterLock.getLockOwner());
		Assert.assertNull("not locked => no lock token", afterLock.getLockToken());
	}

	protected String getUsername() {
		return "svnuser"; // FIXME TODO
	}

	@Test(expected = SubversionException.class)
	public void lockingExisitingDir() {
		repository.lock(EXISTING_EMPTY_DIR);
		Assert.fail("only files can be locked");
	}

	@Test
	public void lockingExisitingFile() {
		assertNotLocked(EXISTING_FILE);

		try {
			repository.lock(EXISTING_FILE);
			assertLocked(EXISTING_FILE, getUsername());
		} finally {
			// ensure we don't leave any locks behind
			repository.unlock(EXISTING_FILE);
			assertNotLocked(EXISTING_FILE);
		}
	}

	@Test(expected = SubversionException.class)
	public void lockingNonExisitingPath() {
		repository.lock(NON_EXISTING);
		Assert.fail("locking of non exisiting path must not be possible");
	}

	@Test
	public void unlockNotLockedExisitingFile() {
		assertNotLocked(EXISTING_FILE);

		repository.unlock(EXISTING_FILE);
		assertNotLocked(EXISTING_FILE);
	}
}
