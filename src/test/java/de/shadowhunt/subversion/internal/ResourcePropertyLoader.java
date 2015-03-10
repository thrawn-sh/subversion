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

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.ResourceProperty.Type;
import de.shadowhunt.subversion.Revision;

public final class ResourcePropertyLoader extends BaseLoader {

    public static final String SUFFIX = ".proplist";

    static class ResourcePropertyHandler extends BasicHandler {

        private final Set<ResourceProperty> properties = new TreeSet<ResourceProperty>(ResourceProperty.TYPE_NAME_COMPARATOR);

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

    ResourcePropertyLoader(final File root) {
        super(root);
    }

    public ResourceProperty[] load(final Resource resource, final Revision revision) throws Exception {
        final File file = new File(root, resolve(revision) + resource.getValue() + SUFFIX);

        final SAXParser saxParser = BasicHandler.FACTORY.newSAXParser();
        final ResourcePropertyHandler handler = new ResourcePropertyHandler();

        saxParser.parse(file, handler);
        final Set<ResourceProperty> properties = handler.getResourceProperties();
        return properties.toArray(new ResourceProperty[properties.size()]);
    }
}
