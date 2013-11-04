/*
 * #%L
 * Shadowhunt Subversion
 * %%
 * Copyright (C) 2013 shadowhunt
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.shadowhunt.subversion.internal;

import java.net.URI;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.Version;

public interface RepositoryConfig {

	static Resource DEFAULT_PREFIX = Resource.create("/!svn");

	Repository create(URI repository, HttpClient client, HttpContext context);

	Resource getCommitMessageResource(Transaction transaction);

	Resource getCreateTransactionResource();

	Version getProtocolVersion();

	Resource getRegisterResource(Resource resource, Revision revision);

	Resource getRegisterTransactionResource(Transaction transaction);

	Resource getTransactionResource(Transaction transaction);

	Resource getVersionedResource(Resource resource, Revision revision);

	Resource getWorkingResource(Transaction transaction);
}
