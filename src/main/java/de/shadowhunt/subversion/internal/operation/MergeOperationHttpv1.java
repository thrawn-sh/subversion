/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2019 shadowhunt (dev@shadowhunt.de)
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
import java.util.Optional;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.LockToken;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.RepositoryInternal;
import de.shadowhunt.subversion.internal.XmlConstants;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

public class MergeOperationHttpv1 extends AbstractRepositoryBaseOperation<RepositoryInternal, Void> {

    private final Set<Info> infos;

    private final QualifiedResource qualifiedResource;

    private final boolean releaseLocks;

    public MergeOperationHttpv1(final RepositoryInternal repository, final QualifiedResource qualifiedResource, final Set<Info> infos, final boolean releaseLocks) {
        super(repository, HttpStatus.SC_OK);
        this.qualifiedResource = qualifiedResource;
        this.infos = infos;
        this.releaseLocks = releaseLocks;
    }

    @Override
    protected HttpUriRequest createRequest() throws IOException {
        final URI uri = repository.getBaseUri();
        final DavTemplateRequest request = new DavTemplateRequest("MERGE", uri);
        if (releaseLocks) {
            request.addHeader("X-SVN-Options", "release-locks");
        }

        final HttpEntity entity = createRequestBody();
        request.setEntity(entity);
        return request;
    }

    protected HttpEntity createRequestBody() throws IOException {
        try (final Writer body = new StringBuilderWriter()) {
            try {
                final XMLStreamWriter writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(body);
                writer.writeStartDocument(XmlConstants.ENCODING, XmlConstants.VERSION_1_0);
                writer.writeStartElement("merge");
                writer.writeDefaultNamespace(XmlConstants.DAV_NAMESPACE);
                writer.writeStartElement("source");
                writer.writeStartElement("href");
                final URI baseUri = repository.getBaseUri();
                final String path = baseUri.getPath();
                final String resourceValue = qualifiedResource.getValue();
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
                if (!infos.isEmpty()) {
                    writer.setPrefix(XmlConstants.SVN_PREFIX, XmlConstants.SVN_NAMESPACE);
                    writer.writeStartElement(XmlConstants.SVN_NAMESPACE, "lock-token-list");
                    writer.writeNamespace(XmlConstants.SVN_PREFIX, XmlConstants.SVN_NAMESPACE);
                    final Resource basePath = repository.getBasePath();
                    for (final Info info : infos) {
                        final Optional<LockToken> lockToken = info.getLockToken();
                        if (lockToken.isPresent()) {
                            final Resource infoResource = info.getResource();

                            writer.writeStartElement(XmlConstants.SVN_NAMESPACE, "lock");
                            writer.writeStartElement(XmlConstants.SVN_NAMESPACE, "lock-path");
                            if (Resource.ROOT.equals(basePath)) {
                                final String valueWithoutLeadingSeparator = infoResource.getValueWithoutLeadingSeparator();
                                writer.writeCData(valueWithoutLeadingSeparator);
                            } else {
                                final String valueWithoutLeadingSeparator = basePath.getValueWithoutLeadingSeparator();
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
