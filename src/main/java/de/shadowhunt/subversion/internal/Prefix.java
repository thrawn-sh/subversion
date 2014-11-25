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

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.shadowhunt.subversion.Repository.ProtocolVersion;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.xml.AbstractSaxExpression;
import de.shadowhunt.subversion.xml.AbstractSaxExpressionHandler;

final class Prefix {

    private static class PrefixExpression extends AbstractSaxExpression<Resource> {

        private static final QName[] PATH = { //
                new QName(XmlConstants.DAV_NAMESPACE, "options-response"), //
                new QName(XmlConstants.DAV_NAMESPACE, "activity-collection-set"), //
                new QName(XmlConstants.DAV_NAMESPACE, "href") //
        };

        private static final Pattern PATH_PATTERN = Pattern.compile(Resource.SEPARATOR);

        private Resource prefix = null;

        private final ProtocolVersion version;

        PrefixExpression(final ProtocolVersion version) {
            super(PATH);
            this.version = version;
        }

        @Override
        public Resource getValue() {
            return prefix;
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
        public void resetHandler() {
            super.resetHandler();
            prefix = null;
        }
    }

    private static class PrefixHandler extends AbstractSaxExpressionHandler<Resource> {

        PrefixHandler(final ProtocolVersion version) {
            super(new PrefixExpression(version));
        }

        @Override
        public Resource getValue() {
            return ((PrefixExpression) expressions[0]).getValue();
        }
    }

    static Resource read(final InputStream inputStream, final ProtocolVersion version) throws IOException {
        final Resource prefix;
        try {
            final PrefixHandler handler = new PrefixHandler(version);
            prefix = handler.parse(inputStream);
        } catch (final ParserConfigurationException | SAXException e) {
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
