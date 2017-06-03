/**
 * Copyright © 2013-2017 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.shadowhunt.subversion.internal;

import java.io.File;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryInfoIT {

    public static final Resource PREFIX = Resource.create("/00000000-0000-0000-0000-000000000000/info");

    public static void assertInfoEquals(final String message, final Info expected, final Info actual) {
        Assert.assertEquals(message, expected.getCreationDate(), actual.getCreationDate());
        Assert.assertEquals(message, expected.getLastModifiedDate(), actual.getLastModifiedDate());
        Assert.assertEquals(message, expected.getLockOwner(), actual.getLockOwner());
        Assert.assertEquals(message, expected.getLockToken(), actual.getLockToken());
        Assert.assertEquals(message, expected.getMd5(), actual.getMd5());
        Assert.assertArrayEquals(message, expected.getProperties(), actual.getProperties());
        Assert.assertEquals(message, expected.getRepositoryId(), actual.getRepositoryId());
        Assert.assertEquals(message, expected.getRevision(), actual.getRevision());
        Assert.assertEquals(message, expected.getResource(), actual.getResource());

        Assert.assertEquals(message, expected, actual);
    }

    static String createMessage(final Resource resource, final Revision revision) {
        return resource + ": @" + revision;
    }

    private final InfoLoader infoLoader;

    private final Repository repository;

    protected AbstractRepositoryInfoIT(final Repository repository, final File root) {
        this.repository = repository;
        infoLoader = new InfoLoader(root, repository.getBasePath());
    }

    @Test(expected = SubversionException.class)
    public void test00_NonExistingResource() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/non_existing.txt"));
        final Revision revision = Revision.HEAD;

        repository.info(resource, revision);
        Assert.fail("info must not complete");
    }

    @Test(expected = SubversionException.class)
    public void test00_NonExistingRevision() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        // there should not be a such high revision
        final Revision revision = Revision.create(Integer.MAX_VALUE);

        repository.info(resource, revision);
        Assert.fail("info must not complete");
    }

    @Test
    public void test01_FileHead() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        final Revision revision = Revision.HEAD;

        final Info expected = infoLoader.load(resource, revision);
        final String message = createMessage(resource, revision);
        assertInfoEquals(message, expected, repository.info(resource, revision));
    }

    @Test
    public void test01_FileRevision() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        final Revision revision = Revision.create(48);

        final Info expected = infoLoader.load(resource, revision);
        final String message = createMessage(resource, revision);
        assertInfoEquals(message, expected, repository.info(resource, revision));
    }

    @Test
    public void test01_FolderHead() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/folder"));
        final Revision revision = Revision.HEAD;

        final Info expected = infoLoader.load(resource, revision);
        final String message = createMessage(resource, revision);
        assertInfoEquals(message, expected, repository.info(resource, revision));
    }

    @Test
    public void test01_FolderRevision() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/folder"));
        final Revision revision = Revision.create(56);

        final Info expected = infoLoader.load(resource, revision);
        final String message = createMessage(resource, revision);
        assertInfoEquals(message, expected, repository.info(resource, revision));
    }

    @Test
    public void test02_FileCopy() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_copy.txt"));
        final Revision revision = Revision.create(51);

        final Info expected = infoLoader.load(resource, revision);
        final String message = createMessage(resource, revision);
        assertInfoEquals(message, expected, repository.info(resource, revision));
    }

    @Test
    public void test02_FileMove() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_move.txt"));
        final Revision revision = Revision.create(53);

        final Info expected = infoLoader.load(resource, revision);
        final String message = createMessage(resource, revision);
        assertInfoEquals(message, expected, repository.info(resource, revision));
    }

    @Test
    public void test02_FolderCopy() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/folder_copy"));
        final Revision revision = Revision.create(57);

        final Info expected = infoLoader.load(resource, revision);
        final String message = createMessage(resource, revision);
        assertInfoEquals(message, expected, repository.info(resource, revision));
    }

    @Test
    public void test02_FolderMove() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/folder_move"));
        final Revision revision = Revision.create(59);

        final Info expected = infoLoader.load(resource, revision);
        final String message = createMessage(resource, revision);
        assertInfoEquals(message, expected, repository.info(resource, revision));
    }
}
