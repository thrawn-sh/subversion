package de.shadowhunt.subversion.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.ResourceProperty.Type;
import de.shadowhunt.subversion.Revision;

public class ResourcePropertyLoader extends BaseLoader {

	static class ResourcePropertyHandler extends BasicHandler {

		private static Type convert(final String prefix) {
			if ("svn".equals(prefix)) {
				return Type.SVN;
			}
			throw new IllegalArgumentException("prefix " + prefix + " not supported");
		}

		private final List<ResourceProperty> properties = new ArrayList<ResourceProperty>();

		private String propertyName;

		private ResourceProperty.Type propertyType;

		@Override
		public void endElement(final String uri, final String localName, final String qName) throws SAXException {
			final String name = getNameFromQName(qName);

			if ("property".equals(name)) {
				properties.add(new ResourceProperty(propertyType, propertyName, getText()));
			}
		}

		List<ResourceProperty> getResourceProperties() {
			return properties;
		}

		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
			clearText();

			final String name = getNameFromQName(qName);

			if ("property".equals(name)) {
				final String value = attributes.getValue("name");
				final int split = value.indexOf(':');
				propertyType = convert(value.substring(0, split));
				propertyName = value.substring(split + 1);
				return;
			}
		}
	}

	private static final String SUFFIX = ".proplist";

	public static ResourceProperty[] load(final Resource resource, final Revision revision) throws Exception {
		final File file = new File(ROOT, resolve(revision) + resource.getValue() + SUFFIX);

		final SAXParser saxParser = BasicHandler.FACTORY.newSAXParser();
		final ResourcePropertyHandler handler = new ResourcePropertyHandler();

		saxParser.parse(file, handler);
		final List<ResourceProperty> list = handler.getResourceProperties();
		return list.toArray(new ResourceProperty[list.size()]);
	}
}
