package de.shadowhunt.subversion;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractRepositoryBasicWriteIT extends AbstractRepositoryAuthenticatedIT {

	private final String base = "/trunk/" + UUID.randomUUID().toString();

	protected AbstractRepositoryBasicWriteIT(final URI uri, final ServerVersion version, final String username, final String password, final String workstation) {
		super(uri, version, username, password, workstation);
	}

	protected String getBase() {
		return base;
	}

	@Test
	public void copyFile() throws IOException {
		final Path src = Path.create(getBase() + "/copySrcFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(src, Revision.HEAD));

		final String content = "content";
		final String messageUpload = "create file";

		repository.upload(src, messageUpload, IOUtils.toInputStream(content));
		RepositoryAssert.assertUpload(repository, src, content, messageUpload, getUsername());

		final Path target = Path.create(getBase() + "/copyTargetFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(target, Revision.HEAD));

		final String messageCopy = "copy file";

		repository.copy(src, Revision.HEAD, target, messageCopy);
		RepositoryAssert.assertUpload(repository, target, content, messageCopy, getUsername());
	}

	@Test
	public void createFolder() {
		final Path folder = Path.create(getBase() + "/createFolder");
		Assert.assertFalse("new folder must not exist", repository.exists(folder, Revision.HEAD));
		final String message = "create folder";

		repository.createFolder(folder, message);
		Assert.assertTrue("created folder must exist", repository.exists(folder, Revision.HEAD));
		RepositoryAssert.assertLastLog(repository, folder, message, getUsername());
	}

	@Test
	public void deleteFile() throws IOException {
		final Path file = Path.create(getBase() + "/deleteFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "content";
		final String message = "create file";
		repository.upload(file, message, IOUtils.toInputStream(content));
		RepositoryAssert.assertUpload(repository, file, content, message, getUsername());

		repository.delete(file, "delete folder");
		Assert.assertFalse("non exisitng file must not exist", repository.exists(file, Revision.HEAD));
	}

	@Test
	public void deleteFolder() {
		final Path folder = Path.create(getBase() + "/deleteFolder");

		repository.createFolder(folder, "create folder");
		Assert.assertTrue("created folder must exist", repository.exists(folder, Revision.HEAD));

		repository.delete(folder, "delete folder");
		Assert.assertFalse("deleted folder must not exist", repository.exists(folder, Revision.HEAD));
	}

	@Test(expected = SubversionException.class)
	public void deleteNonExistingFolder() {
		final Path folder = Path.create(getBase() + "/nonExistingFolder");
		Assert.assertFalse("non exisitng folder must not exist", repository.exists(folder, Revision.HEAD));

		repository.delete(folder, "delete folder");
		Assert.assertFalse("non exisitng folder must not exist", repository.exists(folder, Revision.HEAD));
	}

	@Test
	public void deleteProperties() throws IOException {
		final Path file = Path.create(getBase() + "/deletePropertiesFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "content";
		final String messageUpload = "create file";

		repository.upload(file, messageUpload, IOUtils.toInputStream(content));
		RepositoryAssert.assertUpload(repository, file, content, messageUpload, getUsername());

		final String messageProperties = "set properties";
		final ResourceProperty a = ResourceProperty.createCustomProperty("a", "A");
		final ResourceProperty b = ResourceProperty.createCustomProperty("b", "B");

		repository.setProperties(file, messageProperties, a, b);
		RepositoryAssert.assertUpload(repository, file, content, messageProperties, getUsername(), a, b);

		final String messageDelete = "delete properties";

		repository.deleteProperties(file, messageDelete, a, b);
		RepositoryAssert.assertUpload(repository, file, content, messageDelete, getUsername());
	}

	@Test
	public void doublecreateFolder() {
		final Path folder = Path.create(getBase() + "/doublecreateFolder");
		Assert.assertFalse("new folder must not exist", repository.exists(folder, Revision.HEAD));

		repository.createFolder(folder, "create folder");
		Assert.assertTrue("created folder must exist", repository.exists(folder, Revision.HEAD));

		repository.createFolder(folder, "create folder");
		Assert.assertTrue("created folder must exist", repository.exists(folder, Revision.HEAD));
	}

	@Test
	public void moveFile() throws IOException {
		final Path src = Path.create(getBase() + "/moveSrcFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(src, Revision.HEAD));

		final String content = "content";
		final String messageUpload = "create file";

		repository.upload(src, messageUpload, IOUtils.toInputStream(content));
		RepositoryAssert.assertUpload(repository, src, content, messageUpload, getUsername());

		final Path target = Path.create(getBase() + "/moveTargetFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(target, Revision.HEAD));

		final String messageMove = "move file";

		repository.move(src, target, messageMove);
		RepositoryAssert.assertUpload(repository, target, content, messageMove, getUsername());
		Assert.assertFalse("src file must not exist", repository.exists(src, Revision.HEAD));
	}

	@Test
	public void setPropertiesFile() throws IOException {
		final Path file = Path.create(getBase() + "/setPropertiesFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "content";
		final String messageUpload = "create file";

		repository.upload(file, messageUpload, IOUtils.toInputStream(content));
		RepositoryAssert.assertUpload(repository, file, content, messageUpload, getUsername());

		final String messageProperties = "set properties";
		final ResourceProperty a = ResourceProperty.createCustomProperty("a", "A");
		final ResourceProperty b = ResourceProperty.createCustomProperty("b", "B");

		repository.setProperties(file, messageProperties, a, b);
		RepositoryAssert.assertUpload(repository, file, content, messageProperties, getUsername(), a, b);
	}

	@Test
	public void updatedFile() throws IOException {
		final Path file = Path.create(getBase() + "/updateFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "content";
		final String message = "create file";

		repository.upload(file, message, IOUtils.toInputStream(content));
		RepositoryAssert.assertUpload(repository, file, content, message, getUsername());

		final String contentV2 = "content 2";
		final String messageV2 = "update file";

		repository.upload(file, messageV2, IOUtils.toInputStream(contentV2));
		RepositoryAssert.assertUpload(repository, file, contentV2, messageV2, getUsername());
	}

	@Test
	public void updatedWithPropertiesFile() throws IOException {
		final Path file = Path.create(getBase() + "/updatePropertiesFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "content";
		final String message = "create file";
		final ResourceProperty a = ResourceProperty.createCustomProperty("a", "A");
		final ResourceProperty b = ResourceProperty.createCustomProperty("b", "B");

		repository.upload(file, message, IOUtils.toInputStream(content), a, b);
		RepositoryAssert.assertUpload(repository, file, content, message, getUsername(), a, b);

		final String contentV2 = "content 2";
		final String messageV2 = "update file";

		repository.upload(file, messageV2, IOUtils.toInputStream(contentV2));
		RepositoryAssert.assertUpload(repository, file, contentV2, messageV2, getUsername(), a, b);
	}

	@Test
	public void uploadFile() throws IOException {
		final Path file = Path.create(getBase() + "/uploadFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "content";
		final String message = "create file";

		repository.upload(file, message, IOUtils.toInputStream(content));
		RepositoryAssert.assertUpload(repository, file, content, message, getUsername());
	}

	@Test
	public void uploadFileWithProperties() throws IOException {
		final Path file = Path.create(getBase() + "/uploadPropertiesFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "content";
		final String message = "create file";
		final ResourceProperty a = ResourceProperty.createCustomProperty("a", "A");
		final ResourceProperty b = ResourceProperty.createCustomProperty("b", "B");

		repository.upload(file, message, IOUtils.toInputStream(content), a, b);
		RepositoryAssert.assertUpload(repository, file, content, message, getUsername(), a, b);
	}

	@Test(expected = IllegalArgumentException.class)
	public void uploadNullContent() {
		final Path file = Path.create(getBase() + "/nullContentFile.txt");
		repository.upload(file, "create file", null);
		Assert.fail("null content not allowed");
	}
}
