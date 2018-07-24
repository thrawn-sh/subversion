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

import java.util.Date;

import de.shadowhunt.subversion.Log;
import de.shadowhunt.subversion.Revision;

/**
 * Default implementation for {@link Log}.
 */
final class LogImpl implements Log {

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

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public Date getDate() {
        return new Date(date.getTime());
    }

    @Override
    public String getMessage() {
        return message;
    }

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
