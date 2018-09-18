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

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

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
import de.shadowhunt.subversion.View;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryCombinedOperationsIT {

    private final Resource prefix;

    private final Repository repository;

    protected AbstractRepositoryCombinedOperationsIT(final Repository repository, final UUID testId) {
        prefix = Resource.create("/" + testId + "/combined");
        this.repository = repository;
    }

    @Test
    public void test01_AddFileAndFolder() throws Exception {
        final String content = "test";
        final Resource file = prefix.append(Resource.create("file_and_folder.txt"));
        final Resource folder = prefix.append(Resource.create("file_and_folder"));

        final Transaction transaction = repository.createTransaction();
        try {
            repository.add(transaction, file, true, IOUtils.toInputStream(content, StandardCharsets.UTF_8));
            repository.mkdir(transaction, folder, true);
            repository.commit(transaction, "add " + file + " " + folder, true);
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        final View view = repository.createView();
        final InputStream expected = IOUtils.toInputStream(content, StandardCharsets.UTF_8);
        final InputStream actual = repository.download(view, file, Revision.HEAD);
        AbstractRepositoryDownloadIT.assertEquals("content must match", expected, actual);

        final Info info = repository.info(view, folder, Revision.HEAD);
        Assert.assertTrue("folder exists", info.isDirectory());
    }

    @Test
    public void test01_AddFileAndSetProperties() throws Exception {
        final String content = "test";
        final Resource resource = prefix.append(Resource.create("file_and_properties.txt"));
        final ResourceProperty property = new ResourceProperty(Type.SUBVERSION_CUSTOM, "foo", "bar");

        final Transaction transaction = repository.createTransaction();
        try {
            repository.add(transaction, resource, true, IOUtils.toInputStream(content, StandardCharsets.UTF_8));
            repository.propertiesSet(transaction, resource, property);
            Assert.assertEquals("change set must contain: " + resource, Status.ADDED, transaction.getChangeSet().get(resource));
            repository.commit(transaction, "add " + resource, true);
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        final View view = repository.createView();
        final InputStream expected = IOUtils.toInputStream(content, StandardCharsets.UTF_8);
        final InputStream actual = repository.download(view, resource, Revision.HEAD);
        AbstractRepositoryDownloadIT.assertEquals("content must match", expected, actual);

        final Info info = repository.info(view, resource, Revision.HEAD);
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
            repository.add(transaction, resourceA, true, IOUtils.toInputStream(content, StandardCharsets.UTF_8));
            repository.add(transaction, resourceB, true, IOUtils.toInputStream(content, StandardCharsets.UTF_8));
            repository.commit(transaction, "add " + resourceA + " " + resourceB, true);
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        final View view = repository.createView();
        final InputStream expectedA = IOUtils.toInputStream(content, StandardCharsets.UTF_8);
        final InputStream actualA = repository.download(view, resourceA, Revision.HEAD);
        AbstractRepositoryDownloadIT.assertEquals("content must match", expectedA, actualA);

        final InputStream expectedB = IOUtils.toInputStream(content, StandardCharsets.UTF_8);
        final InputStream actualB = repository.download(view, resourceB, Revision.HEAD);
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
            repository.add(transaction, resourceA, true, IOUtils.toInputStream(content, StandardCharsets.UTF_8));
            repository.add(transaction, resourceB, true, IOUtils.toInputStream(content, StandardCharsets.UTF_8));
            repository.commit(transaction, "add " + resourceA + " " + resourceB, true);
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        final View view = repository.createView();
        final InputStream expectedA = IOUtils.toInputStream(content, StandardCharsets.UTF_8);
        final InputStream actualA = repository.download(view, resourceA, Revision.HEAD);
        AbstractRepositoryDownloadIT.assertEquals("content must match", expectedA, actualA);

        final InputStream expectedB = IOUtils.toInputStream(content, StandardCharsets.UTF_8);
        final InputStream actualB = repository.download(view, resourceB, Revision.HEAD);
        AbstractRepositoryDownloadIT.assertEquals("content must match", expectedB, actualB);
    }

    @Test
    public void test02_OverrideFile() throws Exception {
        final String content = "test";
        final Resource resource = prefix.append(Resource.create("override.txt"));

        final Transaction transaction = repository.createTransaction();
        try {
            repository.add(transaction, resource, true, IOUtils.toInputStream("something else", StandardCharsets.UTF_8));
            repository.add(transaction, resource, true, IOUtils.toInputStream(content, StandardCharsets.UTF_8));
            repository.commit(transaction, "add " + resource, true);
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        final View view = repository.createView();
        final InputStream expected = IOUtils.toInputStream(content, StandardCharsets.UTF_8);
        final InputStream actual = repository.download(view, resource, Revision.HEAD);
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
            repository.add(transaction, resource, true, IOUtils.toInputStream(content, StandardCharsets.UTF_8));
            repository.propertiesSet(transaction, resource, propertyA, propertyB);
            repository.propertiesDelete(transaction, resource, propertyB);
            repository.propertiesSet(transaction, resource, propertyC);
            repository.commit(transaction, "properties " + resource, true);
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        final View view = repository.createView();
        final InputStream expected = IOUtils.toInputStream(content, StandardCharsets.UTF_8);
        final InputStream actual = repository.download(view, resource, Revision.HEAD);
        AbstractRepositoryDownloadIT.assertEquals("content must match", expected, actual);

        final Info info = repository.info(view, resource, Revision.HEAD);
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
            repository.add(transaction, resource, true, IOUtils.toInputStream(content, StandardCharsets.UTF_8));
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
            repository.add(transaction, resource, true, IOUtils.toInputStream(content, StandardCharsets.UTF_8));
            Assert.assertEquals("change set must contain: " + resource, Status.MODIFIED, transaction.getChangeSet().get(resource));
            repository.delete(transaction, resource);
            Assert.assertEquals("change set must contain: " + resource, Status.DELETED, transaction.getChangeSet().get(resource));
            repository.commit(transaction, "empty " + resource, true);
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        final View view = repository.createView();
        Assert.assertFalse("must not exist", repository.exists(view, resource, Revision.HEAD));
    }

    @Test
    public void test04_InfoOfMovedLockedResource() throws Exception {
        final Resource resourceA = prefix.append(Resource.create("A_" + UUID.randomUUID().toString() + ".txt"));
        final Resource resourceB = prefix.append(Resource.create("B_" + UUID.randomUUID().toString() + ".txt"));

        AbstractRepositoryAddIT.file(repository, resourceA, "infoOfMovedLocked", true);

        final Transaction transaction = repository.createTransaction();
        try {
            repository.move(transaction, resourceA, resourceB, false);
            repository.commit(transaction, "move", true);
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        repository.lock(resourceB, true);

        final View view = repository.createView();
        final List<Log> logs = repository.log(view, resourceB, Revision.HEAD, Revision.INITIAL, 0, false);
        Assert.assertEquals("there must be 2 log entries", 2, logs.size());

        final Revision revisionB = logs.get(0).getRevision();
        final Revision revisionA = logs.get(1).getRevision();

        final Info infoB = repository.info(view, resourceB, revisionB);
        Assert.assertEquals("resource must match", resourceB, infoB.getResource());
        final Info infoA = repository.info(view, resourceB, revisionA);
        Assert.assertEquals("resource must match", resourceA, infoA.getResource());
    }
}
