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

final class LogImplReader {

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
     * Reads log information for a resource from the given {@link java.io.InputStream}
     *
     * @param inputStream {@link java.io.InputStream} from which the status information is read (Note: will not be closed)
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

    private LogImplReader() {
        // prevent instantiation
    }
}
