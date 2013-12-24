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

import java.util.UUID;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryResolve {

    private final Resource prefix;

    private final Repository repository;

    protected AbstractRepositoryResolve(final Repository repository, final UUID testId) {
        prefix = Resource.create("/trunk/" + testId + "/resolve");
        this.repository = repository;
    }

    @Test
    public void test01_deleteFile() throws Exception {
        final Resource resource = prefix.append(Resource.create("file_delete.txt"));

        AbstractRepositoryAdd.file(repository, resource, "test", true);
        final Info sInfo = repository.info(resource, Revision.HEAD);

        final Transaction transaction = repository.createTransaction();
        try {
            repository.delete(transaction, resource);
            repository.commit(transaction, "delete");
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }

        Assert.assertFalse("source must not exist", repository.exists(resource, Revision.HEAD));

        final Info targetWithOld = repository.info(resource, sInfo.getRevision());
        AbstractRepositoryInfo.assertEquals("infos must match", sInfo, targetWithOld);
    }

    @Test
    public void test01_deleteFolder() throws Exception {
        final Resource resource = prefix.append(Resource.create("folder_delete/file.txt"));

        AbstractRepositoryAdd.file(repository, resource, "test", true);
        final Info sInfo = repository.info(resource, Revision.HEAD);

        final Transaction transaction = repository.createTransaction();
        try {
            repository.delete(transaction, resource.getParent());
            repository.commit(transaction, "delete");
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }

        Assert.assertFalse("source must not exist", repository.exists(resource, Revision.HEAD));

        final Info targetWithOld = repository.info(resource, sInfo.getRevision());
        AbstractRepositoryInfo.assertEquals("infos must match", sInfo, targetWithOld);
    }

    @Test
    public void test01_movedFile() throws Exception {
        final Resource source = prefix.append(Resource.create("file_moved_source.txt"));
        final Resource target = prefix.append(Resource.create("file_moved_renamed.txt"));

        AbstractRepositoryAdd.file(repository, source, "test", true);
        final Info sInfo = repository.info(source, Revision.HEAD);

        final Transaction transaction = repository.createTransaction();
        try {
            repository.move(transaction, source, target, true);
            repository.commit(transaction, "move");
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }

        Assert.assertFalse("source must not exist", repository.exists(source, Revision.HEAD));

        final Info targetWithOld = repository.info(target, sInfo.getRevision());
        AbstractRepositoryInfo.assertEquals("infos must match", sInfo, targetWithOld);
    }

    @Test
    public void test01_movedFolder() throws Exception {
        final Resource source = prefix.append(Resource.create("folder_move_source/file.txt"));
        final Resource target = prefix.append(Resource.create("folder_moved_renamed/file.txt"));

        AbstractRepositoryAdd.file(repository, source, "test", true);
        final Info sInfo = repository.info(source, Revision.HEAD);

        final Transaction transaction = repository.createTransaction();
        try {
            repository.move(transaction, source.getParent(), target.getParent(), true);
            repository.commit(transaction, "move");
        } catch (final Exception e) {
            repository.rollback(transaction);
            throw e;
        }

        Assert.assertFalse("source must not exist", repository.exists(source, Revision.HEAD));

        final Info targetWithOld = repository.info(target, sInfo.getRevision());
        AbstractRepositoryInfo.assertEquals("infos must match", sInfo, targetWithOld);
    }
}
