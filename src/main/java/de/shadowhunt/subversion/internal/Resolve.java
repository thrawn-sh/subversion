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
            final int intVersion = Integer.parseInt(version);
            final Revision revision = Revision.create(intVersion);

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
     * @param inputStream
     *            {@link InputStream} from which the status information is read (Note: will not be closed)
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

    private Resolve(final Resource resource, final Revision revision) {
        this.resource = resource;
        this.revision = revision;
    }

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
