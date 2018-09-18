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
        repositoryId = id;
        this.config = config;
        this.client = client;
        this.context = context;
    }

    @Override
    public void add(final Transaction transaction, final Resource resource, final boolean parents, final InputStream content) {
        validateTransaction(transaction);
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(content, "content must not be null");

        final String transactionId = transaction.getId();
        LOGGER.trace("adding resource {} during transaction {} (parents: {})", resource, transactionId, parents);
        if (parents) {
            final Resource parent = resource.getParent();
            mkdir(transaction, parent, true);
        }

        final QualifiedResource qualifiedResource = new QualifiedResource(base, resource);
        final Revision transactionHeadRevision = transaction.getHeadRevision();
        final Optional<Info> info = info0(transaction, qualifiedResource, transactionHeadRevision, false, LOCKING);
        final Optional<LockToken> lockToken = info.flatMap(Info::getLockToken);

        final QualifiedResource workingResource = config.getWorkingResource(transaction);
        final QualifiedResource uploadResource = workingResource.append(qualifiedResource);
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

        final String transactionId = transaction.getId();
        LOGGER.trace("copying resource from {}@{} to {} during transaction {} (parents: {})", sourceResource, sourceRevision, targetResource, transactionId, parents);
        final QualifiedResource qualifiedSourceResource = new QualifiedResource(base, sourceResource);
        final QualifiedResource qualifiedTargetResource = new QualifiedResource(base, targetResource);
        copy0(transaction, qualifiedSourceResource, sourceRevision, qualifiedTargetResource, parents, true);
    }

    private void copy0(final Transaction transaction, final QualifiedResource qualifiedSourceResource, final Revision sourceRevision, final QualifiedResource qualifiedTargetResource, final boolean parents, final boolean resolveSource) {
        final QualifiedResource qualifiedParentResource = qualifiedTargetResource.getParent();
        final Revision transactionHeadRevision = transaction.getHeadRevision();
        if (parents) {
            createFolder(transaction, qualifiedParentResource, true);
        } else {
            final Resource parentResource = qualifiedParentResource.getResource();
            registerResource(transaction, parentResource, transactionHeadRevision);
        }

        final Optional<Info> sourceInfo = info0(transaction, qualifiedSourceResource, sourceRevision, resolveSource, REVISION);
        final Info sourceInfoValue = sourceInfo.orElseThrow(() -> new SubversionException("Can't resolve: " + qualifiedSourceResource + '@' + sourceRevision));

        final Optional<Info> targetInfo = info0(transaction, qualifiedTargetResource, transactionHeadRevision, false, LOCKING);
        final Optional<LockToken> lockToken = targetInfo.flatMap(Info::getLockToken);

        final Resource sourceResource = sourceInfoValue.getResource();
        final QualifiedResource resource = new QualifiedResource(base, sourceResource);
        final Revision sourceInfoRevision = sourceInfoValue.getRevision();
        final QualifiedResource source = config.getVersionedResource(resource, sourceInfoRevision);
        final QualifiedResource workingResource = config.getWorkingResource(transaction);
        final QualifiedResource target = workingResource.append(qualifiedTargetResource);

        final CopyOperation operation = new CopyOperation(repository, source, target, lockToken);
        operation.execute(client, context);

        final Resource targetResource = qualifiedTargetResource.getResource();
        if (targetInfo.isPresent()) {
            transaction.register(targetResource, Status.MODIFIED);
        } else {
            transaction.register(targetResource, Status.ADDED);
        }
    }

    private void createFolder(final Transaction transaction, final QualifiedResource qualifiedResource, final boolean parents) {
        final Revision headRevision = transaction.getHeadRevision();
        final Optional<Info> info = info0(transaction, qualifiedResource, headRevision, false, TYPE);

        final Resource resource = qualifiedResource.getResource();
        final QualifiedResource qualifiedParent = qualifiedResource.getParent();
        if (parents && !info.isPresent() && !Resource.ROOT.equals(resource)) {
            createFolder(transaction, qualifiedParent, true);
        }

        if (info.isPresent()) {
            final Info infoElement = info.get();
            if (infoElement.isFile()) {
                throw new SubversionException("Can not create folder. File with same name already exists: " + qualifiedResource);
            }
            final Map<Resource, Status> changeSet = transaction.getChangeSet();
            final Status status = changeSet.get(resource);
            if (status == null) {
                registerResource(transaction, resource, headRevision);
            }
        } else {
            final QualifiedResource workingResource = config.getWorkingResource(transaction);
            final QualifiedResource folder = workingResource.append(qualifiedResource);
            final CreateFolderOperation operation = new CreateFolderOperation(repository, folder);
            operation.execute(client, context);
            transaction.register(resource, Status.ADDED);
        }

        QualifiedResource currentQualifiedResource = qualifiedParent;
        Resource currentResource = currentQualifiedResource.getResource();
        while (!Resource.ROOT.equals(currentResource)) {
            if (!transaction.register(currentResource, Status.EXISTS)) {
                break;
            }
            currentQualifiedResource = currentQualifiedResource.getParent();
            currentResource = currentQualifiedResource.getResource();
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

        final String transactionId = transaction.getId();
        LOGGER.trace("deleting resource {} during transaction {}", resource, transactionId);
        final QualifiedResource qualifiedResource = new QualifiedResource(base, resource);
        delete0(transaction, qualifiedResource);
    }

    private void delete0(final Transaction transaction, final QualifiedResource qualifiedResource) {
        final Revision transactionHeadRevision = transaction.getHeadRevision();
        final Optional<Info> info = info0(transaction, qualifiedResource, transactionHeadRevision, false, LOCKING);
        if (!info.isPresent()) {
            throw new SubversionException("Can't resolve: " + qualifiedResource + '@' + Revision.HEAD);
        }

        final QualifiedResource workingResource = config.getWorkingResource(transaction);
        final QualifiedResource deleteResource = workingResource.append(qualifiedResource);
        final Optional<LockToken> lockToken = info.flatMap(Info::getLockToken);
        final DeleteOperation operation = new DeleteOperation(repository, deleteResource, lockToken);
        operation.execute(client, context);
        final Resource resource = qualifiedResource.getResource();
        transaction.register(resource, Status.DELETED);
    }

    protected Revision determineHeadRevision() {
        final QualifiedResource qualifiedRoot = new QualifiedResource(base, Resource.ROOT);
        final Resource prefix = config.getPrefix();
        final InfoOperation operation = new InfoOperation(repository, base, qualifiedRoot, prefix, REVISION);
        final Optional<Info> info = operation.execute(client, context);
        if (!info.isPresent()) {
            throw new SubversionException("can not determine HEAD revision");
        }
        final Info infoElement = info.get();
        return infoElement.getRevision();
    }

    @Override
    public final InputStream download(final View view, final Resource resource, final Revision revision) {
        Validate.notNull(view, "view must not be null");
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(revision, "revision must not be null");
        validateRevision(view, revision);

        LOGGER.trace("downloading resource {}@{}", resource, revision);
        final QualifiedResource qualifiedResource = new QualifiedResource(base, resource);
        return download0(view, qualifiedResource, revision);
    }

    private InputStream download0(final View view, final QualifiedResource resource, final Revision revision) {
        final QualifiedResource resolved = resolve(view, resource, revision, false);
        final DownloadOperation operation = new DownloadOperation(repository, resolved);
        final Optional<InputStream> is = operation.execute(client, context);
        return is.orElseThrow(() -> new SubversionException("Can't resolve: " + resource + '@' + revision));
    }

    @Override
    public final URI downloadURI(final View view, final Resource resource, final Revision revision) {
        Validate.notNull(view, "view must not be null");
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(revision, "revision must not be null");
        validateRevision(view, revision);

        LOGGER.trace("creating download uri for resource {}@{}", resource, revision);
        final QualifiedResource qualifiedResource = new QualifiedResource(base, resource);
        return downloadURI0(view, qualifiedResource, revision);
    }

    private URI downloadURI0(final View view, final QualifiedResource resource, final Revision revision) {
        if (!exists0(view, resource, revision)) {
            throw new SubversionException("Can't resolve: " + resource + '@' + revision);
        }
        final QualifiedResource resolved = resolve(view, resource, revision, true);
        return URIUtils.appendResources(repository, resolved);
    }

    @Override
    public final boolean exists(final View view, final Resource resource, final Revision revision) {
        Validate.notNull(view, "view must not be null");
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(revision, "revision must not be null");
        validateRevision(view, revision);

        LOGGER.trace("checking existence for resource {}@{}", resource, revision);
        final QualifiedResource qualifiedResource = new QualifiedResource(base, resource);
        return exists0(view, qualifiedResource, revision);
    }

    private boolean exists0(final View view, final QualifiedResource resource, final Revision revision) {
        // ask the server
        final QualifiedResource resolved = resolve(view, resource, revision, false);
        final Resource prefix = config.getPrefix();
        final ExistsOperation operation = new ExistsOperation(repository, resolved, prefix);
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

            final Resource resource = entry.getKey();
            final QualifiedResource qualifiedResource = new QualifiedResource(base, resource);
            final Revision transactionHeadRevision = transaction.getHeadRevision();
            final Optional<Info> info = info0(transaction, qualifiedResource, transactionHeadRevision, false, LOCKING);
            if (info.isPresent()) {
                final Info infoElement = info.get();
                if (infoElement.isLocked()) {
                    infoSet.add(infoElement);
                }
            }
        }
        return infoSet;
    }

    @Override
    public final UUID getRepositoryId() {
        return repositoryId;
    }

    @Override
    public Info info(final View view, final Resource resource, final Revision revision, final ResourceProperty.Key... keys) {
        Validate.notNull(view, "view must not be null");
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(revision, "revision must not be null");
        Validate.noNullElements(keys, "keys must not contain null elements");
        validateRevision(view, revision);

        LOGGER.trace("retrieving info for resource {}@{}", resource, revision);
        final QualifiedResource qualifiedResource = new QualifiedResource(base, resource);
        final Optional<Info> info = info0(view, qualifiedResource, revision, true, keys);
        return info.orElseThrow(() -> new SubversionException("Can't resolve: " + resource + '@' + revision));
    }

    private Optional<Info> info0(final View view, final QualifiedResource resource, final Revision revision, final boolean resolve, final ResourceProperty.Key... keys) {
        final QualifiedResource resolved = resolve(view, resource, revision, resolve);
        final Resource prefix = config.getPrefix();
        final InfoOperation operation = new InfoOperation(repository, base, resolved, prefix, keys);
        return operation.execute(client, context);
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
        final QualifiedResource qualifedResource = new QualifiedResource(base, resource);
        return list0(view, qualifedResource, revision, depth, keys);
    }

    private Set<Info> list0(final View view, final QualifiedResource resource, final Revision revision, final Depth depth, final ResourceProperty.Key... keys) {
        if (Depth.INFINITY == depth) {
            final Set<Info> result = new TreeSet<>(Info.RESOURCE_COMPARATOR);
            listRecursively0(view, resource, revision, result, keys);
            return result;
        }

        final QualifiedResource resolved = resolve(view, resource, revision, true);
        final Resource prefix = config.getPrefix();
        final ListOperation operation = new ListOperation(repository, base, resolved, prefix, depth, keys);
        final Optional<Set<Info>> infoSet = operation.execute(client, context);
        return infoSet.orElseThrow(() -> new SubversionException("Can't resolve: " + resource + '@' + revision));
    }

    private void listRecursively0(final View view, final QualifiedResource qualifiedResource, final Revision revision, final Set<Info> result, final ResourceProperty.Key... keys) {
        for (final Info info : list0(view, qualifiedResource, revision, Depth.IMMEDIATES, keys)) {
            if (!result.add(info)) {
                continue;
            }

            final Resource resource = qualifiedResource.getResource();
            final Resource infoResource = info.getResource();
            if (info.isDirectory() && !resource.equals(infoResource)) {
                final QualifiedResource qualifiedResourceNext = new QualifiedResource(base, infoResource);
                listRecursively0(view, qualifiedResourceNext, revision, result, keys);
            }
        }
    }

    @Override
    public final void lock(final Resource resource, final boolean steal) {
        Validate.notNull(resource, "resource must not be null");

        LOGGER.trace("locking resource {} (steal: {})", resource, steal);
        final QualifiedResource qualifiedResource = new QualifiedResource(base, resource);
        final LockOperation operation = new LockOperation(repository, qualifiedResource, steal);
        operation.execute(client, context);
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
        final QualifiedResource qualifiedResource = new QualifiedResource(base, resource);
        return log0(view, qualifiedResource, startRevision, endRevision, limit, stopOnCopy);
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

        final String transactionId = transaction.getId();
        LOGGER.trace("creating folder for resource {} during {} (parent: {})", resource, transactionId, parent);
        final QualifiedResource qualifiedResource = new QualifiedResource(base, resource);
        createFolder(transaction, qualifiedResource, parent);
    }

    @Override
    public void move(final Transaction transaction, final Resource srcResource, final Resource targetResource, final boolean parents) {
        validateTransaction(transaction);
        Validate.notNull(srcResource, "srcResource must not be null");
        Validate.notNull(targetResource, "targetResource must not be null");

        final String transactionId = transaction.getId();
        LOGGER.trace("moving {} to {} during {} (parents: {})", srcResource, targetResource, transactionId, parents);
        final QualifiedResource qualifiedSourceResource = new QualifiedResource(base, srcResource);
        final Revision transactionHeadRevision = transaction.getHeadRevision();
        final QualifiedResource qualifiedTargetResource = new QualifiedResource(base, targetResource);
        copy0(transaction, qualifiedSourceResource, transactionHeadRevision, qualifiedTargetResource, parents, false);
        delete0(transaction, qualifiedSourceResource);
    }

    @Override
    public void propertiesDelete(final Transaction transaction, final Resource resource, final ResourceProperty... properties) {
        final QualifiedResource qualifiedResource = new QualifiedResource(base, resource);
        propertiesUpdate(transaction, qualifiedResource, PropertiesUpdateOperation.Type.DELETE, properties);
    }

    @Override
    public void propertiesSet(final Transaction transaction, final Resource resource, final ResourceProperty... properties) {
        final QualifiedResource qualifiedResource = new QualifiedResource(base, resource);
        propertiesUpdate(transaction, qualifiedResource, PropertiesUpdateOperation.Type.SET, properties);
    }

    protected void propertiesUpdate(final Transaction transaction, final QualifiedResource qualifiedResource, final PropertiesUpdateOperation.Type type, final ResourceProperty... properties) {
        validateTransaction(transaction);
        Validate.notNull(qualifiedResource, "resource must not be null");
        Validate.noNullElements(properties, "properties must not contain null elements");

        final Resource resource = qualifiedResource.getResource();
        final String transactionId = transaction.getId();
        LOGGER.trace("updating properties {} on {} during {}", properties, resource, transactionId);

        // there can only be a lock token if the file is already in the
        // repository
        final Revision transactionHeadRevision = transaction.getHeadRevision();
        final Optional<Info> info = info0(transaction, qualifiedResource, transactionHeadRevision, false, LOCKING);
        final Optional<LockToken> lockToken = info.flatMap(Info::getLockToken);

        final QualifiedResource workingResource = config.getWorkingResource(transaction);
        final QualifiedResource r = workingResource.append(qualifiedResource);
        final PropertiesUpdateOperation operation = new PropertiesUpdateOperation(repository, r, type, lockToken, properties);
        operation.execute(client, context);
        transaction.register(resource, Status.MODIFIED);
    }

    protected abstract void registerResource(Transaction transaction, Resource resource, Revision revision);

    protected QualifiedResource resolve(final View view, final QualifiedResource resource, final Revision revision, final boolean resolve) {
        final Revision viewHeadRevision = view.getHeadRevision();
        if (Revision.HEAD.equals(revision)) {
            if (resolve) {
                return config.getVersionedResource(resource, viewHeadRevision);
            }
            return resource;
        }

        final QualifiedResource versionedResource = config.getVersionedResource(resource, revision);
        if (resolve) {
            final ResolveOperation r = new ResolveOperation(repository, resource, viewHeadRevision, revision, config);
            final Optional<QualifiedResource> result = r.execute(client, context);
            return result.orElse(versionedResource);
        }
        return versionedResource;
    }

    @Override
    public void rollback(final Transaction transaction) {
        validateTransaction(transaction);

        final String transactionId = transaction.getId();
        LOGGER.trace("rolling transaction {} back", transactionId);
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
        final View view = createView();
        final QualifiedResource qualifiedResource = new QualifiedResource(base, resource);
        unlock0(view, qualifiedResource, force);
    }

    private void unlock0(final View view, final QualifiedResource resource, final boolean force) {
        final Revision viewHeadRevision = view.getHeadRevision();
        final Optional<Info> info = info0(view, resource, viewHeadRevision, true, LOCKING);
        if (!info.isPresent()) {
            throw new SubversionException("Can't resolve: " + resource + '@' + viewHeadRevision);
        }
        final Info infoElement = info.get();
        final Optional<LockToken> lockToken = infoElement.getLockToken();

        if (!lockToken.isPresent()) {
            return;
        }
        final LockToken lockTokenValue = lockToken.get();
        final UnlockOperation operation = new UnlockOperation(repository, resource, lockTokenValue, force);
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
