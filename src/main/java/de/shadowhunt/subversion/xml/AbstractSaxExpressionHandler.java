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
package de.shadowhunt.subversion.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.text.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public abstract class AbstractSaxExpressionHandler<V> extends DefaultHandler {

    private static final SAXParserFactory FACTORY;

    static {
        FACTORY = SAXParserFactory.newInstance();
        FACTORY.setNamespaceAware(true);
        FACTORY.setValidating(false);
    }

    private final StringBuilder buffer = new StringBuilder();

    private int depth = 0;

    protected final SaxExpression<?>[] expressions;

    protected AbstractSaxExpressionHandler(final SaxExpression<?>... expressions) {
        this.expressions = expressions;
    }

    @Override
    public final void characters(final char[] ch, final int start, final int length) {
        buffer.append(ch, start, length);
    }

    protected void clearText() {
        // clear buffer, but reuse the object and its allocated memory
        buffer.setLength(0);
    }

    @Override
    public void endDocument() {
        assert (depth == 0);
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) {
        final String text = getText();
        for (final SaxExpression<?> expression : expressions) {
            expression.end(uri, localName, depth--, text);
        }
        clearText();
    }

    protected String getText() {
        return StringEscapeUtils.unescapeXml(buffer.toString());
    }

    public abstract Optional<V> getValue();

    public Optional<V> parse(final InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
        final SAXParser saxParser = FACTORY.newSAXParser();
        saxParser.parse(inputStream, this);
        return getValue();
    }

    @Override
    public void startDocument() {
        depth = 0;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
        clearText();
        for (final SaxExpression<?> expression : expressions) {
            expression.start(uri, localName, depth++, attributes);
        }
    }
}
