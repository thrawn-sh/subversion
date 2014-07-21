/**
 * Copyright (C) 2013 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

import javax.annotation.CheckForNull;

import org.apache.commons.lang3.Validate;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * Base for all {@link de.shadowhunt.subversion.Repository}
 */
public abstract class AbstractBaseRepository implements Repository {

    private static final Logger LOGGER = LoggerFactory.getLogger("de.shadowhunt.subversion.Repository");

    private static UUID determineRepositoryId(final URI repository, final VersionParser parser, final HttpClient client, final HttpContext context) {
        final InfoOperation operation = new InfoOperation(repository, Resource.ROOT, parser);
        final Info info = operation.execute(client, context);
        return info.getRepositoryId();
    }

    protected static RepositoryCache fromTransaction(final Transaction transaction) {
        if (transaction instanceof RepositoryCache) {
            return (RepositoryCache) transaction;
        }
        throw new IllegalArgumentException("Can not get repository cache for " + transaction);
    }

    protected static interface ResourceMapper {

        Resource getCommitMessageResource(Transaction transaction);

        Resource getCreateTransactionResource();

        Resource getRegisterResource(Resource resource, Revision revision);

        Resource getRegisterTransactionResource(Transaction transaction);

        Resource getTransactionResource(Transaction transaction);

        Resource getVersionedResource(Resource resource, Revision revision);

        Resource getWorkingResource(Transaction transaction);
    }

    protected final HttpClient client;

    protected final ResourceMapper config;

    protected final HttpContext context;

    private final VersionParser parser;

    protected final URI repository;

    private final UUID repositoryId;

    protected AbstractBaseRepository(final URI repository, final ResourceMapper config, final HttpClient client, final HttpContext context) {
        Validate.notNull(repository, "repository must not be null");
        Validate.notNull(config, "config must not be null");
        Validate.notNull(client, "client must not be null");
        Validate.notNull(context, "context must not be null");

        this.repository = URIUtils.createURI(repository);
        this.config = config;
        this.client = client;
        this.context = context;

        parser = new VersionParser(repository.getPath());
        repositoryId = determineRepositoryId(repository, parser, client, context);
    }

    @Override
    public void add(final Transaction transaction, final Resource resource, final boolean parents, final InputStream content) {
        validateTransaction(transaction);
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(content, "content must not be null");

        LOGGER.trace("adding resource {} during transaction {} (parents: {})", resource, transaction.getId(), parents);
        if (parents) {
            mkdir(transaction, resource.getParent(), parents);
        }

        final RepositoryCache cache = fromTransaction(transaction);
        final Info info = info0(cache, resource, Revision.HEAD, true);
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
        Validate.notNull(sourceResource, "sourceResource must not be null");
        Validate.notNull(sourceRevision, "sourceRevision must not be null");
        Validate.notNull(targetResource, "targetResource must not be null");

        LOGGER.trace("coping resource from {}@{} to {} during transaction {} (parents: {})", sourceResource, sourceRevision, targetResource, transaction.getId(), parents);
        if (parents) {
            createFolder(transaction, targetResource.getParent(), Revision.HEAD, parents);
        } else {
            registerResource(transaction, targetResource.getParent(), Revision.HEAD);
        }

        final RepositoryCache cache = fromTransaction(transaction);
        final Info sourceInfo = info0(cache, sourceResource, sourceRevision, true);
        if (sourceInfo == null) {
            throw new SubversionException("Can't resolve: " + sourceResource + '@' + sourceRevision);
        }

        final Info targetInfo = info0(cache, targetResource, Revision.HEAD, true);
        final Resource source = config.getVersionedResource(sourceInfo.getResource(), sourceInfo.getRevision());
        final Resource target = config.getWorkingResource(transaction).append(targetResource);

        final CopyOperation operation = new CopyOperation(repository, source, target, targetInfo);
        operation.execute(client, context);

        if (targetInfo == null) {
            transaction.register(targetResource, Status.ADDED);
        } else {
            transaction.register(targetResource, Status.MODIFIED);
        }
    }

    private void createFolder(final Transaction transaction, final Resource resource, final Revision revision, final boolean parents) {
        final RepositoryCache cache = fromTransaction(transaction);
        final Info info = info0(cache, resource, revision, true); // null if resource does not exists

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
                throw new SubversionException("Can not create folder. File with same name already exists: " + resource);
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
        Validate.notNull(resource, "resource must not be null");

        LOGGER.trace("deleting resource {} during transaction {}", resource, transaction.getId());
        final RepositoryCache cache = fromTransaction(transaction);
        final Info info = info0(cache, resource, Revision.HEAD, true);

        final DeleteOperation operation = new DeleteOperation(repository, config.getWorkingResource(transaction).append(resource), info);
        operation.execute(client, context);
        transaction.register(resource, Status.DELETED);
    }

    @Override
    public final InputStream download(final Resource resource, final Revision revision) {
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(revision, "revision must not be null");

        LOGGER.trace("downloading resource {}@{}", resource, revision);
        return download0(new RepositoryCache(this), resource, revision);
    }

    private InputStream download0(final RepositoryCache cache, final Resource resource, final Revision revision) {
        final Resource resolved = resolve(cache, resource, revision, true);
        if (resolved == null) {
            throw new SubversionException("Can't resolve: " + resource + '@' + revision);
        }
        final DownloadOperation operation = new DownloadOperation(repository, resolved);
        return operation.execute(client, context);
    }

    @Override
    public final URI downloadURI(final Resource resource, final Revision revision) {
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(revision, "revision must not be null");

        LOGGER.trace("creating download uri for resource {}@{}", resource, revision);
        return downloadURI0(new RepositoryCache(this), resource, revision);
    }

    private URI downloadURI0(final RepositoryCache cache, final Resource resource, final Revision revision) {
        final Resource resolved = resolve(cache, resource, revision, true);
        if (resolved == null) {
            throw new SubversionException("Can't resolve: " + resource + '@' + revision);
        }
        return URIUtils.createURI(repository, resolved);
    }

    @Override
    public final boolean exists(final Resource resource, final Revision revision) {
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(revision, "revision must not be null");

        LOGGER.trace("checking existence for resource {}@{}", resource, revision);
        return exists0(new RepositoryCache(this), resource, revision);
    }

    private boolean exists0(final RepositoryCache cache, final Resource resource, final Revision revision) {
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
        final Resource resolved = resolve(cache, resource, revision, false);
        if (resolved == null) {
            throw new SubversionException("Can't resolve: " + resource + '@' + revision);
        }
        final ExistsOperation operation = new ExistsOperation(repository, resolved);
        return operation.execute(client, context);
    }

    @Override
    public final URI getBaseUri() {
        return URIUtils.createURI(repository);
    }

    protected Set<Info> getInfosWithLockTokens(final Transaction transaction) {
        validateTransaction(transaction);

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
            final Info info = info0(repositoryCache, resource, Revision.HEAD, false);
            if ((info != null) && info.isLocked()) {
                infos.add(info);
            }
        }
        return infos;
    }

    @Override
    public final UUID getRepositoryId() {
        return repositoryId;
    }

    protected VersionParser getVersionParser() {
        return parser;
    }

    @Override
    public final Info info(final Resource resource, final Revision revision) {
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(revision, "revision must not be null");

        LOGGER.trace("retrieving info for resource {}@{}", resource, revision);
        final Info info = info0(new RepositoryCache(this), resource, revision, true);
        if (info == null) {
            throw new SubversionException("Can't resolve: " + resource + '@' + revision);
        }
        return info;
    }

    @CheckForNull
    private Info info0(final RepositoryCache cache, final Resource resource, final Revision revision, final boolean resolve) {
        Info info = cache.get(resource, revision);
        if (info != null) {
            return info;
        }

        final Resource resolved = resolve(cache, resource, revision, resolve);
        if (resolved == null) {
            return null; // resource does not exists
        }

        final InfoOperation operation = new InfoOperation(repository, resolved, parser);
        info = operation.execute(client, context);
        cache.put(info);
        return info;
    }

    @Override
    public final Set<Info> list(final Resource resource, final Revision revision, final Depth depth) {
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(revision, "revision must not be null");
        Validate.notNull(depth, "depth must not be null");

        LOGGER.trace("listing info for resource {}@{} and depth ", resource, revision, depth);
        return list0(new RepositoryCache(this), resource, revision, depth);
    }

    private Set<Info> list0(final RepositoryCache cache, final Resource resource, final Revision revision, final Depth depth) {
        if (Depth.INFINITY == depth) {
            final Set<Info> result = new TreeSet<Info>(Info.RESOURCE_COMPARATOR);
            listRecursively0(cache, resource, revision, result);
            return result;
        }

        final Resource resolved = resolve(cache, resource, revision, true);
        if (resolved == null) {
            throw new SubversionException("Can't resolve: " + resource + '@' + revision);
        }

        final ListOperation operation = new ListOperation(repository, resolved, depth, parser);
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
        Validate.notNull(resource, "resource must not be null");

        LOGGER.trace("locking resource {} (steal: {})", resource, steal);
        final LockOperation operation = new LockOperation(repository, resource, steal);
        operation.execute(client, context);
    }

    @Override
    public final List<Log> log(final Resource resource, final Revision startRevision, final Revision endRevision, final int limit) {
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(startRevision, "endRevision must not be null");

        LOGGER.trace("retrieving log for resource {} from {} to {} (limit: {})", resource, startRevision, endRevision, limit);
        return log0(new RepositoryCache(this), resource, startRevision, endRevision, limit);
    }

    private List<Log> log0(final RepositoryCache cache, final Resource resource, final Revision startRevision, final Revision endRevision, final int limit) {
        final Revision concreteStartRevision = cache.getConcreteRevision(startRevision);
        final Revision concreteEndRevision = cache.getConcreteRevision(endRevision);

        final Revision resoledRevision = (concreteStartRevision.compareTo(concreteEndRevision) > 0) ? concreteStartRevision : concreteEndRevision;
        final Resource resolved = resolve(cache, resource, resoledRevision, true);
        if (resolved == null) {
            throw new SubversionException("Can't resolve: " + resource + '@' + resoledRevision);
        }
        final LogOperation operation = new LogOperation(repository, resolved, concreteStartRevision, concreteEndRevision, limit);
        return operation.execute(client, context);
    }

    @Override
    public void mkdir(final Transaction transaction, final Resource resource, final boolean parent) {
        validateTransaction(transaction);
        Validate.notNull(resource, "resource must not be null");

        LOGGER.trace("creating folder for resource {} during {} (parent: {})", resource, transaction.getId(), parent);
        final RepositoryCache cache = fromTransaction(transaction);
        final Revision revision = cache.getConcreteRevision(Revision.HEAD);
        createFolder(transaction, resource, revision, parent);
    }

    @Override
    public void move(final Transaction transaction, final Resource srcResource, final Resource targetResource, final boolean parents) {
        validateTransaction(transaction);
        Validate.notNull(srcResource, "srcResource must not be null");
        Validate.notNull(targetResource, "targetResource must not be null");

        LOGGER.trace("moving {} to {} during {} (parents: {})", srcResource, targetResource, transaction.getId(), parents);
        copy(transaction, srcResource, Revision.HEAD, targetResource, parents);
        delete(transaction, srcResource);
    }

    @Override
    public void propertiesDelete(final Transaction transaction, final Resource resource, final ResourceProperty... properties) {
        validateTransaction(transaction);
        Validate.notNull(resource, "resource must not be null");
        Validate.noNullElements(properties, "properties must not contain null elements");

        LOGGER.trace("deleting properties {} on {} during {}", properties, resource, transaction.getId());

        // there can only be a lock token if the file is already in the repository
        final RepositoryCache cache = fromTransaction(transaction);
        final Info info = info0(cache, resource, Revision.HEAD, true);

        final Resource r = config.getWorkingResource(transaction).append(resource);
        final PropertiesDeleteOperation operation = new PropertiesDeleteOperation(repository, r, info, properties);
        operation.execute(client, context);
        transaction.register(resource, Status.MODIFIED);
    }

    @Override
    public void propertiesSet(final Transaction transaction, final Resource resource, final ResourceProperty... properties) {
        validateTransaction(transaction);
        Validate.notNull(resource, "resource must not be null");
        Validate.noNullElements(properties, "properties must not contain null elements");

        LOGGER.trace("setting properties {} on {} during {}", properties, resource, transaction.getId());

        // there can only be a lock token if the file is already in the repository
        final RepositoryCache cache = fromTransaction(transaction);
        final Info info = info0(cache, resource, Revision.HEAD, true);

        final Resource r = config.getWorkingResource(transaction).append(resource);
        final PropertiesSetOperation operation = new PropertiesSetOperation(repository, r, info, properties);
        operation.execute(client, context);
        transaction.register(resource, Status.MODIFIED);
    }

    protected abstract void registerResource(Transaction transaction, Resource resource, Revision revision);

    @CheckForNull
    Resource resolve(final RepositoryCache cache, final Resource resource, final Revision revision, final boolean resolve) {
        Validate.notNull(cache, "cache must not be null");
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(revision, "revision must not be null");

        if (Revision.HEAD.equals(revision)) {
            final ExistsOperation operation = new ExistsOperation(repository, resource);
            if (!resolve || (operation.execute(client, context))) {
                return resource;
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
        final ResolveOperation operation = new ResolveOperation(repository, resource, head, revision, config);
        return operation.execute(client, context);
    }

    @Override
    public void rollback(final Transaction transaction) {
        validateTransaction(transaction);

        LOGGER.trace("rolling transaction {} back", transaction.getId());
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
        Validate.notNull(resource, "resource must not be null");

        LOGGER.trace("unlocking {} (force: {})", resource, force);
        unlock0(new RepositoryCache(this), resource, force);
    }

    private void unlock0(final RepositoryCache cache, final Resource resource, final boolean force) {
        final Info info = info0(cache, resource, Revision.HEAD, true);
        if (info == null) {
            throw new SubversionException("Can't resolve: " + resource + '@' + Revision.HEAD);
        }

        final String lockToken = info.getLockToken();
        if (lockToken == null) {
            return;
        }
        final UnlockOperation operation = new UnlockOperation(repository, resource, lockToken, force);
        operation.execute(client, context);
    }

    protected void validateTransaction(final Transaction transaction) {
        Validate.notNull(transaction, "transaction must not be null");

        final UUID transactionRepositoryId = transaction.getRepositoryId();
        if (!repositoryId.equals(transactionRepositoryId)) {
            throw new SubversionException("Transaction invalid: does not belong to this repository");
        }
        if (!transaction.isActive()) {
            throw new SubversionException("Transaction invalid: has already been committed or reverted");
        }
    }
}
