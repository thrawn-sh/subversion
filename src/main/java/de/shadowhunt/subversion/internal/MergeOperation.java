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
            final String path = repository.getPath();
            final String resourceValue = resource.getValue();
            writer.writeCData(path + resourceValue);
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
                        if (Resource.ROOT.equals(base)) {
                            final String valueWithoutLeadingSeparator = infoResource.getValueWithoutLeadingSeparator();
                            writer.writeCData(valueWithoutLeadingSeparator);
                        } else {
                            final String valueWithoutLeadingSeparator = base.getValueWithoutLeadingSeparator();
                            final String value = infoResource.getValue();
                            writer.writeCData(valueWithoutLeadingSeparator + value);
                        }
                        writer.writeEndElement(); // lock-path
                        writer.writeStartElement(XmlConstants.SVN_NAMESPACE, "lock-token");
                        final LockToken token = lockToken.get();
                        final String tokenValue = token.toString();
                        writer.writeCharacters(tokenValue);
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

        final String payload = body.toString();
        final StringEntity entity = new StringEntity(payload, CONTENT_TYPE_XML);
        request.setEntity(entity);
        return request;
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        return HttpStatus.SC_OK == statusCode;
    }
}
