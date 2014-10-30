package de.shadowhunt.subversion.internal;

import javax.annotation.CheckForNull;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class SaxExpressionHandler<V> extends DefaultHandler {

    private static final SAXParserFactory FACTORY;

    static class SaxExpression {

        private final boolean end;

        private final QName[] path;

        private int position = 0;

        private final boolean start;

        SaxExpression(final QName[] path, final boolean start, final boolean end) {
            this.path = path;
            this.start = start;
            this.end = end;
        }

        private boolean doesElementMatch(final QName element, final String nameSpaceUri, final String localName) {
            return doesNameSpaceUriMatch(element, nameSpaceUri) && doesLocalNameMatch(element, localName);
        }

        private boolean doesLocalNameMatch(final QName element, final String localName) {
            final String pLocalName = element.getLocalPart();
            return "*".equals(pLocalName) || pLocalName.equals(localName);
        }

        private boolean doesNameSpaceUriMatch(final QName element, final String nameSpaceUri) {
            final String pNamespaceUri = element.getNamespaceURI();
            return (pNamespaceUri == null) || (pNamespaceUri.equals(nameSpaceUri));
        }

        public final void end(final String namespaceUri, final String localName, final int depth, final String text) {
            if ((depth != position) || ((position - 1) >= path.length)) {
                return;
            }

            if (!doesElementMatch(path[position - 1], namespaceUri, localName)) {
                return;
            }

            try {
                if (end && (position == path.length)) {
                    processEnd(namespaceUri, localName, text);
                }
            } finally {
                position--;
            }
        }

        protected void processEnd(final String namespaceUri, final String localName, final String text) {
            throw new UnsupportedOperationException();
        }

        protected void processStart(final String namespaceUri, final String localName, final Attributes attributes) {
            throw new UnsupportedOperationException();
        }

        public final void reset() {
            position = 0;
            resetHandler();
        }

        protected void resetHandler() {
            // nothing to do
        }

        public final void start(final String namespaceUri, final String localName, final int depth, final Attributes attributes) {
            if ((depth != position) || (position >= path.length)) {
                return;
            }

            if (!doesElementMatch(path[position], namespaceUri, localName)) {
                return;
            }

            try {
                if (start && (position == path.length -1)) {
                    processStart(namespaceUri, localName, attributes);
                }
            } finally {
                position++;
            }
        }
    }

    public static SAXParser newParser() throws ParserConfigurationException, SAXException {
        return FACTORY.newSAXParser();
    }

    private final StringBuilder buffer = new StringBuilder();

    private int depth = 0;

    static {
        FACTORY = SAXParserFactory.newInstance();
        FACTORY.setNamespaceAware(true);
        FACTORY.setValidating(false);
    }

    protected final SaxExpression[] expressions;

    protected SaxExpressionHandler(final SaxExpression... expressions) {
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
    public V getValue() {
        throw new UnsupportedOperationException();
    }

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
