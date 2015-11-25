/**
 * Copyright (C) 2013-2015 shadowhunt (dev@shadowhunt.de)
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
