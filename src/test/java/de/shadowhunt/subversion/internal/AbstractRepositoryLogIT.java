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
import java.util.List;

import de.shadowhunt.subversion.Log;
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
public abstract class AbstractRepositoryLogIT {

    public static final Resource PREFIX = Resource.create("/00000000-0000-0000-0000-000000000000/log");

    public static final int UNLIMITED = 0;

    private final LogLoader logLoader;

    private final Repository repository;

    protected AbstractRepositoryLogIT(final Repository repository, final File root) {
        this.repository = repository;
        logLoader = new LogLoader(root, repository.getBasePath());
    }

    private String createMessage(final Resource resource, final Revision start, final Revision end, final int limit) {
        return resource + ": " + start + " -> " + end + " (" + limit + ")";
    }

    @Test(expected = SubversionException.class)
    public void test00_NonExistingEndRevision() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        final Revision start = Revision.INITIAL;
        // there should not be a such high revision
        final Revision end = Revision.create(Integer.MAX_VALUE);
        final int limit = UNLIMITED;

        repository.log(resource, start, end, limit, false);
        Assert.fail("log must not complete");
    }

    @Test(expected = SubversionException.class)
    public void test00_NonExistingResource() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/non_existing.txt"));
        final Revision start = Revision.INITIAL;
        final Revision end = Revision.HEAD;
        final int limit = UNLIMITED;

        repository.log(resource, start, end, limit, false);
        Assert.fail("log must not complete");
    }

    @Test(expected = SubversionException.class)
    public void test00_NonExistingStartRevision() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        // there should not be a such high revision
        final Revision start = Revision.create(Integer.MAX_VALUE);

        final Revision end = Revision.HEAD;
        final int limit = UNLIMITED;

        repository.log(resource, start, end, limit, false);
        Assert.fail("log must not complete");
    }

    @Test
    public void test01_FileHeadAllAscending() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        final Revision start = Revision.INITIAL;
        final Revision end = Revision.HEAD;
        final int limit = UNLIMITED;

        final List<Log> expected = logLoader.load(resource, start, end, limit);
        final String message = createMessage(resource, start, end, limit);
        Assert.assertEquals(message, expected, repository.log(resource, start, end, limit, false));
    }

    @Test
    public void test01_FileHeadAllDescending() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        final Revision start = Revision.HEAD;
        final Revision end = Revision.INITIAL;
        final int limit = UNLIMITED;

        final List<Log> expected = logLoader.load(resource, start, end, limit);
        final String message = createMessage(resource, start, end, limit);
        Assert.assertEquals(message, expected, repository.log(resource, start, end, limit, false));
    }

    @Test
    public void test01_FileHeadOnly2Ascending() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        final Revision start = Revision.INITIAL;
        final Revision end = Revision.HEAD;
        final int limit = 2;

        final List<Log> expected = logLoader.load(resource, start, end, limit);
        final String message = createMessage(resource, start, end, limit);
        Assert.assertEquals(message, limit, expected.size());
        Assert.assertEquals(message, expected, repository.log(resource, start, end, limit, false));
    }

    @Test
    public void test01_FileHeadOnly2Descending() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        final Revision start = Revision.HEAD;
        final Revision end = Revision.INITIAL;
        final int limit = 2;

        final List<Log> expected = logLoader.load(resource, start, end, limit);
        final String message = createMessage(resource, start, end, limit);
        Assert.assertEquals(message, limit, expected.size());
        Assert.assertEquals(message, expected, repository.log(resource, start, end, limit, false));
    }

    @Test
    public void test01_FileRevisionAscending() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_delete.txt"));
        final Revision start = Revision.INITIAL;
        final Revision end = Revision.create(82);
        final int limit = UNLIMITED;

        final List<Log> expected = logLoader.load(resource, start, end, limit);
        final String message = createMessage(resource, start, end, limit);
        Assert.assertEquals(message, expected, repository.log(resource, start, end, limit, false));
    }

    @Test
    public void test01_FileRevisionDescending() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_delete.txt"));
        final Revision start = Revision.create(82);
        final Revision end = Revision.INITIAL;
        final int limit = UNLIMITED;

        final List<Log> expected = logLoader.load(resource, start, end, limit);
        final String message = createMessage(resource, start, end, limit);
        Assert.assertEquals(message, expected, repository.log(resource, start, end, limit, false));
    }

    @Test
    public void test02_FileCopy() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_copy.txt"));
        final Revision start = Revision.INITIAL;
        final Revision end = Revision.create(85);
        final int limit = UNLIMITED;

        final List<Log> expected = logLoader.load(resource, start, end, limit);
        final String message = createMessage(resource, start, end, limit);
        Assert.assertEquals(message, expected, repository.log(resource, start, end, limit, false));
    }

    @Test
    public void test02_FileMove() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_move.txt"));
        final Revision start = Revision.INITIAL;
        final Revision end = Revision.create(87);
        final int limit = UNLIMITED;

        // NOTE: determine last existing revision for loader
        final List<Log> expected = logLoader.load(resource, start, Revision.create(86), limit);
        final String message = createMessage(resource, start, end, limit);
        Assert.assertEquals(message, expected, repository.log(resource, start, end, limit, false));
    }
}
