package de.shadowhunt.subversion.internal;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.shadowhunt.subversion.Log;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.SubversionException;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RepositoryLastLog {

	private Repository repository;

	@Test(expected = SubversionException.class)
	public void test00_NonExisitingResource() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/log/non_exisiting.txt");

		repository.lastLog(resource);
		Assert.fail("lastLog must not complete");
	}

	@Test
	public void test01_File() {
		final Resource resource = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/log/file.txt");

		final Log expected = null;
		final String message = resource.getValue();
		Assert.assertEquals(message, expected, repository.lastLog(resource));
	}

	@Test
	public void test01_Renamed() {
		Assert.fail(); // TODO
	}
}
