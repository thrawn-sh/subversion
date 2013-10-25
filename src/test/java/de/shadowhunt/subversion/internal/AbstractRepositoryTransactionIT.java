package de.shadowhunt.subversion.internal;

import java.util.UUID;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Transaction;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AbstractRepositoryTransactionIT {

	private final Repository repository;

	protected AbstractRepositoryTransactionIT(final Repository repository) {
		this.repository = repository;
	}

	@Test(expected = SubversionException.class)
	public void test00_commitInvalidTransaction() throws Exception {
		final Transaction transaction = new Transaction(UUID.randomUUID(), "1");
		repository.commit(transaction, "empty commit");
		Assert.fail("commit of invalid transaction");
	}

	@Test
	public void test00_rollback() throws Exception {
		final Transaction transaction = repository.createTransaction();
		repository.rollback(transaction);
		Assert.assertTrue("rollback empty transaction", true);
	}

	@Test(expected = SubversionException.class)
	public void test00_rollbackInvalidTransaction() throws Exception {
		final Transaction transaction = new Transaction(UUID.randomUUID(), "1");
		repository.rollback(transaction);
		Assert.fail("rollback of invalid transaction");
	}

	@Test(expected = SubversionException.class)
	public void test01_commit() throws Exception {
		final Transaction transaction = repository.createTransaction();
		repository.commit(transaction, "empty commit");
		Assert.fail("commit empty transaction");
	}
}
