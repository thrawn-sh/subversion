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

    ListOperation(final URI repository, final QualifiedResource resource, final Resource marker, final Depth depth, final ResourceProperty.Key[] keys) {
        super(repository, resource, marker, depth, keys);
    }

    @Override
    protected Optional<Set<Info>> processResponse(final HttpResponse response) throws IOException {
        if (getStatusCode(response) == HttpStatus.SC_NOT_FOUND) {
            return Optional.empty();
        }

        final Set<Info> result = new TreeSet<>(Info.RESOURCE_COMPARATOR);
        final List<Info> infoList = InfoImplReader.readAll(getContent(response));
        result.addAll(infoList);
        return Optional.of(result);
    }

}
