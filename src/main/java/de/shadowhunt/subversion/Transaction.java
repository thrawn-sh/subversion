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
package de.shadowhunt.subversion;

import java.util.UUID;

public final class Transaction {

	private boolean active = true;

	private final String id;

	private final UUID repositoryId;

	public Transaction(final UUID repositoryId, final String id) {
		this.id = id;
		this.repositoryId = repositoryId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Transaction)) {
			return false;
		}

		Transaction that = (Transaction) o;

		if (!id.equals(that.id)) {
			return false;
		}
		if (!repositoryId.equals(that.repositoryId)) {
			return false;
		}

		return true;
	}

	public String getId() {
		return id;
	}

	public UUID getRepositoryId() {
		return repositoryId;
	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + repositoryId.hashCode();
		return result;
	}

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
		sb.append(", repositoryId=").append(repositoryId);
		sb.append(", active=").append(active);
		sb.append(']');
		return sb.toString();
	}
}
