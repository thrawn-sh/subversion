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
import org.apache.http.HttpStatus;
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
import de.shadowhunt.subversion.View;

/**
 * Base for all {@link de.shadowhunt.subversion.Repository}
 */
public abstract class AbstractBaseRepository implements Repository {

    private static final ResourceProperty.Key[] LOCKING = new ResourceProperty.Key[] { ResourceProperty.RESOURCE, ResourceProperty.LOCK_STATUS };

    private static final Logger LOGGER = LoggerFactory.getLogger("de.shadowhunt.subversion.Repository");

    private static final ResourceProperty.Key[] REPOSITORY_UUID = new ResourceProperty.Key[] { ResourceProperty.REPOSITORY_ID };

    private static final ResourceProperty.Key[] REVISION = new ResourceProperty.Key[] { ResourceProperty.RESOURCE, ResourceProperty.VERSION };

    private static final ResourceProperty.Key[] TYPE = new ResourceProperty.Key[] { ResourceProperty.RESOURCE, ResourceProperty.RESOURCE_TYPE };

    @CheckForNull
    private static UUID determineRepositoryId(final URI repository, final HttpClient client, final HttpContext context, final Resource marker) {
        final InfoOperation operation = new InfoOperation(repository, Resource.ROOT, marker, REPOSITORY_UUID);
        final Info info = operation.execute(client, context);
        if (info == null) {
            throw new SubversionException("No repository found at " + repository, HttpStatus.SC_BAD_REQUEST);
        }
        return info.getRepositoryId();
    }

    protected static interface ResourceMapper {

        Resource getCommitMessageResource(Transaction transaction);

        Resource getCreateTransactionResource();

        Resource getPrefix();

        Resource getRegisterResource(Resource resource, Revision revision);

        Resource getRegisterTransactionResource(Transaction transaction);

        Resource getTransactionResource(Transaction transaction);

        Resource getVersionedResource(Resource resource, Revision revision);

        Resource getWorkingResource(Transaction transaction);
    }

    protected final HttpClient client;

    protected final ResourceMapper config;

    protected final HttpContext context;

    protected final URI repository;

    protected final UUID repositoryId;

    protected AbstractBaseRepository(final URI repository, final ResourceMapper config, final HttpClient client, final HttpContext context) {
        Validate.notNull(repository, "repository must not be null");
        Validate.notNull(config, "config must not be null");
        Validate.notNull(client, "client must not be null");
        Validate.notNull(context, "context must not be null");

        this.repository = URIUtils.createURI(repository);
        this.config = config;
        this.client = client;
        this.context = context;

        repositoryId = determineRepositoryId(repository, client, context, config.getPrefix());
        Validate.notNull(repositoryId, "repositoryId must not be null");
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

        final Info info = info0(transaction, resource, transaction.getHeadRevision(), true, LOCKING);
        String lockToken = null;
        if (info != null) {
            lockToken = info.getLockToken();
        }
        final Resource uploadResource = config.getWorkingResource(transaction).append(resource);
        final UploadOperation operation = new UploadOperation(repository, uploadResource, lockToken, content);
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
        validateRevision(transaction, sourceRevision);

        LOGGER.trace("copying resource from {}@{} to {} during transaction {} (parents: {})", sourceResource, sourceRevision, targetResource, transaction.getId(), parents);
        if (parents) {
            createFolder(transaction, targetResource.getParent(), true);
        } else {
            registerResource(transaction, targetResource.getParent(), transaction.getHeadRevision());
        }

        final Info sourceInfo = info0(transaction, sourceResource, sourceRevision, true, REVISION);
        if (sourceInfo == null) {
            throw new SubversionException("Can't resolve: " + sourceResource + '@' + sourceRevision);
        }

        final Info targetInfo = info0(transaction, targetResource, transaction.getHeadRevision(), true, LOCKING);
        String lockToken = null;
        if (targetInfo != null) {
            lockToken = targetInfo.getLockToken();
        }

        final Resource source = config.getVersionedResource(sourceInfo.getResource(), sourceInfo.getRevision());
        final Resource target = config.getWorkingResource(transaction).append(targetResource);

        final CopyOperation operation = new CopyOperation(repository, source, target, lockToken);
        operation.execute(client, context);

        if (targetInfo == null) {
            transaction.register(targetResource, Status.ADDED);
        } else {
            transaction.register(targetResource, Status.MODIFIED);
        }
    }

    private void createFolder(final Transaction transaction, final Resource resource, final boolean parents) {
        final Info info = info0(transaction, resource, transaction.getHeadRevision(), true, TYPE); // null if resource does not exists

        if (parents && (info == null) && !Resource.ROOT.equals(resource)) {
            createFolder(transaction, resource.getParent(), true);
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
                registerResource(transaction, resource, transaction.getHeadRevision());
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
        final Info info = info0(transaction, resource, transaction.getHeadRevision(), true, LOCKING);
        if (info == null) {
            throw new SubversionException("Can't resolve: " + resource + '@' + Revision.HEAD);
        }

        final DeleteOperation operation = new DeleteOperation(repository, config.getWorkingResource(transaction).append(resource), info.getLockToken());
        operation.execute(client, context);
        transaction.register(resource, Status.DELETED);
    }

    protected Revision determineHeadRevision() {
        final InfoOperation operation = new InfoOperation(repository, Resource.ROOT, config.getPrefix(), REVISION);
        final Info info = operation.execute(client, context);
        return info.getRevision();
    }

    @Override
    public final InputStream download(final View view, final Resource resource, final Revision revision) {
        Validate.notNull(view, "view must not be null");
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(revision, "revision must not be null");
        validateRevision(view, revision);

        LOGGER.trace("downloading resource {}@{}", resource, revision);
        return download0(view, resource, revision);
    }

    @Override
    public InputStream download(final Resource resource, final Revision revision) {
        return download(createView(), resource, revision);
    }

    private InputStream download0(final View view, final Resource resource, final Revision revision) {
        final Resource resolved = resolve2(view, resource, revision, false);
        final DownloadOperation operation = new DownloadOperation(repository, resolved);
        final InputStream is = operation.execute(client, context);
        if (is == null) {
            throw new SubversionException("Can't resolve: " + resource + '@' + revision);
        }
        return is;
    }

    @Override
    public final URI downloadURI(final View view, final Resource resource, final Revision revision) {
        Validate.notNull(view, "view must not be null");
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(revision, "revision must not be null");
        validateRevision(view, revision);

        LOGGER.trace("creating download uri for resource {}@{}", resource, revision);
        return downloadURI0(view, resource, revision);
    }

    @Override
    public URI downloadURI(final Resource resource, final Revision revision) {
        return downloadURI(createView(), resource, revision);
    }

    private URI downloadURI0(final View view, final Resource resource, final Revision revision) {
        if (!exists0(view, resource, revision)) {
            throw new SubversionException("Can't resolve: " + resource + '@' + revision);
        }
        final Resource resolved = resolve2(view, resource, revision, true);
        return URIUtils.createURI(repository, resolved);
    }

    @Override
    public final boolean exists(final View view, final Resource resource, final Revision revision) {
        Validate.notNull(view, "view must not be null");
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(revision, "revision must not be null");
        validateRevision(view, revision);

        LOGGER.trace("checking existence for resource {}@{}", resource, revision);
        return exists0(view, resource, revision);
    }

    @Override
    public boolean exists(final Resource resource, final Revision revision) {
        return exists(createView(), resource, revision);
    }

    private boolean exists0(final View view, final Resource resource, final Revision revision) {
        // ask the server
        final Resource resolved = resolve2(view, resource, revision, false);
        final ExistsOperation operation = new ExistsOperation(repository, resolved, config.getPrefix());
        return operation.execute(client, context);
    }

    @Override
    public final URI getBaseUri() {
        return URIUtils.createURI(repository);
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
            final Info info = info0(transaction, resource, transaction.getHeadRevision(), false, LOCKING);
            if ((info != null) && info.isLocked()) {
                infoSet.add(info);
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
        final Info info = info0(view, resource, revision, true, keys);
        if (info == null) {
            throw new SubversionException("Can't resolve: " + resource + '@' + revision);
        }
        return info;
    }

    @CheckForNull
    private Info info0(final View view, final Resource resource, final Revision revision, final boolean resolve, @CheckForNull final ResourceProperty.Key[] keys) {
        final Resource resolved = resolve2(view, resource, revision, resolve);
        final InfoOperation operation = new InfoOperation(repository, resolved, config.getPrefix(), keys);
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
        return list0(view, resource, revision, depth, keys);
    }

    private Set<Info> list0(final View view, final Resource resource, final Revision revision, final Depth depth, @CheckForNull final ResourceProperty.Key[] keys) {
        if (Depth.INFINITY == depth) {
            final Set<Info> result = new TreeSet<>(Info.RESOURCE_COMPARATOR);
            listRecursively0(view, resource, revision, result, keys);
            return result;
        }

        final Resource resolved = resolve2(view, resource, revision, true);
        final ListOperation operation = new ListOperation(repository, resolved, config.getPrefix(), depth, keys);
        final Set<Info> infoSet = operation.execute(client, context);
        if (infoSet == null) {
            throw new SubversionException("Can't resolve: " + resource + '@' + revision);
        }
        return infoSet;
    }

    private void listRecursively0(final View view, final Resource resource, final Revision revision, final Set<Info> result, @CheckForNull final ResourceProperty.Key[] keys) {
        for (final Info info : list0(view, resource, revision, Depth.IMMEDIATES, keys)) {
            if (!result.add(info)) {
                continue;
            }

            if (info.isDirectory() && !resource.equals(info.getResource())) {
                listRecursively0(view, info.getResource(), revision, result, keys);
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
    public final List<Log> log(final View view, final Resource resource, final Revision startRevision, final Revision endRevision, final int limit) {
        Validate.notNull(view, "view must not be null");
        Validate.notNull(resource, "resource must not be null");
        Validate.notNull(startRevision, "startRevision must not be null");
        Validate.notNull(endRevision, "endRevision must not be null");
        validateRevision(view, startRevision);
        validateRevision(view, endRevision);

        LOGGER.trace("retrieving log for resource {} from {} to {} (limit: {})", resource, startRevision, endRevision, limit);
        return log0(view, resource, startRevision, endRevision, limit);
    }

    @Override
    public List<Log> log(final Resource resource, final Revision startRevision, final Revision endRevision, final int limit) {
        return log(createView(), resource, startRevision, endRevision, limit);
    }

    private List<Log> log0(final View view, final Resource resource, final Revision startRevision, final Revision endRevision, final int limit) {
        final Revision concreteStartRevision = getConcreteRevision(view, startRevision);
        final Revision concreteEndRevision = getConcreteRevision(view, endRevision);

        final Revision resoledRevision = (concreteStartRevision.compareTo(concreteEndRevision) > 0) ? concreteStartRevision : concreteEndRevision;
        final Resource resolved = resolve2(view, resource, resoledRevision, true);

        final LogOperation operation = new LogOperation(repository, resolved, concreteStartRevision, concreteEndRevision, limit);
        final List<Log> logs = operation.execute(client, context);
        if (logs == null) {
            throw new SubversionException("Can't resolve: " + resource + '@' + resoledRevision);
        }
        return logs;
    }

    @Override
    public void mkdir(final Transaction transaction, final Resource resource, final boolean parent) {
        validateTransaction(transaction);
        Validate.notNull(resource, "resource must not be null");

        LOGGER.trace("creating folder for resource {} during {} (parent: {})", resource, transaction.getId(), parent);
        createFolder(transaction, resource, parent);
    }

    @Override
    public void move(final Transaction transaction, final Resource srcResource, final Resource targetResource, final boolean parents) {
        validateTransaction(transaction);
        Validate.notNull(srcResource, "srcResource must not be null");
        Validate.notNull(targetResource, "targetResource must not be null");

        LOGGER.trace("moving {} to {} during {} (parents: {})", srcResource, targetResource, transaction.getId(), parents);
        copy(transaction, srcResource, transaction.getHeadRevision(), targetResource, parents);
        delete(transaction, srcResource);
    }

    @Override
    public void propertiesDelete(final Transaction transaction, final Resource resource, final ResourceProperty... properties) {
        propertiesUpdate(transaction, resource, PropertiesUpdateOperation.Type.DELETE, properties);
    }

    @Override
    public void propertiesSet(final Transaction transaction, final Resource resource, final ResourceProperty... properties) {
        propertiesUpdate(transaction, resource, PropertiesUpdateOperation.Type.SET, properties);
    }

    protected void propertiesUpdate(final Transaction transaction, final Resource resource, final PropertiesUpdateOperation.Type type, final ResourceProperty... properties) {
        validateTransaction(transaction);
        Validate.notNull(resource, "resource must not be null");
        Validate.noNullElements(properties, "properties must not contain null elements");

        LOGGER.trace("updating properties {} on {} during {}", properties, resource, transaction.getId());

        // there can only be a lock token if the file is already in the repository
        final Info info = info0(transaction, resource, transaction.getHeadRevision(), true, LOCKING);
        String lockToken = null;
        if (info != null) {
            lockToken = info.getLockToken();
        }

        final Resource r = config.getWorkingResource(transaction).append(resource);
        final PropertiesUpdateOperation operation = new PropertiesUpdateOperation(repository, r, type, lockToken, properties);
        operation.execute(client, context);
        transaction.register(resource, Status.MODIFIED);
    }

    protected abstract void registerResource(Transaction transaction, Resource resource, Revision revision);

    protected Resource resolve2(final View view, final Resource resource, final Revision revision, final boolean resolve) {
        if (Revision.HEAD.equals(revision)) {
            if (resolve) {
                return config.getVersionedResource(resource, view.getHeadRevision());
            }
            return resource;
        }
        return config.getVersionedResource(resource, revision);
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
        unlock0(createView(), resource, force);
    }

    private void unlock0(final View view, final Resource resource, final boolean force) {
        final Info info = info0(view, resource, view.getHeadRevision(), true, LOCKING);
        if (info == null) {
            throw new SubversionException("Can't resolve: " + resource + '@' + view.getHeadRevision());
        }

        final String lockToken = info.getLockToken();
        if (lockToken == null) {
            return;
        }
        final UnlockOperation operation = new UnlockOperation(repository, resource, lockToken, force);
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
