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
package de.shadowhunt.util;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;

import de.shadowhunt.subversion.Resource;

/**
 * {@link URIUtils} provides convenience methods to create properly escaped {@link URI}s to connect to http servers
 */
public final class URIUtils {

	public static URI createURI(final URI repository, final Resource... resources) {
		try {
			return createURI0(repository, resources);
		} catch (final URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Creates an {@link URI} from the given repository with the additional pathSuffix,
	 * by combining the path from the repository and the pathSuffix and properly escaping both
	 * @param repository {@link URI} repository to connect to
	 * @param pathSuffix additional path part that will be combined to the path from the repository
	 * @return the {@link URI} from the given repository with the additional pathSuffix
	 */
	public static URI createURI(final URI repository, final String pathSuffix) {
		try {
			return createURI0(repository, pathSuffix);
		} catch (final URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static URI createURI0(final URI repository, final Resource... resources) throws URISyntaxException {
		final URIBuilder builder = new URIBuilder();
		builder.setScheme(repository.getScheme());
		builder.setHost(repository.getHost());
		builder.setPort(repository.getPort());
		final StringBuilder completePath = new StringBuilder(repository.getPath());
		for (final Resource resource : resources) {
			completePath.append(resource.getValue());
		}
		builder.setPath(completePath.toString());
		return builder.build();
	}

	private static URI createURI0(final URI repository, final String pathSuffix) throws URISyntaxException {
		final URIBuilder builder = new URIBuilder();
		builder.setScheme(repository.getScheme());
		builder.setHost(repository.getHost());
		builder.setPort(repository.getPort());
		final String completePath = repository.getPath() + pathSuffix;
		builder.setPath(completePath);
		return builder.build();
	}

	private URIUtils() {
		// prevent instantiation
	}
}
