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
package de.shadowhunt.subversion.internal.operation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.annotation.Nullable;

import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.TransmissionException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;

public abstract class AbstractBaseOperation<E> implements Operation<E> {

    protected final HttpClient client;

    protected final HttpContext context;

    private final int[] expectedStatusCodes;

    protected AbstractBaseOperation(final HttpClient client, final HttpContext context, final int... expectedStatusCodes) {
        this.client = client;
        this.context = context;
        this.expectedStatusCodes = Arrays.copyOf(expectedStatusCodes, expectedStatusCodes.length);
    }

    protected final void check(final HttpResponse response) {
        final int statusCode = getStatusCode(response);

        if (isExpectedStatusCode(statusCode)) {
            return;
        }

        final String message;
        switch (statusCode) {
            case HttpStatus.SC_UNAUTHORIZED:
                message = "Missing or insufficient user credentials to execute operation";
                break;
            case HttpStatus.SC_FORBIDDEN:
                message = "Operation is not allowed";
                break;
            case HttpStatus.SC_NOT_FOUND:
                message = "Requested resource could not be found";
                break;
            case HttpStatus.SC_LOCKED:
                message = "Requested resource is locked by another user";
                break;
            default:
                message = "Unexpected server response";
                break;
        }

        throw new SubversionException(message, statusCode);
    }

    /**
     * as the Resource can not differ between files and directories each request for an directory (without ending '/') will result in a redirect (with ending '/'), if another call to a redirected URI occurs a CircularRedirectException is thrown, as we can't determine the real target we can't prevent
     * this from happening. Allowing circular redirects globally could lead to live locks on the other hand. Therefore we clear the redirection cache explicitly.
     */
    protected final void clearRedirects() {
        context.removeAttribute(HttpClientContext.REDIRECT_LOCATIONS);
    }

    protected abstract HttpUriRequest createRequest() throws IOException;

    @Override
    public E execute() {
        clearRedirects();
        try {
            final HttpUriRequest request = createRequest();
            return client.execute(request, this, context);
        } catch (final IOException e) {
            throw new TransmissionException(e);
        }
    }

    protected final InputStream getContent(final HttpResponse response) throws IOException {
        final HttpEntity entity = response.getEntity();
        if (entity == null) {
            throw new IOException("Invalid server response: entity is missing");
        }
        return entity.getContent();
    }

    protected final int getStatusCode(final HttpResponse response) {
        final StatusLine statusLine = response.getStatusLine();
        if (statusLine == null) {
            return -1;
        }
        return statusLine.getStatusCode();
    }

    @Override
    public E handleResponse(final HttpResponse response) throws IOException {
        check(response);
        return processResponse(response);
    }

    private boolean isExpectedStatusCode(final int statusCode) {
        for (final int expectedStatusCode : expectedStatusCodes) {
            if (statusCode == expectedStatusCode) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    protected abstract E processResponse(HttpResponse response) throws IOException;
}
