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
import java.util.Optional;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

class InfoOperation extends AbstractPropfindOperation<Optional<Info>> {

    private static final String LOCK_OWNER_HEADER = "X-SVN-Lock-Owner";

    protected final Resource basePath;

    InfoOperation(final URI repository, final Resource basePath, final QualifiedResource resource, final Resource marker, final ResourceProperty.Key[] keys) {
        super(repository, resource, marker, Depth.EMPTY, keys);
        this.basePath = basePath;
    }

    @Override
    protected Optional<Info> processResponse(final HttpResponse response) throws IOException {
        if (getStatusCode(response) == HttpStatus.SC_NOT_FOUND) {
            return Optional.empty();
        }

        final Info info = InfoImplReader.read(getContent(response), basePath);
        if (info.isLocked()) {
            final Header header = response.getFirstHeader(LOCK_OWNER_HEADER);
            ((InfoImpl) info).setLockOwner(header.getValue());
        }
        return Optional.of(info);
    }
}
