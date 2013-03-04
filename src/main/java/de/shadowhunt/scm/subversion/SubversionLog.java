package de.shadowhunt.scm.subversion;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class SubversionLog {

	static class SubversionLogHandler extends BasicHandler {

		private SubversionLog current = null;

		private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");

		private final List<SubversionLog> logs = new ArrayList<SubversionLog>();

		@Override
		public void endElement(final String uri, final String localName, final String qName) throws SAXException {
			final String name = getNameFromQName(qName);

			if ("log-item".equals(name)) {
				logs.add(current);
				current = null;
				return;
			}

			if (current == null) {
				return;
			}

			if ("comment".equals(name)) {
				current.setComment(getText());
				return;
			}

			if ("creator-displayname".equals(name)) {
				current.setUser(getText());
				return;
			}

			if ("date".equals(name)) {
				try {
					current.setDate(format.parse(getText()));
				} catch (final ParseException e) {
					throw new SAXException("date has unexpected format", e);
				}
				return;
			}

			if ("version-name".equals(name)) {
				current.setVersion(getText());
				return;
			}
		}

		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
			clearText();

			final String name = getNameFromQName(qName);

			if ("log-item".equals(name)) {
				current = new SubversionLog();
				return;
			}
		}

		public List<SubversionLog> getLogs() {
			return logs;
		}
	}

	public static List<SubversionLog> read(final InputStream in) throws Exception {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(false);
		factory.setValidating(false);
		final SAXParser saxParser = factory.newSAXParser();
		final SubversionLogHandler handler = new SubversionLogHandler();

		saxParser.parse(in, handler);
		return handler.getLogs();
	}

	private String comment;

	private Date date;

	private String user;

	private String version;

	SubversionLog() {
		// prevent direct instantiation
	}

	public String getComment() {
		return comment;
	}

	public Date getDate() {
		return (date == null) ? null : new Date(date.getTime());
	}

	public String getUser() {
		return user;
	}

	public String getVersion() {
		return version;
	}

	public void setComment(final String comment) {
		this.comment = comment;
	}

	public void setDate(final Date date) {
		this.date = (date == null) ? null : new Date(date.getTime());
	}

	public void setUser(final String user) {
		this.user = user;
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "SubversionLog [comment=" + comment + ", date=" + date + ", user=" + user + ", version=" + version + "]";
	}
}
