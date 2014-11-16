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

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.ResourceProperty.Type;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.Transaction.Status;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AbstractRepositoryCombinedOperations {

    private final Resource prefix;

    private final Repository repository;

    protected AbstractRepositoryCombinedOperations(final Repository repository, final UUID testId) {
        prefix = Resource.create("/trunk/" + testId + "/combined");
        this.repository = repository;
    }

    @Test
    public void test01_AddFileAndFolder() throws Exception {
        final String content = "test";
        final Resource file = prefix.append(Resource.create("file_and_folder.txt"));
        final Resource folder = prefix.append(Resource.create("file_and_folder"));

        final Transaction transaction = repository.createTransaction();
        try {
            repository.add(transaction, file, true, AbstractHelper.getInputStream(content));
            repository.mkdir(transaction, folder, true);
            repository.commit(transaction, "add " + file + " " + folder);
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }

        {
            final InputStream expected = AbstractHelper.getInputStream(content);
            final InputStream actual = repository.download(file, Revision.HEAD);
            AbstractRepositoryDownload.assertEquals("content must match", expected, actual);
        }

        {
            final Info info = repository.info(folder, Revision.HEAD);
            Assert.assertTrue("folder exists", info.isDirectory());
        }
    }

    @Test
    public void test01_AddFileAndSetProperties() throws Exception {
        final String content = "test";
        final Resource resource = prefix.append(Resource.create("file_and_properties.txt"));
        final ResourceProperty property = new ResourceProperty(Type.CUSTOM, "foo", "bar");

        final Transaction transaction = repository.createTransaction();
        try {
            repository.add(transaction, resource, true, AbstractHelper.getInputStream(content));
            repository.propertiesSet(transaction, resource, property);
            Assert.assertEquals("change set must contain: " + resource, Status.ADDED, transaction.getChangeSet().get(resource));
            repository.commit(transaction, "add " + resource);
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }

        {
            final InputStream expected = AbstractHelper.getInputStream(content);
            final InputStream actual = repository.download(resource, Revision.HEAD);
            AbstractRepositoryDownload.assertEquals("content must match", expected, actual);
        }

        {
            final Info info = repository.info(resource, Revision.HEAD);
            final ResourceProperty[] actual = info.getProperties();
            Assert.assertEquals("expected number of properties", 1, actual.length);
            Assert.assertEquals("property must match", property, actual[0]);
        }
    }

    @Test
    public void test01_AddMultipleFiles() throws Exception {
        final String content = "test";
        final Resource resourceA = prefix.append(Resource.create("fileA.txt"));
        final Resource resourceB = prefix.append(Resource.create("fileB.txt"));

        final Transaction transaction = repository.createTransaction();
        try {
            repository.add(transaction, resourceA, true, AbstractHelper.getInputStream(content));
            repository.add(transaction, resourceB, true, AbstractHelper.getInputStream(content));
            repository.commit(transaction, "add " + resourceA + " " + resourceB);
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }

        final InputStream expectedA = AbstractHelper.getInputStream(content);
        final InputStream actualA = repository.download(resourceA, Revision.HEAD);
        AbstractRepositoryDownload.assertEquals("content must match", expectedA, actualA);

        final InputStream expectedB = AbstractHelper.getInputStream(content);
        final InputStream actualB = repository.download(resourceB, Revision.HEAD);
        AbstractRepositoryDownload.assertEquals("content must match", expectedB, actualB);
    }

    @Test
    public void test01_ModifyMultipleLockedFiles() throws Exception {
        final String content = "test";
        final Resource resourceA = prefix.append(Resource.create("lockedFileA.txt"));
        final Resource resourceB = prefix.append(Resource.create("lockedFileB.txt"));

        AbstractRepositoryAdd.file(repository, resourceA, "A", true);
        AbstractRepositoryAdd.file(repository, resourceB, "B", true);

        repository.lock(resourceA, false);
        repository.lock(resourceB, false);

        final Transaction transaction = repository.createTransaction();
        try {
            repository.add(transaction, resourceA, true, AbstractHelper.getInputStream(content));
            repository.add(transaction, resourceB, true, AbstractHelper.getInputStream(content));
            repository.commit(transaction, "add " + resourceA + " " + resourceB);
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }

        final InputStream expectedA = AbstractHelper.getInputStream(content);
        final InputStream actualA = repository.download(resourceA, Revision.HEAD);
        AbstractRepositoryDownload.assertEquals("content must match", expectedA, actualA);

        final InputStream expectedB = AbstractHelper.getInputStream(content);
        final InputStream actualB = repository.download(resourceB, Revision.HEAD);
        AbstractRepositoryDownload.assertEquals("content must match", expectedB, actualB);
    }

    @Test
    public void test02_OverrideFile() throws Exception {
        final String content = "test";
        final Resource resource = prefix.append(Resource.create("override.txt"));

        final Transaction transaction = repository.createTransaction();
        try {
            repository.add(transaction, resource, true, AbstractHelper.getInputStream("something else"));
            repository.add(transaction, resource, true, AbstractHelper.getInputStream(content));
            repository.commit(transaction, "add " + resource);
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }

        final InputStream expected = AbstractHelper.getInputStream(content);
        final InputStream actual = repository.download(resource, Revision.HEAD);
        AbstractRepositoryDownload.assertEquals("content must match", expected, actual);
    }

    @Test
    public void test02_SetAndDeleteProperties() throws Exception {
        final String content = "test";
        final Resource resource = prefix.append(Resource.create("override.txt"));

        final ResourceProperty propertyA = new ResourceProperty(Type.CUSTOM, "propertyA", "a");
        final ResourceProperty propertyB = new ResourceProperty(Type.CUSTOM, "propertyB", "b");
        final ResourceProperty propertyC = new ResourceProperty(Type.CUSTOM, "propertyC", "c");

        final Transaction transaction = repository.createTransaction();
        try {
            repository.add(transaction, resource, true, AbstractHelper.getInputStream(content));
            repository.propertiesSet(transaction, resource, propertyA, propertyB);
            repository.propertiesDelete(transaction, resource, propertyB);
            repository.propertiesSet(transaction, resource, propertyC);
            repository.commit(transaction, "properties " + resource);
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }

        {
            final InputStream expected = AbstractHelper.getInputStream(content);
            final InputStream actual = repository.download(resource, Revision.HEAD);
            AbstractRepositoryDownload.assertEquals("content must match", expected, actual);
        }

        {
            final Info info = repository.info(resource, Revision.HEAD);
            final ResourceProperty[] actual = info.getProperties();
            Assert.assertEquals("expected number of properties", 2, actual.length);
            Assert.assertArrayEquals("properties must match", new ResourceProperty[] { propertyA, propertyC }, actual);
        }
    }

    @Test
    public void test03_AddAndDelete() throws Exception {
        final String content = "test";
        final Resource resource = prefix.append(Resource.create("delete.txt"));

        final Info before = repository.info(Resource.ROOT, Revision.HEAD);
        final Transaction transaction = repository.createTransaction();
        try {
            repository.add(transaction, resource, true, AbstractHelper.getInputStream(content));
            repository.delete(transaction, resource);
            repository.commit(transaction, "empty " + resource);
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }
        final Info after = repository.info(Resource.ROOT, Revision.HEAD);

        Assert.assertFalse("must not exist", repository.exists(resource, Revision.HEAD));
        AbstractRepositoryInfo.assertEquals("repo not modified", before, after);
    }

    @Test
    public void test03_ModifyAndDeleteExisting() throws Exception {
        final String content = "test";
        final Resource resource = prefix.append(Resource.create("modify_delete.txt"));

        AbstractRepositoryAdd.file(repository, resource, "something", true);

        final Transaction transaction = repository.createTransaction();
        try {
            repository.add(transaction, resource, true, AbstractHelper.getInputStream(content));
            Assert.assertEquals("change set must contain: " + resource, Status.MODIFIED, transaction.getChangeSet().get(resource));
            repository.delete(transaction, resource);
            Assert.assertEquals("change set must contain: " + resource, Status.DELETED, transaction.getChangeSet().get(resource));
            repository.commit(transaction, "empty " + resource);
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }

        Assert.assertFalse("must not exist", repository.exists(resource, Revision.HEAD));
    }
}
