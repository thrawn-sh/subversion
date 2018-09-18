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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import de.shadowhunt.subversion.Log;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.xml.AbstractSaxExpression;
import de.shadowhunt.subversion.xml.AbstractSaxExpressionHandler;
import de.shadowhunt.subversion.xml.SaxExpression;
import org.xml.sax.SAXException;

final class LogImplReader {

    private static class CommentExpression extends AbstractSaxExpression<String> {

        private static final Optional<String> EMPTY = Optional.of("");

        private static final QName[] PATH = { new QName(XmlConstants.DAV_NAMESPACE, "comment") };

        private Optional<String> comment = EMPTY;

        CommentExpression() {
            super(PATH);
        }

        @Override
        public Optional<String> getValue() {
            return comment;
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            comment = Optional.of(text);
        }

        @Override
        public void resetHandler() {
            comment = EMPTY;
        }
    }

    private static class CreatorExpression extends AbstractSaxExpression<String> {

        private static final QName[] PATH = { new QName(XmlConstants.DAV_NAMESPACE, "creator-displayname") };

        private Optional<String> creator = Optional.empty();

        CreatorExpression() {
            super(PATH);
        }

        @Override
        public Optional<String> getValue() {
            return creator;
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            creator = Optional.of(text);
        }

        @Override
        public void resetHandler() {
            creator = Optional.empty();
        }
    }

    private static class DateExpression extends AbstractSaxExpression<Date> {

        public static final QName[] PATH = { new QName(XmlConstants.SVN_NAMESPACE, "date") };

        private Optional<Date> date = Optional.empty();

        DateExpression() {
            super(PATH);
        }

        @Override
        public Optional<Date> getValue() {
            return date;
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            final Date createdDate = DateUtils.parseCreatedDate(text);
            date = Optional.of(createdDate);
        }

        @Override
        public void resetHandler() {
            date = Optional.empty();
        }
    }

    private static class LogExpression extends AbstractSaxExpression<List<Log>> {

        private static final QName[] PATH;

        static {
            final QName logReportQname = new QName(XmlConstants.SVN_NAMESPACE, "log-report");
            final QName logItemQname = new QName(XmlConstants.SVN_NAMESPACE, "log-item");
            PATH = new QName[] { logReportQname, logItemQname };
        }

        private static SaxExpression<?>[] createExpressions() {
            final CommentExpression commentExpression = new CommentExpression();
            final CreatorExpression creatorExpression = new CreatorExpression();
            final DateExpression dateExpression = new DateExpression();
            final RevisionExpression revisionExpression = new RevisionExpression();
            return new SaxExpression[] { commentExpression, creatorExpression, dateExpression, revisionExpression };
        }

        private List<Log> entries = new ArrayList<>();

        LogExpression() {
            super(PATH, createExpressions());
        }

        @Override
        public void clear() {
            entries = new ArrayList<>();
        }

        @Override
        public Optional<List<Log>> getValue() {
            return Optional.of(entries);
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            final LogImpl log = new LogImpl();
            final Optional<String> comment = ((CommentExpression) children[0]).getValue();
            final String message = comment.get();
            log.setMessage(message);
            final Optional<String> creator = ((CreatorExpression) children[1]).getValue();
            final String author = creator.get();
            log.setAuthor(author);
            final Optional<Date> date = ((DateExpression) children[2]).getValue();
            final Date dateValue = date.get();
            log.setDate(dateValue);
            final Optional<Revision> revision = ((RevisionExpression) children[3]).getValue();
            final Revision revisionValue = revision.get();
            log.setRevision(revisionValue);
            entries.add(log);
        }
    }

    private static class LogHandler extends AbstractSaxExpressionHandler<List<Log>> {

        LogHandler() {
            super(new LogExpression());
        }

        @Override
        public Optional<List<Log>> getValue() {
            final LogExpression expression = (LogExpression) expressions[0];
            final Optional<List<Log>> logs = expression.getValue();
            expression.clear();
            return logs;
        }
    }

    private static class RevisionExpression extends AbstractSaxExpression<Revision> {

        public static final QName[] PATH;

        static {
            final QName versioNameQName = new QName(XmlConstants.DAV_NAMESPACE, "version-name");
            PATH = new QName[] { versioNameQName };
        }

        private Optional<Revision> revision = Optional.empty();

        RevisionExpression() {
            super(PATH);
        }

        @Override
        public Optional<Revision> getValue() {
            return revision;
        }

        @Override
        protected void processEnd(final String nameSpaceUri, final String localName, final String text) {
            final int version = Integer.parseInt(text);
            final Revision create = Revision.create(version);
            revision = Optional.of(create);
        }

        @Override
        public void resetHandler() {
            revision = Optional.empty();
        }
    }

    /**
     * Reads log information for a resource from the given {@link java.io.InputStream}.
     *
     * @param inputStream
     *            {@link java.io.InputStream} from which the status information is read (Note: will not be closed)
     *
     * @return {@link Log} for the resource
     */
    static List<Log> read(final InputStream inputStream) throws IOException {
        final LogHandler handler = new LogHandler();
        try {
            final Optional<List<Log>> logs = handler.parse(inputStream);
            return logs.get();
        } catch (final ParserConfigurationException | SAXException e) {
            throw new SubversionException("Invalid server response: could not parse response", e);
        }
    }

    private LogImplReader() {
        // prevent instantiation
    }
}
