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

import javax.xml.namespace.QName;

import org.xml.sax.Attributes;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.xml.AbstractSaxExpression;
import de.shadowhunt.subversion.xml.AbstractSaxExpressionHandler;

final class Resolve {

    private static class ResolveExpression extends AbstractSaxExpression<Resolve> {

        private static QName[] PATH;

        static {
            final QName[] path = new QName[2];
            path[0] = new QName(XmlConstants.SVN_NAMESPACE, "get-locations-report");
            path[1] = new QName(XmlConstants.SVN_NAMESPACE, "location");
            PATH = path;
        }

        private Resolve entry = null;

        ResolveExpression() {
            super(PATH);
        }

        @Override
        public Resolve getValue() {
            return entry;
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            // nothing to do
        }

        @Override
        protected void processStart(final String nameSpaceUri, final String localName, final Attributes attributes) {
            entry = new Resolve();

            final String version = attributes.getValue("rev");
            entry.setRevision(Revision.create(Integer.parseInt(version)));
            final String path = attributes.getValue("path");
            entry.setResource(Resource.create(path));
        }

        @Override
        protected void resetHandler() {
            entry = null;
        }
    }

    private static class ResolveHandler extends AbstractSaxExpressionHandler<Resolve> {

        ResolveHandler() {
            super(new ResolveExpression());
        }

        @Override
        public Resolve getValue() {
            return ((ResolveExpression) expressions[0]).getValue();
        }
    }

    /**
     * Reads log information for a resource from the given {@link InputStream}
     *
     * @param inputStream {@link InputStream} from which the status information is read (Note: will not be closed)
     *
     * @return {@link LogImpl} for the resource
     */
    static Resolve read(final InputStream inputStream) {
        final Resolve resolve;
        try {
            final AbstractSaxExpressionHandler<Resolve> handler = new ResolveHandler();
            resolve = handler.parse(inputStream);
        } catch (final Exception e) {
            throw new SubversionException("Invalid server response: could not parse response", e);
        }

        if (resolve == null) {
            throw new SubversionException("Invalid server response: could not parse response");
        }
        return resolve;
    }

    private Resource resource = null;

    private Revision revision = null;

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

    void setResource(final Resource resource) {
        this.resource = resource;
    }

    void setRevision(final Revision revision) {
        this.revision = revision;
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
