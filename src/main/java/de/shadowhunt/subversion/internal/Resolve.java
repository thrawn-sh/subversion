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
import java.util.Optional;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.xml.AbstractSaxExpression;
import de.shadowhunt.subversion.xml.AbstractSaxExpressionHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

final class Resolve {

    private Resolve(final Resource resource, final Revision revision) {
        this.resource = resource;
        this.revision = revision;
    }

    private static class ResolveExpression extends AbstractSaxExpression<Resolve> {

        private static final QName[] PATH = { //
                new QName(XmlConstants.SVN_NAMESPACE, "get-locations-report"), //
                new QName(XmlConstants.SVN_NAMESPACE, "location") //
        };

        private Resolve entry;

        ResolveExpression() {
            super(PATH);
        }

        @Override
        public Optional<Resolve> getValue() {
            return Optional.ofNullable(entry);
        }

        @Override
        protected void processStart(final String nameSpaceUri, final String localName, final Attributes attributes) {
            final String path = attributes.getValue("path");
            final Resource resource = Resource.create(path);

            final String version = attributes.getValue("rev");
            final Revision revision = Revision.create(Integer.parseInt(version));

            entry = new Resolve(resource, revision);
        }

        @Override
        public void resetHandler() {
            super.resetHandler();
            entry = null;
        }
    }

    private static class ResolveHandler extends AbstractSaxExpressionHandler<Resolve> {

        ResolveHandler() {
            super(new ResolveExpression());
        }

        @Override
        public Optional<Resolve> getValue() {
            return ((ResolveExpression) expressions[0]).getValue();
        }
    }

    /**
     * Reads log information for a resource from the given {@link InputStream}.
     *
     * @param inputStream {@link InputStream} from which the status information is read (Note: will not be closed)
     *
     * @return {@link LogImpl} for the resource
     */
    static Resolve read(final InputStream inputStream) throws IOException {
        final ResolveHandler handler = new ResolveHandler();
        final Optional<Resolve> resolve;
        try {
            resolve = handler.parse(inputStream);
        } catch (final ParserConfigurationException | SAXException e) {
            throw new SubversionException("Invalid server response: could not parse response", e);
        }

        return resolve.orElseThrow(() -> new SubversionException("Invalid server response: could not parse response"));
    }

    private final Resource resource;

    private final Revision revision;

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Resolve other = (Resolve) obj;
        if (resource == null) {
            if (other.resource != null) {
                return false;
            }
        } else if (!resource.equals(other.resource)) {
            return false;
        }
        if (revision == null) {
            if (other.revision != null) {
                return false;
            }
        } else if (!revision.equals(other.revision)) {
            return false;
        }
        return true;
    }

    public Resource getResource() {
        return resource;
    }

    public Revision getRevision() {
        return revision;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((resource == null) ? 0 : resource.hashCode());
        result = (prime * result) + ((revision == null) ? 0 : revision.hashCode());
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Resolve [resource=");
        builder.append(resource);
        builder.append(", revision=");
        builder.append(revision);
        builder.append(']');
        return builder.toString();
    }
}
