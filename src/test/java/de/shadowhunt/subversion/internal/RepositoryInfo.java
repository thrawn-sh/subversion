package de.shadowhunt.subversion.internal;

import java.util.List;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RepositoryInfo {

	private Repository repository;

	@Test(expected = SubversionException.class)
	public void test00_NonExisitingResource() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/info/non_existing.txt");
		final Revision revision = Revision.HEAD;

		repository.info(resource, revision, true);
		Assert.fail("info must not complete");
	}

	@Test(expected = SubversionException.class)
	public void test00_NonExisitingRevision() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/info/file.txt");
		final Revision revision = Revision.create(10000000); // there should not be a such high revision

		repository.info(resource, revision, true);
		Assert.fail("info must not complete");
	}

	@Test
	public void test01_FileHead() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/info/file.txt");
		final Revision revision = Revision.HEAD;

		for (final boolean withCustomProperties : new boolean[] { true, false }) {
			final List<Info> expected = null;
			final String message = resource + "@ " + revision + " with withCustomProperties" + withCustomProperties;
			Assert.assertEquals(message, expected, repository.info(resource, revision, withCustomProperties));
		}
	}

	@Test
	public void test01_FileRevision() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/info/file.txt");
		final Revision revision = null; // FIXME

		for (final boolean withCustomProperties : new boolean[] { true, false }) {
			final List<Info> expected = null;
			final String message = resource + "@ " + revision + " with withCustomProperties" + withCustomProperties;
			Assert.assertEquals(message, expected, repository.info(resource, revision, withCustomProperties));
		}
	}

	@Test
	public void test02_FolderHead() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/info/folder");
		final Revision revision = Revision.HEAD;

		for (final boolean withCustomProperties : new boolean[] { true, false }) {
			final List<Info> expected = null;
			final String message = resource + "@ " + revision + " with withCustomProperties" + withCustomProperties;
			Assert.assertEquals(message, expected, repository.info(resource, revision, withCustomProperties));
		}
	}

	@Test
	public void test02_FolderRevision() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/info/folder");
		final Revision revision = null; // FIXME

		for (final boolean withCustomProperties : new boolean[] { true, false }) {
			final List<Info> expected = null;
			final String message = resource + "@ " + revision + " with withCustomProperties" + withCustomProperties;
			Assert.assertEquals(message, expected, repository.info(resource, revision, withCustomProperties));
		}
	}

	@Test
	public void test03_RenamedFolder() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/info/renamed"); // head name
		final Revision revision = null; // FIXME

		final List<Info> expected = null;
		final String message = resource + "@ " + revision + " with withCustomProperties" + true;
		Assert.assertEquals(message, expected, repository.info(resource, revision, true));
	}
}
