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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Container that holds all log information for a single revision of a resource
 */
public final class LogEntry {

	static class SubversionLogHandler extends BasicHandler {

		private LogEntry current = null;

		private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);

		private final List<LogEntry> logs = new ArrayList<LogEntry>();

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

			if ("added-path".equals(name) || "modified-path".equals(name)) {
				final Path path = Path.create(getText());
				current.setPath(path);
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
				final int revision = Integer.parseInt(getText());
				current.setRevision(Revision.create(revision));
				return;
			}
		}

		List<LogEntry> getLogs() {
			return logs;
		}

		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
			clearText();

			final String name = getNameFromQName(qName);

			if ("log-item".equals(name)) {
				current = new LogEntry();
				return;
			}
		}
	}

	/**
	 * Reads log information for a resource from the given {@link InputStream}
	 * @param in {@link InputStream} from which the status information is read (Note: will not be closed)
	 * @return {@link LogEntry} for the resource
	 */
	public static List<LogEntry> read(final InputStream in) {
		try {
			final SAXParser saxParser = BasicHandler.FACTORY.newSAXParser();
			final SubversionLogHandler handler = new SubversionLogHandler();

			saxParser.parse(in, handler);
			return handler.getLogs();
		} catch (final Exception e) {
			throw new SubversionException("could not parse input", e);
		}
	}

	private String comment = "";

	private Date date;

	private Path path;

	private Revision revision;

	private String user;

	LogEntry() {
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
		final LogEntry other = (LogEntry) obj;
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		if (revision == null) {
			if (other.revision != null) {
				return false;
			}
		} else if (!revision.equals(other.revision)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the commit message
	 * @return the commit message or an empty {@link String} if no commit message was specified
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Returns the time of the commit
	 * @return the time of the commit
	 */
	public Date getDate() {
		return new Date(date.getTime());
	}

	public Path getPath() {
		return path;
	}

	/**
	 * Returns the {@link Revision} that was created by the commit
	 * @return the {@link Revision} that was created by the commit
	 */
	public Revision getRevision() {
		return revision;
	}

	/**
	 * Returns the name of the user that committed changes to the repository
	 * @return the name of the user that committed changes to the repository
	 */
	public String getUser() {
		return user;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((path == null) ? 0 : path.hashCode());
		result = (prime * result) + ((revision == null) ? 0 : revision.hashCode());
		return result;
	}

	void setComment(final String comment) {
		this.comment = comment;
	}

	void setDate(final Date date) {
		this.date = new Date(date.getTime());
	}

	void setPath(final Path path) {
		this.path = path;
	}

	void setRevision(final Revision revision) {
		this.revision = revision;
	}

	void setUser(final String user) {
		this.user = user;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("SubversionLog [comment=");
		builder.append(comment);
		builder.append(", date=");
		builder.append(date);
		builder.append(", path=");
		builder.append(path);
		builder.append(", revision=");
		builder.append(revision);
		builder.append(", user=");
		builder.append(user);
		builder.append("]");
		return builder.toString();
	}
}
