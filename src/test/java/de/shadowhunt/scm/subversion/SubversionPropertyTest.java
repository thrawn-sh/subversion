package de.shadowhunt.scm.subversion;

import org.junit.Assert;
import org.junit.Test;

import de.shadowhunt.scm.subversion.SubversionProperty.Type;

public class SubversionPropertyTest {

	@Test
	public void createCustomPropertyTest() {
		final String name = "testName";
		final String value = "testValue";
		final SubversionProperty property = SubversionProperty.createCustomProperty(name, value);

		Assert.assertNotNull("property must not be null", property);
		Assert.assertEquals("type is not CUSTOM", Type.CUSTOM, property.getType());
		Assert.assertEquals("name does not match", name, property.getName());
		Assert.assertEquals("value does not match", value, property.getValue());
	}

	@Test
	public void filteroutSystemPropertiesEmptyTest() {
		final SubversionProperty[] properties = SubversionProperty.filteroutSystemProperties();

		Assert.assertNotNull("properties must not be null", properties);
		Assert.assertEquals("properties is not empty", 0, properties.length);
	}

	@Test
	public void filteroutSystemPropertiesNullTest() {
		final SubversionProperty[] properties = SubversionProperty.filteroutSystemProperties((SubversionProperty) null);

		Assert.assertNotNull("properties must not be null", properties);
		Assert.assertEquals("properties is not empty", 0, properties.length);
	}

	@Test
	public void filteroutSystemPropertiesTest() {
		final SubversionProperty[] input = new SubversionProperty[5];
		{
			input[0] = null;
			input[1] = new SubversionProperty(Type.BASE, "base", "base-value");
			input[2] = new SubversionProperty(Type.CUSTOM, "custom", "custom-value");
			input[3] = new SubversionProperty(Type.DAV, "dav", "dav-value");
			input[4] = new SubversionProperty(Type.SVN, "svn", "svn-value");
		}
		final SubversionProperty[] properties = SubversionProperty.filteroutSystemProperties(input);

		Assert.assertNotNull("properties must not be null", properties);
		Assert.assertEquals("properties is not empty", 2, properties.length);
		Assert.assertEquals("not expected CUSTOM property", input[2], properties[0]);
		Assert.assertEquals("not expected SVN property", input[4], properties[1]);
	}
}
