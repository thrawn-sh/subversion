/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2018 shadowhunt (dev@shadowhunt.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.shadowhunt.subversion.internal.httpv1;

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

        private static final Resource CREATE_TRANSACTION = Resource.create("act");

        private static final Resource REGISTER_TRANSACTION = Resource.create("/vcc/default");

        private final Resource prefix;

        ResourceMapperImpl(final Resource prefix) {
            this.prefix = prefix;
        }

        @Override
        public QualifiedResource getCommitMessageResource(final Transaction transaction) {
            final String transactionId = transaction.getId();
            final Resource suffix = Resource.create("/wbl/" + transactionId);
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
            if (Revision.HEAD.equals(revision)) {
                throw new SubversionException("must not be HEAD revision");
            }
            final String resourceValue = resource.getValue();
            final Resource suffix = Resource.create("/ver/" + revision + Resource.SEPARATOR + resourceValue);
            return new QualifiedResource(prefix, suffix);
        }

        @Override
        public QualifiedResource getRegisterTransactionResource(final Transaction transaction) {
            return new QualifiedResource(prefix, REGISTER_TRANSACTION);
        }

        @Override
        public QualifiedResource getTransactionResource(final Transaction transaction) {
            final String transactionId = transaction.getId();
            final Resource suffix = Resource.create("/act/" + transactionId);
            return new QualifiedResource(prefix, suffix);
        }

        @Override
        public QualifiedResource getVersionedResource(final QualifiedResource resource, final Revision revision) {
            if (Revision.HEAD.equals(revision)) {
                throw new SubversionException("must not be HEAD revision");
            }
            final String resourceValue = resource.getValue();
            final Resource suffix = Resource.create("/bc/" + revision + Resource.SEPARATOR + resourceValue);
            return new QualifiedResource(prefix, suffix);
        }

        @Override
        public QualifiedResource getWorkingResource(final Transaction transaction) {
            final String transactionId = transaction.getId();
            final Resource suffix = Resource.create("/wrk/" + transactionId);
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

        final String transactionId = transaction.getId();
        LOGGER.trace("committing {} with message {}", transactionId, message);

        if (transaction.isChangeSetEmpty()) {
            // empty change set => nothing to commit, release resources
            rollback(transaction);
            return;
        }

        final Revision concreteRevision = transaction.getHeadRevision();
        final String concreteRevisionValue = concreteRevision.toString();
        final Resource concreateRevisionResource = Resource.create(concreteRevisionValue);
        final QualifiedResource qualifiedConcreateRevisionResource = new QualifiedResource(concreateRevisionResource);
        final QualifiedResource commitMessageResource = config.getCommitMessageResource(transaction);
        final QualifiedResource messageResource = commitMessageResource.append(qualifiedConcreateRevisionResource);
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
        final Transaction transaction = createTransaction0();

        // transaction resource must be explicitly registered
        registerTransaction(transaction);

        return transaction;
    }

    private Transaction createTransaction0() {
        final QualifiedResource resource = config.getCreateTransactionResource();
        final Revision headRevision = determineHeadRevision();
        final CreateTransactionOperation cto = new CreateTransactionOperation(repository, repositoryId, resource, headRevision);
        return cto.execute(client, context);
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return ProtocolVersion.HTTP_V1;
    }

    @Override
    protected void registerResource(final Transaction transaction, final Resource resource, final Revision revision) {
        validateTransaction(transaction);

        final Revision concreteRevision = getConcreteRevision(transaction, revision);
        final QualifiedResource qualifiedResource = new QualifiedResource(base, resource);
        final QualifiedResource existingResource = config.getRegisterResource(qualifiedResource, concreteRevision);
        final QualifiedResource transactionResource = config.getTransactionResource(transaction);
        final CheckoutOperation co = new CheckoutOperation(repository, existingResource, transactionResource);
        co.execute(client, context);
        transaction.register(resource, Status.EXISTS);
    }

    private void registerTransaction(final Transaction transaction) {
        final QualifiedResource resource = config.getRegisterTransactionResource(transaction);
        final QualifiedResource transactionResource = config.getTransactionResource(transaction);
        final CheckoutOperation co = new CheckoutOperation(repository, resource, transactionResource);
        co.execute(client, context);
    }
}
