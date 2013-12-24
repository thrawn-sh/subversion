/**
 * Copyright (C) 2013 shadowhunt (dev@shadowhunt.de)
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
    }

    @Test
    public void createRootResource() {
        Assert.assertEquals("/ is ROOT", Resource.ROOT, Resource.create("/"));
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
        final String expexted = "/a/b/c/d.txt";
        final Resource resource = Resource.create(expexted);
        Assert.assertEquals("resource value must match", expexted, resource.getValue());
    }

    @Test
    public void getValueWithoutLeadingSeparator() {
        final String expexted = "a/b/c/d.txt";
        final Resource resource = Resource.create(expexted);
        Assert.assertEquals("resource value must match", expexted, resource.getValueWithoutLeadingSeparator());
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
