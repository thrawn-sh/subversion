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

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.SubversionException;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

public abstract class AbstractRepositoryLocator implements RepositoryLocator {

    private static final ResourceProperty.Key[] REPOSITORY_UUID = new ResourceProperty.Key[] { ResourceProperty.REPOSITORY_ID };

    protected static UUID determineRepositoryId(final URI repository, final Resource prefix, final HttpClient client, final HttpContext context) {
        final InfoOperation operation = new InfoOperation(repository, Resource.ROOT, new QualifiedResource(Resource.ROOT), prefix, REPOSITORY_UUID);
        final Optional<Info> info = operation.execute(client, context);
        return info.orElseThrow(() -> new SubversionException("No repository found at " + repository, HttpStatus.SC_BAD_REQUEST)).getRepositoryId();
    }
}
