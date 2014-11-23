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
package de.shadowhunt.subversion.internal.httpv2;

import java.net.URI;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.Transaction.Status;
import de.shadowhunt.subversion.internal.AbstractBaseRepository;
import de.shadowhunt.subversion.internal.CommitMessageOperation;
import de.shadowhunt.subversion.internal.MergeOperation;

class RepositoryImpl extends AbstractBaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger("de.shadowhunt.subversion.Repository");

    private static class ResourceMapperImpl implements ResourceMapper {

        private static final Resource CREATE_TRANSACTION = Resource.create("me");

        private final Resource prefix;

        public ResourceMapperImpl(final Resource prefix) {
            this.prefix = prefix;
        }

        @Override
        public Resource getCommitMessageResource(final Transaction transaction) {
            final Resource suffix = Resource.create("/txn/" + transaction.getId());
            return prefix.append(suffix);
        }

        @Override
        public Resource getCreateTransactionResource() {
            return prefix.append(CREATE_TRANSACTION);
        }

        @Override
        public Resource getPrefix() {
            return prefix;
        }

        @Override
        public Resource getRegisterResource(final Resource resource, final Revision revision) {
            throw new UnsupportedOperationException("Not supported by httpv2");
        }

        @Override
        public Resource getRegisterTransactionResource(final Transaction transaction) {
            throw new UnsupportedOperationException("Not supported by httpv2");
        }

        @Override
        public Resource getTransactionResource(final Transaction transaction) {
            return getCommitMessageResource(transaction);
        }

        @Override
        public Resource getVersionedResource(final Resource resource, final Revision revision) {
            assert (!Revision.HEAD.equals(revision)) : "must not be HEAD revision";
            final Resource suffix = Resource.create("/rvr/" + revision + Resource.SEPARATOR + resource);
            return prefix.append(suffix);
        }

        @Override
        public Resource getWorkingResource(final Transaction transaction) {
            final Resource suffix = Resource.create("/txr/" + transaction.getId());
            return prefix.append(suffix);
        }
    }

    RepositoryImpl(final URI repository, final Resource prefix, final HttpClient client, final HttpContext context) {
        super(repository, new ResourceMapperImpl(prefix), client, context);
    }

    @Override
    public void commit(final Transaction transaction, @Nullable final String message) {
        validateTransaction(transaction);

        LOGGER.trace("committing {} with message {}", transaction.getId(), message);

        if (transaction.isChangeSetEmpty()) {
            // empty change set => nothing to commit, release resources
            rollback(transaction);
            return;
        }

        final Resource messageResource = config.getCommitMessageResource(transaction);
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
        LOGGER.trace("creating new transaction");

        final Resource resource = config.getCreateTransactionResource();
        final Revision headRevision = determineHeadRevision();
        final CreateTransactionOperation cto = new CreateTransactionOperation(repository, repositoryId, resource, headRevision);
        return cto.execute(client, context);
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return ProtocolVersion.HTTP_V2;
    }

    @Override
    protected void registerResource(final Transaction transaction, final Resource resource, final Revision revision) {
        validateTransaction(transaction);
        transaction.register(resource, Status.EXISTS);
    }
}
