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

import de.shadowhunt.subversion.Repository;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryFactoryIT {

    private final AbstractHelper helper;

    protected AbstractRepositoryFactoryIT(final AbstractHelper helper) {
        this.helper = helper;
    }

    @Test
    public void test00_create() {
        final Repository repository = helper.getRepositoryA();
        Assert.assertNotNull("repository must not be null", repository);

        Assert.assertEquals("base uri must match", helper.getRepositoryBaseUri(), repository.getBaseUri());
        Assert.assertEquals("base path must match", AbstractHelper.BASE_PATH, repository.getBasePath());
        Assert.assertNotNull("protocol must not be null", repository.getProtocolVersion());
        Assert.assertNotNull("repository must not be null", repository.getRepositoryId());
    }

    @Test
    public void test01_deepPath() {
        final Repository repository = helper.getRepositoryDeepPath();
        Assert.assertNotNull("repository must not be null", repository);

        Assert.assertEquals("base uri must match", helper.getRepositoryDeepBaseUri(), repository.getBaseUri());
        Assert.assertEquals("base path must match", AbstractHelper.DEEP_PATH, repository.getBasePath());
        Assert.assertNotNull("protocol must not be null", repository.getProtocolVersion());
        Assert.assertNotNull("repository must not be null", repository.getRepositoryId());
    }

    @Test
    public void test02_createReadOnly() {
        final Repository repository = helper.getRepositoryReadOnly();
        Assert.assertNotNull("repository must not be null", repository);

        Assert.assertEquals("base uri must match", helper.getRepositoryReadOnlyBaseUri(), repository.getBaseUri());
        Assert.assertEquals("base path must match", AbstractHelper.BASE_PATH, repository.getBasePath());
        Assert.assertNotNull("protocol must not be null", repository.getProtocolVersion());
        Assert.assertNotNull("repository must not be null", repository.getRepositoryId());
    }

    @Test
    public void test03_createPath() {
        final Repository repository = helper.getRepositoryPath();
        Assert.assertNotNull("repository must not be null", repository);

        Assert.assertEquals("base uri must match", helper.getRepositoryPathBaseUri(), repository.getBaseUri());
        Assert.assertEquals("base path must match", AbstractHelper.BASE_PATH, repository.getBasePath());
        Assert.assertNotNull("protocol must not be null", repository.getProtocolVersion());
        Assert.assertNotNull("repository must not be null", repository.getRepositoryId());
    }
}
