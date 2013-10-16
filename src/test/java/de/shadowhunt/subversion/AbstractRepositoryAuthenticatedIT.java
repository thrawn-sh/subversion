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
		repository.lock(EXISTING_EMPTY_DIR, false);
		Assert.fail("only files can be locked");
	}

	@Test
	public void lockingExisitingFile() {
		RepositoryAssert.assertNotLocked(repository, EXISTING_FILE);

		try {
			repository.lock(EXISTING_FILE, false);
			RepositoryAssert.assertLocked(repository, EXISTING_FILE, getUsername());
		} finally {
			// ensure we don't leave any locks behind
			repository.unlock(EXISTING_FILE, false);
			RepositoryAssert.assertNotLocked(repository, EXISTING_FILE);
		}
	}

	@Test(expected = SubversionException.class)
	public void lockingNonExisitingPath() {
		repository.lock(NON_EXISTING, false);
		Assert.fail("locking of non exisiting path must not be possible");
	}

	@Test
	public void unlockNotLockedExisitingFile() {
		RepositoryAssert.assertNotLocked(repository, EXISTING_FILE);
		repository.unlock(EXISTING_FILE, false);
		RepositoryAssert.assertNotLocked(repository, EXISTING_FILE);
	}
}
