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

import java.util.UUID;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.View;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryResolveIT {

    private final Resource prefix;

    private final Repository repository;

    protected AbstractRepositoryResolveIT(final Repository repository, final UUID testId) {
        prefix = Resource.create("/" + testId + "/resolve");
        this.repository = repository;
    }

    @Test
    public void test01_deletedFile() throws Exception {
        final Resource resource = prefix.append(Resource.create("file_delete.txt"));

        AbstractRepositoryAddIT.file(repository, resource, "test", true);
        final View beforeView = repository.createView();
        final Info sInfo = repository.info(beforeView, resource, Revision.HEAD);

        final Transaction transaction = repository.createTransaction();
        try {
            repository.delete(transaction, resource);
            repository.commit(transaction, "delete", true);
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        final View afterView = repository.createView();
        Assert.assertFalse("source must not exist", repository.exists(afterView, resource, Revision.HEAD));

        final Info targetWithOld = repository.info(afterView, resource, sInfo.getRevision());
        AbstractRepositoryInfoIT.assertInfoEquals("info must match", sInfo, targetWithOld);
    }

    @Test
    public void test01_deletedFolder() throws Exception {
        final Resource resource = prefix.append(Resource.create("folder_delete/file.txt"));

        AbstractRepositoryAddIT.file(repository, resource, "test", true);
        final View beforeView = repository.createView();
        final Info sInfo = repository.info(beforeView, resource, Revision.HEAD);

        final Transaction transaction = repository.createTransaction();
        try {
            repository.delete(transaction, resource.getParent());
            repository.commit(transaction, "delete", true);
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        final View afterView = repository.createView();
        Assert.assertFalse("source must not exist", repository.exists(afterView, resource, Revision.HEAD));

        final Info targetWithOld = repository.info(afterView, resource, sInfo.getRevision());
        AbstractRepositoryInfoIT.assertInfoEquals("info must match", sInfo, targetWithOld);
    }
}
