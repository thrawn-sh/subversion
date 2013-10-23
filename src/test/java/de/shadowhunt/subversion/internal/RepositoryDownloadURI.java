package de.shadowhunt.subversion.internal;

import java.net.URI;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RepositoryDownloadURI {

	private Repository repository;

	private String createMessage(final Resource resource, final Revision revision) {
		return resource + ": @" + revision;
	}

	@Test(expected = SubversionException.class)
	public void test00_NonExisitingResource() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/download/non_existing.txt");
		final Revision revision = Revision.HEAD;

		repository.downloadURI(resource, revision);
		Assert.fail("downloadURI must not complete");
	}

	@Test(expected = SubversionException.class)
	public void test00_NonExisitingRevision() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/download/file.txt");
		final Revision revision = Revision.create(10000000); // there should not be a such high revision

		repository.downloadURI(resource, revision);
		Assert.fail("downloadURI must not complete");
	}

	@Test
	public void test01_FileHead() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/download/file.txt");
		final Revision revision = Revision.HEAD;

		final URI expected = null; // FIXME
		final String message = createMessage(resource, revision);
		Assert.assertEquals(message, expected, repository.downloadURI(resource, revision));
	}

	@Test
	public void test01_FileRevision() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/download/file_delete.txt");
		final Revision revision = null; // TODO

		final URI expected = null; // FIXME
		final String message = createMessage(resource, revision);
		Assert.assertEquals(message, expected, repository.downloadURI(resource, revision));
	}
}
