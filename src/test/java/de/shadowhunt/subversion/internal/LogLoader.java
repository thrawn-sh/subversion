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

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.SAXParser;

import de.shadowhunt.subversion.Log;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public final class LogLoader extends AbstractBaseLoader {

    static class LogHandler extends BasicHandler {

        private static TimeZone ZULU = TimeZone.getTimeZone("ZULU");

        private LogImpl current = null;

        private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);

        private final List<Log> logs = new ArrayList<>();

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            if ("logentry".equals(localName)) {
                logs.add(current);
                current = null;
                return;
            }

            if (current == null) {
                return;
            }

            if ("msg".equals(localName)) {
                current.setMessage(getText());
                return;
            }

            if ("author".equals(localName)) {
                current.setAuthor(getText());
                return;
            }

            if ("date".equals(localName)) {
                try {
                    format.setTimeZone(ZULU);
                    final Date date = format.parse(getText());
                    current.setDate(date);
                } catch (final ParseException e) {
                    throw new SAXException("date has unexpected format", e);
                }
                return;
            }
        }

        List<Log> getLogs() {
            return logs;
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
            clearText();

            if ("logentry".equals(localName)) {
                current = new LogImpl();
                final String revision = attributes.getValue("revision");
                current.setRevision(Revision.create(Integer.parseInt(revision)));
                return;
            }
        }
    }

    public static final String SUFFIX = ".log";

    LogLoader(final File root, final Resource base) {
        super(root, base);
    }

    public List<Log> load(final Resource resource, final Revision start, final Revision end, final int limit) throws Exception {
        final Revision high;
        final Revision low;
        final boolean reverse;
        if (start.compareTo(end) > 0) {
            high = start;
            low = end;
            reverse = false;
        } else {
            high = end;
            low = start;
            reverse = true;
        }

        final File file = new File(root, resolve(high) + base.getValue() + resource.getValue() + SUFFIX);

        final SAXParser saxParser = BasicHandler.FACTORY.newSAXParser();
        final LogHandler handler = new LogHandler();

        saxParser.parse(file, handler);
        final List<Log> logs = handler.getLogs();
        final Iterator<Log> it = logs.iterator();
        while (it.hasNext()) {
            final Log log = it.next();
            final Revision revision = log.getRevision();
            if ((high.compareTo(revision) < 0) || (low.compareTo(revision) > 0)) {
                it.remove();
            }
        }

        if (reverse) {
            Collections.reverse(logs);
        }

        if (limit > 0) {
            return logs.subList(0, limit);
        }
        return logs;
    }
}
