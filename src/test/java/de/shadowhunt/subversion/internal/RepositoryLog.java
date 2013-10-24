package de.shadowhunt.subversion.internal;

import java.util.List;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.shadowhunt.subversion.Log;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RepositoryLog {

	private static final Resource PREFIX = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/log");

	private final Repository repository;

	protected RepositoryLog(final Repository repository) {
		this.repository = repository;
	}

	private String createMessage(final Resource resource, final Revision start, final Revision end, final int limit) {
		return resource + ": " + start + " -> " + end + " (" + limit + ")";
	}

	@Test(expected = SubversionException.class)
	public void test00_NonExisitingEndRevision() {
		final Resource resource = PREFIX.append(Resource.create("/file.txt"));
		final Revision start = Revision.INITIAL;
		final Revision end = Revision.create(10000000); // there should not be a such high revision
		final int limit = 0;

		repository.log(resource, start, end, limit);
		Assert.fail("log must not complete");
	}

	@Test(expected = SubversionException.class)
	public void test00_NonExisitingResource() {
		final Resource resource = PREFIX.append(Resource.create("/non_existing.txt"));
		final Revision start = Revision.INITIAL;
		final Revision end = Revision.HEAD;
		final int limit = 0;

		repository.log(resource, start, end, limit);
		Assert.fail("log must not complete");
	}

	@Test(expected = SubversionException.class)
	public void test00_NonExisitingStartRevision() {
		final Resource resource = PREFIX.append(Resource.create("/file.txt"));
		final Revision start = Revision.create(10000000); // there should not be a such high revision
		final Revision end = Revision.HEAD;
		final int limit = 0;

		repository.log(resource, start, end, limit);
		Assert.fail("log must not complete");
	}

	@Test
	public void test01_AllAscending() {
		final Resource resource = PREFIX.append(Resource.create("/file.txt"));
		final Revision start = Revision.INITIAL;
		final Revision end = Revision.HEAD;
		final int limit = 0;

		final List<Log> expected = null;
		final String message = createMessage(resource, start, end, limit);
		Assert.assertEquals(message, expected, repository.log(resource, start, end, limit));
	}

	@Test
	public void test01_AllDescending() {
		final Resource resource = PREFIX.append(Resource.create("/file.txt"));
		final Revision start = Revision.HEAD;
		final Revision end = Revision.INITIAL;
		final int limit = 0;

		final List<Log> expected = null;
		final String message = createMessage(resource, start, end, limit);
		Assert.assertEquals(message, expected, repository.log(resource, start, end, limit));
	}

	@Test
	public void test01_Only2Ascending() {
		final Resource resource = PREFIX.append(Resource.create("/file.txt"));
		final Revision start = Revision.INITIAL;
		final Revision end = Revision.HEAD;
		final int limit = 2;

		final List<Log> expected = null;
		final String message = createMessage(resource, start, end, limit);
		Assert.assertEquals(message, expected, repository.log(resource, start, end, limit));
	}

	@Test
	public void test01_Only2Descending() {
		final Resource resource = PREFIX.append(Resource.create("/file.txt"));
		final Revision start = Revision.HEAD;
		final Revision end = Revision.INITIAL;
		final int limit = 2;

		final List<Log> expected = null;
		final String message = createMessage(resource, start, end, limit);
		Assert.assertEquals(message, expected, repository.log(resource, start, end, limit));
	}

	@Test
	public void test01_Renamed() {
		Assert.fail(); // TODO
	}
}
