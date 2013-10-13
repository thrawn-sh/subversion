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
package de.shadowhunt.subversion.v1_6;

import de.shadowhunt.subversion.Resource;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

import de.shadowhunt.subversion.AbstractRepository;
import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.InfoEntry;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.util.URIUtils;

/**
 * {@link Repository1_6} supports subversion servers of version 1.6.X
 */
public class Repository1_6 extends AbstractRepository<RequestFactory1_6> {

	protected static final String PREFIX_ACT = "/!svn/act/";

	protected static final String PREFIX_BC = "/!svn/bc/";

	protected static final String PREFIX_VCC = "/!svn/vcc/";

	protected static final String PREFIX_VER = "/!svn/ver/";

	protected static final String PREFIX_WBL = "/!svn/wbl/";

	protected static final String PREFIX_WRK = "/!svn/wrk/";

	protected Repository1_6(final URI repository, final boolean trustServerCertificat) {
		super(repository, trustServerCertificat, new RequestFactory1_6());
	}

	protected void checkout(final UUID uuid) {
		final URI uri = URIUtils.createURI(repository, PREFIX_VCC + "default");
		final URI href = URIUtils.createURI(repository, PREFIX_ACT + uuid);

		final HttpUriRequest request = requestFactory.createCheckoutRequest(uri, href);
		execute(request, HttpStatus.SC_CREATED);
	}

	protected void contentUpload(final Resource resource, final InfoEntry info, final UUID uuid, @Nullable final InputStream content) {
		if (content == null) {
			return;
		}

		final URI uri = URIUtils.createURI(repository, PREFIX_WRK + uuid + resource.getValue());
		final URI resourceUri = URIUtils.createURI(repository, resource.getValue());

		final HttpUriRequest request = requestFactory.createUploadRequest(uri, info.getLockToken(), resourceUri, content);
		execute(request, HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public void copy(final Resource srcResource, final Revision srcRevision, final Resource targetResource, final String message) {
		final UUID uuid = prepareTransaction();
		try {
			final InfoEntry info = info(srcResource, srcRevision, false);
			final Revision realSourceRevision = info.getRevision();
			setCommitMessage(uuid, realSourceRevision, message);
			createMissingFolders(PREFIX_WRK, uuid.toString(), targetResource.getParent());
			copy0(srcResource, realSourceRevision, targetResource, uuid);
			merge(info, uuid);
		} finally {
			endTransaction(uuid);
		}
	}

	protected void copy0(final Resource srcResource, final Revision srcRevision, final Resource targetResource, final UUID uuid) {
		final URI src = URIUtils.createURI(repository, PREFIX_BC + srcRevision + srcResource.getValue());
		final URI target = URIUtils.createURI(repository, PREFIX_WRK + uuid + targetResource.getValue());
		final HttpUriRequest request = requestFactory.createCopyRequest(src, target);
		execute(request, HttpStatus.SC_CREATED);
	}

	@Override
	public void createFolder(final Resource resource, final String message) {
		if (exists(resource, Revision.HEAD)) {
			return;
		}

		final UUID uuid = prepareTransaction();
		try {
			final Resource infoResource = createMissingFolders(PREFIX_WRK, uuid.toString(), resource);
			final InfoEntry info = info(infoResource, Revision.HEAD, false);
			checkout(uuid);
			setCommitMessage(uuid, info.getRevision(), message);
			merge(info, uuid);
		} finally {
			endTransaction(uuid);
		}
	}

	@Override
	public void delete(final Resource resource, final String message) {
		final InfoEntry info = info(resource, Revision.HEAD, false);
		final Revision revision = info.getRevision();

		final UUID uuid = prepareTransaction();
		try {
			checkout(uuid);
			setCommitMessage(uuid, revision, message);
			prepareContentUpload(resource, uuid, revision);
			delete(resource, uuid);
			merge(info, uuid);
		} finally {
			endTransaction(uuid);
		}
	}

	protected void delete(final Resource resource, final UUID uuid) {
		final URI uri = URIUtils.createURI(repository, PREFIX_WRK + uuid + resource.getValue());
		final HttpUriRequest request = requestFactory.createDeleteRequest(uri);
		execute(request, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public void deleteProperties(final Resource resource, final String message, final ResourceProperty... properties) {
		final InfoEntry info = info(resource, Revision.HEAD, false);
		final Revision revision = info.getRevision();

		final UUID uuid = prepareTransaction();
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

	@Override
	public URI downloadURI(final Resource resource, final Revision revision) {
		if (Revision.HEAD.equals(revision)) {
			return URIUtils.createURI(repository, resource.getValue());
		}
		return URIUtils.createURI(repository, PREFIX_BC + revision + resource.getValue());
	}

	protected void endTransaction(final UUID uuid) {
		final URI uri = URIUtils.createURI(repository, PREFIX_ACT + uuid);
		final HttpUriRequest request = requestFactory.createDeleteRequest(uri);
		execute(request, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public List<InfoEntry> list(final Resource resource, final Revision revision, final Depth depth, final boolean withCustomProperties) {
		final Revision concreateRevision = getConcreateRevision(resource, revision);
		final String pathPrefix = PREFIX_BC + concreateRevision;
		return list(pathPrefix, resource, depth, withCustomProperties);
	}

	protected void merge(final InfoEntry info, final UUID uuid) {
		final Resource resource = Resource.create(repository.getPath() + PREFIX_ACT + uuid);
		final HttpUriRequest request = requestFactory.createMergeRequest(repository, resource, info);
		execute(request, HttpStatus.SC_OK);
	}

	@Override
	public void move(final Resource srcResource, final Resource targetResource, final String message) {
		final UUID uuid = prepareTransaction();
		try {
			final InfoEntry info = info(srcResource, Revision.HEAD, false);
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

	protected void prepareContentUpload(final Resource resource, final UUID uuid, final Revision revision) {
		final URI uri = URIUtils.createURI(repository, PREFIX_VER + revision + resource.getValue());
		final URI href = URIUtils.createURI(repository, PREFIX_ACT + uuid);

		final HttpUriRequest request = requestFactory.createCheckoutRequest(uri, href);
		execute(request, HttpStatus.SC_CREATED);
	}

	protected UUID prepareTransaction() {
		final UUID uuid = UUID.randomUUID();
		final URI uri = URIUtils.createURI(repository, PREFIX_ACT + uuid);

		final HttpUriRequest request = requestFactory.createActivityRequest(uri);
		execute(request, HttpStatus.SC_CREATED);
		return uuid;
	}

	protected void propertiesRemove(final Resource resource, final InfoEntry info, final UUID uuid, final ResourceProperty... properties) {
		final ResourceProperty[] filtered = ResourceProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URIUtils.createURI(repository, PREFIX_WRK + uuid + resource.getValue());
		final URI resourceUri = downloadURI(resource, Revision.HEAD);

		final HttpUriRequest request = requestFactory.createRemovePropertiesRequest(uri, info.getLockToken(), resourceUri, filtered);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	protected void propertiesSet(final Resource resource, final InfoEntry info, final UUID uuid, final ResourceProperty... properties) {
		final ResourceProperty[] filtered = ResourceProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URIUtils.createURI(repository, PREFIX_WRK + uuid + resource.getValue());
		final URI resourceUri = URIUtils.createURI(repository, resource.getValue());

		final HttpUriRequest request = requestFactory.createSetPropertiesRequest(uri, info.getLockToken(), resourceUri, filtered);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	protected void setCommitMessage(final UUID uuid, final Revision revision, final String message) {
		final URI uri = URIUtils.createURI(repository, PREFIX_WBL + uuid + "/" + revision);

		final String trimmedMessage = StringUtils.trimToEmpty(message);
		final HttpUriRequest request = requestFactory.createCommitMessageRequest(uri, trimmedMessage);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	@Override
	protected void upload0(final Resource resource, final String message, @Nullable final InputStream content, final ResourceProperty... properties) {
		final UUID uuid = prepareTransaction();
		try {
			final boolean exists = exists(resource, Revision.HEAD);
			final Resource infoResource;
			if (exists) {
				infoResource = resource;
			} else {
				infoResource = createMissingFolders(PREFIX_WRK, uuid.toString(), resource.getParent());
			}

			final InfoEntry info = info(infoResource, Revision.HEAD, false);
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
