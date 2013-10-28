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
package de.shadowhunt.subversion.internal.httpv2;

import java.net.URI;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.Version;

public class RepositoryConfig implements de.shadowhunt.subversion.internal.RepositoryConfig {

	private final Resource prefix;

	public RepositoryConfig() {
		this(DEFAULT_PREFIX);
	}

	public RepositoryConfig(final Resource prefix) {
		this.prefix = prefix;
	}

	@Override
	public de.shadowhunt.subversion.Repository create(final URI repository, final HttpClient client, final HttpContext context) {
		return new Repository(repository, this, client, context);
	}

	@Override
	public Resource getCommitMessageResource(final Transaction transaction) {
		final Resource suffix = Resource.create("/txn/" + transaction.getId());
		return prefix.append(suffix);
	}

	@Override
	public Resource getPrefix() {
		return prefix;
	}

	@Override
	public Version getProtocolVersion() {
		return Version.HTTPv2;
	}

	@Override
	public Resource getTransactionResource(final Transaction transaction) {
		return getCommitMessageResource(transaction);
	}

	@Override
	public Resource getVersionedResource(final Revision revision) {
		final Resource suffix = Resource.create("/rvr/" + revision);
		return prefix.append(suffix);
	}

	@Override
	public Resource getWorkingResource(final Transaction transaction) {
		final Resource suffix = Resource.create("/txr/" + transaction.getId());
		return prefix.append(suffix);
	}
}
