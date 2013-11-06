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
import java.util.Set;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.Transaction.Status;
import de.shadowhunt.subversion.internal.AbstractBaseRepository;
import de.shadowhunt.subversion.internal.CommitMessageOperation;
import de.shadowhunt.subversion.internal.MergeOperation;
import de.shadowhunt.subversion.internal.RepositoryCache;
import de.shadowhunt.subversion.internal.RepositoryConfig;
import de.shadowhunt.subversion.internal.TransactionImpl;

/**
 * {@link Repository} supports subversion servers of version 1.6.X
 */
public class Repository extends AbstractBaseRepository {

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

		final Set<Info> lockTokenInfos = getInfosWithLockTokens(transaction);
		final Resource mergeResource = config.getTransactionResource(transaction);
		final MergeOperation mo = new MergeOperation(repository, mergeResource, lockTokenInfos);
		mo.execute(client, context);
		transaction.invalidate(); // only invalidate after successful commit to allow rollback
	}

	@Override
	public Transaction createTransaction() {
		final TransactionImpl transaction;

		{ // create transaction
			final Resource resource = config.getCreateTransactionResource();
			final CreateTransactionOperation cto = new CreateTransactionOperation(repository, resource);
			transaction = cto.execute(client, context);
			transaction.setRepository(this);
		}

		{// transaction resource must be explicitly registered
			final Resource resource = config.getRegisterTransactionResource(transaction);
			final Resource transactionResource = config.getTransactionResource(transaction);
			final CheckoutOperation co = new CheckoutOperation(repository, resource, transactionResource);
			co.execute(client, context);
		}

		return transaction;
	}

	@Override
	public ProtocolVersion getProtocolVersion() {
		return ProtocolVersion.HTTPv1;
	}

	@Override
	protected void registerResource(final Transaction transaction, final Resource resource, final Revision revision) {
		final RepositoryCache cache = fromTransaction(transaction);
		if (cache.status(resource) != null) {
			return;
		}

		final Revision concreteRevision = cache.getConcreteRevision(revision);
		final Resource existingResource = config.getRegisterResource(resource, concreteRevision);
		final Resource transactionResource = config.getTransactionResource(transaction);
		final CheckoutOperation co = new CheckoutOperation(repository, existingResource, transactionResource);
		co.execute(client, context);
		transaction.register(resource, Status.EXISTS);
	}
}
