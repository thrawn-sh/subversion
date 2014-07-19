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

import java.net.URI;
import java.util.UUID;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.RepositoryFactory;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryFactory {

    private final HttpClient client;

    private final HttpContext context;

    private final Repository repository;


    protected AbstractRepositoryFactory(final Repository repository, final HttpClient client, final HttpContext context) {
        this.repository = repository;
        this.client = client;
        this.context = context;
    }

    @Test
    public void test00_probe() {
        final RepositoryFactory factory = RepositoryFactory.getInstance();
        final Repository probeRepository = factory.probeRepository(repository.getBaseUri(), client, context);
        Assert.assertNotNull("probe repository must not be null", probeRepository);

        Assert.assertEquals("base uri must match", repository.getBaseUri(), probeRepository.getBaseUri());
        Assert.assertEquals("protocol version must match", repository.getProtocolVersion(), probeRepository.getProtocolVersion());
        Assert.assertEquals("repository id must match", repository.getRepositoryId(), probeRepository.getRepositoryId());
    }

    @Test
    public void test01_probe() {
        final RepositoryFactory factory = RepositoryFactory.getInstance();
        final URI uri = URI.create(repository.getBaseUri().toString() + "/" + UUID.randomUUID().toString());
        final Repository probeRepository = factory.probeRepository(uri, client, context);
        Assert.assertNotNull("probe repository must not be null", probeRepository);

        Assert.assertEquals("base uri must match", repository.getBaseUri(), probeRepository.getBaseUri());
        Assert.assertEquals("protocol version must match", repository.getProtocolVersion(), probeRepository.getProtocolVersion());
        Assert.assertEquals("repository id must match", repository.getRepositoryId(), probeRepository.getRepositoryId());
    }
}
