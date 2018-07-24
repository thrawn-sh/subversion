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

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import de.shadowhunt.subversion.Depth;
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
public abstract class AbstractRepositoryListIT {

    public static final Resource PREFIX = Resource.create("/00000000-0000-0000-0000-000000000000/list");

    public static void assertListEquals(final String message, final Set<Info> expected, final Set<Info> actual) {
        Assert.assertEquals(message, expected.size(), actual.size());
        final Iterator<Info> eIt = expected.iterator();
        final Iterator<Info> aIt = actual.iterator();
        while (eIt.hasNext()) {
            final Info e = eIt.next();
            final Info a = aIt.next();
            AbstractRepositoryInfoIT.assertInfoEquals(message, e, a);
        }
    }

    private final ListLoader listLoader;

    private final Repository repository;

    protected AbstractRepositoryListIT(final Repository repository, final File root) {
        this.repository = repository;
        listLoader = new ListLoader(root, repository.getBasePath());
    }

    private String createMessage(final Resource resource, final Revision revision, final Depth depth) {
        return resource + ": @" + revision + " with depth: " + depth;
    }

    @Test(expected = SubversionException.class)
    public void test00_NonExistingResource() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/non_existing.txt"));
        final Revision revision = Revision.HEAD;

        repository.list(resource, revision, Depth.EMPTY);
        Assert.fail("list must not complete");
    }

    @Test(expected = SubversionException.class)
    public void test00_NonExistingRevision() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        // there should not be a such high revision
        final Revision revision = Revision.create(Integer.MAX_VALUE);

        repository.list(resource, revision, Depth.EMPTY);
        Assert.fail("list must not complete");
    }

    @Test
    public void test01_FileHead() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        final Revision revision = Revision.HEAD;

        for (final Depth depth : Depth.values()) {
            final Set<Info> expected = listLoader.load(resource, revision, depth);
            final String message = createMessage(resource, revision, depth);
            assertListEquals(message, expected, repository.list(resource, revision, depth));
        }
    }

    @Test
    public void test01_FileRevision() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_delete.txt"));
        final Revision revision = Revision.create(64);

        for (final Depth depth : Depth.values()) {
            final Set<Info> expected = listLoader.load(resource, revision, depth);
            final String message = createMessage(resource, revision, depth);
            assertListEquals(message, expected, repository.list(resource, revision, depth));
        }
    }

    @Test
    public void test01_FolderHead() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/folder"));
        final Revision revision = Revision.HEAD;

        for (final Depth depth : Depth.values()) {
            final Set<Info> expected = listLoader.load(resource, revision, depth);
            final String message = createMessage(resource, revision, depth);
            assertListEquals(message, expected, repository.list(resource, revision, depth));
        }
    }

    @Test
    public void test01_FolderRevision() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/folder_delete"));
        final Revision revision = Revision.create(72);

        for (final Depth depth : Depth.values()) {
            final Set<Info> expected = listLoader.load(resource, revision, depth);
            final String message = createMessage(resource, revision, depth);
            assertListEquals(message, expected, repository.list(resource, revision, depth));
        }
    }

    @Test
    public void test02_FileCopy() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_copy.txt"));
        final Revision revision = Revision.create(67);

        for (final Depth depth : Depth.values()) {
            final Set<Info> expected = listLoader.load(resource, revision, depth);
            final String message = createMessage(resource, revision, depth);
            assertListEquals(message, expected, repository.list(resource, revision, depth));
        }
    }

    @Test
    public void test02_FileMove() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_move.txt"));
        final Revision revision = Revision.create(69);

        for (final Depth depth : Depth.values()) {
            final Set<Info> expected = listLoader.load(resource, revision, depth);
            final String message = createMessage(resource, revision, depth);
            assertListEquals(message, expected, repository.list(resource, revision, depth));
        }
    }

    @Test
    public void test02_FolderCopy() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/folder_copy"));
        final Revision revision = Revision.create(73);

        for (final Depth depth : Depth.values()) {
            final Set<Info> expected = listLoader.load(resource, revision, depth);
            final String message = createMessage(resource, revision, depth);
            assertListEquals(message, expected, repository.list(resource, revision, depth));
        }
    }

    @Test
    public void test02_FolderMove() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/folder_move"));
        final Revision revision = Revision.create(75);

        for (final Depth depth : Depth.values()) {
            final Set<Info> expected = listLoader.load(resource, revision, depth);
            final String message = createMessage(resource, revision, depth);
            assertListEquals(message, expected, repository.list(resource, revision, depth));
        }
    }

    @Test
    public void test03_Base() throws Exception {
        final Resource resource = PREFIX;
        final Revision revision = Revision.HEAD;
        final Depth depth = Depth.INFINITY;

        final Set<Info> expected = listLoader.load(resource, revision, depth);
        final String message = createMessage(resource, revision, depth);
        assertListEquals(message, expected, repository.list(resource, revision, depth));
    }
}
