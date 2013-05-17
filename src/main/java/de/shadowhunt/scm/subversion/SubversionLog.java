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
				final int revision = Integer.parseInt(getText());
				current.setRevision(revision);
				return;
			}
		}

		List<SubversionLog> getLogs() {
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

	/**
	 * Reads log information for a resource from the given {@link InputStream}
	 * @param in {@link InputStream} from which the status information is read (Note: will not be closed)
	 * @return {@link SubversionLog} for the resource
	 */
	public static List<SubversionLog> read(final InputStream in) {
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

	private int revision;

	private String user;

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
		if (revision != other.revision) {
			return false;
		}
		if (user == null) {
			if (other.user != null) {
				return false;
			}
		} else if (!user.equals(other.user)) {
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

	/**
	 * Returns the revision that was created by the commit
	 * @return the revision that was created by the commit
	 */
	public int getRevision() {
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
		result = (prime * result) + ((comment == null) ? 0 : comment.hashCode());
		result = (prime * result) + ((date == null) ? 0 : date.hashCode());
		result = (prime * result) + revision;
		result = (prime * result) + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	void setComment(final String comment) {
		this.comment = comment;
	}

	void setDate(final Date date) {
		this.date = new Date(date.getTime());
	}

	void setRevision(final int revision) {
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
		builder.append(", revision=");
		builder.append(revision);
		builder.append(", user=");
		builder.append(user);
		builder.append("]");
		return builder.toString();
	}
}
