/**
 * Copyright (C) 2013-2017 shadowhunt (dev@shadowhunt.de)
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
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.LockToken;
import de.shadowhunt.subversion.Log;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.Transaction.Status;
import de.shadowhunt.subversion.View;
import org.apache.commons.lang3.Validate;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base for all {@link de.shadowhunt.subversion.Repository}.
 */
public abstract class AbstractBaseRepository implements Repository {

    protected interface ResourceMapper {

        QualifiedResource getCommitMessageResource(Transaction transaction);

        QualifiedResource getCreateTransactionResource();

        Resource getPrefix();

        QualifiedResource getRegisterResource(QualifiedResource resource, Revision revision);

        QualifiedResource getRegisterTransactionResource(Transaction transaction);

        QualifiedResource getTransactionResource(Transaction transaction);

        QualifiedResource getVersionedResource(QualifiedResource resource, Revision revision);

        QualifiedResource getWorkingResource(Transaction transaction);
    }

    private static final ResourceProperty.Key[] LOCKING = new ResourceProperty.Key[] { ResourceProperty.RESOURCE, ResourceProperty.LOCK_STATUS };

    private static final Logger LOGGER = LoggerFactory.getLogger("de.shadowhunt.subversion.Repository");

    private static final ResourceProperty.Key[] REVISION = new ResourceProperty.Key[] { ResourceProperty.RESOURCE, ResourceProperty.VERSION };

    private static final ResourceProperty.Key[] TYPE = new ResourceProperty.Key[] { ResourceProperty.RESOURCE, ResourceProperty.RESOURCE_TYPE };

    protected final Resource base;

    protected final HttpClient client;

    protected final ResourceMapper config;

    protected final HttpContext context;

    protected final URI repository;

    protected final UUID repositoryId;

    protected AbstractBaseRepository(final URI repository, final Resource base, final UUID id, final ResourceMapper config, final HttpClient client, final HttpContext context) {
        Validate.notNull(repository, "repository must not be null");
        Validate.notNull(base, "base must not be null");
        Validate.notNull(id, "id must not be null");
        Validate.notNull(config, "config must not be null");
        Validate.notNull(client, "client must not be null");
        Validate.notNull(context, "context must not be null");

        this.repository = repository;
        this.base = base;
        this.repositoryId = id;
        this.config = config;
        this.client = client;
        this.context = context;
    }

    @Override
    public void add(final Transaction transaction, final Resource resource, final boolean parents, final InputStream content) {
        validateTransaction(transaction);
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(content, "content must not be null");

        LOGGER.trace("adding resource {} during transaction {} (parents: {})", resource, transaction.getId(), parents);
        if (parents) {
            mkdir(transaction, resource.getParent(), true);
        }

        final QualifiedResource qualifiedResource = new QualifiedResource(base, resource);
        final Optional<Info> info = info0(transaction, qualifiedResource, transaction.getHeadRevision(), false, LOCKING);
        final Optional<LockToken> lockToken = info.flatMap(Info::getLockToken);

        final QualifiedResource uploadResource = config.getWorkingResource(transaction).append(qualifiedResource);
        final UploadOperation operation = new UploadOperation(repository, uploadResource, lockToken, content);
        operation.execute(client, context);
        if (info.isPresent()) {
            // file existed already
            transaction.register(resource, Status.MODIFIED);
        } else {
            transaction.register(resource, Status.ADDED);
        }
    }

    @Override
    public void copy(final Transaction transaction, final Resource sourceResource, final Revision sourceRevision, final Resource targetResource, final boolean parents) {
        validateTransaction(transaction);
        Validate.notNull(sourceResource, "sourceResource must not be null");
        Validate.notNull(sourceRevision, "sourceRevision must not be null");
        Validate.notNull(targetResource, "targetResource must not be null");
        validateRevision(transaction, sourceRevision);

        LOGGER.trace("copying resource from {}@{} to {} during transaction {} (parents: {})", sourceResource, sourceRevision, targetResource, transaction.getId(), parents);
        copy0(transaction, new QualifiedResource(base, sourceResource), sourceRevision, new QualifiedResource(base, targetResource), parents, true);
    }

    private void copy0(final Transaction transaction, final QualifiedResource sourceResource, final Revision sourceRevision, final QualifiedResource targetResource, final boolean parents, final boolean resolveSource) {
        if (parents) {
            createFolder(transaction, targetResource.getParent(), true);
        } else {
            registerResource(transaction, targetResource.getParent().getResource(), transaction.getHeadRevision());
        }

        final Optional<Info> sourceInfo = info0(transaction, sourceResource, sourceRevision, resolveSource, REVISION);
        final Info sourceInfoValue = sourceInfo.orElseThrow(() -> new SubversionException("Can't resolve: " + sourceResource + '@' + sourceRevision));

        final Optional<Info> targetInfo = info0(transaction, targetResource, transaction.getHeadRevision(), false, LOCKING);
        final Optional<LockToken> lockToken = targetInfo.flatMap(Info::getLockToken);

        final QualifiedResource source = config.getVersionedResource(new QualifiedResource(base, sourceInfoValue.getResource()), sourceInfoValue.getRevision());
        final QualifiedResource target = config.getWorkingResource(transaction).append(targetResource);

        final CopyOperation operation = new CopyOperation(repository, source, target, lockToken);
        operation.execute(client, context);

        if (targetInfo.isPresent()) {
            transaction.register(targetResource.getResource(), Status.MODIFIED);
        } else {
            transaction.register(targetResource.getResource(), Status.ADDED);
        }
    }

    private void createFolder(final Transaction transaction, final QualifiedResource resource, final boolean parents) {
        final Revision headRevision = transaction.getHeadRevision();
        final Optional<Info> info = info0(transaction, resource, headRevision, false, TYPE);

        if (parents && !info.isPresent() && !Resource.ROOT.equals(resource.getResource())) {
            createFolder(transaction, resource.getParent(), true);
        }

        if (info.isPresent()) {
            if (info.get().isFile()) {
                throw new SubversionException("Can not create folder. File with same name already exists: " + resource);
            }
            final Map<Resource, Status> changeSet = transaction.getChangeSet();
            final Status status = changeSet.get(resource.getResource());
            if (status == null) {
                registerResource(transaction, resource.getResource(), headRevision);
            }
        } else {
            final QualifiedResource folder = config.getWorkingResource(transaction).append(resource);
            final CreateFolderOperation operation = new CreateFolderOperation(repository, folder);
            operation.execute(client, context);
            transaction.register(resource.getResource(), Status.ADDED);
        }

        QualifiedResource current = resource.getParent();
        while (!Resource.ROOT.equals(current.getResource())) {
            if (!transaction.register(current.getResource(), Status.EXISTS)) {
                break;
            }
            current = current.getParent();
        }
    }

    @Override
    public View createView() {
        LOGGER.trace("creating new view");
        final Revision headRevision = determineHeadRevision();
        return new ViewImpl(repositoryId, headRevision);
    }

    @Override
    public void delete(final Transaction transaction, final Resource resource) {
        validateTransaction(transaction);
        Validate.notNull(resource, "resource must not be null");

        LOGGER.trace("deleting resource {} during transaction {}", resource, transaction.getId());
        delete0(transaction, new QualifiedResource(base, resource));
    }

    private void delete0(final Transaction transaction, final QualifiedResource resource) {
        final Optional<Info> info = info0(transaction, resource, transaction.getHeadRevision(), false, LOCKING);
        info.orElseThrow(() -> new SubversionException("Can't resolve: " + resource + '@' + Revision.HEAD));

        final QualifiedResource deleteResource = config.getWorkingResource(transaction).append(resource);
        final Optional<LockToken> lockToken = info.flatMap(Info::getLockToken);
        final DeleteOperation operation = new DeleteOperation(repository, deleteResource, lockToken);
        operation.execute(client, context);
        transaction.register(resource.getResource(), Status.DELETED);
    }

    protected Revision determineHeadRevision() {
        final InfoOperation operation = new InfoOperation(repository, base, new QualifiedResource(base, Resource.ROOT), config.getPrefix(), REVISION);
        final Optional<Info> info = operation.execute(client, context);
        return info.orElseThrow(() -> new SubversionException("can not determine HEAD revision")).getRevision();
    }

    @Override
    public InputStream download(final Resource resource, final Revision revision) {
        return download(createView(), resource, revision);
    }

    @Override
    public final InputStream download(final View view, final Resource resource, final Revision revision) {
        Validate.notNull(view, "view must not be null");
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(revision, "revision must not be null");
        validateRevision(view, revision);

        LOGGER.trace("downloading resource {}@{}", resource, revision);
        return download0(view, new QualifiedResource(base, resource), revision);
    }

    private InputStream download0(final View view, final QualifiedResource resource, final Revision revision) {
        final QualifiedResource resolved = resolve(view, resource, revision, false);
        final DownloadOperation operation = new DownloadOperation(repository, resolved);
        final Optional<InputStream> is = operation.execute(client, context);
        return is.orElseThrow(() -> new SubversionException("Can't resolve: " + resource + '@' + revision));
    }

    @Override
    public URI downloadURI(final Resource resource, final Revision revision) {
        return downloadURI(createView(), resource, revision);
    }

    @Override
    public final URI downloadURI(final View view, final Resource resource, final Revision revision) {
        Validate.notNull(view, "view must not be null");
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(revision, "revision must not be null");
        validateRevision(view, revision);

        LOGGER.trace("creating download uri for resource {}@{}", resource, revision);
        return downloadURI0(view, new QualifiedResource(base, resource), revision);
    }

    private URI downloadURI0(final View view, final QualifiedResource resource, final Revision revision) {
        if (!exists0(view, resource, revision)) {
            throw new SubversionException("Can't resolve: " + resource + '@' + revision);
        }
        final QualifiedResource resolved = resolve(view, resource, revision, true);
        return URIUtils.appendResources(repository, resolved);
    }

    @Override
    public boolean exists(final Resource resource, final Revision revision) {
        return exists(createView(), resource, revision);
    }

    @Override
    public final boolean exists(final View view, final Resource resource, final Revision revision) {
        Validate.notNull(view, "view must not be null");
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(revision, "revision must not be null");
        validateRevision(view, revision);

        LOGGER.trace("checking existence for resource {}@{}", resource, revision);
        return exists0(view, new QualifiedResource(base, resource), revision);
    }

    private boolean exists0(final View view, final QualifiedResource resource, final Revision revision) {
        // ask the server
        final QualifiedResource resolved = resolve(view, resource, revision, false);
        final ExistsOperation operation = new ExistsOperation(repository, resolved, config.getPrefix());
        return operation.execute(client, context);
    }

    @Override
    public final Resource getBasePath() {
        return base;
    }

    @Override
    public final URI getBaseUri() {
        return URIUtils.appendResources(repository);
    }

    protected Revision getConcreteRevision(final View view, final Revision revision) {
        if (Revision.HEAD.equals(revision)) {
            return view.getHeadRevision();
        }
        return revision;
    }

    protected Set<Info> getInfoSetWithLockTokens(final Transaction transaction) {
        validateTransaction(transaction);

        final Map<Resource, Status> changeSet = transaction.getChangeSet();
        if (changeSet.isEmpty()) {
            return Collections.emptySet();
        }

        final Set<Info> infoSet = new TreeSet<>(Info.RESOURCE_COMPARATOR);
        for (final Map.Entry<Resource, Status> entry : changeSet.entrySet()) {
            final Status status = entry.getValue();
            if ((Status.EXISTS == status) || (Status.ADDED == status)) {
                continue;
            }

            final QualifiedResource resource = new QualifiedResource(base, entry.getKey());
            final Optional<Info> info = info0(transaction, resource, transaction.getHeadRevision(), false, LOCKING);
            if (info.isPresent() && info.get().isLocked()) {
                infoSet.add(info.get());
            }
        }
        return infoSet;
    }

    @Override
    public final UUID getRepositoryId() {
        return repositoryId;
    }

    @Override
    public Info info(final Resource resource, final Revision revision, final ResourceProperty.Key... keys) {
        return info(createView(), resource, revision, keys);
    }

    @Override
    public Info info(final View view, final Resource resource, final Revision revision, final ResourceProperty.Key... keys) {
        Validate.notNull(view, "view must not be null");
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(revision, "revision must not be null");
        Validate.noNullElements(keys, "keys must not contain null elements");
        validateRevision(view, revision);

        LOGGER.trace("retrieving info for resource {}@{}", resource, revision);
        final Optional<Info> info = info0(view, new QualifiedResource(base, resource), revision, true, keys);
        return info.orElseThrow(() -> new SubversionException("Can't resolve: " + resource + '@' + revision));
    }

    private Optional<Info> info0(final View view, final QualifiedResource resource, final Revision revision, final boolean resolve, final ResourceProperty.Key[] keys) {
        final QualifiedResource resolved = resolve(view, resource, revision, resolve);
        final InfoOperation operation = new InfoOperation(repository, base, resolved, config.getPrefix(), keys);
        return operation.execute(client, context);
    }

    @Override
    public Set<Info> list(final Resource resource, final Revision revision, final Depth depth, final ResourceProperty.Key... keys) {
        return list(createView(), resource, revision, depth, keys);
    }

    @Override
    public Set<Info> list(final View view, final Resource resource, final Revision revision, final Depth depth, final ResourceProperty.Key... keys) {
        Validate.notNull(view, "view must not be null");
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(revision, "revision must not be null");
        Validate.notNull(depth, "depth must not be null");
        Validate.noNullElements(keys, "keys must not contain null elements");
        validateRevision(view, revision);

        LOGGER.trace("listing info for resource {}@{} and depth {}", resource, revision, depth);
        return list0(view, new QualifiedResource(base, resource), revision, depth, keys);
    }

    private Set<Info> list0(final View view, final QualifiedResource resource, final Revision revision, final Depth depth, final ResourceProperty.Key[] keys) {
        if (Depth.INFINITY == depth) {
            final Set<Info> result = new TreeSet<>(Info.RESOURCE_COMPARATOR);
            listRecursively0(view, resource, revision, result, keys);
            return result;
        }

        final QualifiedResource resolved = resolve(view, resource, revision, true);
        final ListOperation operation = new ListOperation(repository, base, resolved, config.getPrefix(), depth, keys);
        final Optional<Set<Info>> infoSet = operation.execute(client, context);
        return infoSet.orElseThrow(() -> new SubversionException("Can't resolve: " + resource + '@' + revision));
    }

    private void listRecursively0(final View view, final QualifiedResource resource, final Revision revision, final Set<Info> result, final ResourceProperty.Key[] keys) {
        for (final Info info : list0(view, resource, revision, Depth.IMMEDIATES, keys)) {
            if (!result.add(info)) {
                continue;
            }

            if (info.isDirectory() && !resource.getResource().equals(info.getResource())) {
                listRecursively0(view, new QualifiedResource(base, info.getResource()), revision, result, keys);
            }
        }
    }

    @Override
    public final void lock(final Resource resource, final boolean steal) {
        Validate.notNull(resource, "resource must not be null");

        LOGGER.trace("locking resource {} (steal: {})", resource, steal);
        final LockOperation operation = new LockOperation(repository, new QualifiedResource(base, resource), steal);
        operation.execute(client, context);
    }

    @Override
    public List<Log> log(final Resource resource, final Revision startRevision, final Revision endRevision, final int limit, final boolean stopOnCopy) {
        return log(createView(), resource, startRevision, endRevision, limit, stopOnCopy);
    }

    @Override
    public final List<Log> log(final View view, final Resource resource, final Revision startRevision, final Revision endRevision, final int limit, final boolean stopOnCopy) {
        Validate.notNull(view, "view must not be null");
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(startRevision, "startRevision must not be null");
        Validate.notNull(endRevision, "endRevision must not be null");
        validateRevision(view, startRevision);
        validateRevision(view, endRevision);

        LOGGER.trace("retrieving log for resource {} from {} to {} (limit: {})", resource, startRevision, endRevision, limit);
        return log0(view, new QualifiedResource(base, resource), startRevision, endRevision, limit, stopOnCopy);
    }

    private List<Log> log0(final View view, final QualifiedResource resource, final Revision startRevision, final Revision endRevision, final int limit, final boolean stopOnCopy) {
        final Revision concreteStartRevision = getConcreteRevision(view, startRevision);
        final Revision concreteEndRevision = getConcreteRevision(view, endRevision);

        final Revision resoledRevision = (concreteStartRevision.compareTo(concreteEndRevision) > 0) ? concreteStartRevision : concreteEndRevision;
        final QualifiedResource resolved = resolve(view, resource, resoledRevision, false);

        final LogOperation operation = new LogOperation(repository, resolved, concreteStartRevision, concreteEndRevision, limit, stopOnCopy);
        return operation.execute(client, context);
    }

    @Override
    public void mkdir(final Transaction transaction, final Resource resource, final boolean parent) {
        validateTransaction(transaction);
        Validate.notNull(resource, "resource must not be null");

        LOGGER.trace("creating folder for resource {} during {} (parent: {})", resource, transaction.getId(), parent);
        createFolder(transaction, new QualifiedResource(base, resource), parent);
    }

    @Override
    public void move(final Transaction transaction, final Resource srcResource, final Resource targetResource, final boolean parents) {
        validateTransaction(transaction);
        Validate.notNull(srcResource, "srcResource must not be null");
        Validate.notNull(targetResource, "targetResource must not be null");

        LOGGER.trace("moving {} to {} during {} (parents: {})", srcResource, targetResource, transaction.getId(), parents);
        final QualifiedResource qualifiedSourceResource = new QualifiedResource(base, srcResource);
        copy0(transaction, qualifiedSourceResource, transaction.getHeadRevision(), new QualifiedResource(base, targetResource), parents, false);
        delete0(transaction, qualifiedSourceResource);
    }

    @Override
    public void propertiesDelete(final Transaction transaction, final Resource resource, final ResourceProperty... properties) {
        propertiesUpdate(transaction, new QualifiedResource(base, resource), PropertiesUpdateOperation.Type.DELETE, properties);
    }

    @Override
    public void propertiesSet(final Transaction transaction, final Resource resource, final ResourceProperty... properties) {
        propertiesUpdate(transaction, new QualifiedResource(base, resource), PropertiesUpdateOperation.Type.SET, properties);
    }

    protected void propertiesUpdate(final Transaction transaction, final QualifiedResource resource, final PropertiesUpdateOperation.Type type, final ResourceProperty... properties) {
        validateTransaction(transaction);
        Validate.notNull(resource, "resource must not be null");
        Validate.noNullElements(properties, "properties must not contain null elements");

        LOGGER.trace("updating properties {} on {} during {}", properties, resource.getResource(), transaction.getId());

        // there can only be a lock token if the file is already in the
        // repository
        final Optional<Info> info = info0(transaction, resource, transaction.getHeadRevision(), false, LOCKING);
        final Optional<LockToken> lockToken = info.flatMap(Info::getLockToken);

        final QualifiedResource r = config.getWorkingResource(transaction).append(resource);
        final PropertiesUpdateOperation operation = new PropertiesUpdateOperation(repository, r, type, lockToken, properties);
        operation.execute(client, context);
        transaction.register(resource.getResource(), Status.MODIFIED);
    }

    protected abstract void registerResource(Transaction transaction, Resource resource, Revision revision);

    protected QualifiedResource resolve(final View view, final QualifiedResource resource, final Revision revision, final boolean resolve) {
        if (Revision.HEAD.equals(revision)) {
            if (resolve) {
                return config.getVersionedResource(resource, view.getHeadRevision());
            }
            return resource;
        }

        if (resolve) {
            final ResolveOperation r = new ResolveOperation(repository, resource, view.getHeadRevision(), revision, config);
            return r.execute(client, context).orElse(config.getVersionedResource(resource, revision));
        }
        return config.getVersionedResource(resource, revision);
    }

    @Override
    public void rollback(final Transaction transaction) {
        validateTransaction(transaction);

        LOGGER.trace("rolling transaction {} back", transaction.getId());
        try {
            final QualifiedResource resource = config.getTransactionResource(transaction);
            final Optional<LockToken> token = Optional.empty();
            final DeleteOperation operation = new DeleteOperation(repository, resource, token);
            operation.execute(client, context);
        } finally {
            transaction.invalidate();
        }
    }

    @Override
    public void rollbackIfNotCommitted(final Transaction transaction) {
        Validate.notNull(transaction, "transaction must not be null");

        if (transaction.isActive()) {
            rollback(transaction);
        }
    }

    @Override
    public final void unlock(final Resource resource, final boolean force) {
        Validate.notNull(resource, "resource must not be null");

        LOGGER.trace("unlocking {} (force: {})", resource, force);
        unlock0(createView(), new QualifiedResource(base, resource), force);
    }

    private void unlock0(final View view, final QualifiedResource resource, final boolean force) {
        final Optional<Info> info = info0(view, resource, view.getHeadRevision(), true, LOCKING);
        final Optional<LockToken> lockToken = info.orElseThrow(() -> new SubversionException("Can't resolve: " + resource + '@' + view.getHeadRevision())).getLockToken();

        if (!lockToken.isPresent()) {
            return;
        }
        final UnlockOperation operation = new UnlockOperation(repository, resource, lockToken.get(), force);
        operation.execute(client, context);
    }

    protected void validateRevision(final View view, final Revision revision) {
        if (Revision.HEAD.equals(revision)) {
            return;
        }

        final Revision headRevision = view.getHeadRevision();
        if (headRevision.compareTo(revision) < 0) {
            throw new SubversionException("revision " + revision + " is to new for Transaction/View with head revision " + headRevision);
        }
    }

    protected void validateTransaction(final Transaction transaction) {
        Validate.notNull(transaction, "transaction must not be null");

        validateView(transaction);
        if (!transaction.isActive()) {
            throw new SubversionException("Transaction invalid: has already been committed or reverted");
        }
    }

    protected void validateView(final View view) {
        Validate.notNull(view, "view must not be null");

        final UUID transactionRepositoryId = view.getRepositoryId();
        if (!repositoryId.equals(transactionRepositoryId)) {
            throw new SubversionException("Transaction/View invalid: does not belong to this repository");
        }
    }
}
