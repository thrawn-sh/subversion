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
package de.shadowhunt.subversion.internal;

import java.lang.reflect.Field;
import java.net.URI;

import de.shadowhunt.subversion.Resource;

import org.junit.Assert;
import org.junit.Test;

public class URIUtilsTest {

    private static final URI BASE = URI.create("http://www.example.net/foo");

    private static final URI ESCAPED_BASE = URI.create("http://www.example.net/subversion%20repository");

    @Test
    public void appendResourcesBasicRepo() {
        final URI germanUmlautsURI = URIUtils.appendResources(BASE, Resource.create("/üöäÜÖÄß.txt"));
        Assert.assertEquals("escaped german umlauts uri", "http://www.example.net/foo/%C3%BC%C3%B6%C3%A4%C3%9C%C3%96%C3%84%C3%9F.txt",
                germanUmlautsURI.toString());

        final URI specialCharsURI = URIUtils.appendResources(BASE, Resource.create("/^°²³\"§$%&{([)]}=?\\´`~+*'#,;.:-_µ@€<>| .txt"));
        Assert.assertEquals("escaped special chars uri",
                "http://www.example.net/foo/%5E%C2%B0%C2%B2%C2%B3%22%C2%A7$%25&%7B(%5B)%5D%7D=%3F%5C%C2%B4%60~+*'%23,;.:-_%C2%B5@%E2%82%AC%3C%3E%7C%20.txt",
                specialCharsURI.toString());

        final URI utf8URI = URIUtils.appendResources(BASE, Resource.create("/ジャワ.txt")); // java
        Assert.assertEquals("escaped utf8 uri", "http://www.example.net/foo/%E3%82%B8%E3%83%A3%E3%83%AF.txt", utf8URI.toString());
    }

    @Test
    public void appendResourcesEscapedRepo() {
        final URI germanUmlautsURI = URIUtils.appendResources(ESCAPED_BASE, Resource.create("/üöäÜÖÄß.txt"));
        Assert.assertEquals("escaped german umlauts uri", "http://www.example.net/subversion%20repository/%C3%BC%C3%B6%C3%A4%C3%9C%C3%96%C3%84%C3%9F.txt",
                germanUmlautsURI.toString());

        final URI specialCharsURI = URIUtils.appendResources(ESCAPED_BASE, Resource.create("/^°²³\"§$%&{([)]}=?\\´`~+*'#,;.:-_µ@€<>| .txt"));
        Assert.assertEquals("escaped special chars uri",
                "http://www.example.net/subversion%20repository/%5E%C2%B0%C2%B2%C2%B3%22%C2%A7$%25&%7B(%5B)%5D%7D=%3F%5C%C2%B4%60~+*'%23,;.:-_%C2%B5@%E2%82%AC%3C%3E%7C%20.txt",
                specialCharsURI.toString());

        final URI utf8URI = URIUtils.appendResources(ESCAPED_BASE, Resource.create("/ジャワ.txt")); // java
        Assert.assertEquals("escaped utf8 uri", "http://www.example.net/subversion%20repository/%E3%82%B8%E3%83%A3%E3%83%AF.txt", utf8URI.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void appendResourcesException() throws Exception {
        final URI uri = URI.create(BASE.toASCIIString());
        final Field field = URI.class.getDeclaredField("scheme");
        field.setAccessible(true);
        field.set(uri, "0http");

        URIUtils.appendResources(uri, Resource.create("/test"));
        Assert.fail("don't create illegal uris");
    }
}
