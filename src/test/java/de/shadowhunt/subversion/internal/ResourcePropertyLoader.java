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

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.SAXParser;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.ResourceProperty.Type;
import de.shadowhunt.subversion.Revision;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public final class ResourcePropertyLoader extends AbstractBaseLoader {

    static class ResourcePropertyHandler extends BasicHandler {

        private final Set<ResourceProperty> properties = new TreeSet<>(ResourceProperty.TYPE_NAME_COMPARATOR);

        private String propertyName;

        private Type propertyType;

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {

            if ("property".equals(localName)) {
                properties.add(new ResourceProperty(propertyType, propertyName, getText()));
            }
        }

        Set<ResourceProperty> getResourceProperties() {
            return properties;
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
            clearText();

            if ("property".equals(localName)) {
                final String value = attributes.getValue("name");
                if (value.startsWith("svn:")) {
                    final int split = value.indexOf(':');
                    propertyType = Type.SUBVERSION_SVN;
                    propertyName = value.substring(split + 1);
                } else {
                    propertyType = Type.SUBVERSION_CUSTOM;
                    propertyName = value;
                }
                return;
            }
        }
    }

    public static final String SUFFIX = ".proplist";

    ResourcePropertyLoader(final File root, final Resource base) {
        super(root, base);
    }

    public ResourceProperty[] load(final Resource resource, final Revision revision) throws Exception {
        final File file = new File(root, resolve(revision) + base.getValue() + resource.getValue() + SUFFIX);

        final SAXParser saxParser = BasicHandler.FACTORY.newSAXParser();
        final ResourcePropertyHandler handler = new ResourcePropertyHandler();

        saxParser.parse(file, handler);
        final Set<ResourceProperty> properties = handler.getResourceProperties();
        return properties.toArray(new ResourceProperty[properties.size()]);
    }
}
