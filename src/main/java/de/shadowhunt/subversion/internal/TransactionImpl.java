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
package de.shadowhunt.subversion.internal;

import java.util.UUID;

import de.shadowhunt.subversion.Transaction;

public final class TransactionImpl extends RepositoryCache implements Transaction {

	private boolean active = true;

	private final String id;

	public TransactionImpl(AbstractBasicRepository repository, final String id) {
		super(repository);
		this.id = id;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof TransactionImpl)) {
			return false;
		}

		final TransactionImpl that = (TransactionImpl) o;

		if (!id.equals(that.id)) {
			return false;
		}

		return true;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public UUID getRepositoryId() {
		return repository.getRepositoryId();
	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = (31 * result) + getRepositoryId().hashCode();
		return result;
	}

	@Override
	public void invalidate() {
		active = false;
	}

	public boolean isActive() {
		return active;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Transaction [");
		sb.append("id=").append(id);
		sb.append(", repositoryId=").append(getRepositoryId());
		sb.append(", active=").append(active);
		sb.append(']');
		return sb.toString();
	}
}
