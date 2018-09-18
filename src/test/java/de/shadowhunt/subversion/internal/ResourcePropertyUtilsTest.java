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
package de.shadowhunt.subversion.internal;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class ResourcePropertyUtilsTest {

    private static final String MARKER = ResourcePropertyUtils.MARKER;

    @Test
    public void testEscapedInputStream_emptyTag() throws Exception {
        final String xml = "<C:foo:bar/>";
        final String expected = "<C:foo" + MARKER + "bar/>";
        final InputStream stream = IOUtils.toInputStream(xml, StandardCharsets.UTF_8);

        final InputStream escapedStream = ResourcePropertyUtils.escapedInputStream(stream);
        final String escapedXml = IOUtils.toString(escapedStream, StandardCharsets.UTF_8);

        Assert.assertEquals(expected, escapedXml);
    }

    @Test
    public void testEscapedInputStream_emptyTagInHierarchy() throws Exception {
        final String xml = "<A><svn:B><C:foo:bar/></svn:B></A>";
        final String expected = "<A><svn:B><C:foo" + MARKER + "bar/></svn:B></A>";
        final InputStream stream = IOUtils.toInputStream(xml, StandardCharsets.UTF_8);

        final InputStream escapedStream = ResourcePropertyUtils.escapedInputStream(stream);
        final String escapedXml = IOUtils.toString(escapedStream, StandardCharsets.UTF_8);

        Assert.assertEquals(expected, escapedXml);
    }

    @Test
    public void testEscapedInputStream_tagWithTagText() throws Exception {
        final String xml = "<C:foo:bar>text >/C:foo:bar</C:foo:bar>";
        final String expected = "<C:foo" + MARKER + "bar>text >/C:foo:bar</C:foo" + MARKER + "bar>";
        final InputStream stream = IOUtils.toInputStream(xml, StandardCharsets.UTF_8);

        final InputStream escapedStream = ResourcePropertyUtils.escapedInputStream(stream);
        final String escapedXml = IOUtils.toString(escapedStream, StandardCharsets.UTF_8);

        Assert.assertEquals(expected, escapedXml);
    }

    @Test
    public void testEscapedInputStream_tagWithText() throws Exception {
        final String xml = "<C:foo:bar>text</C:foo:bar>";
        final String expected = "<C:foo" + MARKER + "bar>text</C:foo" + MARKER + "bar>";
        final InputStream stream = IOUtils.toInputStream(xml, StandardCharsets.UTF_8);

        final InputStream escapedStream = ResourcePropertyUtils.escapedInputStream(stream);
        final String escapedXml = IOUtils.toString(escapedStream, StandardCharsets.UTF_8);

        Assert.assertEquals(expected, escapedXml);
    }

    @Test
    public void testEscapedKeyNameXml() throws Exception {
        final String expectedNoEscaped = "foo_bar";
        Assert.assertEquals(expectedNoEscaped, ResourcePropertyUtils.escapedKeyNameXml(expectedNoEscaped));

        final String expectedEscaped = "foo" + MARKER + "bar";
        Assert.assertEquals(expectedEscaped, ResourcePropertyUtils.escapedKeyNameXml("foo:bar"));
    }

    @Test
    public void testFilterMarker() throws Exception {
        final String expectedNoEscaped = "foo_bar:foobar";
        Assert.assertEquals(expectedNoEscaped, ResourcePropertyUtils.filterMarker(expectedNoEscaped));

        final String expectedEscaped = "foo:bar:foobar";
        Assert.assertEquals(expectedEscaped, ResourcePropertyUtils.unescapedKeyNameXml("foo" + MARKER + "bar:foobar"));
    }

    @Test
    public void testUnescapedKeyNameXml() throws Exception {
        final String expectedNoEscaped = "foo_bar";
        Assert.assertEquals(expectedNoEscaped, ResourcePropertyUtils.unescapedKeyNameXml(expectedNoEscaped));

        final String expectedEscaped = "foo:bar";
        Assert.assertEquals(expectedEscaped, ResourcePropertyUtils.unescapedKeyNameXml("foo" + MARKER + "bar"));
    }
}
