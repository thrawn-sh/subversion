package de.shadowhunt.subversion;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class RepositoryWriteIT extends RepositoryAuthenticatedIT {

	private static String BASE = "/trunk/" + UUID.randomUUID().toString();

	private static void assertLastLog(final Path resource, final String message, final String user) {
		final LogEntry logEntry = repository.lastLog(resource);
		Assert.assertNotNull("LogEntry must not be null", logEntry);
		Assert.assertEquals("message must match", message, logEntry.getComment());
		Assert.assertEquals("user must match", user, logEntry.getUser());
	}

	private static void assertProperties(final Path resource, final ResourceProperty... properties) {
		final InfoEntry info = repository.info(resource, Revision.HEAD, true);
		Assert.assertNotNull("InfoEntry must not be null", info);

		final ResourceProperty[] customProperties = info.getCustomProperties();
		Assert.assertEquals("custom properties", properties.length, customProperties.length);

		Arrays.sort(properties, ResourceProperty.NAME_COMPARATOR);
		Arrays.sort(customProperties, ResourceProperty.NAME_COMPARATOR);
		for (int i = 0; i < customProperties.length; i++) {
			Assert.assertEquals("property must match", properties[i], customProperties[i]);
		}
	}

	private static void assertUpload(final Path resource, final String content) throws IOException {
		Assert.assertTrue("created resource must exist", repository.exists(resource, Revision.HEAD));

		final InputStream download = repository.download(resource, Revision.HEAD);
		Assert.assertNotNull("InputStream must not be null", download);
		Assert.assertEquals("content must match", content, retrieveContent(download));
		IOUtils.closeQuietly(download);
	}

	@Test
	public void copyFile() throws IOException {
		final Path src = Path.create(BASE + "/copySrcFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(src, Revision.HEAD));

		final Path target = Path.create(BASE + "/copyTargetFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(target, Revision.HEAD));

		final String content = "content";
		final String messageUpload = "create file";

		repository.upload(src, messageUpload, IOUtils.toInputStream(content));
		assertUpload(src, content);
		assertLastLog(src, messageUpload, getUsername());

		final String messageCopy = "copy file";

		repository.copy(src, Revision.HEAD, target, messageCopy);
		assertUpload(target, content);
		assertLastLog(target, messageCopy, getUsername());
	}

	@Test
	public void createFolder() {
		final Path folder = Path.create(BASE + "/createFolder");
		Assert.assertFalse("new folder must not exist", repository.exists(folder, Revision.HEAD));
		final String message = "create folder";

		repository.createFolder(folder, message);
		Assert.assertTrue("created folder must exist", repository.exists(folder, Revision.HEAD));
		assertLastLog(folder, message, getUsername());
	}

	@Test
	public void deleteFile() throws IOException {
		final Path file = Path.create(BASE + "/deleteFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "content";
		final String message = "create file";
		repository.upload(file, message, IOUtils.toInputStream(content));
		assertUpload(file, content);
		assertLastLog(file, message, getUsername());

		repository.delete(file, "delete folder");
		Assert.assertFalse("non exisitng file must not exist", repository.exists(file, Revision.HEAD));
	}

	@Test
	public void deleteFolder() {
		final Path folder = Path.create(BASE + "/deleteFolder");

		repository.createFolder(folder, "create folder");
		Assert.assertTrue("created folder must exist", repository.exists(folder, Revision.HEAD));

		repository.delete(folder, "delete folder");
		Assert.assertFalse("deleted folder must not exist", repository.exists(folder, Revision.HEAD));
	}

	@Test(expected = SubversionException.class)
	public void deleteNonExistingFolder() {
		final Path folder = Path.create(BASE + "/nonExistingFolder");
		Assert.assertFalse("non exisitng folder must not exist", repository.exists(folder, Revision.HEAD));

		repository.delete(folder, "delete folder");
		Assert.assertFalse("non exisitng folder must not exist", repository.exists(folder, Revision.HEAD));
	}

	@Test
	public void deleteProperties() throws IOException {
		final Path file = Path.create(BASE + "/deletePropertiesFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "content";
		final String messageUpload = "create file";

		repository.upload(file, messageUpload, IOUtils.toInputStream(content));
		assertUpload(file, content);
		assertProperties(file);
		assertLastLog(file, messageUpload, getUsername());

		final String messageProperties = "set properties";
		final ResourceProperty a = ResourceProperty.createCustomProperty("a", "A");
		final ResourceProperty b = ResourceProperty.createCustomProperty("b", "B");

		repository.setProperties(file, messageProperties, a, b);
		assertProperties(file, a, b);
		assertLastLog(file, messageProperties, getUsername());

		final String messageDelete = "delete properties";

		repository.deleteProperties(file, messageDelete, a, b);
		assertProperties(file);
		assertLastLog(file, messageDelete, getUsername());
	}

	@Test
	@Ignore("see exisitngFolder")
	public void doublecreateFolder() {
		final Path folder = Path.create(BASE + "/doublecreateFolder");
		Assert.assertFalse("new folder must not exist", repository.exists(folder, Revision.HEAD));

		repository.createFolder(folder, "create folder");
		Assert.assertTrue("created folder must exist", repository.exists(folder, Revision.HEAD));

		repository.createFolder(folder, "create folder");
		Assert.assertTrue("created folder must exist", repository.exists(folder, Revision.HEAD));
	}

	@Test
	public void moveFile() throws IOException {
		final Path src = Path.create(BASE + "/moveSrcFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(src, Revision.HEAD));

		final Path target = Path.create(BASE + "/moveTargetFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(target, Revision.HEAD));

		final String content = "content";
		final String messageUpload = "create file";

		repository.upload(src, messageUpload, IOUtils.toInputStream(content));
		assertUpload(src, content);
		assertLastLog(src, messageUpload, getUsername());

		final String messageMove = "copy file";

		repository.move(src, target, messageMove);
		assertUpload(target, content);
		assertLastLog(target, messageMove, getUsername());
		Assert.assertFalse("src file must not exist", repository.exists(src, Revision.HEAD));
	}

	@Test
	public void setProperties() throws IOException {
		final Path file = Path.create(BASE + "/setPropertiesFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "content";
		final String messageUpload = "create file";
		repository.upload(file, messageUpload, IOUtils.toInputStream(content));
		assertUpload(file, content);
		assertProperties(file);
		assertLastLog(file, messageUpload, getUsername());

		final String messageProperties = "set properties";
		final ResourceProperty a = ResourceProperty.createCustomProperty("a", "A");
		final ResourceProperty b = ResourceProperty.createCustomProperty("b", "B");

		repository.setProperties(file, messageProperties, a, b);
		assertProperties(file, a, b);
		assertLastLog(file, messageProperties, getUsername());
	}

	@Test
	public void uploadFile() throws IOException {
		final Path file = Path.create(BASE + "/uploadFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "content";
		final String message = "create file";

		repository.upload(file, message, IOUtils.toInputStream(content));
		assertUpload(file, content);
		assertLastLog(file, message, getUsername());
	}

	@Test
	public void updatedFile() throws IOException {
		final Path file = Path.create(BASE + "/updateFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "content";
		final String message = "create file";

		repository.upload(file, message, IOUtils.toInputStream(content));
		assertUpload(file, content);
		assertLastLog(file, message, getUsername());

		final String contentV2 = "content 2";
		final String messageV2 = "update file";

		repository.upload(file, messageV2, IOUtils.toInputStream(contentV2));
		assertUpload(file, contentV2);
		assertLastLog(file, messageV2, getUsername());
	}

	@Test
	public void updatedWithPropertiesFile() throws IOException {
		final Path file = Path.create(BASE + "/updateFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "content";
		final String message = "create file";
		final ResourceProperty a = ResourceProperty.createCustomProperty("a", "A");
		final ResourceProperty b = ResourceProperty.createCustomProperty("b", "B");

		repository.upload(file, message, IOUtils.toInputStream(content), a, b);
		assertUpload(file, content);
		assertProperties(file, a, b);
		assertLastLog(file, message, getUsername());

		final String contentV2 = "content 2";
		final String messageV2 = "update file";

		repository.upload(file, messageV2, IOUtils.toInputStream(contentV2));
		assertUpload(file, contentV2);
		assertProperties(file, a, b);
		assertLastLog(file, messageV2, getUsername());
	}

	@Test
	public void uploadLockedFile() throws IOException {
		final Path file = Path.create(BASE + "/locked/updateFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "content";
		final String message = "create file";

		repository.upload(file, message, IOUtils.toInputStream(content));
		assertUpload(file, content);
		assertLastLog(file, message, getUsername());
		assertNotLocked(file);

		repository.lock(file);
		assertLocked(file, getUsername());

		final String contentV2 = "content 2";
		final String messageV2 = "update file";

		repository.upload(file, messageV2, IOUtils.toInputStream(contentV2));
		assertUpload(file, contentV2);
		assertLastLog(file, messageV2, getUsername());
		assertNotLocked(file);
	}

	@Test
	public void uploadFileWithProperties() throws IOException {
		final Path file = Path.create(BASE + "/uploadPropertiesFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "content";
		final String message = "create file";
		final ResourceProperty a = ResourceProperty.createCustomProperty("a", "A");
		final ResourceProperty b = ResourceProperty.createCustomProperty("b", "B");

		repository.upload(file, message, IOUtils.toInputStream(content), a, b);
		assertUpload(file, content);
		assertProperties(file, a, b);
		assertLastLog(file, message, getUsername());
	}

	@Test(expected = IllegalArgumentException.class)
	public void uploadNullContent() {
		final Path file = Path.create(BASE + "/nullContentFile.txt");
		repository.upload(file, "create file", null);
		Assert.fail("null content not allowed");
	}
}
