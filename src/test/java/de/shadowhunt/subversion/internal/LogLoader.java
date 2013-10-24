package de.shadowhunt.subversion.internal;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
				current.setUser(getText());
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

	public static List<Log> load(final Resource resource, final Revision revision) throws Exception {
		final File file = new File(ROOT, resolve(revision) + resource.getValue() + SUFFIX);

		final SAXParser saxParser = BasicHandler.FACTORY.newSAXParser();
		final LogHandler handler = new LogHandler();

		saxParser.parse(file, handler);
		return handler.getLogs();
	}
}
