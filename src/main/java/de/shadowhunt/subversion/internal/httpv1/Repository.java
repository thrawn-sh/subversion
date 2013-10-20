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

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.internal.AbstractRepository;
import de.shadowhunt.subversion.internal.CommitMessageOperation;
import de.shadowhunt.subversion.internal.MergeOperation;
import de.shadowhunt.subversion.internal.RepositoryConfig;
import java.net.URI;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

/**
 * {@link Repository} supports subversion servers of version 1.6.X
 */
public class Repository extends AbstractRepository {

	public Repository(URI repository, RepositoryConfig config, HttpClient client, HttpContext context) {
		super(repository, config, client, context);
	}

	protected static final String PREFIX_VCC = "/!svn/vcc/";

	protected static final String PREFIX_VER = "/!svn/ver/";

	protected void checkout(final Transaction transaction) {
		final CheckoutOperation co = new CheckoutOperation(repository, Resource.create(PREFIX_VCC + "default"), config.getTransactionResource(transaction));
		co.execute(client, context);
	}

	@Override
	public Transaction createTransaction() {
		final CreateTransactionOperation cto = new CreateTransactionOperation(repository);
		Transaction transaction = cto.execute(client, context);

		// FIXME checkout request

		return transaction;
	}

	@Override
	public void commit(Transaction transaction, String message) {
		final Resource messageResource = config.getCommitMessageResource(transaction);
		final CommitMessageOperation cmo = new CommitMessageOperation(repository, messageResource, message);
		cmo.execute(client, context);

		final Resource mergeResource = config.getTransactionResource(transaction);
		final MergeOperation mo = new MergeOperation(repository, mergeResource, null); // FIXME locktoken
		mo.execute(client, context);
	}

	protected void prepareContentUpload(final Resource resource, final Transaction transaction, final Revision revision) {
		final CheckoutOperation co = new CheckoutOperation(repository, Resource.create(PREFIX_VER + revision).append(resource), config.getTransactionResource(transaction));
		co.execute(client, context);
	}
}
