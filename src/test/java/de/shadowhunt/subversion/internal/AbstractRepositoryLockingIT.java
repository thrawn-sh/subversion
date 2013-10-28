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
package de.shadowhunt.subversion.internal;

import java.util.UUID;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Transaction;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryLockingIT {

	private final Resource prefix;

	private final Repository repositoryA, repositoryB;

	protected AbstractRepositoryLockingIT(final Repository repositoryA, final Repository repositoryB, final UUID testId) {
		prefix = Resource.create("/trunk/" + testId + "/locking");
		this.repositoryA = repositoryA;
		this.repositoryB = repositoryB;
	}

	@Test(expected = SubversionException.class)
	public void test00_LockNonExisitingResource() throws Exception {
		final Resource resource = prefix.append(Resource.create("/non_existing.txt"));

		repositoryA.lock(resource, false);
		Assert.fail("lock must not complete");
	}

	@Test(expected = SubversionException.class)
	public void test00_UnlockNonExisitingResource() throws Exception {
		final Resource resource = prefix.append(Resource.create("/non_existing.txt"));

		repositoryA.unlock(resource, false);
		Assert.fail("unlock must not complete");
	}

	@Test
	public void test01_file() throws Exception {
		final Resource resource = prefix.append(Resource.create("/file.txt"));

		AbstractRepositoryAddIT.file(repositoryA, resource, "test", true);
		for (final boolean force : new boolean[] { true, false }) {
			for (final boolean steal : new boolean[] { true, false }) {
				final Info before = repositoryA.info(resource, Revision.HEAD);
				Assert.assertFalse(resource + " must not be locked", before.isLocked());
				repositoryA.lock(resource, steal);
				final Info afterLock = repositoryA.info(resource, Revision.HEAD);
				Assert.assertTrue(resource + " must be locked", afterLock.isLocked());
				repositoryA.unlock(resource, force);
				final Info afterUnlock = repositoryA.info(resource, Revision.HEAD);
				Assert.assertFalse(resource + " must not be locked", afterUnlock.isLocked());
			}
		}
	}

	@Test
	public void test01_lock() throws Exception {
		final Resource resource = prefix.append(Resource.create("/lock.txt"));

		AbstractRepositoryAddIT.file(repositoryA, resource, "test", true);
		final Info before = repositoryA.info(resource, Revision.HEAD);
		Assert.assertFalse(resource + " must not be locked", before.isLocked());

		try {
			repositoryA.lock(resource, true);
			final Info afterFirst = repositoryA.info(resource, Revision.HEAD);
			Assert.assertTrue(resource + " must be locked", afterFirst.isLocked());
			repositoryA.lock(resource, false);
			final Info afterSecond = repositoryA.info(resource, Revision.HEAD);
			Assert.assertTrue(resource + " must be locked", afterSecond.isLocked());
		} finally {
			repositoryA.unlock(resource, false);
		}
	}

	@Test
	public void test01_unlock() throws Exception {
		final Resource resource = prefix.append(Resource.create("/unlock.txt"));

		AbstractRepositoryAddIT.file(repositoryA, resource, "test", true);
		for (final boolean force : new boolean[] { true, false }) {
			final Info before = repositoryA.info(resource, Revision.HEAD);
			Assert.assertFalse(resource + " must not be locked", before.isLocked());
			repositoryA.unlock(resource, force);
			final Info after = repositoryA.info(resource, Revision.HEAD);
			Assert.assertFalse(resource + " must not be locked", after.isLocked());
		}
	}

	@Test(expected = SubversionException.class)
	public void test02_lockFail() throws Exception {
		final Resource resource = prefix.append(Resource.create("/lock_fail.txt"));

		AbstractRepositoryAddIT.file(repositoryA, resource, "test", true);
		final Info before = repositoryA.info(resource, Revision.HEAD);
		Assert.assertFalse(resource + " must not be locked", before.isLocked());

		try {
			repositoryA.lock(resource, false);
			final Info afterFirst = repositoryA.info(resource, Revision.HEAD);
			Assert.assertTrue(resource + " must be locked", afterFirst.isLocked());
			repositoryB.lock(resource, false);
			Assert.fail("lock must not complete");
		} finally {
			repositoryA.unlock(resource, false);
		}
	}

	@Test
	public void test02_lockSteal() throws Exception {
		final Resource resource = prefix.append(Resource.create("/lock_steal.txt"));

		AbstractRepositoryAddIT.file(repositoryA, resource, "test", true);
		final Info before = repositoryA.info(resource, Revision.HEAD);
		Assert.assertFalse(resource + " must not be locked", before.isLocked());

		try {
			repositoryA.lock(resource, false);
			final Info afterFirst = repositoryA.info(resource, Revision.HEAD);
			Assert.assertTrue(resource + " must be locked", afterFirst.isLocked());
			repositoryB.lock(resource, true);
			final Info afterSecond = repositoryA.info(resource, Revision.HEAD);
			Assert.assertTrue(resource + " must be locked", afterSecond.isLocked());
			Assert.assertNotEquals(resource + " must be locked", afterFirst.getLockOwner(), afterSecond.getLockOwner());
		} finally {
			repositoryB.unlock(resource, false);
		}
	}

	@Test(expected = SubversionException.class)
	public void test02_unlockFail() throws Exception {
		final Resource resource = prefix.append(Resource.create("/unlock_fail.txt"));

		AbstractRepositoryAddIT.file(repositoryA, resource, "test", true);
		final Info before = repositoryA.info(resource, Revision.HEAD);
		Assert.assertFalse(resource + " must not be locked", before.isLocked());

		try {
			repositoryA.lock(resource, false);
			final Info after = repositoryA.info(resource, Revision.HEAD);
			Assert.assertTrue(resource + " must be locked", after.isLocked());
			repositoryB.unlock(resource, false);
			Assert.fail("unlock must not complete");
		} finally {
			repositoryA.unlock(resource, false);
		}
	}

	@Test
	public void test02_unlockForce() throws Exception {
		final Resource resource = prefix.append(Resource.create("/unlock_force.txt"));

		AbstractRepositoryAddIT.file(repositoryA, resource, "test", true);
		final Info before = repositoryA.info(resource, Revision.HEAD);
		Assert.assertFalse(resource + " must not be locked", before.isLocked());

		try {
			repositoryA.lock(resource, false);
			final Info afterLock = repositoryA.info(resource, Revision.HEAD);
			Assert.assertTrue(resource + " must be locked", afterLock.isLocked());
			repositoryB.unlock(resource, true);
			final Info afterUnlock = repositoryA.info(resource, Revision.HEAD);
			Assert.assertFalse(resource + " must not be locked", afterUnlock.isLocked());
		} finally {
			repositoryA.unlock(resource, false);
		}
	}

	@Test
	public void test03_FileCopy() throws Exception {
		final Resource source = prefix.append(Resource.create("/file_copy_source.txt"));

		AbstractRepositoryAddIT.file(repositoryA, source, "test", true);
		final Info before = repositoryA.info(source, Revision.HEAD);
		Assert.assertFalse(source + " must not be locked", before.isLocked());

		try {
			repositoryA.lock(source, false);
			final Info afterLock = repositoryA.info(source, Revision.HEAD);
			Assert.assertTrue(source + " must be locked", afterLock.isLocked());
		} finally {
			repositoryA.unlock(source, false);
		}

		final Resource target = prefix.append(Resource.create("/file_copy_target.txt"));
		final Transaction transaction = repositoryA.createTransaction();
		try {
			repositoryA.copy(transaction, source, Revision.HEAD, target, false);
			repositoryA.commit(transaction, "copy");
		} catch (final Exception e) {
			repositoryA.rollback(transaction);
			throw e;
		}

		try {
			final Info beforeUnlock = repositoryA.info(target, Revision.HEAD);
			Assert.assertTrue(target + " must be locked", beforeUnlock.isLocked());
			repositoryA.unlock(target, false);
			final Info afterUnlock = repositoryA.info(target, Revision.HEAD);
			Assert.assertFalse(target + " must not be locked", afterUnlock.isLocked());
		} finally {
			repositoryA.unlock(target, false);
		}
	}

	@Test
	public void test03_FileMove() throws Exception {
		final Resource source = prefix.append(Resource.create("/file_move_source.txt"));

		AbstractRepositoryAddIT.file(repositoryA, source, "test", true);
		final Info before = repositoryA.info(source, Revision.HEAD);
		Assert.assertFalse(source + " must not be locked", before.isLocked());

		try {
			repositoryA.lock(source, false);
			final Info afterLock = repositoryA.info(source, Revision.HEAD);
			Assert.assertTrue(source + " must be locked", afterLock.isLocked());
		} finally {
			repositoryA.unlock(source, false);
		}

		final Resource target = prefix.append(Resource.create("/file_move_target.txt"));
		final Transaction transaction = repositoryA.createTransaction();
		try {
			repositoryA.move(transaction, source, target, false);
			repositoryA.commit(transaction, "move");
		} catch (final Exception e) {
			repositoryA.rollback(transaction);
			throw e;
		}

		try {
			final Info beforeUnlock = repositoryA.info(target, Revision.HEAD);
			Assert.assertTrue(target + " must be locked", beforeUnlock.isLocked());
			repositoryA.unlock(target, false);
			final Info afterUnlock = repositoryA.info(target, Revision.HEAD);
			Assert.assertFalse(target + " must not be locked", afterUnlock.isLocked());
		} finally {
			repositoryA.unlock(target, false);
		}
	}
}
