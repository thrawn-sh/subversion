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
package de.shadowhunt.subversion;

import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.helpers.DefaultHandler;

class BasicHandler extends DefaultHandler {

	protected static final SAXParserFactory FACTORY;

	static {
		FACTORY = SAXParserFactory.newInstance();
		FACTORY.setNamespaceAware(false);
		FACTORY.setValidating(false);
	}

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
		final int index = qName.indexOf(':');
		if (index >= 0) {
			return qName.substring(index + 1);
		}
		return qName;
	}

	protected final String getNamespaceFromQName(final String qName) {
		final int index = qName.indexOf(':');
		if (index >= 0) {
			return qName.substring(0, index);
		}
		return "";
	}

	protected String getText() {
		return StringEscapeUtils.unescapeXml(buffer.toString());
	}
}
