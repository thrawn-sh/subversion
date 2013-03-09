package de.shadowhunt.scm.subversion;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.helpers.DefaultHandler;

class BasicHandler extends DefaultHandler {

	private final StringBuilder buffer = new StringBuilder();

	@Override
	public final void characters(final char[] ch, final int start, final int length) {
		buffer.append(ch, start, length);
	}

	protected void clearText() {
		// clear buffer, but reuse the object and its allocated memory
		buffer.setLength(0);
	}

	protected final String getNameFromQName(final String qName) {
		if (qName == null) {
			return null;
		}
		final int index = qName.indexOf(':');
		if (index >= 0) {
			return qName.substring(index + 1);
		}
		return qName;
	}

	protected final String getNamespaceFromQName(final String qName) {
		if (qName == null) {
			return null;
		}
		final int index = qName.indexOf(':');
		if (index >= 0) {
			return qName.substring(0, index);
		}
		return qName;
	}

	protected String getText() {
		return StringEscapeUtils.unescapeXml(buffer.toString());
	}
}
