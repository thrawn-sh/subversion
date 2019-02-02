/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2019 shadowhunt (dev@shadowhunt.de)
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

import java.util.Map;
import java.util.UUID;

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
public abstract class AbstractRepositoryMkdirIT {

    public static void assertParentsMapped(final Resource resource, final Transaction transaction) {
        final Map<Resource, Status> changeSet = transaction.getChangeSet();

        Resource current = resource;
        while (!Resource.ROOT.equals(current)) {
            Assert.assertEquals(current + " must be mapped", Status.EXISTS, changeSet.get(current));
            current = current.getParent();
        }
    }

    public static void mkdir(final Repository repository, final Resource resource, final boolean parents) throws Exception {
        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());

            repository.mkdir(transaction, resource, parents);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.commit(transaction, "mkdir " + resource, true);
            Assert.assertFalse("transaction must be not active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
    }

    private final Resource prefix;

    private final Repository repository;

    protected AbstractRepositoryMkdirIT(final Repository repository, final UUID testId) {
        prefix = Resource.create("/" + testId + "/mkdir");
        this.repository = repository;
    }

    @Test(expected = SubversionException.class)
    public void test00_invalid() throws Exception {
        final Resource resource = prefix.append(Resource.create("invalid"));

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            final TransactionInternal transactionInternal = TransactionInternal.from(transaction);
            transactionInternal.invalidate();
            Assert.assertFalse("transaction must not be active", transaction.isActive());
            repository.mkdir(transaction, resource, false);
            Assert.fail("must not complete");
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
    }

    @Test(expected = SubversionException.class)
    public void test00_noParents() throws Exception {
        final Transaction transaction = repository.createTransaction();

        Assert.assertFalse(prefix + " does already exist", repository.exists(transaction, prefix, Revision.HEAD));
        final Resource resource = prefix.append(Resource.create("no_parents"));
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.mkdir(transaction, resource, false);
            Assert.fail("must not complete");
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
    }

    @Test(expected = SubversionException.class)
    public void test00_rollback() throws Exception {
        final Resource resource = prefix.append(Resource.create("rollback"));

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.mkdir(transaction, resource, false);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertEquals("change set must contain: " + resource, Status.ADDED, transaction.getChangeSet().get(resource));
            assertParentsMapped(resource.getParent(), transaction);
            repository.rollback(transaction);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
    }

    @Test
    public void test01_mkdirBase() throws Exception {
        mkdir(repository, prefix, true);
    }

    @Test(expected = SubversionException.class)
    public void test02_mkdirExistingFile() throws Exception {
        final Resource resource = prefix.append(Resource.create("existing.txt"));

        AbstractRepositoryAddIT.file(repository, resource, "test", true);
        mkdir(repository, resource, false);
        Assert.fail("mkdir must not complete");
    }

    @Test
    public void test02_mkdirExistingFolder() throws Exception {
        final Resource resource = prefix.append(Resource.create("existing"));

        mkdir(repository, resource, true);
        mkdir(repository, resource, false);
    }

    @Test
    public void test02_mkdirFolders() throws Exception {
        final Resource resource = prefix.append(Resource.create("a/b/c"));

        mkdir(repository, resource, true);
    }

    @Test(expected = SubversionException.class)
    public void test02_mkdirFoldersWithoutParents() throws Exception {
        final Resource resource = prefix.append(Resource.create("b/c"));

        mkdir(repository, resource, false);
        Assert.fail("mkdir must not complete");
    }
}
