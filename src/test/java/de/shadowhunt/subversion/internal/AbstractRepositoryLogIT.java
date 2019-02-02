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

import java.io.File;
import java.util.List;

import de.shadowhunt.subversion.LogEntry;
import de.shadowhunt.subversion.ReadOnlyRepository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.View;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryLogIT {

    public static final Resource PREFIX = Resource.create("/00000000-0000-0000-0000-000000000000/log");

    public static final int UNLIMITED = 0;

    private final LogLoader logLoader;

    private final ReadOnlyRepository repository;

    private View view;

    protected AbstractRepositoryLogIT(final ReadOnlyRepository repository, final File root) {
        this.repository = repository;
        logLoader = new LogLoader(root, repository.getBasePath());
    }

    @Before
    public void before() throws Exception {
        view = repository.createView();
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

        repository.log(view, resource, start, end, limit, false);
        Assert.fail("log must not complete");
    }

    @Test(expected = SubversionException.class)
    public void test00_NonExistingResource() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/non_existing.txt"));
        final Revision start = Revision.INITIAL;
        final Revision end = Revision.HEAD;
        final int limit = UNLIMITED;

        repository.log(view, resource, start, end, limit, false);
        Assert.fail("log must not complete");
    }

    @Test(expected = SubversionException.class)
    public void test00_NonExistingStartRevision() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        // there should not be a such high revision
        final Revision start = Revision.create(Integer.MAX_VALUE);

        final Revision end = Revision.HEAD;
        final int limit = UNLIMITED;

        repository.log(view, resource, start, end, limit, false);
        Assert.fail("log must not complete");
    }

    @Test
    public void test01_FileHeadAllAscending() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        final Revision start = Revision.INITIAL;
        final Revision end = Revision.HEAD;
        final int limit = UNLIMITED;

        final List<LogEntry> expected = logLoader.load(resource, start, end, limit);
        final String message = createMessage(resource, start, end, limit);
        Assert.assertEquals(message, expected, repository.log(view, resource, start, end, limit, false));
    }

    @Test
    public void test01_FileHeadAllDescending() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        final Revision start = Revision.HEAD;
        final Revision end = Revision.INITIAL;
        final int limit = UNLIMITED;

        final List<LogEntry> expected = logLoader.load(resource, start, end, limit);
        final String message = createMessage(resource, start, end, limit);
        Assert.assertEquals(message, expected, repository.log(view, resource, start, end, limit, false));
    }

    @Test
    public void test01_FileHeadOnly2Ascending() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        final Revision start = Revision.INITIAL;
        final Revision end = Revision.HEAD;
        final int limit = 2;

        final List<LogEntry> expected = logLoader.load(resource, start, end, limit);
        final String message = createMessage(resource, start, end, limit);
        Assert.assertEquals(message, limit, expected.size());
        Assert.assertEquals(message, expected, repository.log(view, resource, start, end, limit, false));
    }

    @Test
    public void test01_FileHeadOnly2Descending() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        final Revision start = Revision.HEAD;
        final Revision end = Revision.INITIAL;
        final int limit = 2;

        final List<LogEntry> expected = logLoader.load(resource, start, end, limit);
        final String message = createMessage(resource, start, end, limit);
        Assert.assertEquals(message, limit, expected.size());
        Assert.assertEquals(message, expected, repository.log(view, resource, start, end, limit, false));
    }

    @Test
    public void test01_FileRevisionAscending() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_delete.txt"));
        final Revision start = Revision.INITIAL;
        final Revision end = Revision.create(82);
        final int limit = UNLIMITED;

        final List<LogEntry> expected = logLoader.load(resource, start, end, limit);
        final String message = createMessage(resource, start, end, limit);
        Assert.assertEquals(message, expected, repository.log(view, resource, start, end, limit, false));
    }

    @Test
    public void test01_FileRevisionDescending() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_delete.txt"));
        final Revision start = Revision.create(82);
        final Revision end = Revision.INITIAL;
        final int limit = UNLIMITED;

        final List<LogEntry> expected = logLoader.load(resource, start, end, limit);
        final String message = createMessage(resource, start, end, limit);
        Assert.assertEquals(message, expected, repository.log(view, resource, start, end, limit, false));
    }

    @Test
    public void test02_FileCopy() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_copy.txt"));
        final Revision start = Revision.INITIAL;
        final Revision end = Revision.create(85);
        final int limit = UNLIMITED;

        final List<LogEntry> expected = logLoader.load(resource, start, end, limit);
        final String message = createMessage(resource, start, end, limit);
        Assert.assertEquals(message, expected, repository.log(view, resource, start, end, limit, false));
    }

    @Test
    public void test02_FileMove() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_move.txt"));
        final Revision start = Revision.INITIAL;
        final Revision end = Revision.create(87);
        final int limit = UNLIMITED;

        // NOTE: determine last existing revision for loader
        final List<LogEntry> expected = logLoader.load(resource, start, Revision.create(86), limit);
        final String message = createMessage(resource, start, end, limit);
        Assert.assertEquals(message, expected, repository.log(view, resource, start, end, limit, false));
    }
}
