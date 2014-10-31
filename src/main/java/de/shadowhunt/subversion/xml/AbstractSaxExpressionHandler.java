package de.shadowhunt.subversion.xml;

import javax.annotation.CheckForNull;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractSaxExpressionHandler<V> extends DefaultHandler {

    private static final SAXParserFactory FACTORY;

    private final StringBuilder buffer = new StringBuilder();

    private int depth = 0;

    static {
        FACTORY = SAXParserFactory.newInstance();
        FACTORY.setNamespaceAware(true);
        FACTORY.setValidating(false);
    }

    protected final SaxExpression[] expressions;

    protected AbstractSaxExpressionHandler(final SaxExpression... expressions) {
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
        assert(depth == 0);
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) {
        final String text = getText();
        for (final SaxExpression expression : expressions) {
            expression.end(uri, localName, depth--, text);
        }
        clearText();
    }

    protected String getText() {
        return StringEscapeUtils.unescapeXml(buffer.toString());
    }

    @CheckForNull
    public V parse(final InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
        final SAXParser saxParser = FACTORY.newSAXParser();
        saxParser.parse(inputStream, this);
        return getValue();
    }

    @CheckForNull
    public abstract V getValue();

    @Override
    public void startDocument() {
        depth = 0;
        clearText();
        for (final SaxExpression expression : expressions) {
            expression.reset();
        }
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
        clearText();
        for (final SaxExpression expression : expressions) {
            expression.start(uri, localName, depth++, attributes);
        }
    }
}
