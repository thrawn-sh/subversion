/*
 * #%L
 * Shadowhunt Subversion
 * %%
 * Copyright (C) 2013 shadowhunt
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.shadowhunt.subversion.Log;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;

public class LogLoader extends BaseLoader {

	static class LogHandler extends BasicHandler {

		private static TimeZone ZULU = TimeZone.getTimeZone("ZULU");

		private LogImpl current = null;

		private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);

		private final List<Log> logs = new ArrayList<Log>();

		@Override
		public void endElement(final String uri, final String localName, final String qName) throws SAXException {
			final String name = getNameFromQName(qName);

			if ("logentry".equals(name)) {
				logs.add(current);
				current = null;
				return;
			}

			if (current == null) {
				return;
			}

			if ("msg".equals(name)) {
				current.setMessage(getText());
				return;
			}

			if ("author".equals(name)) {
				current.setAuthor(getText());
				return;
			}

			if ("date".equals(name)) {
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

			final String name = getNameFromQName(qName);

			if ("logentry".equals(name)) {
				current = new LogImpl();
				final String revision = attributes.getValue("revision");
				current.setRevision(Revision.create(Integer.parseInt(revision)));
				return;
			}
		}
	}

	private static final String SUFFIX = ".log";

	public static List<Log> load(final Resource resource, final Revision start, final Revision end, final int limit) throws Exception {
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

		final File file = new File(ROOT, resolve(high) + resource.getValue() + SUFFIX);

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
