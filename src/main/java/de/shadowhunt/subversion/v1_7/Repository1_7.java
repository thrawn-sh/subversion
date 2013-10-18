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
package de.shadowhunt.subversion.v1_7;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.annotation.Nullable;

import de.shadowhunt.subversion.AbstractRepository;
import de.shadowhunt.subversion.CommitMessageOperation;
import de.shadowhunt.subversion.CopyOperation;
import de.shadowhunt.subversion.CreateTransactionOperationV2;
import de.shadowhunt.subversion.DeleteOperation;
import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.InfoEntry;
import de.shadowhunt.subversion.MergeOperation;
import de.shadowhunt.subversion.PropertiesDeleteOperation;
import de.shadowhunt.subversion.PropertiesSetOperation;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.UploadOperation;

/**
 * {@link Repository1_7} supports subversion servers of version 1.7.X
 */
public class Repository1_7 extends AbstractRepository {

	protected Repository1_7(final URI repositoryRoot, final boolean trustServerCertificat) {
		super(repositoryRoot, trustServerCertificat);
	}

	protected void contentUpload(final Resource resource, final InfoEntry info, final String uuid, @Nullable final InputStream content) {
		if (content == null) {
			return;
		}

		final Resource r = config.getWorkingResource(uuid).append(resource);
		final UploadOperation uo = new UploadOperation(repository, r, info.getLockToken(), content);
		uo.execute(client, context);
	}

	@Override
	public void copy(final Resource srcResource, final Revision srcRevision, final Resource targetResource, final String message) {
		final InfoEntry info = info(srcResource, srcRevision, false);
		final String uuid = prepareTransaction().getId();
		setCommitMessage(uuid, message);
		createFolder(config.getWorkingResource(uuid).append(targetResource.getParent()), true);
		copy0(srcResource, info.getRevision(), targetResource, uuid);
		merge(info, uuid);
	}

	protected void copy0(final Resource srcResource, final Revision srcRevision, final Resource targetResource, final String uuid) {
		final Resource s = config.getVersionedResource(srcRevision).append(srcResource);
		final Resource t = config.getWorkingResource(uuid).append(targetResource);

		final CopyOperation co = new CopyOperation(repository, s, t);
		co.execute(client, context);
	}

	@Override
	public void createFolder(final Resource resource, final boolean parent, final String message) {
		if (exists(resource, Revision.HEAD)) {
			return;
		}

		final String uuid = prepareTransaction().getId();
		final Resource infoResource = createFolder(config.getWorkingResource(uuid).append(resource), parent);
		final InfoEntry info = info(infoResource, Revision.HEAD, false);
		setCommitMessage(uuid, message);
		merge(info, uuid);
	}

	@Override
	public void delete(final Resource resource, final String message) {
		final String uuid = prepareTransaction().getId();
		setCommitMessage(uuid, message);
		delete0(resource, uuid);
		final InfoEntry info = info(resource, Revision.HEAD, false);
		merge(info, uuid);
	}

	protected void delete0(final Resource resource, final String uuid) {
		final DeleteOperation o = new DeleteOperation(repository, config.getWorkingResource(uuid).append(resource));
		o.execute(client, context);
	}

	@Override
	public void deleteProperties(final Resource resource, final String message, final ResourceProperty... properties) {
		final String uuid = prepareTransaction().getId();
		setCommitMessage(uuid, message);
		final InfoEntry info = info(resource, Revision.HEAD, false);
		propertiesRemove(resource, info, uuid, properties);
		merge(info, uuid);
	}

	@Override
	protected Resource downloadResource(final Resource resource, final Revision revision) {
		if (Revision.HEAD.equals(revision)) {
			return resource;
		}
		return resolve(config.getVersionedResource(revision).append(resource), resource, revision);
	}

	@Override
	public List<InfoEntry> list(final Resource resource, final Revision revision, final Depth depth, final boolean withCustomProperties) {
		final Revision concreateRevision = getConcreteRevision(resource, revision);
		final Resource prefix = config.getVersionedResource(concreateRevision);
		return list(prefix, resource, depth, withCustomProperties);
	}

	protected void merge(final InfoEntry info, final String uuid) {
		final Resource resource = config.getCommitMessageResource(uuid);

		final MergeOperation mo = new MergeOperation(repository, resource, info.getLockToken());
		mo.execute(client, context);
	}

	@Override
	public void move(final Resource srcResource, final Resource targetResource, final String message) {
		final InfoEntry info = info(srcResource, Revision.HEAD, false);
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

	protected void propertiesRemove(final Resource resource, final InfoEntry info, final String uuid, final ResourceProperty... properties) {
		final ResourceProperty[] filtered = ResourceProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final Resource r = config.getWorkingResource(uuid).append(resource);
		final PropertiesDeleteOperation uo = new PropertiesDeleteOperation(repository, r, info.getLockToken(), filtered);
		uo.execute(client, context);
	}

	protected void propertiesSet(final Resource resource, final InfoEntry info, final String uuid, final ResourceProperty... properties) {
		final ResourceProperty[] filtered = ResourceProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final Resource r = config.getWorkingResource(uuid).append(resource);
		final PropertiesSetOperation uo = new PropertiesSetOperation(repository, r, info.getLockToken(), filtered);
		uo.execute(client, context);
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
		final InfoEntry info = info(infoResource, Revision.HEAD, false);
		setCommitMessage(uuid, message);
		contentUpload(resource, info, uuid, content);
		propertiesSet(resource, info, uuid, properties);
		merge(info, uuid);
	}
}
