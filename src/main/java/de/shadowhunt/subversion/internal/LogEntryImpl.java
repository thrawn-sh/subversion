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

import de.shadowhunt.subversion.LogEntry;
import de.shadowhunt.subversion.Revision;

public final class LogEntryImpl implements LogEntry {

    private String author;

    private Date date;

    private String message;

    private Revision revision;

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
        final LogEntryImpl other = (LogEntryImpl) obj;
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
        final long time = date.getTime();
        return new Date(time);
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

    public void setAuthor(final String author) {
        this.author = author;
    }

    public void setDate(final Date date) {
        final long time = date.getTime();
        this.date = new Date(time);
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public void setRevision(final Revision revision) {
        this.revision = revision;
    }

    @Override
    public String toString() {
        return "LogEntry [author=" + author + ", date=" + date + ", message=" + message + ", revision=" + revision + "]";
    }

}
