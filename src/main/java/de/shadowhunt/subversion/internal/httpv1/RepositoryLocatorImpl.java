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
package de.shadowhunt.subversion.internal.httpv1;

import java.net.URI;
import java.util.UUID;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.internal.AbstractRepositoryLocator;
import de.shadowhunt.subversion.internal.Probe;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

public class RepositoryLocatorImpl extends AbstractRepositoryLocator {

    @Override
    public Repository create(final URI uri, final Probe probe, final HttpClient client, final HttpContext context) {
        final URI repository = probe.getBaseUri(uri);
        final Resource base = probe.getBasePath(uri);
        final Resource prefix = probe.getPrefix();
        final UUID id = determineRepositoryId(uri, prefix, client, context);
        return new RepositoryImpl(repository, base, id, prefix, client, context);
    }

    @Override
    public boolean isSupported(final Repository.ProtocolVersion version) {
        return Repository.ProtocolVersion.HTTP_V1 == version;
    }
}
