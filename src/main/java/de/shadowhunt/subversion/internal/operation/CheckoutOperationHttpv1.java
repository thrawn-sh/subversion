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
package de.shadowhunt.subversion.internal.operation;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.RepositoryInternal;
import de.shadowhunt.subversion.internal.XmlConstants;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

public class CheckoutOperationHttpv1 extends AbstractRepositoryBaseOperation<RepositoryInternal, Void> {

    private final QualifiedResource qualifiedResource;

    private final QualifiedResource qualifiedTransactionResource;

    public CheckoutOperationHttpv1(final RepositoryInternal repository, final QualifiedResource qualifiedResource, final QualifiedResource qualifiedTransactionResource) {
        super(repository, HttpStatus.SC_CREATED);
        this.qualifiedResource = qualifiedResource;
        this.qualifiedTransactionResource = qualifiedTransactionResource;
    }

    @Override
    protected HttpUriRequest createRequest() throws IOException {
        final URI uri = repository.getRequestUri(qualifiedResource);
        final DavTemplateRequest request = new DavTemplateRequest("CHECKOUT", uri);

        final HttpEntity entity = createRequestBody();
        request.setEntity(entity);
        return request;
    }

    protected HttpEntity createRequestBody() throws IOException {
        try (final Writer body = new StringBuilderWriter()) {
            try {
                final XMLStreamWriter writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(body);
                writer.writeStartDocument(XmlConstants.ENCODING, XmlConstants.VERSION_1_0);
                writer.writeStartElement("checkout");
                writer.writeDefaultNamespace(XmlConstants.DAV_NAMESPACE);
                writer.writeStartElement("activity-set");
                writer.writeStartElement("href");
                final URI uri = repository.getRequestUri(qualifiedTransactionResource);
                final String uriValue = uri.toASCIIString();
                writer.writeCData(uriValue);
                writer.writeEndElement(); // href
                writer.writeEndElement(); // activity-set
                writer.writeEmptyElement("apply-to-version");
                writer.writeEndElement(); // checkout
                writer.writeEndDocument();
                writer.close();
            } catch (final XMLStreamException e) {
                throw new IOException("could not create request body", e);
            }

            final String payload = body.toString();
            return new StringEntity(payload, CONTENT_TYPE_XML);
        }
    }

    @Override
    protected Void processResponse(final HttpResponse response) throws IOException {
        // nothing to do
        return null;
    }

}
