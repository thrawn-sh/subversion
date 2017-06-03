/**
 * Copyright (C) 2013-2017 shadowhunt (dev@shadowhunt.de)
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
import java.net.URLDecoder;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import de.shadowhunt.subversion.Repository.ProtocolVersion;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.xml.AbstractSaxExpression;
import de.shadowhunt.subversion.xml.AbstractSaxExpressionHandler;
import org.xml.sax.SAXException;

final class ProbeReader {

    private static class CollectionExpression extends AbstractSaxExpression<Probe> {

        private static final QName[] PATH = { //
                new QName(XmlConstants.DAV_NAMESPACE, "options-response"), //
                new QName(XmlConstants.DAV_NAMESPACE, "activity-collection-set"), //
                new QName(XmlConstants.DAV_NAMESPACE, "href") //
        };

        private static final Pattern PATH_PATTERN = Pattern.compile(Resource.SEPARATOR);

        private Probe probe;

        private final ProtocolVersion version;

        CollectionExpression(final ProtocolVersion version) {
            super(PATH);
            this.version = version;
        }

        private String decodePath(final String path) {
            try {
                return URLDecoder.decode(path, "UTF-8");
            } catch (final Exception e) {
                throw new SubversionException("can not decode path: " + path, e);
            }
        }

        @Override
        public Optional<Probe> getValue() {
            return Optional.ofNullable(probe);
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            if ((ProtocolVersion.HTTP_V1 == version) || (ProtocolVersion.HTTP_V2 == version)) {
                final String unescapedPath = decodePath(text);
                // .../${svn}/act/
                // ^^^^^^ <- prefix
                // ^^^ <- part of baseUri
                final String[] segments = PATH_PATTERN.split(unescapedPath);
                final int prefixIndex = segments.length - 2;
                final Resource prefix = Resource.create(segments[prefixIndex]);

                final StringBuilder sb = new StringBuilder();
                for (int i = 0; i < prefixIndex; i++) {
                    sb.append(Resource.SEPARATOR_CHAR);
                    sb.append(segments[i]);
                }
                final Resource basePath = Resource.create(sb.toString());
                probe = new Probe(basePath, prefix);
            }
        }

        @Override
        public void resetHandler() {
            super.resetHandler();
            probe = null;
        }
    }

    private static class ProbeHandler extends AbstractSaxExpressionHandler<Probe> {

        ProbeHandler(final ProtocolVersion version) {
            super(new CollectionExpression(version));
        }

        @Override
        public Optional<Probe> getValue() {
            return ((CollectionExpression) expressions[0]).getValue();
        }
    }

    static Probe read(final InputStream inputStream, final ProtocolVersion version) throws IOException {
        try {
            final ProbeHandler handler = new ProbeHandler(version);
            final Optional<Probe> probe = handler.parse(inputStream);
            return probe.orElseThrow(() -> new SubversionException("Invalid server response: could not be parsed"));
        } catch (final ParserConfigurationException | SAXException e) {
            throw new SubversionException("Invalid server response: could not parse response", e);
        }
    }

    private ProbeReader() {
        // prevent instantiation
    }

}
