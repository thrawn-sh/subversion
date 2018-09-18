/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2018 shadowhunt (dev@shadowhunt.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.shadowhunt.subversion;

import de.shadowhunt.subversion.ResourceProperty.Key;
import de.shadowhunt.subversion.ResourceProperty.Type;
import org.junit.Assert;
import org.junit.Test;

public class ResourcePropertyTest {

    @Test
    public void createCustomPropertyTest() {
        final String name = "testName";
        final String value = "testValue";
        final Key key = new Key(Type.SUBVERSION_CUSTOM, name);
        Assert.assertEquals("type is not SUBVERSION_CUSTOM", Type.SUBVERSION_CUSTOM, key.getType());
        Assert.assertEquals("name does not match", name, key.getName());
        final ResourceProperty property = new ResourceProperty(key, value);

        Assert.assertNotNull("property must not be null", property);
        Assert.assertEquals("value does not match", value, property.getValue());
    }
}
