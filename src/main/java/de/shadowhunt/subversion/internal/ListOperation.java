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

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

class ListOperation extends AbstractPropfindOperation<Optional<Set<Info>>> {

    private final Resource basePath;

    ListOperation(final URI repository, final Resource basePath, final QualifiedResource resource, final Resource marker, final Depth depth, final ResourceProperty.Key[] keys) {
        super(repository, resource, marker, depth, keys);
        this.basePath = basePath;
    }

    @Override
    protected Optional<Set<Info>> processResponse(final HttpResponse response) throws IOException {
        if (getStatusCode(response) == HttpStatus.SC_NOT_FOUND) {
            return Optional.empty();
        }

        final Set<Info> result = new TreeSet<>(Info.RESOURCE_COMPARATOR);
        final List<Info> infoList = InfoImplReader.readAll(getContent(response), basePath);
        result.addAll(infoList);
        return Optional.of(result);
    }

}
