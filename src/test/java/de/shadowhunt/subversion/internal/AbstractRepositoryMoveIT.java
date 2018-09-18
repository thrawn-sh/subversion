/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2018 shadowhunt (dev@shadowhunt.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.shadowhunt.subversion.internal;

import java.util.List;
import java.util.UUID;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.LogEntry;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.Transaction.Status;
import de.shadowhunt.subversion.View;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryMoveIT {

    private final Resource prefix;

    private final Repository repository;

    protected AbstractRepositoryMoveIT(final Repository repository, final UUID testId) {
        prefix = Resource.create("/" + testId + "/move");
        this.repository = repository;
    }

    @Test(expected = SubversionException.class)
    public void test00_invalid() throws Exception {
        final Resource source = prefix.append(Resource.create("invalid.txt"));
        final Resource target = prefix.append(Resource.create("invalid.txt"));

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            final TransactionInternal transactionInternal = TransactionInternal.from(transaction);
            transactionInternal.invalidate();
            Assert.assertFalse("transaction must not be active", transaction.isActive());
            repository.move(transaction, source, target, true);
            Assert.fail("must not complete");
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
    }

    @Test(expected = SubversionException.class)
    public void test00_rollback() throws Exception {
        final Resource source = prefix.append(Resource.create("rollback.txt"));
        final Resource target = prefix.append(Resource.create("rollback.txt"));

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.move(transaction, source, target, true);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertTrue("change set must contain: " + target, transaction.getChangeSet().containsKey(target));
            repository.rollback(transaction);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
    }

    @Test
    public void test01_moveFile() throws Exception {
        final Resource source = prefix.append(Resource.create("file.txt"));
        final Resource target = prefix.append(Resource.create("file_move.txt"));

        AbstractRepositoryAddIT.file(repository, source, "A", true);
        AbstractRepositoryAddIT.file(repository, source, "B", false);

        final View beforeView = repository.createView();
        final Info sInfo = repository.info(beforeView, source, Revision.HEAD);
        final List<LogEntry> sLog = repository.log(beforeView, source, Revision.INITIAL, Revision.HEAD, 0, false);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.move(transaction, source, target, true);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertTrue("change set must contain: " + target, transaction.getChangeSet().containsKey(target));
            repository.commit(transaction, "move", true);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        final View afterView = repository.createView();
        Assert.assertFalse(source + " must not exist", repository.exists(afterView, source, Revision.HEAD));
        Assert.assertTrue(target + " must exist", repository.exists(afterView, target, Revision.HEAD));

        final Info tInfo = repository.info(afterView, target, Revision.HEAD);
        Assert.assertEquals("must be same file", sInfo.getMd5(), tInfo.getMd5());

        final List<LogEntry> tLog = repository.log(afterView, target, Revision.INITIAL, Revision.HEAD, 0, false);
        Assert.assertEquals("must be same file", sLog.size(), tLog.size() - 1);
        Assert.assertEquals("logs must match", sLog, tLog.subList(0, sLog.size()));
    }

    @Test(expected = SubversionException.class)
    public void test01_moveFileMissingParent() throws Exception {
        final Resource source = prefix.append(Resource.create("file_missing_parent.txt"));
        final Resource target = prefix.append(Resource.create("a/file_missing_parent/file_move.txt"));

        AbstractRepositoryAddIT.file(repository, source, "A", true);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.move(transaction, source, target, false);
            Assert.fail("move must not complete");
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
    }

    @Test
    public void test01_moveFolder() throws Exception {
        final Resource source = prefix.append(Resource.create("folder"));
        final Resource target = prefix.append(Resource.create("folder_move"));

        AbstractRepositoryMkdirIT.mkdir(repository, source, true);

        final View beforeView = repository.createView();
        final Info sInfo = repository.info(beforeView, source, Revision.HEAD);
        final List<LogEntry> sLog = repository.log(beforeView, source, Revision.INITIAL, Revision.HEAD, 0, false);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.move(transaction, source, target, true);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertTrue("change set must contain: " + target, transaction.getChangeSet().containsKey(target));
            repository.commit(transaction, "move", true);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        final View afterView = repository.createView();
        Assert.assertFalse(source + " must not exist", repository.exists(afterView, source, Revision.HEAD));
        Assert.assertTrue(target + " must exist", repository.exists(afterView, target, Revision.HEAD));

        final Info tInfo = repository.info(afterView, target, Revision.HEAD);
        Assert.assertEquals("must be same file", sInfo.getMd5(), tInfo.getMd5());

        final List<LogEntry> tLog = repository.log(afterView, target, Revision.INITIAL, Revision.HEAD, 0, false);
        Assert.assertEquals("must be same file", sLog.size(), tLog.size() - 1);
        Assert.assertEquals("logs must match", sLog, tLog.subList(0, sLog.size()));
    }

    @Test(expected = SubversionException.class)
    public void test01_moveFolderMissingParent() throws Exception {
        final Resource source = prefix.append(Resource.create("folder_missing_parent"));
        final Resource target = prefix.append(Resource.create("a/folder_missing_parent"));

        AbstractRepositoryMkdirIT.mkdir(repository, source, true);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.move(transaction, source, target, false);
            Assert.fail("move must not complete");
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
    }

    @Test
    public void test02_moveMixedFolder() throws Exception {
        final Resource source = prefix.append(Resource.create("mixed"));
        final Resource target = prefix.append(Resource.create("mixed_move"));

        AbstractRepositoryMkdirIT.mkdir(repository, source, true);

        final Resource subFile = Resource.create("file.txt");
        AbstractRepositoryAddIT.file(repository, source.append(subFile), "A", true);
        final Resource subFolder = Resource.create("subFolder");
        AbstractRepositoryMkdirIT.mkdir(repository, source.append(subFolder), false);

        final View beforeView = repository.createView();
        final Info sInfo = repository.info(beforeView, source, Revision.HEAD);
        final List<LogEntry> sLog = repository.log(beforeView, source, Revision.INITIAL, Revision.HEAD, 0, false);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.move(transaction, source, target, true);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertTrue("change set must contain: " + target, transaction.getChangeSet().containsKey(target));
            repository.commit(transaction, "move", true);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        final View afterView = repository.createView();
        Assert.assertFalse(source + " must not exist", repository.exists(afterView, source, Revision.HEAD));
        Assert.assertTrue(target + " must exist", repository.exists(afterView, target, Revision.HEAD));

        final Info tInfo = repository.info(afterView, target, Revision.HEAD);
        Assert.assertEquals("must be same file", sInfo.getMd5(), tInfo.getMd5());

        final List<LogEntry> tLog = repository.log(afterView, target, Revision.INITIAL, Revision.HEAD, 0, false);
        Assert.assertEquals("must be same file", sLog.size(), tLog.size() - 1);
        Assert.assertEquals("logs must match", sLog, tLog.subList(0, sLog.size()));

        Assert.assertTrue("subFile must exist", repository.exists(afterView, target.append(subFile), Revision.HEAD));
        Assert.assertTrue("subFolder must exist", repository.exists(afterView, target.append(subFolder), Revision.HEAD));
    }

    @Test
    public void test04_moveFileToExisting() throws Exception {
        final Resource source = prefix.append(Resource.create("file_existing_source.txt"));
        final Resource target = prefix.append(Resource.create("file_existing_target.txt"));

        AbstractRepositoryAddIT.file(repository, source, "source", true);
        AbstractRepositoryAddIT.file(repository, target, "target", true);

        final View beforeView = repository.createView();
        final Info sInfo = repository.info(beforeView, source, Revision.HEAD);
        final List<LogEntry> sLog = repository.log(beforeView, source, Revision.INITIAL, Revision.HEAD, 0, false);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.move(transaction, source, target, true);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertEquals("change set must contain: " + target, Status.MODIFIED, transaction.getChangeSet().get(target));
            AbstractRepositoryMkdirIT.assertParentsMapped(target.getParent(), transaction);
            repository.commit(transaction, "copy", true);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        final View afterView = repository.createView();
        Assert.assertFalse(source + " must not exist", repository.exists(afterView, source, Revision.HEAD));
        Assert.assertTrue(target + " must exist", repository.exists(afterView, target, Revision.HEAD));

        final Info tInfo = repository.info(afterView, target, Revision.HEAD);
        Assert.assertEquals("must be same file", sInfo.getMd5(), tInfo.getMd5());

        final List<LogEntry> tLog = repository.log(afterView, target, Revision.INITIAL, Revision.HEAD, 0, false);
        Assert.assertEquals("must be same file", sLog.size(), tLog.size() - 1);
        Assert.assertEquals("logs must match", sLog, tLog.subList(0, sLog.size()));
    }

    @Test
    public void test04_moveFolderToExisting() throws Exception {
        final Resource source = prefix.append(Resource.create("folder_existing_source"));
        final Resource target = prefix.append(Resource.create("folder_existing_target"));

        AbstractRepositoryMkdirIT.mkdir(repository, source, true);
        AbstractRepositoryMkdirIT.mkdir(repository, target, true);

        final View beforeView = repository.createView();
        final Info sInfo = repository.info(beforeView, source, Revision.HEAD);
        final List<LogEntry> sLog = repository.log(beforeView, source, Revision.INITIAL, Revision.HEAD, 0, false);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.move(transaction, source, target, true);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertEquals("change set must contain: " + target, Status.MODIFIED, transaction.getChangeSet().get(target));
            AbstractRepositoryMkdirIT.assertParentsMapped(target.getParent(), transaction);
            repository.commit(transaction, "copy", true);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        final View afterView = repository.createView();
        Assert.assertFalse(source + " must not exist", repository.exists(afterView, source, Revision.HEAD));
        Assert.assertTrue(target + " must exist", repository.exists(afterView, target, Revision.HEAD));

        final Info tInfo = repository.info(afterView, target, Revision.HEAD);
        Assert.assertEquals("must be same file", sInfo.getMd5(), tInfo.getMd5());

        final List<LogEntry> tLog = repository.log(afterView, target, Revision.INITIAL, Revision.HEAD, 0, false);
        Assert.assertEquals("must be same file", sLog.size(), tLog.size() - 1);
        Assert.assertEquals("logs must match", sLog, tLog.subList(0, sLog.size()));
    }
}
