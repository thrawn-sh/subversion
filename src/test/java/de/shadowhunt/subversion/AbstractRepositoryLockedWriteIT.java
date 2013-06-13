package de.shadowhunt.subversion;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractRepositoryLockedWriteIT extends AbstractRepositoryBasicWriteIT {

	protected AbstractRepositoryLockedWriteIT(final URI uri, final ServerVersion version, final String username, final String password, final String workstation) {
		super(uri, version, username, password, workstation);
	}

	@Test
	public void copyLockedFile() throws IOException {
		final Path src = Path.create(getBase() + "/copySrcFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(src, Revision.HEAD));

		final String content = "content";
		final String messageUpload = "create file";

		repository.upload(src, messageUpload, IOUtils.toInputStream(content));
		RepositoryAssert.assertUpload(repository, src, content, messageUpload, getUsername());

		repository.lock(src);
		RepositoryAssert.assertLocked(repository, src, getUsername());

		final Path target = Path.create(getBase() + "/copyTargetFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(target, Revision.HEAD));

		final String messageCopy = "copy file";

		repository.copy(src, Revision.HEAD, target, messageCopy);
		RepositoryAssert.assertUpload(repository, target, content, messageCopy, getUsername());
	}

	@Test(expected = SubversionException.class)
	public void deleteLockedFile() throws IOException {
		final Path file = Path.create(getBase() + "/deleteFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "content";
		final String message = "create file";
		repository.upload(file, message, IOUtils.toInputStream(content));
		RepositoryAssert.assertUpload(repository, file, content, message, getUsername());

		repository.lock(file);
		RepositoryAssert.assertLocked(repository, file, getUsername());

		repository.delete(file, "delete folder");
		Assert.fail("locked resource can not be deleted");
	}

	@Test
	public void deletePropertiesLockedFile() throws IOException {
		final Path file = Path.create(getBase() + "/deletePropertiesFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "content";
		final String messageUpload = "create file";

		repository.upload(file, messageUpload, IOUtils.toInputStream(content));
		RepositoryAssert.assertUpload(repository, file, content, messageUpload, getUsername());

		repository.lock(file);
		RepositoryAssert.assertLocked(repository, file, getUsername());

		final String messageProperties = "set properties";
		final ResourceProperty a = ResourceProperty.createCustomProperty("a", "A");
		final ResourceProperty b = ResourceProperty.createCustomProperty("b", "B");

		repository.setProperties(file, messageProperties, a, b);
		RepositoryAssert.assertUpload(repository, file, content, messageProperties, getUsername(), a, b);

		final String messageDelete = "delete properties";

		repository.deleteProperties(file, messageDelete, a, b);
		RepositoryAssert.assertUpload(repository, file, content, messageDelete, getUsername());
	}

	@Override
	protected String getBase() {
		return super.getBase() + "/locked";
	}

	@Test(expected = SubversionException.class)
	public void moveLockedFile() throws IOException {
		final Path src = Path.create(getBase() + "/moveSrcFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(src, Revision.HEAD));

		final String content = "content";
		final String messageUpload = "create file";

		repository.upload(src, messageUpload, IOUtils.toInputStream(content));
		RepositoryAssert.assertUpload(repository, src, content, messageUpload, getUsername());

		repository.lock(src);
		RepositoryAssert.assertLocked(repository, src, getUsername());

		final Path target = Path.create(getBase() + "/moveTargetFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(target, Revision.HEAD));

		repository.move(src, target, "move file");
		Assert.fail("locked files can not be moved");
	}

	@Test
	public void setPropertiesLockedFile() throws IOException {
		final Path file = Path.create(getBase() + "/setPropertiesFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "content";
		final String messageUpload = "create file";
		repository.upload(file, messageUpload, IOUtils.toInputStream(content));
		RepositoryAssert.assertUpload(repository, file, content, messageUpload, getUsername());

		repository.lock(file);
		RepositoryAssert.assertLocked(repository, file, getUsername());

		final String messageProperties = "set properties";
		final ResourceProperty a = ResourceProperty.createCustomProperty("a", "A");
		final ResourceProperty b = ResourceProperty.createCustomProperty("b", "B");

		repository.setProperties(file, messageProperties, a, b);
		RepositoryAssert.assertUpload(repository, file, content, messageProperties, getUsername(), a, b);
	}

	@Test
	public void updatedLockedFile() throws IOException {
		final Path file = Path.create(getBase() + "/updateFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "content";
		final String message = "create file";

		repository.upload(file, message, IOUtils.toInputStream(content));
		RepositoryAssert.assertUpload(repository, file, content, message, getUsername());

		repository.lock(file);
		RepositoryAssert.assertLocked(repository, file, getUsername());

		final String contentV2 = "content 2";
		final String messageV2 = "update file";

		repository.upload(file, messageV2, IOUtils.toInputStream(contentV2));
		RepositoryAssert.assertUpload(repository, file, contentV2, messageV2, getUsername());
	}

	@Test
	public void updatedWithPropertiesLockedFile() throws IOException {
		final Path file = Path.create(getBase() + "/updatePropertiesFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "content";
		final String message = "create file";
		final ResourceProperty a = ResourceProperty.createCustomProperty("a", "A");
		final ResourceProperty b = ResourceProperty.createCustomProperty("b", "B");

		repository.upload(file, message, IOUtils.toInputStream(content), a, b);
		RepositoryAssert.assertUpload(repository, file, content, message, getUsername(), a, b);

		repository.lock(file);
		RepositoryAssert.assertLocked(repository, file, getUsername());

		final String contentV2 = "content 2";
		final String messageV2 = "update file";

		repository.upload(file, messageV2, IOUtils.toInputStream(contentV2));
		RepositoryAssert.assertUpload(repository, file, contentV2, messageV2, getUsername(), a, b);
	}

	@Test
	public void uploadLockedFile() throws IOException {
		final Path file = Path.create(getBase() + "/uploadFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "content";
		final String message = "create file";

		repository.upload(file, message, IOUtils.toInputStream(content));
		RepositoryAssert.assertUpload(repository, file, content, message, getUsername());

		repository.lock(file);
		RepositoryAssert.assertLocked(repository, file, getUsername());

		final String contentV2 = "content 2";
		final String messageV2 = "update file";

		repository.upload(file, messageV2, IOUtils.toInputStream(contentV2));
		RepositoryAssert.assertUpload(repository, file, contentV2, messageV2, getUsername());
	}
}
