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
package de.shadowhunt.subversion.internal.httpv1;

import java.net.URI;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.Transaction.Status;
import de.shadowhunt.subversion.internal.AbstractBasicRepository;
import de.shadowhunt.subversion.internal.CommitMessageOperation;
import de.shadowhunt.subversion.internal.MergeOperation;
import de.shadowhunt.subversion.internal.RepositoryCache;
import de.shadowhunt.subversion.internal.RepositoryConfig;

/**
 * {@link Repository} supports subversion servers of version 1.6.X
 */
public class Repository extends AbstractBasicRepository {

	protected static final String PREFIX_VCC = "/vcc/default";

	protected static final String PREFIX_VER = "/ver/";

	public Repository(final URI repository, final RepositoryConfig config, final HttpClient client, final HttpContext context) {
		super(repository, config, client, context);
	}

	@Override
	public void commit(final Transaction transaction, final String message) {
		validateTransaction(transaction);

		if (transaction.isChangeSetEmpty()) {
			// empty change set => nothing to commit, release resources
			rollback(transaction);
			return;
		}

		final RepositoryCache cache = fromTransaction(transaction);
		final Revision concreteRevision = cache.getConcreteRevision(Revision.HEAD);
		final Resource messageResource = config.getCommitMessageResource(transaction).append(Resource.create(concreteRevision.toString()));
		final CommitMessageOperation cmo = new CommitMessageOperation(repository, messageResource, message);
		cmo.execute(client, context);

		final Resource mergeResource = config.getTransactionResource(transaction);
		final MergeOperation mo = new MergeOperation(repository, mergeResource, null); // FIXME locktoken
		mo.execute(client, context);
		transaction.invalidate(); // only invalidate after successful commit to allow rollback
	}

	@Override
	public Transaction createTransaction() {
		final CreateTransactionOperation cto = new CreateTransactionOperation(repository, this);
		final Transaction transaction = cto.execute(client, context);

		// transaction resource must be explicitly registered
		final Resource resource = config.getPrefix().append(Resource.create(PREFIX_VCC));
		final Resource transactionResource = config.getTransactionResource(transaction);
		final CheckoutOperation co = new CheckoutOperation(repository, resource, transactionResource);
		co.execute(client, context);

		return transaction;
	}

	@Override
	protected void registerResource(final Transaction transaction, final Resource resource, final Revision revision) {
		final RepositoryCache cache = fromTransaction(transaction);
		if (cache.status(resource) != null) {
			return;
		}

		final Revision concreteRevision = cache.getConcreteRevision(revision);
		final Resource existingResource = config.getPrefix().append(Resource.create(PREFIX_VER + concreteRevision)).append(resource);
		final Resource transactionResource = config.getTransactionResource(transaction);
		final CheckoutOperation co = new CheckoutOperation(repository, existingResource, transactionResource);
		co.execute(client, context);
		transaction.register(resource, Status.EXISTED);
	}
}
