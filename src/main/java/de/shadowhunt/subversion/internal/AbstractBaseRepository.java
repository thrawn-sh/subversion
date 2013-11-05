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
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
public abstract class AbstractBaseRepository implements Repository {

	private static UUID determineRepositoryId(final URI repository, final HttpClient client, final HttpContext context) {
		final InfoOperation operation = new InfoOperation(repository, Resource.ROOT);
		final Info info = operation.execute(client, context);
		return info.getRepositoryId();
	}

	protected static RepositoryCache fromTransaction(final Transaction transaction) {
		if (transaction instanceof RepositoryCache) {
			return (RepositoryCache) transaction;
		}
		throw new IllegalArgumentException("can't convert " + transaction + " to a repository cache");
	}

	protected final HttpClient client;

	protected final RepositoryConfig config;

	protected final HttpContext context;

	protected final URI repository;

	private final UUID repositoryId;

	protected AbstractBaseRepository(final URI repository, final RepositoryConfig config, final HttpClient client, final HttpContext context) {
		this.repository = URIUtils.createURI(repository);
		this.config = config;
		this.client = client;
		this.context = context;

		repositoryId = determineRepositoryId(repository, client, context);
	}

	@Override
	public void add(final Transaction transaction, final Resource resource, final boolean parents, final InputStream content) {
		validateTransaction(transaction);

		if (content == null) {
			throw new IllegalArgumentException("content can not be null");
		}

		if (parents) {
			mkdir(transaction, resource.getParent(), parents);
		}

		final RepositoryCache cache = fromTransaction(transaction);
		final Info info = info0(cache, resource, Revision.HEAD, true, false);
		final Resource uploadResource = config.getWorkingResource(transaction).append(resource);
		final UploadOperation operation = new UploadOperation(repository, uploadResource, info, content);
		operation.execute(client, context);
		if (info == null) {
			transaction.register(resource, Status.ADDED);
		} else {
			// file existed already
			transaction.register(resource, Status.MODIFIED);
		}
	}

	@Override
	public void copy(final Transaction transaction, final Resource sourceResource, final Revision sourceRevision, final Resource targetResource, final boolean parents) {
		validateTransaction(transaction);

		if (parents) {
			createFolder(transaction, targetResource.getParent(), Revision.HEAD, parents);
		} else {
			registerResource(transaction, targetResource.getParent(), Revision.HEAD);
		}

		final RepositoryCache cache = fromTransaction(transaction);
		final Info soruceInfo = info0(cache, sourceResource, sourceRevision, true, true);
		final Info targetInfo = info0(cache, targetResource, Revision.HEAD, true, false);
		final Resource source = config.getVersionedResource(soruceInfo.getResource(), soruceInfo.getRevision());
		final Resource target = config.getWorkingResource(transaction).append(targetResource);

		final CopyOperation operation = new CopyOperation(repository, source, target, targetInfo);
		operation.execute(client, context);

		if (targetInfo == null) {
			transaction.register(targetResource, Status.ADDED);
		} else {
			transaction.register(targetResource, Status.MODIFIED);
		}
	}

	void createFolder(final Transaction transaction, final Resource resource, final Revision revision, final boolean parents) {
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
				throw new SubversionException("file with same name as directory already exists: " + resource);
			}
			final Status status = transaction.getChangeSet().get(resource);
			if (status == null) {
				registerResource(transaction, resource, revision);
			}
		}

		Resource current = resource.getParent();
		while (!Resource.ROOT.equals(current)) {
			if (!transaction.register(current, Status.EXISTS)) {
				break;
			}
			current = current.getParent();
		}
	}

	@Override
	public void delete(final Transaction transaction, final Resource resource) {
		validateTransaction(transaction);

		final RepositoryCache cache = fromTransaction(transaction);
		final Info info = info0(cache, resource, Revision.HEAD, true, false);

		final DeleteOperation operation = new DeleteOperation(repository, config.getWorkingResource(transaction).append(resource), info);
		operation.execute(client, context);
		transaction.register(resource, Status.DELETED);
	}

	@Override
	public final InputStream download(final Resource resource, final Revision revision) {
		return download0(new RepositoryCache(this), resource, revision);
	}

	InputStream download0(final RepositoryCache cache, final Resource resource, final Revision revision) {
		final Resource resolved = resolve(cache, resource, revision, true, true);
		final DownloadOperation operation = new DownloadOperation(repository, resolved);
		return operation.execute(client, context);
	}

	@Override
	public final URI downloadURI(final Resource resource, final Revision revision) {
		return downloadURI0(new RepositoryCache(this), resource, revision);
	}

	URI downloadURI0(final RepositoryCache cache, final Resource resource, final Revision revision) {
		final Resource resolved = resolve(cache, resource, revision, true, true);
		return URIUtils.createURI(repository, resolved);
	}

	@Override
	public final boolean exists(final Resource resource, final Revision revision) {
		return exists0(new RepositoryCache(this), resource, revision);
	}

	boolean exists0(final RepositoryCache cache, final Resource resource, final Revision revision) {
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

	@Override
	public final URI getBaseUri() {
		return URIUtils.createURI(repository);
	}

	protected Set<Info> getInfosWithLockTokens(final Transaction transaction) {
		final Map<Resource, Status> changeSet = transaction.getChangeSet();
		if (changeSet.isEmpty()) {
			return Collections.emptySet();
		}

		final RepositoryCache repositoryCache = fromTransaction(transaction);
		final Set<Info> infos = new TreeSet<Info>(Info.RESOURCE_COMPARATOR);
		for (final Map.Entry<Resource, Status> entry : changeSet.entrySet()) {
			final Status status = entry.getValue();
			if ((Status.EXISTS == status) || (Status.ADDED == status)) {
				continue;
			}

			final Resource resource = entry.getKey();
			final Info info = info0(repositoryCache, resource, Revision.HEAD, false, false);
			if (info.isLocked()) {
				infos.add(info);
			}
		}
		return infos;
	}

	@Override
	public final UUID getRepositoryId() {
		return repositoryId;
	}

	@Override
	public final Info info(final Resource resource, final Revision revision) {
		return info0(new RepositoryCache(this), resource, revision, true, true);
	}

	Info info0(final RepositoryCache cache, final Resource resource, final Revision revision, final boolean resolve, final boolean report) {
		Info info = cache.get(resource, revision);
		if (info != null) {
			return info;
		}

		final Resource resolved = resolve(cache, resource, revision, resolve, report);
		if (resolved == null) {
			return null; // resource does not exists
		}

		final InfoOperation operation = new InfoOperation(repository, resolved);
		info = operation.execute(client, context);
		cache.put(info);
		return info;
	}

	@Override
	public final Set<Info> list(final Resource resource, final Revision revision, final Depth depth) {
		return list0(new RepositoryCache(this), resource, revision, depth);
	}

	Set<Info> list0(final RepositoryCache cache, final Resource resource, final Revision revision, final Depth depth) {
		if (Depth.INFINITY == depth) {
			final Set<Info> result = new TreeSet<Info>(Info.RESOURCE_COMPARATOR);
			listRecursively0(cache, resource, revision, result);
			return result;
		}
		final Resource resolved = resolve(cache, resource, revision, true, true);
		final ListOperation operation = new ListOperation(repository, resolved, depth);
		final Set<Info> infoSet = operation.execute(client, context);
		cache.putAll(infoSet);
		return infoSet;
	}

	private void listRecursively0(final RepositoryCache cache, final Resource resource, final Revision revision, final Set<Info> result) {
		for (final Info info : list0(cache, resource, revision, Depth.IMMEDIATES)) {
			if (!result.add(info)) {
				continue;
			}

			if (info.isDirectory()) {
				listRecursively0(cache, info.getResource(), revision, result);
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

	List<Log> log0(final RepositoryCache cache, final Resource resource, final Revision startRevision, final Revision endRevision, final int limit) {
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

		// there can only be a lock token if the file is already in the repository
		final RepositoryCache cache = fromTransaction(transaction);
		final Info info = info0(cache, resource, Revision.HEAD, true, false);

		final Resource r = config.getWorkingResource(transaction).append(resource);
		final PropertiesDeleteOperation operation = new PropertiesDeleteOperation(repository, r, info, properties);
		operation.execute(client, context);
		transaction.register(resource, Status.MODIFIED);
	}

	@Override
	public void propertiesSet(final Transaction transaction, final Resource resource, final ResourceProperty... properties) {
		validateTransaction(transaction);

		// there can only be a lock token if the file is already in the repository
		final RepositoryCache cache = fromTransaction(transaction);
		final Info info = info0(cache, resource, Revision.HEAD, true, false);

		final Resource r = config.getWorkingResource(transaction).append(resource);
		final PropertiesSetOperation operation = new PropertiesSetOperation(repository, r, info, properties);
		operation.execute(client, context);
		transaction.register(resource, Status.MODIFIED);
	}

	protected abstract void registerResource(Transaction transaction, Resource resource, Revision revision);

	Resource resolve(final RepositoryCache cache, final Resource resource, final Revision revision, final boolean resolve, final boolean report) {
		if (Revision.HEAD.equals(revision)) {
			final ExistsOperation operation = new ExistsOperation(repository, resource);
			if (!resolve || (operation.execute(client, context))) {
				return resource;
			}
			if (report) {
				throw new SubversionException("Can't resolve: " + resource + "@" + Revision.HEAD);
			}
			return null;
		}

		final Resource expectedResource = config.getVersionedResource(resource, revision);
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
			final DeleteOperation operation = new DeleteOperation(repository, resource, null);
			operation.execute(client, context);
		} finally {
			transaction.invalidate();
		}
	}

	@Override
	public final void unlock(final Resource resource, final boolean force) {
		unlock0(new RepositoryCache(this), resource, force);
	}

	void unlock0(final RepositoryCache cache, final Resource resource, final boolean force) {
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
			throw new SubversionException("Transaction invalid: does not belong to this repository");
		}
		if (!transaction.isActive()) {
			throw new SubversionException("Transaction invalid: has already been commited or rolledback");
		}
	}
}
