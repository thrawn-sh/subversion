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
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.subversion.LogEntry;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.ReadOnlyRepositoryInternal;
import de.shadowhunt.subversion.internal.XmlConstants;
import de.shadowhunt.subversion.internal.jaxb.LogEntryParser;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

public class LogOperationHttpv1 extends AbstractRepositoryBaseOperation<ReadOnlyRepositoryInternal, List<LogEntry>> {

    private final Revision endRevision;

    private final int limit;

    private final QualifiedResource qualifiedResource;

    private final Revision startRevision;

    private final boolean stopOnCopy;

    public LogOperationHttpv1(final ReadOnlyRepositoryInternal repository, final QualifiedResource qualifiedResource, final Revision startRevision, final Revision endRevision, final int limit, final boolean stopOnCopy) {
        super(repository, HttpStatus.SC_OK);
        this.qualifiedResource = qualifiedResource;
        this.startRevision = startRevision;
        this.endRevision = endRevision;
        this.limit = limit;
        this.stopOnCopy = stopOnCopy;
    }

    @Override
    protected HttpUriRequest createRequest() throws IOException {
        final URI uri = repository.getRequestUri(qualifiedResource);
        final DavTemplateRequest request = new DavTemplateRequest("REPORT", uri);

        final HttpEntity entity = createRequestBody();
        request.setEntity(entity);
        return request;
    }

    protected HttpEntity createRequestBody() throws IOException {
        try (final Writer body = new StringBuilderWriter()) {
            try {
                final XMLStreamWriter writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(body);
                writer.writeStartDocument(XmlConstants.ENCODING, XmlConstants.VERSION_1_0);
                writer.writeStartElement("log-report");
                writer.writeDefaultNamespace(XmlConstants.SVN_NAMESPACE);
                writer.writeStartElement("start-revision");
                final String stringString = startRevision.toString();
                writer.writeCharacters(stringString);
                writer.writeEndElement(); // start-revision
                writer.writeStartElement("end-revision");
                final String endString = endRevision.toString();
                writer.writeCharacters(endString);
                writer.writeEndElement(); // end-revision
                if (stopOnCopy) {
                    writer.writeEmptyElement("strict-node-history");
                }
                if (limit > 0) {
                    writer.writeStartElement("limit");
                    final String limitValue = Integer.toString(limit);
                    writer.writeCharacters(limitValue);
                    writer.writeEndElement(); // limit
                }
                writer.writeEmptyElement("discover-changed-paths");
                writer.writeEmptyElement("all-revprops");
                writer.writeEmptyElement("path");
                writer.writeEndElement(); // log-report
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
    protected List<LogEntry> processResponse(final HttpResponse response) throws IOException {
        try (final InputStream content = getContent(response)) {
            return LogEntryParser.parse(content);
        }
    }

}
