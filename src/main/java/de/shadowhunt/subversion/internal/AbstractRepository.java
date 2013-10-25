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

import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Log;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.ResourceProperty.Type;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.internal.util.URIUtils;

/**
 * Base for all {@link de.shadowhunt.subversion.Repository}
 */
public abstract class AbstractRepository implements Repository {

	public final HttpClient client;

	protected final RepositoryConfig config;

	protected final HttpContext context;

	protected final URI repository;

	protected AbstractRepository(final URI repository, final RepositoryConfig config, final HttpClient client, final HttpContext context) {
		this.repository = URIUtils.createURI(repository);
		this.config = config;
		this.client = client;
		this.context = context;
	}

	@Override
	public void copy(final Transaction transaction, final Resource srcResource, final Revision srcRevision, final Resource targetResource) {
		createFolder0(config.getWorkingResource(transaction).append(targetResource.getParent()), true);

		final Info info = info(srcResource, srcRevision);
		final Resource s = config.getVersionedResource(info.getRevision()).append(info.getResource());
		final Resource t = config.getWorkingResource(transaction).append(targetResource);

		final CopyOperation operation = new CopyOperation(repository, s, t);
		operation.execute(client, context);
	}

	@Override
	public void createFolder(final Transaction transaction, final Resource resource, final boolean parent) {
		if (exists(resource, Revision.HEAD)) {
			return;
		}

		createFolder0(config.getWorkingResource(transaction).append(resource), parent);
	}

	protected Resource createFolder0(final Resource resource, final boolean parent) {
		Resource result = null;
		if (parent) {
			if (Resource.ROOT.equals(resource)) {
				return null;
			}

			result = createFolder0(resource.getParent(), parent);
		}

		final CreateFolderOperation operation = new CreateFolderOperation(repository, resource);
		final boolean created = operation.execute(client, context);
		if (!created) {
			result = resource;
		}
		return result;
	}

	@Override
	public void delete(final Transaction transaction, final Resource resource) {
		final DeleteOperation operation = new DeleteOperation(repository, config.getWorkingResource(transaction).append(resource));
		operation.execute(client, context);
	}

	@Override
	public void deleteProperties(final Transaction transaction, final Resource resource, final ResourceProperty... properties) {
		final ResourceProperty[] withoutBASE = ResourceProperty.filterOutByType(Type.BASE, properties);
		final ResourceProperty[] withoutDAV = ResourceProperty.filterOutByType(Type.DAV, withoutBASE);

		if (withoutDAV.length == 0) {
			return;
		}

		final Info info = info(resource, Revision.HEAD);

		final Resource r = config.getWorkingResource(transaction).append(resource);
		final PropertiesDeleteOperation operation = new PropertiesDeleteOperation(repository, r, info.getLockToken(), withoutDAV);
		operation.execute(client, context);
	}

	@Override
	public InputStream download(final Resource resource, final Revision revision) {
		final DownloadOperation operation = new DownloadOperation(repository, resolve(resource, revision, true));
		return operation.execute(client, context);
	}

	@Override
	public URI downloadURI(final Resource resource, final Revision revision) {
		return URIUtils.createURI(repository, resolve(resource, revision, true));
	}

	@Override
	public boolean exists(final Resource resource, final Revision revision) {
		final ExistsOperation operation = new ExistsOperation(repository, resolve(resource, revision, false));
		return operation.execute(client, context);
	}

	@Override
	public final URI getBaseUri() {
		return URIUtils.createURI(repository);
	}

	protected Revision getConcreteRevision(final Revision revision) {
		if (Revision.HEAD.equals(revision)) {
			final Info info = info(Resource.ROOT, Revision.HEAD);
			return info.getRevision();
		}
		return revision;
	}

	@Override
	public Info info(final Resource resource, final Revision revision) {
		final InfoOperation operation = new InfoOperation(repository, resolve(resource, revision, true), Depth.EMPTY);
		return operation.execute(client, context);
	}

	protected List<Info> list(final Resource prefix, final Resource resource, final Depth depth) {
		final Resource r = prefix.append(resource);

		final ListOperation operation = new ListOperation(repository, r, depth);
		return operation.execute(client, context);
	}

	@Override
	public List<Info> list(final Resource resource, final Revision revision, final Depth depth) {
		final Revision concreteRevision = getConcreteRevision(revision);
		final Resource prefix = config.getVersionedResource(concreteRevision);
		return list(prefix, resource, depth);
	}

	protected void listRecursive(final Resource prefix, final Collection<Info> todo, final Set<Info> done) {
		for (final Info info : todo) {
			if (done.contains(info)) {
				continue;
			}

			done.add(info);
			if (info.isDirectory()) {
				final Resource resource = prefix.append(info.getResource());
				final List<Info> children = list(prefix, resource, Depth.IMMEDIATES);
				listRecursive(prefix, children, done);
			}
		}
	}

	@Override
	public void lock(final Resource resource, final boolean steal) {
		final LockOperation operation = new LockOperation(repository, resource, steal);
		operation.execute(client, context);
	}

	@Override
	public List<Log> log(final Resource resource, final Revision startRevision, final Revision endRevision, final int limit) {
		final Revision concreteStartRevision = getConcreteRevision(startRevision);
		final Revision concreteEndRevision = getConcreteRevision(endRevision);

		final Resource resolvedResource;
		if (concreteStartRevision.compareTo(concreteEndRevision) > 0) {
			resolvedResource = resolve(resource, concreteStartRevision, true);
		} else {
			resolvedResource = resolve(resource, concreteEndRevision, true);
		}
		final LogOperation operation = new LogOperation(repository, resolvedResource, concreteStartRevision, concreteEndRevision, limit);
		return operation.execute(client, context);
	}

	@Override
	public void move(final Transaction transaction, final Resource srcResource, final Resource targetResource) {
		copy(transaction, srcResource, Revision.HEAD, targetResource);
		delete(transaction, srcResource);
	}

	protected Resource resolve(final Resource resource, final Revision revision, final boolean resolve) {
		if (Revision.HEAD.equals(revision)) {
			return resource;
		}

		final Resource expectedResource = config.getVersionedResource(revision).append(resource);
		if (!resolve) {
			return expectedResource;
		}

		{ // check whether the expectedResource exists
			final ExistsOperation operation = new ExistsOperation(repository, expectedResource);
			if (operation.execute(client, context)) {
				return expectedResource;
			}
		}

		final Info headInfo = info(resource, Revision.HEAD);
		final ResolveOperation operation = new ResolveOperation(repository, resource, headInfo.getRevision(), revision, config);
		return operation.execute(client, context);
	}

	@Override
	public void rollback(final Transaction transaction) {
		final Resource resource = config.getTransactionResource(transaction);
		final DeleteOperation operation = new DeleteOperation(repository, resource);
		operation.execute(client, context);
	}

	@Override
	public void setProperties(final Transaction transaction, final Resource resource, final ResourceProperty... properties) {
		final ResourceProperty[] withoutBASE = ResourceProperty.filterOutByType(Type.BASE, properties);
		final ResourceProperty[] withoutDAV = ResourceProperty.filterOutByType(Type.DAV, withoutBASE);

		if (withoutDAV.length == 0) {
			return;
		}

		final Info info = info(resource, Revision.HEAD);
		final Resource r = config.getWorkingResource(transaction).append(resource);
		final PropertiesSetOperation operation = new PropertiesSetOperation(repository, r, info.getLockToken(), withoutDAV);
		operation.execute(client, context);
	}

	@Override
	public void unlock(final Resource resource, final boolean force) {
		final Info info = info(resource, Revision.HEAD);
		final String lockToken = info.getLockToken();
		if (lockToken == null) {
			return;
		}
		final UnlockOperation operation = new UnlockOperation(repository, resource, lockToken, force);
		operation.execute(client, context);
	}

	@Override
	public void upload(final Transaction transaction, final Resource resource, final InputStream content) {
		if (content == null) {
			throw new IllegalArgumentException("content can not be null");
		}

		final Resource infoResource;
		if (exists(resource, Revision.HEAD)) {
			infoResource = resource;
		} else {
			infoResource = createFolder0(config.getWorkingResource(transaction).append(resource.getParent()), true);
		}
		final Info info = info(infoResource, Revision.HEAD);
		final Resource r = config.getWorkingResource(transaction).append(resource);
		final UploadOperation operation = new UploadOperation(repository, r, info.getLockToken(), content);
		operation.execute(client, context);
	}
}
