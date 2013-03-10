package de.shadowhunt.scm.subversion;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class SubversionLog {

	static class SubversionLogHandler extends BasicHandler {

		private SubversionLog current = null;

		private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);

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
				final long version = Long.parseLong(getText());
				current.setVersion(version);
				return;
			}
		}

		public List<SubversionLog> getLogs() {
			return logs;
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

	private long version;

	SubversionLog() {
		// prevent direct instantiation
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final SubversionLog other = (SubversionLog) obj;
		if (comment == null) {
			if (other.comment != null) {
				return false;
			}
		} else if (!comment.equals(other.comment)) {
			return false;
		}
		if (date == null) {
			if (other.date != null) {
				return false;
			}
		} else if (!date.equals(other.date)) {
			return false;
		}
		if (user == null) {
			if (other.user != null) {
				return false;
			}
		} else if (!user.equals(other.user)) {
			return false;
		}
		if (version != other.version) {
			return false;
		}
		return true;
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

	public long getVersion() {
		return version;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((comment == null) ? 0 : comment.hashCode());
		result = (prime * result) + ((date == null) ? 0 : date.hashCode());
		result = (prime * result) + ((user == null) ? 0 : user.hashCode());
		result = (prime * result) + (int) (version ^ (version >>> 32));
		return result;
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

	public void setVersion(final long version) {
		this.version = version;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("SubversionLog [comment=");
		builder.append(comment);
		builder.append(", date=");
		builder.append(date);
		builder.append(", user=");
		builder.append(user);
		builder.append(", version=");
		builder.append(version);
		builder.append("]");
		return builder.toString();
	}
}
