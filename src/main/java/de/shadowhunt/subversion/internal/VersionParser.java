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

import java.util.regex.Pattern;

import org.apache.commons.lang3.Validate;

import de.shadowhunt.subversion.Revision;

class VersionParser {

	private static final Pattern PATH_PATTERN = Pattern.compile("/");

	private final int prefixPathLength;

	VersionParser(final String prefixPath) {
		Validate.notNull(prefixPath, "prefixPath must not be null");

		this.prefixPathLength = PATH_PATTERN.split(prefixPath).length;
	}

	public Revision getRevisionFromPath(final String path) {
		Validate.notNull(path, "path must not be null");

		final String[] parts = PATH_PATTERN.split(path);
		final int revision = Integer.parseInt(parts[prefixPathLength + 2]); // prefixPathLength + $svn + bc/vrv + VERSION);
		if (revision == 0) {
			return Revision.EMPTY;
		}
		return Revision.create(revision);
	}
}
