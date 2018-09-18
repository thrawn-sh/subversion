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
package de.shadowhunt.subversion.internal.httpv1;

import java.io.Writer;
import java.net.URI;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.internal.AbstractVoidOperation;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.URIUtils;
import de.shadowhunt.subversion.internal.XmlConstants;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

class CheckoutOperation extends AbstractVoidOperation {

    private final QualifiedResource resource;

    private final QualifiedResource transaction;

    CheckoutOperation(final URI repository, final QualifiedResource resource, final QualifiedResource transaction) {
        super(repository);
        this.resource = resource;
        this.transaction = transaction;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final Writer body = new StringBuilderWriter();
        try {
            final XMLStreamWriter writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(body);
            writer.writeStartDocument(XmlConstants.ENCODING, XmlConstants.VERSION_1_0);
            writer.writeStartElement("checkout");
            writer.writeDefaultNamespace(XmlConstants.DAV_NAMESPACE);
            writer.writeStartElement("activity-set");
            writer.writeStartElement("href");
            final URI transactionUri = URIUtils.appendResources(repository, transaction);
            final String transactionUriValue = transactionUri.toString();
            writer.writeCData(transactionUriValue);
            writer.writeEndElement(); // href
            writer.writeEndElement(); // activity-set
            writer.writeEmptyElement("apply-to-version");
            writer.writeEndElement(); // checkout
            writer.writeEndDocument();
            writer.close();
        } catch (final XMLStreamException e) {
            throw new SubversionException("could not create request body", e);
        }

        final URI uri = URIUtils.appendResources(repository, resource);
        final DavTemplateRequest request = new DavTemplateRequest("CHECKOUT", uri);
        final String payload = body.toString();
        final StringEntity entity = new StringEntity(payload, CONTENT_TYPE_XML);
        request.setEntity(entity);
        return request;
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        return HttpStatus.SC_CREATED == statusCode;
    }
}
