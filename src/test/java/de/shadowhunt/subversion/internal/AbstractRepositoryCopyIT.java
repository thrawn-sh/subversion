/**
 * Copyright (C) 2013-2015 shadowhunt (dev@shadowhunt.de)
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

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Log;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.Transaction.Status;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryCopyIT {

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
            repository.copy(transaction, source, Revision.HEAD, target, true);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertEquals("change set must contain: " + target, Status.ADDED, transaction.getChangeSet().get(target));
            repository.rollback(transaction);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
    }

    @Test
    public void test01_copyFile() throws Exception {
        final Resource source = prefix.append(Resource.create("file.txt"));
        final Resource target = prefix.append(Resource.create("file_copy.txt"));

        AbstractRepositoryAddIT.file(repository, source, "A", true);
        AbstractRepositoryAddIT.file(repository, source, "B", false);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.copy(transaction, source, Revision.HEAD, target, true);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertEquals("change set must contain: " + target, Status.ADDED, transaction.getChangeSet().get(target));
            AbstractRepositoryMkdirIT.assertParentsMapped(target.getParent(), transaction);
            repository.commit(transaction, "copy");
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        Assert.assertTrue(target + " must exist", repository.exists(target, Revision.HEAD));

        final Info sInfo = repository.info(source, Revision.HEAD);
        final Info tInfo = repository.info(target, Revision.HEAD);
        Assert.assertEquals("must be same file", sInfo.getMd5(), tInfo.getMd5());

        final List<Log> sLog = repository.log(source, Revision.INITIAL, Revision.HEAD, 0, false);
        final List<Log> tLog = repository.log(target, Revision.INITIAL, Revision.HEAD, 0, false);
        Assert.assertEquals("must be same file", sLog.size(), tLog.size() - 1);
        Assert.assertEquals("logs must match", sLog, tLog.subList(0, sLog.size()));
    }

    @Test(expected = SubversionException.class)
    public void test01_copyFileMissingParent() throws Exception {
        final Resource source = prefix.append(Resource.create("file_missing_parent.txt"));
        final Resource target = prefix.append(Resource.create("a/file_missing_parent/file_copy.txt"));

        AbstractRepositoryAddIT.file(repository, source, "A", true);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.copy(transaction, source, Revision.HEAD, target, false);
            Assert.fail("copy must not complete");
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
    }

    @Test
    public void test01_copyFolder() throws Exception {
        final Resource source = prefix.append(Resource.create("folder"));
        final Resource target = prefix.append(Resource.create("folder_copy"));

        AbstractRepositoryMkdirIT.mkdir(repository, source, true);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.copy(transaction, source, Revision.HEAD, target, true);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertEquals("change set must contain: " + target, Status.ADDED, transaction.getChangeSet().get(target));
            AbstractRepositoryMkdirIT.assertParentsMapped(target.getParent(), transaction);
            repository.commit(transaction, "copy");
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        Assert.assertTrue(target + " must exist", repository.exists(target, Revision.HEAD));

        final List<Log> sLog = repository.log(source, Revision.INITIAL, Revision.HEAD, 0, false);
        final List<Log> tLog = repository.log(target, Revision.INITIAL, Revision.HEAD, 0, false);
        Assert.assertEquals("must be same file", sLog.size(), tLog.size() - 1);
        Assert.assertEquals("logs must match", sLog, tLog.subList(0, sLog.size()));
    }

    @Test(expected = SubversionException.class)
    public void test01_copyFolderMissingParent() throws Exception {
        final Resource source = prefix.append(Resource.create("folder_missing_parent"));
        final Resource target = prefix.append(Resource.create("a/folder_missing_parent"));

        AbstractRepositoryMkdirIT.mkdir(repository, source, true);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.copy(transaction, source, Revision.HEAD, target, false);
            Assert.fail("copy must not complete");
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
    }

    @Test
    public void test02_copyMixedFolder() throws Exception {
        final Resource source = prefix.append(Resource.create("mixed"));
        final Resource target = prefix.append(Resource.create("mixed_copy"));

        AbstractRepositoryMkdirIT.mkdir(repository, source, true);

        final Resource subFile = Resource.create("file.txt");
        AbstractRepositoryAddIT.file(repository, source.append(subFile), "A", true);
        final Resource subFolder = Resource.create("subFolder");
        AbstractRepositoryMkdirIT.mkdir(repository, source.append(subFolder), false);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.copy(transaction, source, Revision.HEAD, target, true);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertEquals("change set must contain: " + target, Status.ADDED, transaction.getChangeSet().get(target));
            AbstractRepositoryMkdirIT.assertParentsMapped(target.getParent(), transaction);
            repository.commit(transaction, "copy");
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        Assert.assertTrue(target + " must exist", repository.exists(target, Revision.HEAD));

        final List<Log> sLog = repository.log(source, Revision.INITIAL, Revision.HEAD, 0, false);
        final List<Log> tLog = repository.log(target, Revision.INITIAL, Revision.HEAD, 0, false);
        Assert.assertEquals("must be same file", sLog.size(), tLog.size() - 1);
        Assert.assertEquals("logs must match", sLog, tLog.subList(0, sLog.size()));

        Assert.assertTrue("subFile must exist", repository.exists(target.append(subFile), Revision.HEAD));
        Assert.assertTrue("subFolder must exist", repository.exists(target.append(subFolder), Revision.HEAD));
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
            Assert.assertEquals("change set must contain: " + target, Status.ADDED, transaction.getChangeSet().get(target));
            AbstractRepositoryMkdirIT.assertParentsMapped(target.getParent(), transaction);
            repository.commit(transaction, "copy");
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        Assert.assertTrue(target + " must exist", repository.exists(target, Revision.HEAD));

        final Info sInfo = repository.info(source, sourceRevision);
        final Info tInfo = repository.info(target, Revision.HEAD);
        Assert.assertEquals("must be same file", sInfo.getMd5(), tInfo.getMd5());

        final List<Log> sLog = repository.log(source, Revision.INITIAL, sourceRevision, 0, false);
        final List<Log> tLog = repository.log(target, Revision.INITIAL, Revision.HEAD, 0, false);
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
            Assert.assertEquals("change set must contain: " + target, Status.ADDED, transaction.getChangeSet().get(target));
            AbstractRepositoryMkdirIT.assertParentsMapped(target.getParent(), transaction);
            repository.commit(transaction, "copy");
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        Assert.assertTrue(target + " must exist", repository.exists(target, Revision.HEAD));

        final List<Log> sLog = repository.log(source, Revision.INITIAL, sourceRevision, 0, false);
        final List<Log> tLog = repository.log(target, Revision.INITIAL, Revision.HEAD, 0, false);
        Assert.assertEquals("must be same file", sLog.size(), tLog.size() - 1);
        Assert.assertEquals("logs must match", sLog, tLog.subList(0, sLog.size()));
    }

    @Test
    public void test04_copyFileToExisting() throws Exception {
        final Resource source = prefix.append(Resource.create("file_existing_source.txt"));
        final Resource target = prefix.append(Resource.create("file_existing_target.txt"));

        AbstractRepositoryAddIT.file(repository, source, "source", true);
        AbstractRepositoryAddIT.file(repository, target, "target", true);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.copy(transaction, source, Revision.HEAD, target, true);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertEquals("change set must contain: " + target, Status.MODIFIED, transaction.getChangeSet().get(target));
            AbstractRepositoryMkdirIT.assertParentsMapped(target.getParent(), transaction);
            repository.commit(transaction, "copy");
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        Assert.assertTrue(target + " must exist", repository.exists(target, Revision.HEAD));

        final Info sInfo = repository.info(source, Revision.HEAD);
        final Info tInfo = repository.info(target, Revision.HEAD);
        Assert.assertEquals("must be same file", sInfo.getMd5(), tInfo.getMd5());

        final List<Log> sLog = repository.log(source, Revision.INITIAL, Revision.HEAD, 0, false);
        final List<Log> tLog = repository.log(target, Revision.INITIAL, Revision.HEAD, 0, false);
        Assert.assertEquals("must be same file", sLog.size(), tLog.size() - 1);
        Assert.assertEquals("logs must match", sLog, tLog.subList(0, sLog.size()));
    }

    @Test
    public void test04_copyFolderToExisting() throws Exception {
        final Resource source = prefix.append(Resource.create("folder_existing_source"));
        final Resource target = prefix.append(Resource.create("folder_existing_target"));

        AbstractRepositoryMkdirIT.mkdir(repository, source, true);
        AbstractRepositoryMkdirIT.mkdir(repository, target, true);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.copy(transaction, source, Revision.HEAD, target, true);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertEquals("change set must contain: " + target, Status.MODIFIED, transaction.getChangeSet().get(target));
            AbstractRepositoryMkdirIT.assertParentsMapped(target.getParent(), transaction);
            repository.commit(transaction, "copy");
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        Assert.assertTrue(target + " must exist", repository.exists(target, Revision.HEAD));

        final List<Log> sLog = repository.log(source, Revision.INITIAL, Revision.HEAD, 0, false);
        final List<Log> tLog = repository.log(target, Revision.INITIAL, Revision.HEAD, 0, false);
        Assert.assertEquals("must be same file", sLog.size(), tLog.size() - 1);
        Assert.assertEquals("logs must match", sLog, tLog.subList(0, sLog.size()));
    }
}
