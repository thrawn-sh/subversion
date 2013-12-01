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

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Transaction;

/**
 * Default implementation for {@link Transaction}
 */
public final class TransactionImpl extends RepositoryCache implements Transaction {

	private boolean active = true;

	private final Map<Resource, Status> changeSet = new TreeMap<Resource, Status>();

	private final String id;

	public TransactionImpl(final String id) {
		Validate.notNull(id, "id must not be null");

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
	public Map<Resource, Status> getChangeSet() {
		return new TreeMap<Resource, Status>(changeSet);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public UUID getRepositoryId() {
		return getRepository().getRepositoryId();
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
		changeSet.clear();
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public boolean isChangeSetEmpty() {
		if (changeSet.isEmpty()) {
			return true;
		}
		for (final Status status : changeSet.values()) {
			if (status != Status.EXISTS) {
				return false;
			}
		}
		// no modifications to repository
		return true;
	}

	@Override
	public boolean register(final Resource resource, final Status status) {
		Validate.notNull(resource, "resource must not be null");
		Validate.notNull(status, "status must not be null");

		final Status old = changeSet.put(resource, status);
		if (old != null) {
			// if we delete an newly added resource, we remove completely
			// remove the resource from the change set
			if ((Status.ADDED == old) && (Status.DELETED == status)) {
				changeSet.remove(resource);
				return true;
			}

			// previous value had higher order, and must therefore be preserved
			if (old.order > status.order) {
				changeSet.put(resource, old);
				return false;
			}
		}
		return true;
	}

	@Override
	public Status status(final Resource resource) {
		Validate.notNull(resource, "resource must not be null");

		return changeSet.get(resource);
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
