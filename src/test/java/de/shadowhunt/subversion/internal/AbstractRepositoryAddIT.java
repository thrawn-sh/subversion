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
public class AbstractRepositoryAddIT {

	private final Resource prefix;

	private final Repository repository;

	protected AbstractRepositoryAddIT(final Repository repository, final UUID testId) {
		prefix = Resource.create("/trunk/" + testId + "/add");
		this.repository = repository;
	}

	@Test(expected = SubversionException.class)
	public void test00_addNoParents() throws Exception {
		Assert.assertFalse(prefix + " does already exist", repository.exists(prefix, Revision.HEAD));
		final Resource resource = prefix.append(Resource.create("no_parents.txt"));

		final Transaction transaction = repository.createTransaction();
		try {
			// Assert.assertTrue("transaction must be valid", transaction.isValid());
			repository.add(transaction, resource, false, Helper.getInputStream("test"));
			Assert.fail("must not complete");
		} catch (final Exception e) {
			repository.rollback(transaction);
			throw e;
		}
	}

	@Test(expected = SubversionException.class)
	public void test00_rollback() throws Exception {
		final Resource resource = prefix.append(Resource.create("rollback.txt"));

		final Transaction transaction = repository.createTransaction();
		try {
			// Assert.assertTrue("transaction must be valid", transaction.isValid());
			repository.add(transaction, resource, false, Helper.getInputStream("test"));
			// Assert.assertTrue("transaction must be valid", transaction.isValid());
			repository.rollback(transaction);
			// Assert.assertFalse("transaction must not be valid", transaction.isValid());
		} catch (final Exception e) {
			repository.rollback(transaction);
			throw e;
		}
	}

	@Test(expected = SubversionException.class)
	public void test00_invalid() throws Exception {
		final Resource resource = prefix.append(Resource.create("invalid.txt"));

		final Transaction transaction = repository.createTransaction();
		try {
			// Assert.assertTrue("transaction must be valid", transaction.isValid());
			// transaction.invalide();
			// Assert.assertFalse("transaction must not be valid", transaction.isValid());
			repository.add(transaction, resource, false, Helper.getInputStream("test"));
			// Assert.assertTrue("transaction must be valid", transaction.isValid());
			Assert.fail("must not complete");
			// Assert.assertFalse("transaction must not be valid", transaction.isValid());
		} catch (final Exception e) {
			repository.rollback(transaction);
			throw e;
		}
	}

	@Test
	public void test01_addFile() throws Exception {
		final Transaction transaction = repository.createTransaction();
		try {
			// Assert.assertTrue("transaction must be valid", transaction.isValid());
			final Resource resource = prefix.append(Resource.create("file.txt"));

			repository.add(transaction, resource, true, Helper.getInputStream("test"));
			// Assert.assertTrue("transaction must be valid", transaction.isValid());
			repository.commit(transaction, "add " + resource);
			// Assert.assertFalse("transaction must be valid", transaction.isValid());
		} catch (final Exception e) {
			repository.rollback(transaction);
			throw e;
		}
	}
}
