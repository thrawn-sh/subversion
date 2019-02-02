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
import de.shadowhunt.subversion.View;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryTransactionIT {

    private final Repository repository;

    protected AbstractRepositoryTransactionIT(final Repository repository) {
        this.repository = repository;
    }

    protected abstract Transaction createInactiveTransaction(UUID repositoryId);

    @Test(expected = SubversionException.class)
    public void test00_commitInactiveTransaction() throws Exception {
        final Transaction transaction = createInactiveTransaction(repository.getRepositoryId());
        repository.commit(transaction, "empty commit", true);
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
        final Transaction transaction = createInactiveTransaction(repository.getRepositoryId());
        repository.rollback(transaction);
        Assert.fail("rollback of inactive transaction");
    }

    @Test
    public void test01_commit() throws Exception {
        final View beforeView = repository.createView();
        final Info before = repository.info(beforeView, Resource.ROOT, Revision.HEAD);

        final Transaction transaction = repository.createTransaction();
        Assert.assertTrue("transaction must be active", transaction.isActive());
        repository.commit(transaction, "empty commit", true);
        Assert.assertFalse("transaction must be inactive", transaction.isActive());

        final View afterView = repository.createView();
        final Info after = repository.info(afterView, Resource.ROOT, Revision.HEAD);
        AbstractRepositoryInfoIT.assertInfoEquals("empty commit", before, after);
    }
}
