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

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

public abstract class AbstractVoidOperation extends AbstractOperation<Void> {

	protected AbstractVoidOperation(final URI repository) {
		super(repository);
	}

	protected abstract void checkResponse(final HttpResponse response);

	@Override
	protected final Void processResponse(final HttpResponse response) {
		try {
			checkResponse(response);
		} finally {
			EntityUtils.consumeQuietly(response.getEntity());
		}
		return null;
	}

}
