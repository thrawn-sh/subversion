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

import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Version;

class Prefix {

	static class PrefixHandler extends BasicHandler {

		private Resource prefix;

		private final Version version;

		public PrefixHandler(final Version version) {
			this.version = version;
		}

		@Override
		public void endElement(final String uri, final String localName, final String qName) throws SAXException {
			final String name = getNameFromQName(qName);

			if ((Version.HTTPv1 == version) || (Version.HTTPv2 == version)) {
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
