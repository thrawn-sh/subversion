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
package de.shadowhunt.subversion.internal.httpv1;

import de.shadowhunt.subversion.Info;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.annotation.Nullable;

import de.shadowhunt.subversion.internal.AbstractRepository;
import de.shadowhunt.subversion.internal.CommitMessageOperation;
import de.shadowhunt.subversion.internal.DeleteOperation;
import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction;

/**
 * {@link Repository} supports subversion servers of version 1.6.X
 */
public class Repository extends AbstractRepository {

	protected static final String PREFIX_VCC = "/!svn/vcc/";

	protected static final String PREFIX_VER = "/!svn/ver/";

	public Repository(final URI repository, final boolean trustServerCertificat) {
		super(repository, trustServerCertificat);
	}

	protected void checkout(final String uuid) {
		final CheckoutOperation co = new CheckoutOperation(repository, Resource.create(PREFIX_VCC + "default"), config.getTransactionResource(uuid));
		co.execute(client, context);
	}

	@Override
	public void copy(final Resource srcResource, final Revision srcRevision, final Resource targetResource, final String message) {
		final String uuid = prepareTransaction().getId();
		try {
			final Info info = info(srcResource, srcRevision, false);
			final Revision realSourceRevision = info.getRevision();
			setCommitMessage(uuid, realSourceRevision, message);
			createFolder(config.getWorkingResource(uuid).append(targetResource.getParent()), true);
			copy0(srcResource, realSourceRevision, targetResource, uuid);
			merge(info, uuid);
		} finally {
			endTransaction(uuid);
		}
	}

	@Override
	public void createFolder(final Resource resource, final boolean parent, final String message) {
		if (exists(resource, Revision.HEAD)) {
			return;
		}

		final String uuid = prepareTransaction().getId();
		try {
			final Resource infoResource = createFolder(config.getWorkingResource(uuid).append(resource), parent);
			final Info info = info(infoResource, Revision.HEAD, false);
			checkout(uuid);
			setCommitMessage(uuid, info.getRevision(), message);
			merge(info, uuid);
		} finally {
			endTransaction(uuid);
		}
	}

	@Override
	public void delete(final Resource resource, final String message) {
		final Info info = info(resource, Revision.HEAD, false);
		final Revision revision = info.getRevision();

		final String uuid = prepareTransaction().getId();
		try {
			checkout(uuid);
			setCommitMessage(uuid, revision, message);
			prepareContentUpload(resource, uuid, revision);
			delete0(resource, uuid);
			merge(info, uuid);
		} finally {
			endTransaction(uuid);
		}
	}

	@Override
	public void deleteProperties(final Resource resource, final String message, final ResourceProperty... properties) {
		final Info info = info(resource, Revision.HEAD, false);
		final Revision revision = info.getRevision();

		final String uuid = prepareTransaction().getId();
		try {
			checkout(uuid);
			setCommitMessage(uuid, revision, message);
			prepareContentUpload(resource, uuid, revision);
			propertiesRemove(resource, info, uuid, properties);
			merge(info, uuid);
		} finally {
			endTransaction(uuid);
		}
	}

	protected void endTransaction(final String uuid) {
		final Resource resource = config.getTransactionResource(uuid);
		final DeleteOperation o = new DeleteOperation(repository, resource);
		o.execute(client, context);
	}

	@Override
	public List<Info> list(final Resource resource, final Revision revision, final Depth depth, final boolean withCustomProperties) {
		final Revision concreateRevision = getConcreteRevision(resource, revision);
		final Resource prefix = config.getVersionedResource(concreateRevision);
		return list(prefix, resource, depth, withCustomProperties);
	}

	@Override
	public void move(final Resource srcResource, final Resource targetResource, final String message) {
		final String uuid = prepareTransaction().getId();
		try {
			final Info info = info(srcResource, Revision.HEAD, false);
			final Revision revision = info.getRevision();
			checkout(uuid);
			setCommitMessage(uuid, revision, message);
			copy0(srcResource, info.getRevision(), targetResource, uuid);
			delete(srcResource, uuid);
			merge(info, uuid);
		} finally {
			endTransaction(uuid);
		}
	}

	protected void prepareContentUpload(final Resource resource, final String uuid, final Revision revision) {
		final CheckoutOperation co = new CheckoutOperation(repository, Resource.create(PREFIX_VER + revision).append(resource), config.getTransactionResource(uuid));
		co.execute(client, context);
	}

	protected Transaction prepareTransaction() {
		final CreateTransactionOperationV1 ct = new CreateTransactionOperationV1(repository);
		return ct.execute(client, context);
	}

	protected void setCommitMessage(final String uuid, final Revision revision, final String message) {
		final Resource resource = Resource.create(config.getCommitMessageResource(uuid) + "/" + revision);
		final CommitMessageOperation cmo = new CommitMessageOperation(repository, resource, message);
		cmo.execute(client, context);
	}

	@Override
	protected void upload0(final Resource resource, final String message, @Nullable final InputStream content, final ResourceProperty... properties) {
		final String uuid = prepareTransaction().getId();
		try {
			final boolean exists = exists(resource, Revision.HEAD);
			final Resource infoResource;
			if (exists) {
				infoResource = resource;
			} else {
				infoResource = createFolder(config.getWorkingResource(uuid).append(resource.getParent()), true);
			}

			final Info info = info(infoResource, Revision.HEAD, false);
			final Revision revision = info.getRevision();
			checkout(uuid);
			setCommitMessage(uuid, revision, message);
			if (exists) {
				prepareContentUpload(resource, uuid, revision);
			}
			contentUpload(resource, info, uuid, content);
			propertiesSet(resource, info, uuid, properties);
			merge(info, uuid);
		} finally {
			endTransaction(uuid);
		}
	}
}
