package de.shadowhunt.scm.subversion;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractSubversionRepositoryTest {

	protected static String BASE;

	protected static SubversionRepository REPO;

	@Test
	public void testDownload() throws IOException {
		//		System.out.println("download");
		//		final InputStream download = sr.download(resource);
		//		final OutputStream output = new FileOutputStream("download-" + now);
		//		//		IOUtils.(download, output);
		//		download.close();
		//		output.close();
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
	}

	public void testListing() {
		//		System.out.println("list");
		//		sr.list("/", 1, false);

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
	public void testNonExistingResource() {
		final String resource = BASE + "/nonexisting.txt";
		Assert.assertFalse("resource already exists", REPO.exisits(resource));
	}

	@Test
	public void testResourceLocking() throws IOException {
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
	public void testUpload() throws IOException {
		final String resource = BASE + "/upload.txt";
		upload(resource, "upload");
	}

	@Test
	public void testUploadWithProperties() throws IOException {
		final String resource = BASE + "/upload.txt";
		upload(resource, "upload", SubversionProperty.createCustomProperty("testname", "testvalue"));

		final SubversionInfo info = REPO.info(resource, true);
		Assert.assertEquals("property is missing", 1, info.getCustomProperties().length);
		Assert.assertEquals("property has wrong value", "testvalue", info.getSubversionPropertyValue("testname"));
	}

	protected void upload(final String resource, final String content) throws IOException {
		final InputStream is = new ByteArrayInputStream(content.getBytes());
		try {
			REPO.upload(resource, content, is);
		} finally {
			is.close();
		}
	}

	protected void upload(final String resource, final String content, final SubversionProperty... properties) throws IOException {
		final InputStream is = new ByteArrayInputStream(content.getBytes());
		try {
			REPO.uploadWithProperties(resource, content, is, properties);
		} finally {
			is.close();
		}
	}

	//
	//	System.out.println("upload");
	//	final String message = "v" + now + "\n";
	//	final InputStream content = new ByteArrayInputStream(message.getBytes());
	//	sr.upload(resource, message, content);
	//
	//	System.out.println("set property");
	//	sr.setProperties(resource, "setting property", SubversionProperty.createCustomProperty("ade2", "hero"));
	//
	//	final SubversionInfo infoWithCustomPropertiesBefore = sr.info(resource, true);
	//	System.out.println(Arrays.toString(infoWithCustomPropertiesBefore.getCustomProperties()));
	//
	//	System.out.println("delete property");
	//	sr.deleteProperties(resource, "delete properties", infoWithCustomPropertiesBefore.getCustomProperties());
	//
	//	final SubversionInfo infoWithCustomPropertiesAfter = sr.info(resource, true);
	//	System.out.println(Arrays.toString(infoWithCustomPropertiesAfter.getCustomProperties()));
	//
	//	System.out.println("upload after lock");
	//	sr.lock(resource);
	//	final String message2 = "aaa" + now + "\n";
	//	final InputStream content2 = new ByteArrayInputStream(message.getBytes());
	//	sr.upload(resource, message2, content2);
	//
	//	System.out.println("delete");
	//	sr.delete(resource, "delete");
}
