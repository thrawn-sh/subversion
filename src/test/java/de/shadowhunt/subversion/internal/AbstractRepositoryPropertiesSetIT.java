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

import java.util.UUID;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.ResourceProperty.Key;
import de.shadowhunt.subversion.ResourceProperty.Type;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.View;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryPropertiesSetIT {

    public static void setProperties(final Repository repository, final Resource resource, final ResourceProperty... properties) throws Exception {
        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.propertiesSet(transaction, resource, properties);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.commit(transaction, "add " + resource, true);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }

        final View view = repository.createView();
        final Info info = repository.info(view, resource, Revision.HEAD);
        final ResourceProperty[] actual = info.getProperties();
        Assert.assertEquals("expected number of properties", properties.length, actual.length);
        Assert.assertArrayEquals("properties must match", properties, actual);
    }

    private final Resource prefix;

    private final Repository repository;

    protected AbstractRepositoryPropertiesSetIT(final Repository repository, final UUID testId) {
        prefix = Resource.create("/" + testId + "/propset");
        this.repository = repository;
    }

    @Test(expected = SubversionException.class)
    public void test00_invalid() throws Exception {
        final Resource resource = prefix.append(Resource.create("invalid.txt"));
        final Key key = new Key(Type.SUBVERSION_CUSTOM, "test");
        final ResourceProperty property = new ResourceProperty(key, "test");

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            final TransactionInternal transactionInternal = TransactionInternal.from(transaction);
            transactionInternal.invalidate();
            Assert.assertFalse("transaction must not be active", transaction.isActive());
            repository.propertiesSet(transaction, resource, property);
            Assert.fail("must not complete");
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
    }

    @Test(expected = SubversionException.class)
    public void test00_NonExistingResource() throws Exception {
        final Resource resource = prefix.append(Resource.create("non_existing.txt"));
        final Key key = new Key(Type.SUBVERSION_CUSTOM, "test");
        final ResourceProperty property = new ResourceProperty(key, "test");
        final View view = repository.createView();
        Assert.assertFalse(resource + " does already exist", repository.exists(view, resource, Revision.HEAD));

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.propertiesSet(transaction, resource, property);
            Assert.fail("must not complete");
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
    }

    @Test(expected = SubversionException.class)
    public void test00_rollback() throws Exception {
        final Resource resource = prefix.append(Resource.create("rollback.txt"));
        final Key key = new Key(Type.SUBVERSION_CUSTOM, "test");
        final ResourceProperty property = new ResourceProperty(key, "test");

        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.propertiesSet(transaction, resource, property);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.rollback(transaction);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
    }

    @Test
    public void test01_setProperties() throws Exception {
        final Resource resource = prefix.append(Resource.create("file.txt"));
        final Key key = new Key(Type.SUBVERSION_CUSTOM, "test");
        final ResourceProperty property = new ResourceProperty(key, "test");

        AbstractRepositoryAddIT.file(repository, resource, "test", true);
        setProperties(repository, resource, property);
    }

    @Test
    public void test02_overrideProperties() throws Exception {
        final Resource resource = prefix.append(Resource.create("update.txt"));

        AbstractRepositoryAddIT.file(repository, resource, "test", true);
        final Key key = new Key(Type.SUBVERSION_CUSTOM, "test");
        final ResourceProperty property = new ResourceProperty(key, "test");
        setProperties(repository, resource, property);
        final Key keyNew = new Key(Type.SUBVERSION_CUSTOM, "new");
        final ResourceProperty newProperty = new ResourceProperty(keyNew, "new");
        final ResourceProperty existingProperty = new ResourceProperty(key, "B");
        setProperties(repository, resource, newProperty, existingProperty);
    }

    @Test
    public void test03_subversionProperties() throws Exception {
        final Resource resource = prefix.append(Resource.create("subversion_properties.txt"));

        AbstractRepositoryAddIT.file(repository, resource, "test", true);
        final ResourceProperty eolProperty = new ResourceProperty(ResourceProperty.Key.EOL_SYTLE, "native");
        final ResourceProperty executableProperty = new ResourceProperty(ResourceProperty.Key.EXECUTABLE, "*");
        final ResourceProperty keywordsProperty = new ResourceProperty(ResourceProperty.Key.KEYWORDS, "Author Id");
        final ResourceProperty mimeProperty = new ResourceProperty(ResourceProperty.Key.MIME_TYPE, "text/plain");

        setProperties(repository, resource, eolProperty, executableProperty, keywordsProperty, mimeProperty);
    }
}
