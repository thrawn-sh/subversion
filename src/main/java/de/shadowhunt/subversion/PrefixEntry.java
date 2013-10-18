package de.shadowhunt.subversion;

import java.io.InputStream;

import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class PrefixEntry {

	static class PrefixHandler extends BasicHandler {

		private Resource prefix;

		private final Version version;

		public PrefixHandler(final Version version) {
			this.version = version;
		}

		@Override
		public void endElement(final String uri, final String localName, final String qName) throws SAXException {
			final String name = getNameFromQName(qName);

			if (Version.HTTPv1 == version) {
				if ("href".equals(name)) {
					final String text = getText();
					// .../${svn}/act/
					//      ^^^^^^ <- prefix
					final String[] segments = text.split("/");
					prefix = Resource.create(segments[segments.length - 2]);
					return;
				}
			}
		}

		Resource getPrefix() {
			return prefix;
		}

		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
			clearText();
		}
	}

	public static Resource read(final InputStream in, final Version version) {
		try {
			final SAXParser saxParser = BasicHandler.FACTORY.newSAXParser();
			final PrefixHandler handler = new PrefixHandler(version);

			saxParser.parse(in, handler);
			return handler.getPrefix();
		} catch (final Exception e) {
			throw new SubversionException("could not parse input", e);
		}
	}
}
