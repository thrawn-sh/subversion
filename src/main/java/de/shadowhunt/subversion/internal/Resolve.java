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

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;

public final class Resolve {

	static class ResolveHandler extends BasicHandler {

		private Resolve entry;

		public Resolve getEntry() {
			return entry;
		}

		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
			clearText();

			final String name = getNameFromQName(qName);
			if ("location".equals(name)) {
				entry = new Resolve();

				final String version = attributes.getValue("rev");
				entry.setRevision(Revision.create(Integer.parseInt(version)));
				final String path = attributes.getValue("path");
				entry.setResource(Resource.create(path));
			}
		}
	}

	/**
	 * Reads log information for a resource from the given {@link InputStream}
	 *
	 * @param in {@link InputStream} from which the status information is read (Note: will not be closed)
	 *
	 * @return {@link de.shadowhunt.subversion.internal.LogImpl} for the resource
	 */
	public static Resolve read(final InputStream in) {
		try {
			final SAXParser saxParser = BasicHandler.FACTORY.newSAXParser();
			final ResolveHandler handler = new ResolveHandler();

			saxParser.parse(in, handler);
			return handler.getEntry();
		} catch (final Exception e) {
			throw new SubversionException("could not parse input", e);
		}
	}

	private Resource resource;

	private Revision revision;

	Resolve() {
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
		final Resolve other = (Resolve) obj;
		if (resource == null) {
			if (other.resource != null) {
				return false;
			}
		} else if (!resource.equals(other.resource)) {
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

	public Resource getResource() {
		return resource;
	}

	public Revision getRevision() {
		return revision;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((resource == null) ? 0 : resource.hashCode());
		result = (prime * result) + ((revision == null) ? 0 : revision.hashCode());
		return result;
	}

	void setResource(final Resource resource) {
		this.resource = resource;
	}

	void setRevision(final Revision revision) {
		this.revision = revision;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Resolve [resource=");
		builder.append(resource);
		builder.append(", revision=");
		builder.append(revision);
		builder.append("]");
		return builder.toString();
	}
}
