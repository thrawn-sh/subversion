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

    InfoOperation(final URI repository, final Resource resource, final Resource marker, final ResourceProperty.Key[] keys) {
        super(repository, resource, marker, Depth.EMPTY, keys);
    }

    @Override
    protected Optional<Info> processResponse(final HttpResponse response) throws IOException {
        if (getStatusCode(response) == HttpStatus.SC_NOT_FOUND) {
            return Optional.empty();
        }

        final Info info = InfoImplReader.read(getContent(response));
        if (info.isLocked()) {
            final Header header = response.getFirstHeader(LOCK_OWNER_HEADER);
            ((InfoImpl) info).setLockOwner(header.getValue());
        }
        return Optional.of(info);
    }
}
