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

import org.junit.Assert;
import org.junit.Test;

public class ResourceTest {

    @Test
    public void compareResources() {
        final Resource resource = Resource.create("/a");
        final Resource same = Resource.create("/a");
        Assert.assertEquals("resource compareTo same: 0", 0, resource.compareTo(same));
        Assert.assertEquals("same compareTo resource: 0", 0, same.compareTo(resource));

        final Resource other = Resource.create("/b");
        Assert.assertTrue("resource is smaller than other", (resource.compareTo(other) < 0));
        Assert.assertTrue("other is bigger than resource", (other.compareTo(resource) > 0));
    }

    @Test
    public void createResource() {
        final Resource expected = Resource.create("/a/b/c/d.txt");
        Assert.assertEquals(expected, Resource.create("/a/b/c/d.txt"));
        Assert.assertEquals(expected, Resource.create("a/b/c/d.txt"));
        Assert.assertEquals(expected, Resource.create("//a/b/c/d.txt"));
        Assert.assertEquals(expected, Resource.create("a//b/c//d.txt"));
        Assert.assertEquals(expected, Resource.create("/a/b/c/d.txt/"));
        Assert.assertEquals(expected, Resource.create("/a/b/./c/d.txt"));
    }

    @Test(expected = SubversionException.class)
    public void createResource_withParentDirectory() throws Exception {
        Resource.create("/a/b/../c/d.txt");
        Assert.fail("path with parent directory must not complete");
    }

    @Test
    public void createRootResource() {
        Assert.assertEquals("/ is ROOT", Resource.ROOT, Resource.create(Resource.SEPARATOR));
        Assert.assertEquals("empty is ROOT", Resource.ROOT, Resource.create(""));
        Assert.assertEquals("null is ROOT", Resource.ROOT, Resource.create(null));
    }

    @Test
    public void equalsResource() {
        final Resource resource = Resource.create("/a");
        Assert.assertEquals("resource equals resource", resource, resource);

        final Resource same = Resource.create("/a");

        Assert.assertNotSame("resource and same are different object", resource, same);
        Assert.assertEquals("resource equals same", resource, same);
        Assert.assertEquals("same equals resource", same, resource);

        final Resource other = Resource.create("/b");
        Assert.assertNotEquals("resource doesn't equal other", resource, other);
        Assert.assertNotEquals("same doesn't equal other", same, other);
    }

    @Test
    public void getParent() {
        final Resource child = Resource.create("/a/b/c/d.txt");
        Assert.assertEquals(Resource.create("/a/b/c"), child.getParent());
        Assert.assertEquals(Resource.ROOT, Resource.ROOT.getParent());
    }

    @Test
    public void getValue() {
        final String expected = "/a/b/c/d.txt";
        final Resource resource = Resource.create(expected);
        Assert.assertEquals("resource value must match", expected, resource.getValue());
    }

    @Test
    public void getValueWithoutLeadingSeparator() {
        final String expected = "a/b/c/d.txt";
        final Resource resource = Resource.create(expected);
        Assert.assertEquals("resource value must match", expected, resource.getValueWithoutLeadingSeparator());
        Assert.assertEquals("root resource value must match", "", Resource.ROOT.getValueWithoutLeadingSeparator());
    }

    @Test
    public void hashCodeResource() {
        final Resource resource = Resource.create("/a");
        Assert.assertEquals("resource has same hashCode as resource", resource.hashCode(), resource.hashCode());

        final Resource same = Resource.create("/a");

        Assert.assertEquals("resource and same have same hashCode", resource.hashCode(), same.hashCode());

        final Resource other = Resource.create("/b");
        Assert.assertNotEquals("resource and other don't have same hashCode", resource.hashCode(), other.hashCode());
    }
}
