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

import java.io.InputStream;
import java.util.UUID;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.Transaction.Status;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AbstractRepositoryAdd {

    public static void file(final Repository repository, final Resource resource, final String content, final boolean initial) throws Exception {
        if (!initial) {
            Assert.assertTrue(resource + " must not exist", repository.exists(resource, Revision.HEAD));
        }

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.add(transaction, resource, true, Helper.getInputStream(content));
            Assert.assertTrue("transaction must be active", transaction.isActive());

            final Status expectedStatus = (initial) ? Status.ADDED : Status.MODIFIED;
            Assert.assertEquals("changeset must contain: " + resource, expectedStatus, transaction.getChangeSet().get(resource));
            repository.commit(transaction, "add " + resource);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }

        final InputStream expected = Helper.getInputStream(content);
        final InputStream actual = repository.download(resource, Revision.HEAD);
        AbstractRepositoryDownload.assertEquals("content must match", expected, actual);
    }

    private final Resource prefix;

    private final Repository repository;

    protected AbstractRepositoryAdd(final Repository repository, final UUID testId) {
        prefix = Resource.create("/trunk/" + testId + "/add");
        this.repository = repository;
    }

    @Test(expected = SubversionException.class)
    public void test00_invalid() throws Exception {
        final Resource resource = prefix.append(Resource.create("invalid.txt"));

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            transaction.invalidate();
            Assert.assertFalse("transaction must not be active", transaction.isActive());
            repository.add(transaction, resource, false, Helper.getInputStream("test"));
            Assert.fail("must not complete");
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }
    }

    @Test(expected = SubversionException.class)
    public void test00_noParents() throws Exception {
        Assert.assertFalse(prefix + " does already exist", repository.exists(prefix, Revision.HEAD));
        final Resource resource = prefix.append(Resource.create("no_parents.txt"));

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.add(transaction, resource, false, Helper.getInputStream("test"));
            Assert.fail("must not complete");
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }
    }

    @Test(expected = SubversionException.class)
    public void test00_rollback() throws Exception {
        final Resource resource = prefix.append(Resource.create("rollback.txt"));

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.add(transaction, resource, false, Helper.getInputStream("test"));
            Assert.assertTrue("transaction must be active", transaction.isActive());
            Assert.assertEquals("changeset must contain: " + resource, Status.ADDED, transaction.getChangeSet().get(resource));
            repository.rollback(transaction);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
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
