/**
 * Copyright (C) 2013-2017 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.shadowhunt.subversion;

import de.shadowhunt.subversion.ResourceProperty.Type;

import org.junit.Assert;
import org.junit.Test;

public class ResourcePropertyTest {

    @Test
    public void createCustomPropertyTest() {
        final String name = "testName";
        final String value = "testValue";
        final ResourceProperty property = new ResourceProperty(Type.SUBVERSION_CUSTOM, name, value);

        Assert.assertNotNull("property must not be null", property);
        Assert.assertEquals("type is not SUBVERSION_CUSTOM", Type.SUBVERSION_CUSTOM, property.getType());
        Assert.assertEquals("name does not match", name, property.getName());
        Assert.assertEquals("value does not match", value, property.getValue());
    }
}
