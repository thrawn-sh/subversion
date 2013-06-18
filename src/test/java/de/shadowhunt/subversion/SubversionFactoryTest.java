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

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

public class SubversionFactoryTest {

	@Test(expected = SubversionException.class)
	public void assertSupportedSchemeFileTest() {
		final URI uri = URI.create("file:///svn/test-repo");
		SubversionFactory.assertSupportedScheme(uri);
		Assert.fail("uri must not be supported");
	}

	@Test
	public void assertSupportedSchemeHttpsTest() {
		final URI uri = URI.create("https://subversion.example.net/svn/test-repo");
		SubversionFactory.assertSupportedScheme(uri);
		Assert.assertTrue("https is supported", true);
	}

	@Test
	public void assertSupportedSchemeHttpTest() {
		final URI uri = URI.create("http://subversion.example.net/svn/test-repo");
		SubversionFactory.assertSupportedScheme(uri);
		Assert.assertTrue("http is supported", true);
	}

	@Test
	public void getInstanceTest() {
		final URI uri = URI.create("http://subversion.example.net/svn/test-repo");
		final Repository repository = SubversionFactory.getInstance(uri, true, ServerVersion.V1_6);
		Assert.assertNotNull("SubversionRepository must not be null", repository);
	}

	@Test(expected = SubversionException.class)
	public void getInstanceUnsupportedServerVersionTest() {
		final URI uri = URI.create("http://subversion.example.net/svn/test-repo");
		SubversionFactory.getInstance(uri, true, null);
		Assert.fail("ServerVersion must not be null");
	}

	@Test
	public void removeEndingSlash() {
		final URI withoutSlash = URI.create("https://subversion.example.net/svn/test-repo");
		Assert.assertEquals("uri without ending slash are not modified", withoutSlash, SubversionFactory.removeEndingSlash(withoutSlash));

		final URI withSlash = URI.create("https://subversion.example.net/svn/test-repo/");
		Assert.assertEquals("uri without ending slash are not modified", withoutSlash, SubversionFactory.removeEndingSlash(withSlash));
	}
}
