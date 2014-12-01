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

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Transaction;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryTransaction {

    private final Repository repository;

    protected AbstractRepositoryTransaction(final Repository repository) {
        this.repository = repository;
    }

    @Test(expected = SubversionException.class)
    public void test00_commitInactiveTransaction() throws Exception {
        final TransactionImpl transaction = new TransactionImpl("1", repository.getRepositoryId(), Revision.HEAD);
        repository.commit(transaction, "empty commit");
        Assert.fail("commit of inactive transaction");
    }

    @Test
    public void test00_rollback() throws Exception {
        final Transaction transaction = repository.createTransaction();
        Assert.assertTrue("transaction must be active", transaction.isActive());
        repository.rollback(transaction);
        Assert.assertFalse("transaction must be inactive", transaction.isActive());
    }

    @Test(expected = SubversionException.class)
    public void test00_rollbackInactiveTransaction() throws Exception {
        final TransactionImpl transaction = new TransactionImpl("1", repository.getRepositoryId(), Revision.HEAD);
        repository.rollback(transaction);
        Assert.fail("rollback of inactive transaction");
    }

    @Test
    public void test01_commit() throws Exception {
        final Info before = repository.info(Resource.ROOT, Revision.HEAD);
        final Transaction transaction = repository.createTransaction();
        Assert.assertTrue("transaction must be active", transaction.isActive());
        repository.commit(transaction, "empty commit");
        Assert.assertFalse("transaction must be inactive", transaction.isActive());
        final Info after = repository.info(Resource.ROOT, Revision.HEAD);
        AbstractRepositoryInfo.assertInfoEquals("empty commit", before, after);
    }
}
