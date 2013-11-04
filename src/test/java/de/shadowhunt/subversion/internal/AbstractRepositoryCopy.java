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
public class AbstractRepositoryCopy {

	private final Resource prefix;

	private final Repository repository;

	protected AbstractRepositoryCopy(final Repository repository, final UUID testId) {
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
			Assert.assertTrue("changeset must contain: " + target, transaction.getChangeSet().containsKey(target));
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

		AbstractRepositoryAdd.file(repository, source, "A", true);
		AbstractRepositoryAdd.file(repository, source, "B", true);

		final Transaction transaction = repository.createTransaction();
		try {
			Assert.assertTrue("transaction must be active", transaction.isActive());
			repository.copy(transaction, source, Revision.HEAD, target, true);
			Assert.assertTrue("transaction must be active", transaction.isActive());
			Assert.assertTrue("changeset must contain: " + target, transaction.getChangeSet().containsKey(target));
			repository.commit(transaction, "copy");
			Assert.assertFalse("transaction must not be active", transaction.isActive());
		} catch (final Exception e) {
			repository.rollback(transaction);
			throw e;
		}

		Assert.assertTrue(target + " must exist", repository.exists(target, Revision.HEAD));

		final Info sInfo = repository.info(source, Revision.HEAD);
		final Info tInfo = repository.info(target, Revision.HEAD);
		Assert.assertEquals("must be same file", sInfo.getMd5(), tInfo.getMd5());

		final List<Log> sLog = repository.log(source, Revision.INITIAL, Revision.HEAD, 0);
		final List<Log> tLog = repository.log(target, Revision.INITIAL, Revision.HEAD, 0);
		Assert.assertEquals("must be same file", sLog.size(), tLog.size() - 1);
		Assert.assertEquals("logs must match", sLog, tLog.subList(0, sLog.size()));
	}

	@Test(expected = SubversionException.class)
	public void test01_copyFileMissingParent() throws Exception {
		final Resource source = prefix.append(Resource.create("file_missing_parent.txt"));
		final Resource target = prefix.append(Resource.create("a/file_missing_parent/file_copy.txt"));

		AbstractRepositoryAdd.file(repository, source, "A", true);

		final Transaction transaction = repository.createTransaction();
		try {
			Assert.assertTrue("transaction must be active", transaction.isActive());
			repository.copy(transaction, source, Revision.HEAD, target, false);
			Assert.fail("copy must not complet");
		} catch (final Exception e) {
			repository.rollback(transaction);
			throw e;
		}
	}

	@Test
	public void test01_copyFolder() throws Exception {
		final Resource source = prefix.append(Resource.create("folder"));
		final Resource target = prefix.append(Resource.create("folder_copy"));

		AbstractRepositoryMkdir.mkdir(repository, source, true);

		final Transaction transaction = repository.createTransaction();
		try {
			Assert.assertTrue("transaction must be active", transaction.isActive());
			repository.copy(transaction, source, Revision.HEAD, target, true);
			Assert.assertTrue("transaction must be active", transaction.isActive());
			Assert.assertTrue("changeset must contain: " + target, transaction.getChangeSet().containsKey(target));
			repository.commit(transaction, "copy");
			Assert.assertFalse("transaction must not be active", transaction.isActive());
		} catch (final Exception e) {
			repository.rollback(transaction);
			throw e;
		}

		Assert.assertTrue(target + " must exist", repository.exists(target, Revision.HEAD));

		final List<Log> sLog = repository.log(source, Revision.INITIAL, Revision.HEAD, 0);
		final List<Log> tLog = repository.log(target, Revision.INITIAL, Revision.HEAD, 0);
		Assert.assertEquals("must be same file", sLog.size(), tLog.size() - 1);
		Assert.assertEquals("logs must match", sLog, tLog.subList(0, sLog.size()));
	}

	@Test(expected = SubversionException.class)
	public void test01_copyFolderMissingParent() throws Exception {
		final Resource source = prefix.append(Resource.create("folder_missing_parent"));
		final Resource target = prefix.append(Resource.create("a/folder_missing_parent"));

		AbstractRepositoryMkdir.mkdir(repository, source, true);

		final Transaction transaction = repository.createTransaction();
		try {
			Assert.assertTrue("transaction must be active", transaction.isActive());
			repository.copy(transaction, source, Revision.HEAD, target, false);
			Assert.fail("copy must not complet");
		} catch (final Exception e) {
			repository.rollback(transaction);
			throw e;
		}
	}

	@Test
	public void test02_copyMixedFolder() throws Exception {
		final Resource source = prefix.append(Resource.create("mixed"));
		final Resource target = prefix.append(Resource.create("mixed_copy"));

		AbstractRepositoryMkdir.mkdir(repository, source, true);

		final Resource subFile = Resource.create("file.txt");
		AbstractRepositoryAdd.file(repository, source.append(subFile), "A", true);
		final Resource subFolder = Resource.create("subfolder");
		AbstractRepositoryMkdir.mkdir(repository, source.append(subFolder), false);

		final Transaction transaction = repository.createTransaction();
		try {
			Assert.assertTrue("transaction must be active", transaction.isActive());
			repository.copy(transaction, source, Revision.HEAD, target, true);
			Assert.assertTrue("transaction must be active", transaction.isActive());
			Assert.assertTrue("changeset must contain: " + target, transaction.getChangeSet().containsKey(target));
			repository.commit(transaction, "copy");
			Assert.assertFalse("transaction must not be active", transaction.isActive());
		} catch (final Exception e) {
			repository.rollback(transaction);
			throw e;
		}

		Assert.assertTrue(target + " must exist", repository.exists(target, Revision.HEAD));

		final List<Log> sLog = repository.log(source, Revision.INITIAL, Revision.HEAD, 0);
		final List<Log> tLog = repository.log(target, Revision.INITIAL, Revision.HEAD, 0);
		Assert.assertEquals("must be same file", sLog.size(), tLog.size() - 1);
		Assert.assertEquals("logs must match", sLog, tLog.subList(0, sLog.size()));

		Assert.assertTrue("subfile must exist", repository.exists(target.append(subFile), Revision.HEAD));
		Assert.assertTrue("subfolder must exist", repository.exists(target.append(subFolder), Revision.HEAD));
	}

	@Test
	public void test03_copyFileRevision() throws Exception {
		final Resource source = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/copy/file_delete.txt");
		final Revision sourceRevision = Revision.create(5);
		final Resource target = prefix.append(Resource.create("file_delete.txt"));

		final Transaction transaction = repository.createTransaction();
		try {
			Assert.assertTrue("transaction must be active", transaction.isActive());
			repository.copy(transaction, source, sourceRevision, target, true);
			Assert.assertTrue("transaction must be active", transaction.isActive());
			Assert.assertTrue("changeset must contain: " + target, transaction.getChangeSet().containsKey(target));
			repository.commit(transaction, "copy");
			Assert.assertFalse("transaction must not be active", transaction.isActive());
		} catch (final Exception e) {
			repository.rollback(transaction);
			throw e;
		}

		Assert.assertTrue(target + " must exist", repository.exists(target, Revision.HEAD));

		final Info sInfo = repository.info(source, sourceRevision);
		final Info tInfo = repository.info(target, Revision.HEAD);
		Assert.assertEquals("must be same file", sInfo.getMd5(), tInfo.getMd5());

		final List<Log> sLog = repository.log(source, Revision.INITIAL, sourceRevision, 0);
		final List<Log> tLog = repository.log(target, Revision.INITIAL, Revision.HEAD, 0);
		Assert.assertEquals("must be same file", sLog.size(), tLog.size() - 1);
		Assert.assertEquals("logs must match", sLog, tLog.subList(0, sLog.size()));
	}

	@Test
	public void test03_copyFolderRevision() throws Exception {
		final Resource source = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/copy/folder_delete");
		final Revision sourceRevision = Revision.create(14);
		final Resource target = prefix.append(Resource.create("folder_delete"));

		final Transaction transaction = repository.createTransaction();
		try {
			Assert.assertTrue("transaction must be active", transaction.isActive());
			repository.copy(transaction, source, sourceRevision, target, true);
			Assert.assertTrue("transaction must be active", transaction.isActive());
			Assert.assertTrue("changeset must contain: " + target, transaction.getChangeSet().containsKey(target));
			repository.commit(transaction, "copy");
			Assert.assertFalse("transaction must not be active", transaction.isActive());
		} catch (final Exception e) {
			repository.rollback(transaction);
			throw e;
		}

		Assert.assertTrue(target + " must exist", repository.exists(target, Revision.HEAD));

		final List<Log> sLog = repository.log(source, Revision.INITIAL, sourceRevision, 0);
		final List<Log> tLog = repository.log(target, Revision.INITIAL, Revision.HEAD, 0);
		Assert.assertEquals("must be same file", sLog.size(), tLog.size() - 1);
		Assert.assertEquals("logs must match", sLog, tLog.subList(0, sLog.size()));
	}
}
