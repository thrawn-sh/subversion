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
package de.shadowhunt.subversion.internal.jaxb;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.transform.stream.StreamSource;

import de.shadowhunt.subversion.LogEntry;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.internal.LogEntryImpl;
import de.shadowhunt.subversion.internal.XmlConstants;
import de.shadowhunt.subversion.internal.jaxb.converter.CreatedDateAdapter;
import de.shadowhunt.subversion.internal.jaxb.converter.MessageAdapter;
import de.shadowhunt.subversion.internal.jaxb.converter.RevisionAdapter;

@XmlRootElement(namespace = XmlConstants.SVN_NAMESPACE, name = "log-report")
@XmlAccessorType(XmlAccessType.FIELD)
public final class LogEntryParser {

    static class LogItemJaxb {

        @XmlElement(namespace = XmlConstants.DAV_NAMESPACE, name = "creator-displayname", required = true)
        String author;

        @XmlElement(namespace = XmlConstants.SVN_NAMESPACE, name = "date", required = true)
        @XmlJavaTypeAdapter(CreatedDateAdapter.class)
        Date date;

        @XmlElement(namespace = XmlConstants.DAV_NAMESPACE, name = "comment")
        @XmlJavaTypeAdapter(MessageAdapter.class)
        String message;

        @XmlElement(namespace = XmlConstants.DAV_NAMESPACE, name = "version-name", required = true)
        @XmlJavaTypeAdapter(RevisionAdapter.class)
        Revision revision;
    }

    private static final JAXBContext CONTEXT;

    static {
        try {
            CONTEXT = JAXBContext.newInstance(LogEntryParser.class);
        } catch (final JAXBException e) {
            throw new SubversionException("can not create context", e);
        }
    }

    private static List<LogEntry> convert(final List<LogItemJaxb> entries) {
        final int size = entries.size();
        final List<LogEntry> result = new ArrayList<>(size);
        for (final LogItemJaxb entry : entries) {
            final LogEntryImpl impl = new LogEntryImpl();
            impl.setAuthor(entry.author);
            impl.setDate(entry.date);
            impl.setMessage(entry.message);
            impl.setRevision(entry.revision);
            result.add(impl);
        }
        return result;
    }

    public static List<LogEntry> parse(final InputStream input) throws IOException {
        try {
            final Unmarshaller unmarshaller = CONTEXT.createUnmarshaller();
            final StreamSource source = new StreamSource(input);
            final LogEntryParser parser = (LogEntryParser) unmarshaller.unmarshal(source);
            if (parser.logEntries == null) {
                return Collections.emptyList();
            }
            return convert(parser.logEntries);
        } catch (final JAXBException e) {
            throw new IOException("can not read input", e);
        }
    }

    @XmlElement(namespace = XmlConstants.SVN_NAMESPACE, name = "log-item")
    private List<LogItemJaxb> logEntries;
}
