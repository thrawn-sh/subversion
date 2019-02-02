/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2019 shadowhunt (dev@shadowhunt.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.shadowhunt.subversion.http.client;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpRequest;
import org.apache.http.RequestLine;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;

/**
 * A {@link SubversionRequestRetryHandler} which retires all requested HTTP and DAV methods which should be idempotent according to RFC-2616.
 */
public class SubversionRequestRetryHandler extends DefaultHttpRequestRetryHandler {

    /**
     * Default value for retiring a request before giving up.
     */
    public static final int DEFAULT_RETRIES = 3;

    private final Map<String, Boolean> idempotentMethods;

    /**
     * Create a {@link SubversionRequestRetryHandler} with default settings.
     */
    public SubversionRequestRetryHandler() {
        this(DEFAULT_RETRIES, true);
    }

    /**
     * Create a {@link SubversionRequestRetryHandler}.
     *
     * @param retryCount
     *            number of times a method will be retried
     * @param requestSentRetryEnabled
     *            whether or not methods that have successfully sent their request will be retried
     */
    public SubversionRequestRetryHandler(final int retryCount, final boolean requestSentRetryEnabled) {
        super(retryCount, requestSentRetryEnabled);

        idempotentMethods = new HashMap<>();
        // http
        idempotentMethods.put("DELETE", Boolean.TRUE);
        idempotentMethods.put("GET", Boolean.TRUE);
        idempotentMethods.put("HEAD", Boolean.TRUE);
        idempotentMethods.put("OPTIONS", Boolean.TRUE);
        idempotentMethods.put("PUT", Boolean.TRUE);
        // NOTE: POST request are only used to create transactional resources in httpv2,
        // these POST requests, don't have any state => they can be retried
        idempotentMethods.put("POST", Boolean.TRUE);
        idempotentMethods.put("TRACE", Boolean.TRUE);

        // webdav
        idempotentMethods.put("CHECKOUT", Boolean.TRUE);
        idempotentMethods.put("COPY", Boolean.TRUE);
        idempotentMethods.put("LOCK", Boolean.TRUE);
        idempotentMethods.put("MERGE", Boolean.TRUE);
        idempotentMethods.put("MKACTIVITY", Boolean.TRUE);
        idempotentMethods.put("MKCOL", Boolean.FALSE); // not idempotent
        idempotentMethods.put("PROPFIND", Boolean.TRUE);
        idempotentMethods.put("PROPPATCH", Boolean.TRUE);
        idempotentMethods.put("REPORT", Boolean.TRUE);
        idempotentMethods.put("UNLOCK", Boolean.TRUE);
    }

    @Override
    protected boolean handleAsIdempotent(final HttpRequest request) {
        final RequestLine requestLine = request.getRequestLine();
        final String method = requestLine.getMethod();
        final String methodUppercase = method.toUpperCase(Locale.US);
        final Boolean idempotent = idempotentMethods.get(methodUppercase);
        return Boolean.TRUE.equals(idempotent);
    }
}
