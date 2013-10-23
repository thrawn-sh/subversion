package de.shadowhunt.subversion.internal;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RepositoryExists {

	private Repository repository;

	private String createMessage(final Resource resource, final Revision revision) {
		return resource + ": @" + revision;
	}

	@Test
	public void test00_NonExisitingResource() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/exists/non_existing.txt");
		final Revision revision = Revision.HEAD;

		final String message = createMessage(resource, revision);
		Assert.assertFalse(message, repository.exists(resource, revision));
	}

	@Test
	public void test00_NonExisitingRevision() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/exists/file.txt");
		final Revision revision = Revision.create(10000000); // there should not be a such high revision

		final String message = createMessage(resource, revision);
		Assert.assertFalse(message, repository.exists(resource, revision));
	}

	@Test
	public void test01_FileHead() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/exists/file.txt");
		final Revision revision = Revision.HEAD;

		final String message = createMessage(resource, revision);
		Assert.assertTrue(message, repository.exists(resource, revision));
	}

	@Test
	public void test01_FileRevision() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/exists/file_delete.txt");
		final Revision revision = null; // TODO

		final String message = createMessage(resource, revision);
		Assert.assertTrue(message, repository.exists(resource, revision));
	}

	@Test
	public void test01_FolderHead() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/exists/folder");
		final Revision revision = Revision.HEAD;

		final String message = createMessage(resource, revision);
		Assert.assertTrue(message, repository.exists(resource, revision));
	}

	@Test
	public void test01_FolderRevision() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/exists/folder_delete");
		final Revision revision = null; // TODO

		final String message = createMessage(resource, revision);
		Assert.assertTrue(message, repository.exists(resource, revision));
	}
}
