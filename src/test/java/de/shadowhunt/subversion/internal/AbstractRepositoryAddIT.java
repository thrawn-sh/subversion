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
import java.util.UUID;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.Transaction.Status;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryAddIT {

    public static void file(final Repository repository, final Resource resource, final String content, final boolean initial) throws Exception {
        final Transaction transaction = repository.createTransaction();

        if (!initial) {
            Assert.assertTrue(resource + " must not exist", repository.exists(transaction, resource, Revision.HEAD));
        }

        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.add(transaction, resource, true, IOUtils.toInputStream(content, StandardCharsets.UTF_8));
            Assert.assertTrue("transaction must be active", transaction.isActive());

            final Status expectedStatus = (initial) ? Status.ADDED : Status.MODIFIED;
            Assert.assertEquals("change set must contain: " + resource, expectedStatus, transaction.getChangeSet().get(resource));
            repository.commit(transaction, "add " + resource, true);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        final InputStream expected = IOUtils.toInputStream(content, StandardCharsets.UTF_8);
        final InputStream actual = repository.download(transaction, resource, Revision.HEAD);
        AbstractRepositoryDownloadIT.assertEquals("content must match", expected, actual);
    }

    private final Resource prefix;

    private final Repository repository;

    protected AbstractRepositoryAddIT(final Repository repository, final UUID testId) {
        prefix = Resource.create("/" + testId + "/add");
        this.repository = repository;
    }

    @Test(expected = SubversionException.class)
    public void test00_invalid() throws Exception {
        final Resource resource = prefix.append(Resource.create("invalid.txt"));

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            final TransactionInternal transactionInternal = TransactionInternal.from(transaction);
            transactionInternal.invalidate();
            Assert.assertFalse("transaction must not be active", transaction.isActive());
            repository.add(transaction, resource, false, IOUtils.toInputStream("test", StandardCharsets.UTF_8));
            Assert.fail("must not complete");
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
    }

    @Test(expected = SubversionException.class)
    public void test00_noParents() throws Exception {
        final Transaction transaction = repository.createTransaction();

        Assert.assertFalse(prefix + " does already exist", repository.exists(transaction, prefix, Revision.HEAD));
        final Resource resource = prefix.append(Resource.create("no_parents.txt"));

        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.add(transaction, resource, false, IOUtils.toInputStream("test", StandardCharsets.UTF_8));
            Assert.fail("must not complete");
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
    }

    @Test(expected = SubversionException.class)
    public void test00_rollback() throws Exception {
        final Resource resource = prefix.append(Resource.create("rollback.txt"));

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.add(transaction, resource, false, IOUtils.toInputStream("test", StandardCharsets.UTF_8));
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertEquals("change set must contain: " + resource, Status.ADDED, transaction.getChangeSet().get(resource));
            repository.rollback(transaction);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
    }

    @Test
    public void test01_addFile() throws Exception {
        final Resource resource = prefix.append(Resource.create("file.txt"));

        file(repository, resource, "test", true);
    }

    @Test
    public void test02_updateFile() throws Exception {
        final Resource resource = prefix.append(Resource.create("update.txt"));

        file(repository, resource, "A", true);
        file(repository, resource, "B", false);
    }
}
