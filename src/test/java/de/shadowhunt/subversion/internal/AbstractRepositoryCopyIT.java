package de.shadowhunt.subversion.internal;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Log;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Transaction;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AbstractRepositoryCopyIT {

	private final Resource prefix;

	private final Repository repository;

	protected AbstractRepositoryCopyIT(final Repository repository, final UUID testId) {
		prefix = Resource.create("/trunk/" + testId + "/copy");
		this.repository = repository;
	}

	@Test(expected = SubversionException.class)
	public void test00_invalid() throws Exception {
		final Resource source = prefix.append(Resource.create("invalid.txt"));
		final Resource target = prefix.append(Resource.create("invalid.txt"));

		final Transaction transaction = repository.createTransaction();
		try {
			Assert.assertTrue("transaction must be active", transaction.isActive());
			transaction.invalidate();
			Assert.assertFalse("transaction must not be active", transaction.isActive());
			repository.copy(transaction, source, Revision.HEAD, target, true);
			Assert.fail("must not complete");
		} catch (final Exception e) {
			repository.rollback(transaction);
			throw e;
		}
	}

	@Test(expected = SubversionException.class)
	public void test00_rollback() throws Exception {
		final Resource source = prefix.append(Resource.create("rollback.txt"));
		final Resource target = prefix.append(Resource.create("rollback.txt"));

		final Transaction transaction = repository.createTransaction();
		try {
			Assert.assertTrue("transaction must be active", transaction.isActive());
			repository.copy(transaction, source, Revision.HEAD, target, true);
			Assert.assertTrue("transaction must be active", transaction.isActive());
			repository.rollback(transaction);
			Assert.assertFalse("transaction must not be active", transaction.isActive());
		} catch (final Exception e) {
			repository.rollback(transaction);
			throw e;
		}
	}

	@Test
	public void test01_copyFile() throws Exception {
		final Resource source = prefix.append(Resource.create("file.txt"));
		final Resource target = prefix.append(Resource.create("file_copy.txt"));

		AbstractRepositoryAddIT.file(repository, source, "A", true);
		AbstractRepositoryAddIT.file(repository, source, "B", true);

		final Transaction transaction = repository.createTransaction();
		try {
			Assert.assertTrue("transaction must be active", transaction.isActive());
			repository.copy(transaction, source, Revision.HEAD, target, true);
			Assert.assertTrue("transaction must be active", transaction.isActive());
			repository.rollback(transaction);
			Assert.assertFalse("transaction must not be active", transaction.isActive());
		} catch (final Exception e) {
			repository.rollback(transaction);
			throw e;
		}

		final Info tInfo = repository.info(source, Revision.HEAD);
		final Info sInfo = repository.info(source, Revision.HEAD);
		AbstractRepositoryInfoIT.assertEquals("info must", sInfo, tInfo); // FIXME

		final List<Log> sLog = repository.log(source, Revision.INITIAL, Revision.HEAD, 0);
		final List<Log> tLog = repository.log(target, Revision.INITIAL, Revision.HEAD, 0);
	}
}
