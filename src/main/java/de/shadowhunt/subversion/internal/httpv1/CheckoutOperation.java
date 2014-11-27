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
package de.shadowhunt.subversion.internal.httpv1;

import java.io.Writer;
import java.net.URI;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.internal.AbstractVoidOperation;
import de.shadowhunt.subversion.internal.URIUtils;
import de.shadowhunt.subversion.internal.XmlConstants;

class CheckoutOperation extends AbstractVoidOperation {

    private final Resource resource;

    private final Resource transaction;

    CheckoutOperation(final URI repository, final Resource resource, final Resource transaction) {
        super(repository);
        this.resource = resource;
        this.transaction = transaction;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final URI uri = URIUtils.createURI(repository, resource);
        final DavTemplateRequest request = new DavTemplateRequest("CHECKOUT", uri);

        final Writer body = new StringBuilderWriter();
        try {
            final XMLStreamWriter writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(body);
            writer.writeStartDocument(XmlConstants.ENCODING, XmlConstants.VERSION_1_0);
            writer.writeStartElement("checkout");
            writer.writeDefaultNamespace(XmlConstants.DAV_NAMESPACE);
            writer.writeStartElement("activity-set");
            writer.writeStartElement("href");
            final URI transactionURI = URIUtils.createURI(repository, transaction);
            writer.writeCData(transactionURI.toString());
            writer.writeEndElement(); // href
            writer.writeEndElement(); // activity-set
            writer.writeEmptyElement("apply-to-version");
            writer.writeEndElement(); //checkout
            writer.writeEndDocument();
            writer.close();
        } catch (final XMLStreamException e) {
            throw new SubversionException("could not create request body", e);
        }

        request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
        return request;
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        return HttpStatus.SC_CREATED == statusCode;
    }
}
