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
package de.shadowhunt.subversion.internal;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.NavigableSet;
import java.util.UUID;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.LogEntry;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.View;
import de.shadowhunt.subversion.internal.action.Action;
import de.shadowhunt.subversion.internal.action.CreateViewActionHttpv1;
import de.shadowhunt.subversion.internal.action.DownloadActionHttpv1;
import de.shadowhunt.subversion.internal.action.DownloadUriActionHttpv1;
import de.shadowhunt.subversion.internal.action.ExistsActionHttpv1;
import de.shadowhunt.subversion.internal.action.InfoActionHttpv1;
import de.shadowhunt.subversion.internal.action.ListActionHttpv1;
import de.shadowhunt.subversion.internal.action.LogActionHttpv1;
import de.shadowhunt.subversion.internal.action.QualifiedVersionedResourceActionHttpv1;
import de.shadowhunt.subversion.internal.action.ResolveActionHttpv1;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

public class ReadOnlyRepositoryHttpv1 implements ReadOnlyRepositoryInternal {

    private final Resource basePath;

    private final URI baseUri;

    private final HttpClient client;

    private final HttpContext context;

    private final String prefix;

    private final UUID repositoryId;

    public ReadOnlyRepositoryHttpv1(final URI baseUri, final Resource basePath, final UUID repositoryId, final String prefix, final HttpClient client, final HttpContext context) {
        this.baseUri = baseUri;
        this.basePath = basePath;
        this.repositoryId = repositoryId;
        this.prefix = prefix;
        this.client = client;
        this.context = context;
    }

    @Override
    public View createView() {
        final Action<ViewInternal> action = new CreateViewActionHttpv1(this);
        return action.perform();
    }

    @Override
    public InputStream download(final View view, final Resource resource, final Revision revision) {
        final ViewInternal viewInternal = ViewInternal.from(view);
        final Action<InputStream> action = new DownloadActionHttpv1(this, viewInternal, resource, revision);
        return action.perform();
    }

    @Override
    public URI downloadURI(final View view, final Resource resource, final Revision revision) {
        final ViewInternal viewInternal = ViewInternal.from(view);
        final Action<URI> action = new DownloadUriActionHttpv1(this, viewInternal, resource, revision);
        return action.perform();
    }

    @Override
    public boolean exists(final View view, final Resource resource, final Revision revision) {
        final ViewInternal viewInternal = ViewInternal.from(view);
        final Action<Boolean> action = new ExistsActionHttpv1(this, viewInternal, resource, revision);
        return action.perform();
    }

    @Override
    public Resource getBasePath() {
        return basePath;
    }

    @Override
    public URI getBaseUri() {
        return baseUri;
    }

    @Override
    public HttpClient getClient() {
        return client;
    }

    @Override
    public HttpContext getContext() {
        return context;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return ProtocolVersion.HTTP_V1;
    }

    @Override
    public QualifiedResource getQualifiedResource(final Resource resource) {
        return new QualifiedResource(basePath, resource);
    }

    @Override
    public QualifiedResource getQualifiedVersionedResource(final QualifiedResource qualifiedResource, final Revision revision) {
        final Action<QualifiedResource> action = new QualifiedVersionedResourceActionHttpv1(this, qualifiedResource, revision);
        return action.perform();
    }

    @Override
    public UUID getRepositoryId() {
        return repositoryId;
    }

    @Override
    public URI getRequestUri(final QualifiedResource qualifiedResource) {
        return URIUtils.appendResources(baseUri, qualifiedResource);
    }

    @Override
    public Info info(final View view, final Resource resource, final Revision revision) {
        final ViewInternal viewInternal = ViewInternal.from(view);
        final Action<Info> action = new InfoActionHttpv1(this, viewInternal, resource, revision);
        return action.perform();
    }

    @Override
    public NavigableSet<Info> list(final View view, final Resource resource, final Revision revision, final Depth depth) {
        final ViewInternal viewInternal = ViewInternal.from(view);
        final Action<NavigableSet<Info>> action = new ListActionHttpv1(this, viewInternal, resource, revision, depth);
        return action.perform();
    }

    @Override
    public List<LogEntry> log(final View view, final Resource resource, final Revision startRevision, final Revision endRevision, final int limit, final boolean stopOnCopy) {
        final ViewInternal viewInternal = ViewInternal.from(view);
        final Action<List<LogEntry>> action = new LogActionHttpv1(this, viewInternal, resource, startRevision, endRevision, limit, stopOnCopy);
        return action.perform();
    }

    @Override
    public QualifiedResource resolve(final View view, final QualifiedResource qualifiedResource, final Revision revision, final boolean resolve) {
        final ViewInternal viewInternal = ViewInternal.from(view);
        final Action<QualifiedResource> action = new ResolveActionHttpv1(this, viewInternal, qualifiedResource, revision, resolve);
        return action.perform();
    }
}
