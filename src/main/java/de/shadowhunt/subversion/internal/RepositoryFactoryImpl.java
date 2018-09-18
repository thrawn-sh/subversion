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
package de.shadowhunt.subversion.internal;

import java.net.URI;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.RepositoryFactory;
import de.shadowhunt.subversion.Transaction;

public class RepositoryFactoryImpl extends RepositoryFactory {

    @Override
    protected Repository createReadOnlyRepository0(final URI saneUri, final HttpClient client, final HttpContext context, final boolean validate) {
        return createRepository0(saneUri, client, context, validate);
    }

    @Override
    protected Repository createRepository0(final URI saneUri, final HttpClient client, final HttpContext context, final boolean validate) {
        final ProbeServerOperation operation = new ProbeServerOperation(saneUri);
        final Repository repository = operation.execute(client, context);

        if (validate) {
            // do we got the real repository root
            final Transaction transaction = repository.createTransaction();
            repository.rollback(transaction);
        }

        return repository;
    }
}
