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

import java.util.Map;
import java.util.UUID;

public interface Transaction {

	public static enum Status {
		ADDED("A", 2),
		DELETED("D", 3),
		MODIFIED("M", 1);

		private final String abbreviation;

		public final int order;

		private Status(final String abbreviation, final int order) {
			this.abbreviation = abbreviation;
			this.order = order;
		}

		@Override
		public String toString() {
			return abbreviation;
		}
	}

	Map<Resource, Status> getChangeSet();

	String getId();

	UUID getRepositoryId();

	void invalidate();

	boolean isActive();

	boolean isChangeSetEmpty();

	void register(Resource resource, Status status);

}
