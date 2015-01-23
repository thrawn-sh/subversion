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

import java.io.IOException;
import java.net.URI;

import javax.annotation.CheckForNull;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;

class InfoOperation extends PropfindOperation<Info> {

    private static final String LOCK_OWNER_HEADER = "X-SVN-Lock-Owner";

    InfoOperation(final URI repository, final Resource resource, final Resource marker, @CheckForNull final ResourceProperty.Key[] requestedProperties) {
        super(repository, resource, marker, Depth.EMPTY, requestedProperties);
    }

    @CheckForNull
    public Info execute(final HttpClient client, final HttpContext context) {
        return super.execute(client, context);
    }

    @Override
    @CheckForNull
    protected Info processResponse(final HttpResponse response) throws IOException {
        if (getStatusCode(response) == HttpStatus.SC_NOT_FOUND) {
            return null;
        }

        final Info info = InfoImplReader.read(getContent(response));
        if (info.isLocked()) {
            final Header header = response.getFirstHeader(LOCK_OWNER_HEADER);
            ((InfoImpl) info).setLockOwner(header.getValue());
        }
        return info;
    }
}
