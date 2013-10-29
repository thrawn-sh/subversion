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
import de.shadowhunt.subversion.Transaction.Status;
import de.shadowhunt.subversion.internal.util.URIUtils;

/**
 * Base for all {@link de.shadowhunt.subversion.Repository}
 */
public abstract class AbstractBasicRepository implements Repository {

	private static UUID determineRepositoryId(final URI repository, final HttpClient client, final HttpContext context) {
		final InfoOperation operation = new InfoOperation(repository, Resource.ROOT, Depth.EMPTY);
		final Info info = operation.execute(client, context);
		return info.getRepositoryId();
	}

	public final HttpClient client;

	protected final RepositoryConfig config;

	protected final HttpContext context;

	protected final URI repository;

	protected final UUID repositoryId;

	protected AbstractBasicRepository(final URI repository, final RepositoryConfig config, final HttpClient client, final HttpContext context) {
		this.repository = URIUtils.createURI(repository);
		this.config = config;
		this.client = client;
		this.context = context;

		repositoryId = determineRepositoryId(repository, client, context);
	}

	@Override
	public void add(final Transaction transaction, final Resource resource, final boolean parents, final InputStream content) {
		if (content == null) {
			throw new IllegalArgumentException("content can not be null");
		}

		if (parents) {
			mkdir(transaction, resource.getParent(), parents);
		}

		//		final Resource infoResource;
		//		if (exists(resource, Revision.HEAD)) {
		//			infoResource = resource;
		//		} else {
		//			infoResource = createFolder0(config.getWorkingResource(transaction).append(resource.getParent()), true);
		//		}
		//		final Info info = info(resource, Revision.HEAD);
		final Resource uploadResource = config.getWorkingResource(transaction).append(resource);
		final UploadOperation operation = new UploadOperation(repository, uploadResource, null, content); // FIXME locktoken
		operation.execute(client, context);
		transaction.register(resource, Status.ADDED);
	}

	@Override
	public void copy(final Transaction transaction, final Resource srcResource, final Revision srcRevision, final Resource targetResource, final boolean parents) {
		validateTransaction(transaction);

		createFolder(transaction, targetResource.getParent(), srcRevision, parents);

		final RepositoryCache cache = fromTransaction(transaction);
		final Info info = info0(cache, srcResource, srcRevision, true, true);
		final Resource s = config.getVersionedResource(info.getRevision()).append(info.getResource());
		final Resource t = config.getWorkingResource(transaction).append(targetResource);

		final CopyOperation operation = new CopyOperation(repository, s, t);
		operation.execute(client, context);
		transaction.register(targetResource, Status.ADDED);
	}

	protected void createFolder(final Transaction transaction, final Resource resource, final Revision revision, final boolean parents) {
		final RepositoryCache cache = fromTransaction(transaction);
		final Info info = info0(cache, resource, revision, true, false); // null if resource does not exists

		if (parents && (info == null) && !Resource.ROOT.equals(resource)) {
			createFolder(transaction, resource.getParent(), revision, parents);
		}

		if (info == null) {
			final Resource folder = config.getWorkingResource(transaction).append(resource);
			final CreateFolderOperation operation = new CreateFolderOperation(repository, folder);
			operation.execute(client, context);
			transaction.register(resource, Status.ADDED);
		} else {
			if (info.isFile()) {
				throw new SubversionException("can't create directory, file with same name already exists: " + resource);
			}
			registerResource(transaction, resource, revision);
		}
	}

	@Override
	public void delete(final Transaction transaction, final Resource resource) {
		validateTransaction(transaction);

		final DeleteOperation operation = new DeleteOperation(repository, config.getWorkingResource(transaction).append(resource));
		operation.execute(client, context);
		transaction.register(resource, Status.DELETED);
	}

	@Override
	public final InputStream download(final Resource resource, final Revision revision) {
		return download0(new RepositoryCache(this), resource, revision);
	}

	public InputStream download0(final RepositoryCache cache, final Resource resource, final Revision revision) {
		final Resource resolved = resolve(cache, resource, revision, true, true);
		final DownloadOperation operation = new DownloadOperation(repository, resolved);
		return operation.execute(client, context);
	}

	@Override
	public final URI downloadURI(final Resource resource, final Revision revision) {
		return downloadURI0(new RepositoryCache(this), resource, revision);
	}

	public URI downloadURI0(final RepositoryCache cache, final Resource resource, final Revision revision) {
		final Resource resolved = resolve(cache, resource, revision, true, true);
		return URIUtils.createURI(repository, resolved);
	}

	@Override
	public final boolean exists(final Resource resource, final Revision revision) {
		return exists0(new RepositoryCache(this), resource, revision);
	}

	public boolean exists0(final RepositoryCache cache, final Resource resource, final Revision revision) {
		// check change set for non commit changes
		if (Revision.HEAD.equals(revision)) {
			final Status status = cache.status(resource);
			if ((status == Status.ADDED) || (status == Status.MODIFIED)) {
				return true;
			}
			if (status == Status.DELETED) {
				return false;
			}
		}

		// check cache for entries
		final Info info = cache.get(resource, revision);
		if (info != null) {
			return true;
		}

		// ask the server
		final Resource resolved = resolve(cache, resource, revision, false, true);
		final ExistsOperation operation = new ExistsOperation(repository, resolved);
		return operation.execute(client, context);
	}

	protected RepositoryCache fromTransaction(final Transaction transaction) {
		if (transaction instanceof TransactionImpl) {
			return (TransactionImpl) transaction;
		}

		throw new IllegalArgumentException(); // FIXME
	}

	@Override
	public final URI getBaseUri() {
		return URIUtils.createURI(repository);
	}

	@Override
	public final UUID getRepositoryId() {
		return repositoryId;
	}

	@Override
	public final Info info(final Resource resource, final Revision revision) {
		return info0(new RepositoryCache(this), resource, revision, true, true);
	}

	public Info info0(final RepositoryCache cache, final Resource resource, final Revision revision, final boolean resolve, final boolean report) {
		Info info = cache.get(resource, revision);
		if (info != null) {
			return info;
		}
		final Resource resolved = resolve(cache, resource, revision, resolve, report);
		if (resolved == null) {
			return null; // resource does not exists
		}
		final InfoOperation operation = new InfoOperation(repository, resolved, Depth.EMPTY);
		info = operation.execute(client, context);
		return info;
	}

	@Override
	public final Set<Info> list(final Resource resource, final Revision revision, final Depth depth) {
		return list0(new RepositoryCache(this), resource, revision, depth);
	}

	public Set<Info> list0(final RepositoryCache cache, final Resource resource, final Revision revision, final Depth depth) {
		if (Depth.INFINITY == depth) {
			final Set<Info> result = new TreeSet<Info>(Info.RESOURCE_COMPARATOR);
			listRecursively(cache, resource, revision, result);
			return result;
		}
		final Resource resolved = resolve(cache, resource, revision, true, true);
		final ListOperation operation = new ListOperation(repository, resolved, depth);
		final Set<Info> infoSet = operation.execute(client, context);
		cache.putAll(infoSet);
		return infoSet;
	}

	private void listRecursively(final RepositoryCache cache, final Resource resource, final Revision revision, final Set<Info> result) {
		for (final Info info : list0(cache, resource, revision, Depth.IMMEDIATES)) {
			if (!result.add(info)) {
				continue;
			}

			if (info.isDirectory()) {
				listRecursively(cache, info.getResource(), revision, result);
			}
		}
	}

	@Override
	public final void lock(final Resource resource, final boolean steal) {
		final LockOperation operation = new LockOperation(repository, resource, steal);
		operation.execute(client, context);
	}

	@Override
	public final List<Log> log(final Resource resource, final Revision startRevision, final Revision endRevision, final int limit) {
		return log0(new RepositoryCache(this), resource, startRevision, endRevision, limit);
	}

	public List<Log> log0(final RepositoryCache cache, final Resource resource, final Revision startRevision, final Revision endRevision, final int limit) {
		final Revision concreteStartRevision = cache.getConcreteRevision(startRevision);
		final Revision concreteEndRevision = cache.getConcreteRevision(endRevision);

		final Resource resolved;
		if (concreteStartRevision.compareTo(concreteEndRevision) > 0) {
			resolved = resolve(cache, resource, concreteStartRevision, true, true);
		} else {
			resolved = resolve(cache, resource, concreteEndRevision, true, true);
		}
		final LogOperation operation = new LogOperation(repository, resolved, concreteStartRevision, concreteEndRevision, limit);
		return operation.execute(client, context);
	}

	@Override
	public void mkdir(final Transaction transaction, final Resource resource, final boolean parent) {
		validateTransaction(transaction);

		final RepositoryCache cache = fromTransaction(transaction);
		final Revision revision = cache.getConcreteRevision(Revision.HEAD);
		createFolder(transaction, resource, revision, parent);
	}

	@Override
	public void move(final Transaction transaction, final Resource srcResource, final Resource targetResource, final boolean parents) {
		validateTransaction(transaction);

		copy(transaction, srcResource, Revision.HEAD, targetResource, parents);
		delete(transaction, srcResource);
	}

	@Override
	public void propertiesDelete(final Transaction transaction, final Resource resource, final ResourceProperty... properties) {
		validateTransaction(transaction);

		final Info info = info(resource, Revision.HEAD);

		final Resource r = config.getWorkingResource(transaction).append(resource);
		final PropertiesDeleteOperation operation = new PropertiesDeleteOperation(repository, r, info.getLockToken(), properties);
		operation.execute(client, context);
		transaction.register(resource, Status.MODIFIED);
	}

	@Override
	public void propertiesSet(final Transaction transaction, final Resource resource, final ResourceProperty... properties) {
		validateTransaction(transaction);

		final Info info = info(resource, Revision.HEAD);
		final Resource r = config.getWorkingResource(transaction).append(resource);
		final PropertiesSetOperation operation = new PropertiesSetOperation(repository, r, info.getLockToken(), properties);
		operation.execute(client, context);
		transaction.register(resource, Status.MODIFIED);
	}

	protected abstract void registerResource(Transaction transaction, Resource resource, Revision revision);

	protected Resource resolve(final RepositoryCache cache, final Resource resource, final Revision revision, final boolean resolve, final boolean report) {
		if (Revision.HEAD.equals(revision)) {
			final ExistsOperation operation = new ExistsOperation(repository, resource);
			if (!resolve || (operation.execute(client, context))) {
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

		final Revision head = cache.getConcreteRevision(Revision.HEAD);
		final ResolveOperation operation = new ResolveOperation(repository, resource, head, revision, config, report);
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
	public final void unlock(final Resource resource, final boolean force) {
		unlock0(new RepositoryCache(this), resource, force);
	}

	protected void unlock0(final RepositoryCache cache, final Resource resource, final boolean force) {
		final Info info = info0(cache, resource, Revision.HEAD, true, true);
		final String lockToken = info.getLockToken();
		if (lockToken == null) {
			return;
		}
		final UnlockOperation operation = new UnlockOperation(repository, resource, lockToken, force);
		operation.execute(client, context);
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
}
