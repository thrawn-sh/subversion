package de.shadowhunt.subversion.internal;

import java.util.List;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RepositoryList {

	private Repository repository;

	private String createMessage(final Resource resource, final Revision revision, final Depth depth) {
		return resource + ": @" + revision + " with depth: " + depth;
	}

	@Test(expected = SubversionException.class)
	public void test00_NonExisitingResource() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/list/non_existing.txt");
		final Revision revision = Revision.HEAD;

		repository.list(resource, revision, Depth.EMPTY, true);
		Assert.fail("list must not complete");
	}

	@Test(expected = SubversionException.class)
	public void test00_NonExisitingRevision() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/list/file.txt");
		final Revision revision = Revision.create(10000000); // there should not be a such high revision

		repository.list(resource, revision, Depth.EMPTY, true);
		Assert.fail("list must not complete");
	}

	@Test
	public void test01_FileHead() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/list/file.txt");
		final Revision revision = Revision.HEAD;

		for (final Depth depth : Depth.values()) {
			final List<Info> expected = null;
			final String message = createMessage(resource, revision, depth);
			Assert.assertEquals(message, expected, repository.list(resource, revision, depth, true));
		}
	}

	@Test
	public void test01_FileRevision() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/list/file.txt");
		final Revision revision = null; // FIXME

		for (final Depth depth : Depth.values()) {
			final List<Info> expected = null;
			final String message = createMessage(resource, revision, depth);
			Assert.assertEquals(message, expected, repository.list(resource, revision, depth, true));
		}
	}

	@Test
	public void test02_FolderHead() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/list/folder");
		final Revision revision = Revision.HEAD;

		for (final Depth depth : Depth.values()) {
			final List<Info> expected = null;
			final String message = createMessage(resource, revision, depth);
			Assert.assertEquals(message, expected, repository.list(resource, revision, depth, true));
		}
	}

	@Test
	public void test02_FolderRevision() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/list/folder");
		final Revision revision = null; // FIXME

		for (final Depth depth : Depth.values()) {
			final List<Info> expected = null;
			final String message = createMessage(resource, revision, depth);
			Assert.assertEquals(message, expected, repository.list(resource, revision, depth, true));
		}
	}

	@Test
	public void test03_RenamedFolder() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/list/renamed"); // head name
		final Revision revision = null; // FIXME
		final Depth depth = Depth.INFINITY;

		final List<Info> expected = null;
		final String message = createMessage(resource, revision, depth);
		Assert.assertEquals(message, expected, repository.list(resource, revision, depth, true));
	}
}
