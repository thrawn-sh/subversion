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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.ReadOnlyRepository;
import de.shadowhunt.subversion.ReadOnlyRepository.ProtocolVersion;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.RepositoryFactory;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.View;
import de.shadowhunt.subversion.internal.operation.Operation;
import de.shadowhunt.subversion.internal.operation.ProbeServerOperation;
import de.shadowhunt.subversion.internal.operation.SparseInfoOperationHttpv1;
import de.shadowhunt.subversion.internal.tracing.TracingReadOnlyRepository;
import de.shadowhunt.subversion.internal.tracing.TracingRepository;
import de.shadowhunt.subversion.internal.validate.ValidatingReadOnlyRepository;
import de.shadowhunt.subversion.internal.validate.ValidatingRepository;
import org.apache.commons.lang3.Validate;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

public class ReposiotryFactoryInternal implements RepositoryFactory {

    private static final String DEFAULT_FRAGMENT = null;

    private static final String DEFAULT_QUERY = null;

    private static final String DEFAULT_USER_INFO = null;

    private static final UUID DEFAULT_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private static URI sanitise(final URI uri, final Resource path) {
        try {
            final String scheme = uri.getScheme();
            final String host = uri.getHost();
            final int port = uri.getPort();
            final String pathValue = path.getValue();
            return new URI(scheme, DEFAULT_USER_INFO, host, port, pathValue, DEFAULT_QUERY, DEFAULT_FRAGMENT);
        } catch (final URISyntaxException e) {
            final String message = e.getMessage();
            throw new IllegalArgumentException(message, e);
        }
    }

    @Override
    public ReadOnlyRepository createReadOnlyRepository(final URI uri, final HttpClient client, final HttpContext context) {
        Validate.notNull(uri, "uri must not be null");
        Validate.notNull(client, "client must not be null");
        Validate.notNull(context, "context must not be null");

        final String path = uri.getPath();
        final Resource resource = Resource.create(path);
        final URI saneUri = sanitise(uri, resource);
        ReadOnlyRepository repository = createReadOnlyRepositoryInternal(saneUri, client, context);
        repository = new TracingReadOnlyRepository(repository);
        repository = new ValidatingReadOnlyRepository(repository);
        return repository;
    }

    public ReadOnlyRepositoryInternal createReadOnlyRepositoryInternal(final URI uri, final HttpClient client, final HttpContext context) {
        final Operation<ProbeResult> probeOperation = new ProbeServerOperation(uri, client, context);
        final ProbeResult probe = probeOperation.execute();

        final URI baseUri = probe.getBaseUri(uri);
        final Resource basePath = probe.getBasePath(uri);
        final String prefix = probe.getPrefix();
        final ProtocolVersion version = probe.getVersion();

        switch (version) {
            case HTTP_V1: {
                final ReadOnlyRepositoryInternal incompleteRepository = new ReadOnlyRepositoryHttpv1(baseUri, basePath, DEFAULT_UUID, prefix, client, context);
                final Info info = getSparseInfo(incompleteRepository);
                final UUID repositoryId = info.getRepositoryId();
                return new ReadOnlyRepositoryHttpv1(baseUri, basePath, repositoryId, prefix, client, context);
            }
            case HTTP_V2: {
                final ReadOnlyRepositoryInternal incompleteRepository = new ReadOnlyRepositoryHttpv2(baseUri, basePath, DEFAULT_UUID, prefix, client, context);
                final Info info = getSparseInfo(incompleteRepository);
                final UUID repositoryId = info.getRepositoryId();
                return new ReadOnlyRepositoryHttpv2(baseUri, basePath, repositoryId, prefix, client, context);
            }
            default:
                throw new SubversionException("Could not find suitable repository for " + uri);
        }
    }

    @Override
    public Repository createRepository(final URI uri, final HttpClient client, final HttpContext context) {
        Validate.notNull(uri, "uri must not be null");
        Validate.notNull(client, "client must not be null");
        Validate.notNull(context, "context must not be null");

        final String path = uri.getPath();
        final Resource resource = Resource.create(path);
        final URI saneUri = sanitise(uri, resource);
        Repository repository = createRepositoryInternal(saneUri, client, context);
        repository = new TracingRepository(repository);
        repository = new ValidatingRepository(repository);
        return repository;
    }

    public RepositoryInternal createRepositoryInternal(final URI uri, final HttpClient client, final HttpContext context) {
        final Operation<ProbeResult> probeOperation = new ProbeServerOperation(uri, client, context);
        final ProbeResult probe = probeOperation.execute();

        final URI baseUri = probe.getBaseUri(uri);
        final Resource basePath = probe.getBasePath(uri);
        final String prefix = probe.getPrefix();
        final ProtocolVersion version = probe.getVersion();

        switch (version) {
            case HTTP_V1: {
                final ReadOnlyRepositoryInternal incompleteRepository = new ReadOnlyRepositoryHttpv1(baseUri, basePath, DEFAULT_UUID, prefix, client, context);
                final Info info = getSparseInfo(incompleteRepository);
                final UUID repositoryId = info.getRepositoryId();
                return new RepositoryHttpv1(baseUri, basePath, repositoryId, prefix, client, context);
            }
            case HTTP_V2: {
                final ReadOnlyRepositoryInternal incompleteRepository = new ReadOnlyRepositoryHttpv2(baseUri, basePath, DEFAULT_UUID, prefix, client, context);
                final Info info = getSparseInfo(incompleteRepository);
                final UUID repositoryId = info.getRepositoryId();
                return new RepositoryHttpv2(baseUri, basePath, repositoryId, prefix, client, context);
            }
            default:
                throw new SubversionException("Could not find suitable repository for " + uri);
        }
    }

    private Info getSparseInfo(final ReadOnlyRepositoryInternal repository) {
        final View view = repository.createView();
        final QualifiedResource qualifiedResource = repository.getQualifiedResource(Resource.ROOT);
        final Revision revision = view.getHeadRevision();
        final QualifiedResource resolvedQualifiedResource = repository.resolve(view, qualifiedResource, revision, false);

        final Operation<Info> infoOperation = new SparseInfoOperationHttpv1(repository, resolvedQualifiedResource, ResourcePropertyUtils.REPOSITORY_ID);
        return infoOperation.execute();
    }

}
