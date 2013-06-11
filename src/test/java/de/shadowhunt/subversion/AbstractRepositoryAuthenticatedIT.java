package de.shadowhunt.subversion;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractRepositoryAuthenticatedIT extends AbstractRepositoryReadOnlyIT {

	private final String username;

	protected AbstractRepositoryAuthenticatedIT(final URI uri, final ServerVersion version, final String username, final String password, final String workstation) {
		super(uri, version);
		this.username = username;
		repository.setCredentials(username, password, workstation);
	}

	protected String getUsername() {
		return username;
	}

	@Test(expected = SubversionException.class)
	public void lockingExisitingDir() {
		repository.lock(EXISTING_EMPTY_DIR);
		Assert.fail("only files can be locked");
	}

	@Test
	public void lockingExisitingFile() {
		RepositoryAssert.assertNotLocked(repository, EXISTING_FILE);

		try {
			repository.lock(EXISTING_FILE);
			RepositoryAssert.assertLocked(repository, EXISTING_FILE, getUsername());
		} finally {
			// ensure we don't leave any locks behind
			repository.unlock(EXISTING_FILE);
			RepositoryAssert.assertNotLocked(repository, EXISTING_FILE);
		}
	}

	@Test(expected = SubversionException.class)
	public void lockingNonExisitingPath() {
		repository.lock(NON_EXISTING);
		Assert.fail("locking of non exisiting path must not be possible");
	}

	@Test
	public void unlockNotLockedExisitingFile() {
		RepositoryAssert.assertNotLocked(repository, EXISTING_FILE);

		repository.unlock(EXISTING_FILE);
		RepositoryAssert.assertNotLocked(repository, EXISTING_FILE);
	}
}
