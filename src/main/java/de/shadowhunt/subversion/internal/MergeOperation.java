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

import java.io.StringWriter;
import java.net.URI;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.SubversionException;

public class MergeOperation extends AbstractVoidOperation {

    private final Set<Info> infos;

    private final Resource resource;

    public MergeOperation(final URI repository, final Resource resource, final Set<Info> infos) {
        super(repository);
        this.resource = resource;
        this.infos = infos;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final DavTemplateRequest request = new DavTemplateRequest("MERGE", repository);
        request.addHeader("X-SVN-Options", "release-locks");

        final StringWriter body = new StringWriter();
        try {
            final XMLStreamWriter writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(body);
            writer.writeStartDocument(XmlConstants.ENCODING, XmlConstants.VERSION_1_0);
            writer.writeStartElement("merge");
            writer.writeDefaultNamespace(XmlConstants.DAV_NAMESPACE);
            writer.writeStartElement("source");
            writer.writeStartElement("href");
            writer.writeCharacters(repository.getPath());
            writer.writeCharacters(resource.getValue());
            writer.writeEndElement(); // href
            writer.writeEndElement(); // source
            writer.writeEmptyElement("no-auto-merge");
            writer.writeEmptyElement("no-checkout");
            writer.writeStartElement("prop");
            writer.writeEmptyElement("checked-in");
            writer.writeEmptyElement("version-name");
            writer.writeEmptyElement("resourcetype");
            writer.writeEmptyElement("creationdate");
            writer.writeEmptyElement("creator-displayname");
            writer.writeEndElement(); // prop
            if (!infos.isEmpty()) {
                writer.setPrefix(XmlConstants.SVN_PREFIX, XmlConstants.SVN_NAMESPACE);
                writer.writeStartElement(XmlConstants.SVN_NAMESPACE, "lock-token-list");
                writer.writeNamespace(XmlConstants.SVN_PREFIX, XmlConstants.SVN_NAMESPACE);
                for (final Info info : infos) {
                    final String lockToken = info.getLockToken();
                    assert (lockToken != null) : "must not be null";
                    final Resource infoResource = info.getResource();

                    writer.writeStartElement(XmlConstants.SVN_NAMESPACE, "lock");
                    writer.writeStartElement(XmlConstants.SVN_NAMESPACE, "lock-path");
                    writer.writeCharacters(infoResource.getValueWithoutLeadingSeparator());
                    writer.writeEndElement(); // lock-path
                    writer.writeStartElement(XmlConstants.SVN_NAMESPACE, "lock-token");
                    writer.writeCharacters(lockToken);
                    writer.writeEndElement(); // lock-token
                    writer.writeEndElement(); // lock
                }
                writer.writeEndElement(); // lock-token-list
            }
            writer.writeEndElement(); // merge
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
        return HttpStatus.SC_OK == statusCode;
    }
}
