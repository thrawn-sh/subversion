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

import java.io.InputStream;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParser;

import de.shadowhunt.subversion.Repository.ProtocolVersion;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.xml.AbstractSaxExpression;
import de.shadowhunt.subversion.xml.AbstractSaxExpressionHandler;
import org.xml.sax.Attributes;

final class Prefix {

    private static class PrefixExpression extends AbstractSaxExpression<Resource> {

        private static QName[] PATH;

        private static final Pattern PATH_PATTERN = Pattern.compile(Resource.SEPARATOR);

        static {
            final QName[] path = new QName[3];
            path[0] = new QName(XmlConstants.DAV_NAMESPACE, "options-response");
            path[1] = new QName(XmlConstants.DAV_NAMESPACE, "activity-collection-set");
            path[2] = new QName(XmlConstants.DAV_NAMESPACE, "href");
            PATH = path;
        }

        private Resource prefix = null;

        private final ProtocolVersion version;

        PrefixExpression(final ProtocolVersion version) {
            super(PATH);
            this.version = version;
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            if ((ProtocolVersion.HTTP_V1 == version) || (ProtocolVersion.HTTP_V2 == version)) {
                // .../${svn}/act/
                //     ^^^^^^ <- prefix
                final String[] segments = PATH_PATTERN.split(text);
                prefix = Resource.create(segments[segments.length - 2]);
            }
        }

        @Override
        protected void processStart(final String nameSpaceUri, final String localName, final Attributes attributes) {
            // nothing to do
        }

        @Override
        protected void resetHandler() {
            prefix = null;
        }

        @Override
        public Resource getValue() {
            return prefix;
        }
    }

    private static class PrefixHandler extends AbstractSaxExpressionHandler<Resource> {

        PrefixHandler(final ProtocolVersion version) {
            super(new PrefixExpression(version));
        }

        @Override
        public Resource getValue() {
            return ((PrefixExpression) expressions[0]).prefix;
        }
    }

    static Resource read(final InputStream inputStream, final ProtocolVersion version) {
        final Resource prefix;
        try {
            final AbstractSaxExpressionHandler<Resource> handler = new PrefixHandler(version);
            prefix = handler.parse(inputStream);
        } catch (final Exception e) {
            throw new SubversionException("Invalid server response: could not parse response", e);
        }

        if (prefix == null) {
            throw new SubversionException("Invalid server response: could not parse response");
        }
        return prefix;
    }

    private Prefix() {
        // prevent instantiation
    }

}
