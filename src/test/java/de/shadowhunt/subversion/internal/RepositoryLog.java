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

	public static final int UNLIMITED = 0;

	protected RepositoryLog(final Repository repository) {
		this.repository = repository;
	}

	private String createMessage(final Resource resource, final Revision start, final Revision end, final int limit) {
		return resource + ": " + start + " -> " + end + " (" + limit + ")";
	}

	@Test(expected = SubversionException.class)
	public void test00_NonExisitingEndRevision() throws Exception {
		final Resource resource = PREFIX.append(Resource.create("/file.txt"));
		final Revision start = Revision.INITIAL;
		final Revision end = Revision.create(Integer.MAX_VALUE); // there should not be a such high revision
		final int limit = UNLIMITED;

		repository.log(resource, start, end, limit);
		Assert.fail("log must not complete");
	}

	@Test(expected = SubversionException.class)
	public void test00_NonExisitingResource() throws Exception {
		final Resource resource = PREFIX.append(Resource.create("/non_existing.txt"));
		final Revision start = Revision.INITIAL;
		final Revision end = Revision.HEAD;
		final int limit = UNLIMITED;

		repository.log(resource, start, end, limit);
		Assert.fail("log must not complete");
	}

	@Test(expected = SubversionException.class)
	public void test00_NonExisitingStartRevision() throws Exception {
		final Resource resource = PREFIX.append(Resource.create("/file.txt"));
		final Revision start = Revision.create(Integer.MAX_VALUE); // there should not be a such high revision
		final Revision end = Revision.HEAD;
		final int limit = UNLIMITED;

		repository.log(resource, start, end, limit);
		Assert.fail("log must not complete");
	}

	@Test
	public void test01_FileHeadAllAscending() throws Exception {
		final Resource resource = PREFIX.append(Resource.create("/file.txt"));
		final Revision start = Revision.INITIAL;
		final Revision end = Revision.HEAD;
		final int limit = UNLIMITED;

		final List<Log> expected = LogLoader.load(resource, start, end, limit);
		final String message = createMessage(resource, start, end, limit);
		Assert.assertEquals(message, expected, repository.log(resource, start, end, limit));
	}

	@Test
	public void test01_FileHeadAllDescending() throws Exception {
		final Resource resource = PREFIX.append(Resource.create("/file.txt"));
		final Revision start = Revision.HEAD;
		final Revision end = Revision.INITIAL;
		final int limit = UNLIMITED;

		final List<Log> expected = LogLoader.load(resource, start, end, limit);
		final String message = createMessage(resource, start, end, limit);
		Assert.assertEquals(message, expected, repository.log(resource, start, end, limit));
	}

	@Test
	public void test01_FileHeadOnly2Ascending() throws Exception {
		final Resource resource = PREFIX.append(Resource.create("/file.txt"));
		final Revision start = Revision.INITIAL;
		final Revision end = Revision.HEAD;
		final int limit = 2;

		final List<Log> expected = LogLoader.load(resource, start, end, limit);
		final String message = createMessage(resource, start, end, limit);
		Assert.assertEquals(message, limit, expected.size());
		Assert.assertEquals(message, expected, repository.log(resource, start, end, limit));
	}

	@Test
	public void test01_FileHeadOnly2Descending() throws Exception {
		final Resource resource = PREFIX.append(Resource.create("/file.txt"));
		final Revision start = Revision.HEAD;
		final Revision end = Revision.INITIAL;
		final int limit = 2;

		final List<Log> expected = LogLoader.load(resource, start, end, limit);
		final String message = createMessage(resource, start, end, limit);
		Assert.assertEquals(message, limit, expected.size());
		Assert.assertEquals(message, expected, repository.log(resource, start, end, limit));
	}

	@Test
	public void test01_FileRevisionAscending() throws Exception {
		final Resource resource = PREFIX.append(Resource.create("/file_delete.txt"));
		final Revision start = Revision.INITIAL;
		final Revision end = Revision.create(82);
		final int limit = UNLIMITED;

		final List<Log> expected = LogLoader.load(resource, start, end, limit);
		final String message = createMessage(resource, start, end, limit);
		Assert.assertEquals(message, expected, repository.log(resource, start, end, limit));
	}

	@Test
	public void test01_FileRevisionDescending() throws Exception {
		final Resource resource = PREFIX.append(Resource.create("/file_delete.txt"));
		final Revision start = Revision.create(82);
		final Revision end = Revision.INITIAL;
		final int limit = UNLIMITED;

		final List<Log> expected = LogLoader.load(resource, start, end, limit);
		final String message = createMessage(resource, start, end, limit);
		Assert.assertEquals(message, expected, repository.log(resource, start, end, limit));
	}

	@Test
	public void test02_FileCopy() throws Exception {
		final Resource resource = PREFIX.append(Resource.create("/file_copy.txt"));
		final Revision start = Revision.INITIAL;
		final Revision end = Revision.create(85);
		final int limit = UNLIMITED;

		final List<Log> expected = LogLoader.load(resource, start, end, limit);
		final String message = createMessage(resource, start, end, limit);
		Assert.assertEquals(message, expected, repository.log(resource, start, end, limit));
	}

	@Test
	public void test02_FileMove() throws Exception {
		final Resource resource = PREFIX.append(Resource.create("/file_move.txt"));
		final Revision start = Revision.INITIAL;
		final Revision end = Revision.create(87);
		final int limit = UNLIMITED;

		// NOTE: determine last existing revision for loader
		final List<Log> expected = LogLoader.load(resource, start, Revision.create(86), limit);
		final String message = createMessage(resource, start, end, limit);
		Assert.assertEquals(message, expected, repository.log(resource, start, end, limit));
	}
}
