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
package de.shadowhunt.subversion;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public abstract class AbstractRepositoryBasicWriteIT extends AbstractRepositoryAuthenticatedIT {

	private static String uid;

	private final String run;

	@BeforeClass
	public static void setup() {
		uid = UUID.randomUUID().toString();
	}

	protected AbstractRepositoryBasicWriteIT(final URI uri, final Version version, final String username, final String password, final String workstation) {
		super(uri, version, username, password, workstation);
		run = new File(uri.getPath()).getParent();
	}

	@Test
	public void copyFile() throws IOException {
		final Resource src = Resource.create(getBase() + "/copySrcFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(src, Revision.HEAD));

		final String content = "content";
		final String messageUpload = "create file";

		repository.upload(src, messageUpload, IOUtils.toInputStream(content));
		RepositoryAssert.assertUpload(repository, src, content, messageUpload, getUsername());

		final Resource target = Resource.create(getBase() + "/copyTargetFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(target, Revision.HEAD));

		final String messageCopy = "copy file";

		repository.copy(src, Revision.HEAD, target, messageCopy);
		RepositoryAssert.assertUpload(repository, target, content, messageCopy, getUsername());
	}

	@Test
	public void createFolder() {
		final Resource folder = Resource.create(getBase() + "/createFolder");
		Assert.assertFalse("new folder must not exist", repository.exists(folder, Revision.HEAD));
		final String message = "create folder";

		repository.createFolder(folder, message);
		Assert.assertTrue("created folder must exist", repository.exists(folder, Revision.HEAD));
		RepositoryAssert.assertLastLog(repository, folder, message, getUsername());
	}

	@Test
	public void deleteFile() throws IOException {
		final Resource file = Resource.create(getBase() + "/deleteFile.txt");
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
		final Resource folder = Resource.create(getBase() + "/deleteFolder");

		repository.createFolder(folder, "create folder");
		Assert.assertTrue("created folder must exist", repository.exists(folder, Revision.HEAD));

		repository.delete(folder, "delete folder");
		Assert.assertFalse("deleted folder must not exist", repository.exists(folder, Revision.HEAD));
	}

	@Test(expected = SubversionException.class)
	public void deleteNonExistingFolder() {
		final Resource folder = Resource.create(getBase() + "/nonExistingFolder");
		Assert.assertFalse("non exisitng folder must not exist", repository.exists(folder, Revision.HEAD));

		repository.delete(folder, "delete folder");
		Assert.assertFalse("non exisitng folder must not exist", repository.exists(folder, Revision.HEAD));
	}

	@Test
	public void deleteProperties() throws IOException {
		final Resource file = Resource.create(getBase() + "/deletePropertiesFile.txt");
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
	public void doubleCreateFolder() {
		final Resource folder = Resource.create(getBase() + "/doubleCreateFolder");
		Assert.assertFalse("new folder must not exist", repository.exists(folder, Revision.HEAD));

		repository.createFolder(folder, "create folder");
		Assert.assertTrue("created folder must exist", repository.exists(folder, Revision.HEAD));

		repository.createFolder(folder, "create folder");
		Assert.assertTrue("created folder must exist", repository.exists(folder, Revision.HEAD));
	}

	protected String getBase() {
		final String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		return "/trunk/" + date + "/" + run + "/" + uid;
	}

	@Test
	public void moveFile() throws IOException {
		final Resource src = Resource.create(getBase() + "/moveSrcFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(src, Revision.HEAD));

		final String content = "content";
		final String messageUpload = "create file";

		repository.upload(src, messageUpload, IOUtils.toInputStream(content));
		RepositoryAssert.assertUpload(repository, src, content, messageUpload, getUsername());

		final Resource target = Resource.create(getBase() + "/moveTargetFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(target, Revision.HEAD));

		final String messageMove = "move file";

		repository.move(src, target, messageMove);
		RepositoryAssert.assertUpload(repository, target, content, messageMove, getUsername());
		Assert.assertFalse("src file must not exist", repository.exists(src, Revision.HEAD));
	}

	@Test
	public void setPropertiesFile() throws IOException {
		final Resource file = Resource.create(getBase() + "/setPropertiesFile.txt");
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
		final Resource file = Resource.create(getBase() + "/updateFile.txt");
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
		final Resource file = Resource.create(getBase() + "/updatePropertiesFile.txt");
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
		final Resource file = Resource.create(getBase() + "/uploadFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "content";
		final String message = "create file";

		repository.upload(file, message, IOUtils.toInputStream(content));
		RepositoryAssert.assertUpload(repository, file, content, message, getUsername());
	}

	@Test
	public void uploadFileWithProperties() throws IOException {
		final Resource file = Resource.create(getBase() + "/uploadPropertiesFile.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "content";
		final String message = "create file";
		final ResourceProperty a = ResourceProperty.createCustomProperty("a", "A");
		final ResourceProperty b = ResourceProperty.createCustomProperty("b", "B");

		repository.upload(file, message, IOUtils.toInputStream(content), a, b);
		RepositoryAssert.assertUpload(repository, file, content, message, getUsername(), a, b);
	}

	@Test
	public void uploadFileWithSpecialCharacters() throws IOException {
		final Resource file = Resource.create(getBase() + "/specialChars-\u30b8\u30e3\u30ef.txt");
		Assert.assertFalse("new file must not exist", repository.exists(file, Revision.HEAD));

		final String content = "\u30b8\u30e3\u30ef";
		final String message = "create file with name \u30b8\u30e3\u30ef.txt";

		repository.upload(file, message, IOUtils.toInputStream(content));
		RepositoryAssert.assertUpload(repository, file, content, message, getUsername());
	}

	@Test(expected = IllegalArgumentException.class)
	public void uploadNullContent() {
		final Resource file = Resource.create(getBase() + "/nullContentFile.txt");
		repository.upload(file, "create file", null);
		Assert.fail("null content not allowed");
	}
}
