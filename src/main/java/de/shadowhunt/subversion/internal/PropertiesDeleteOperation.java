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

import java.io.Writer;
import java.net.URI;
import java.util.Arrays;

import javax.annotation.Nullable;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.SubversionException;

class PropertiesDeleteOperation extends AbstractVoidOperation {

    private final Info info;

    private final ResourceProperty[] properties;

    private final Resource resource;

    PropertiesDeleteOperation(final URI repository, final Resource resource, @Nullable final Info info, final ResourceProperty[] properties) {
        super(repository);
        this.resource = resource;
        this.info = info;
        this.properties = Arrays.copyOf(properties, properties.length);
    }

    @Override
    protected HttpUriRequest createRequest() {
        final URI uri = URIUtils.createURI(repository, resource);
        final DavTemplateRequest request = new DavTemplateRequest("PROPPATCH", uri);

        if ((info != null) && info.isLocked()) {
            final URI lockTarget = URIUtils.createURI(repository, info.getResource());
            request.addHeader("If", '<' + lockTarget.toASCIIString() + "> (<" + info.getLockToken() + ">)");
        }

        final Writer body = new StringBuilderWriter();
        try {
            final XMLStreamWriter writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(body);
            writer.writeStartDocument(XmlConstants.ENCODING, XmlConstants.VERSION_1_0);
            writer.writeStartElement("propertyupdate");
            writer.writeDefaultNamespace(XmlConstants.DAV_NAMESPACE);
            writer.setPrefix(XmlConstants.CUSTOM_PROPERTIES_PREFIX, XmlConstants.CUSTOM_PROPERTIES_NAMESPACE);
            writer.writeNamespace(XmlConstants.CUSTOM_PROPERTIES_PREFIX, XmlConstants.CUSTOM_PROPERTIES_NAMESPACE);
            writer.setPrefix(XmlConstants.SVN_PROPERTIES_PREFIX, XmlConstants.SVN_PROPERTIES_NAMESPACE);
            writer.writeNamespace(XmlConstants.SVN_PROPERTIES_PREFIX, XmlConstants.SVN_PROPERTIES_NAMESPACE);
            writer.setPrefix(XmlConstants.SVN_DAV_PREFIX, XmlConstants.SVN_DAV_NAMESPACE);
            writer.writeNamespace(XmlConstants.SVN_DAV_PREFIX, XmlConstants.SVN_DAV_NAMESPACE);
            writer.writeStartElement("remove");
            writer.writeStartElement("prop");
            for (final ResourceProperty property : properties) {
                final String prefix = property.getType().getPrefix();
                writer.writeEmptyElement(prefix, property.getName());
            }
            writer.writeEndElement(); // prop
            writer.writeEndElement(); // remove
            writer.writeEndElement(); // propertyupdate
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
        return HttpStatus.SC_MULTI_STATUS == statusCode;
    }

}
