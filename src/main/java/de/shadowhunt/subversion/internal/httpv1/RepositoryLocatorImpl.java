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
