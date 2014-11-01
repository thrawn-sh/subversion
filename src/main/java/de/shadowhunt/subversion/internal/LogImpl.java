/**
 * Copyright (C) 2013 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.shadowhunt.subversion.internal;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;

import org.xml.sax.Attributes;

import de.shadowhunt.subversion.Log;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.xml.AbstractSaxExpression;
import de.shadowhunt.subversion.xml.AbstractSaxExpressionHandler;
import de.shadowhunt.subversion.xml.SaxExpression;

/**
 * Default implementation for {@link Log}
 */
final class LogImpl implements Log {

    private static class CommentExpression extends AbstractSaxExpression<String> {

        private static final QName[] PATH = { new QName(XmlConstants.DAV_NAMESPACE, "comment") };

        private String comment = "";

        CommentExpression() {
            super(PATH);
        }

        @Override
        public String getValue() {
            return comment;
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            comment = text;
        }

        @Override
        public void resetHandler() {
            comment = "";
        }
    }

    private static class CreatorExpression extends AbstractSaxExpression<String> {

        private static final QName[] PATH = { new QName(XmlConstants.DAV_NAMESPACE, "creator-displayname") };

        private String creator = "";

        CreatorExpression() {
            super(PATH);
        }

        @Override
        public String getValue() {
            return creator;
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            creator = text;
        }

        @Override
        public void resetHandler() {
            creator = "";
        }
    }

    private static class DateExpression extends AbstractSaxExpression<Date> {

        public static final QName[] PATH = { new QName(XmlConstants.SVN_NAMESPACE, "date") };

        private Date date = null;

        DateExpression() {
            super(PATH);
        }

        @Override
        public Date getValue() {
            return date;
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            date = DateUtils.parseCreatedDate(text);
        }

        @Override
        public void resetHandler() {
            date = null;
        }
    }

    private static class LogExpression extends AbstractSaxExpression<List<Log>> {

        private static final SaxExpression[] CHILDREN = { //
                new CommentExpression(), //
                new CreatorExpression(), //
                new DateExpression(), //
                new RevisionExpression() //
        };

        private static final QName[] PATH = { //
                new QName(XmlConstants.SVN_NAMESPACE, "log-report"), //
                new QName(XmlConstants.SVN_NAMESPACE, "log-item") //
        };

        private List<Log> entries = new ArrayList<Log>();

        LogExpression() {
            super(PATH, CHILDREN);
        }

        @Override
        public void clear() {
            entries = new ArrayList<Log>();
        }

        @Override
        public List<Log> getValue() {
            return entries;
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            final LogImpl log = new LogImpl();
            log.setMessage(((CommentExpression) children[0]).getValue());
            log.setAuthor(((CreatorExpression) children[1]).getValue());
            log.setDate(((DateExpression) children[2]).getValue());
            log.setRevision(((RevisionExpression) children[3]).getValue());
            entries.add(log);
        }
    }

    private static class LogHandler extends AbstractSaxExpressionHandler<List<Log>> {

        LogHandler() {
            super(new LogExpression());
        }

        @Override
        public List<Log> getValue() {
            final LogExpression expression = (LogExpression) expressions[0];
            final List<Log> logs = expression.getValue();
            expression.clear();
            return logs;
        }
    }

    private static class RevisionExpression extends AbstractSaxExpression<Revision> {

        public static final QName[] PATH = { new QName(XmlConstants.DAV_NAMESPACE, "version-name") };

        private Revision revision = null;

        RevisionExpression() {
            super(PATH);
        }

        @Override
        public Revision getValue() {
            return revision;
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            final int revision = Integer.parseInt(text);
            this.revision = Revision.create(revision);
        }

        @Override
        protected void processStart(final String nameSpaceUri, final String localName, final Attributes attributes) {
            // nothing to do
        }

        @Override
        public void resetHandler() {
            revision = null;
        }
    }

    /**
     * Reads log information for a resource from the given {@link InputStream}
     *
     * @param inputStream {@link InputStream} from which the status information is read (Note: will not be closed)
     *
     * @return {@link Log} for the resource
     */
    static List<Log> read(final InputStream inputStream) {
        try {
            final LogHandler handler = new LogHandler();
            return handler.parse(inputStream);
        } catch (final Exception e) {
            throw new SubversionException("Invalid server response: could not parse response", e);
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
