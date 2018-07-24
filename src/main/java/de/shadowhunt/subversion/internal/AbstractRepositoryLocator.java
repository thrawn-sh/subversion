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
