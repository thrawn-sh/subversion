/**
 * Copyright (C) 2013-2017 shadowhunt (dev@shadowhunt.de)
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
public abstract class AbstractRepositoryExistsIT {

    public static final Resource PREFIX = Resource.create("/00000000-0000-0000-0000-000000000000/exists");

    private final Repository repository;

    protected AbstractRepositoryExistsIT(final Repository repository) {
        this.repository = repository;
    }

    private String createMessage(final Resource resource, final Revision revision) {
        return resource + ": @" + revision;
    }

    @Test
    public void test00_NonExistingResource() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/non_existing.txt"));
        final Revision revision = Revision.HEAD;

        final String message = createMessage(resource, revision);
        Assert.assertFalse(message, repository.exists(resource, revision));
    }

    @Test(expected = SubversionException.class)
    public void test00_NonExistingRevision() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        // there should not be a such high revision
        final Revision revision = Revision.create(Integer.MAX_VALUE);

        repository.exists(resource, revision);
        Assert.fail("exists must not complete");
    }

    @Test
    public void test01_FileHead() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        final Revision revision = Revision.HEAD;

        final String message = createMessage(resource, revision);
        Assert.assertTrue(message, repository.exists(resource, revision));
    }

    @Test
    public void test01_FileRevision() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_delete.txt"));
        final Revision revision = Revision.create(32);

        final String message = createMessage(resource, revision);
        Assert.assertTrue(message, repository.exists(resource, revision));
    }

    @Test
    public void test01_FolderHead() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/folder"));
        final Revision revision = Revision.HEAD;

        final String message = createMessage(resource, revision);
        Assert.assertTrue(message, repository.exists(resource, revision));
    }

    @Test
    public void test01_FolderRevision() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/folder_delete"));
        final Revision revision = Revision.create(39);

        final String message = createMessage(resource, revision);
        Assert.assertTrue(message, repository.exists(resource, revision));
    }

    @Test
    public void test02_FileCopy() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_copy.txt"));
        final Revision revision = Revision.create(35);

        final String message = createMessage(resource, revision);
        Assert.assertTrue(message, repository.exists(resource, revision));
    }

    @Test
    public void test02_FileMove() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_move.txt"));
        final Revision revision = Revision.create(37);

        final String message = createMessage(resource, revision);
        Assert.assertTrue(message, repository.exists(resource, revision));
    }

    @Test
    public void test02_FolderCopy() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/folder_copy"));
        final Revision revision = Revision.create(41);

        final String message = createMessage(resource, revision);
        Assert.assertTrue(message, repository.exists(resource, revision));
    }

    @Test
    public void test02_FolderMove() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/folder_move"));
        final Revision revision = Revision.create(43);

        final String message = createMessage(resource, revision);
        Assert.assertTrue(message, repository.exists(resource, revision));
    }
}
