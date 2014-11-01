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

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.xml.sax.Attributes;

public abstract class AbstractSaxExpression<V> implements SaxExpression<V> {

    private static final SaxExpression[] NO_CHILDREN = new SaxExpression[0];

    private static boolean doesElementMatch(final QName element, final String nameSpaceUri, final String localName) {
        return doesNameSpaceUriMatch(element, nameSpaceUri) && doesLocalNameMatch(element, localName);
    }

    private static boolean doesLocalNameMatch(final QName element, final String localName) {
        final String pLocalName = element.getLocalPart();
        // * = any localName
        return "*".equals(pLocalName) || pLocalName.equals(localName);
    }

    private static boolean doesNameSpaceUriMatch(final QName element, final String nameSpaceUri) {
        final String pNameSpaceUri = element.getNamespaceURI();
        // XMLConstants.NULL_NS_URI = any nameSpace
        return XMLConstants.NULL_NS_URI.equals(pNameSpaceUri) || (pNameSpaceUri.equals(nameSpaceUri));
    }

    private final SaxExpression[] children;

    private final QName[] path;

    private int position = 0;

    protected AbstractSaxExpression(final QName[] path) {
        this(path, NO_CHILDREN);
    }

    protected AbstractSaxExpression(final QName[] path, final SaxExpression... children) {
        this.path = path;
        this.children = children;
    }

    @Override
    public final void end(final String nameSpaceUri, final String localName, final int depth, final String text) {
        if ((depth > position) || ((position - 1) >= path.length)) {
            final int childDepth = depth - position;
            for (final SaxExpression child : children) {
                child.end(nameSpaceUri, localName, childDepth, text);
            }
            return;
        }

        try {
            if (!doesElementMatch(path[position - 1], nameSpaceUri, localName)) {
                return;
            }

            if (position == path.length) {
                processEnd(nameSpaceUri, localName, text);
            }
        } finally {
            position--;
        }
    }

    protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
        // nothing to do
    }

    protected void processStart(final String nameSpaceUri, final String localName, final Attributes attributes) {
        // nothing to do
    }

    @Override
    public final void reset() {
        position = 0;
        resetHandler();
        for (final SaxExpression child : children) {
            child.reset();
        }
    }

    protected void resetHandler() {
        // nothing to do
    }

    @Override
    public final void start(final String nameSpaceUri, final String localName, final int depth, final Attributes attributes) {
        if ((depth > position) || (position >= path.length)) {
            final int childDepth = depth - position;
            for (final SaxExpression child : children) {
                child.start(nameSpaceUri, localName, childDepth, attributes);
            }
            return;
        }

        try {
            if (!doesElementMatch(path[position], nameSpaceUri, localName)) {
                return;
            }

            if (position == path.length - 1) {
                processStart(nameSpaceUri, localName, attributes);
            }
        } finally {
            position++;
        }
    }
}
