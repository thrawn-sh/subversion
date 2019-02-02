/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2019 shadowhunt (dev@shadowhunt.de)
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

import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.text.StringEscapeUtils;
import org.xml.sax.helpers.DefaultHandler;

class BasicHandler extends DefaultHandler {

    protected static final SAXParserFactory FACTORY;

    static {
        FACTORY = SAXParserFactory.newInstance();
        FACTORY.setNamespaceAware(true);
        FACTORY.setValidating(false);
    }

    private final StringBuilder buffer = new StringBuilder();

    @Override
    public final void characters(final char[] ch, final int start, final int length) {
        buffer.append(ch, start, length);
    }

    protected void clearText() {
        // clear buffer, but reuse the object and its allocated memory
        buffer.setLength(0);
    }

    protected String getText() {
        return StringEscapeUtils.unescapeXml(buffer.toString());
    }
}
