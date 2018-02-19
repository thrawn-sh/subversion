/**
 * Copyright © 2013-2018 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.shadowhunt.subversion.internal;

import java.net.URI;

import de.shadowhunt.subversion.Resource;

public class Probe {

    private final Resource basePath;

    private final Resource prefix;

    Probe(final Resource basePath, final Resource prefix) {
        this.basePath = basePath;
        this.prefix = prefix;
    }

    public Resource getBasePath() {
        return basePath;
    }

    public Resource getBasePath(final URI server) {
        final String completePath = server.getPath();
        final String repoPath = getBaseUri(server).getPath();
        final String suffix = completePath.substring(repoPath.length());
        return Resource.create(suffix);
    }

    public URI getBaseUri(final URI server) {
        return URIUtils.replacePathWithResources(server, basePath);
    }

    public Resource getPrefix() {
        return prefix;
    }
}
