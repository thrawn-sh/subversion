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

import java.util.Map;
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
public abstract class AbstractRepositoryMkdir {

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
            repository.commit(transaction, "mkdir " + resource);
            Assert.assertFalse("transaction must be not active", transaction.isActive());
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }
    }

    private final Resource prefix;

    private final Repository repository;

    protected AbstractRepositoryMkdir(final Repository repository, final UUID testId) {
        prefix = Resource.create("/trunk/" + testId + "/mkdir");
        this.repository = repository;
    }

    @Test(expected = SubversionException.class)
    public void test00_invalid() throws Exception {
        final Resource resource = prefix.append(Resource.create("invalid"));

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            transaction.invalidate();
            Assert.assertFalse("transaction must not be active", transaction.isActive());
            repository.mkdir(transaction, resource, false);
            Assert.fail("must not complete");
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }
    }

    @Test(expected = SubversionException.class)
    public void test00_noParents() throws Exception {
        Assert.assertFalse(prefix + " does already exist", repository.exists(prefix, Revision.HEAD));
        final Resource resource = prefix.append(Resource.create("no_parents"));

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.mkdir(transaction, resource, false);
            Assert.fail("must not complete");
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
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
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }
    }

    @Test
    public void test01_mkdirBase() throws Exception {
        mkdir(repository, prefix, true);
    }

    @Test(expected = SubversionException.class)
    public void test02_mkdirExistingFile() throws Exception {
        final Resource resource = prefix.append(Resource.create("existing.txt"));

        AbstractRepositoryAdd.file(repository, resource, "test", true);
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
