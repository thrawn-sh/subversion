package de.shadowhunt.scm.subversion;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractSubversionRepositoryIT {

	protected static String BASE;

	protected static final ResourceProperty PROPERTY = ResourceProperty.createCustomProperty("testname", "testvalue");

	protected static Repository REPO;

	protected String retrieveContent(final Path resource, final Revision revision) throws IOException {
		final InputStream download = REPO.download(resource, revision);
		try {
			return IOUtils.toString(download, "UTF-8");
		} finally {
			download.close();
		}
	}

	@Test
	public void testDelete() throws IOException {
		final Path resource = Path.create(BASE + "/delete.txt");
		upload(resource, "delete");

		Assert.assertTrue("resource does not exist", REPO.exists(resource));
		REPO.delete(resource, "del");
		Assert.assertFalse("resource does still exist", REPO.exists(resource));
	}

	@Test
	public void testDeleteNullProperties() throws IOException {
		final Path resource = Path.create(BASE + "/set_properties.txt");
		upload(resource, "properties");

		REPO.setProperties(resource, "set", PROPERTY);
		final InfoEntry afterCreate = REPO.info(resource, Revision.HEAD, true);
		Assert.assertEquals("property is missing", 1, afterCreate.getCustomProperties().length);
		Assert.assertEquals("property has wrong value", "testvalue", afterCreate.getSubversionPropertyValue("testname"));

		REPO.deleteProperties(resource, "delete", (ResourceProperty) null);
		final InfoEntry afterDelete = REPO.info(resource, Revision.HEAD, true);
		Assert.assertEquals("property is missing", 1, afterDelete.getCustomProperties().length);
		Assert.assertEquals("property has wrong value", "testvalue", afterDelete.getSubversionPropertyValue("testname"));
	}

	@Test
	public void testDeleteProperties() throws IOException {
		final Path resource = Path.create(BASE + "/delete_properties.txt");
		upload(resource, "properties");

		REPO.setProperties(resource, "set", PROPERTY);
		final InfoEntry afterCreate = REPO.info(resource, Revision.HEAD, true);
		Assert.assertEquals("property is missing", 1, afterCreate.getCustomProperties().length);
		Assert.assertEquals("property has wrong value", "testvalue", afterCreate.getSubversionPropertyValue("testname"));

		REPO.deleteProperties(resource, "delete", PROPERTY);
		final InfoEntry afterDelete = REPO.info(resource, Revision.HEAD, true);
		Assert.assertEquals("property still present", 0, afterDelete.getCustomProperties().length);
		Assert.assertNull("property has wrong value", afterDelete.getSubversionPropertyValue("testname"));
	}

	@Test
	public void testDeletePropertiesWithLockedResource() throws IOException {
		final Path resource = Path.create(BASE + "/delete_properties.txt");
		upload(resource, "properties");

		REPO.setProperties(resource, "set", PROPERTY);
		final InfoEntry afterCreate = REPO.info(resource, Revision.HEAD, true);
		Assert.assertEquals("property is missing", 1, afterCreate.getCustomProperties().length);
		Assert.assertEquals("property has wrong value", "testvalue", afterCreate.getSubversionPropertyValue("testname"));

		REPO.lock(resource);
		final InfoEntry afterLock = REPO.info(resource, Revision.HEAD, false);
		Assert.assertNotNull("resource is not locked", afterLock.getLockToken());

		REPO.deleteProperties(resource, "delete", PROPERTY);
		final InfoEntry afterDelete = REPO.info(resource, Revision.HEAD, true);
		Assert.assertNull("resource is locked", afterDelete.getLockToken());
		Assert.assertEquals("property still present", 0, afterDelete.getCustomProperties().length);
		Assert.assertNull("property has wrong value", afterDelete.getSubversionPropertyValue("testname"));
	}

	@Test
	public void testDownload() throws IOException {
		final Path resource = Path.create(BASE + "/download.txt");
		final String expected = "download";
		upload(resource, expected);

		final String actual = retrieveContent(resource, Revision.HEAD);
		Assert.assertEquals("content differes", expected, actual);
	}

	@Test
	public void testDownloadVersion() throws IOException {
		final Path resource = Path.create(BASE + "/download_version.txt");
		final String expected = "download";
		upload(resource, expected);

		final InfoEntry info = REPO.info(resource, Revision.HEAD, false);
		REPO.delete(resource, "del");
		final String actual = retrieveContent(resource, info.getRevision());
		Assert.assertEquals("content differes", expected, actual);
	}

	@Test
	public void testExistingResource() throws IOException {
		final Path resource = Path.create(BASE + "/existing.txt");
		upload(resource, "exisiting");
		Assert.assertTrue("resource does not exist", REPO.exists(resource));
	}

	@Test
	public void testInfo() throws IOException {
		final Path resource = Path.create(BASE + "/info.txt");
		upload(resource, "info");

		final InfoEntry info = REPO.info(resource, Revision.HEAD, false);
		Assert.assertTrue("resource is not a file", info.isFile());
	}

	@Test
	public void testInfoVersion() throws IOException {
		final Path resource = Path.create(BASE + "/info_version.txt");
		upload(resource, "info");

		final InfoEntry headInfo = REPO.info(resource, Revision.HEAD, false);
		upload(resource, "info2");

		final InfoEntry versionInfo = REPO.info(resource, headInfo.getRevision(), false);
		Assert.assertEquals("infos differ", headInfo, versionInfo);
	}

	@Test
	public void testListing() throws IOException {
		final Path base = Path.create(BASE + "/listings");
		upload(Path.create(base + "/l1.txt"), "list");
		upload(Path.create(base + "/l2.txt"), "list");
		upload(Path.create(base + "/l3.txt"), "list");

		final List<InfoEntry> list = REPO.list(base, Revision.HEAD, Depth.IMMEDIATES, false);
		Assert.assertEquals("missing entries in list", 4, list.size());
	}

	@Test
	public void testLocking() throws IOException {
		final Path resource = Path.create(BASE + "/lock.txt");
		upload(resource, "locking");

		final InfoEntry beforeLock = REPO.info(resource, Revision.HEAD, false);
		Assert.assertNull("resource is locked", beforeLock.getLockToken());

		REPO.lock(resource);
		final InfoEntry afterLock = REPO.info(resource, Revision.HEAD, false);
		Assert.assertNotNull("resource is not locked", afterLock.getLockToken());

		REPO.unlock(resource);
		final InfoEntry afterUnlock = REPO.info(resource, Revision.HEAD, false);
		Assert.assertNull("resource is locked", afterUnlock.getLockToken());
	}

	@Test
	public void testLog() throws IOException {
		final Path resource = Path.create(BASE + "/log.txt");
		upload(resource, "log1");
		upload(resource, "log2");
		upload(resource, "log3");

		final List<LogEntry> log = REPO.log(resource, Revision.HEAD, Revision.INITIAL);
		Assert.assertEquals("must have 3 log entries", 3, log.size());
	}

	@Test
	public void testLogLast() throws IOException {
		final Path resource = Path.create(BASE + "/loglast.txt");
		upload(resource, "log1");
		upload(resource, "log2");
		upload(resource, "log3");

		Assert.assertNotNull("must have 1 log entries", REPO.lastLog(resource));
	}

	@Test
	public void testNonExistingResource() {
		final Path resource = Path.create(BASE + "/nonexisting.txt");
		Assert.assertFalse("resource already exists", REPO.exists(resource));
	}

	@Test
	public void testSetProperties() throws IOException {
		final Path resource = Path.create(BASE + "/set_properties.txt");
		upload(resource, "properties");

		REPO.setProperties(resource, "set", PROPERTY);
		final InfoEntry info = REPO.info(resource, Revision.HEAD, true);
		Assert.assertEquals("property is missing", 1, info.getCustomProperties().length);
		Assert.assertEquals("property has wrong value", "testvalue", info.getSubversionPropertyValue("testname"));
	}

	@Test
	public void testSetPropertiesWithLockedResource() throws IOException {
		final Path resource = Path.create(BASE + "/set_properties_locked.txt");
		upload(resource, "properties");
		REPO.lock(resource);
		final InfoEntry afterLock = REPO.info(resource, Revision.HEAD, false);
		Assert.assertNotNull("resource is not locked", afterLock.getLockToken());

		REPO.setProperties(resource, "set", PROPERTY);
		final InfoEntry afterSet = REPO.info(resource, Revision.HEAD, true);
		Assert.assertNull("resource is locked", afterSet.getLockToken());
		Assert.assertEquals("property is missing", 1, afterSet.getCustomProperties().length);
		Assert.assertEquals("property has wrong value", "testvalue", afterSet.getSubversionPropertyValue("testname"));
	}

	@Test
	public void testUnlockOfNotLocked() throws IOException {
		final Path resource = Path.create(BASE + "/unlock.txt");
		upload(resource, "locking");

		final InfoEntry beforeUnlock = REPO.info(resource, Revision.HEAD, false);
		Assert.assertNull("resource is locked", beforeUnlock.getLockToken());

		REPO.unlock(resource);
		final InfoEntry afterUnlock = REPO.info(resource, Revision.HEAD, false);
		Assert.assertNull("resource is locked", afterUnlock.getLockToken());
	}

	@Test
	public void testUpload() throws IOException {
		final Path resource = Path.create(BASE + "/upload.txt");
		upload(resource, "upload");
		Assert.assertTrue("upload did not throw any exception", true);
	}

	@Test
	public void testUploadWithFolders() throws IOException {
		final Path resource = Path.create(BASE + "/a/b/c/upload.txt");
		upload(resource, "upload");
		Assert.assertTrue("upload did not throw any exception", true);
	}

	@Test
	public void testUploadWithLockedResource() throws IOException {
		final Path resource = Path.create(BASE + "/upload_locked.txt");
		upload(resource, "upload");
		REPO.lock(resource);
		final InfoEntry afterLock = REPO.info(resource, Revision.HEAD, false);
		Assert.assertNotNull("resource is not locked", afterLock.getLockToken());

		upload(resource, "upload2");
		final InfoEntry afterUpload = REPO.info(resource, Revision.HEAD, false);
		Assert.assertNull("resource is locked", afterUpload.getLockToken());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUploadWithNullContent() throws IOException {
		final Path resource = Path.create(BASE + "/upload_null.txt");
		upload(resource, null);
	}

	@Test
	public void testUploadWithProperties() throws IOException {
		final Path resource = Path.create(BASE + "/upload.txt");
		upload(resource, "upload", PROPERTY);

		final InfoEntry info = REPO.info(resource, Revision.HEAD, true);
		Assert.assertEquals("property is missing", 1, info.getCustomProperties().length);
		Assert.assertEquals("property has wrong value", "testvalue", info.getSubversionPropertyValue("testname"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUploadWithPropertiesNullContent() throws IOException {
		final Path resource = Path.create(BASE + "/upload_null.txt");
		upload(resource, null, PROPERTY);
	}

	protected void upload(final Path resource, final String content, final ResourceProperty... properties) throws IOException {
		final InputStream is = (content == null) ? null : new ByteArrayInputStream(content.getBytes());
		try {
			REPO.uploadWithProperties(resource, content, is, properties);
		} finally {
			if (is != null) {
				IOUtils.closeQuietly(is);
			}
		}
	}
}
