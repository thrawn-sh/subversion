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
package de.shadowhunt.http.protocol;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.concurrent.ThreadSafe;
import org.apache.http.protocol.HttpContext;

/**
 * {@link ThreadLocalHttpContext} represents execution state of an HTTP process. It uses {@link ThreadLocal} to ensure thread-safety.
 */
@ThreadSafe
public class ThreadLocalHttpContext implements HttpContext {

	private final ThreadLocal<Map<String, Object>> threadLocalMap = new ThreadLocal<Map<String, Object>>();

	/**
	 * Removes all attributes in the thread-local context
	 */
	public void clear() {
		final Map<String, Object> map = getMap();
		map.clear();
	}

	@Override
	public Object getAttribute(final String id) {
		if (id == null) {
			throw new IllegalArgumentException("Id may not be null");
		}

		final Map<String, Object> map = getMap();
		return map.get(id);
	}

	protected Map<String, Object> getMap() {
		Map<String, Object> map = threadLocalMap.get();
		if (map == null) {
			map = new HashMap<String, Object>();
			threadLocalMap.set(map);
		}
		return map;
	}

	@Override
	public Object removeAttribute(final String id) {
		if (id == null) {
			throw new IllegalArgumentException("Id may not be null");
		}
		final Map<String, Object> map = getMap();
		return map.remove(id);
	}

	@Override
	public void setAttribute(final String id, final Object obj) {
		if (id == null) {
			throw new IllegalArgumentException("Id may not be null");
		}
		final Map<String, Object> map = getMap();
		map.put(id, obj);
	}
}
