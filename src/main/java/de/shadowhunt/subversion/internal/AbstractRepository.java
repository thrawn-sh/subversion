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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Log;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.internal.util.URIUtils;

/**
 * Base for all {@link de.shadowhunt.subversion.Repository}
 */
public abstract class AbstractRepository implements Repository {

	private static UUID determineRepoistoryId(final URI repository, final HttpClient client, final HttpContext context) {
		final InfoOperation operation = new InfoOperation(repository, Resource.ROOT, Depth.EMPTY);
		final Info info = operation.execute(client, context);
		return info.getRepositoryId();
	}

	protected void validateTransaction(final Transaction transaction) {
		final UUID transactionRepositoryId = transaction.getRepositoryId();
		if (!repositoryId.equals(transactionRepositoryId)) {
			throw new SubversionException("TODO"); // FIXME
		}
		if (!transaction.isActive()) {
			throw new SubversionException("TODO"); // FIXME
		}
	}

	public final HttpClient client;

	protected final RepositoryConfig config;

	protected final HttpContext context;

	protected final URI repository;

	protected final UUID repositoryId;

	protected AbstractRepository(final URI repository, final RepositoryConfig config, final HttpClient client, final HttpContext context) {
		this.repository = URIUtils.createURI(repository);
		this.config = config;
		this.client = client;
		this.context = context;

		repositoryId = determineRepoistoryId(repository, client, context);
	}

	@Override
	public void add(final Transaction transaction, final Resource resource, final boolean parents, final InputStream content) {
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

	@Override
	public void copy(final Transaction transaction, final Resource srcResource, final Revision srcRevision, final Resource targetResource, final boolean parents) {
		validateTransaction(transaction);

		createFolder0(config.getWorkingResource(transaction).append(targetResource.getParent()), true);

		final Info info = info(srcResource, srcRevision);
		final Resource s = config.getVersionedResource(info.getRevision()).append(info.getResource());
		final Resource t = config.getWorkingResource(transaction).append(targetResource);

		final CopyOperation operation = new CopyOperation(repository, s, t);
		operation.execute(client, context);
	}

	@Override
	public void createFolder(final Transaction transaction, final Resource resource, final boolean parent) {
		validateTransaction(transaction);

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
		validateTransaction(transaction);

		final DeleteOperation operation = new DeleteOperation(repository, config.getWorkingResource(transaction).append(resource));
		operation.execute(client, context);
	}

	@Override
	public void deleteProperties(final Transaction transaction, final Resource resource, final ResourceProperty... properties) {
		validateTransaction(transaction);

		final Info info = info(resource, Revision.HEAD);

		final Resource r = config.getWorkingResource(transaction).append(resource);
		final PropertiesDeleteOperation operation = new PropertiesDeleteOperation(repository, r, info.getLockToken(), properties);
		operation.execute(client, context);
	}

	@Override
	public InputStream download(final Resource resource, final Revision revision) {
		final Resource resolved = resolve(resource, revision, true);
		final DownloadOperation operation = new DownloadOperation(repository, resolved);
		return operation.execute(client, context);
	}

	@Override
	public URI downloadURI(final Resource resource, final Revision revision) {
		final Resource resolved = resolve(resource, revision, true);
		return URIUtils.createURI(repository, resolved);
	}

	@Override
	public boolean exists(final Resource resource, final Revision revision) {
		final Resource resolved = resolve(resource, revision, false);
		final ExistsOperation operation = new ExistsOperation(repository, resolved);
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
	public final UUID getRepositoryId() {
		return repositoryId;
	}

	@Override
	public Info info(final Resource resource, final Revision revision) {
		final Resource resolved = resolve(resource, revision, true);
		final InfoOperation operation = new InfoOperation(repository, resolved, Depth.EMPTY);
		return operation.execute(client, context);
	}

	@Override
	public Set<Info> list(final Resource resource, final Revision revision, final Depth depth) {
		if (Depth.INFINITY == depth) {
			final Set<Info> result = new TreeSet<Info>(Info.RESOURCE_COMPARATOR);
			listRecursively(resource, revision, result);
			return result;
		}
		final Resource resolved = resolve(resource, revision, true);
		final ListOperation operation = new ListOperation(repository, resolved, depth);
		return operation.execute(client, context);
	}

	private void listRecursively(final Resource resource, final Revision revision, final Set<Info> result) {
		for (final Info info : list(resource, revision, Depth.IMMEDIATES)) {
			if (!result.add(info)) {
				continue;
			}

			if (info.isDirectory()) {
				listRecursively(info.getResource(), revision, result);
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

		final Resource resolved;
		if (concreteStartRevision.compareTo(concreteEndRevision) > 0) {
			resolved = resolve(resource, concreteStartRevision, true);
		} else {
			resolved = resolve(resource, concreteEndRevision, true);
		}
		final LogOperation operation = new LogOperation(repository, resolved, concreteStartRevision, concreteEndRevision, limit);
		return operation.execute(client, context);
	}

	@Override
	public void move(final Transaction transaction, final Resource srcResource, final Resource targetResource, final boolean parents) {
		validateTransaction(transaction);

		copy(transaction, srcResource, Revision.HEAD, targetResource, parents);
		delete(transaction, srcResource);
	}

	protected Resource resolve(final Resource resource, final Revision revision, final boolean resolve) {
		if (Revision.HEAD.equals(revision)) {
			final ExistsOperation operation = new ExistsOperation(repository, resource);
			if (operation.execute(client, context)) {
				return resource;
			}
			throw new SubversionException(resource.getValue());
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
		validateTransaction(transaction);

		try {
			final Resource resource = config.getTransactionResource(transaction);
			final DeleteOperation operation = new DeleteOperation(repository, resource);
			operation.execute(client, context);
		} finally {
			transaction.invalidate();
		}
	}

	@Override
	public void setProperties(final Transaction transaction, final Resource resource, final ResourceProperty... properties) {
		validateTransaction(transaction);

		final Info info = info(resource, Revision.HEAD);
		final Resource r = config.getWorkingResource(transaction).append(resource);
		final PropertiesSetOperation operation = new PropertiesSetOperation(repository, r, info.getLockToken(), properties);
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
}
