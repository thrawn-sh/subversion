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

import java.net.URI;

/**
 * {@link RepositoryFactory} creates {@link Repository}
 */
public interface RepositoryFactory {

	/**
	 * @param repository {@link URI} to the root of the repository
	 * @param trustServerCertificat whether to trust all SSL certificates (see {@code NonValidatingX509TrustManager})
	 * @return a new {@link Repository} for given {@link URI}
	 */
	public Repository createRepository(URI repository, boolean trustServerCertificat);

	/**
	 * Determine whether the {@link ServerVersion} is supported by the {@link Repository} created by this factory
	 * @param version the {@link ServerVersion} of the server
	 * @return {@code true} if the {@link ServerVersion} is supported, otherwise {@code false}
	 */
	public boolean isServerVersionSupported(ServerVersion version);
}
