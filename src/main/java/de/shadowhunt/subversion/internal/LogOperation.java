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

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.subversion.Log;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

class LogOperation extends AbstractOperation<List<Log>> {

    private final Revision end;

    private final int limit;

    private final QualifiedResource resource;

    private final Revision start;

    private final boolean stopOnCopy;

    LogOperation(final URI repository, final QualifiedResource resource, final Revision start, final Revision end, final int limit, final boolean stopOnCopy) {
        super(repository);
        this.resource = resource;
        this.start = start;
        this.end = end;
        this.limit = limit;
        this.stopOnCopy = stopOnCopy;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final Writer body = new StringBuilderWriter();
        try {
            final XMLStreamWriter writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(body);
            writer.writeStartDocument(XmlConstants.ENCODING, XmlConstants.VERSION_1_0);
            writer.writeStartElement("log-report");
            writer.writeDefaultNamespace(XmlConstants.SVN_NAMESPACE);
            writer.writeStartElement("start-revision");
            writer.writeCharacters(start.toString());
            writer.writeEndElement(); // start-revision
            writer.writeStartElement("end-revision");
            writer.writeCharacters(end.toString());
            writer.writeEndElement(); // end-revision
            if (stopOnCopy) {
                writer.writeEmptyElement("strict-node-history");
            }
            if (limit > 0) {
                writer.writeStartElement("limit");
                writer.writeCharacters(Integer.toString(limit));
                writer.writeEndElement(); // limit
            }
            writer.writeEmptyElement("discover-changed-paths");
            writer.writeEmptyElement("all-revprops");
            writer.writeEmptyElement("path");
            writer.writeEndElement(); // log-report
            writer.writeEndDocument();
            writer.close();
        } catch (final XMLStreamException e) {
            throw new SubversionException("could not create request body", e);
        }

        final URI uri = URIUtils.appendResources(repository, resource);
        final DavTemplateRequest request = new DavTemplateRequest("REPORT", uri);
        request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
        return request;
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        return HttpStatus.SC_OK == statusCode;
    }

    @Override
    protected List<Log> processResponse(final HttpResponse response) throws IOException {
        return LogImplReader.read(getContent(response));
    }
}
