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
package de.shadowhunt.subversion.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public abstract class AbstractSaxExpressionHandler<V> extends DefaultHandler {

    private static final SAXParserFactory FACTORY;

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

    static {
        FACTORY = SAXParserFactory.newInstance();
        FACTORY.setNamespaceAware(true);
        FACTORY.setValidating(false);
    }
}
