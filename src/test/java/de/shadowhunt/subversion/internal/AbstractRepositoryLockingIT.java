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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.LogEntry;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.ResourceProperty.Key;
import de.shadowhunt.subversion.ResourceProperty.Type;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.Transaction.Status;
import de.shadowhunt.subversion.View;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryLockingIT {

    private final Resource prefix;

    private final Repository repositoryA;

    private final Repository repositoryB;

    protected AbstractRepositoryLockingIT(final Repository repositoryA, final Repository repositoryB, final UUID testId) {
        prefix = Resource.create("/" + testId + "/locking");
        this.repositoryA = repositoryA;
        this.repositoryB = repositoryB;
    }

    @Test(expected = SubversionException.class)
    public void test00_LockNonExistingResource() throws Exception {
        final Resource resource = prefix.append(Resource.create("/non_existing.txt"));

        repositoryA.lock(resource, false);
        Assert.fail("lock must not complete");
    }

    @Test(expected = SubversionException.class)
    public void test00_UnlockNonExistingResource() throws Exception {
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
                final View beforeView = repositoryA.createView();
                final Info before = repositoryA.info(beforeView, resource, Revision.HEAD);
                Assert.assertFalse(resource + " must not be locked", before.isLocked());
                repositoryA.lock(resource, steal);
                final View afterLockView = repositoryA.createView();
                final Info afterLock = repositoryA.info(afterLockView, resource, Revision.HEAD);
                Assert.assertTrue(resource + " must be locked", afterLock.isLocked());
                repositoryA.unlock(resource, force);
                final View afterUnlockView = repositoryA.createView();
                final Info afterUnlock = repositoryA.info(afterUnlockView, resource, Revision.HEAD);
                Assert.assertFalse(resource + " must not be locked", afterUnlock.isLocked());
            }
        }
    }

    @Test
    public void test01_relockWithForce() throws Exception {
        final Resource resource = prefix.append(Resource.create("/relock_with.txt"));

        AbstractRepositoryAddIT.file(repositoryA, resource, "test", true);
        final View beforeView = repositoryA.createView();
        final Info before = repositoryA.info(beforeView, resource, Revision.HEAD);
        Assert.assertFalse(resource + " must not be locked", before.isLocked());

        try {
            repositoryA.lock(resource, true);
            final View afterLockView = repositoryA.createView();
            final Info afterFirst = repositoryA.info(afterLockView, resource, Revision.HEAD);
            Assert.assertTrue(resource + " must be locked", afterFirst.isLocked());
            repositoryA.lock(resource, true);
            final View afterSecondView = repositoryA.createView();
            final Info afterSecond = repositoryA.info(afterSecondView, resource, Revision.HEAD);
            Assert.assertTrue(resource + " must be locked", afterSecond.isLocked());
            Assert.assertEquals("owner must not change", afterFirst.getLockOwner(), afterSecond.getLockOwner());
            Assert.assertNotEquals("token must change", afterFirst.getLockToken(), afterSecond.getLockToken());
        } finally {
            repositoryA.unlock(resource, false);
        }
    }

    @Test(expected = SubversionException.class)
    public void test01_relockWithoutForce() throws Exception {
        final Resource resource = prefix.append(Resource.create("/relock_without.txt"));

        AbstractRepositoryAddIT.file(repositoryA, resource, "test", true);
        final View beforeView = repositoryA.createView();
        final Info before = repositoryA.info(beforeView, resource, Revision.HEAD);
        Assert.assertFalse(resource + " must not be locked", before.isLocked());

        try {
            repositoryA.lock(resource, true);
            final View afterView = repositoryA.createView();
            final Info after = repositoryA.info(afterView, resource, Revision.HEAD);
            Assert.assertTrue(resource + " must be locked", after.isLocked());
            repositoryA.lock(resource, false);
            Assert.fail("relock without steal must fail");
        } finally {
            repositoryA.unlock(resource, false);
        }
    }

    @Test
    public void test01_unlock() throws Exception {
        final Resource resource = prefix.append(Resource.create("/unlock.txt"));

        AbstractRepositoryAddIT.file(repositoryA, resource, "test", true);
        for (final boolean force : new boolean[] { true, false }) {
            final View beforeView = repositoryA.createView();
            final Info before = repositoryA.info(beforeView, resource, Revision.HEAD);
            Assert.assertFalse(resource + " must not be locked", before.isLocked());
            repositoryA.unlock(resource, force);
            final View afterView = repositoryA.createView();
            final Info after = repositoryA.info(afterView, resource, Revision.HEAD);
            Assert.assertFalse(resource + " must not be locked", after.isLocked());
        }
    }

    @Test(expected = SubversionException.class)
    public void test02_lockFail() throws Exception {
        final Resource resource = prefix.append(Resource.create("/lock_fail.txt"));

        AbstractRepositoryAddIT.file(repositoryA, resource, "test", true);
        final View beforeView = repositoryA.createView();
        final Info before = repositoryA.info(beforeView, resource, Revision.HEAD);
        Assert.assertFalse(resource + " must not be locked", before.isLocked());

        try {
            repositoryA.lock(resource, false);
            final View afterView = repositoryA.createView();
            final Info afterFirst = repositoryA.info(afterView, resource, Revision.HEAD);
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
        final View beforeView = repositoryA.createView();
        final Info before = repositoryA.info(beforeView, resource, Revision.HEAD);
        Assert.assertFalse(resource + " must not be locked", before.isLocked());

        try {
            repositoryA.lock(resource, false);
            final View afterLockView = repositoryA.createView();
            final Info afterFirst = repositoryA.info(afterLockView, resource, Revision.HEAD);
            Assert.assertTrue(resource + " must be locked", afterFirst.isLocked());
            repositoryB.lock(resource, true);
            final View afterSecondView = repositoryA.createView();
            final Info afterSecond = repositoryA.info(afterSecondView, resource, Revision.HEAD);
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
        final View beforeView = repositoryA.createView();
        final Info before = repositoryA.info(beforeView, resource, Revision.HEAD);
        Assert.assertFalse(resource + " must not be locked", before.isLocked());

        try {
            repositoryA.lock(resource, false);
            final View afterView = repositoryA.createView();
            final Info after = repositoryA.info(afterView, resource, Revision.HEAD);
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
        final View beforeView = repositoryA.createView();
        final Info before = repositoryA.info(beforeView, resource, Revision.HEAD);
        Assert.assertFalse(resource + " must not be locked", before.isLocked());

        try {
            repositoryA.lock(resource, false);
            final View afterLockView = repositoryA.createView();
            final Info afterLock = repositoryA.info(afterLockView, resource, Revision.HEAD);
            Assert.assertTrue(resource + " must be locked", afterLock.isLocked());
            repositoryB.unlock(resource, true);
            final View afterUnlockView = repositoryA.createView();
            final Info afterUnlock = repositoryA.info(afterUnlockView, resource, Revision.HEAD);
            Assert.assertFalse(resource + " must not be locked", afterUnlock.isLocked());
        } finally {
            repositoryA.unlock(resource, false);
        }
    }

    @Test
    public void test03_FileCopy() throws Exception {
        final Resource source = prefix.append(Resource.create("/file_copy_source.txt"));

        AbstractRepositoryAddIT.file(repositoryA, source, "test", true);
        final View beforeView = repositoryA.createView();
        final Info before = repositoryA.info(beforeView, source, Revision.HEAD);
        Assert.assertFalse(source + " must not be locked", before.isLocked());

        try {
            repositoryA.lock(source, false);
            final View afterView = repositoryA.createView();
            final Info afterLock = repositoryA.info(afterView, source, Revision.HEAD);
            Assert.assertTrue(source + " must be locked", afterLock.isLocked());
        } finally {
            repositoryA.unlock(source, false);
        }

        final Resource target = prefix.append(Resource.create("/file_copy_target.txt"));
        final Transaction transaction = repositoryA.createTransaction();
        try {
            repositoryA.copy(transaction, source, Revision.HEAD, target, false);
            repositoryA.commit(transaction, "copy", true);
        } finally {
            repositoryA.rollbackIfNotCommitted(transaction);
        }

        final View afterView = repositoryA.createView();
        final Info after = repositoryA.info(afterView, target, Revision.HEAD);
        Assert.assertFalse(target + " must not be locked", after.isLocked());
    }

    @Test
    public void test03_FileMove() throws Exception {
        final Resource source = prefix.append(Resource.create("/file_move_source.txt"));

        AbstractRepositoryAddIT.file(repositoryA, source, "test", true);
        final View beforeView = repositoryA.createView();
        final Info before = repositoryA.info(beforeView, source, Revision.HEAD);
        Assert.assertFalse(source + " must not be locked", before.isLocked());

        try {
            repositoryA.lock(source, false);
            final View afterView = repositoryA.createView();
            final Info afterLock = repositoryA.info(afterView, source, Revision.HEAD);
            Assert.assertTrue(source + " must be locked", afterLock.isLocked());
        } finally {
            repositoryA.unlock(source, false);
        }

        final Resource target = prefix.append(Resource.create("/file_move_target.txt"));
        final Transaction transaction = repositoryA.createTransaction();
        try {
            repositoryA.move(transaction, source, target, false);
            repositoryA.commit(transaction, "move", true);
        } finally {
            repositoryA.rollbackIfNotCommitted(transaction);
        }

        final View afterView = repositoryA.createView();
        final Info after = repositoryA.info(afterView, target, Revision.HEAD);
        Assert.assertFalse(target + " must not be locked", after.isLocked());
    }

    @Test
    public void test04_DeletePropertiesOfLocked() throws Exception {
        final Resource resource = prefix.append(Resource.create("file_delete_properties_locked.txt"));
        final Key key = new Key(Type.SUBVERSION_CUSTOM, "test");
        final ResourceProperty property = new ResourceProperty(key, "A");

        AbstractRepositoryAddIT.file(repositoryA, resource, "resource", true);
        AbstractRepositoryPropertiesSetIT.setProperties(repositoryA, resource, property);
        repositoryA.lock(resource, false);

        final Transaction transaction = repositoryA.createTransaction();
        try {
            repositoryA.propertiesDelete(transaction, resource, property);
            repositoryA.commit(transaction, "update", true);
        } finally {
            repositoryA.rollbackIfNotCommitted(transaction);
        }

        final View view = repositoryA.createView();
        final Info info = repositoryA.info(view, resource, Revision.HEAD);
        final ResourceProperty[] actual = info.getProperties();
        Assert.assertEquals("expected number of properties", 0, actual.length);
    }

    @Test
    public void test04_FileCopyToLocked() throws Exception {
        final Resource source = prefix.append(Resource.create("file_copy_locked_source.txt"));
        final Resource target = prefix.append(Resource.create("file_copy_locked_target.txt"));

        AbstractRepositoryAddIT.file(repositoryA, source, "source", true);
        AbstractRepositoryAddIT.file(repositoryA, target, "target", true);
        repositoryA.lock(target, false);

        final Transaction transaction = repositoryA.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repositoryA.copy(transaction, source, Revision.HEAD, target, true);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertEquals("change set must contain: " + target, Status.MODIFIED, transaction.getChangeSet().get(target));
            AbstractRepositoryMkdirIT.assertParentsMapped(target.getParent(), transaction);
            repositoryA.commit(transaction, "copy", true);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repositoryA.rollbackIfNotCommitted(transaction);
        }

        final View view = repositoryA.createView();
        Assert.assertTrue(target + " must exist", repositoryA.exists(view, target, Revision.HEAD));

        final Info sInfo = repositoryA.info(view, source, Revision.HEAD);
        final Info tInfo = repositoryA.info(view, target, Revision.HEAD);
        Assert.assertEquals("must be same file", sInfo.getMd5(), tInfo.getMd5());

        final List<LogEntry> sLog = repositoryA.log(view, source, Revision.INITIAL, Revision.HEAD, 0, false);
        final List<LogEntry> tLog = repositoryA.log(view, target, Revision.INITIAL, Revision.HEAD, 0, false);
        Assert.assertEquals("must be same file", sLog.size(), tLog.size() - 1);
        Assert.assertEquals("logs must match", sLog, tLog.subList(0, sLog.size()));
    }

    @Test
    public void test04_FileDeleteLocked() throws Exception {
        final Resource resource = prefix.append(Resource.create("file_delete_locked.txt"));

        AbstractRepositoryAddIT.file(repositoryA, resource, "source", true);
        repositoryA.lock(resource, false);

        final Transaction transaction = repositoryA.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repositoryA.delete(transaction, resource);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertEquals("change set must contain: " + resource, Status.DELETED, transaction.getChangeSet().get(resource));
            repositoryA.commit(transaction, "deleted", true);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repositoryA.rollbackIfNotCommitted(transaction);
        }

        final View view = repositoryA.createView();
        Assert.assertFalse(resource + " must not exist", repositoryA.exists(view, resource, Revision.HEAD));
    }

    @Test
    public void test04_FileMoveToLocked() throws Exception {
        final Resource source = prefix.append(Resource.create("file_move_locked_source.txt"));
        final Resource target = prefix.append(Resource.create("file_move_locked_target.txt"));

        AbstractRepositoryAddIT.file(repositoryA, source, "source", true);
        AbstractRepositoryAddIT.file(repositoryA, target, "target", true);
        repositoryA.lock(target, false);

        final View beforeView = repositoryA.createView();
        final Info sInfo = repositoryA.info(beforeView, source, Revision.HEAD);
        final List<LogEntry> sLog = repositoryA.log(beforeView, source, Revision.INITIAL, Revision.HEAD, 0, false);

        final Transaction transaction = repositoryA.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repositoryA.move(transaction, source, target, true);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertEquals("change set must contain: " + target, Status.MODIFIED, transaction.getChangeSet().get(target));
            AbstractRepositoryMkdirIT.assertParentsMapped(target.getParent(), transaction);
            repositoryA.commit(transaction, "move", true);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repositoryA.rollbackIfNotCommitted(transaction);
        }

        final View afterView = repositoryA.createView();
        Assert.assertFalse(source + " must not exist", repositoryA.exists(afterView, source, Revision.HEAD));
        Assert.assertTrue(target + " must exist", repositoryA.exists(afterView, target, Revision.HEAD));

        final Info tInfo = repositoryA.info(afterView, target, Revision.HEAD);
        Assert.assertEquals("must be same file", sInfo.getMd5(), tInfo.getMd5());

        final List<LogEntry> tLog = repositoryA.log(afterView, target, Revision.INITIAL, Revision.HEAD, 0, false);
        Assert.assertEquals("must be same file", sLog.size(), tLog.size() - 1);
        Assert.assertEquals("logs must match", sLog, tLog.subList(0, sLog.size()));
    }

    @Test
    public void test04_FileUploadToLocked() throws Exception {
        final String content = "test";
        final Resource resource = prefix.append(Resource.create("file_upload_locked.txt"));

        AbstractRepositoryAddIT.file(repositoryA, resource, "resource", true);
        repositoryA.lock(resource, false);

        final Transaction transaction = repositoryA.createTransaction();
        try {
            repositoryA.add(transaction, resource, false, IOUtils.toInputStream(content, StandardCharsets.UTF_8));
            repositoryA.commit(transaction, "update", true);
        } finally {
            repositoryA.rollbackIfNotCommitted(transaction);
        }

        final InputStream expected = IOUtils.toInputStream(content, StandardCharsets.UTF_8);
        final View view = repositoryA.createView();
        final InputStream actual = repositoryA.download(view, resource, Revision.HEAD);
        AbstractRepositoryDownloadIT.assertEquals("content must match", expected, actual);
    }

    @Test
    public void test04_SetPropertiesOfLocked() throws Exception {
        final Resource resource = prefix.append(Resource.create("file_set_properties_locked.txt"));
        final Key key = new Key(Type.SUBVERSION_CUSTOM, "test");
        final ResourceProperty propertyA = new ResourceProperty(key, "A");
        final ResourceProperty propertyB = new ResourceProperty(key, "B");

        AbstractRepositoryAddIT.file(repositoryA, resource, "resource", true);
        AbstractRepositoryPropertiesSetIT.setProperties(repositoryA, resource, propertyA);
        repositoryA.lock(resource, false);

        final Transaction transaction = repositoryA.createTransaction();
        try {
            repositoryA.propertiesSet(transaction, resource, propertyB);
            repositoryA.commit(transaction, "update", true);
        } finally {
            repositoryA.rollbackIfNotCommitted(transaction);
        }

        final View view = repositoryA.createView();
        final Info info = repositoryA.info(view, resource, Revision.HEAD);
        final ResourceProperty[] actual = info.getProperties();
        Assert.assertEquals("expected number of properties", 1, actual.length);
        Assert.assertEquals("property must match", propertyB, actual[0]);
    }

    @Test
    public void test05_KeepLockOnCommit() throws Exception {
        final Resource resource = prefix.append(Resource.create("file_keep_lock_on_commit.txt"));

        AbstractRepositoryAddIT.file(repositoryA, resource, "resource", true);
        repositoryA.lock(resource, false);
        final View beforeView = repositoryA.createView();
        final Info afterLock = repositoryA.info(beforeView, resource, Revision.HEAD);
        Assert.assertTrue(resource + " must be locked", afterLock.isLocked());

        final Transaction transaction = repositoryA.createTransaction();
        try {
            repositoryA.add(transaction, resource, false, IOUtils.toInputStream("res", StandardCharsets.UTF_8));
            repositoryA.commit(transaction, "update", false);
        } finally {
            repositoryA.rollbackIfNotCommitted(transaction);
        }

        final View afterView = repositoryA.createView();
        final Info afterCommit = repositoryA.info(afterView, resource, Revision.HEAD);
        Assert.assertTrue(resource + " must be locked", afterCommit.isLocked());
    }

    @Test
    public void test05_ReleaseLockOnCommit() throws Exception {
        final Resource resource = prefix.append(Resource.create("file_release_lock_on_commit.txt"));

        AbstractRepositoryAddIT.file(repositoryA, resource, "resource", true);
        repositoryA.lock(resource, false);
        final View beforeView = repositoryA.createView();
        final Info afterLock = repositoryA.info(beforeView, resource, Revision.HEAD);
        Assert.assertTrue(resource + " must be locked", afterLock.isLocked());

        final Transaction transaction = repositoryA.createTransaction();
        try {
            repositoryA.add(transaction, resource, false, IOUtils.toInputStream("res", StandardCharsets.UTF_8));
            repositoryA.commit(transaction, "update", true);
        } finally {
            repositoryA.rollbackIfNotCommitted(transaction);
        }

        final View afterView = repositoryA.createView();
        final Info afterCommit = repositoryA.info(afterView, resource, Revision.HEAD);
        Assert.assertFalse(resource + " must not be locked", afterCommit.isLocked());
    }

}
