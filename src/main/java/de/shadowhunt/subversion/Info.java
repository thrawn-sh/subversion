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

import java.util.Comparator;

import javax.annotation.CheckForNull;

public interface Info {
	/**
	 * {@link java.util.Comparator} orders {@link de.shadowhunt.subversion.Info}s by their relative {@link Resource}
	 */
	Comparator<Info> RESOURCE_COMPARATOR = new Comparator<Info>() {

		@Override
		public int compare(final Info i1, final Info i2) {
			return i1.getResource().compareTo(i2.getResource());
		}
	};

	ResourceProperty[] getProperties();

	@CheckForNull
	String getLockOwner();

	@CheckForNull
	String getLockToken();

	@CheckForNull
	String getMd5();

	String getRepositoryUuid();

	Resource getResource();

	@CheckForNull
	String getResourcePropertyValue(String name);

	Revision getRevision();

	boolean isDirectory();

	boolean isFile();

	boolean isLocked();
}
