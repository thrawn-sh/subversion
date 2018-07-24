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

import java.net.URI;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.View;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryDownloadUriIT {

    public static final Resource PREFIX = Resource.create("/00000000-0000-0000-0000-000000000000/download");

    private final Repository repository;

    protected AbstractRepositoryDownloadUriIT(final Repository repository) {
        this.repository = repository;
    }

    private String createMessage(final Resource resource, final Revision revision) {
        return resource + ": @" + revision;
    }

    @Test(expected = SubversionException.class)
    public void test00_NonExistingResource() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/non_existing.txt"));
        final Revision revision = Revision.HEAD;

        repository.downloadURI(resource, revision);
        Assert.fail("downloadURI must not complete");
    }

    @Test(expected = SubversionException.class)
    public void test00_NonExistingRevision() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        // there should not be a such high revision
        final Revision revision = Revision.create(Integer.MAX_VALUE);

        repository.downloadURI(resource, revision);
        Assert.fail("downloadURI must not complete");
    }

    @Test
    public void test01_FileHead() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        final Revision revision = Revision.HEAD;

        final View view = repository.createView();
        final AbstractBaseRepository ar = (AbstractBaseRepository) repository;
        final QualifiedResource qualifiedResource = new QualifiedResource(repository.getBasePath(), resource);
        final URI expected = URIUtils.appendResources(repository.getBaseUri(), ar.config.getVersionedResource(qualifiedResource, view.getHeadRevision()));
        final String message = createMessage(resource, revision);
        Assert.assertEquals(message, expected, repository.downloadURI(resource, revision));
    }

    @Test
    public void test01_FileRevision() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_delete.txt"));
        final Revision revision = Revision.create(22);

        final AbstractBaseRepository ar = (AbstractBaseRepository) repository;
        final QualifiedResource qualifiedResource = new QualifiedResource(repository.getBasePath(), resource);
        final URI expected = URIUtils.appendResources(repository.getBaseUri(), ar.config.getVersionedResource(qualifiedResource, revision));
        final String message = createMessage(resource, revision);
        Assert.assertEquals(message, expected, repository.downloadURI(resource, revision));
    }

    @Test
    public void test02_FileCopy() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_copy.txt"));
        final Revision revision = Revision.create(25);

        final AbstractBaseRepository ar = (AbstractBaseRepository) repository;
        final QualifiedResource qualifiedResource = new QualifiedResource(repository.getBasePath(), resource);
        final URI expected = URIUtils.appendResources(repository.getBaseUri(), ar.config.getVersionedResource(qualifiedResource, revision));
        final String message = createMessage(resource, revision);
        Assert.assertEquals(message, expected, repository.downloadURI(resource, revision));
    }

    @Test
    public void test02_FileMove() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_move.txt"));
        final Revision revision = Revision.create(27);

        final AbstractBaseRepository ar = (AbstractBaseRepository) repository;
        final QualifiedResource qualifiedResource = new QualifiedResource(repository.getBasePath(), resource);
        final URI expected = URIUtils.appendResources(repository.getBaseUri(), ar.config.getVersionedResource(qualifiedResource, revision));
        final String message = createMessage(resource, revision);
        Assert.assertEquals(message, expected, repository.downloadURI(resource, revision));
    }
}
