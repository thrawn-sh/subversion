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

import java.net.URI;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

public interface RepositoryLocator {

    Repository create(URI repository, Resource prefix, HttpClient client, HttpContext context);

    boolean isSupported(Repository.ProtocolVersion version);
}
