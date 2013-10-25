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

import java.io.File;

import de.shadowhunt.subversion.Revision;

abstract class BaseLoader {

	protected static final File ROOT = new File("src/test/resources/dump");

	protected static Revision resolvedHeadRevision;

	protected static Revision resolve(final Revision revision) {
		if (Revision.HEAD.equals(revision)) {
			synchronized (revision) {
				if (resolvedHeadRevision == null) {
					resolvedHeadRevision = resolveHead();
				}
				return resolvedHeadRevision;
			}
		}
		return revision;
	}

	private static Revision resolveHead() {
		Revision revision = Revision.EMPTY;
		for (final File child : ROOT.listFiles()) {
			if (child.isDirectory()) {
				final String name = child.getName();
				final Revision current = Revision.create(Integer.parseInt(name));
				if (revision.compareTo(current) < 0) {
					revision = current;
				}
			}
		}
		return revision;
	}
}
