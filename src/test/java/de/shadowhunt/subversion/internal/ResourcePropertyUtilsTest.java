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
package de.shadowhunt.subversion.internal;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

public class ResourcePropertyUtilsTest {

    private static final String MARKER = ResourcePropertyUtils.MARKER;

    @Test
    public void testEscapedKeyNameXml() throws Exception {
        final String expectedNoEscaped = "foo_bar";
        Assert.assertEquals(expectedNoEscaped, ResourcePropertyUtils.escapedKeyNameXml(expectedNoEscaped));

        final String expectedEscaped = "foo" + MARKER + "bar";
        Assert.assertEquals(expectedEscaped, ResourcePropertyUtils.escapedKeyNameXml("foo:bar"));
    }

    @Test
    public void testUnescapedKeyNameXml() throws Exception {
        final String expectedNoEscaped = "foo_bar";
        Assert.assertEquals(expectedNoEscaped, ResourcePropertyUtils.unescapedKeyNameXml(expectedNoEscaped));

        final String expectedEscaped = "foo:bar";
        Assert.assertEquals(expectedEscaped, ResourcePropertyUtils.unescapedKeyNameXml("foo" + MARKER + "bar"));
    }

    @Test
    public void testFilterMarker() throws Exception {
        final String expectedNoEscaped = "foo_bar:foobar";
        Assert.assertEquals(expectedNoEscaped, ResourcePropertyUtils.filterMarker(expectedNoEscaped));

        final String expectedEscaped = "foo:bar:foobar";
        Assert.assertEquals(expectedEscaped, ResourcePropertyUtils.unescapedKeyNameXml("foo" + MARKER + "bar:foobar"));
    }

    @Test
    public void testEscapedInputStream_emptyTag() throws Exception {
        final String xml = "<C:foo:bar/>";
        final String expected = "<C:foo" + MARKER + "bar/>";
        final InputStream stream = IOUtils.toInputStream(xml, ResourcePropertyUtils.UTF8);

        final InputStream escapedStream = ResourcePropertyUtils.escapedInputStream(stream);
        final String escapedXml = IOUtils.toString(escapedStream, ResourcePropertyUtils.UTF8);

        Assert.assertEquals(expected, escapedXml);
    }

    @Test
    public void testEscapedInputStream_emptyTagInHierarchy() throws Exception {
        final String xml = "<A><svn:B><C:foo:bar/></svn:B></A>";
        final String expected = "<A><svn:B><C:foo" + MARKER + "bar/></svn:B></A>";
        final InputStream stream = IOUtils.toInputStream(xml, ResourcePropertyUtils.UTF8);

        final InputStream escapedStream = ResourcePropertyUtils.escapedInputStream(stream);
        final String escapedXml = IOUtils.toString(escapedStream, ResourcePropertyUtils.UTF8);

        Assert.assertEquals(expected, escapedXml);
    }

    @Test
    public void testEscapedInputStream_tagWithText() throws Exception {
        final String xml = "<C:foo:bar>text</C:foo:bar>";
        final String expected = "<C:foo" + MARKER + "bar>text</C:foo" + MARKER + "bar>";
        final InputStream stream = IOUtils.toInputStream(xml, ResourcePropertyUtils.UTF8);

        final InputStream escapedStream = ResourcePropertyUtils.escapedInputStream(stream);
        final String escapedXml = IOUtils.toString(escapedStream, ResourcePropertyUtils.UTF8);

        Assert.assertEquals(expected, escapedXml);
    }

    @Test
    public void testEscapedInputStream_tagWithTagText() throws Exception {
        final String xml = "<C:foo:bar>text >/C:foo:bar</C:foo:bar>";
        final String expected = "<C:foo" + MARKER + "bar>text >/C:foo:bar</C:foo" + MARKER + "bar>";
        final InputStream stream = IOUtils.toInputStream(xml, ResourcePropertyUtils.UTF8);

        final InputStream escapedStream = ResourcePropertyUtils.escapedInputStream(stream);
        final String escapedXml = IOUtils.toString(escapedStream, ResourcePropertyUtils.UTF8);

        Assert.assertEquals(expected, escapedXml);
    }
}