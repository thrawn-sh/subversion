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

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public final class RepositoryUtils {

	public static String retrieveContent(final InputStream download) throws IOException {
		try {
			return StringUtils.trimToEmpty(IOUtils.toString(download, "UTF-8"));
		} finally {
			IOUtils.closeQuietly(download);
		}
	}

	private RepositoryUtils() {
		// prevent instantiation
	}
}
