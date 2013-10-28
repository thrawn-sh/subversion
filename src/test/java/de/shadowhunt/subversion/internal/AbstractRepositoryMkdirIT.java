package de.shadowhunt.subversion.internal;

import java.util.UUID;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Transaction;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AbstractRepositoryMkdirIT {

	private final Resource prefix;

	private final Repository repository;

	protected AbstractRepositoryMkdirIT(final Repository repository, final UUID testId) {
		prefix = Resource.create("/trunk/" + testId + "/mkdir");
		this.repository = repository;
	}

	@Test(expected = SubversionException.class)
	public void test00_invalid() throws Exception {
		final Resource resource = prefix.append(Resource.create("invalid"));

		final Transaction transaction = repository.createTransaction();
		try {
			Assert.assertTrue("transaction must be active", transaction.isActive());
			transaction.invalidate();
			Assert.assertFalse("transaction must not be active", transaction.isActive());
			repository.mkdir(transaction, resource, false);
			Assert.fail("must not complete");
		} catch (final Exception e) {
			repository.rollback(transaction);
			throw e;
		}
	}

	@Test(expected = SubversionException.class)
	public void test00_noParents() throws Exception {
		Assert.assertFalse(prefix + " does already exist", repository.exists(prefix, Revision.HEAD));
		final Resource resource = prefix.append(Resource.create("no_parents"));

		final Transaction transaction = repository.createTransaction();
		try {
			Assert.assertTrue("transaction must be active", transaction.isActive());
			repository.mkdir(transaction, resource, false);
			Assert.fail("must not complete");
		} catch (final Exception e) {
			repository.rollback(transaction);
			throw e;
		}
	}

	@Test(expected = SubversionException.class)
	public void test00_rollback() throws Exception {
		final Resource resource = prefix.append(Resource.create("rollback"));

		final Transaction transaction = repository.createTransaction();
		try {
			Assert.assertTrue("transaction must be active", transaction.isActive());
			repository.mkdir(transaction, resource, false);
			Assert.assertTrue("transaction must be active", transaction.isActive());
			repository.rollback(transaction);
			Assert.assertFalse("transaction must not be active", transaction.isActive());
		} catch (final Exception e) {
			repository.rollback(transaction);
			throw e;
		}
	}

	@Test
	public void test01_mkdirBase() throws Exception {
		final Transaction transaction = repository.createTransaction();
		try {
			Assert.assertTrue("transaction must be active", transaction.isActive());
			final Resource resource = prefix;

			repository.mkdir(transaction, resource, false);
			Assert.assertTrue("transaction must be active", transaction.isActive());
			repository.commit(transaction, "mkdir " + resource);
			Assert.assertFalse("transaction must be not active", transaction.isActive());
		} catch (final Exception e) {
			repository.rollback(transaction);
			throw e;
		}
	}

	@Test
	public void test02_mkdirFolders() throws Exception {
		final Transaction transaction = repository.createTransaction();
		try {
			Assert.assertTrue("transaction must be active", transaction.isActive());
			final Resource resource = prefix.append(Resource.create("a/b/c"));

			repository.mkdir(transaction, resource, true);
			Assert.assertTrue("transaction must be active", transaction.isActive());
			repository.commit(transaction, "mkdir " + resource);
			Assert.assertFalse("transaction must be not active", transaction.isActive());
		} catch (final Exception e) {
			repository.rollback(transaction);
			throw e;
		}
	}

	@Test
	public void test02_mkdirExisitingBase() throws Exception {
		final Transaction transaction = repository.createTransaction();
		try {
			Assert.assertTrue("transaction must be active", transaction.isActive());
			final Resource resource = prefix;

			repository.mkdir(transaction, resource, true);
			Assert.assertTrue("transaction must be active", transaction.isActive());
			repository.commit(transaction, "mkdir " + resource);
			Assert.assertFalse("transaction must be not active", transaction.isActive());
		} catch (final Exception e) {
			repository.rollback(transaction);
			throw e;
		}
	}
}
