/**
 * Copyright (C) 2013-2017 shadowhunt (dev@shadowhunt.de)
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
