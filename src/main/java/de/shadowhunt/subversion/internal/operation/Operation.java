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
package de.shadowhunt.subversion.internal.operation;

import javax.annotation.Nullable;
import javax.xml.stream.XMLOutputFactory;

import de.shadowhunt.subversion.internal.XmlConstants;
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.ContentType;

/**
 * An {@code Operation} represents a single HTTP request/response pair
 */
public interface Operation<E> extends ResponseHandler<E> {

    ContentType CONTENT_TYPE_XML = ContentType.create("text/xml", XmlConstants.ENCODING);

    XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();

    /**
     * Send the current {@code Operation} to the Subversion server and process the response
     *
     * @return the processed response
     */
    @Nullable
    E execute();
}
