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
package de.shadowhunt.subversion.internal.jaxb.converter;

import javax.annotation.CheckForNull;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.ResourceProperty.Key;
import de.shadowhunt.subversion.ResourceProperty.Type;
import de.shadowhunt.subversion.internal.ResourcePropertyUtils;
import de.shadowhunt.subversion.internal.XmlConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ResourcePropertyAdapter extends XmlAdapter<Element, ResourceProperty> {

    @CheckForNull
    private Type getType(final String namespaceURI) {
        if (XmlConstants.SUBVERSION_CUSTOM_NAMESPACE.equals(namespaceURI)) {
            return Type.SUBVERSION_CUSTOM;
        }
        if (XmlConstants.SUBVERSION_SVN_NAMESPACE.equals(namespaceURI)) {
            return Type.SUBVERSION_SVN;
        }
        return null;
    }

    @Override
    public Element marshal(final ResourceProperty v) throws Exception {
        throw new UnsupportedOperationException("marshalling not supported");
    }

    @Override
    public ResourceProperty unmarshal(final Element element) throws Exception {
        if (element == null) {
            return null;
        }

        final String namespaceURI = element.getNamespaceURI();
        final Type type = getType(namespaceURI);
        if (type != null) {
            final String localName = element.getLocalName();
            final String filteredLocalName = ResourcePropertyUtils.filterMarker(localName);
            final Key key = new Key(type, filteredLocalName);
            final Node child = element.getFirstChild();
            String value = "";
            if (child != null) {
                value = child.getTextContent();
            }
            return new ResourceProperty(key, value);
        }
        return null;
    }

}
