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
package de.shadowhunt.subversion.v1_6;

import java.net.URI;

import de.shadowhunt.subversion.AbstractRepositoryLockedWriteIT;
import de.shadowhunt.subversion.ServerVersion;

public class RepositoryNTLMAuthIT extends AbstractRepositoryLockedWriteIT {

	public RepositoryNTLMAuthIT() {
		super(URI.create("http://subversion-16.vm.shadowhunt.de/svn-ntlm/test"), ServerVersion.V1_6, "SVNDOMAIN\\svnuser", "svnpass", "svn-client.shadowhunt.de");
	}

	@Override
	protected String getUsername() {
		final String username = super.getUsername();
		final int indexOf = username.indexOf('\\');
		return username.substring(indexOf + 1);
	}
}
