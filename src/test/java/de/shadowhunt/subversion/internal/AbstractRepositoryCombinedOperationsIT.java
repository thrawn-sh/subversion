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
import java.util.List;
import java.util.UUID;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Log;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.ResourceProperty.Type;
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
public abstract class AbstractRepositoryCombinedOperationsIT {

    private final Resource prefix;

    private final Repository repository;

    protected AbstractRepositoryCombinedOperationsIT(final Repository repository, final UUID testId) {
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
            repository.add(transaction, file, true, IOUtils.toInputStream(content, AbstractHelper.UTF8));
            repository.mkdir(transaction, folder, true);
            repository.commit(transaction, "add " + file + " " + folder);
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        final InputStream expected = IOUtils.toInputStream(content, AbstractHelper.UTF8);
        final InputStream actual = repository.download(file, Revision.HEAD);
        AbstractRepositoryDownloadIT.assertEquals("content must match", expected, actual);

        final Info info = repository.info(folder, Revision.HEAD);
        Assert.assertTrue("folder exists", info.isDirectory());
    }

    @Test
    public void test01_AddFileAndSetProperties() throws Exception {
        final String content = "test";
        final Resource resource = prefix.append(Resource.create("file_and_properties.txt"));
        final ResourceProperty property = new ResourceProperty(Type.SUBVERSION_CUSTOM, "foo", "bar");

        final Transaction transaction = repository.createTransaction();
        try {
            repository.add(transaction, resource, true, IOUtils.toInputStream(content, AbstractHelper.UTF8));
            repository.propertiesSet(transaction, resource, property);
            Assert.assertEquals("change set must contain: " + resource, Status.ADDED, transaction.getChangeSet().get(resource));
            repository.commit(transaction, "add " + resource);
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        final InputStream expected = IOUtils.toInputStream(content, AbstractHelper.UTF8);
        final InputStream actual = repository.download(resource, Revision.HEAD);
        AbstractRepositoryDownloadIT.assertEquals("content must match", expected, actual);

        final Info info = repository.info(resource, Revision.HEAD);
        final ResourceProperty[] actualProperties = info.getProperties();
        Assert.assertEquals("expected number of properties", 1, actualProperties.length);
        Assert.assertEquals("property must match", property, actualProperties[0]);
    }

    @Test
    public void test01_AddMultipleFiles() throws Exception {
        final String content = "test";
        final Resource resourceA = prefix.append(Resource.create("fileA.txt"));
        final Resource resourceB = prefix.append(Resource.create("fileB.txt"));

        final Transaction transaction = repository.createTransaction();
        try {
            repository.add(transaction, resourceA, true, IOUtils.toInputStream(content, AbstractHelper.UTF8));
            repository.add(transaction, resourceB, true, IOUtils.toInputStream(content, AbstractHelper.UTF8));
            repository.commit(transaction, "add " + resourceA + " " + resourceB);
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        final InputStream expectedA = IOUtils.toInputStream(content, AbstractHelper.UTF8);
        final InputStream actualA = repository.download(resourceA, Revision.HEAD);
        AbstractRepositoryDownloadIT.assertEquals("content must match", expectedA, actualA);

        final InputStream expectedB = IOUtils.toInputStream(content, AbstractHelper.UTF8);
        final InputStream actualB = repository.download(resourceB, Revision.HEAD);
        AbstractRepositoryDownloadIT.assertEquals("content must match", expectedB, actualB);
    }

    @Test
    public void test01_ModifyMultipleLockedFiles() throws Exception {
        final String content = "test";
        final Resource resourceA = prefix.append(Resource.create("lockedFileA.txt"));
        final Resource resourceB = prefix.append(Resource.create("lockedFileB.txt"));

        AbstractRepositoryAddIT.file(repository, resourceA, "A", true);
        AbstractRepositoryAddIT.file(repository, resourceB, "B", true);

        repository.lock(resourceA, false);
        repository.lock(resourceB, false);

        final Transaction transaction = repository.createTransaction();
        try {
            repository.add(transaction, resourceA, true, IOUtils.toInputStream(content, AbstractHelper.UTF8));
            repository.add(transaction, resourceB, true, IOUtils.toInputStream(content, AbstractHelper.UTF8));
            repository.commit(transaction, "add " + resourceA + " " + resourceB);
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        final InputStream expectedA = IOUtils.toInputStream(content, AbstractHelper.UTF8);
        final InputStream actualA = repository.download(resourceA, Revision.HEAD);
        AbstractRepositoryDownloadIT.assertEquals("content must match", expectedA, actualA);

        final InputStream expectedB = IOUtils.toInputStream(content, AbstractHelper.UTF8);
        final InputStream actualB = repository.download(resourceB, Revision.HEAD);
        AbstractRepositoryDownloadIT.assertEquals("content must match", expectedB, actualB);
    }

    @Test
    public void test02_OverrideFile() throws Exception {
        final String content = "test";
        final Resource resource = prefix.append(Resource.create("override.txt"));

        final Transaction transaction = repository.createTransaction();
        try {
            repository.add(transaction, resource, true, IOUtils.toInputStream("something else", AbstractHelper.UTF8));
            repository.add(transaction, resource, true, IOUtils.toInputStream(content, AbstractHelper.UTF8));
            repository.commit(transaction, "add " + resource);
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        final InputStream expected = IOUtils.toInputStream(content, AbstractHelper.UTF8);
        final InputStream actual = repository.download(resource, Revision.HEAD);
        AbstractRepositoryDownloadIT.assertEquals("content must match", expected, actual);
    }

    @Test
    public void test02_SetAndDeleteProperties() throws Exception {
        final String content = "test";
        final Resource resource = prefix.append(Resource.create("override.txt"));

        final ResourceProperty propertyA = new ResourceProperty(Type.SUBVERSION_CUSTOM, "propertyA", "a");
        final ResourceProperty propertyB = new ResourceProperty(Type.SUBVERSION_CUSTOM, "propertyB", "b");
        final ResourceProperty propertyC = new ResourceProperty(Type.SUBVERSION_CUSTOM, "propertyC", "c");

        final Transaction transaction = repository.createTransaction();
        try {
            repository.add(transaction, resource, true, IOUtils.toInputStream(content, AbstractHelper.UTF8));
            repository.propertiesSet(transaction, resource, propertyA, propertyB);
            repository.propertiesDelete(transaction, resource, propertyB);
            repository.propertiesSet(transaction, resource, propertyC);
            repository.commit(transaction, "properties " + resource);
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        final InputStream expected = IOUtils.toInputStream(content, AbstractHelper.UTF8);
        final InputStream actual = repository.download(resource, Revision.HEAD);
        AbstractRepositoryDownloadIT.assertEquals("content must match", expected, actual);

        final Info info = repository.info(resource, Revision.HEAD);
        final ResourceProperty[] actualProperties = info.getProperties();
        Assert.assertEquals("expected number of properties", 2, actualProperties.length);
        final ResourceProperty[] expectedProperties = new ResourceProperty[] { propertyA, propertyC };
        Assert.assertArrayEquals("properties must match", expectedProperties, actualProperties);
    }

    @Test(expected = SubversionException.class)
    public void test03_AddAndDelete() throws Exception {
        final String content = "test";
        final Resource resource = prefix.append(Resource.create("delete.txt"));

        final Transaction transaction = repository.createTransaction();
        try {
            repository.add(transaction, resource, true, IOUtils.toInputStream(content, AbstractHelper.UTF8));
            repository.delete(transaction, resource);
            Assert.fail("must not complete");
        } finally {
            repository.rollback(transaction);
        }
    }

    @Test
    public void test03_ModifyAndDeleteExisting() throws Exception {
        final String content = "test";
        final Resource resource = prefix.append(Resource.create("modify_delete.txt"));

        AbstractRepositoryAddIT.file(repository, resource, "something", true);

        final Transaction transaction = repository.createTransaction();
        try {
            repository.add(transaction, resource, true, IOUtils.toInputStream(content, AbstractHelper.UTF8));
            Assert.assertEquals("change set must contain: " + resource, Status.MODIFIED, transaction.getChangeSet().get(resource));
            repository.delete(transaction, resource);
            Assert.assertEquals("change set must contain: " + resource, Status.DELETED, transaction.getChangeSet().get(resource));
            repository.commit(transaction, "empty " + resource);
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        Assert.assertFalse("must not exist", repository.exists(resource, Revision.HEAD));
    }

    @Test
    public void test04_InfoOfMovedLockedResource() throws Exception {
        final Resource resourceA = prefix.append(Resource.create("A_" + UUID.randomUUID().toString() + ".txt"));
        final Resource resourceB = prefix.append(Resource.create("B_" + UUID.randomUUID().toString() + ".txt"));

        AbstractRepositoryAddIT.file(repository, resourceA, "infoOfMovedLocked", true);

        final Transaction transaction = repository.createTransaction();
        try {
            repository.move(transaction, resourceA, resourceB, false);
            repository.commit(transaction, "move");
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        repository.lock(resourceB, true);

        final List<Log> logs = repository.log(resourceB, Revision.HEAD, Revision.INITIAL, 0, false);
        Assert.assertEquals("there must be 2 log entries", 2, logs.size());

        final Revision revisionB = logs.get(0).getRevision();
        final Revision revisionA = logs.get(1).getRevision();

        final Info infoB = repository.info(resourceB, revisionB);
        Assert.assertEquals("resource must match", resourceB, infoB.getResource());
        final Info infoA = repository.info(resourceB, revisionA);
        Assert.assertEquals("resource must match", resourceA, infoA.getResource());
    }
}
