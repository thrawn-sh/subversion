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
package de.shadowhunt.subversion.internal.httpv2;

import de.shadowhunt.subversion.Info;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.annotation.Nullable;

import de.shadowhunt.subversion.internal.AbstractRepository;
import de.shadowhunt.subversion.internal.CommitMessageOperation;
import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction;

/**
 * {@link Repository} supports subversion servers of version 1.7.X
 */
public class Repository extends AbstractRepository {

	protected Repository(final URI repositoryRoot, final boolean trustServerCertificat) {
		super(repositoryRoot, trustServerCertificat);
	}

	@Override
	public void copy(final Resource srcResource, final Revision srcRevision, final Resource targetResource, final String message) {
		final Info info = info(srcResource, srcRevision, false);
		final String uuid = prepareTransaction().getId();
		setCommitMessage(uuid, message);
		createFolder(config.getWorkingResource(uuid).append(targetResource.getParent()), true);
		copy0(srcResource, info.getRevision(), targetResource, uuid);
		merge(info, uuid);
	}

	@Override
	public void createFolder(final Resource resource, final boolean parent, final String message) {
		if (exists(resource, Revision.HEAD)) {
			return;
		}

		final String uuid = prepareTransaction().getId();
		final Resource infoResource = createFolder(config.getWorkingResource(uuid).append(resource), parent);
		final Info info = info(infoResource, Revision.HEAD, false);
		setCommitMessage(uuid, message);
		merge(info, uuid);
	}

	@Override
	public void delete(final Resource resource, final String message) {
		final String uuid = prepareTransaction().getId();
		setCommitMessage(uuid, message);
		delete0(resource, uuid);
		final Info info = info(resource, Revision.HEAD, false);
		merge(info, uuid);
	}

	@Override
	public void deleteProperties(final Resource resource, final String message, final ResourceProperty... properties) {
		final String uuid = prepareTransaction().getId();
		setCommitMessage(uuid, message);
		final Info info = info(resource, Revision.HEAD, false);
		propertiesRemove(resource, info, uuid, properties);
		merge(info, uuid);
	}

	@Override
	public List<Info> list(final Resource resource, final Revision revision, final Depth depth, final boolean withCustomProperties) {
		final Revision concreateRevision = getConcreteRevision(resource, revision);
		final Resource prefix = config.getVersionedResource(concreateRevision);
		return list(prefix, resource, depth, withCustomProperties);
	}

	@Override
	public void move(final Resource srcResource, final Resource targetResource, final String message) {
		final Info info = info(srcResource, Revision.HEAD, false);
		final String uuid = prepareTransaction().getId();
		setCommitMessage(uuid, message);
		copy0(srcResource, info.getRevision(), targetResource, uuid);
		delete0(srcResource, uuid);
		merge(info, uuid);
	}

	protected Transaction prepareTransaction() {
		final CreateTransactionOperationV2 cto = new CreateTransactionOperationV2(repository);
		return cto.execute(client, context);
	}

	protected void setCommitMessage(final String uuid, final String message) {
		final Resource resource = config.getCommitMessageResource(uuid);
		final CommitMessageOperation cmo = new CommitMessageOperation(repository, resource, message);
		cmo.execute(client, context);
	}

	@Override
	protected void upload0(final Resource resource, final String message, @Nullable final InputStream content, @Nullable final ResourceProperty... properties) {
		final String uuid = prepareTransaction().getId();

		final Resource infoResource;
		if (exists(resource, Revision.HEAD)) {
			infoResource = resource;
		} else {
			infoResource = createFolder(config.getWorkingResource(uuid).append(resource.getParent()), true);
		}
		final Info info = info(infoResource, Revision.HEAD, false);
		setCommitMessage(uuid, message);
		contentUpload(resource, info, uuid, content);
		propertiesSet(resource, info, uuid, properties);
		merge(info, uuid);
	}
}
