/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2019 shadowhunt (dev@shadowhunt.de)
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
package de.shadowhunt.subversion.internal;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Transaction;

public interface TransactionInternal extends Transaction, ViewInternal {

    static TransactionInternal from(final Transaction transaction) {
        if (transaction instanceof TransactionInternal) {
            return (TransactionInternal) transaction;
        }
        throw new SubversionException("transaction can not be used");
    }

    QualifiedResource getQualifiedCommitMessageResource(QualifiedResource qualifiedResource);

    QualifiedResource getQualifiedTransactionResource();

    QualifiedResource getQualifiedWorkingResource(QualifiedResource qualifiedResource);

    void invalidate();

    boolean register(Resource resource, Status status);
}
