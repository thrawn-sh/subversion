/**
 * Copyright (C) 2013 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import de.shadowhunt.subversion.Transaction.Status;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryMove {

    private final Resource prefix;

    private final Repository repository;

    protected AbstractRepositoryMove(final Repository repository, final UUID testId) {
        prefix = Resource.create("/trunk/" + testId + "/move");
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
            repository.move(transaction, source, target, true);
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
            repository.move(transaction, source, target, true);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertTrue("change set must contain: " + target, transaction.getChangeSet().containsKey(target));
            repository.rollback(transaction);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }
    }

    @Test
    public void test01_moveFile() throws Exception {
        final Resource source = prefix.append(Resource.create("file.txt"));
        final Resource target = prefix.append(Resource.create("file_move.txt"));

        AbstractRepositoryAdd.file(repository, source, "A", true);
        AbstractRepositoryAdd.file(repository, source, "B", false);

        final Info sInfo = repository.info(source, Revision.HEAD);
        final List<Log> sLog = repository.log(source, Revision.INITIAL, Revision.HEAD, 0, false);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.move(transaction, source, target, true);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertTrue("change set must contain: " + target, transaction.getChangeSet().containsKey(target));
            repository.commit(transaction, "move");
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }

        Assert.assertFalse(source + " must not exist", repository.exists(source, Revision.HEAD));
        Assert.assertTrue(target + " must exist", repository.exists(target, Revision.HEAD));

        final Info tInfo = repository.info(target, Revision.HEAD);
        Assert.assertEquals("must be same file", sInfo.getMd5(), tInfo.getMd5());

        final List<Log> tLog = repository.log(target, Revision.INITIAL, Revision.HEAD, 0, false);
        Assert.assertEquals("must be same file", sLog.size(), tLog.size() - 1);
        Assert.assertEquals("logs must match", sLog, tLog.subList(0, sLog.size()));
    }

    @Test(expected = SubversionException.class)
    public void test01_moveFileMissingParent() throws Exception {
        final Resource source = prefix.append(Resource.create("file_missing_parent.txt"));
        final Resource target = prefix.append(Resource.create("a/file_missing_parent/file_move.txt"));

        AbstractRepositoryAdd.file(repository, source, "A", true);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.move(transaction, source, target, false);
            Assert.fail("move must not complete");
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }
    }

    @Test
    public void test01_moveFolder() throws Exception {
        final Resource source = prefix.append(Resource.create("folder"));
        final Resource target = prefix.append(Resource.create("folder_move"));

        AbstractRepositoryMkdir.mkdir(repository, source, true);

        final Info sInfo = repository.info(source, Revision.HEAD);
        final List<Log> sLog = repository.log(source, Revision.INITIAL, Revision.HEAD, 0, false);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.move(transaction, source, target, true);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertTrue("change set must contain: " + target, transaction.getChangeSet().containsKey(target));
            repository.commit(transaction, "move");
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }

        Assert.assertFalse(source + " must not exist", repository.exists(source, Revision.HEAD));
        Assert.assertTrue(target + " must exist", repository.exists(target, Revision.HEAD));

        final Info tInfo = repository.info(target, Revision.HEAD);
        Assert.assertEquals("must be same file", sInfo.getMd5(), tInfo.getMd5());

        final List<Log> tLog = repository.log(target, Revision.INITIAL, Revision.HEAD, 0, false);
        Assert.assertEquals("must be same file", sLog.size(), tLog.size() - 1);
        Assert.assertEquals("logs must match", sLog, tLog.subList(0, sLog.size()));
    }

    @Test(expected = SubversionException.class)
    public void test01_moveFolderMissingParent() throws Exception {
        final Resource source = prefix.append(Resource.create("folder_missing_parent"));
        final Resource target = prefix.append(Resource.create("a/folder_missing_parent"));

        AbstractRepositoryMkdir.mkdir(repository, source, true);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.move(transaction, source, target, false);
            Assert.fail("move must not complete");
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }
    }

    @Test
    public void test02_moveMixedFolder() throws Exception {
        final Resource source = prefix.append(Resource.create("mixed"));
        final Resource target = prefix.append(Resource.create("mixed_move"));

        AbstractRepositoryMkdir.mkdir(repository, source, true);

        final Resource subFile = Resource.create("file.txt");
        AbstractRepositoryAdd.file(repository, source.append(subFile), "A", true);
        final Resource subFolder = Resource.create("subFolder");
        AbstractRepositoryMkdir.mkdir(repository, source.append(subFolder), false);

        final Info sInfo = repository.info(source, Revision.HEAD);
        final List<Log> sLog = repository.log(source, Revision.INITIAL, Revision.HEAD, 0, false);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.move(transaction, source, target, true);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertTrue("change set must contain: " + target, transaction.getChangeSet().containsKey(target));
            repository.commit(transaction, "move");
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }

        Assert.assertFalse(source + " must not exist", repository.exists(source, Revision.HEAD));
        Assert.assertTrue(target + " must exist", repository.exists(target, Revision.HEAD));

        final Info tInfo = repository.info(target, Revision.HEAD);
        Assert.assertEquals("must be same file", sInfo.getMd5(), tInfo.getMd5());

        final List<Log> tLog = repository.log(target, Revision.INITIAL, Revision.HEAD, 0, false);
        Assert.assertEquals("must be same file", sLog.size(), tLog.size() - 1);
        Assert.assertEquals("logs must match", sLog, tLog.subList(0, sLog.size()));

        Assert.assertTrue("subFile must exist", repository.exists(target.append(subFile), Revision.HEAD));
        Assert.assertTrue("subFolder must exist", repository.exists(target.append(subFolder), Revision.HEAD));
    }

    @Test
    public void test04_moveFileToExisting() throws Exception {
        final Resource source = prefix.append(Resource.create("file_existing_source.txt"));
        final Resource target = prefix.append(Resource.create("file_existing_target.txt"));

        AbstractRepositoryAdd.file(repository, source, "source", true);
        AbstractRepositoryAdd.file(repository, target, "target", true);

        final Info sInfo = repository.info(source, Revision.HEAD);
        final List<Log> sLog = repository.log(source, Revision.INITIAL, Revision.HEAD, 0, false);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.move(transaction, source, target, true);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertEquals("change set must contain: " + target, Status.MODIFIED, transaction.getChangeSet().get(target));
            AbstractRepositoryMkdir.assertParentsMapped(target.getParent(), transaction);
            repository.commit(transaction, "copy");
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }

        Assert.assertFalse(source + " must not exist", repository.exists(source, Revision.HEAD));
        Assert.assertTrue(target + " must exist", repository.exists(target, Revision.HEAD));

        final Info tInfo = repository.info(target, Revision.HEAD);
        Assert.assertEquals("must be same file", sInfo.getMd5(), tInfo.getMd5());

        final List<Log> tLog = repository.log(target, Revision.INITIAL, Revision.HEAD, 0, false);
        Assert.assertEquals("must be same file", sLog.size(), tLog.size() - 1);
        Assert.assertEquals("logs must match", sLog, tLog.subList(0, sLog.size()));
    }

    @Test
    public void test04_moveFolderToExisting() throws Exception {
        final Resource source = prefix.append(Resource.create("folder_existing_source"));
        final Resource target = prefix.append(Resource.create("folder_existing_target"));

        AbstractRepositoryMkdir.mkdir(repository, source, true);
        AbstractRepositoryMkdir.mkdir(repository, target, true);

        final Info sInfo = repository.info(source, Revision.HEAD);
        final List<Log> sLog = repository.log(source, Revision.INITIAL, Revision.HEAD, 0, false);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.move(transaction, source, target, true);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertEquals("change set must contain: " + target, Status.MODIFIED, transaction.getChangeSet().get(target));
            AbstractRepositoryMkdir.assertParentsMapped(target.getParent(), transaction);
            repository.commit(transaction, "copy");
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }

        Assert.assertFalse(source + " must not exist", repository.exists(source, Revision.HEAD));
        Assert.assertTrue(target + " must exist", repository.exists(target, Revision.HEAD));

        final Info tInfo = repository.info(target, Revision.HEAD);
        Assert.assertEquals("must be same file", sInfo.getMd5(), tInfo.getMd5());

        final List<Log> tLog = repository.log(target, Revision.INITIAL, Revision.HEAD, 0, false);
        Assert.assertEquals("must be same file", sLog.size(), tLog.size() - 1);
        Assert.assertEquals("logs must match", sLog, tLog.subList(0, sLog.size()));
    }
}
