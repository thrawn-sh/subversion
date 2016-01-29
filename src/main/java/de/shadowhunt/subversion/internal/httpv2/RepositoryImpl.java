/**
 * Copyright (C) 2013-2016 shadowhunt (dev@shadowhunt.de)
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
import java.util.UUID;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.Transaction.Status;
import de.shadowhunt.subversion.internal.AbstractBaseRepository;
import de.shadowhunt.subversion.internal.CommitMessageOperation;
import de.shadowhunt.subversion.internal.MergeOperation;
import de.shadowhunt.subversion.internal.QualifiedResource;

import org.apache.commons.lang3.Validate;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RepositoryImpl extends AbstractBaseRepository {

    private static class ResourceMapperImpl implements ResourceMapper {

        private static final Resource CREATE_TRANSACTION = Resource.create("me");

        private final Resource prefix;

        ResourceMapperImpl(final Resource prefix) {
            this.prefix = prefix;
        }

        @Override
        public QualifiedResource getCommitMessageResource(final Transaction transaction) {
            final Resource suffix = Resource.create("/txn/" + transaction.getId());
            return new QualifiedResource(prefix, suffix);
        }

        @Override
        public QualifiedResource getCreateTransactionResource() {
            return new QualifiedResource(prefix, CREATE_TRANSACTION);
        }

        @Override
        public Resource getPrefix() {
            return prefix;
        }

        @Override
        public QualifiedResource getRegisterResource(final QualifiedResource resource, final Revision revision) {
            throw new UnsupportedOperationException("Not supported by httpv2");
        }

        @Override
        public QualifiedResource getRegisterTransactionResource(final Transaction transaction) {
            throw new UnsupportedOperationException("Not supported by httpv2");
        }

        @Override
        public QualifiedResource getTransactionResource(final Transaction transaction) {
            return getCommitMessageResource(transaction);
        }

        @Override
        public QualifiedResource getVersionedResource(final QualifiedResource resource, final Revision revision) {
            if (Revision.HEAD.equals(revision)) {
                throw new SubversionException("must not be HEAD revision");
            }
            final Resource suffix = Resource.create("/rvr/" + revision + Resource.SEPARATOR + resource.getValue());
            return new QualifiedResource(prefix, suffix);
        }

        @Override
        public QualifiedResource getWorkingResource(final Transaction transaction) {
            final Resource suffix = Resource.create("/txr/" + transaction.getId());
            return new QualifiedResource(prefix, suffix);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("de.shadowhunt.subversion.Repository");

    RepositoryImpl(final URI repository, final Resource base, final UUID id, final Resource prefix, final HttpClient client, final HttpContext context) {
        super(repository, base, id, new ResourceMapperImpl(prefix), client, context);
    }

    @Override
    public void commit(final Transaction transaction, final String message, final boolean releaseLocks) {
        validateTransaction(transaction);
        Validate.notNull(message, "message must not be null");

        LOGGER.trace("committing {} with message {}", transaction.getId(), message);

        if (transaction.isChangeSetEmpty()) {
            // empty change set => nothing to commit, release resources
            rollback(transaction);
            return;
        }

        final QualifiedResource messageResource = config.getCommitMessageResource(transaction);
        final CommitMessageOperation cmo = new CommitMessageOperation(repository, messageResource, message);
        cmo.execute(client, context);

        final Set<Info> lockTokenInfoSet = getInfoSetWithLockTokens(transaction);
        final QualifiedResource mergeResource = config.getTransactionResource(transaction);
        final MergeOperation mo = new MergeOperation(repository, mergeResource, lockTokenInfoSet, base, releaseLocks);
        mo.execute(client, context);
        // only invalidate after successful commit to allow rollback
        transaction.invalidate();
    }

    @Override
    public Transaction createTransaction() {
        LOGGER.trace("creating new transaction");

        final QualifiedResource resource = config.getCreateTransactionResource();
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
