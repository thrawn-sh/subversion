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

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Log;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.RepositoryFactory;
import de.shadowhunt.subversion.RepositoryUtils;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.ResourceProperty.Type;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Transaction;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public abstract class AbstractRepositoryReadOnlyIT {

	protected static final Resource EXISTING_DIR_WITH_DIRS = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/folder_with_dirs");

	protected static final Resource EXISTING_DIR_WITH_FILES = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/folder_with_files");

	protected static final Resource EXISTING_EMPTY_DIR = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/folder_empty");

	protected static final Resource EXISTING_FILE = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/revisions.txt");

	protected static final Resource EXISTING_MIXED_DIR = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/folder_mixed");

	protected static final Resource EXISTING_MULTIPLE_PROPERTIES = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/folder_mixed/a/file.txt");

	protected static final Resource EXISTING_PROPERTY_VERSION = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/folder_mixed/file.txt");

	protected static final Resource NON_EXISTING = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/no_Existing.txt");

	protected final Repository repository;

	protected AbstractRepositoryReadOnlyIT(URI uri) {
		this.repository = createRepository(uri, createClient(), createContext());
	}

	HttpContext createContext() {
		return new BasicHttpContext();
	}

	HttpClient createClient() {
		return new DefaultHttpClient();
	}

	Repository createRepository(final URI uri, final HttpClient client, final HttpContext context) {
		return RepositoryFactory.createRepository(uri, client, context);
	}

	@Test
	public void downloadExistingDir() {
		final InputStream download = repository.download(EXISTING_EMPTY_DIR, Revision.HEAD);
		Assert.assertNotNull("InputStream must not be null", download);
		// returns HTML showing the directory content
		IOUtils.closeQuietly(download);
	}

	@Test
	public void downloadExistingFileHeadRevision() throws IOException {
		final InputStream download = repository.download(EXISTING_FILE, Revision.HEAD);
		Assert.assertNotNull("InputStream must not be null", download);

		final String content = RepositoryUtils.retrieveContent(download);
		Assert.assertEquals("content must match", "Test 9", content);
	}

	@Test
	public void downloadExistingFileRevision() throws IOException {
		final InputStream download = repository.download(EXISTING_FILE, Revision.create(20));
		Assert.assertNotNull("InputStream must not be null", download);

		final String content = RepositoryUtils.retrieveContent(download);
		Assert.assertEquals("content must match", "Test 4", content);
	}

	@Test
	public void existsExistingDir() {
		Assert.assertTrue("folder exists in expected revision", repository.exists(EXISTING_EMPTY_DIR, Revision.HEAD));
		Assert.assertFalse("folder didn't exists in revision 1", repository.exists(EXISTING_EMPTY_DIR, Revision.INITIAL));
	}

	@Test
	public void existsExistingFile() {
		Assert.assertTrue("file exists in expected revision", repository.exists(EXISTING_FILE, Revision.HEAD));
		Assert.assertFalse("file didn't exists in revision 1", repository.exists(EXISTING_FILE, Revision.INITIAL));
	}

	@Test
	public void existsNonExistingFile() {
		Assert.assertFalse("file exists in expected revision", repository.exists(NON_EXISTING, Revision.HEAD));
		Assert.assertFalse("file didn't exists in revision 1", repository.exists(NON_EXISTING, Revision.INITIAL));
	}

	@Test
	public void existsNonExistingResource() {
		Assert.assertFalse("revisions file doesn't exists in expected revision", repository.exists(NON_EXISTING, Revision.HEAD));
		Assert.assertFalse("revisions file didn't exists in revision 1", repository.exists(NON_EXISTING, Revision.INITIAL));
	}

	@Test
	public void infoExistingDirHeadRevision() {
		final Info info = repository.info(EXISTING_EMPTY_DIR, Revision.HEAD, true);
		Assert.assertNotNull("result must not be null", info);

		Assert.assertFalse("resource is not locked", info.isLocked());
		Assert.assertNull("not locked => no lock owner", info.getLockOwner());
		Assert.assertNull("not locked => no lock token", info.getLockToken());
		Assert.assertNull("folder don't have md5", info.getMd5());
		Assert.assertEquals("resource are equal", EXISTING_EMPTY_DIR, info.getResource());
		Assert.assertNotNull("repository uuid mut not be null", info.getRepositoryUuid());
		Assert.assertEquals("last cahnged at", Revision.create(2), info.getRevision());
		Assert.assertFalse("resource is not a file", info.isFile());
		Assert.assertTrue("resource is a directory", info.isDirectory());

		final ResourceProperty[] customProperties = info.getCustomProperties();
		Assert.assertEquals("no custom properties", 0, customProperties.length);
	}

	@Test
	public void infoExistingDirRevision() {
		final Revision revision = Revision.create(2);
		final Info info = repository.info(EXISTING_EMPTY_DIR, revision, true);
		Assert.assertNotNull("result must not be null", info);

		Assert.assertFalse("resource is not locked", info.isLocked());
		Assert.assertNull("not locked => no lock owner", info.getLockOwner());
		Assert.assertNull("not locked => no lock token", info.getLockToken());
		Assert.assertNull("folder don't have md5", info.getMd5());
		Assert.assertEquals("resource are equal", EXISTING_EMPTY_DIR, info.getResource());
		Assert.assertNotNull("repository uuid mut not be null", info.getRepositoryUuid());
		Assert.assertEquals("last changed at", revision, info.getRevision());
		Assert.assertFalse("resource is not a file", info.isFile());
		Assert.assertTrue("resource is a directory", info.isDirectory());

		final ResourceProperty[] customProperties = info.getCustomProperties();
		Assert.assertEquals("no custom properties", 0, customProperties.length);
	}

	@Test
	public void infoExistingFileHeadRevision() {
		final Info info = repository.info(EXISTING_FILE, Revision.HEAD, true);
		Assert.assertNotNull("result must not be null", info);

		Assert.assertFalse("resource is not locked", info.isLocked());
		Assert.assertNull("not locked => no lock owner", info.getLockOwner());
		Assert.assertNull("not locked => no lock token", info.getLockToken());
		Assert.assertEquals("is expected file", "c9aa593b08050501075f060ba1708196", info.getMd5());
		Assert.assertEquals("resource are equal", EXISTING_FILE, info.getResource());
		Assert.assertNotNull("repository uuid mut not be null", info.getRepositoryUuid());
		Assert.assertEquals("last changed at", Revision.create(30), info.getRevision());
		Assert.assertTrue("resource is a file", info.isFile());
		Assert.assertFalse("resource is not a directory", info.isDirectory());

		final ResourceProperty[] customProperties = info.getCustomProperties();
		Assert.assertEquals("no custom properties", 0, customProperties.length);
	}

	@Test
	public void infoExistingFileRevision() {
		final Revision revision = Revision.create(20);
		final Info info = repository.info(EXISTING_FILE, revision, true);
		Assert.assertNotNull("result must not be null", info);

		Assert.assertFalse("resource is not locked", info.isLocked());
		Assert.assertNull("not locked => no lock owner", info.getLockOwner());
		Assert.assertNull("not locked => no lock token", info.getLockToken());
		Assert.assertEquals("is expected file", "5ae6c6c33fea175753939e037efeb751", info.getMd5());
		Assert.assertEquals("resource are equal", EXISTING_FILE, info.getResource());
		Assert.assertNotNull("repository uuid mut not be null", info.getRepositoryUuid());
		Assert.assertEquals("last changed at", revision, info.getRevision());
		Assert.assertTrue("resource is a file", info.isFile());
		Assert.assertFalse("resource is not a directory", info.isDirectory());
	}

	@Test
	public void infoMultipleProperties() {
		final Info info = repository.info(EXISTING_MULTIPLE_PROPERTIES, Revision.HEAD, true);
		Assert.assertNotNull("result must not be null", info);

		final ResourceProperty[] properties = info.getCustomProperties();
		Assert.assertEquals("custom properties", 5, properties.length);
		for (final ResourceProperty property : properties) {
			final char count = property.getName().charAt(10);
			Assert.assertEquals("type is always custom", ResourceProperty.Type.CUSTOM, property.getType());
			Assert.assertEquals("value is expected", "value " + count, property.getValue());
		}
	}

	@Test
	public void infoMultiplePropertiesRevision() {
		final Info info = repository.info(EXISTING_MULTIPLE_PROPERTIES, Revision.create(11), true);
		Assert.assertNotNull("result must not be null", info);

		final ResourceProperty[] properties = info.getCustomProperties();
		Assert.assertEquals("custom properties", 3, properties.length);
		for (final ResourceProperty property : properties) {
			final char count = property.getName().charAt(10);
			Assert.assertEquals("type is always custom", ResourceProperty.Type.CUSTOM, property.getType());
			Assert.assertEquals("value is expected", "value " + count, property.getValue());
		}
	}

	@Test(expected = SubversionException.class)
	public void infoNonExistingResource() {
		repository.info(NON_EXISTING, Revision.HEAD, true);
		Assert.fail("no info for non Existing resource must be created");
	}

	@Test
	public void infoPropertyIgnore() {
		final Info info = repository.info(EXISTING_PROPERTY_VERSION, Revision.HEAD, false);
		Assert.assertNotNull("result must not be null", info);

		final ResourceProperty[] properties = info.getCustomProperties();
		Assert.assertEquals("custom properties where not retrieved", 0, properties.length);
	}

	@Test
	public void infoPropertyVersions() {
		final Info info = repository.info(EXISTING_PROPERTY_VERSION, Revision.HEAD, true);
		Assert.assertNotNull("result must not be null", info);

		final ResourceProperty[] properties = info.getCustomProperties();
		Assert.assertEquals("custom properties", 1, properties.length);

		Assert.assertEquals("property value must match", "value 5", info.getResourcePropertyValue("myProperty"));
	}

	@Test
	public void infoPropertyVersionsRevision() {
		final Info info = repository.info(EXISTING_PROPERTY_VERSION, Revision.create(6), true);
		Assert.assertNotNull("result must not be null", info);

		final ResourceProperty[] properties = info.getCustomProperties();
		Assert.assertEquals("custom properties", 1, properties.length);

		Assert.assertEquals("property value must match", "value 3", info.getResourcePropertyValue("myProperty"));
	}

	@Test
	public void lastLogExistingDir() {
		final Log log = repository.lastLog(EXISTING_EMPTY_DIR);
		Assert.assertNotNull("Log must not be null", log);

		Assert.assertEquals("comment must match", "create test structure", log.getMessage());
		Assert.assertNotNull("Date must not be null", log.getDate());
		Assert.assertEquals("revision must match", Revision.create(2), log.getRevision());
		Assert.assertEquals("user must match", "root", log.getUser());
	}

	@Test
	public void lastLogExistingFile() {
		final Log log = repository.lastLog(EXISTING_FILE);
		Assert.assertNotNull("Log must not be null", log);

		Assert.assertEquals("comment must match", "adding revisions file 9", log.getMessage());
		Assert.assertNotNull("Date must not be null", log.getDate());
		Assert.assertEquals("revision must match", Revision.create(30), log.getRevision());
		Assert.assertEquals("user must match", "root", log.getUser());
	}

	@Test(expected = SubversionException.class)
	public void lastLogNonExistingResource() {
		repository.lastLog(NON_EXISTING);
		Assert.fail("no log for non Existing resource must be created");
	}

	@Test
	public void listExistingDirWithDirs() {
		final List<Info> listEmpty = repository.list(EXISTING_DIR_WITH_DIRS, Revision.HEAD, Depth.EMPTY, false);
		Assert.assertNotNull("Info must not be null", listEmpty);
		Assert.assertEquals("number of Log must match", 1, listEmpty.size());

		final List<Info> listFiles = repository.list(EXISTING_DIR_WITH_DIRS, Revision.HEAD, Depth.FILES, false);
		Assert.assertNotNull("Info must not be null", listFiles);
		Assert.assertTrue("number of Log must match", listFiles.isEmpty());

		final List<Info> immediateResources = repository.list(EXISTING_DIR_WITH_DIRS, Revision.HEAD, Depth.IMMEDIATES, false);
		Assert.assertNotNull("Info must not be null", immediateResources);
		Assert.assertEquals("number of Log must match", 5, immediateResources.size());

		final List<Info> listInfinity = repository.list(EXISTING_DIR_WITH_DIRS, Revision.HEAD, Depth.INFINITY, false);
		Assert.assertNotNull("Info must not be null", listInfinity);
		Assert.assertEquals("number of Log must match", 5, listInfinity.size());
	}

	@Test
	public void listExistingDirWithFiles() {
		final List<Info> listEmpty = repository.list(EXISTING_DIR_WITH_FILES, Revision.HEAD, Depth.EMPTY, false);
		Assert.assertNotNull("Info must not be null", listEmpty);
		Assert.assertEquals("number of Log must match", 1, listEmpty.size());

		final List<Info> listFiles = repository.list(EXISTING_DIR_WITH_FILES, Revision.HEAD, Depth.FILES, false);
		Assert.assertNotNull("Info must not be null", listFiles);
		Assert.assertEquals("number of Log must match", 4, listFiles.size());

		final List<Info> immediateResources = repository.list(EXISTING_DIR_WITH_FILES, Revision.HEAD, Depth.IMMEDIATES, false);
		Assert.assertNotNull("Info must not be null", immediateResources);
		Assert.assertEquals("number of Log must match", 5, immediateResources.size());

		final List<Info> listInfinity = repository.list(EXISTING_DIR_WITH_FILES, Revision.HEAD, Depth.INFINITY, false);
		Assert.assertNotNull("Info must not be null", listInfinity);
		Assert.assertEquals("number of Log must match", 5, listInfinity.size());
	}

	@Test
	public void listExistingEmptyDir() {
		final List<Info> listEmpty = repository.list(EXISTING_EMPTY_DIR, Revision.HEAD, Depth.EMPTY, false);
		Assert.assertNotNull("Info must not be null", listEmpty);
		Assert.assertEquals("number of Log must match", 1, listEmpty.size());

		final List<Info> listFiles = repository.list(EXISTING_EMPTY_DIR, Revision.HEAD, Depth.FILES, false);
		Assert.assertNotNull("Info must not be null", listFiles);
		Assert.assertTrue("number of Log must match", listFiles.isEmpty());

		final List<Info> immediateResources = repository.list(EXISTING_EMPTY_DIR, Revision.HEAD, Depth.IMMEDIATES, false);
		Assert.assertNotNull("Info must not be null", immediateResources);
		Assert.assertEquals("number of Log must match", 1, immediateResources.size());

		final List<Info> listInfinity = repository.list(EXISTING_EMPTY_DIR, Revision.HEAD, Depth.INFINITY, false);
		Assert.assertNotNull("Info must not be null", listInfinity);
		Assert.assertEquals("number of Log must match", 1, listInfinity.size());
	}

	@Test
	public void listExistingFile() {
		final List<Info> listEmpty = repository.list(EXISTING_FILE, Revision.HEAD, Depth.EMPTY, false);
		Assert.assertNotNull("Info must not be null", listEmpty);
		Assert.assertEquals("number of Log must match", 1, listEmpty.size());

		final List<Info> listFiles = repository.list(EXISTING_FILE, Revision.HEAD, Depth.FILES, false);
		Assert.assertNotNull("Info must not be null", listFiles);
		Assert.assertEquals("number of Log must match", 1, listFiles.size());

		final List<Info> immediateResources = repository.list(EXISTING_FILE, Revision.HEAD, Depth.IMMEDIATES, false);
		Assert.assertNotNull("Info must not be null", immediateResources);
		Assert.assertEquals("number of Log must match", 1, immediateResources.size());

		final List<Info> listInfinity = repository.list(EXISTING_FILE, Revision.HEAD, Depth.INFINITY, false);
		Assert.assertNotNull("Info must not be null", listInfinity);
		Assert.assertEquals("number of Log must match", 1, listInfinity.size());
	}

	@Test
	public void listExistingMixedDir() {
		final List<Info> listEmpty = repository.list(EXISTING_MIXED_DIR, Revision.HEAD, Depth.EMPTY, false);
		Assert.assertNotNull("Info must not be null", listEmpty);
		Assert.assertEquals("number of Log must match", 1, listEmpty.size());

		final List<Info> listFiles = repository.list(EXISTING_MIXED_DIR, Revision.HEAD, Depth.FILES, false);
		Assert.assertNotNull("Info must not be null", listFiles);
		Assert.assertEquals("number of Log must match", 1, listFiles.size());

		final List<Info> immediateResources = repository.list(EXISTING_MIXED_DIR, Revision.HEAD, Depth.IMMEDIATES, false);
		Assert.assertNotNull("Info must not be null", immediateResources);
		Assert.assertEquals("number of Log must match", 3, immediateResources.size());

		final List<Info> listInfinity = repository.list(EXISTING_MIXED_DIR, Revision.HEAD, Depth.INFINITY, false);
		Assert.assertNotNull("Info must not be null", listInfinity);
		Assert.assertEquals("number of Log must match", 6, listInfinity.size());
	}

	@Test(expected = SubversionException.class)
	public void listNonExistingResource() {
		repository.list(NON_EXISTING, Revision.HEAD, Depth.EMPTY, false);
		Assert.fail("no listings for non Existing resource must be created");
	}

	@Test
	public void logExistingFileAscending() {
		final List<Log> logs = repository.log(EXISTING_FILE, Revision.INITIAL, Revision.HEAD, 0);
		Assert.assertNotNull("Log must not be null", logs);
		Assert.assertEquals("number of Log must match", 9, logs.size());

		Revision last = Revision.INITIAL;
		for (int i = 0; i < 9; i++) {
			final Log entry = logs.get(i);

			Assert.assertEquals("comment must match", "adding revisions file " + (i + 1), entry.getMessage());
			Assert.assertNotNull("Date must not be null", entry.getDate());
			final Revision revision = entry.getRevision();
			Assert.assertTrue(last + " must be smaller than " + revision, (last.compareTo(revision) < 0));
			last = revision;
			Assert.assertEquals("user must match", "root", entry.getUser());
		}
	}

	@Test
	public void logExistingFileDescending() {
		final List<Log> logs = repository.log(EXISTING_FILE, Revision.HEAD, Revision.INITIAL, 0);
		Assert.assertNotNull("Log must not be null", logs);
		Assert.assertEquals("number of Log must match", 9, logs.size());

		Revision last = Revision.create(99);
		for (int i = 0; i < 9; i++) {
			final Log entry = logs.get(i);

			Assert.assertEquals("comment must match", "adding revisions file " + (9 - i), entry.getMessage());
			Assert.assertNotNull("Date must not be null", entry.getDate());
			final Revision revision = entry.getRevision();
			Assert.assertTrue(last + " must be smaller than " + revision, (last.compareTo(revision) > 0));
			last = revision;
			Assert.assertEquals("user must match", "root", entry.getUser());
		}
	}

	@Test(expected = SubversionException.class)
	public void logNonExistingResource() {
		repository.log(NON_EXISTING, Revision.INITIAL, Revision.HEAD, 0);
		Assert.fail("no logs for non existing resource must be created");
	}

	@Ignore
	@Test
	public void removeEmptyResourceProperties() {
		final Info before = repository.info(EXISTING_PROPERTY_VERSION, Revision.HEAD, true);
		Assert.assertNotNull("result must not be null", before);

		final ResourceProperty[] beforeProperties = before.getCustomProperties();
		Assert.assertEquals("custom properties", 1, beforeProperties.length);

		Transaction transaction = repository.createTransaction();
		try {
			repository.deleteProperties(transaction, EXISTING_PROPERTY_VERSION, new ResourceProperty[0]);
			repository.commit(transaction, "remove properties");
		} catch (SubversionException se) {
			repository.rollback(transaction);
			throw se;
		}
		final Info after = repository.info(EXISTING_PROPERTY_VERSION, Revision.HEAD, true);
		Assert.assertNotNull("result must not be null", after);

		final ResourceProperty[] afterProperties = after.getCustomProperties();
		Assert.assertEquals("custom properties", 1, afterProperties.length);
	}

	@Ignore
	@Test
	public void removeSystemResourceProperties() {
		final Info before = repository.info(EXISTING_PROPERTY_VERSION, Revision.HEAD, true);
		Assert.assertNotNull("result must not be null", before);

		final ResourceProperty[] beforeProperties = before.getCustomProperties();
		Assert.assertEquals("custom properties", 1, beforeProperties.length);

		final ResourceProperty baseProperty = new ResourceProperty(Type.BASE, "base", "base");
		final ResourceProperty davProperty = new ResourceProperty(Type.DAV, "dav", "dav");
		final ResourceProperty svnProperty = new ResourceProperty(Type.SVN, "svn", "svn");
		Transaction transaction = repository.createTransaction();
		try {
			repository.deleteProperties(transaction, EXISTING_PROPERTY_VERSION, baseProperty, davProperty, svnProperty);
			repository.commit(transaction, "remove properties");
		} catch (SubversionException se) {
			repository.rollback(transaction);
			throw se;
		}

		final Info after = repository.info(EXISTING_PROPERTY_VERSION, Revision.HEAD, true);
		Assert.assertNotNull("result must not be null", after);

		final ResourceProperty[] afterProperties = after.getCustomProperties();
		Assert.assertEquals("custom properties", 1, afterProperties.length);
	}
}
