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

import de.shadowhunt.subversion.ReadOnlyRepository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.View;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

public interface ReadOnlyRepositoryInternal extends ReadOnlyRepository {

    HttpClient getClient();

    HttpContext getContext();

    String getPrefix();

    QualifiedResource getQualifiedResource(Resource resource);

    QualifiedResource getQualifiedVersionedResource(QualifiedResource qualifiedResource, Revision revision);

    URI getRequestUri(QualifiedResource qualifiedResource);

    QualifiedResource resolve(View view, QualifiedResource qualifiedResource, Revision revision, boolean resolve);
}
