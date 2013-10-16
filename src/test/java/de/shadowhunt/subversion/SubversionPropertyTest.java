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

import de.shadowhunt.subversion.ResourceProperty.Type;
import org.junit.Assert;
import org.junit.Test;

public class SubversionPropertyTest {

	@Test
	public void createCustomPropertyTest() {
		final String name = "testName";
		final String value = "testValue";
		final ResourceProperty property = ResourceProperty.createCustomProperty(name, value);

		Assert.assertNotNull("property must not be null", property);
		Assert.assertEquals("type is not CUSTOM", Type.CUSTOM, property.getType());
		Assert.assertEquals("name does not match", name, property.getName());
		Assert.assertEquals("value does not match", value, property.getValue());
	}

	@Test
	public void filteroutSystemPropertiesEmptyTest() {
		final ResourceProperty[] properties = ResourceProperty.filteroutSystemProperties();

		Assert.assertNotNull("properties must not be null", properties);
		Assert.assertEquals("properties is not empty", 0, properties.length);
	}

	@Test
	public void filteroutSystemPropertiesNullTest() {
		final ResourceProperty[] properties = ResourceProperty.filteroutSystemProperties((ResourceProperty) null);

		Assert.assertNotNull("properties must not be null", properties);
		Assert.assertEquals("properties is not empty", 0, properties.length);
	}

	@Test
	public void filteroutSystemPropertiesTest() {
		final ResourceProperty[] input = new ResourceProperty[5];
		{
			input[0] = null;
			input[1] = new ResourceProperty(Type.BASE, "base", "base-value");
			input[2] = new ResourceProperty(Type.CUSTOM, "custom", "custom-value");
			input[3] = new ResourceProperty(Type.DAV, "dav", "dav-value");
			input[4] = new ResourceProperty(Type.SVN, "svn", "svn-value");
		}
		final ResourceProperty[] properties = ResourceProperty.filteroutSystemProperties(input);

		Assert.assertNotNull("properties must not be null", properties);
		Assert.assertEquals("properties is not empty", 2, properties.length);
		Assert.assertEquals("not expected CUSTOM property", input[2], properties[0]);
		Assert.assertEquals("not expected SVN property", input[4], properties[1]);
	}
}
