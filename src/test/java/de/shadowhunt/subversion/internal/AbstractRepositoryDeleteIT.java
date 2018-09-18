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

import java.util.UUID;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.View;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryDeleteIT {

    private final Resource prefix;

    private final Repository repository;

    protected AbstractRepositoryDeleteIT(final Repository repository, final UUID testId) {
        prefix = Resource.create("/" + testId + "/delete");
        this.repository = repository;
    }

    @Test(expected = SubversionException.class)
    public void test00_inactive() throws Exception {
        final Resource resource = prefix.append(Resource.create("inactive.txt"));

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            final TransactionInternal transactionInternal = TransactionInternal.from(transaction);
            transactionInternal.invalidate();
            Assert.assertFalse("transaction must not be active", transaction.isActive());
            repository.delete(transaction, resource);
            Assert.fail("delete must not complete");
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
    }

    @Test(expected = SubversionException.class)
    public void test00_noExisting() throws Exception {
        final Transaction transaction = repository.createTransaction();

        Assert.assertFalse(prefix + " does already exist", repository.exists(transaction, prefix, Revision.HEAD));
        final Resource resource = prefix.append(Resource.create("non_existing.txt"));
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.delete(transaction, resource);
            Assert.fail("delete must not complete");
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
    }

    @Test
    public void test00_rollback() throws Exception {
        final Resource resource = prefix.append(Resource.create("rollback.txt"));
        AbstractRepositoryAddIT.file(repository, resource, "test", true);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.delete(transaction, resource);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.rollback(transaction);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
        final View view = repository.createView();
        Assert.assertTrue(resource + " must still exist", repository.exists(view, resource, Revision.HEAD));
    }

    @Test
    public void test01_deleteFile() throws Exception {
        final Resource resource = prefix.append(Resource.create("file.txt"));
        AbstractRepositoryAddIT.file(repository, resource, "test", true);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.delete(transaction, resource);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.commit(transaction, "deleted", true);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
        final View view = repository.createView();
        Assert.assertFalse(resource + " must not exist", repository.exists(view, resource, Revision.HEAD));
    }

    @Test
    public void test01_deleteFolder() throws Exception {
        final Resource resource = prefix.append(Resource.create("folder"));
        AbstractRepositoryMkdirIT.mkdir(repository, resource, false);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.delete(transaction, resource);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.commit(transaction, "deleted", true);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
        final View view = repository.createView();
        Assert.assertFalse(resource + " must not exist", repository.exists(view, resource, Revision.HEAD));
    }

    @Test
    public void test01_deleteFolderWithContent() throws Exception {
        final Resource root = prefix.append(Resource.create("folder_with_content"));
        AbstractRepositoryMkdirIT.mkdir(repository, root, false);

        final Resource file = root.append(Resource.create("file.txt"));
        AbstractRepositoryAddIT.file(repository, file, "test", true);

        final Resource subFolder = root.append(Resource.create("sub"));
        AbstractRepositoryMkdirIT.mkdir(repository, subFolder, false);

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.delete(transaction, root);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.commit(transaction, "deleted", true);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
        final View view = repository.createView();
        Assert.assertFalse(root + " must not exist", repository.exists(view, root, Revision.HEAD));
    }
}
