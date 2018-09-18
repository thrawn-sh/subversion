/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2018 shadowhunt (dev@shadowhunt.de)
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

import java.net.URI;

import de.shadowhunt.subversion.BuildProperties;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

public final class DavTemplateRequest extends HttpEntityEnclosingRequestBase {

    private static final String USER_AGENT;

    static {
        final String version = BuildProperties.getBuildVersion();
        USER_AGENT = "SVN/" + version + " (https:/.shadowhunt.de/subversion)";
    }

    private final String method;

    /**
     * Create a new {@link DavTemplateRequest}.
     *
     * @param method
     *            HTTP method name
     * @param uri
     *            full qualified {@link URI} this {@link DavTemplateRequest} is directed to
     */
    public DavTemplateRequest(final String method, final URI uri) {
        this.method = method;
        setURI(uri);
        addHeader("User-Agent", USER_AGENT);
    }

    @Override
    public String getMethod() {
        return method;
    }
}
