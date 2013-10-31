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

import java.io.InputStream;
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
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;

/**
 * Container that holds all log information for a single revision of a resource
 */
public final class LogImpl implements Log {

	private static class SubversionLogHandler extends BasicHandler {

		private static final TimeZone ZULU = TimeZone.getTimeZone("ZULU");

		private LogImpl current = null;

		private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);

		private final List<LogImpl> logs = new ArrayList<LogImpl>();

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
				current.setMessage(getText());
				return;
			}

			if ("creator-displayname".equals(name)) {
				current.setAuthor(getText());
				return;
			}

			if ("date".equals(name)) {
				try {
					format.setTimeZone(ZULU);
					String time = getText();
					final int index = time.indexOf('.');
					if (index > 0) {
						time = time.substring(0, index + 4); // remove nanoseconds
					}
					final Date date = format.parse(time);
					current.setDate(date);
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

		List<LogImpl> getLogs() {
			return logs;
		}

		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
			clearText();

			final String name = getNameFromQName(qName);

			if ("log-item".equals(name)) {
				current = new LogImpl();
				return;
			}
		}
	}

	/**
	 * Reads log information for a resource from the given {@link InputStream}
	 *
	 * @param in {@link InputStream} from which the status information is read (Note: will not be closed)
	 *
	 * @return {@link LogImpl} for the resource
	 */
	public static List<LogImpl> read(final InputStream in) {
		try {
			final SAXParser saxParser = BasicHandler.FACTORY.newSAXParser();
			final SubversionLogHandler handler = new SubversionLogHandler();

			saxParser.parse(in, handler);
			return handler.getLogs();
		} catch (final Exception e) {
			throw new SubversionException("could not parse input", e);
		}
	}

	private String author = null;

	private Date date = null;

	private String message = "";

	private Revision revision = null;

	LogImpl() {
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
		final LogImpl other = (LogImpl) obj;
		if (author == null) {
			if (other.author != null) {
				return false;
			}
		} else if (!author.equals(other.author)) {
			return false;
		}
		if (date == null) {
			if (other.date != null) {
				return false;
			}
		} else if (!date.equals(other.date)) {
			return false;
		}
		if (message == null) {
			if (other.message != null) {
				return false;
			}
		} else if (!message.equals(other.message)) {
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
	 * Returns the name of the author that committed changes to the repository
	 *
	 * @return the name of the author that committed changes to the repository
	 */
	@Override
	public String getAuthor() {
		return author;
	}

	/**
	 * Returns the time of the commit
	 *
	 * @return the time of the commit
	 */
	@Override
	public Date getDate() {
		return new Date(date.getTime());
	}

	/**
	 * Returns the commit message
	 *
	 * @return the commit message or an empty {@link String} if no commit message was specified
	 */
	@Override
	public String getMessage() {
		return message;
	}

	/**
	 * Returns the {@link Revision} that was created by the commit
	 *
	 * @return the {@link Revision} that was created by the commit
	 */
	@Override
	public Revision getRevision() {
		return revision;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((author == null) ? 0 : author.hashCode());
		result = (prime * result) + ((date == null) ? 0 : date.hashCode());
		result = (prime * result) + ((message == null) ? 0 : message.hashCode());
		result = (prime * result) + ((revision == null) ? 0 : revision.hashCode());
		return result;
	}

	void setAuthor(final String author) {
		this.author = author;
	}

	void setDate(final Date date) {
		this.date = new Date(date.getTime());
	}

	void setMessage(final String message) {
		this.message = message;
	}

	void setRevision(final Revision revision) {
		this.revision = revision;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Log [author=");
		builder.append(author);
		builder.append(", date=");
		builder.append(date);
		builder.append(", message=");
		builder.append(message);
		builder.append(", revision=");
		builder.append(revision);
		builder.append(']');
		return builder.toString();
	}
}
