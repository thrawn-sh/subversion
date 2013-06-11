package de.shadowhunt.subversion;

import org.junit.Assert;
import org.junit.Test;

public class RepositoryAuthenticatedIT extends RepositoryReadOnlyIT {

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
		final InfoEntry beforeLock = repository.info(EXISTING_FILE, Revision.HEAD, false);
		Assert.assertNotNull("InfoEntry must not be null", beforeLock);
		Assert.assertFalse(beforeLock.isLocked());
		Assert.assertNull("not locked => no lock owner", beforeLock.getLockOwner());
		Assert.assertNull("not locked => no lock token", beforeLock.getLockToken());

		try {
			repository.lock(EXISTING_FILE);

			final InfoEntry afterLock = repository.info(EXISTING_FILE, Revision.HEAD, false);
			Assert.assertNotNull("InfoEntry must not be null", afterLock);
			Assert.assertTrue(afterLock.isLocked());
			Assert.assertEquals("locked => lock owner", "svnuser", afterLock.getLockOwner());
			Assert.assertNotNull("locked => lock token", afterLock.getLockToken());
		} finally {
			// ensure we don't leave any locks behind
			repository.unlock(EXISTING_FILE);
		}

		final InfoEntry afterUnlock = repository.info(EXISTING_FILE, Revision.HEAD, false);
		Assert.assertNotNull("InfoEntry must not be null", afterUnlock);
		Assert.assertFalse(afterUnlock.isLocked());
		Assert.assertNull("not locked => no lock owner", afterUnlock.getLockOwner());
		Assert.assertNull("not locked => no lock token", afterUnlock.getLockToken());
	}

	@Test(expected = SubversionException.class)
	public void lockingNonExisitingPath() {
		repository.lock(NON_EXISTING);
		Assert.fail("locking of non exisiting path must not be possible");
	}

	@Test
	public void unlockNotLockedExisitingFile() {
		final InfoEntry beforeUnlock = repository.info(EXISTING_FILE, Revision.HEAD, false);
		Assert.assertNotNull("InfoEntry must not be null", beforeUnlock);
		Assert.assertFalse(beforeUnlock.isLocked());
		Assert.assertNull("not locked => no lock owner", beforeUnlock.getLockOwner());
		Assert.assertNull("not locked => no lock token", beforeUnlock.getLockToken());

		repository.unlock(EXISTING_FILE);

		final InfoEntry afterunlock = repository.info(EXISTING_FILE, Revision.HEAD, false);
		Assert.assertNotNull("InfoEntry must not be null", afterunlock);
		Assert.assertFalse(afterunlock.isLocked());
		Assert.assertNull("not locked => no lock owner", afterunlock.getLockOwner());
		Assert.assertNull("not locked => no lock token", afterunlock.getLockToken());
	}
}
