/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2019 shadowhunt (dev@shadowhunt.de)
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
@XmlSchema(elementFormDefault = XmlNsForm.QUALIFIED, //
        xmlns = { //
                @XmlNs(prefix = XmlConstants.DAV_PREFIX, namespaceURI = XmlConstants.DAV_NAMESPACE), //
                @XmlNs(prefix = XmlConstants.SVN_PREFIX, namespaceURI = XmlConstants.SVN_NAMESPACE), //
                @XmlNs(prefix = XmlConstants.SUBVERSION_CUSTOM_PREFIX, namespaceURI = XmlConstants.SUBVERSION_CUSTOM_NAMESPACE), //
                @XmlNs(prefix = XmlConstants.SUBVERSION_DAV_PREFIX, namespaceURI = XmlConstants.SUBVERSION_DAV_NAMESPACE), //
                @XmlNs(prefix = XmlConstants.SUBVERSION_SVN_PREFIX, namespaceURI = XmlConstants.SUBVERSION_SVN_NAMESPACE) //
        })
@de.shadowhunt.annotation.ReturnValuesAreNonnullByDefault
@javax.annotation.ParametersAreNonnullByDefault
package de.shadowhunt.subversion.internal.jaxb;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;

import de.shadowhunt.subversion.internal.XmlConstants;
