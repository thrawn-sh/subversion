package de.shadowhunt.scm.subversion;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractSubversionRepositoryTest {

	protected static String BASE;

	protected static final SubversionProperty PROPERTY = SubversionProperty.createCustomProperty("testname", "testvalue");

	protected static SubversionRepository REPO;

	protected String download(final String resource) throws IOException {
		final InputStream download = REPO.download(resource);
		try {
			return IOUtils.toString(download, "UTF-8");
		} finally {
			download.close();
		}
	}

	protected String download(final String resource, final int version) throws IOException {
		final InputStream download = REPO.download(resource, version);
		try {
			return IOUtils.toString(download, "UTF-8");
		} finally {
			download.close();
		}
	}

	@Test
	public void testDelete() throws IOException {
		final String resource = BASE + "/delete.txt";
		upload(resource, "delete");

		Assert.assertTrue("resource does not exist", REPO.exisits(resource));
		REPO.delete(resource, "del");
		Assert.assertFalse("resource does still exist", REPO.exisits(resource));
	}

	@Test
	public void testDeleteNullProperties() throws IOException {
		final String resource = BASE + "/set_properties.txt";
		upload(resource, "properties");

		REPO.setProperties(resource, "set", PROPERTY);
		final SubversionInfo afterCreate = REPO.info(resource, true);
		Assert.assertEquals("property is missing", 1, afterCreate.getCustomProperties().length);
		Assert.assertEquals("property has wrong value", "testvalue", afterCreate.getSubversionPropertyValue("testname"));

		REPO.deleteProperties(resource, "delete", (SubversionProperty) null);
		final SubversionInfo afterDelete = REPO.info(resource, true);
		Assert.assertEquals("property is missing", 1, afterDelete.getCustomProperties().length);
		Assert.assertEquals("property has wrong value", "testvalue", afterDelete.getSubversionPropertyValue("testname"));
	}

	@Test
	public void testDeleteProperties() throws IOException {
		final String resource = BASE + "/delete_properties.txt";
		upload(resource, "properties");

		REPO.setProperties(resource, "set", PROPERTY);
		final SubversionInfo afterCreate = REPO.info(resource, true);
		Assert.assertEquals("property is missing", 1, afterCreate.getCustomProperties().length);
		Assert.assertEquals("property has wrong value", "testvalue", afterCreate.getSubversionPropertyValue("testname"));

		REPO.deleteProperties(resource, "delete", PROPERTY);
		final SubversionInfo afterDelete = REPO.info(resource, true);
		Assert.assertEquals("property still present", 0, afterDelete.getCustomProperties().length);
		Assert.assertNull("property has wrong value", afterDelete.getSubversionPropertyValue("testname"));
	}

	@Test
	public void testDeletePropertiesWithLockedResource() throws IOException {
		final String resource = BASE + "/delete_properties.txt";
		upload(resource, "properties");

		REPO.setProperties(resource, "set", PROPERTY);
		final SubversionInfo afterCreate = REPO.info(resource, true);
		Assert.assertEquals("property is missing", 1, afterCreate.getCustomProperties().length);
		Assert.assertEquals("property has wrong value", "testvalue", afterCreate.getSubversionPropertyValue("testname"));

		REPO.lock(resource);
		final SubversionInfo afterLock = REPO.info(resource, false);
		Assert.assertNotNull("resource is not locked", afterLock.getLockToken());

		REPO.deleteProperties(resource, "delete", PROPERTY);
		final SubversionInfo afterDelete = REPO.info(resource, true);
		Assert.assertNull("resource is locked", afterDelete.getLockToken());
		Assert.assertEquals("property still present", 0, afterDelete.getCustomProperties().length);
		Assert.assertNull("property has wrong value", afterDelete.getSubversionPropertyValue("testname"));
	}

	@Test
	public void testDownload() throws IOException {
		final String resource = BASE + "/download.txt";
		final String expected = "download";
		upload(resource, expected);

		final String actual = download(resource);
		Assert.assertEquals("content differes", expected, actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDownloadIllegalVersion() throws IOException {
		final String resource = BASE + "/download_illegal.txt";
		download(resource, -1);
	}

	@Test
	public void testDownloadVersion() throws IOException {
		final String resource = BASE + "/download_version.txt";
		final String expected = "download";
		upload(resource, expected);

		final SubversionInfo info = REPO.info(resource, false);
		REPO.delete(resource, "del");
		final String actual = download(resource, info.getVersion());
		Assert.assertEquals("content differes", expected, actual);
	}

	@Test
	public void testExistingResource() throws IOException {
		final String resource = BASE + "/existing.txt";
		upload(resource, "exisiting");
		Assert.assertTrue("resource does not exist", REPO.exisits(resource));
	}

	@Test
	public void testInfo() throws IOException {
		final String resource = BASE + "/info.txt";
		upload(resource, "info");

		final SubversionInfo info = REPO.info(resource, false);
		Assert.assertTrue("resource is not a file", info.isFile());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInfoIllegalVersion() {
		final String resource = BASE + "/info_illegal.txt";
		REPO.info(resource, -1, false);
	}

	@Test
	public void testInfoVersion() throws IOException {
		final String resource = BASE + "/info_version.txt";
		upload(resource, "info");

		final SubversionInfo headInfo = REPO.info(resource, false);
		upload(resource, "info2");

		final SubversionInfo versionInfo = REPO.info(resource, headInfo.getVersion(), false);
		Assert.assertEquals("infos differ", headInfo, versionInfo);
	}

	@Test
	public void testListing() throws IOException {
		final String base = BASE + "/listings";
		upload(base + "/l1.txt", "list");
		upload(base + "/l2.txt", "list");
		upload(base + "/l3.txt", "list");

		final List<SubversionInfo> list = REPO.list(base, Depth.IMMEDIATES, false);
		Assert.assertEquals("missing entries in list", 4, list.size());
	}

	@Test
	public void testLocking() throws IOException {
		final String resource = BASE + "/lock.txt";
		upload(resource, "locking");

		final SubversionInfo beforeLock = REPO.info(resource, false);
		Assert.assertNull("resource is locked", beforeLock.getLockToken());

		REPO.lock(resource);
		final SubversionInfo afterLock = REPO.info(resource, false);
		Assert.assertNotNull("resource is not locked", afterLock.getLockToken());

		REPO.unlock(resource, afterLock);
		final SubversionInfo afterUnlock = REPO.info(resource, false);
		Assert.assertNull("resource is locked", afterUnlock.getLockToken());
	}

	@Test
	public void testLog() throws IOException {
		final String resource = BASE + "/log.txt";
		upload(resource, "log1");
		upload(resource, "log2");
		upload(resource, "log3");

		final List<SubversionLog> log = REPO.log(resource);
		Assert.assertEquals("must have 3 log entries", 3, log.size());
	}

	@Test
	public void testLogLast() throws IOException {
		final String resource = BASE + "/loglast.txt";
		upload(resource, "log1");
		upload(resource, "log2");
		upload(resource, "log3");

		Assert.assertNotNull("must have 1 log entries", REPO.lastLog(resource));
	}

	@Test
	public void testNonExistingResource() {
		final String resource = BASE + "/nonexisting.txt";
		Assert.assertFalse("resource already exists", REPO.exisits(resource));
	}

	@Test
	public void testSetNullProperties() throws IOException {
		final String resource = BASE + "/set_null_properties.txt";
		upload(resource, "properties");

		REPO.setProperties(resource, "set", (SubversionProperty) null);
		final SubversionInfo info = REPO.info(resource, true);
		Assert.assertEquals("property was created", 0, info.getCustomProperties().length);
	}

	@Test
	public void testSetProperties() throws IOException {
		final String resource = BASE + "/set_properties.txt";
		upload(resource, "properties");

		REPO.setProperties(resource, "set", PROPERTY);
		final SubversionInfo info = REPO.info(resource, true);
		Assert.assertEquals("property is missing", 1, info.getCustomProperties().length);
		Assert.assertEquals("property has wrong value", "testvalue", info.getSubversionPropertyValue("testname"));
	}

	@Test
	public void testSetPropertiesWithLockedResource() throws IOException {
		final String resource = BASE + "/set_properties_locked.txt";
		upload(resource, "properties");
		REPO.lock(resource);
		final SubversionInfo afterLock = REPO.info(resource, false);
		Assert.assertNotNull("resource is not locked", afterLock.getLockToken());

		REPO.setProperties(resource, "set", PROPERTY);
		final SubversionInfo afterSet = REPO.info(resource, true);
		Assert.assertNull("resource is locked", afterSet.getLockToken());
		Assert.assertEquals("property is missing", 1, afterSet.getCustomProperties().length);
		Assert.assertEquals("property has wrong value", "testvalue", afterSet.getSubversionPropertyValue("testname"));
	}

	@Test
	public void testUnlockOfNotLocked() throws IOException {
		final String resource = BASE + "/unlock.txt";
		upload(resource, "locking");

		final SubversionInfo beforeUnlock = REPO.info(resource, false);
		Assert.assertNull("resource is locked", beforeUnlock.getLockToken());

		REPO.unlock(resource, beforeUnlock);
		final SubversionInfo afterUnlock = REPO.info(resource, false);
		Assert.assertNull("resource is locked", afterUnlock.getLockToken());
	}

	@Test
	public void testUpload() throws IOException {
		final String resource = BASE + "/upload.txt";
		upload(resource, "upload");
	}

	@Test
	public void testUploadWithFolders() throws IOException {
		final String resource = BASE + "/a/b/c/upload.txt";
		upload(resource, "upload");
	}

	@Test
	public void testUploadWithLockedResource() throws IOException {
		final String resource = BASE + "/upload_locked.txt";
		upload(resource, "upload");
		REPO.lock(resource);
		final SubversionInfo afterLock = REPO.info(resource, false);
		Assert.assertNotNull("resource is not locked", afterLock.getLockToken());

		upload(resource, "upload2");
		final SubversionInfo afterUpload = REPO.info(resource, false);
		Assert.assertNull("resource is locked", afterUpload.getLockToken());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUploadWithNullContent() throws IOException {
		final String resource = BASE + "/upload_null.txt";
		upload(resource, null);
	}

	@Test
	public void testUploadWithProperties() throws IOException {
		final String resource = BASE + "/upload.txt";
		uploadWithProperties(resource, "upload", PROPERTY);

		final SubversionInfo info = REPO.info(resource, true);
		Assert.assertEquals("property is missing", 1, info.getCustomProperties().length);
		Assert.assertEquals("property has wrong value", "testvalue", info.getSubversionPropertyValue("testname"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUploadWithPropertiesNullContent() throws IOException {
		final String resource = BASE + "/upload_null.txt";
		uploadWithProperties(resource, null, PROPERTY);
	}

	protected void upload(final String resource, final String content) throws IOException {
		final InputStream is = (content == null) ? null : new ByteArrayInputStream(content.getBytes());
		try {
			REPO.upload(resource, content, is);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	protected void uploadWithProperties(final String resource, final String content, final SubversionProperty... properties) throws IOException {
		final InputStream is = (content == null) ? null : new ByteArrayInputStream(content.getBytes());
		try {
			REPO.uploadWithProperties(resource, content, is, properties);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}
}
