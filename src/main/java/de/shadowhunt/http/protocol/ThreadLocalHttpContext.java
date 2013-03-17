package de.shadowhunt.http.protocol;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.protocol.HttpContext;

public class ThreadLocalHttpContext implements HttpContext {

	private final ThreadLocal<Map<String, Object>> threadLocalMap = new ThreadLocal<Map<String, Object>>();

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
