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
package de.shadowhunt.subversion.internal.action;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction.Status;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.RepositoryInternal;
import de.shadowhunt.subversion.internal.TransactionInternal;
import de.shadowhunt.subversion.internal.operation.CommitMessageOperationHttpv1;
import de.shadowhunt.subversion.internal.operation.MergeOperationHttpv1;
import de.shadowhunt.subversion.internal.operation.Operation;
import org.apache.commons.lang3.StringUtils;

public class CommitActionHttpv2 implements Action<Void> {

    private final String message;

    private final boolean releaseLocks;

    private final RepositoryInternal repository;

    private final TransactionInternal transaction;

    public CommitActionHttpv2(final RepositoryInternal repository, final TransactionInternal transaction, final String message, final boolean releaseLocks) {
        this.repository = repository;
        this.transaction = transaction;
        this.message = message;
        this.releaseLocks = releaseLocks;
    }

    private Set<Info> getInfoSetWithLockTokens() {
        final Map<Resource, Status> changeSet = transaction.getChangeSet();
        if (changeSet.isEmpty()) {
            return Collections.emptySet();
        }

        final Revision headRevision = transaction.getHeadRevision();

        final Set<Info> result = new TreeSet<>(Info.RESOURCE_COMPARATOR);
        for (final Map.Entry<Resource, Status> entry : changeSet.entrySet()) {
            final Status status = entry.getValue();
            if ((Status.EXISTS == status) || (Status.ADDED == status)) {
                continue;
            }

            final Resource resource = entry.getKey();
            final Info info = repository.info(transaction, resource, headRevision);
            if (info.isLocked()) {
                result.add(info);
            }
        }
        return result;
    }

    @Override
    public Void perform() {
        if (transaction.isChangeSetEmpty()) {
            // empty change set => nothing to commit, release resources
            repository.rollback(transaction);
            return null;
        }

        if (StringUtils.isNotBlank(message)) {
            final Revision headRevision = transaction.getHeadRevision();
            final String concreteRevisionValue = headRevision.toString();
            final Resource concreateRevisionResource = Resource.create(concreteRevisionValue);
            final QualifiedResource qualifiedConcreateRevisionResource = new QualifiedResource(Resource.ROOT, concreateRevisionResource);
            final QualifiedResource qualifiedCommitMessageResource = transaction.getQualifiedCommitMessageResource(qualifiedConcreateRevisionResource);
            final Operation<Void> commitMessageOperation = new CommitMessageOperationHttpv1(repository, qualifiedCommitMessageResource, message);
            commitMessageOperation.execute();
        }

        // only invalidate after successful commit to allow rollback
        final Set<Info> lockTokenInfoSet = getInfoSetWithLockTokens();
        final QualifiedResource qualifiedMergeResource = transaction.getQualifiedTransactionResource();
        final Operation<Void> mergeOperation = new MergeOperationHttpv1(repository, qualifiedMergeResource, lockTokenInfoSet, releaseLocks);
        mergeOperation.execute();
        transaction.invalidate();
        return null;
    }

}
