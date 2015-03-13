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

import java.util.UUID;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Transaction;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryDelete {

    private final Resource prefix;

    private final Repository repository;

    protected AbstractRepositoryDelete(final Repository repository, final UUID testId) {
        prefix = Resource.create("/trunk/" + testId + "/delete");
        this.repository = repository;
    }

    @Test(expected = SubversionException.class)
    public void test00_inactive() throws Exception {
        final Resource resource = prefix.append(Resource.create("inactive.txt"));

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            transaction.invalidate();
            Assert.assertFalse("transaction must not be active", transaction.isActive());
            repository.delete(transaction, resource);
            Assert.fail("delete must not complete");
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }
    }

    @Test(expected = SubversionException.class)
    public void test00_noExisting() throws Exception {
        Assert.assertFalse(prefix + " does already exist", repository.exists(prefix, Revision.HEAD));
        final Resource resource = prefix.append(Resource.create("non_existing.txt"));

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.delete(transaction, resource);
            Assert.fail("delete must not complete");
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }
    }

    @Test
    public void test00_rollback() throws Exception {
        final Resource resource = prefix.append(Resource.create("rollback.txt"));
        AbstractRepositoryAdd.file(repository, resource, "test", true);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.delete(transaction, resource);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.rollback(transaction);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }
        Assert.assertTrue(resource + " must still exist", repository.exists(resource, Revision.HEAD));
    }

    @Test
    public void test01_deleteFile() throws Exception {
        final Resource resource = prefix.append(Resource.create("file.txt"));
        AbstractRepositoryAdd.file(repository, resource, "test", true);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.delete(transaction, resource);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.commit(transaction, "deleted");
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }
        Assert.assertFalse(resource + " must not exist", repository.exists(resource, Revision.HEAD));
    }

    @Test
    public void test01_deleteFolder() throws Exception {
        final Resource resource = prefix.append(Resource.create("folder"));
        AbstractRepositoryMkdir.mkdir(repository, resource, false);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.delete(transaction, resource);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.commit(transaction, "deleted");
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }
        Assert.assertFalse(resource + " must not exist", repository.exists(resource, Revision.HEAD));
    }

    @Test
    public void test01_deleteFolderWithContent() throws Exception {
        final Resource root = prefix.append(Resource.create("folder_with_content"));
        AbstractRepositoryMkdir.mkdir(repository, root, false);

        final Resource file = root.append(Resource.create("file.txt"));
        AbstractRepositoryAdd.file(repository, file, "test", true);

        final Resource subFolder = root.append(Resource.create("sub"));
        AbstractRepositoryMkdir.mkdir(repository, subFolder, false);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.delete(transaction, root);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.commit(transaction, "deleted");
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }
        Assert.assertFalse(root + " must not exist", repository.exists(root, Revision.HEAD));
    }
}
