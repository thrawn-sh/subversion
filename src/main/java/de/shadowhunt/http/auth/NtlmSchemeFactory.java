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
package de.shadowhunt.http.auth;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.impl.auth.NTLMScheme;
import org.apache.http.params.HttpParams;

import de.shadowhunt.jcifs.JcifsEngine;

/**
 * Factory for {@link NTLMScheme} with {@link JcifsEngine} implementations
 */
@ThreadSafe
public class NtlmSchemeFactory implements AuthSchemeFactory {

	/**
	 * Singleton instance
	 */
	public static final AuthSchemeFactory INSTANCE = new NtlmSchemeFactory();

	@Override
	public AuthScheme newInstance(final HttpParams params) {
		return new NTLMScheme(JcifsEngine.INSTANCE);
	}
}
