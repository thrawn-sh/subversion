/**
 * Copyright (C) 2013-2015 shadowhunt (dev@shadowhunt.de)
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
import java.util.Optional;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.LockToken;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.SubversionException;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

public class MergeOperation extends AbstractVoidOperation {

    private final Resource base;

    private final Set<Info> infoSet;

    private final boolean releaseLocks;

    private final QualifiedResource resource;

    public MergeOperation(final URI repository, final QualifiedResource resource, final Set<Info> infoSet, final Resource base, final boolean releaseLocks) {
        super(repository);
        this.resource = resource;
        this.infoSet = infoSet;
        this.base = base;
        this.releaseLocks = releaseLocks;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final DavTemplateRequest request = new DavTemplateRequest("MERGE", repository);

        if (releaseLocks) {
            request.addHeader("X-SVN-Options", "release-locks");
        }

        final Writer body = new StringBuilderWriter();
        try {
            final XMLStreamWriter writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(body);
            writer.writeStartDocument(XmlConstants.ENCODING, XmlConstants.VERSION_1_0);
            writer.writeStartElement("merge");
            writer.writeDefaultNamespace(XmlConstants.DAV_NAMESPACE);
            writer.writeStartElement("source");
            writer.writeStartElement("href");
            writer.writeCData(repository.getPath() + resource.getValue());
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
            if (!infoSet.isEmpty()) {
                writer.setPrefix(XmlConstants.SVN_PREFIX, XmlConstants.SVN_NAMESPACE);
                writer.writeStartElement(XmlConstants.SVN_NAMESPACE, "lock-token-list");
                writer.writeNamespace(XmlConstants.SVN_PREFIX, XmlConstants.SVN_NAMESPACE);
                for (final Info info : infoSet) {
                    final Optional<LockToken> lockToken = info.getLockToken();
                    if (lockToken.isPresent()) {
                        final Resource infoResource = info.getResource();

                        writer.writeStartElement(XmlConstants.SVN_NAMESPACE, "lock");
                        writer.writeStartElement(XmlConstants.SVN_NAMESPACE, "lock-path");
                        writer.writeCData(base.getValueWithoutLeadingSeparator() + infoResource.getValue());
                        writer.writeEndElement(); // lock-path
                        writer.writeStartElement(XmlConstants.SVN_NAMESPACE, "lock-token");
                        writer.writeCharacters(lockToken.get().toString());
                        writer.writeEndElement(); // lock-token
                        writer.writeEndElement(); // lock
                    }
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
