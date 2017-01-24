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

import java.util.UUID;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction;

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
        final Info sInfo = repository.info(resource, Revision.HEAD);

        final Transaction transaction = repository.createTransaction();
        try {
            repository.delete(transaction, resource);
            repository.commit(transaction, "delete", true);
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        Assert.assertFalse("source must not exist", repository.exists(resource, Revision.HEAD));

        final Info targetWithOld = repository.info(resource, sInfo.getRevision());
        AbstractRepositoryInfoIT.assertInfoEquals("info must match", sInfo, targetWithOld);
    }

    @Test
    public void test01_deletedFolder() throws Exception {
        final Resource resource = prefix.append(Resource.create("folder_delete/file.txt"));

        AbstractRepositoryAddIT.file(repository, resource, "test", true);
        final Info sInfo = repository.info(resource, Revision.HEAD);

        final Transaction transaction = repository.createTransaction();
        try {
            repository.delete(transaction, resource.getParent());
            repository.commit(transaction, "delete", true);
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        Assert.assertFalse("source must not exist", repository.exists(resource, Revision.HEAD));

        final Info targetWithOld = repository.info(resource, sInfo.getRevision());
        AbstractRepositoryInfoIT.assertInfoEquals("info must match", sInfo, targetWithOld);
    }
}
