package de.shadowhunt.scm.subversion;

import org.junit.Assert;
import org.junit.Test;

public class AbstractSubversionRepositoryTest {

	@Test
	public void containsTest() {
		Assert.assertTrue("all status codes", AbstractSubversionRepository.contains(400));

		Assert.assertTrue("found status code", AbstractSubversionRepository.contains(400, 400));
		Assert.assertTrue("found status code", AbstractSubversionRepository.contains(400, 800, 600, 400));

		Assert.assertFalse("missing status code", AbstractSubversionRepository.contains(400, 800, 600));
	}
}
